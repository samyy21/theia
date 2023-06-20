package com.paytm.pgplus.biz.workflow.service.util;

import com.paytm.pgplus.biz.core.model.request.FailureLogBean;
import com.paytm.pgplus.biz.core.notification.service.IFailureLogService;
import com.paytm.pgplus.biz.utils.AlipayRequestUtils;
import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.utils.EventUtils;
import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.common.enums.EventNameEnum;
import com.paytm.pgplus.logging.ExtendedLogger;
import com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant;
import com.paytm.pgplus.theia.redis.ITheiaSessionRedisUtil;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Map;

@Service("failureLogUtil")
public class FailureLogUtil {

    @Autowired
    @Qualifier("theiaSessionRedisUtil")
    private ITheiaSessionRedisUtil theiaSessionRedisUtil;

    @Autowired
    Ff4jUtils ff4JUtils;

    @Autowired
    private IFailureLogService failureLogService;

    public static final Logger LOGGER = LoggerFactory.getLogger(FailureLogUtil.class);
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(FailureLogUtil.class);

    private void pushFailureLogToDwhKafkaTopic(FailureLogBean failureLogBean, String redisKey) {
        try {
            long startTime = System.currentTimeMillis();
            failureLogService.pushFailureLogToKafka(failureLogBean);
            EXT_LOGGER.customInfo("Total time to push Failure log on DWH Kafka Topic: {}",
                    (System.currentTimeMillis() - startTime));
        } catch (Exception e) {
            EventUtils.pushTheiaEvents(EventNameEnum.FAILURE_LOG_KAFKA_ERROR, new ImmutablePair<>(
                    "Error While sending Failure log to DWH Kafka Topic", e.getMessage()));
        }
    }

    private String getParameter(Map<String, String[]> paramMap, String name) {
        String[] strings = paramMap.get(name);
        if (strings != null) {
            return strings[0];
        }
        return null;
    }

    public void setFailureMsgForDwhPush(String errorCode, String errorMessage, String apiName, boolean isTheiaError) {
        try {
            HttpServletRequest httpServletRequest = AlipayRequestUtils.httpServletRequest();
            String redisKey = getFailureMessageKey(httpServletRequest);
            if (redisKey != null && BooleanUtils.isFalse(theiaSessionRedisUtil.isExist(redisKey))) {
                FailureLogBean failureLogBean = new FailureLogBean();
                failureLogBean.setCreateDateAndTime(new Date());
                if (isTheiaError) {
                    failureLogBean.setTheiaErrorCode(errorCode);
                    failureLogBean.setTheiaErrorMessage(errorMessage);
                } else {
                    failureLogBean.setApiName(apiName);
                    failureLogBean.setApiErrorCode(errorCode);
                    failureLogBean.setApiErrorMessage(errorMessage);
                }
                theiaSessionRedisUtil.hset(redisKey, BizConstant.FailureLogs.FAILURE_LOG_BEAN, failureLogBean, 900);
            }
        } catch (Exception e) {
            LOGGER.error("Error while setting failure msg for DWH push: {}", e.getMessage());
        }

    }

    private String getFailureMessageKey(HttpServletRequest httpServletRequest) {
        if (httpServletRequest != null) {
            if (BooleanUtils.isTrue((Boolean) httpServletRequest
                    .getAttribute(BizConstant.FailureLogs.IS_ORDER_ID_NEED_TO_BE_GENERATED))) {
                return httpServletRequest.getHeader(TheiaConstant.RequestHeaders.X_PGP_UNIQUE_ID);
            } else {
                String mid = (String) httpServletRequest.getAttribute(TheiaConstant.RequestParams.MID);
                String orderId = (String) httpServletRequest.getAttribute(TheiaConstant.RequestParams.ORDER_ID);
                return mid + BizConstant.FailureLogs.DELIMITER + orderId;
            }
        }
        return null;
    }

    public void pushFailureLogToDwhKafka(Map<String, String[]> paramMap, String theiaApiName) {
        try {
            HttpServletRequest request = AlipayRequestUtils.httpServletRequest();
            String redisKey = getFailureMessageKey(request);
            if (redisKey != null
                    && BooleanUtils.isTrue(theiaSessionRedisUtil.isExist(redisKey)
                            && (ff4JUtils.isFeatureEnabledOnMid(
                                    (String) request.getAttribute(TheiaConstant.RequestParams.MID),
                                    BizConstant.Ff4jFeature.ENABLE_FAILURE_LOG_TO_DWH_KAFKA, false)))) {
                FailureLogBean failureLogBean = (FailureLogBean) theiaSessionRedisUtil.hget(redisKey,
                        BizConstant.FailureLogs.FAILURE_LOG_BEAN);
                failureLogBean.setMid((String) request.getAttribute(TheiaConstant.RequestParams.MID));
                failureLogBean
                        .setOrderId(request.getAttribute(TheiaConstant.RequestParams.ORDER_ID) != null ? (String) request
                                .getAttribute(TheiaConstant.RequestParams.ORDER_ID) : request
                                .getHeader(TheiaConstant.RequestHeaders.X_PGP_UNIQUE_ID));
                failureLogBean.setUniqueId(request.getHeader(TheiaConstant.RequestHeaders.X_PGP_UNIQUE_ID));
                failureLogBean.setTheiaApiName(theiaApiName);
                failureLogBean.setPaymentMode(getParameter(paramMap, TheiaConstant.RequestParams.Native.PAYMENT_MODE));
                failureLogBean.setChannelCode(getParameter(paramMap, TheiaConstant.RequestParams.CHANNEL_ID));
                pushFailureLogToDwhKafkaTopic(failureLogBean, redisKey);
            }
        } catch (Exception e) {
            LOGGER.error("Error while pushing failure logs to DWH kafka: {}", e.getMessage());
        }
    }

    public void deleteKeyFromRedis(HttpServletRequest request) {
        try {
            String redisKey = getFailureMessageKey(request);
            if (theiaSessionRedisUtil.isExist(redisKey)) {
                theiaSessionRedisUtil.hdel(redisKey, BizConstant.FailureLogs.FAILURE_LOG_BEAN);
            }
        } catch (Exception e) {
            LOGGER.error("Error while deleting failure log bean from redis: {}", e.getMessage());
        }
    }
}
