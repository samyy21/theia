package com.paytm.pgplus.biz.core.merchant.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.pgplus.biz.core.merchant.service.IMerchantCenterService;
import com.paytm.pgplus.biz.core.model.MerchantCenterService.PG2EmiDetailsRequest;
import com.paytm.pgplus.biz.core.model.MerchantCenterService.PG2ResultInfo;
import com.paytm.pgplus.biz.core.model.MerchantCenterService.Pg2RestResponse;
import com.paytm.pgplus.biz.utils.ConfigurationUtil;
import com.paytm.pgplus.cache.model.EMIDetailList;
import com.paytm.pgplus.cache.model.EMIDetails;
import com.paytm.pgplus.common.signature.wrapper.SignatureUtilWrapper;
import com.paytm.pgplus.facade.enums.Type;
import com.paytm.pgplus.facade.utils.RequestIdGenerator;
import com.paytm.pgplus.httpclient.HttpRequestPayload;
import com.paytm.pgplus.httpclient.JerseyHttpClient;
import com.paytm.pgplus.httpclient.enums.HttpMethod;
import com.paytm.pgplus.stats.util.AWSStatsDUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.*;

import static com.paytm.pgplus.biz.utils.BizConstant.*;
import static com.paytm.pgplus.facade.utils.LogUtil.logPayload;

@Service
public class MerchantCenterServiceImpl implements IMerchantCenterService {
    public static final Logger LOGGER = LoggerFactory.getLogger(MerchantCenterServiceImpl.class);

    public EMIDetailList getEMIDetailsByMid(String mid) throws Exception {
        String url = ConfigurationUtil.getProperty(PG2_MERCHANT_CENTER_BASE_URL)
                + ConfigurationUtil.getProperty(MERCHANT_CENTER_EMI_URL);
        if (StringUtils.isBlank(url)) {
            LOGGER.error("Merchant center url can't be null or empty");
            throw new Exception("Merchant center url can't be null or empty");
        }
        PG2EmiDetailsRequest pg2EmiDetailsRequest = new PG2EmiDetailsRequest();
        pg2EmiDetailsRequest.setMerchantId(mid);
        JSONObject jsonObject = new JSONObject(pg2EmiDetailsRequest);
        Pg2RestResponse pg2RestResponse = callMerchantCenterForPost(url, jsonObject.toString());
        boolean isSuccessResponse = isSuccessResponse(pg2RestResponse.getResultInfo());
        if (!isSuccessResponse || org.springframework.util.ObjectUtils.isEmpty(pg2RestResponse.getBody())) {
            LOGGER.error("pg2GetEmiDetails : Getting Error Response from Merchant Center");
            throw new Exception("Getting Error Response from Merchant Center");
        }
        Map<Long, EMIDetails> emiDetailsMap = new HashMap<>();
        List<EMIDetails> emiDetailsList = (List<EMIDetails>) pg2RestResponse.getBody();
        Iterator it = emiDetailsList.iterator();
        while (it.hasNext()) {
            ObjectMapper objectMapper = new ObjectMapper();
            EMIDetails emiDetails = objectMapper.convertValue(it.next(), EMIDetails.class);
            emiDetailsMap.put(emiDetails.getId(), emiDetails);
        }
        EMIDetailList emiDetailList = new EMIDetailList(emiDetailsMap);
        return emiDetailList;
    }

    public boolean isSuccessResponse(PG2ResultInfo pg2ResultInfo) {
        if (Objects.nonNull(pg2ResultInfo) && StringUtils.isNotEmpty(pg2ResultInfo.getResultStatus())
                && StringUtils.equalsIgnoreCase(pg2ResultInfo.getResultMsg(), SUCCESS)) {
            return true;
        }
        return false;
    }

    private Pg2RestResponse callMerchantCenterForPost(String url, String requestEntity) throws Exception {
        HttpRequestPayload<String> requestPayload = getHttpPostRequestPayload(url, requestEntity);
        logPayload(url, Type.REQUEST, requestEntity);
        LOGGER.info("MC-REQUEST : Post Request to Merchant Center {} , url {}", requestEntity, url);
        Response response = JerseyHttpClient.sendHttpPostRequest(requestPayload);
        Pg2RestResponse restResponse = response.readEntity(Pg2RestResponse.class);
        logPayload(url, Type.RESPONSE, restResponse.toString());
        LOGGER.info("MC-RESPONSE : {}", restResponse);
        return restResponse;
    }

    public HttpRequestPayload<String> getHttpPostRequestPayload(String url, String requestEntity) {
        HttpRequestPayload<String> requestPayload = new HttpRequestPayload<>();
        requestPayload.setTarget(url);
        requestPayload.setMediaTypeProduced(MediaType.APPLICATION_JSON);
        requestPayload.setMediaTypeConsumed(MediaType.APPLICATION_JSON);
        requestPayload.setEntity(requestEntity);
        requestPayload.setHttpMethod(HttpMethod.POST);
        return requestPayload;
    }
}
