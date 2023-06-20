package com.paytm.pgplus.theia.aspects;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.kafkabase.theia.models.NotFoundTranslationStrings;
import com.paytm.pgplus.kafkabase.v2.producer.IKafkaProducer;
import com.paytm.pgplus.localisationProcessor.annotation.LocaleAPI;
import com.paytm.pgplus.logging.ExtendedLogger;
import com.paytm.pgplus.theia.redis.ITheiaSessionRedisUtil;
import com.paytm.pgplus.theia.sns.model.LocalizationRequestBody;
import com.paytm.pgplus.theia.sns.service.SNSService;
import com.paytm.pgplus.theia.utils.LocalizationUtil;
import com.paytm.pgplus.theia.utils.ReflectionUtil;
import org.apache.commons.collections.CollectionUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.paytm.pgplus.biz.utils.BizConstant.Ff4jFeature.ENABLE_PUBLISH_TO_SNS_FOR_TRANSLATION;

@Aspect
@Service
public class LocaleFieldAspect {

    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(LocaleFieldAspect.class);
    @Autowired
    @Qualifier("theiaSessionRedisUtil")
    ITheiaSessionRedisUtil theiaSessionRedisUtil;

    @Autowired
    @Qualifier(value = "kafkaProducer")
    private IKafkaProducer kafkaProducer;
    @Autowired
    @Qualifier(value = "snsService")
    private SNSService snsService;

    @Autowired
    private LocalizationUtil localizationUtil;

    @Autowired
    private Ff4jUtils ff4jUtil;

    private static final Logger LOGGER = LoggerFactory.getLogger(LocaleFieldAspect.class);

    @Pointcut("@annotation(com.paytm.pgplus.localisationProcessor.annotation.LocaleAPI)")
    public void methodAnnotatedWithLocaleAPI() {
    }

    @AfterReturning(pointcut = "methodAnnotatedWithLocaleAPI()", returning = "object")
    private void modifyLocaleFieldsInAPIResponse(JoinPoint joinPoint, Object object) throws Throwable {
        LocaleAPI localeAPI = ((MethodSignature) joinPoint.getSignature()).getMethod().getAnnotation(
                com.paytm.pgplus.localisationProcessor.annotation.LocaleAPI.class);
        if (localeAPI.isResponseObjectType()) {
            LOGGER.info("Adding locale field values in API object " + localeAPI.apiName());
            addLocaleFieldsInObject(object, localeAPI.apiName());
        }
    }

    public void addLocaleFieldsInObject(Object responseObject, String apiName) {
        String languageCode = localizationUtil.getLanguageCodeFromRequest();
        String mid = localizationUtil.getMIDFromHeader();
        EXT_LOGGER.customInfo(String.format("Language Code in header found as %s for API %s", languageCode, apiName));
        if (!localizationUtil.isLocaleEnabled()) {
            // translation not required
            // todo: move in common in phase2
            return;
        }
        if (responseObject != null) {
            Map<String, String> localeStringMap = (Map<String, String>) theiaSessionRedisUtil.hget(apiName,
                    languageCode);
            if (localeStringMap == null) {
                localeStringMap = new HashMap<>();
            }
            EXT_LOGGER.customInfo("Populating locale fields values received from redis for MID " + mid);
            Set<String> notFoundText = new HashSet<>();
            ReflectionUtil.populateLocaleFieldsInApiResponse(responseObject, localeStringMap, notFoundText);
            notFoundText.remove(null);
            if (CollectionUtils.isNotEmpty(notFoundText)) {
                if (ff4jUtil.isFeatureEnabled(ENABLE_PUBLISH_TO_SNS_FOR_TRANSLATION, false)) {
                    LocalizationRequestBody localizationRequestBody = new LocalizationRequestBody(apiName, notFoundText);
                    try {
                        String localizationRequest = new ObjectMapper().writeValueAsString(localizationRequestBody);
                        snsService.publish(localizationRequest);
                        LOGGER.info("Localization SNS request sent for translation : {}", localizationRequest);
                    } catch (Exception e) {
                        LOGGER.error("Exception while pushing data into sns ", e);
                    }
                } else {
                    NotFoundTranslationStrings notFoundStrings = new NotFoundTranslationStrings(apiName, notFoundText);
                    try {
                        kafkaProducer.produceAsGenericKafkaObject("THEIA_LOCALIZED_TEXT", notFoundStrings);
                        LOGGER.info("Kafka request sent: " + notFoundStrings.toString());
                    } catch (Exception e) {
                        LOGGER.error("Exception while pushing data into kafka ", e);
                    }
                }
            }
        }
    }

}
