package com.paytm.pgplus.theia.promo.service;

import com.paytm.pgplus.cache.enums.MerchantInfoRequest;
import com.paytm.pgplus.cache.model.MerchantInfo;
import com.paytm.pgplus.cache.model.MerchantInfoResponse;
import com.paytm.pgplus.common.enums.ResultCode;
import com.paytm.pgplus.common.model.ResultInfo;
import com.paytm.pgplus.common.signature.JWTWithHmacSHA256;
import com.paytm.pgplus.mappingserviceclient.exception.MappingServiceClientException;
import com.paytm.pgplus.mappingserviceclient.service.IMerchantDataService;
import com.paytm.pgplus.response.ResponseHeader;
import com.paytm.pgplus.theia.promo.model.FetchMerchantInfoRequest;
import com.paytm.pgplus.theia.promo.model.MerchantBaseInfo;
import com.paytm.pgplus.theia.promo.model.FetchMerchantInfoResponse;
import com.paytm.pgplus.theia.promo.model.MerchantInfoResponseBody;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import com.paytm.pgplus.logging.ExtendedLogger;

import java.util.*;

@Service("merchantInfoDataService")
public class MerchantInfoService {

    @Autowired
    @Qualifier("merchantDataServiceImpl")
    private IMerchantDataService merchantDataService;

    private static final Logger LOGGER = LoggerFactory.getLogger(MerchantInfoService.class);
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(MerchantInfoService.class);

    private FetchMerchantInfoResponse prepareMerchantInfoResponse(MerchantInfoResponse merchantInfoResponse,
            FetchMerchantInfoRequest request) {

        FetchMerchantInfoResponse response = new FetchMerchantInfoResponse(new ResponseHeader(),
                new MerchantInfoResponseBody());
        Set<String> merchantVerify = new HashSet<>();

        for (String mid : request.getBody().getMid()) {
            if (StringUtils.isNotBlank(mid)) {
                merchantVerify.add(mid);
            }
        }

        MerchantBaseInfo merchantBaseInfo = null;
        for (MerchantInfo merchantInfo : merchantInfoResponse.getMerchantInfoList()) {
            if (merchantInfo != null && StringUtils.isNotBlank(merchantInfo.getOfficialName())
                    && StringUtils.isNotBlank(merchantInfo.getPaytmId())) {
                merchantBaseInfo = new MerchantBaseInfo(merchantInfo.getPaytmId(), merchantInfo.getOfficialName(), true);
                if (response.getBody().getMerchantBaseInfoList() == null) {
                    response.getBody().setMerchantBaseInfoList(new ArrayList<MerchantBaseInfo>());
                }
                response.getBody().getMerchantBaseInfoList().add(merchantBaseInfo);
                merchantVerify.remove(merchantInfo.getPaytmId());
            }
        }

        for (String mid : merchantVerify) {
            if (StringUtils.isNotBlank(mid)) {
                merchantBaseInfo = new MerchantBaseInfo(mid, "", false);
            }
            response.getBody().getMerchantBaseInfoList().add(merchantBaseInfo);
        }

        setApiResponse(response, merchantInfoResponse);

        return response;
    }

    private void setApiResponse(FetchMerchantInfoResponse response, MerchantInfoResponse merchantInfoResponse) {
        ResultInfo resultInfo = new ResultInfo();
        resultInfo
                .setResultCode(StringUtils.isNotBlank(merchantInfoResponse.getResponse().getResultCode()) ? merchantInfoResponse
                        .getResponse().getResultCode() : ResultCode.PROCESS_FAIL.getCode());
        resultInfo
                .setResultStatus(StringUtils.isNotBlank(merchantInfoResponse.getResponse().getResultStatus()) ? merchantInfoResponse
                        .getResponse().getResultStatus() : ResultCode.PROCESS_FAIL.getResultStatus());
        resultInfo
                .setResultMsg(StringUtils.isNotBlank(merchantInfoResponse.getResponse().getMessaage()) ? merchantInfoResponse
                        .getResponse().getMessaage() : ResultCode.PROCESS_FAIL.getResultMsg());
        response.getBody().setResultInfo(resultInfo);
    }

    /**
     * This method gets merchantInfo from mapping service
     * 
     * @param request
     *            {@link FetchMerchantInfoRequest}
     * @return {@link FetchMerchantInfoResponse}
     */
    public FetchMerchantInfoResponse getMerchantInfoResponse(FetchMerchantInfoRequest request) {
        MerchantInfoResponse merchantInfoResponse = null;

        FetchMerchantInfoResponse response = null;
        try {
            MerchantInfoRequest merchantInfoRequest = new MerchantInfoRequest(request.getBody().getMid());
            LOGGER.info("Getting Data from Mapping Service...");
            merchantInfoResponse = merchantDataService.getMerchantInfoList(merchantInfoRequest);
            EXT_LOGGER.customInfo("Mapping response - MerchantInfoResponse :: {}", merchantInfoResponse);
        } catch (MappingServiceClientException e) {
            response = new FetchMerchantInfoResponse(new ResponseHeader(), new MerchantInfoResponseBody());
            ResultInfo resultInfo = new ResultInfo();

            resultInfo.setResultCode(ResultCode.SYSTEM_ERROR.getCode());
            resultInfo.setResultStatus(ResultCode.SYSTEM_ERROR.getResultStatus());
            resultInfo.setResultMsg(e.getErrorMessage());

            response.getBody().setResultInfo(resultInfo);
            LOGGER.error(ExceptionUtils.getStackTrace(e));
            return response;
        }

        if (isValidResponse(merchantInfoResponse)) {
            response = prepareMerchantInfoResponse(merchantInfoResponse, request);
        } else {
            response = new FetchMerchantInfoResponse(new ResponseHeader(), new MerchantInfoResponseBody());
            ResultInfo resultInfo = new ResultInfo();

            resultInfo.setResultCode(ResultCode.PROCESS_FAIL.getCode());
            resultInfo.setResultStatus(ResultCode.PROCESS_FAIL.getResultStatus());
            if (!(StringUtils.isNotBlank(resultInfo.getResultMsg())))
                resultInfo.setResultMsg(ResultCode.PROCESS_FAIL.getResultMsg());

            response.getBody().setResultInfo(resultInfo);
        }
        return response;
    }

    private boolean isValidResponse(MerchantInfoResponse response) {
        if (response != null && response.getMerchantInfoList() != null && !response.getMerchantInfoList().isEmpty()) {
            return true;
        }
        LOGGER.info("Validation Failed for Response : {}", response);
        return false;
    }

    public boolean isValidRequest(FetchMerchantInfoRequest request) {
        if (request != null && request.getBody().getMid() != null && !request.getBody().getMid().isEmpty()
                && validateToken(request)) {
            return true;
        }
        LOGGER.info("Validation Failed for request : {}", request);
        return false;
    }

    private boolean validateToken(FetchMerchantInfoRequest request) {
        if (!verifyJwtToken(request)) {
            LOGGER.error("JWT Validation failed returning response");
            return false;
        }
        EXT_LOGGER.customInfo("JWT validated successfully");
        return true;
    }

    private boolean verifyJwtToken(FetchMerchantInfoRequest request) {
        Map<String, String> jwtMap = new HashMap<>();
        String midList = StringUtils.join(request.getBody().getMid(), ",");
        jwtMap.put("mids", midList);

        return JWTWithHmacSHA256.verifyJsonWebToken(jwtMap, request.getHead().getToken());
    }
}
