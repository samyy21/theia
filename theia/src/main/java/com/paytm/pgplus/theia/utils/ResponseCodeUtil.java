package com.paytm.pgplus.theia.utils;

import com.paytm.pgplus.cache.model.MerchantBussinessLogoInfo;
import com.paytm.pgplus.cache.model.MessageAndRetryDetails;
import com.paytm.pgplus.cache.model.ResponseCodeDetails;
import com.paytm.pgplus.common.responsecode.enums.SystemResponseCode;
import com.paytm.pgplus.common.responsecode.models.CommonResponseCode;
import com.paytm.pgplus.payloadvault.theia.enums.ButtonAction;
import com.paytm.pgplus.payloadvault.theia.enums.FailureType;
import com.paytm.pgplus.payloadvault.theia.response.RetryInfo;
import com.paytm.pgplus.payloadvault.theia.response.TransactionResponse;
import com.paytm.pgplus.responsecode.utils.MerchantResponseUtil;
import com.paytm.pgplus.theia.nativ.model.merchantuserinfo.MerchantInfoServiceRequest;
import com.paytm.pgplus.theia.nativ.service.MerchantInfoService;
import com.paytm.pgplus.theia.offline.facade.ICommonFacade;
import com.paytm.pgplus.theia.services.helper.RetryServiceHelper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.FAILURE_IMAGE_NAME;

@Component
public class ResponseCodeUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResponseCodeUtil.class);

    @Autowired
    MerchantResponseUtil merchantResponseUtil;

    @Autowired
    private MerchantInfoService merchantInfoService;

    @Autowired
    @Qualifier("commonFacade")
    private ICommonFacade commonFacade;

    @Autowired
    private RetryServiceHelper retryServiceHelper;

    public void setRespMsgeAndCode(TransactionResponse response, String alipayRespCode,
            SystemResponseCode systemResponseCode) {
        ResponseCodeDetails responseCodeDetails = getResponseCodeDetails(alipayRespCode, systemResponseCode,
                response.getTransactionStatus());
        if (responseCodeDetails != null && StringUtils.isNotBlank(responseCodeDetails.getResponseCode())) {
            response.setResponseCode(responseCodeDetails.getResponseCode());
            response.setResponseMsg(getResponseMsg(responseCodeDetails));
        }
        if (responseCodeDetails != null && responseCodeDetails.getMessageAndRetryDetails() != null
                && retryServiceHelper.checkNativePaymentRetry(response.getMid(), response.getOrderId())) {
            LOGGER.info("Setting Retry Info in Transaction response Object");
            setRetryInfoInResponse(response, responseCodeDetails.getMessageAndRetryDetails());
        }
    }

    public void setRetryInfoInResponse(TransactionResponse response, MessageAndRetryDetails messageAndRetryDetails) {
        RetryInfo retryInfo = getRetryInfoFromResponseCodeDetails(messageAndRetryDetails, response.getMid());
        response.setRetryInfo(retryInfo);
    }

    public RetryInfo getRetryInfoFromResponseCodeDetails(MessageAndRetryDetails messageAndRetryDetails, String mid) {
        RetryInfo retryInfo = new RetryInfo();
        RetryInfo.Button proceedButton = new RetryInfo.Button();
        RetryInfo.Button backButton = new RetryInfo.Button();
        List<RetryInfo.Button> buttonList = new ArrayList<>();

        MerchantInfoServiceRequest merchantDetailsRequest = new MerchantInfoServiceRequest(mid);
        MerchantBussinessLogoInfo merchantDetailsResponse = merchantInfoService.getMerchantInfo(merchantDetailsRequest);

        retryInfo.setFailureLogo(commonFacade.getLogoName(FAILURE_IMAGE_NAME));
        if (merchantDetailsResponse != null) {
            retryInfo.setMerchantName(merchantDetailsResponse.getMerchantDisplayName());
            retryInfo.setMerchantLogo(merchantDetailsResponse.getMerchantImageName());
        }
        retryInfo.setFailureHeader(messageAndRetryDetails.getHeaderMessage());
        retryInfo.setBlockerMessage(messageAndRetryDetails.getBlockerMessage());
        retryInfo.setFailureMessage(messageAndRetryDetails.getBodyMessage());
        retryInfo.setPopupEnable(messageAndRetryDetails.isPopupEnable());
        if (StringUtils.isNotBlank(messageAndRetryDetails.getFailureType())) {
            retryInfo.setFailureType(FailureType.valueOf(messageAndRetryDetails.getFailureType()));
        }

        if (StringUtils.isNotBlank(messageAndRetryDetails.getProceedButtonAction())) {
            proceedButton.setButtonAction(ButtonAction.valueOf(messageAndRetryDetails.getProceedButtonAction()));
            proceedButton.setText(messageAndRetryDetails.getProceedButtonText());
            buttonList.add(proceedButton);
        }
        if (StringUtils.isNotBlank(messageAndRetryDetails.getBackButtonAction())) {
            backButton.setButtonAction(ButtonAction.valueOf(messageAndRetryDetails.getBackButtonAction()));
            backButton.setText(messageAndRetryDetails.getBackButtonText());
            buttonList.add(backButton);
        }

        retryInfo.setButtons(buttonList);

        LOGGER.info("Retry Info from DB is {} ", retryInfo);

        return retryInfo;
    }

    public ResponseCodeDetails getResponseCodeDetails(SystemResponseCode systemResponseCode) {
        return getResponseCodeDetails(null, systemResponseCode, null);
    }

    public ResponseCodeDetails getResponseCodeDetails(String alipayRespCode, SystemResponseCode systemResponseCode,
            String txnStatus) {
        return getResponseCodeDetails(getCommonRespCode(alipayRespCode, systemResponseCode, txnStatus));
    }

    public CommonResponseCode getCommonRespCode(String alipayRespCode, SystemResponseCode systemResponseCode,
            String txnStatus) {
        CommonResponseCode commonResponseCode = new CommonResponseCode();
        commonResponseCode.setAlipayRespCode(alipayRespCode);
        commonResponseCode.setSystemRespCode(systemResponseCode);
        commonResponseCode.setTxnStatus(txnStatus);
        return commonResponseCode;
    }

    public ResponseCodeDetails getResponseCodeDetails(CommonResponseCode commonResponseCode) {
        return merchantResponseUtil.getMerchantRespCodeDetails(commonResponseCode);
    }

    public String getResponseMsg(ResponseCodeDetails responseCodeDetails) {
        String respMsg = "";
        if (responseCodeDetails != null) {
            respMsg = StringUtils.isNotBlank(responseCodeDetails.getDisplayMessage()) ? responseCodeDetails
                    .getDisplayMessage()
                    : StringUtils.isNotBlank(responseCodeDetails.getRemark()) ? responseCodeDetails.getRemark() : "";
        }
        return respMsg;
    }

}
