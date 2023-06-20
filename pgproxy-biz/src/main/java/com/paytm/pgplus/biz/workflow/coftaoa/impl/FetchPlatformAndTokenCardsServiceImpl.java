package com.paytm.pgplus.biz.workflow.coftaoa.impl;

import com.paytm.pgplus.biz.workflow.coftaoa.FetchPlatformAndTokenCardsRequestBuilder;
import com.paytm.pgplus.biz.workflow.coftaoa.FetchPlatformAndTokenCardsService;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.dynamicwrapper.utils.JSONUtils;
import com.paytm.pgplus.facade.coft.model.FetchPlatformAndTokenCardsRequest;
import com.paytm.pgplus.facade.coft.model.FetchPlatformAndTokenCardsResponse;
import com.paytm.pgplus.facade.enums.SaveCardServiceUrl;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.facade.utils.LogUtil;
import com.paytm.pgplus.httpclient.HttpRequestPayload;
import com.paytm.pgplus.httpclient.JerseyHttpClient;
import com.paytm.pgplus.httpclient.enums.HttpMethod;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import static com.paytm.pgplus.facade.enums.ExternalEntity.SAVE_CARD_SERVICE;
import static com.paytm.pgplus.facade.enums.Type.REQUEST;
import static com.paytm.pgplus.facade.enums.Type.RESPONSE;

@Service("fetchPlatformAndTokenCardsServiceImpl")
public class FetchPlatformAndTokenCardsServiceImpl implements FetchPlatformAndTokenCardsService {
    private static final Logger LOGGER = LoggerFactory.getLogger(FetchPlatformAndTokenCardsServiceImpl.class);
    private static final String FETCH_PLATFORM_TOKENS_CARD = "FETCH_PLATFORM_AND_TOKENS_CARD";

    @Qualifier("fetchPlatformAndTokenCardsRequestBuilderImpl")
    @Autowired
    private FetchPlatformAndTokenCardsRequestBuilder requestBuilder;

    @Override
    public GenericCoreResponseBean<FetchPlatformAndTokenCardsResponse> getAllPlatformAndTokenCards(
            WorkFlowRequestBean flowRequestBean) {
        long startTime = System.currentTimeMillis();
        try {
            final FetchPlatformAndTokenCardsRequest request = requestBuilder.buildRequest(flowRequestBean);
            final HttpRequestPayload<String> payload = generatePayload(request);

            LogUtil.logPayload(SAVE_CARD_SERVICE, FETCH_PLATFORM_TOKENS_CARD, REQUEST, payload.getEntity());
            final Response response = JerseyHttpClient.sendHttpPostRequest(payload);
            if (response != null) {
                FetchPlatformAndTokenCardsResponse res = CoftAoaHelper.getResponse(response,
                        FetchPlatformAndTokenCardsResponse.class);
                String responseString = JSONUtils.toJsonString(res);
                LogUtil.logPayload(SAVE_CARD_SERVICE, FETCH_PLATFORM_TOKENS_CARD, RESPONSE, responseString);
                return new GenericCoreResponseBean<>(res);
            }
            return null;
        } catch (Exception e) {
            LOGGER.error("Exception occurred in /getAllPlatformAndTokenCards API : {}", e);
            return new GenericCoreResponseBean<>(e.getMessage());
        } finally {
            LOGGER.info("Total time taken by /getAllPlatformAndTokenCards API : {} ms", System.currentTimeMillis()
                    - startTime);
        }
    }

    private HttpRequestPayload<String> generatePayload(FetchPlatformAndTokenCardsRequest request)
            throws FacadeCheckedException {

        final HttpRequestPayload<String> payload = new HttpRequestPayload<>();
        String url = SaveCardServiceUrl.FETCH_PLATFORM_TOKENS_CARD.getUrl();
        final MultivaluedMap<String, Object> headerMap = CoftAoaHelper.prepareCommonHeaderMap();

        payload.setHeaders(headerMap);

        payload.setTarget(url);

        payload.setHttpMethod(HttpMethod.POST);
        payload.setMediaTypeProduced(MediaType.APPLICATION_JSON);
        payload.setMediaTypeConsumed(MediaType.APPLICATION_JSON);

        final String req = JsonMapper.mapObjectToJson(request);
        payload.setEntity(req);
        return payload;
    }
}
