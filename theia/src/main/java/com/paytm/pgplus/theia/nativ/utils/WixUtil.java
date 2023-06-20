package com.paytm.pgplus.theia.nativ.utils;

import com.paytm.pgplus.cache.model.MerchantInfo;
import com.paytm.pgplus.facade.acquiring.models.response.QueryByMerchantTransIdResponse;
import com.paytm.pgplus.logging.ExtendedLogger;
import com.paytm.pgplus.mappingserviceclient.service.impl.MerchantDataServiceImpl;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.response.CustomInitTxnResponse;
import com.paytm.pgplus.response.ResultInfo;
import com.paytm.pgplus.responsecode.utils.MerchantResponseUtil;
import com.paytm.pgplus.theia.nativ.service.ICustomInitiateTransactionService;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.redis.ITheiaTransactionalRedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.MerchantExtendedInfo.MerchantExtendInfoKeys.*;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.ResponseConstants.*;

/**
 * Created by charu on 02/10/18.
 */

@Service
public class WixUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(WixUtil.class);
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(WixUtil.class);

    @Autowired
    private ICustomInitiateTransactionService nativeInitTxnService;

    @Autowired
    MerchantResponseUtil merchantResponseUtil;

    @Autowired
    private ITheiaTransactionalRedisUtil theiaTransactionalRedisUtil;

    @Autowired
    @Qualifier("merchantDataServiceImpl")
    private MerchantDataServiceImpl merchantDataService;

    public boolean isWixWrapper(InitiateTransactionRequestBody requestBody) {
        return requestBody.getExtraParamsMap() != null && requestBody.getExtraParamsMap().containsKey(WRAPPER_NAME)
                && StringUtils.equals((String) requestBody.getExtraParamsMap().get(WRAPPER_NAME), WIX_WRAPPER);
    }

    public void putErrorMsgInRedisForWix(InitiateTransactionRequestBody initiateTransactionRequestBody,
            String resultCode) {
        if (isWixWrapper(initiateTransactionRequestBody)) {
            String wixRedisKey = getWixRedisKey(initiateTransactionRequestBody.getMid(),
                    initiateTransactionRequestBody.getOrderId());
            updateWixErrorInRedis(wixRedisKey, resultCode);
        }
    }

    private Object getWixErrorInRedis(String wixRedisKey) {
        return theiaTransactionalRedisUtil.hget(wixRedisKey, WIX_ERROR_CODE_REDIS_KEY);
    }

    public void updateWixErrorInRedis(String wixRedisKey, String resultCodeId) {
        theiaTransactionalRedisUtil.hset(wixRedisKey, WIX_ERROR_CODE_REDIS_KEY, resultCodeId, 86400);
    }

    public String getWixRedisKey(String mid, String orderId) {
        return WIX_WRAPPER + "_" + mid + "_" + orderId;
    }

    public void updateWixResponseData(CustomInitTxnResponse customInitTxnResponse, ResultInfo resultInfo,
            String orderID, String mid, String resellerParentMid, String type, String redirectUrl) throws Exception {
        String resultStatus = resultInfo.getResultStatus();
        String resultCode = resultInfo.getResultCode();
        String resultMsg = resultInfo.getResultMsg();
        String responseCodeAndMsgForWix = null;
        customInitTxnResponse.setPluginTransactionId(orderID);
        if (StringUtils.equals(resultCode, ResultCode.SUCCESS_IDEMPOTENT_ERROR.getResultCodeId())
                || StringUtils.equals(resultCode, "325")) {
            MerchantInfo merchantInfo = merchantDataService.getMerchantMappingData(mid);
            EXT_LOGGER.customInfo("Mapping response - MerchantInfo :: {}", merchantInfo);
            String alipayMid = merchantInfo.getAlipayId();
            final QueryByMerchantTransIdResponse merchantTransIdResponse = nativeInitTxnService
                    .getMerchantTransIdResponse(orderID, alipayMid, mid);
            nativeInitTxnService.setStatusDetails(merchantTransIdResponse.getBody(), customInitTxnResponse,
                    resellerParentMid, redirectUrl);
        } else if (StringUtils.equals(resultStatus, ResultCode.FAILED.getResultStatus())) {
            customInitTxnResponse.setErrorCode(resultCode);
            customInitTxnResponse.setErrorDescription(resultMsg);
            String wixRedisKey = getWixRedisKey(mid, orderID);
            String resultCodeId = (String) getWixErrorInRedis(wixRedisKey);
            if (resultCodeId != null)
                responseCodeAndMsgForWix = merchantResponseUtil.getReasonCodeFromMapping(resultCodeId,
                        resellerParentMid);
            if (responseCodeAndMsgForWix != null && StringUtils.isNotEmpty(responseCodeAndMsgForWix)) {
                customInitTxnResponse.setReasonCode(Integer.valueOf(responseCodeAndMsgForWix));
            } else if (StringUtils.equals(type, "JWT")) {
                customInitTxnResponse.setReasonCode(WIX_DEFAULT_JWT_CODE);
            } else if (StringUtils.equals(type, "CURRENCY")) {
                customInitTxnResponse.setReasonCode(WIX_CURRENCY_NOT_SUPPORTED_CODE);
            } else {
                customInitTxnResponse.setReasonCode(WIX_DEFAULT_REASON_CODE);
            }
        }
    }
}