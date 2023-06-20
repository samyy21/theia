package com.paytm.pgplus.theia.helper;

import com.paytm.pgplus.biz.core.risk.RiskConstants;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.cache.model.BinDetail;
import com.paytm.pgplus.common.bankForm.model.BankForm;
import com.paytm.pgplus.common.bankForm.model.FormDetail;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.response.ResponseHeader;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.enums.ELitePayViewDisabledReasonMsg;
import com.paytm.pgplus.theia.models.NativeJsonResponse;
import com.paytm.pgplus.theia.models.NativeJsonResponseBody;
import com.paytm.pgplus.theia.nativ.model.enhancenative.BankRedirectionDetail;
import com.paytm.pgplus.theia.nativ.model.payview.response.BankCard;
import com.paytm.pgplus.theia.nativ.model.payview.response.NativeCashierInfoResponse;
import com.paytm.pgplus.theia.nativ.model.payview.response.PayChannelBase;
import com.paytm.pgplus.theia.nativ.model.payview.response.PayMethod;
import com.paytm.pgplus.theia.nativ.utils.NativePaymentUtil;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.paytm.pgplus.theia.constants.TheiaConstant.DccConstants.CONTENT_VALUE;
import static com.paytm.pgplus.theia.constants.TheiaConstant.DccConstants.DCC_PAGE_URL;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.CONTENT_TYPE;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.REDIRECT;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.Native.POST;

@Service("dccPaymentHelper")
public class DccPaymentHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(DccPaymentHelper.class);

    @Autowired
    private NativeSessionUtil nativeSessionUtil;

    public NativeJsonResponse getNativePlusJsonDccBankform(WorkFlowRequestBean flowRequestBean) {
        BankForm dccBankFrom = buildBankFormForDcc(flowRequestBean);
        NativeJsonResponse nativeJsonResponse = new NativeJsonResponse();
        nativeJsonResponse.setHead(new ResponseHeader());
        NativeJsonResponseBody body = new NativeJsonResponseBody();
        body.setBankForm(dccBankFrom);
        body.setResultInfo(NativePaymentUtil.resultInfo(ResultCode.SUCCESS));
        nativeJsonResponse.setBody(body);
        return nativeJsonResponse;
    }

    public BankRedirectionDetail getNativeEnhanceJsonDccBankform(WorkFlowRequestBean flowRequestBean) {
        BankRedirectionDetail bankRedirectionDetail = new BankRedirectionDetail();
        StringBuilder actionUrl = new StringBuilder(
                com.paytm.pgplus.common.config.ConfigurationUtil.getProperty(RiskConstants.THEIA_BASE_URL))
                .append(DCC_PAGE_URL).append("?").append("mid=").append(flowRequestBean.getPaytmMID()).append("&")
                .append("orderId=").append(flowRequestBean.getOrderID());
        bankRedirectionDetail.setCallbackUrl(actionUrl.toString());
        bankRedirectionDetail.setMethod(POST);
        BankForm dccBankFrom = buildBankFormForDcc(flowRequestBean);
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put(CONTENT_TYPE, CONTENT_VALUE);
        Map<String, String> content = getContentForDccPage(flowRequestBean);
        bankRedirectionDetail.setBankForm(dccBankFrom);
        bankRedirectionDetail.setContent(content);
        return bankRedirectionDetail;

    }

    private BankForm buildBankFormForDcc(WorkFlowRequestBean flowRequestBean) {

        BankForm dccBankFrom = new BankForm();
        dccBankFrom.setPageType(REDIRECT);
        dccBankFrom.setTxnToken(flowRequestBean.getTxnToken());
        FormDetail redirectForm = new FormDetail();
        StringBuilder actionUrl = new StringBuilder(
                com.paytm.pgplus.common.config.ConfigurationUtil.getProperty(RiskConstants.THEIA_BASE_URL))
                .append(DCC_PAGE_URL).append("?").append("mid=").append(flowRequestBean.getPaytmMID()).append("&")
                .append("orderId=").append(flowRequestBean.getOrderID());
        redirectForm.setActionUrl(actionUrl.toString());
        redirectForm.setMethod(POST);
        redirectForm.setType(REDIRECT);
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put(CONTENT_TYPE, CONTENT_VALUE);
        Map<String, String> content = getContentForDccPage(flowRequestBean);
        redirectForm.setContent(content);
        redirectForm.setHeaders(headers);
        dccBankFrom.setRedirectForm(redirectForm);

        return dccBankFrom;
    }

    public List<String> dccEnabledAcquirersOnMerchantInLpv(PaymentRequestBean requestData,
            WorkFlowRequestBean flowRequestBean, BinDetail binDetail) {
        if (requestData.getTxnToken() != null) {
            NativeCashierInfoResponse nativeCashierInfoResponse = nativeSessionUtil.getCashierInfoResponse(requestData
                    .getTxnToken());
            if (nativeCashierInfoResponse != null && nativeCashierInfoResponse.getBody() != null
                    && nativeCashierInfoResponse.getBody().getMerchantPayOption().getPayMethods() != null) {
                for (PayMethod payMethod : nativeCashierInfoResponse.getBody().getMerchantPayOption().getPayMethods()) {
                    if (payMethod.getPayMethod() != null && payMethod.getPayMethod().equals(binDetail.getCardType())
                            && payMethod.getPayChannelOptions() != null && !payMethod.getPayChannelOptions().isEmpty()) {
                        for (PayChannelBase payChannelBase : payMethod.getPayChannelOptions()) {
                            if (payChannelBase instanceof BankCard) {
                                if (payChannelBase.getIsDisabled() != null
                                        && TheiaConstant.ExtraConstants.TRUE.equals(payChannelBase.getIsDisabled()
                                                .getStatus())
                                        && ELitePayViewDisabledReasonMsg.CHANNEL_NOT_AVAILABLE.getDisabledReason()
                                                .equals(payChannelBase.getIsDisabled().getMsg())) {
                                    continue;
                                }
                                BankCard bankCard = (BankCard) payChannelBase;
                                if (CollectionUtils.isNotEmpty(bankCard.getDccServiceInstIds())
                                        && (bankCard.getPayChannelOption().equals(binDetail.getCardType() + "_"
                                                + binDetail.getCardName()))) {
                                    return bankCard.getDccServiceInstIds();
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private Map<String, String> getContentForDccPage(WorkFlowRequestBean flowRequestBean) {
        Map<String, String> content = new HashMap<String, String>();

        content.put(TheiaConstant.RequestParams.Native.ORDER_ID, flowRequestBean.getOrderID());
        content.put(TheiaConstant.RequestParams.Native.MID, flowRequestBean.getPaytmMID());
        content.put(com.paytm.pgplus.theiacommon.constants.TheiaConstant.RequestParams.Native.TXN_TOKEN,
                flowRequestBean.getTxnToken());
        return content;
    }

    public boolean dccPageTobeRendered(WorkFlowRequestBean flowRequestBean) {
        if (flowRequestBean.getDccSupported() && !flowRequestBean.isPaymentCallFromDccPage()) {
            return true;
        }
        return false;
    }

    public boolean dccPaymentAllowed(WorkFlowRequestBean flowRequestBean) {
        if (flowRequestBean.getDccSupported() && flowRequestBean.isPaymentCallFromDccPage()) {
            return true;
        }
        return false;
    }

}
