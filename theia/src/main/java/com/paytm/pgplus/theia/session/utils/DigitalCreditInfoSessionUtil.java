package com.paytm.pgplus.theia.session.utils;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import com.paytm.pgplus.theia.utils.ConfigurationUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.pgplus.biz.core.model.request.ExternalAccountInfoBiz;
import com.paytm.pgplus.biz.core.model.request.PayChannelOptionViewBiz;
import com.paytm.pgplus.biz.core.model.request.PayMethodViewsBiz;
import com.paytm.pgplus.biz.enums.EPayMode;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.common.enums.PayMethod;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.pgproxycommon.utils.AmountUtils;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.services.ITheiaSessionDataService;
import com.paytm.pgplus.theia.sessiondata.DigitalCreditInfo;
import com.paytm.pgplus.theia.sessiondata.TransactionInfo;
import com.paytm.pgplus.theia.sessiondata.WalletInfo;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.PaytmDigitalCreditConstant.*;

/**
 * @author kartik
 * @date 19-04-2017
 */
@Component("digitalCreditInfoSessionUtil")
public class DigitalCreditInfoSessionUtil {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final Logger LOGGER = LoggerFactory.getLogger(DigitalCreditInfoSessionUtil.class);

    @Autowired
    @Qualifier("theiaSessionDataService")
    private ITheiaSessionDataService theiaSessionDataService;

    public void setDigitalCreditInfoInSession(PaymentRequestBean requestBean, WorkFlowResponseBean workFlowResponseBean) {

        DigitalCreditInfo digitalCreditInfo = theiaSessionDataService.getDigitalCreditInfoFromSession(
                requestBean.getRequest(), true);
        List<PayMethodViewsBiz> payMethodViewsList = (workFlowResponseBean.getMerchnatViewResponse() != null) ? workFlowResponseBean
                .getMerchnatViewResponse().getPayMethodViews()
                : (workFlowResponseBean.getMerchnatLiteViewResponse() != null) ? workFlowResponseBean
                        .getMerchnatLiteViewResponse().getPayMethodViews() : Collections.emptyList();

        boolean displayRequired = true;
        boolean digitalCreditInactive = false;

        for (PayMethodViewsBiz payMethodViewBiz : payMethodViewsList) {
            if (PayMethod.PAYTM_DIGITAL_CREDIT.getMethod().equals(payMethodViewBiz.getPayMethod())) {
                if (!CollectionUtils.isEmpty(payMethodViewBiz.getPayChannelOptionViews())) {
                    for (PayChannelOptionViewBiz payChannelOptionViewBiz : payMethodViewBiz.getPayChannelOptionViews()) {
                        if (payChannelOptionViewBiz.isEnableStatus()
                                && !CollectionUtils.isEmpty(payChannelOptionViewBiz.getExternalAccountInfos())) {
                            ExternalAccountInfoBiz externalAccountInfo = payChannelOptionViewBiz
                                    .getExternalAccountInfos().get(0);
                            digitalCreditInfo.setExternalAccountNo(externalAccountInfo.getExternalAccountNo());
                            digitalCreditInfo
                                    .setPaymentRetryCount(TheiaConstant.DigitalCreditConfiguration.MAX_PASS_CODE_ATTEMPTS);
                            String accountStatus = null;
                            String infoButtonMessage = null;
                            if (StringUtils.isNotBlank(externalAccountInfo.getExtendInfo())) {
                                String lenderId = null;
                                boolean passcodeRequired = true;
                                try {
                                    JsonNode node = OBJECT_MAPPER.readTree(externalAccountInfo.getExtendInfo());
                                    if (node != null) {
                                        JsonNode lenderIdNode = node.get("lenderId");
                                        if (lenderIdNode != null) {
                                            lenderId = lenderIdNode.textValue();
                                        }
                                        JsonNode passcodeNode = node.get("passcodeRequired");
                                        if (passcodeNode != null) {
                                            LOGGER.info("Received passcodeRequired flag as : {}",
                                                    passcodeNode.asBoolean());
                                            passcodeRequired = passcodeNode.asBoolean();
                                        }
                                        JsonNode displayNode = node.get("displayRequired");
                                        if (displayNode != null) {
                                            displayRequired = displayNode.asBoolean();
                                        }
                                        JsonNode accountStatusNode = node.get("accountStatus");
                                        if (null != accountStatusNode) {
                                            accountStatus = accountStatusNode.textValue();
                                        }
                                        JsonNode infoButtonMessageNode = node.get("infoButtonMessage");
                                        if (null != accountStatusNode) {
                                            infoButtonMessage = infoButtonMessageNode.textValue();
                                        }

                                    }
                                } catch (IOException e) {
                                    LOGGER.warn("Lender id or passcode flag not found for Paytm CC account : {}",
                                            digitalCreditInfo.getExternalAccountNo());
                                }
                                digitalCreditInfo.setLenderId(lenderId);
                                digitalCreditInfo.setPasscodeRequired(passcodeRequired);
                            }
                            LOGGER.info("Paytm digitail credit display required flag is : {}", displayRequired);
                            if (displayRequired) {
                                digitalCreditInfo.setDigitalCreditEnabled(true);
                            }
                            if (StringUtils.isNotBlank(externalAccountInfo.getAccountBalance())
                                    && NumberUtils.isNumber(externalAccountInfo.getAccountBalance())) {
                                digitalCreditInfo.setAccountBalance(AmountUtils
                                        .getTransactionAmountInRupee(externalAccountInfo.getAccountBalance()));
                                final TransactionInfo txnInfo = theiaSessionDataService
                                        .getTxnInfoFromSession(requestBean.getRequest());
                                final WalletInfo walletInfo = theiaSessionDataService
                                        .getWalletInfoFromSession(requestBean.getRequest());
                                // TODO:Setting for offline use case
                                if (txnInfo != null) {
                                    Double txnAmount = Double.parseDouble(txnInfo.getTxnAmount());
                                    Double chargeFeeAmount = Double.parseDouble(String.valueOf(txnInfo
                                            .getChargeFeeAmountDigitalCredit()));
                                    Double chargeFeeAmountHybrid = Double.parseDouble(String.valueOf(txnInfo
                                            .getChargeFeeAmountHybrid()));
                                    Double pccAccountBalance = Double
                                            .parseDouble(digitalCreditInfo.getAccountBalance());
                                    EPayMode allowedPayMode = workFlowResponseBean.getAllowedPayMode();
                                    if (allowedPayMode != null) {
                                        switch (allowedPayMode) {
                                        case NONE:
                                            // If Postpaid balance < Txn Amount
                                            // -> Inactive
                                            digitalCreditInactive = (txnAmount + chargeFeeAmount) > pccAccountBalance ? true
                                                    : false;
                                            break;
                                        case HYBRID:
                                            if (walletInfo != null && walletInfo.isWalletEnabled()) {
                                                Double walletBalance = walletInfo.getWalletBalance();
                                                Double differenceAmount = txnAmount + chargeFeeAmountHybrid
                                                        - walletBalance;
                                                digitalCreditInactive = differenceAmount > pccAccountBalance ? true
                                                        : false;
                                            }
                                            break;
                                        }
                                    }
                                    if (digitalCreditInactive) {
                                        digitalCreditInfo
                                                .setPaymentRetryCount(TheiaConstant.DigitalCreditConfiguration.PASS_CODE_INVALID_ATTEMPTS);
                                    }
                                }
                            }
                            if (StringUtils.isBlank(accountStatus)
                                    || !StringUtils.equals(ACCOUNT_STATUS_ACTIVE, accountStatus)) {
                                digitalCreditInactive = true;
                                if (StringUtils.isBlank(infoButtonMessage)) {
                                    infoButtonMessage = ConfigurationUtil.getProperty(DEFAULT_INFO_BUTTON_MESSAGE,
                                            "We are facing some issue with postpaid, please use other payment options");
                                }
                                digitalCreditInfo.setInvalidPassCodeMessage(infoButtonMessage);
                            }
                            digitalCreditInfo.setDigitalCreditInactive(digitalCreditInactive);

                        }
                    }
                }
            }
        }
    }
}
