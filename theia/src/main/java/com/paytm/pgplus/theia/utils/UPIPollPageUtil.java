package com.paytm.pgplus.theia.utils;

import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.models.UltimateBeneficiaryDetails;
import com.paytm.pgplus.pgpff4jclient.IPgpFf4jClient;
import com.paytm.pgplus.theia.aspects.LocaleFieldAspect;
import com.paytm.pgplus.theia.sessiondata.MerchantInfo;
import com.paytm.pgplus.theia.sessiondata.TransactionInfo;
import com.paytm.pgplus.theia.sessiondata.UPITransactionInfo;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.V1_PTC;
import static com.paytm.pgplus.theia.constants.TheiaConstant.GvConsent.PTR_URL;
import static com.paytm.pgplus.theia.constants.TheiaConstant.GvConsent.UPI_POLL_PAGE_V1;

@Component
public class UPIPollPageUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(UPIPollPageUtil.class);

    @Autowired
    private LocalizationUtil localizationUtil;

    @Autowired
    private LocaleFieldAspect localeFieldAspect;

    @Autowired
    @Qualifier("processTransactionUtil")
    private ProcessTransactionUtil processTransactionUtil;

    @Autowired
    private IPgpFf4jClient iPgpFf4jClient;

    public boolean returnUPIHTMLPollPage(TransactionInfo txnInfo, UPITransactionInfo upiTransactionInfo,
            MerchantInfo merchInfo, HttpServletResponse response, UltimateBeneficiaryDetails ultimateBeneficiaryDetails)
            throws IOException {
        String upiPollHtmlPage = com.paytm.pgplus.common.config.ConfigurationUtil.getUpiPollingPage();
        if (StringUtils.isNotEmpty(upiPollHtmlPage)) {// to
            // check
            // ui
            // file
            // exists
            // for
            // this
            // flow
            try {
                JSONObject pollingJson = new JSONObject();
                JSONObject txnInformation = new JSONObject();
                txnInformation.put("txnId", txnInfo.getTxnId());
                txnInformation.put("orderId", txnInfo.getOrderId());
                txnInformation.put("txnAmount", upiTransactionInfo.getTransactionAmount());
                pollingJson.put("upiTransactionInfo", upiTransactionInfo);
                pollingJson.put("txnInfo", txnInformation);
                pollingJson.put("merchantinfo", merchInfo);
                pollingJson.put("ultimateBeneficiaryDetails", ultimateBeneficiaryDetails);
                if (processTransactionUtil.isRequestOfType(PTR_URL) || processTransactionUtil.isRequestOfType(V1_PTC)
                        || processTransactionUtil.isRequestOfType(UPI_POLL_PAGE_V1)) {
                    localeFieldAspect.addLocaleFieldsInObject(upiTransactionInfo, UPI_POLL_PAGE_V1);
                    localizationUtil.addLocaleAppData(upiTransactionInfo);
                }
                upiPollHtmlPage = upiPollHtmlPage.replace("pollingJSON", JsonMapper.mapObjectToJson(pollingJson));
            } catch (FacadeCheckedException e) {
                LOGGER.error("Exception occurred while parsing pollingJson Object to JSON in upiPollHtmlPage {}", e);
            }
            response.setContentType("text/html; charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().print(upiPollHtmlPage);
            return true;
        }
        return false;
    }

    public boolean isUPIPollPageEnabledOnMid(String mid) {
        if (StringUtils.isEmpty(mid)) {
            LOGGER.error("MID found empty while checking ff4j flag for UPI Poll page");
            return false;
        }
        Map<String, Object> context = new HashMap<>();
        context.put("mid", mid);
        return iPgpFf4jClient.checkWithdefault("upiPollPageMid", context, false);
    }
}
