package com.paytm.pgplus.theia.services.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.utils.ErrorCodeConstants;
import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.cache.model.MerchantPreferenceInfoResponse;
import com.paytm.pgplus.checksum.crypto.EncryptionFactory;
import com.paytm.pgplus.checksum.crypto.IEncryption;
import com.paytm.pgplus.checksum.exception.SecurityException;
import com.paytm.pgplus.checksum.utils.AESMerchantService;
import com.paytm.pgplus.checksum.utils.CryptoUtils;
import com.paytm.pgplus.common.config.ConfigurationUtil;
import com.paytm.pgplus.common.signature.wrapper.SignatureUtilWrapper;
import com.paytm.pgplus.facade.utils.VaultPropertyUtil;
import com.paytm.pgplus.logging.ExtendedLogger;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.cache.model.MerchantPreferenceStore;
import com.paytm.pgplus.theia.constants.TheiaConstant.MerchantPreference.PreferenceKeys;
import com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams;
import com.paytm.pgplus.theia.models.EncryptedParameterRequest;
import com.paytm.pgplus.theia.utils.HttpRequestWithModifiableParameters;
import com.paytm.pgplus.theia.utils.MerchantExtendInfoUtils;
import com.paytm.pgplus.theia.utils.MerchantPreferenceProvider;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.crypto.Cipher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import static com.paytm.pgplus.common.util.CommonConstants.CKSUM_KEY_SUFFIX;
import static com.paytm.pgplus.common.util.CommonConstants.ENC_DEC_KEY_SUFFIX;
import static com.paytm.pgplus.facade.constants.FacadeConstants.BANK_OAUTH_PUBLIC_KEY;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.CLIENT_ID;
import static com.paytm.pgplus.theia.utils.ConfigurationUtil.isRedisOPtimizedFlow;

@Service
public class EncryptedParamsRequestServiceHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(EncryptedParamsRequestServiceHelper.class);
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(EncryptedParamsRequestServiceHelper.class);

    private static final String CHECKSUM_KEY = "checksum_key";
    private static final String ENCRYPT_KEY = "encrypt_key";

    @Autowired
    Environment env;

    @Autowired
    private MerchantExtendInfoUtils merchantExtendInfoUtils;

    @Autowired
    @Qualifier("merchantPreferenceService")
    private IMerchantPreferenceService merchantPreferenceService;

    @Autowired
    @Qualifier("merchantPreferenceProvider")
    private MerchantPreferenceProvider merchantPreferenceProvider;

    @Autowired
    private AESMerchantService aesMerchantService;

    @Autowired
    private Ff4jUtils ff4jUtils;

    private static Class<?> redisSessionValveClazz;

    private final Base64 base64 = new Base64();

    public static String encrypt(String passcode) throws Exception {
        String pubKey = VaultPropertyUtil.getProperty(BANK_OAUTH_PUBLIC_KEY);
        KeyFactory kf = KeyFactory.getInstance("RSA", "BC");
        PublicKey publicKey = kf.generatePublic(new X509EncodedKeySpec(java.util.Base64.getDecoder().decode(pubKey)));
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWITHSHA-256ANDMGF1PADDING", "BC");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedBytes = cipher.doFinal(passcode.getBytes());
        String encrypted = java.util.Base64.getEncoder().encodeToString(encryptedBytes);
        return encrypted;
    }

    public EncryptedParameterRequest wrapHttpRequestIfEncrypted(HttpServletRequest request,
            HttpServletResponse response, MerchantPreferenceInfoResponse merchantPreferenceInfoResponse)
            throws Exception {
        EncryptedParameterRequest encryptedParameterRequest = getEncParamsRequestIfExists(request,
                merchantPreferenceInfoResponse);
        if (null != encryptedParameterRequest && StringUtils.isBlank(encryptedParameterRequest.getErrorCode())
                && null != encryptedParameterRequest.getRequest()) {
            LOGGER.info("ENC_DATA request encountered ");
            request = encryptedParameterRequest.getRequest();
            if (request != null && request.getParameterMap() != null)
                EXT_LOGGER.customInfo("Encrypted Parameter request : {}",
                        new ObjectMapper().writeValueAsString(request.getParameterMap()));
        } else if (null != encryptedParameterRequest
                && StringUtils.isNotBlank(encryptedParameterRequest.getErrorCode())) {
            LOGGER.error("Error occured while decrypting ENC_PARAMS {}-{}", encryptedParameterRequest.getErrorCode(),
                    encryptedParameterRequest.getErrorMsg());
            throw new Exception(encryptedParameterRequest.getErrorCode());
        }

        return encryptedParameterRequest;
    }

    private void setMDCForEncRequest(Map<String, Object> attributeMap) {
        if (attributeMap != null && !attributeMap.isEmpty()) {
            for (Entry<String, Object> entry : attributeMap.entrySet()) {
                org.jboss.logging.MDC.put(entry.getKey(), entry.getValue());
                // Setting OrderId in MDC as it's not set in thread interceptor
                MDC.put(entry.getKey(), (String) entry.getValue());
            }
        }
    }

    public void invokeCustomValve(Map<String, Object> attributeMap) {

        try {
            if (null == redisSessionValveClazz) {
                redisSessionValveClazz = this.getClass().getClassLoader().getParent()
                        .loadClass("com.paytm.pgplus.session.valve.GlobalSessionValve");
            }
            Method method = redisSessionValveClazz.getMethod("addCustomAttributesInRequest", Map.class);
            method.invoke(null, attributeMap);

        } catch (Exception e) {
            LOGGER.error("Exception : {}", e);
        }
    }

    public void setAttributesInRequest(Map<String, Object> attributeMap) {
        EXT_LOGGER.customInfo("setAttributesInRequest method invoked");
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                    .getRequest();
            if (attributeMap != null) {
                Set<Entry<String, Object>> entrySet = attributeMap.entrySet();
                for (Entry<String, Object> entry : entrySet) {
                    request.setAttribute(entry.getKey(), entry.getValue());
                }
            }

        } catch (Exception e) {
            LOGGER.error("Exception : {}", e);
        }
    }

    public Map<String, Object> buildCustomAttributeMap(HttpServletRequest decryptRequest) {
        Map<String, Object> overrideAttributeMap = new HashMap<String, Object>();
        overrideAttributeMap.put(RequestParams.ORDER_ID, decryptRequest.getParameter(RequestParams.ORDER_ID));
        overrideAttributeMap.put(RequestParams.MID, decryptRequest.getParameter(RequestParams.MID));
        return overrideAttributeMap;
    }

    private EncryptedParameterRequest validateAndDecryptParams(HttpServletRequest request, boolean isAES256Encrypted) {
        EncryptedParameterRequest encryptedReqOutputTO = new EncryptedParameterRequest();
        String mid = request.getParameter(RequestParams.MID);
        String encParams = request.getParameter(RequestParams.ENC_PARAMS);
        String clientId = request.getParameter(CLIENT_ID);
        String merchantKey = merchantExtendInfoUtils.getMerchantKey(mid, clientId);

        if (isAES256Encrypted) {
            merchantKey = aesMerchantService.fetchAesEncDecKey(mid);
        }

        if (StringUtils.isEmpty(merchantKey) || StringUtils.isBlank(merchantKey)) {
            EXT_LOGGER.customError("merchantKey details is empty or blank");
            encryptedReqOutputTO.setErrorCode("303");
            encryptedReqOutputTO.setErrorMsg("KEY EXPIRED OR KEY NOT DEFINED");
        } else {
            HttpServletRequest decryptRequest = decryptParamsTORequest(request, encParams, merchantKey,
                    isAES256Encrypted);
            if (null != decryptRequest) {
                encryptedReqOutputTO.setRequest(decryptRequest);
                Map<String, Object> attributeMap = buildCustomAttributeMap(decryptRequest);

                if (ff4jUtils.isFeatureEnabled(BizConstant.Ff4jFeature.DISABLE_CUSTOM_VALVE_TO_ADD_ATTRIBUTES, false)) {
                    setAttributesInRequest(attributeMap);
                } else {
                    invokeCustomValve(attributeMap);
                }

                setMDCForEncRequest(attributeMap);
            } else {
                encryptedReqOutputTO.setErrorCode(ErrorCodeConstants.INVALID_ENC_PARAMS);
                encryptedReqOutputTO.setErrorMsg("Invalid encrypted params");
            }
        }
        return encryptedReqOutputTO;
    }

    private Map<String, String[]> buildRequestMapFromEncParam(String stringToDecrypt, String merchantKey) {
        try {
            String decMerchantkey = null;
            decMerchantkey = CryptoUtils.decrypt(merchantKey);
            String rawData = CryptoUtils.decrypt(stringToDecrypt, decMerchantkey);
            // LOGGER.info("Request Paramters in ENC_DATA request : {}",
            // rawData);
            EXT_LOGGER.customInfo("Request Paramters in ENC_DATA request : {}", rawData);

            if (StringUtils.isNotBlank(rawData)) {
                Map<String, String[]> params = new TreeMap<String, String[]>();
                String[] keyValuePair = null;
                String key = null;
                String value = null;
                String[] tokens = rawData.split("\\|");
                for (String token : tokens) {
                    if (StringUtils.isNotBlank(token)) {
                        keyValuePair = token.split("=", 2);
                        if (keyValuePair.length > 1) {
                            key = keyValuePair[0].trim();
                            value = keyValuePair[1].trim();
                            params.put(key, new String[] { value });
                        } else if (keyValuePair.length > 0) {
                            key = keyValuePair[0].trim();
                            value = "";
                            params.put(key, new String[] { value });
                        }
                    }
                }
                return params;
            }
        } catch (SecurityException e) {
            LOGGER.error("Error occured while decrypting merhchant encrypted params ", e);
        }
        return null;
    }

    private Map<String, String[]> buildSpecialRequestMapFromEncParam(String stringToDecrypt, String merchantKey) {
        try {
            LOGGER.info("ENC_DATA {} Merchant Key {}", stringToDecrypt, merchantKey);
            String rawData = CryptoUtils.decrypt(stringToDecrypt, merchantKey);
            // LOGGER.info("Request Paramters in ENC_DATA request : {}",
            // rawData);
            EXT_LOGGER.customInfo("Request Paramters in ENC_DATA request : {}", rawData);

            if (StringUtils.isNotBlank(rawData)) {
                Map<String, String[]> params = new TreeMap<String, String[]>();
                String[] keyValuePair = null;
                String key = null;
                String value = null;
                String[] tokens = rawData.split("\\|");
                for (String token : tokens) {
                    if (StringUtils.isNotBlank(token)) {
                        keyValuePair = token.split("=", 2);
                        if (keyValuePair.length > 1) {
                            key = keyValuePair[0].trim();
                            value = keyValuePair[1].trim();
                            params.put(key, new String[] { value });
                        } else if (keyValuePair.length > 0) {
                            key = keyValuePair[0].trim();
                            value = "";
                            params.put(key, new String[] { value });
                        }
                    }
                }
                return params;
            }
        } catch (SecurityException e) {
            LOGGER.error("Error occured while decrypting merhchant encrypted params ", e);
        }
        return null;
    }

    public HttpServletRequest decryptParamsTORequest(HttpServletRequest request, String encryptedParams,
            String merchantKey, boolean isAesEncrypted) {
        Map<String, String[]> requestParams;
        EXT_LOGGER.customError("Decrypt Paramas to Request ");
        if (isAesEncrypted) {
            requestParams = buildSpecialRequestMapFromEncParam(encryptedParams, merchantKey);
        } else {
            requestParams = buildRequestMapFromEncParam(encryptedParams, merchantKey);
        }
        EXT_LOGGER.customError("After Decrypting ENC_DATA ,request param : {}", requestParams);
        if (null != requestParams) {
            return new HttpRequestWithModifiableParameters(request, requestParams);
        }
        return null;
    }

    public String fetchAesEncDecKey(String mid) {
        String keyName = ConfigurationUtil.getProperty(mid + ENC_DEC_KEY_SUFFIX);
        String enckey = null;
        if (StringUtils.isNotBlank(keyName)) {
            LOGGER.info("Getting Value for Keyname {}", keyName);
            enckey = env.getProperty(keyName);
            if (StringUtils.isBlank(enckey)) {
                enckey = SignatureUtilWrapper.sbiEncDecKey;
                LOGGER.info("The merchant enc/dec key is not fetched from vault");
            }
            return (new String(base64.decode(enckey), Charset.forName("UTF-8")));
        } else {
            LOGGER.info("Value Not Found in Vault, Getting from Mapping Service {} ", ENCRYPT_KEY);
            return getDecryptedKey(mid, ENCRYPT_KEY);
        }
    }

    private String getDecryptedKey(String mid, String clientId) {
        String enckey = null;
        String merchantKey = merchantExtendInfoUtils.getMerchantKey(mid, clientId);

        LOGGER.info("Encrypted Merchant Key {}, clientId {} ", merchantKey, clientId);
        IEncryption encryption = EncryptionFactory.getEncryptionInstance("AES");
        try {
            enckey = encryption.decrypt(merchantKey);
            LOGGER.info("Decrypted Merchant Key {}", enckey);
        } catch (SecurityException e) {
            LOGGER.error("Exception occurred while fetching encrypt_key{}", e);
        }
        return enckey;
    }

    public String fetchAesChecksumKey(String mid) {
        String keyName = ConfigurationUtil.getProperty(mid + CKSUM_KEY_SUFFIX);
        String enckey = null;
        if (StringUtils.isNotBlank(keyName)) {
            enckey = env.getProperty(keyName);
            if (org.apache.commons.lang.StringUtils.isBlank(enckey)) {
                enckey = SignatureUtilWrapper.sbiChecksumKey;
                LOGGER.info("The merchant checksum key is NOT fetched from vault");
            }
            return (new String(base64.decode(enckey), Charset.forName("UTF-8")));
        } else {
            LOGGER.info("Value Not Found in Vault, Getting from Mapping Service for {} ", CHECKSUM_KEY);
            return getDecryptedKey(mid, CHECKSUM_KEY);
        }
    }

    private EncryptedParameterRequest getEncParamsRequestIfExists(HttpServletRequest request,
            MerchantPreferenceInfoResponse merchantPreferenceInfoResponse) {

        String mid = request.getParameter(RequestParams.MID);
        EXT_LOGGER.customInfo("Decrypting ENC_DATA Request if exists {} for mid {}  : ",
                request.getParameter(RequestParams.ENC_PARAMS), mid);
        if (StringUtils.isNotBlank(request.getParameter(RequestParams.ENC_PARAMS)) && StringUtils.isNotBlank(mid)) {
            boolean encParamPreferenceEnabled = false;
            boolean isAES256Encrypted = false; /*
                                                * a flag basically to check
                                                * whether a aes-256 encrypted
                                                * request
                                                */
            MerchantPreferenceStore merchantPreferenceStore = null;
            if (isRedisOPtimizedFlow() && null != merchantPreferenceInfoResponse) {
                merchantPreferenceStore = merchantPreferenceProvider.parseResponse(merchantPreferenceInfoResponse);
            } else {
                merchantPreferenceStore = merchantPreferenceService.getMerchantPreferenceStore(mid);
            }
            if (StringUtils.isNotBlank(request.getParameter(RequestParams.CHECKSUMHASH))
                    && merchantPreferenceProvider.isEncRequestEnabled(merchantPreferenceStore)) {
                encParamPreferenceEnabled = true;
            } else if (merchantPreferenceProvider.isAES256EncRequestEnabled(merchantPreferenceStore)) {
                encParamPreferenceEnabled = true;
                isAES256Encrypted = true;
            } else {
                LOGGER.info("Merchant does not have the {} set", PreferenceKeys.ENC_PARAMS_ENABLED);
            }
            if (encParamPreferenceEnabled) {
                EXT_LOGGER.customInfo("Validating and decrypting enc param for mid : {}  ", mid);
                return validateAndDecryptParams(request, isAES256Encrypted);
            }
        }
        return null;
    }
}
