package com.paytm.pgplus.theia.utils;

import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.cache.model.*;
import com.paytm.pgplus.common.config.ConfigurationUtil;
import com.paytm.pgplus.logging.ExtendedLogger;
import com.paytm.pgplus.mappingserviceclient.exception.MappingServiceClientException;
import com.paytm.pgplus.mappingserviceclient.service.IBankInfoDataService;
import com.paytm.pgplus.mappingserviceclient.service.ILookupDataInfoService;
import com.paytm.pgplus.mappingserviceclient.service.IMerchantDataService;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.exceptions.PaymentRequestValidationException;
import com.paytm.pgplus.theia.helper.PreRedisCacheHelper;
import com.paytm.pgplus.theia.logging.ExceptionLogUtils;
import com.paytm.pgplus.theia.models.MerchantDetails;
import com.paytm.pgplus.theia.nativ.model.payview.response.NativeCashierInfoResponse;
import com.paytm.pgplus.theia.nativ.utils.AOAUtils;
import com.paytm.pgplus.theia.offline.model.payview.PayChannelBase;
import com.paytm.pgplus.theia.offline.model.payview.PayMethod;
import com.paytm.pgplus.theia.offline.model.request.CashierInfoRequest;
import com.paytm.pgplus.theia.offline.model.response.CashierInfoResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.paytm.pgplus.theia.constants.TheiaConstant.FF4J.FEATURE_THEIA_APP_INVOKE_AS_COLLECT;

/**
 * Created by vivek kumar on 10/01/18.
 */
@Service
public class MerchantDataUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(MerchantDataUtil.class);
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(MerchantDataUtil.class);

    @Autowired
    private IMerchantDataService merchantDataServiceImpl;

    @Autowired
    private ILookupDataInfoService lookupDataServiceImpl;

    @Autowired
    private IBankInfoDataService bankInfoDataServiceImpl;

    @Autowired
    private PreRedisCacheHelper preRedisCacheHelper;

    @Autowired
    Ff4jUtils ff4jUtils;

    @Autowired
    @Qualifier("aoaUtils")
    private AOAUtils aoaUtils;

    private static final String MP_ADD_MONEY_MID = "MP.ADD.MONEY.MID";

    public String getAggregatorMid(final String mid) {
        String aggregatorMid = mid;
        try {
            final MerchantExtendedInfoResponse merchantExtendedInfo = preRedisCacheHelper.getMerchantExtendedData(mid);

            if ((null != merchantExtendedInfo) && (null != merchantExtendedInfo.getExtendedInfo())) {
                String agMid = merchantExtendedInfo.getExtendedInfo().getAggregatorMid();
                if (StringUtils.isNotEmpty(agMid)) {
                    aggregatorMid = agMid;
                }
            } else {
                LOGGER.error("merchantExtendedInfo is null");
            }
        } catch (PaymentRequestValidationException e) {
            LOGGER.error(" Error while fetching merchantExtendedInfo  ", e);
        }

        return aggregatorMid;
    }

    public void mapMerchantDataForMPINGeneration(PaymentRequestBean paymentRequestBean,
            CashierInfoResponse cashierInfoResponse) {
        List<PayMethod> merchantPayMethod = cashierInfoResponse.getBody().getPayMethodViews().getMerchantPayMethods();
        boolean upiPushEnabled = checkForUpiSpecificChannel(merchantPayMethod, TheiaConstant.BasicPayOption.UPI_PUSH);
        boolean upiPushExpressEnabled = checkForUpiSpecificChannel(merchantPayMethod,
                TheiaConstant.BasicPayOption.UPI_PUSH_EXPRESS);
        if (upiPushExpressEnabled) {
            cashierInfoResponse.getBody().setMerchantDetails(getMerchantDetails(paymentRequestBean.getMid(), true));
        } else if (upiPushEnabled) {
            cashierInfoResponse.getBody().setMerchantDetails(getMerchantDetails(paymentRequestBean.getMid(), false));
        }
    }

    private boolean checkForUpiSpecificChannel(List<PayMethod> payMethods, String upiPayChannel) {
        if (payMethods == null || payMethods.isEmpty()) {
            return false;
        }
        Iterator<PayMethod> payMethodIterator = payMethods.iterator();
        while (payMethodIterator.hasNext()) {
            PayMethod payMethod = payMethodIterator.next();
            if (payMethod.getPayChannelOptions() != null) {
                Iterator<PayChannelBase> payChannelBaseIterator = payMethod.getPayChannelOptions().iterator();
                while (payChannelBaseIterator.hasNext()) {
                    PayChannelBase payChannelBase = payChannelBaseIterator.next();
                    if (upiPayChannel.equals(payChannelBase.getPayChannelOption())
                            && payChannelBase.getIsDisabled() != null
                            && !Boolean.parseBoolean(payChannelBase.getIsDisabled().getStatus())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static Map<String, String> getParamMap(String params) {
        if (StringUtils.isBlank(params))
            return null;
        Map<String, String> map = new HashMap<String, String>();
        String[] keyValueArray = params.split(";");
        for (int i = 0; i < keyValueArray.length; i++) {
            String[] keyValuePair = keyValueArray[i].split("=");
            if (keyValuePair.length > 1)
                map.put(keyValuePair[0], keyValuePair[1]);
        }
        return map;
    }

    public MerchantDetails getMerchantDetails(String mid, boolean isUpiExpress) {
        Long bankCodeId = null;
        Long paymentId = null;
        String merchantName = "";
        try {
            MerchantInfo merchantInfo = merchantDataServiceImpl.getMerchantMappingData(mid);
            EXT_LOGGER.customInfo("Mapping response - MerchantInfo :: {}", merchantInfo);
            if (null != merchantInfo) {
                merchantName = merchantInfo.getOfficialName();
            }
            LookupDataDetails lookupDetailsPaymethod = lookupDataServiceImpl.getLookupInfoData(
                    TheiaConstant.ExtraConstants.UPI, TheiaConstant.ExtraConstants.PAYMETHOD_CATEGORY);
            EXT_LOGGER.customInfo("Mapping response - LookupDataDetails for UPI PAYMENT_MODE :: {}",
                    lookupDetailsPaymethod);

            LookupDataDetails lookupDetailsAuthMode = lookupDataServiceImpl.getLookupInfoData(
                    TheiaConstant.ExtraConstants.USRPWD, TheiaConstant.ExtraConstants.AUTH_MODE_CATEGORY);
            EXT_LOGGER.customInfo("Mapping response - LookupDataDetails for USRPWD AUTH_MODE :: {}",
                    lookupDetailsAuthMode);

            BankMasterDetails bankMaster = bankInfoDataServiceImpl
                    .getBankInfoData(isUpiExpress ? TheiaConstant.ExtraConstants.PPBEX
                            : TheiaConstant.ExtraConstants.PAYMENTS_BANK_CODE);
            EXT_LOGGER.customInfo("Mapping response - BankMasterDetails :: {}", bankMaster);

            if (null != bankMaster) {
                bankCodeId = bankMaster.getBankId();
            }
            if (null != lookupDetailsPaymethod) {
                paymentId = lookupDetailsPaymethod.getId();
            }
            Long authModeId = lookupDetailsAuthMode != null ? lookupDetailsAuthMode.getId() : null;
            MBIdConfiguration mbidData = merchantDataServiceImpl.getMBID(mid, bankCodeId, paymentId, authModeId);
            EXT_LOGGER
                    .customInfo(
                            "Mapping response - MBIdConfiguration :: {} for MID : {} BankCodeId : {} PaymentId : {} AuthModeId : {}",
                            mbidData, mid, bankCodeId, paymentId, authModeId);
            String params = mbidData.getParameter();
            Map<String, String> paramMap = getParamMap(params);
            String mcc = "";
            String merchantVpa = "";
            if (paramMap != null) {
                mcc = paramMap.get("MCC");
                merchantVpa = paramMap.get("MERCHANT_VPA");
            }
            MerchantDetails merchantDetails = new MerchantDetails(mcc, merchantVpa, merchantName);
            LOGGER.debug("Merchant Details obtained and returned for MPIN generation are : {}", merchantDetails);
            return merchantDetails;
        } catch (MappingServiceClientException e) {
            // LOGGER.error("Exception Occurred : {}", e);
            LOGGER.error("Exception Occurred : {}", ExceptionLogUtils.limitLengthOfStackTrace(e));
        }
        return null;
    }

    public void mapMerchantDataForMPINGenerationInNative(CashierInfoRequest request,
            NativeCashierInfoResponse cashierInfoResponse, WorkFlowResponseBean workFlowResponseBean) {
        List<com.paytm.pgplus.theia.nativ.model.payview.response.PayMethod> addMoneypayMethod = null;
        if (cashierInfoResponse.getBody().getAddMoneyPayOption() != null) {
            addMoneypayMethod = cashierInfoResponse.getBody().getAddMoneyPayOption().getPayMethods();
        }
        List<com.paytm.pgplus.theia.nativ.model.payview.response.PayMethod> merchantPayMethod = cashierInfoResponse
                .getBody().getMerchantPayOption().getPayMethods();

        boolean upiPushEnabled = checkForUpiSpecificChannelInNative(merchantPayMethod,
                TheiaConstant.BasicPayOption.UPI_PUSH);
        boolean upiPushExpressEnabled = checkForUpiSpecificChannelInNative(merchantPayMethod,
                TheiaConstant.BasicPayOption.UPI_PUSH_EXPRESS);

        boolean isNativeAddMoneyFlow = workFlowResponseBean != null
                && workFlowResponseBean.getWorkFlowRequestBean().isNativeAddMoney();
        String nativeAddMoneyMid = ConfigurationUtil.getProperty(MP_ADD_MONEY_MID);
        String merchantMid = request.getHead().getMid();
        /**
         * Hack to Support UPI Push for AOA
         */
        if (aoaUtils.isAOAMerchant(merchantMid)) {
            merchantMid = aoaUtils.getPgMidForAoaMid(merchantMid);
        }
        if (upiPushExpressEnabled) {
            cashierInfoResponse.getBody().setMerchantDetails(
                    getMerchantDetails(isNativeAddMoneyFlow ? nativeAddMoneyMid : merchantMid, true));
        } else if (upiPushEnabled) {
            cashierInfoResponse.getBody().setMerchantDetails(
                    getMerchantDetails(isNativeAddMoneyFlow ? nativeAddMoneyMid : merchantMid, false));
        }

        boolean addMoneyUpiPushEnabled = checkForUpiSpecificChannelInNative(addMoneypayMethod,
                TheiaConstant.BasicPayOption.UPI_PUSH);
        boolean addMoneyUpiPushExpressEnabled = checkForUpiSpecificChannelInNative(addMoneypayMethod,
                TheiaConstant.BasicPayOption.UPI_PUSH_EXPRESS);

        if (addMoneyUpiPushExpressEnabled) {
            cashierInfoResponse.getBody().setAddMoneyMerchantDetails(
                    getMerchantDetails(ConfigurationUtil.getProperty(MP_ADD_MONEY_MID), true));

            /*
             * Hack to support UPI Push in Add and Pay App Invoke case for
             * Flipkart This may break when user unchecks the wallet checkbox in
             * add and pay so it is done specific to flipkart where they sent
             * only WALLET as enabled pay mode in Initiate Txn
             */
            if (workFlowResponseBean != null
                    && workFlowResponseBean.getWorkFlowRequestBean() != null
                    && workFlowResponseBean.getUserDetails() != null
                    && ff4jUtils.isFeatureEnabledOnMidAndCustIdOrUserId(FEATURE_THEIA_APP_INVOKE_AS_COLLECT, request
                            .getHead().getMid(), workFlowResponseBean.getWorkFlowRequestBean().getCustID(),
                            workFlowResponseBean.getUserDetails().getUserId())) {
                cashierInfoResponse.getBody().setMerchantDetails(
                        cashierInfoResponse.getBody().getAddMoneyMerchantDetails());
            }
        } else if (addMoneyUpiPushEnabled) {
            cashierInfoResponse.getBody().setAddMoneyMerchantDetails(
                    getMerchantDetails(ConfigurationUtil.getProperty(MP_ADD_MONEY_MID), false));
        }

    }

    private boolean checkForUpiSpecificChannelInNative(
            List<com.paytm.pgplus.theia.nativ.model.payview.response.PayMethod> payMethods, String upiPayChannel) {
        if (payMethods == null || payMethods.isEmpty()) {
            return false;
        }
        Iterator<com.paytm.pgplus.theia.nativ.model.payview.response.PayMethod> payMethodIterator = payMethods
                .iterator();
        while (payMethodIterator.hasNext()) {
            com.paytm.pgplus.theia.nativ.model.payview.response.PayMethod payMethod = payMethodIterator.next();
            if (payMethod.getPayChannelOptions() != null) {
                Iterator<com.paytm.pgplus.theia.nativ.model.payview.response.PayChannelBase> payChannelBaseIterator = payMethod
                        .getPayChannelOptions().iterator();
                while (payChannelBaseIterator.hasNext()) {
                    com.paytm.pgplus.theia.nativ.model.payview.response.PayChannelBase payChannelBase = payChannelBaseIterator
                            .next();
                    if (upiPayChannel.equals(payChannelBase.getPayChannelOption())
                            && payChannelBase.getIsDisabled() != null
                            && !Boolean.parseBoolean(payChannelBase.getIsDisabled().getStatus())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
