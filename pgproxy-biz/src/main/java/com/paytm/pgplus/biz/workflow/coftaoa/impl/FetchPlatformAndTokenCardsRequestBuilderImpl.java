package com.paytm.pgplus.biz.workflow.coftaoa.impl;

import com.paytm.pgplus.biz.utils.FF4JUtil;
import com.paytm.pgplus.biz.workflow.coftaoa.FetchPlatformAndTokenCardsRequestBuilder;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.enums.TokenType;
import com.paytm.pgplus.facade.coft.model.FetchPlatformAndTokenCardsRequest;
import com.paytm.pgplus.facade.coft.model.FetchPlatformAndTokenCardsRequestBody;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.request.SecureRequestHeader;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static com.paytm.pgplus.biz.utils.BizConstant.Ff4jFeature.COFT_AOA_RETURN_TOKEN_CARDS;
import static com.paytm.pgplus.biz.utils.BizConstant.Ff4jFeature.COFT_AOA_RETURN_TOKEN_CARDS_SUBSCRIPTION;

@Service("fetchPlatformAndTokenCardsRequestBuilderImpl")
public class FetchPlatformAndTokenCardsRequestBuilderImpl implements FetchPlatformAndTokenCardsRequestBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(FetchPlatformAndTokenCardsRequestBuilderImpl.class);

    @Autowired
    private Environment environment;

    @Autowired
    private FF4JUtil ff4JUtil;

    @Override
    public FetchPlatformAndTokenCardsRequest buildRequest(WorkFlowRequestBean requestBean)
            throws FacadeCheckedException {

        FetchPlatformAndTokenCardsRequest request = new FetchPlatformAndTokenCardsRequest();
        SecureRequestHeader header = new SecureRequestHeader();
        FetchPlatformAndTokenCardsRequestBody body = new FetchPlatformAndTokenCardsRequestBody();
        String mid = requestBean.getPaytmMID();
        String custId = requestBean.getCustID();
        if (StringUtils.isBlank(mid) || StringUtils.isBlank(custId)) {
            throw new FacadeCheckedException("MID OR CUST ID can't be blank");
        }
        header.setTokenType(TokenType.JWT.getType());
        header.setToken(getJwt(mid, custId));
        body.setMid(mid);
        body.setCustId(custId);
        boolean returnTokenCards;

        if (ERequestType.NATIVE_SUBSCRIPTION_PAY.equals(requestBean.getRequestType())
                || ERequestType.NATIVE_MF_SIP_PAY.equals(requestBean.getRequestType())) {
            returnTokenCards = ff4JUtil.isFeatureEnabled(COFT_AOA_RETURN_TOKEN_CARDS_SUBSCRIPTION,
                    requestBean.getPaytmMID());
        } else {
            returnTokenCards = ff4JUtil.isFeatureEnabled(COFT_AOA_RETURN_TOKEN_CARDS, requestBean.getPaytmMID());
        }
        body.setFilterTokenCards(!returnTokenCards);

        body.setAdditionalFieldsRequired(true);
        request.setHead(header);
        request.setBody(body);
        return request;
    }

    public String getJwt(String mid, String custId) throws FacadeCheckedException {
        Map<String, String> jwtClaims = new HashMap<>();
        jwtClaims.put("mid", mid);
        jwtClaims.put("custId", custId);
        String secretKey = environment.getProperty("jwt.saveCard.key");
        String jwt = CoftAoaHelper.createJWToken(jwtClaims, secretKey);
        if (StringUtils.isBlank(jwt)) {
            LOGGER.error("jwt token is null");
            throw new FacadeCheckedException("Jwt token is null");
        }
        return jwt;
    }
}
