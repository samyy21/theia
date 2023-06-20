package com.paytm.pgplus.theia.utils;

import com.paytm.pgplus.biz.mapping.models.EntityIgnoreParamsData;
import com.paytm.pgplus.biz.mapping.models.EntityIgnoreParamsDataResponse;
import com.paytm.pgplus.biz.mapping.models.PaytmDefaultValuesData;
import com.paytm.pgplus.checksum.ValidateChecksum;
import com.paytm.pgplus.checksum.exception.SecurityException;
import com.paytm.pgplus.checksum.model.CheckSumInput;
import com.paytm.pgplus.checksum.utils.AESMerchantService;
import com.paytm.pgplus.common.config.ConfigurationUtil;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.facade.constants.FacadeConstants;
import com.paytm.pgplus.logging.ExtendedLogger;
import com.paytm.pgplus.payloadvault.dynamicwrapper.enums.API;
import com.paytm.pgplus.payloadvault.dynamicwrapper.enums.PayloadType;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.theia.cache.IConfigurationDataService;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.constants.TheiaConstant.RequestTypes;
import com.paytm.pgplus.theia.datamapper.impl.BizRequestResponseMapperImpl;
import com.paytm.pgplus.theia.models.bin.BinDetailsRequest;
import com.paytm.pgplus.theia.services.helper.EncryptedParamsRequestServiceHelper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.Map.Entry;

import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.MerchantExtendedInfo.MerchantExtendInfoKeys.*;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.*;

@Component
public class ChecksumService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChecksumService.class);
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(ChecksumService.class);
    private final String SKIP_CHECKSUM_VALIDATION = "N";
    @Autowired
    EncryptedParamsRequestServiceHelper encParamRequestService;

    @Autowired
    Environment env;

    @Autowired
    @Qualifier(value = "merchantPreferenceService")
    private IMerchantPreferenceService merchantPreferenceService;

    @Autowired
    private MerchantExtendInfoUtils merchantExtendInfoUtils;

    @Autowired
    @Qualifier("configurationDataService")
    private IConfigurationDataService configurationDataService;

    @Autowired
    DynamicWrapperUtil dynamicWrapperUtil;

    @Autowired
    @Qualifier("merchantPreferenceProvider")
    private MerchantPreferenceProvider merchantPreferenceProvider;

    @Autowired
    private AESMerchantService aesMerchantService;

    public boolean validateChecksum(PaymentRequestBean requestData) {
        String valChkSum = ConfigurationUtil.getProperty("skip.checksum.validation", "Y");
        boolean isWrappedRequest = dynamicWrapperUtil.isDynamicWrapperEnabled()
                && dynamicWrapperUtil.isDynamicWrapperConfigPresent(requestData.getMid(), API.PROCESS_TRANSACTION,
                        PayloadType.REQUEST);
        boolean isWrappedRequestByParentMID = dynamicWrapperUtil.isDynamicWrapperEnabled()
                && requestData.getExtraParamsMap() != null
                && (StringUtils.equals((String) requestData.getExtraParamsMap().get(WRAPPER_NAME), SBMOPS_WRAPPER))
                && dynamicWrapperUtil.isDynamicWrapperConfigPresent(
                        (String) requestData.getExtraParamsMap().get(AGG_MID_WRAPPER), API.PROCESS_TRANSACTION,
                        PayloadType.REQUEST);
        if ((isWrappedRequest || isWrappedRequestByParentMID) && requestData.isMerchantVerifiedChecksum()) {
            return requestData.isChecksumVerificationResult();
        }

        /* Hack to support offline and link based payment - 07/12/17 */
        if (RequestTypes.OFFLINE.equals(requestData.getRequestType())
                || RequestTypes.DYNAMIC_QR.equals(requestData.getRequestType())
                || RequestTypes.DYNAMIC_QR_2FA.equals(requestData.getRequestType())
                || BizRequestResponseMapperImpl.isQRCodeRequest(requestData)) {
            LOGGER.warn("Checksum is not enabled for requestType : {}", requestData.getRequestType());
            return true;
        }

        if (!merchantPreferenceProvider.isChecksumEnabled(requestData)) {
            LOGGER.error("Checksum is not enabled for merchant : {}", requestData.getMid());

            if (SKIP_CHECKSUM_VALIDATION.equals(valChkSum)) {
                return false;
            } else {
                /** Hack to support merchant testing */
                return true;
            }
        }

        if (StringUtils.isBlank(requestData.getChecksumhash())) {
            LOGGER.warn("Checksum not received for Transaction.");
            return false;
        }

        Map<String, String> paramMap = new TreeMap<>();
        // CLW_APP_PAY exist only for Fast-Forward functionality
        if (requestData.getRequestType() != null
                && requestData.getRequestType().equals(
                        com.paytm.pgplus.theia.constants.TheiaConstant.RequestTypes.AUTO_DEBIT)) {
            StringBuilder chkSumBuilder = new StringBuilder();

            String appendedValue = checkSumStringForAutoDebit(requestData, chkSumBuilder);

            requestData.setChecksumString(appendedValue);
        } else if (!requestData.getChecksumMap().isEmpty()) {

            /*
             * Email Invoice Case , Seamless S2S
             */
            EntityIgnoreParamsDataResponse entityIgnoreParamsResponse = getEntityIgnoreParams(requestData);
            for (Entry<String, String> mapKeys : requestData.getChecksumMap().entrySet()) {
                if (!checkIfParamIgnored(requestData, mapKeys.getKey(), entityIgnoreParamsResponse)) {
                    paramMap.put(mapKeys.getKey(), mapKeys.getValue());
                }

            }
        } else if (requestData.getRequestType() != null
                && (requestData.getRequestType().equals(RequestTypes.LINK_BASED_PAYMENT) || requestData
                        .getRequestType().equals(RequestTypes.LINK_BASED_PAYMENT_INVOICE))) {

            paramMap.put(TheiaConstant.RequestParams.MID, requestData.getMid());
            paramMap.put(TheiaConstant.RequestParams.CHANNEL_ID, requestData.getChannelId());
            paramMap.put(TheiaConstant.RequestParams.THEME, requestData.getTheme());
            paramMap.put(TheiaConstant.RequestParams.WEBSITE, requestData.getWebsite());
            paramMap.put(TheiaConstant.RequestParams.INDUSTRY_TYPE_ID, requestData.getIndustryTypeId());
            paramMap.put(TheiaConstant.RequestParams.REQUEST_TYPE, requestData.getRequestType());

        } else if ((isWrappedRequest || isWrappedRequestByParentMID)
                && StringUtils.isNotBlank(requestData.getChecksumString())) {
            // Do nothing in case the request is wrapped through dynamicwrapper
            // and contains a checksumString
        } else {
            // 1. check if not part of entity ignore param 2.fetch paytmdefault
            // values for only those which are not part of entity ignore params
            // 3. if paytmdefaultvalue is not null then only add in paramMap.
            List<String> requestParamList = new ArrayList<String>();
            Map<String, String> requestMap = new HashMap<String, String>();
            Optional<List<PaytmDefaultValuesData>> paytmDefaultValuesData;
            EntityIgnoreParamsDataResponse entityIgnoreParamsResponse = getEntityIgnoreParams(requestData);

            for (Entry<String, String[]> requestParamsEntry : requestData.getRequest().getParameterMap().entrySet()) {
                if (!checkIfParamIgnored(requestData, requestParamsEntry.getKey(), entityIgnoreParamsResponse)) {
                    requestParamList.add(requestParamsEntry.getKey());
                }
            }
            if (!CollectionUtils.isEmpty(requestParamList)) {
                paytmDefaultValuesData = configurationDataService.getPaytmDefaultValues(requestParamList);
                if (paytmDefaultValuesData.isPresent()) {
                    paytmDefaultValuesData.get().forEach(
                            paytmDefaultValueData -> {
                                if (requestData.getRequest().getParameterMap()
                                        .containsKey(paytmDefaultValueData.getFieldName())) {
                                    paramMap.put(paytmDefaultValueData.getFieldName(), requestData.getRequest()
                                            .getParameterMap().get(paytmDefaultValueData.getFieldName())[0]);
                                }
                            });
                }
            }
        }
        if (requestData.getRequestType() != null && RequestTypes.DEFAULT_MF.equals(requestData.getRequestType())) {
            LOGGER.info("Adding account Number {} in checksum for {}", requestData.getAccountNumber(),
                    requestData.getRequestType());
            paramMap.put(FacadeConstants.ACCOUNT_NUMBER, requestData.getAccountNumber());
        }

        if (StringUtils.isNotBlank(requestData.getCorporateCustId())) {
            EXT_LOGGER.customInfo("Adding Specfic paramater for Corporate Advance account in checksum");
            paramMap.put(CORPORATE_CUST_ID, requestData.getCorporateCustId());
            paramMap.put(BID, requestData.getbId());
            paramMap.put(TEMPLATE_ID, requestData.getTemplateId());
            paramMap.put(ACCOUNT_NUMBER, requestData.getAccountNumber());
        }

        if (validateChecksum(requestData, paramMap)) {
            return true;
        }
        for (Entry<String, String> entry : paramMap.entrySet()) {
            paramMap.put(entry.getKey(), entry.getValue().trim());
        }
        return validateChecksum(requestData, paramMap);

    }

    private boolean validateChecksum(PaymentRequestBean requestData, Map<String, String> paramMap) {

        boolean isAesEncrypted = merchantPreferenceService.isAES256EncRequestEnabled(requestData.getMid());
        String paytmChecksumString = "";
        boolean checksumMatched = false;
        if (!paramMap.isEmpty()) {
            paytmChecksumString = ValidateChecksum.getInstance().getPaytmChecksumString(paramMap);
        } else if (StringUtils.isNotBlank(requestData.getChecksumString())) {
            paytmChecksumString = requestData.getChecksumString();
        } else {
            LOGGER.info("Paytm Checksum String is blank");
            return false;
        }

        String merchantKey = merchantExtendInfoUtils.getMerchantKey(requestData.getMid(), requestData.getClientId());
        if (isAesEncrypted) {
            merchantKey = aesMerchantService.fetchAesChecksumKey(requestData.getMid());
        }
        CheckSumInput checkSumInput = new CheckSumInput();

        String encMerchCheckSum = requestData.getChecksumhash();

        // Handling has been done for IRCTC
        if (encMerchCheckSum != null && !encMerchCheckSum.trim().equals("")) {
            encMerchCheckSum = encMerchCheckSum.replaceAll(" ", "");
            encMerchCheckSum = encMerchCheckSum.replaceAll("\r", "");
            encMerchCheckSum = encMerchCheckSum.replaceAll("\n", "");
            LOGGER.debug("AFTER encMerchCheckSum>> {}", encMerchCheckSum);
        }

        checkSumInput.setMerchantChecksumHash(encMerchCheckSum);
        checkSumInput.setPaytmChecksumHash(paytmChecksumString);
        checkSumInput.setMerchantKey(merchantKey);

        try {
            if (isAesEncrypted) {
                checksumMatched = ValidateChecksum.getInstance().verifySpecialCheckSum(checkSumInput);
            } else {
                checksumMatched = ValidateChecksum.getInstance().verifyCheckSum(checkSumInput);
            }
            // masking sso Token
            if (StringUtils.isNotBlank(requestData.getSsoToken())) {
                paytmChecksumString = StringUtils.replace(paytmChecksumString, requestData.getSsoToken(),
                        StringUtils.repeat("*", requestData.getSsoToken().length()));
            } else if (StringUtils.isNotBlank(requestData.getPaytmToken())) {
                paytmChecksumString = StringUtils.replace(paytmChecksumString, requestData.getPaytmToken(),
                        StringUtils.repeat("*", requestData.getPaytmToken().length()));
            }
        } catch (SecurityException e) {
            LOGGER.error("SecurityException Occurred : ", e.getMessage());
        } catch (Exception e) {
            LOGGER.error("Exception Occurred : ", e.getMessage());
        }
        if (!checksumMatched) {
            LOGGER.error("CheckSum Failure");
            EXT_LOGGER.customInfo("Paytm Checksum String is : {} ", paytmChecksumString);
        }
        LOGGER.debug("Paytm Checksum String is : {} ", paytmChecksumString);
        return checksumMatched;
    }

    public boolean validateChecksum(BinDetailsRequest request, Map<String, String> requestMap) {

        if (requestMap.isEmpty())
            return false;
        String paytmChecksumString = ValidateChecksum.getInstance().getPaytmChecksumString(requestMap);

        String merchantKey = merchantExtendInfoUtils.getMerchantKey(request.getHead().getMid());
        CheckSumInput checkSumInput = new CheckSumInput();

        checkSumInput.setMerchantChecksumHash(request.getBody().getChecksum());
        checkSumInput.setPaytmChecksumHash(paytmChecksumString);
        checkSumInput.setMerchantKey(merchantKey);

        try {
            return ValidateChecksum.getInstance().verifyCheckSum(checkSumInput);
        } catch (SecurityException e) {
            LOGGER.error("SecurityException Occurred : ", e.getMessage());
        } catch (Exception e) {
            LOGGER.error("Exception Occurred : ", e.getMessage());
        }
        LOGGER.error("CheckSum Failure");
        return false;
    }

    private boolean checkIfParamIgnored(PaymentRequestBean requestData, String requestParam,
            EntityIgnoreParamsDataResponse entityIgnoreParamsResponse) {

        // Ignore Payment Details Param for Seamless Native
        if (ERequestType.SEAMLESS_NATIVE.getType().equals(requestData.getRequestType())
                && TheiaConstant.RequestParams.PAYMENT_DETAILS.equals(requestParam)) {
            return true;
        }

        // Ignore params for merchant/entityId if required

        if (entityIgnoreParamsResponse != null && entityIgnoreParamsResponse.isSuccessfullyProcessed()) {
            for (EntityIgnoreParamsData params : entityIgnoreParamsResponse.getParamsList()) {
                if (requestParam.equalsIgnoreCase(params.getFieldName())) {
                    return true;
                }
            }
        }
        return false;
    }

    private EntityIgnoreParamsDataResponse getEntityIgnoreParams(PaymentRequestBean requestData) {
        String entityId = merchantExtendInfoUtils.getEntityIDCorrespodingToMerchant(requestData.getMid());
        LOGGER.debug("Entity id :{}", entityId);
        EntityIgnoreParamsDataResponse entityIgnoreParamsResponse = configurationDataService
                .getEntityIgnoreParams(entityId);
        LOGGER.debug("Entity ignore response:{}", entityIgnoreParamsResponse);
        return entityIgnoreParamsResponse;
    }

    private static String checkSumStringForAutoDebit(PaymentRequestBean requestData, StringBuilder chkSumBuilder) {
        // Populating chkSumBuilder for Paytm FastForward request
        if (StringUtils.isNotBlank(requestData.getRequestType())) {
            chkSumBuilder.append(requestData.getRequestType()).append("|");
        }
        if (StringUtils.isNotBlank(requestData.getMid())) {
            chkSumBuilder.append(requestData.getMid()).append("|");
        }
        if (StringUtils.isNotBlank(requestData.getOrderId())) {
            chkSumBuilder.append(requestData.getOrderId()).append("|");
        }
        if (StringUtils.isNotBlank(requestData.getTxnAmount())) {
            chkSumBuilder.append(requestData.getTxnAmount()).append("|");
        }
        if (StringUtils.isNotBlank(requestData.getCustId())) {
            chkSumBuilder.append(requestData.getCustId()).append("|");
        }
        if (StringUtils.isNotBlank(requestData.getCurrency())) {
            chkSumBuilder.append(requestData.getCurrency()).append("|");
        }

        if (StringUtils.isNotBlank(requestData.getDeviceId())) {
            chkSumBuilder.append(requestData.getDeviceId()).append("|");
        }

        if (StringUtils.isNotBlank(requestData.getSsoToken())) {
            chkSumBuilder.append(requestData.getSsoToken()).append("|");
        }

        if (StringUtils.isNotBlank(requestData.getPaymentTypeId())) {
            chkSumBuilder.append(requestData.getPaymentTypeId()).append("|");
        }
        if (StringUtils.isNotBlank(requestData.getIndustryTypeId())) {
            chkSumBuilder.append(requestData.getIndustryTypeId()).append("|");
        }
        if (StringUtils.isNotBlank(requestData.getClientId())) {
            chkSumBuilder.append(requestData.getClientId()).append("|");
        }
        if (StringUtils.isNotBlank(requestData.getExchangeRate())) {
            chkSumBuilder.append(requestData.getExchangeRate()).append("|");
        }
        if (StringUtils.isNotBlank(requestData.getLoyaltyPointRootUserId())) {
            chkSumBuilder.append(requestData.getLoyaltyPointRootUserId()).append("|");
        }
        if (StringUtils.isNotBlank(requestData.getLoyaltyPointRootUserPGMid())) {
            chkSumBuilder.append(requestData.getLoyaltyPointRootUserPGMid()).append("|");
        }

        return chkSumBuilder.toString();
    }

}
