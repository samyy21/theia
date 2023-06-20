package com.paytm.pgplus.biz.utils;

import static com.paytm.pgplus.biz.utils.BizConstant.*;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.PER_MID_LIMIT_IDENTIFIER;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;
import com.paytm.pgplus.biz.core.model.request.ExtendedInfoRequestBean;
import com.paytm.pgplus.common.enums.EventNameEnum;
import com.paytm.pgplus.common.model.MerchantLimitBreachedResponse;
import com.paytm.pgplus.logging.ExtendedLogger;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import com.paytm.pgplus.cache.model.MerchantTxnLimitInfoResponse;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.velocity.enums.LimitType;
import com.paytm.pgplus.facade.velocity.enums.Operation;
import com.paytm.pgplus.facade.velocity.models.MerchantLimitRequest;
import com.paytm.pgplus.facade.velocity.models.MerchantLimitResponse;
import com.paytm.pgplus.facade.velocity.services.IMerchantLimit;
import com.paytm.pgplus.mappingserviceclient.exception.MappingServiceClientException;
import com.paytm.pgplus.mappingserviceclient.service.IMerchantTxnLimits;

@Component
public class MerchantVelocityUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(MerchantVelocityUtil.class);
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(MerchantVelocityUtil.class);

    @Autowired
    @Qualifier("merchantLimitImpl")
    private IMerchantLimit merchantLimitImpl;

    @Autowired
    private IMerchantTxnLimits merchantTxnLimitServiceImpl;

    public MerchantLimitBreachedResponse checkMerchantLimitBreached(final String mid, final String txnAmtInRupees,
            final ExtendedInfoRequestBean extendedInfoRequestBean) {
        MerchantLimitBreachedResponse merchantLimitBreachedResponse = new MerchantLimitBreachedResponse();
        String velocityLimitEnabledFlag = ConfigurationUtil.getProperty("velocity.limit.enabled.flag",
                VELOCITY_LIMIT_ENABLED_FLAG_FALSE);
        LOGGER.debug("Velocity limit enabled flag set to {}", velocityLimitEnabledFlag);

        if (VELOCITY_LIMIT_ENABLED_FLAG_TRUE.equalsIgnoreCase(velocityLimitEnabledFlag)) {
            MerchantLimitResponse merchantLimitResponse = checkMerchantLimitBreachedAndUpdate(mid, txnAmtInRupees,
                    extendedInfoRequestBean);
            if (merchantLimitResponse != null) {
                createMerchantLimitBreachedResponse(merchantLimitResponse, merchantLimitBreachedResponse);
            }
        }
        return merchantLimitBreachedResponse;
    }

    private MerchantLimitResponse checkMerchantLimitBreachedAndUpdate(final String mid, final String txnAmtInRupees,
            final ExtendedInfoRequestBean extendedInfoRequestBean) {
        MerchantTxnLimitInfoResponse merchantTxnLimitInfoResponse = getMerchantTxnLimitInfoConfigFromMappingService(
                mid, extendedInfoRequestBean);
        if (merchantTxnLimitInfoResponse == null) {
            return null;
        }
        List<MerchantTxnLimitInfoResponse.TxnLimitInfo> perMidLimitInfos = filterPerMidTxnLimitInfos(merchantTxnLimitInfoResponse);
        if (CollectionUtils.isEmpty(perMidLimitInfos)) {
            LOGGER.info("No velocity limits configured for MID = {}", mid);
            extendedInfoRequestBean.setMerchantLimitEnabled(false);
        } else if (perMidLimitInfos.size() != 1) {
            LOGGER.error("PER_MID limits active on merchant not equal to 1, with size {}", perMidLimitInfos.size());
            // LOGGER.info("For MID = {}, not calling velocity limit service",
            // mid);
            extendedInfoRequestBean.setMerchantLimitEnabled(false);
        } else {
            return updateMerchantLimit(mid, txnAmtInRupees, extendedInfoRequestBean);
        }
        return null;
    }

    public boolean rollbackMerchantVelocityLimitUpdate(String mid, String txnAmtInRupees,
            ExtendedInfoRequestBean extendedInfoRequestBean) {
        return rollbackMerchantLimit(mid, txnAmtInRupees, extendedInfoRequestBean);
    }

    private MerchantLimitResponse updateMerchantLimit(String mid, String txnAmtInRupees,
            ExtendedInfoRequestBean extendedInfoRequestBean) {

        extendedInfoRequestBean.setMerchantLimitEnabled(true);
        MerchantLimitRequest merchantLimitRequest = getMerchantLimitRequest(mid, txnAmtInRupees, Operation.ADD);
        MerchantLimitResponse merchantLimitResponse = null;
        Map<String, String> metaData = new LinkedHashMap<>();
        try {
            merchantLimitResponse = merchantLimitImpl.changeMerchantLimit(merchantLimitRequest);
        } catch (FacadeCheckedException e) {
            LOGGER.error("Exception while calling merchant velocity limit service for MID = {}",
                    merchantLimitRequest.getMid(), e);
            /*
             * set merchant limit checked to false so that it can be updated at
             * terminal state of txn
             */
            extendedInfoRequestBean.setMerchantLimitUpdated(false);
        }

        if (merchantLimitResponse != null) {

            if (!StringUtils.isEmpty(merchantLimitResponse.getApi())
                    && merchantLimitResponse.getApi().equalsIgnoreCase(VELOCITY_SERVICE_FAIL_MESSAGE)) {
                LOGGER.error("Merchant Limit check failed for merchant with MID = {}", mid);
                extendedInfoRequestBean.setMerchantLimitUpdated(false);
            } else if (!StringUtils.isEmpty(merchantLimitResponse.getValidation())
                    && merchantLimitResponse.getValidation().equalsIgnoreCase(VELOCITY_SERVICE_FAIL_MESSAGE)) {
                LOGGER.error("Merchant Limit check failed due to invalid request for merchant with MID = {}", mid);
                extendedInfoRequestBean.setMerchantLimitUpdated(false);
            } else {
                if (merchantLimitResponse.getLimitBreached()) {
                    LOGGER.error("Limit breached for merchant with MID = {}, txnAmt = {}", mid, txnAmtInRupees);
                    metaData.put("mid", mid);
                    metaData.put("txnAmtInRupees", txnAmtInRupees);
                    metaData.put("eventMsg", "Limit breached for merchant with MID");
                    EventUtils.pushTheiaEvents(EventNameEnum.MERCHANT_LIMIT_BREACHED, metaData);
                    return merchantLimitResponse;
                } else {
                    LOGGER.info("Merchant Limit updated successfully for merchant with MID ={}, amount={}", mid,
                            txnAmtInRupees);
                    extendedInfoRequestBean.setMerchantLimitUpdated(true);
                }
            }
        }
        return null;
    }

    private boolean rollbackMerchantLimit(String mid, String txnAmtInRupees,
            ExtendedInfoRequestBean extendedInfoRequestBean) {
        if (extendedInfoRequestBean.isMerchantLimitEnabled() && extendedInfoRequestBean.isMerchantLimitUpdated()) {
            LOGGER.info("Rolling back merchant velocity limit update");
            MerchantLimitRequest merchantLimitRequest = getMerchantLimitRequest(mid, txnAmtInRupees, Operation.SUBTRACT);
            MerchantLimitResponse merchantLimitResponse = null;
            try {
                merchantLimitResponse = merchantLimitImpl.changeMerchantLimit(merchantLimitRequest);
                if (merchantLimitResponse.getOperationSuccessful()) {
                    LOGGER.info("velocity limit rollback successfull for MID = {}, Amount = {}", mid, txnAmtInRupees);
                    return true;
                } else {
                    LOGGER.error("velocity limit rollback failed for MID = {}, Amount = {}", mid, txnAmtInRupees);
                }
            } catch (FacadeCheckedException e) {
                LOGGER.error("Exception in velocity limit rollback for MID = {}, Amount = {}",
                        merchantLimitRequest.getMid(), txnAmtInRupees);
                LOGGER.error(ExceptionUtils.getStackTrace(e));
            }
            return false;
        }
        LOGGER.info("merchantLimit is not enabled or isMerchantLimitUpdated is false");
        return false;
    }

    private List<MerchantTxnLimitInfoResponse.TxnLimitInfo> filterPerMidTxnLimitInfos(
            MerchantTxnLimitInfoResponse merchantTxnLimitInfoResponse) {
        List<MerchantTxnLimitInfoResponse.TxnLimitInfo> merchantTxnLimitInfoList = merchantTxnLimitInfoResponse
                .getMerchantTxnLimitInfos();

        return merchantTxnLimitInfoList
                .stream()
                .filter(txnLimitTo -> {
                    return PER_MID_LIMIT_IDENTIFIER.equals(txnLimitTo.getLimitIdentifierType())
                            && "ACTIVE".equals(txnLimitTo.getStatus());
                }).collect(Collectors.toList());
    }

    private MerchantTxnLimitInfoResponse getMerchantTxnLimitInfoConfigFromMappingService(String mid,
            ExtendedInfoRequestBean extendedInfoRequestBean) {
        MerchantTxnLimitInfoResponse merchantTxnLimitInfoResponse = null;
        try {
            merchantTxnLimitInfoResponse = merchantTxnLimitServiceImpl.getMerchantTxnLimitInfo(mid);
            EXT_LOGGER
                    .customInfo("Mapping response - MerchantTxnLimitInfoResponse :: {}", merchantTxnLimitInfoResponse);
        } catch (MappingServiceClientException e) {
            LOGGER.error("Exception while fetching merchant txn limit info for MID = {}", mid, e);
        }
        if (merchantTxnLimitInfoResponse == null) {
            LOGGER.error("Could not check merchant txn limit info for MID = {}", mid);
            // LOGGER.info("Setting merchant enabled to true so that it can be checked again at termainal state of txn");
            extendedInfoRequestBean.setMerchantLimitEnabled(true);
            extendedInfoRequestBean.setMerchantLimitUpdated(false);
        }
        return merchantTxnLimitInfoResponse;
    }

    private MerchantLimitRequest getMerchantLimitRequest(final String mid, final String txnAmtInRupees,
            final Operation operation) {

        MerchantLimitRequest merchantLimitRequest = new MerchantLimitRequest(mid, LIMIT_TXN_TYPE_ANY, LimitType.AMOUNT,
                LIMIT_DURATION_MONTH, new Date(), txnAmtInRupees, operation);

        return merchantLimitRequest;
    }

    public void createMerchantLimitBreachedResponse(MerchantLimitResponse merchantLimitResponse,
            MerchantLimitBreachedResponse merchantLimitBreachedResponse) {
        if (merchantLimitBreachedResponse != null && merchantLimitResponse != null
                && merchantLimitResponse.getLimitBreached()) {
            merchantLimitBreachedResponse.setLimitBreached(merchantLimitResponse.getLimitBreached());
            merchantLimitBreachedResponse.setLimitDuration(merchantLimitResponse.getLimitDuration());
            merchantLimitBreachedResponse.setLimitType(merchantLimitResponse.getLimitType().getValue());
        }
    }

}
