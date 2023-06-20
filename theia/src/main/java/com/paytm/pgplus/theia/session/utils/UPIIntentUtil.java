package com.paytm.pgplus.theia.session.utils;

import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.common.enums.DeepLinkFields;
import com.paytm.pgplus.common.util.UPIIntentUtility;

import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.exceptions.TheiaServiceException;
import com.paytm.pgplus.theia.models.FetchDeepLinkRequestBody;
import com.paytm.pgplus.theia.utils.ConfigurationUtil;
import com.paytm.pgplus.theia.utils.MerchantExtendInfoUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;

@Component("upiIntentUtil")
public class UPIIntentUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(UPIIntentUtil.class);

    @Autowired
    MerchantExtendInfoUtils merchantExtendInfoUtils;

    @Autowired
    Environment env;

    public String createEncodedDeepLink(Map<String, String> params) {
        UriBuilder builder = UriBuilder.fromPath("upi://pay");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            builder.queryParam(entry.getKey(), entry.getValue());
        }
        URI uri = builder.build();
        return uri.toString();
    }

    public String createDeepLink(FetchDeepLinkRequestBody body, WorkFlowResponseBean workFlowResponseBean)
            throws GeneralSecurityException {
        Map<String, String> params = getMapForDeepLink(body, workFlowResponseBean);
        String flag = ConfigurationUtil.getProperty(TheiaConstant.ExtraConstants.DEEPLINK_SIGN_PROPERT);
        String purpose;
        if ("Y".equals(flag)) {
            params.put(DeepLinkFields.MODE.getValue(), TheiaConstant.ExtraConstants.MODE);
            params.put(DeepLinkFields.ORGID.getValue(), TheiaConstant.ExtraConstants.ORGID);
            purpose = ConfigurationUtil.getUpiMccProperty(TheiaConstant.ExtraConstants.UPI_MCC_PREFIX
                    .concat(workFlowResponseBean.getMcc()));
            if (StringUtils.isBlank(purpose)) {
                purpose = TheiaConstant.ExtraConstants.DEFAULT_PURPOSE;
            }
            params.put(DeepLinkFields.PURPOSE.getValue(), purpose);
            LOGGER.info("Creating signed deepLink");
            try {
                return UPIIntentUtility.createSignedDeepLinkV2(params);
            } catch (GeneralSecurityException e) {
                LOGGER.error("exception occured while signing deep link:", e);
                throw e;
            }

        }
        return UPIIntentUtility.createUnsignedDeepLink(params);
    }

    public String createDeepLinkUTF8(Map<String, String> params) {
        StringBuilder stringBuilder = UPIIntentUtility.getDeepLinkFromMap(params);
        try {
            return URLEncoder.encode(stringBuilder.toString(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("Exception occurred while encoding deeplink {}", e);
            throw new TheiaServiceException("Exception occurred while encoding deeplink");
        }
    }

    private Map<String, String> getMapForDeepLink(FetchDeepLinkRequestBody body,
            WorkFlowResponseBean workFlowResponseBean) {
        Map<String, String> fieldsMap = new HashMap<>();
        fieldsMap.put(DeepLinkFields.MERCHANT_VPA.getValue(), workFlowResponseBean.getMerchantVpa());
        fieldsMap.put(DeepLinkFields.MERCHANT_NAME.getValue(), merchantExtendInfoUtils.getMerchantName(body.getMid()));
        fieldsMap.put(DeepLinkFields.MCC.getValue(), workFlowResponseBean.getMcc());
        fieldsMap.put(DeepLinkFields.TXN_REF_ID.getValue(), workFlowResponseBean.getUpiPSPResponse()
                .getExternalSerialNo());
        fieldsMap.put(DeepLinkFields.TXN_NOTE.getValue(), body.getTxnNote());
        fieldsMap.put(DeepLinkFields.TXN_AMMOUNT.getValue(), body.getTxnAmount());
        fieldsMap.put(DeepLinkFields.REF_URL.getValue(), body.getRefUrl());
        return fieldsMap;
    }

}
