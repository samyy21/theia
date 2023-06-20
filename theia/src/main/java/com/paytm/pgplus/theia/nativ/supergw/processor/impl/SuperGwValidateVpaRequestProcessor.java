package com.paytm.pgplus.theia.nativ.supergw.processor.impl;

import com.paytm.pgplus.biz.mapping.models.MappingMerchantData;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.facade.bankrequest.IValidateVpaService;
import com.paytm.pgplus.facade.bankrequest.model.ValidateVpaAndPspRequest;
import com.paytm.pgplus.facade.bankrequest.model.ValidateVpaAndPspResponse;
import com.paytm.pgplus.facade.constants.FacadeConstants;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.response.ResponseHeader;
import com.paytm.pgplus.response.ResultInfo;
import com.paytm.pgplus.stats.util.AWSStatsDUtils;
import com.paytm.pgplus.theia.cache.IMerchantMappingService;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.exceptions.InvalidRequestParameterException;
import com.paytm.pgplus.theia.nativ.model.response.FeatureDetails;
import com.paytm.pgplus.theia.nativ.model.response.ValidateVpaV4Response;
import com.paytm.pgplus.theia.nativ.model.response.ValidateVpaV4ResponseBody;
import com.paytm.pgplus.theia.nativ.model.response.ValidateVpaV4ServiceResponse;
import com.paytm.pgplus.theia.nativ.model.vpaValidate.ValidateVpaV4Request;
import com.paytm.pgplus.theia.nativ.model.vpaValidate.ValidateVpaV4ServiceRequest;
import com.paytm.pgplus.theia.nativ.processor.AbstractRequestProcessor;
import com.paytm.pgplus.theia.nativ.supergw.enums.PaymentType;
import com.paytm.pgplus.theia.nativ.supergw.util.SuperGwValidationUtil;
import com.paytm.pgplus.theia.nativ.utils.INativeValidationService;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.exceptions.RequestValidationException;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.paytm.pgplus.facade.bankrequest.enums.AlipayRequestBodyParams.mobile_constant;
import static com.paytm.pgplus.facade.bankrequest.enums.BankRequestParams.DEVICE;

@Service("superGwValidateVpaRequestProcessor")
public class SuperGwValidateVpaRequestProcessor
        extends
        AbstractRequestProcessor<ValidateVpaV4Request, ValidateVpaV4Response, ValidateVpaV4ServiceRequest, ValidateVpaV4ServiceResponse> {

    @Autowired
    private Environment environment;

    @Autowired
    private AWSStatsDUtils statsDUtils;

    private static final Logger LOGGER = LoggerFactory.getLogger(SuperGwValidateVpaRequestProcessor.class);

    @Autowired
    @Qualifier("nativeValidationService")
    private INativeValidationService nativeValidationService;

    @Autowired
    @Qualifier("validateVpaService")
    private IValidateVpaService validateVpaService;

    @Autowired
    private IMerchantMappingService merchantMappingService;

    @Override
    protected ValidateVpaV4ServiceRequest preProcess(ValidateVpaV4Request request) {
        validateRequest(request);
        return getValidateVpaServiceRequest(request);
    }

    @Override
    protected ValidateVpaV4ServiceResponse onProcess(ValidateVpaV4Request request,
            ValidateVpaV4ServiceRequest serviceRequest) {
        ValidateVpaV4ServiceResponse serviceResponse;
        try {
            // code to be moved in common jar - start
            validateRequest(serviceRequest);
            String mid = serviceRequest.getMid();
            final GenericCoreResponseBean<MappingMerchantData> merchantMappingResponse = merchantMappingService
                    .fetchMerchanData(mid);
            if (merchantMappingResponse == null || !merchantMappingResponse.isSuccessfullyProcessed()
                    || StringUtils.isBlank(merchantMappingResponse.getResponse().getOfficialName())) {
                throw new InvalidRequestParameterException("Merchant details not found");
            }
            String merchantName = getTrimmedMerchantName(merchantMappingResponse.getResponse().getOfficialName());
            String vpa = serviceRequest.getVpaAddress();
            String seqNo = generateRandomString();
            ValidateVpaAndPspRequest validateVpaAndPspRequest = new ValidateVpaAndPspRequest();
            validateVpaAndPspRequest.setSeqNo(seqNo);
            validateVpaAndPspRequest.setPayeeName(merchantName);
            validateVpaAndPspRequest.setMobile(mobile_constant.getName());
            validateVpaAndPspRequest.setVirtualAddress(vpa);
            validateVpaAndPspRequest.setDeviceId(DEVICE.getName());
            String additionalInfoType = serviceRequest.getPaymentType() != null ? serviceRequest.getPaymentType()
                    .getValue() : null;
            validateVpaAndPspRequest.setAdditionalInfoType(additionalInfoType);
            ValidateVpaAndPspResponse validateVpaAndPspResponse = validateVpaService
                    .fetchValidatedVpa(validateVpaAndPspRequest);
            try {
                Map<String, String> responseMap = new HashMap<>();
                responseMap.put("RESPONSE_STATUS", validateVpaAndPspResponse.getStatus());
                responseMap.put("RESPONSE_MESSAGE", validateVpaAndPspResponse.getRespMessage());
                statsDUtils.pushResponse("VALIDATE_VPA", responseMap);
            } catch (Exception exception) {
                LOGGER.error("Error in pushing response message " + "VALIDATE_VPA" + "to grafana", exception);
            }
            serviceResponse = getServiceResponse(serviceRequest, validateVpaAndPspResponse);
            // code to be moved in common jar - end
        } catch (Exception e) {
            LOGGER.error("Error in validating VPA in native : {}", e);
            serviceResponse = new ValidateVpaV4ServiceResponse();
            ResultInfo resultInfo = new ResultInfo();
            resultInfo.setResultCode(ResponseConstants.SYSTEM_ERROR.getCode());
            resultInfo.setResultMsg("Sorry! We could not verify the UPI ID. Please try again.");
            resultInfo.setResultStatus(ResultCode.FAILED.getResultStatus());
            serviceResponse.setResultInfo(resultInfo);
        }
        LOGGER.info("Response returning to merchant : {}", serviceResponse);
        return serviceResponse;
    }

    @Override
    protected ValidateVpaV4Response postProcess(ValidateVpaV4Request request,
            ValidateVpaV4ServiceRequest serviceRequest, ValidateVpaV4ServiceResponse serviceResponse) throws Exception {
        ValidateVpaV4Response validateVpaResponse = getValidateVpaResponse(serviceResponse);
        return validateVpaResponse;
    }

    private void validateRequest(ValidateVpaV4Request request) {
        validateMandatoryParams(request);
        nativeValidationService.validateMid(request.getBody().getMid());
        validateJwt(request);
    }

    private void validateMandatoryParams(ValidateVpaV4Request request) {
        if (StringUtils.isBlank(request.getBody().getVpa()) || StringUtils.isBlank(request.getBody().getMid())) {
            com.paytm.pgplus.common.model.ResultInfo resultInfo = new com.paytm.pgplus.common.model.ResultInfo();
            ResponseConstants responseConstant = ResponseConstants.INVALID_PARAM;
            resultInfo.setResultCode(responseConstant.getCode());
            resultInfo.setResultMsg(responseConstant.getMessage());
            resultInfo.setResultStatus(ResultCode.FAILED.getResultStatus());
            throw new RequestValidationException(resultInfo);
        }
    }

    private ValidateVpaV4ServiceRequest getValidateVpaServiceRequest(ValidateVpaV4Request request) {
        ValidateVpaV4ServiceRequest serviceRequest = new ValidateVpaV4ServiceRequest();
        serviceRequest.setMid(request.getBody().getMid());
        serviceRequest.setVpaAddress(request.getBody().getVpa());
        serviceRequest.setQueryParams(request.getBody().getQueryParams());
        if (ERequestType.NATIVE_SUBSCRIPTION.equals(request.getBody().getRequestType())) {
            serviceRequest.setPaymentType(PaymentType.RECURRING);
        }
        return serviceRequest;
    }

    private ValidateVpaV4Response getValidateVpaResponse(ValidateVpaV4ServiceResponse serviceResponse) {
        ResponseHeader responseHeader = new ResponseHeader(TheiaConstant.RequestHeaders.SUPERGW_VERSION);

        ValidateVpaV4ResponseBody validateVpaResponseBody = new ValidateVpaV4ResponseBody();
        validateVpaResponseBody.setValid(serviceResponse.isValid());
        validateVpaResponseBody.setVpa(serviceResponse.getVpa());
        validateVpaResponseBody.setResultInfo(serviceResponse.getResultInfo());
        validateVpaResponseBody.setFeatureDetails(serviceResponse.getFeatureDetails());

        ValidateVpaV4Response validateVpaResponse = new ValidateVpaV4Response();
        validateVpaResponse.setHead(responseHeader);
        validateVpaResponse.setBody(validateVpaResponseBody);
        return validateVpaResponse;
    }

    private void validateJwt(ValidateVpaV4Request request) {
        String clientId = request.getHead().getClientId();
        if (StringUtils.isBlank(clientId)) {
            throw new InvalidRequestParameterException("clientId can't be null");
        }
        String clientSecret = environment.getProperty(clientId);
        Map<String, String> jwtClaims = new HashMap<>();
        jwtClaims.put(FacadeConstants.MID, request.getBody().getMid());
        jwtClaims.put(TheiaConstant.ExtendedInfoPay.VPA, request.getBody().getVpa());
        SuperGwValidationUtil.validateJwtToken(jwtClaims, clientId, clientSecret);
    }

    private void validateRequest(ValidateVpaV4ServiceRequest inRequest) throws InvalidRequestParameterException {
        if (inRequest == null) {
            throw new InvalidRequestParameterException("Request object can't be null");
        }
        if (StringUtils.isBlank(inRequest.getMid())) {
            throw new InvalidRequestParameterException("Mid can't be null");
        }
        if (StringUtils.isBlank(inRequest.getVpaAddress())) {
            throw new InvalidRequestParameterException("Vpa Address can't be null");
        }
    }

    private ValidateVpaV4ServiceResponse getServiceResponse(ValidateVpaV4ServiceRequest request,
            ValidateVpaAndPspResponse vpaAndPspResponse) {
        ValidateVpaV4ServiceResponse serviceResponse = new ValidateVpaV4ServiceResponse();
        ResultInfo resultInfo = new ResultInfo();
        serviceResponse.setVpa(vpaAndPspResponse.getVpa());
        if ("SUCCESS".equals(vpaAndPspResponse.getStatus())) {
            serviceResponse.setValid(true);
            resultInfo.setResultMsg(ResultCode.SUCCESS.getResultMsg());
            resultInfo.setResultCode(ResultCode.SUCCESS.getResultCodeId());
            resultInfo.setResultStatus(ResultCode.SUCCESS.getResultStatus());
            // set feature details
            if (vpaAndPspResponse.getFeatureDetails() != null) {
                FeatureDetails featureDetails = new FeatureDetails();
                boolean pspSupported = "Y".equals(vpaAndPspResponse.getFeatureDetails().getPspSupported());
                boolean bankSupported = "Y".equals(vpaAndPspResponse.getFeatureDetails().getDefaultBankSupported());
                featureDetails.setPspSupported(pspSupported);
                featureDetails.setBankSupported(bankSupported);
                serviceResponse.setFeatureDetails(featureDetails);
            }
        } else {
            serviceResponse.setValid(false);
            resultInfo.setResultCode(ResultCode.FAILED.getResultCodeId());
            resultInfo.setResultMsg(vpaAndPspResponse.getRespMessage());
            resultInfo.setResultStatus(ResultCode.FAILED.getResultStatus());
        }
        serviceResponse.setResultInfo(resultInfo);
        return serviceResponse;
    }

    private String generateRandomString() {
        String randStr = RandomStringUtils.random(10, true, true);
        StringBuilder sb = new StringBuilder();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddhhmm");
        sb.append("PTM").append(randStr).append(dateFormat.format(new Date()));
        return sb.toString();
    }

    private String getTrimmedMerchantName(String merchantName) {
        return merchantName != null ? merchantName.replace(" ", "") : merchantName;
    }

}