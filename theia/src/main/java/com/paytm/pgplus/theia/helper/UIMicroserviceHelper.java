package com.paytm.pgplus.theia.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.pgplus.enums.EChannelId;
import com.paytm.pgplus.facade.enums.Type;
import com.paytm.pgplus.facade.uimicroservice.service.IUIMicroservice;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.facade.utils.LogUtil;
import com.paytm.pgplus.theia.models.uimicroservice.request.UIMicroserviceRequest;
import com.paytm.pgplus.theia.models.uimicroservice.response.UIMicroserviceResponse;
import com.paytm.pgplus.theia.services.helper.FF4JHelper;
import com.paytm.pgplus.theia.utils.EnvInfoUtil;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.Native.CHANNEL_ID;

@Service
public class UIMicroserviceHelper {

    @Autowired
    @Qualifier("UIMicroservice")
    private IUIMicroservice uiMicroService;

    @Autowired
    private FF4JHelper ff4JHelper;

    public static final Logger LOGGER = LoggerFactory.getLogger(UIMicroserviceHelper.class);

    private static ObjectMapper objectMapper = new ObjectMapper();

    public Map<String, Object> getUiMicroServiceMetaDataMap(String channelId, String isDataEncoded) {

        Map<String, Object> meta = new HashMap<String, Object>();

        EChannelId eChannelId = EChannelId.getEChannelIdByValue(channelId);

        if (eChannelId == null) {
            eChannelId = EChannelId.WEB;
        }

        meta.put(CHANNEL_ID, eChannelId.getValue());
        if (StringUtils.isNotBlank(isDataEncoded)) {
            meta.put("isDataEncoded", Boolean.parseBoolean(isDataEncoded));
        }

        saveUiContext(meta);

        return meta;
    }

    public String getUiMicroServiceRequestJson(String jsonPayload, String channelId, String isDataEncoded)
            throws IOException {
        Map<String, Object> payload = new HashMap<String, Object>();
        Map<String, Object> meta = getUiMicroServiceMetaDataMap(channelId, isDataEncoded);
        payload.put("payload", objectMapper.readValue(jsonPayload, Map.class));

        if (MapUtils.isNotEmpty(meta)) {
            payload.put("meta", meta);
        }
        String finalPayloadJson = null;
        try {
            finalPayloadJson = JsonMapper.mapObjectToJson(payload);
        } catch (Exception e) {
            LOGGER.info("Exception occured while mapping UI_MICROSERVICE request to Json :{}", e);
        }
        return finalPayloadJson;
    }

    public String getUiMicroServiceRequestJson(String jsonPayload) throws IOException {
        Map<String, Object> payload = new HashMap<String, Object>();
        payload.put("payload", objectMapper.readValue(jsonPayload, Map.class));
        Map<String, Object> meta = new HashMap<String, Object>();
        saveUiContext(meta);
        payload.put("meta", meta);
        String finalPayloadJson = null;
        try {
            finalPayloadJson = JsonMapper.mapObjectToJson(payload);
        } catch (Exception e) {
            LOGGER.info("Exception occured while mapping UI_MICROSERVICE request to Json :{}", e);
        }
        return finalPayloadJson;
    }

    public UIMicroserviceResponse getHtmlPageFromUI(UIMicroserviceRequest uiMicroserviceRequest,
            String ff4jFeatureName, String mid) {
        long startTime = System.currentTimeMillis();
        UIMicroserviceResponse uiMicroserviceResponse = new UIMicroserviceResponse();

        try {
            if (ff4JHelper.isFF4JFeatureForMidEnabled(ff4jFeatureName, mid)) {

                String uiMicroserviceRequestJson = null;

                switch (uiMicroserviceRequest.getUiMicroServiceUrl()) {
                case GV_CONSENT_URL:
                case RISK_VERIFICATION_URL:
                    uiMicroserviceRequestJson = getUiMicroServiceRequestJson(uiMicroserviceRequest.getJsonPayload());
                    break;
                case ENHANCED_CASHIER_URL:
                    uiMicroserviceRequestJson = getUiMicroServiceRequestJson(uiMicroserviceRequest.getJsonPayload(),
                            uiMicroserviceRequest.getChannelId(), uiMicroserviceRequest.getIsDataEncoded());
                    break;
                default:
                    break;
                }
                LogUtil.logPayload(uiMicroserviceRequest.getUiMicroServiceUrl().getUrl(), Type.REQUEST,
                        uiMicroserviceRequestJson);
                String htmlPage = uiMicroService.getHtmlPageFromUiMicroService(uiMicroserviceRequestJson,
                        uiMicroserviceRequest.getUiMicroServiceUrl());
                uiMicroserviceResponse.setHtmlPage(htmlPage);
                LogUtil.logResponsePayload(uiMicroserviceRequest.getUiMicroServiceUrl().getUrl(), Type.RESPONSE,
                        uiMicroserviceResponse.toString(), startTime);
                if (StringUtils.isNotBlank(htmlPage)) {
                    LOGGER.info("Successfully fetched html page from UI_MICROSERVICE");
                } else {
                    LOGGER.info("Html page fetched from UI_MICROSERVICE is blank");
                }
                return uiMicroserviceResponse;
            }
        } catch (Exception e) {
            LOGGER.info("Exception occured while fetching html page from UI_MICROSERVICE :{}", e);
        }
        return uiMicroserviceResponse;
    }

    private void saveUiContext(Map<String, Object> meta) {
        HttpServletRequest servletRequest = EnvInfoUtil.httpServletRequest();

        Map<String, String> map = new HashMap<String, String>();

        Enumeration headerNames = servletRequest.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String key = (String) headerNames.nextElement();
            String value = servletRequest.getHeader(key);
            map.put(key, value);
        }
        meta.put("headers", map);
        meta.put("serverName", servletRequest.getServerName());
    }

}
