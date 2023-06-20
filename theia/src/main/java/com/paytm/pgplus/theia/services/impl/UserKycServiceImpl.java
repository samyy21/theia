package com.paytm.pgplus.theia.services.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.DatatypeConverter;

import com.paytm.pgplus.theia.nativ.model.kyc.NativeKYCDetailRequest;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.paytm.pgplus.checksum.crypto.impl.AesEncryptionKycImpl;
import com.paytm.pgplus.common.config.ConfigurationUtil;
import com.paytm.pgplus.common.signature.JWTKycWithHmacSHA256;
import com.paytm.pgplus.facade.ControlCenter;
import com.paytm.pgplus.facade.user.models.UserDocument;
import com.paytm.pgplus.facade.user.models.request.UserKycData;
import com.paytm.pgplus.facade.user.models.request.UserProfileSaveRequest;
import com.paytm.pgplus.facade.user.models.response.UserProfileSaveResponse;
import com.paytm.pgplus.facade.user.services.ISaveUserProfile;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.pgproxycommon.exception.KycValidationException;
import com.paytm.pgplus.theia.models.KycRequestModel;
import com.paytm.pgplus.theia.services.ITheiaSessionDataService;
import com.paytm.pgplus.theia.services.IUserKycService;
import com.paytm.pgplus.theia.sessiondata.LoginInfo;
import com.paytm.pgplus.theia.sessiondata.OAuthUserInfo;

/**
 * @author kartik
 * @date 06-Mar-2018
 */
@Service("userKycServiceImpl")
public class UserKycServiceImpl implements IUserKycService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserKycServiceImpl.class);

    @Autowired
    @Qualifier("saveUserProfileImpl")
    private ISaveUserProfile saveUserProfileImpl;

    @Autowired
    @Qualifier("theiaSessionDataService")
    ITheiaSessionDataService sessionDataService;

    private static final String jwtTokenTimeStamp = "ts";
    private static final String jwtTokenKycProfileId = "kyc_profile_id";

    private static final String KYC_NAME_ON_DOC = "KYC_NAME_ON_DOC";
    private static final String KYC_DOC_VALUE = "KYC_DOC_VALUE";
    private static final String KYC_DOC_CODE = "KYC_DOC_CODE";

    private static final String secretKey;
    private static final String initVector;
    private static final byte[] secretKeyHex;
    private static final byte[] initVectorHex;
    private static final String kycProfileId;

    static {
        secretKey = ConfigurationUtil.getProperty("kyc.aes.secret.key");
        initVector = ConfigurationUtil.getProperty("kyc.iv.vector");
        kycProfileId = ConfigurationUtil.getProperty("kyc.profile.id");

        if (StringUtils.isBlank(secretKey) || StringUtils.isBlank(initVector) || StringUtils.isBlank(kycProfileId)) {
            LOGGER.error("kyc configuration not defined in properties file. System will terminate now.");
            ControlCenter.stop("kyc configuration not defined in properties file. System will terminate now.");
        }
        secretKeyHex = DatatypeConverter.parseHexBinary(secretKey);
        initVectorHex = DatatypeConverter.parseHexBinary(initVector);
    }

    @Override
    public void doKYC(HttpServletRequest request) throws KycValidationException {

        final LoginInfo loginInfo = sessionDataService.getLoginInfoFromSession(request);
        final OAuthUserInfo userInfo = loginInfo != null ? loginInfo.getUser() : null;
        // perform kyc when user is logged in

        if (userInfo == null) {
            LOGGER.error("Exception occurred while performing user kyc : as no user info found");
            return;
        }

        String userId = userInfo.getUserID();
        doActualKYC(request.getParameter(KYC_NAME_ON_DOC), request.getParameter(KYC_DOC_CODE),
                request.getParameter(KYC_DOC_VALUE), userId);
        return;
    }

    @Override
    public void doKYC(HttpServletRequest request, String userId) throws KycValidationException {
        doActualKYC(request.getParameter(KYC_NAME_ON_DOC), request.getParameter(KYC_DOC_CODE),
                request.getParameter(KYC_DOC_VALUE), userId);
        return;
    }

    private void doActualKYC(String kycNameOnDoc, String kycDocCode, String kycDocValue, String userId)
            throws KycValidationException {
        try {
            // Claims to generate jwt token
            Map<String, String> claims = new HashMap<String, String>();
            claims.put(jwtTokenTimeStamp, String.valueOf(System.currentTimeMillis()));
            claims.put(jwtTokenKycProfileId, kycProfileId);
            String jwtToken = getJwtToken(claims, secretKey);

            UserProfileSaveRequest userProfileSaveRequest = new UserProfileSaveRequest();
            userProfileSaveRequest.setJwtToken(jwtToken);
            userProfileSaveRequest.setUserId(userId);

            UserKycData data = new UserKycData();
            data.setName(kycNameOnDoc);
            List<UserDocument> documents = new ArrayList<UserDocument>();
            UserDocument doc = new UserDocument();
            doc.setDocCode(kycDocCode);
            doc.setDocValue(kycDocValue);

            // TODO : As discussed this field wont require
            // doc.setNameOnDoc(request.getParameter(KYC_NAME_ON_DOC));

            documents.add(doc);
            KycRequestModel kycRequest = new KycRequestModel();
            kycRequest.setData(data);
            kycRequest.setDocuments(documents);
            String jsonRequest = JsonMapper.mapObjectToJson(kycRequest);

            LOGGER.info("KYC request payload for add/update user profile : {}", jsonRequest);

            String encRequest = AesEncryptionKycImpl.aesEncrypt(secretKeyHex, initVectorHex, jsonRequest);
            userProfileSaveRequest.setEncRequestBody(encRequest);
            UserProfileSaveResponse userProfileSaveResponse = saveUserProfileImpl
                    .submitUserDocumentsForKyc(userProfileSaveRequest);
            if ("FAILURE".equals(userProfileSaveResponse.getStatusMessage())
                    || StringUtils.isBlank(userProfileSaveResponse.getEncResponse())) {
                LOGGER.error("Failure status returned from Kyc API");
                throw new KycValidationException(
                        "We could not validate your ID. Please try again with a different document ID");
            }
            String decResponse = AesEncryptionKycImpl.aesDecrypt(secretKeyHex, initVectorHex,
                    userProfileSaveResponse.getEncResponse());

            LOGGER.info("Response received from KYC for add/update user profile : {}", decResponse);

            JsonNode status = (JsonNode) JsonMapper.getParamFromJson(decResponse, "statusMessage");
            // Successful kyc
            if (status != null && "SUCCESS".equalsIgnoreCase(status.textValue())) {
                JsonNode minKycFlag = (JsonNode) JsonMapper.getParamFromJson(decResponse, "minKyc");
                if (minKycFlag != null && minKycFlag.asBoolean() == true) {
                    return;
                }
                throw new KycValidationException("User KYC failed");
            } else {
                // Read error node
                JsonNode error = (JsonNode) JsonMapper.getParamFromJson(decResponse, "error");

                if (error != null && !StringUtils.isBlank(error.textValue())) {
                    String errorMsg = JsonMapper.getStringParamFromJson(error.textValue(), "errorMsg");
                    throw new KycValidationException(errorMsg);
                }
                LOGGER.error("Invalid response from Kyc API");
                throw new KycValidationException(
                        "We could not validate your ID. Please try again with a different document ID");
            }
        } catch (KycValidationException e) {
            LOGGER.error("Exception occurred while performing user kyc : ", e);
            throw e;
        } catch (Exception e) {
            LOGGER.error("Exception occurred while performing user kyc : ", e);
            throw new KycValidationException(e);
        }
    }

    private String getJwtToken(Map<String, String> jwtClaims, String jwtKey) {
        return JWTKycWithHmacSHA256.createJwtToken(jwtClaims, jwtKey);
    }

    @Override
    public void doKYC(NativeKYCDetailRequest request, String userId) throws KycValidationException {
        doActualKYC(request.getBody().getKycNameOnDoc(), request.getBody().getKycDocCode(), request.getBody()
                .getKycDocValue(), userId);
        return;
    }

}
