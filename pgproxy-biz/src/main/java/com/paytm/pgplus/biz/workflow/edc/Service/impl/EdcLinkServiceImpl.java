package com.paytm.pgplus.biz.workflow.edc.Service.impl;

import com.paytm.pgplus.biz.exception.EdcLinkBankAndBrandEmiCheckoutException;
import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.biz.workflow.edc.Service.IEdcLinkService;
import com.paytm.pgplus.biz.workflow.edc.helper.EdcLinkHelper;
import com.paytm.pgplus.biz.workflow.model.*;
import com.paytm.pgplus.common.config.ConfigurationUtil;
import com.paytm.pgplus.common.model.link.EdcEmiChannelDetail;
import com.paytm.pgplus.facade.enums.EdcLinkPaymentServiceUrl;
import com.paytm.pgplus.facade.enums.ExternalEntity;
import com.paytm.pgplus.facade.enums.Type;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.facade.utils.LogUtil;
import com.paytm.pgplus.httpclient.HttpRequestPayload;
import com.paytm.pgplus.httpclient.JerseyHttpClient;
import com.paytm.pgplus.httpclient.enums.HttpMethod;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.pgproxycommon.utils.AmountUtils;
import com.paytm.pgplus.logging.ExtendedLogger;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.time.Instant;
import java.util.*;

import static com.paytm.pgplus.dynamicwrapper.utils.JSONUtils.toJsonString;
import static com.paytm.pgplus.biz.utils.BizConstant.Ff4jFeature.THEIA_ENABLE_VELOCITY_REVAMP_FLOW;

@Service("edcLinkServiceImpl")
public class EdcLinkServiceImpl implements IEdcLinkService {
    private static final Logger LOGGER = LoggerFactory.getLogger(EdcLinkServiceImpl.class);
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(EdcLinkServiceImpl.class);

    @Autowired
    private EdcLinkHelper edcLinkHelper;

    @Autowired
    private Ff4jUtils ff4jUtils;

    private static List<String> edcLinkAddSubAmountBankList = Arrays.asList(ConfigurationUtil.getProperty(
            BizConstant.EDC_LINK_ADD_SUBVENTION_AMOUNT_BANK_LIST, "").split(","));
    private static final String EOS_FAILURE_CODE = "EOS_0081";

    @Override
    public GenericCoreResponseBean<BankEmiResponse> getBankEmiResponse(WorkFlowTransactionBean workflowTxnBean)
            throws EdcLinkBankAndBrandEmiCheckoutException {
        long startTime = System.currentTimeMillis();
        try {
            BankEmiRequest bankEmiRequest = edcLinkHelper.prepareBankEmiRequest(workflowTxnBean);
            LOGGER.debug("bank Emi Request Received {} ", bankEmiRequest);
            final Map<String, String> queryMap = edcLinkHelper.prepareQueryParams(workflowTxnBean);
            final MultivaluedMap<String, Object> headerMap = edcLinkHelper.prepareHeaderMap(workflowTxnBean);
            BankEmiResponse bankEmiResponse = executePostV2(bankEmiRequest, EdcLinkPaymentServiceUrl.BANK_EMI.getUrl(),
                    BankEmiResponse.class, queryMap, headerMap, ExternalEntity.BANK_EMI);

            if (!StringUtils.equals(bankEmiResponse.getBody().getResultStatus(), BizConstant.SUCCESS)) {
                throw new EdcLinkBankAndBrandEmiCheckoutException(
                        BizConstant.EdcLinkEmiTxn.BANK_EMI_API_FAILURE_RESPONSE_MESSAGE,
                        ResponseConstants.EDC_LINK_BANK_OFFER_FAILURE);
            }
            boolean isValidated = validateBankEmiResponse(bankEmiResponse, workflowTxnBean);
            if (isValidated) {
                LOGGER.info(BizConstant.EdcLinkEmiTxn.BANK_EMI_API_SUCCESSFUL_RESPONSE);
                return new GenericCoreResponseBean<>(bankEmiResponse);
            } else {
                return new GenericCoreResponseBean<>(BizConstant.EdcLinkEmiTxn.EMI_RESPONSE_MISMATCH_MESSAGE,
                        ResponseConstants.EDC_LINK_BANK_OFFER_FAILURE);
            }
        } catch (Exception e) {
            LOGGER.error("Exception occurred in /getBankEmiResponse API : {}", e);
            String message = BizConstant.EdcLinkEmiTxn.EXCEPTION_MESSAGE_BANK_API;
            if (e instanceof EdcLinkBankAndBrandEmiCheckoutException && StringUtils.isNotBlank(e.getMessage())) {
                message = e.getMessage();
                ResponseConstants resultCode = ((EdcLinkBankAndBrandEmiCheckoutException) e).getResultCode();
                throw new EdcLinkBankAndBrandEmiCheckoutException(message, resultCode);
            } else {
                throw new EdcLinkBankAndBrandEmiCheckoutException(message, ResponseConstants.FAILURE);
            }
        } finally {
            LOGGER.info("Total time taken by /getBankEmiResponse API : {} ms", System.currentTimeMillis() - startTime);
        }
    }

    @Override
    public GenericCoreResponseBean<BrandEmiResponse> getBrandEmiResponse(WorkFlowTransactionBean workflowTxnBean)
            throws EdcLinkBankAndBrandEmiCheckoutException {
        long startTime = System.currentTimeMillis();
        try {
            BrandEmiRequest request = edcLinkHelper.prepareBrandEmiRequest(workflowTxnBean);
            LOGGER.debug("Brand Emi Request Received {} ", request);
            final Map<String, String> queryMap = edcLinkHelper.prepareQueryParams(workflowTxnBean);
            final MultivaluedMap<String, Object> headerMap = edcLinkHelper.prepareHeaderMap(workflowTxnBean);
            BrandEmiResponse brandEmiResponse = executePostV2(request, EdcLinkPaymentServiceUrl.BRAND_EMI.getUrl(),
                    BrandEmiResponse.class, queryMap, headerMap, ExternalEntity.BRAND_EMI);
            if (!StringUtils.equals(brandEmiResponse.getBody().getResultStatus(), BizConstant.SUCCESS)) {
                String message = BizConstant.EdcLinkEmiTxn.BANK_EMI_API_FAILURE_RESPONSE_MESSAGE;
                throw new EdcLinkBankAndBrandEmiCheckoutException(message,
                        ResponseConstants.EDC_LINK_BRAND_EMI_OFFER_FAILURE);
            }

            if (ff4jUtils.isFeatureEnabledOnMid(workflowTxnBean.getWorkFlowBean().getPaytmMID(),
                    THEIA_ENABLE_VELOCITY_REVAMP_FLOW, false)) {
                GenericCoreResponseBean<Boolean> validateEosFailure = validateBrandEmiEosFailure(brandEmiResponse,
                        workflowTxnBean);
                if (BooleanUtils.isFalse(validateEosFailure.isSuccessfullyProcessed())) {
                    return new GenericCoreResponseBean<>(BizConstant.EdcLinkEmiTxn.EMI_RESPONSE_MISMATCH_MESSAGE,
                            ResponseConstants.EDC_LINK_BRAND_EMI_OFFER_FAILURE);
                }
                EXT_LOGGER.customInfo("Successfully validated Brand EMI EOS : For EOS-0081 Code");
            }

            boolean validationCheck = validateBrandEmiResponse(brandEmiResponse, workflowTxnBean);
            if (validationCheck) {
                LOGGER.info(BizConstant.EdcLinkEmiTxn.BRAND_EMI_API_SUCCESSFUL_RESPONSE);
                return new GenericCoreResponseBean<>(brandEmiResponse);
            } else {
                return new GenericCoreResponseBean<>(BizConstant.EdcLinkEmiTxn.EMI_RESPONSE_MISMATCH_MESSAGE,
                        ResponseConstants.EDC_LINK_BRAND_EMI_OFFER_FAILURE);
            }
        } catch (Exception e) {
            LOGGER.error("Exception occurred in /getBrandEmiResponse API : {}", e);
            String message = BizConstant.EdcLinkEmiTxn.EXCEPTION_MESSAGE_BRAND_API;
            if (e instanceof EdcLinkBankAndBrandEmiCheckoutException && StringUtils.isNotBlank(e.getMessage())) {
                message = e.getMessage();
                ResponseConstants resultCode = ((EdcLinkBankAndBrandEmiCheckoutException) e).getResultCode();
                throw new EdcLinkBankAndBrandEmiCheckoutException(message, resultCode);
            } else {
                throw new EdcLinkBankAndBrandEmiCheckoutException(message, ResponseConstants.FAILURE);
            }
        } finally {
            LOGGER.info("Total time taken by /getBrandEmiResponse API : {} ms", System.currentTimeMillis() - startTime);
        }
    }

    protected GenericCoreResponseBean<Boolean> validateBrandEmiEosFailure(BrandEmiResponse brandEmiResponse,
            WorkFlowTransactionBean workflowTxnBean) {
        EXT_LOGGER.customInfo("Validating Brand EMI EOS Failure in BrandEmiResponse");
        String pgPlanId = getPgPlanIdFromWorkFlowTxnBean(workflowTxnBean);
        if (Objects.nonNull(brandEmiResponse.getBody().getEmiDetail())
                && Objects.nonNull(brandEmiResponse.getBody().getEmiDetail().getEmiChannelDetails())) {
            Optional<EmiChannelDetails> emiChannelDetail = brandEmiResponse.getBody().getEmiDetail()
                    .getEmiChannelDetails().stream().filter(channel -> pgPlanId.equals(channel.getPgPlanId()))
                    .findAny();
            if (emiChannelDetail.isPresent() && Objects.nonNull(emiChannelDetail.get().getEmiTypeChangeObj())
                    && EOS_FAILURE_CODE.equals(emiChannelDetail.get().getEmiTypeChangeObj().getCode())) {
                EXT_LOGGER.customInfo("Validation Failure :: EOS-0081 Failure Code found");
                return new GenericCoreResponseBean<>(emiChannelDetail.get().getEmiTypeChangeObj().getMessage());
            }
        }
        return new GenericCoreResponseBean<>(Boolean.TRUE);
    }

    @Override
    public GenericCoreResponseBean<OfferCheckoutResponse> getOfferCheckoutResponse(
            WorkFlowTransactionBean workflowTxnBean, String bankVerificationCode, String brandVerificationCode,
            List<String> velocityOfferId) throws EdcLinkBankAndBrandEmiCheckoutException {
        long startTime = System.currentTimeMillis();
        OfferCheckoutResponse offerCheckoutResponse = null;
        try {
            OfferCheckoutRequest checkoutRequest = edcLinkHelper.prepareCheckoutRequest(workflowTxnBean);
            if (ff4jUtils.isFeatureEnabledOnMid(workflowTxnBean.getWorkFlowBean().getPaytmMID(),
                    THEIA_ENABLE_VELOCITY_REVAMP_FLOW, false)) {
                EXT_LOGGER.info("OfferCheckout API for revamp flow, received :: bankVerificationCode : {},"
                        + "brandVerificationCode : {}, velocityOfferId : {}", bankVerificationCode,
                        brandVerificationCode, velocityOfferId);
                checkoutRequest.getBody().setBankVerificationCode(bankVerificationCode);
                checkoutRequest.getBody().setBrandVerificationCode(brandVerificationCode);
                checkoutRequest.getBody().setVelocityOfferId(velocityOfferId);
                checkoutRequest.getBody().setClientOrderCreatedAt(String.valueOf(Instant.now().toEpochMilli()));
            }
            LOGGER.debug("Checkout Request received {} ", checkoutRequest);
            final Map<String, String> queryMap = edcLinkHelper.prepareQueryParams(workflowTxnBean);
            final MultivaluedMap<String, Object> headerMap = edcLinkHelper.prepareHeaderMap(workflowTxnBean);
            offerCheckoutResponse = executePostV2(checkoutRequest,
                    EdcLinkPaymentServiceUrl.ORDER_OFFER_CHECKOUT.getUrl(), OfferCheckoutResponse.class, queryMap,
                    headerMap, ExternalEntity.ORDER_OFFER_CHECKOUT);
            if (((!StringUtils.equals(offerCheckoutResponse.getResultStatus(), BizConstant.SUCCESS)) || (!offerCheckoutResponse
                    .getInputFlag().equalsIgnoreCase(offerCheckoutResponse.getOutputFlag())))) {
                String message = BizConstant.EdcLinkEmiTxn.BRAND_BANK_EMI_CHECKOUT_FAILURE_RESPONSE_MESSAGE;
                return new GenericCoreResponseBean<>(offerCheckoutResponse, message,
                        ResponseConstants.EDC_LINK_BANK_BRAND_EMI_CHECKOUT_FAILURE);
            }
            LOGGER.info(BizConstant.EdcLinkEmiTxn.BANK_BRAND_CHECKOUT_API_SUCCESSFUL_RESPONSE);
            return new GenericCoreResponseBean<>(offerCheckoutResponse);
        } catch (Exception e) {
            LOGGER.error("Exception occurred in /getOfferCheckoutResponse API : {}", e);
            String message = "Error in Edc Link Brand Emi Offer Checkout";
            LOGGER.error(message, e);
            if (e instanceof EdcLinkBankAndBrandEmiCheckoutException && StringUtils.isNotBlank(e.getMessage())) {
                message = e.getMessage();
                ResponseConstants resultCode = ((EdcLinkBankAndBrandEmiCheckoutException) e).getResultCode();
                throw new EdcLinkBankAndBrandEmiCheckoutException(message, resultCode);
            } else {
                throw new EdcLinkBankAndBrandEmiCheckoutException(
                        BizConstant.EdcLinkEmiTxn.EXCEPTION_MESSAGE_BANK_BRAND_CHECKOUT_API, ResponseConstants.FAILURE);
            }
        } finally {
            LOGGER.info("Total time taken by /getOfferCheckoutResponse API : {} ms", System.currentTimeMillis()
                    - startTime);
        }
    }

    @Override
    public GenericCoreResponseBean<ValidateVelocityResponse> validateVelocity(WorkFlowTransactionBean workflowTxnBean)
            throws EdcLinkBankAndBrandEmiCheckoutException {
        long startTime = System.currentTimeMillis();
        ValidateVelocityResponse validateVelocityResponse;
        try {
            EXT_LOGGER.customInfo("Brand EMI - Validate Velocity API called");
            ValidateVelocityRequest validateVelocityRequest = edcLinkHelper
                    .prepareValidateVelocityRequest(workflowTxnBean);
            final MultivaluedMap<String, Object> headerMap = edcLinkHelper.prepareHeaderMap(workflowTxnBean);
            EXT_LOGGER.customInfo("Validate Velocity Request : {}", validateVelocityRequest);
            validateVelocityResponse = executePostV2(validateVelocityRequest,
                    EdcLinkPaymentServiceUrl.VALIDATE_VELOCITY.getUrl(), ValidateVelocityResponse.class,
                    new HashMap<>(), headerMap, ExternalEntity.VALIDATE_VELOCITY);
            EXT_LOGGER.customInfo("Validate Velocity Response : {}", validateVelocityResponse);
            if ((!StringUtils.equals(validateVelocityResponse.getResultStatus(), BizConstant.SUCCESS))) {
                String message = BizConstant.EdcLinkEmiTxn.EDC_VALIDATE_VELOCITY_FAILURE_RESPONSE_MESSAGE;
                throw new EdcLinkBankAndBrandEmiCheckoutException(message,
                        ResponseConstants.EDC_LINK_VALIDATE_VELOCITY_API_FAILURE);
            }
            LOGGER.info(BizConstant.EdcLinkEmiTxn.EDC_VALIDATE_VELOCITY_API_SUCCESSFUL_RESPONSE);
            return new GenericCoreResponseBean<>(validateVelocityResponse);
        } catch (Exception e) {
            LOGGER.error("Exception occurred in /eos/validateVelocity API : {}", e);
            if (e instanceof EdcLinkBankAndBrandEmiCheckoutException && StringUtils.isNotBlank(e.getMessage())) {
                ResponseConstants resultCode = ((EdcLinkBankAndBrandEmiCheckoutException) e).getResultCode();
                throw new EdcLinkBankAndBrandEmiCheckoutException(e.getMessage(), resultCode);
            } else {
                throw new EdcLinkBankAndBrandEmiCheckoutException(
                        BizConstant.EdcLinkEmiTxn.EDC_VALIDATE_VELOCITY_FAILURE_RESPONSE_MESSAGE,
                        ResponseConstants.FAILURE);
            }
        } finally {
            LOGGER.info("Total time taken by /eos/validateVelocity API : {} ms", System.currentTimeMillis() - startTime);
        }
    }

    private <Req, Resp> Resp executePostV2(Req request, String url, Class<Resp> respClass,
            Map<String, String> queryParams, MultivaluedMap<String, Object> headerMap, ExternalEntity externalEntity)
            throws FacadeCheckedException {
        long startTime = System.currentTimeMillis();
        final HttpRequestPayload<String> payload = generatePayloadV2(request, url, headerMap, queryParams);
        try {
            LogUtil.logPayload(externalEntity, url, Type.REQUEST, payload.toString());
            final Response response = JerseyHttpClient.sendHttpPostRequest(payload);

            final String responseEntity = response.readEntity(String.class);
            final Resp responseObject = JsonMapper.mapJsonToObject(responseEntity, respClass);
            String responseString = toJsonString(responseObject);
            LogUtil.logResponsePayload(externalEntity, url, Type.RESPONSE, responseString, startTime);
            return responseObject;
        } catch (final Exception e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
            throw new FacadeCheckedException(e);
        }
    }

    private <T> HttpRequestPayload<String> generatePayloadV2(final T request, String url,
            MultivaluedMap<String, Object> headerMap, Map<String, String> queryParams) throws FacadeCheckedException {
        final HttpRequestPayload<String> payload = new HttpRequestPayload<>();
        payload.setTarget(url);
        // payload.setHeaders(headerMap);
        // payload.setQueryParameters(queryParams);
        String requestBody = generateBody(request);
        payload.setEntity(requestBody);
        payload.setMediaTypeProduced(MediaType.APPLICATION_JSON);
        payload.setMediaTypeConsumed(MediaType.APPLICATION_JSON);
        payload.setHttpMethod(HttpMethod.POST);
        return payload;
    }

    private <T> String generateBody(final T request) throws FacadeCheckedException {
        if (request == null)
            return null;
        return JsonMapper.mapObjectToJson(request);
    }

    private boolean validateBankEmiResponse(BankEmiResponse bankEmiResponse, WorkFlowTransactionBean workflowTxnBean) {
        boolean validationCheck = false;
        if (bankEmiResponse.getBody() != null && bankEmiResponse.getBody().getEmiDetail() != null) {
            if (bankEmiResponse.getBody().getEmiDetail().getBankOffer() == null) {
                LOGGER.info("Recieved bankOffer null from EDC");
            }
            List<EmiChannelInfos> emiChannelInfosList = bankEmiResponse.getBody().getEmiDetail().getEmiChannelInfos();
            String workflowPgPlanId = getPgPlanIdFromWorkFlowTxnBean(workflowTxnBean);
            LOGGER.debug("Executing Validation of Received Bank Emi Response with pgPlanId {} , {} ", bankEmiResponse,
                    workflowPgPlanId);
            if (CollectionUtils.isNotEmpty(emiChannelInfosList)) {
                for (EmiChannelInfos emiChannelInfos : emiChannelInfosList) {
                    if (StringUtils.equals(emiChannelInfos.getPlanId(), (workflowPgPlanId))) {

                        double totalInterest = Double.valueOf(emiChannelInfos.getInterestAmount().getValue());
                        double discountedAmount = Double.valueOf(emiChannelInfos.getTotalAmount().getValue())
                                - totalInterest;
                        LOGGER.info("Final Amount in Bank Emi {} ", discountedAmount);
                        validationCheck = validateDiscountedAmount(workflowTxnBean, discountedAmount);
                        if (ff4jUtils.isFeatureEnabledOnMid(workflowTxnBean.getWorkFlowBean().getPaytmMID(),
                                THEIA_ENABLE_VELOCITY_REVAMP_FLOW, false)) {
                            if (workflowTxnBean.getWorkFlowBean().getPaymentRequestBean().getLinkDetailsData() != null
                                    && workflowTxnBean.getWorkFlowBean().getPaymentRequestBean().getLinkDetailsData()
                                            .getEdcEmiFields() != null
                                    && workflowTxnBean.getWorkFlowBean().getPaymentRequestBean().getLinkDetailsData()
                                            .getEdcEmiFields().getEmiChannelDetail() != null) {
                                boolean validResponse = checkEmiResponseForBankEmi(workflowTxnBean.getWorkFlowBean()
                                        .getPaymentRequestBean().getLinkDetailsData().getEdcEmiFields()
                                        .getEmiChannelDetail(), emiChannelInfos, bankEmiResponse.getBody()
                                        .getEmiDetail().getBankOffer());
                                if (!validResponse)
                                    throw new EdcLinkBankAndBrandEmiCheckoutException(
                                            BizConstant.EdcLinkEmiTxn.EMI_RESPONSE_MISMATCH_MESSAGE,
                                            ResponseConstants.EDC_LINK_BANK_OFFER_FAILURE);
                            }
                        }
                        break;
                    }
                }
            }
        }
        return validationCheck;
    }

    private boolean checkEmiResponseForBankEmi(EdcEmiChannelDetail emiChannelDetail, EmiChannelInfos emiChannelInfos,
            BankOffer bankOffer) {
        if (StringUtils.isNotBlank(emiChannelDetail.getInterestAmount().getValue())
                && !AmountUtils.formatNumberToTwoDecimalPlaces(emiChannelDetail.getInterestAmount().getValue()).equals(
                        AmountUtils.formatNumberToTwoDecimalPlaces(AmountUtils
                                .getTransactionAmountInRupee(emiChannelInfos.getInterestAmount().getValue())))) {
            EXT_LOGGER.customError("Interest Amount mismatch");
            return false;
        }
        if (CollectionUtils.isNotEmpty(emiChannelDetail.getBankOfferDetails())
                && emiChannelDetail.getBankOfferDetails().get(0) != null) {
            if (bankOffer == null && StringUtils.isNotBlank(emiChannelDetail.getBankOfferDetails().get(0).getType())) {
                EXT_LOGGER.customError("Bank offer received as null from edc");
                return false;
            }
            if (bankOffer != null && StringUtils.isBlank(emiChannelDetail.getBankOfferDetails().get(0).getType())
                    && bankOffer.getTenure() != null && emiChannelDetail.getEmiMonths() != null
                    && bankOffer.getTenure().contains(emiChannelDetail.getEmiMonths())) {
                EXT_LOGGER.customError("Tenure mismatch for bank offer");
                return false;
            }
            if (bankOffer != null && bankOffer.getTenure() != null && emiChannelDetail.getEmiMonths() != null
                    && bankOffer.getTenure().contains(emiChannelDetail.getEmiMonths())
                    && emiChannelDetail.getBankOfferDetails().get(0).getAmount() != null
                    && emiChannelDetail.getBankOfferDetails().get(0).getAmount().getValue() != null) {
                // For full swipe cases - check payInFullAmount field
                if ("0".equals(emiChannelDetail.getEmiMonths())
                        && !AmountUtils.formatNumberToTwoDecimalPlaces(
                                emiChannelDetail.getBankOfferDetails().get(0).getAmount().getValue()).equals(
                                AmountUtils.formatNumberToTwoDecimalPlaces(AmountUtils
                                        .getTransactionAmountInRupee(bankOffer.getPayInFullAmount())))) {
                    EXT_LOGGER.customError("Amount (payInFullAmount) value mismatch for bank offer");
                    return false;
                }
                // For non-full swipe cases - check amount field
                if (!"0".equals(emiChannelDetail.getEmiMonths())
                        && !AmountUtils.formatNumberToTwoDecimalPlaces(
                                emiChannelDetail.getBankOfferDetails().get(0).getAmount().getValue()).equals(
                                AmountUtils.formatNumberToTwoDecimalPlaces(AmountUtils
                                        .getTransactionAmountInRupee(bankOffer.getAmount())))) {
                    EXT_LOGGER.customError("Amount value mismatch for bank offer");
                    return false;
                }
            }

            if (bankOffer != null && bankOffer.getTenure() != null && emiChannelDetail.getEmiMonths() != null
                    && bankOffer.getTenure().contains(emiChannelDetail.getEmiMonths())
                    && emiChannelDetail.getBankOfferDetails().get(0).getType() != null
                    && !emiChannelDetail.getBankOfferDetails().get(0).getType().equals(bankOffer.getType())) {
                EXT_LOGGER.customError("Bank offer type mismatch");
                return false;
            }
        }
        return true;
    }

    private boolean validateBrandEmiResponse(BrandEmiResponse brandEmiResponse, WorkFlowTransactionBean workflowTxnBean) {
        boolean validationCheck = false;

        if (brandEmiResponse.getBody() != null && brandEmiResponse.getBody().getEmiDetail() != null) {
            List<EmiChannelDetails> emiChannelDetailsList = brandEmiResponse.getBody().getEmiDetail()
                    .getEmiChannelDetails();
            if (emiChannelDetailsList == null)
                return false;
            String workflowPgPlanId = getPgPlanIdFromWorkFlowTxnBean(workflowTxnBean);
            if (workflowPgPlanId == null)
                return false;
            LOGGER.debug("Executing Validation of Received Brand Emi Response with pgPlanId {} , {} ",
                    brandEmiResponse, workflowPgPlanId);
            for (EmiChannelDetails emiChannelDetails : emiChannelDetailsList) {
                if (StringUtils.equals(emiChannelDetails.getPgPlanId(), workflowPgPlanId)) {

                    double totalInterest = Double.valueOf(emiChannelDetails.getInterestAmount().getValue());
                    double discountedAmount = Double.valueOf(emiChannelDetails.getTotalAmount().getValue())
                            - totalInterest;
                    if (!(edcLinkAddSubAmountBankList.size() == 1 && "".equals(edcLinkAddSubAmountBankList.get(0)))
                            && edcLinkAddSubAmountBankList.contains(emiChannelDetails.getBankName())
                            && CollectionUtils.isNotEmpty(emiChannelDetails.getOfferDetails())) {
                        OfferDetails offerDetail = emiChannelDetails.getOfferDetails().get(0);
                        if (offerDetail != null && BizConstant.DISCOUNT.equalsIgnoreCase(offerDetail.getType())
                                && offerDetail.getAmount() != null
                                && StringUtils.isNotBlank(offerDetail.getAmount().getValue())) {
                            discountedAmount = discountedAmount
                                    + Double.parseDouble(offerDetail.getAmount().getValue());
                            LOGGER.info("This is special bank case:{}, Final amount is: {} ",
                                    emiChannelDetails.getBankName(), discountedAmount);
                        }
                    }
                    LOGGER.info("Final Amount in Brand Emi {} ", discountedAmount);
                    validationCheck = validateDiscountedAmount(workflowTxnBean, discountedAmount);
                    if (ff4jUtils.isFeatureEnabledOnMid(workflowTxnBean.getWorkFlowBean().getPaytmMID(),
                            THEIA_ENABLE_VELOCITY_REVAMP_FLOW, false)) {
                        if (workflowTxnBean.getWorkFlowBean().getPaymentRequestBean().getLinkDetailsData() != null
                                && workflowTxnBean.getWorkFlowBean().getPaymentRequestBean().getLinkDetailsData()
                                        .getEdcEmiFields() != null
                                && workflowTxnBean.getWorkFlowBean().getPaymentRequestBean().getLinkDetailsData()
                                        .getEdcEmiFields().getEmiChannelDetail() != null) {
                            boolean validResponse = checkEmiResponseForBrandEmi(workflowTxnBean.getWorkFlowBean()
                                    .getPaymentRequestBean().getLinkDetailsData().getEdcEmiFields()
                                    .getEmiChannelDetail(), emiChannelDetails);
                            if (!validResponse)
                                throw new EdcLinkBankAndBrandEmiCheckoutException(
                                        BizConstant.EdcLinkEmiTxn.EMI_RESPONSE_MISMATCH_MESSAGE,
                                        ResponseConstants.EDC_LINK_BANK_OFFER_FAILURE);
                        }
                    }
                    break;
                }
            }
        }
        return validationCheck;
    }

    private boolean checkEmiResponseForBrandEmi(EdcEmiChannelDetail emiChannelDetail,
            EmiChannelDetails emiChannelDetails) {
        if (StringUtils.isNotBlank(emiChannelDetail.getInterestAmount().getValue())
                && !AmountUtils.formatNumberToTwoDecimalPlaces(emiChannelDetail.getInterestAmount().getValue()).equals(
                        AmountUtils.formatNumberToTwoDecimalPlaces(AmountUtils
                                .getTransactionAmountInRupee(emiChannelDetails.getInterestAmount().getValue())))) {
            EXT_LOGGER.customError("Interest Amount mismatch");
            return false;
        }

        if (CollectionUtils.isNotEmpty(emiChannelDetail.getOfferDetails())
                && emiChannelDetail.getOfferDetails().get(0) != null
                && StringUtils.isNotBlank(emiChannelDetail.getOfferDetails().get(0).getType())
                && (CollectionUtils.isEmpty(emiChannelDetails.getOfferDetails()) || emiChannelDetails.getOfferDetails()
                        .get(0) == null)) {

            EXT_LOGGER.customError("Invalid offer details :{}", emiChannelDetails.getOfferDetails());
            return false;

        } else if (CollectionUtils.isNotEmpty(emiChannelDetails.getOfferDetails())
                && emiChannelDetails.getOfferDetails().get(0) != null) {

            if (emiChannelDetail.getOfferDetails().get(0).getAmount() != null
                    && emiChannelDetail.getOfferDetails().get(0).getAmount().getValue() != null)
                if (emiChannelDetails.getOfferDetails().get(0).getAmount() == null
                        || !AmountUtils.formatNumberToTwoDecimalPlaces(
                                emiChannelDetail.getOfferDetails().get(0).getAmount().getValue()).equals(
                                AmountUtils.formatNumberToTwoDecimalPlaces(AmountUtils
                                        .getTransactionAmountInRupee(emiChannelDetails.getOfferDetails().get(0)
                                                .getAmount().getValue())))) {
                    EXT_LOGGER.customError("Invalid offer Amount :{}", emiChannelDetails.getOfferDetails().get(0)
                            .getAmount());
                    return false;
                }
            if (emiChannelDetail.getOfferDetails().get(0).getType() != null
                    && !emiChannelDetail.getOfferDetails().get(0).getType()
                            .equals(emiChannelDetails.getOfferDetails().get(0).getType())) {
                EXT_LOGGER.customError("Invalid offer type :{}", emiChannelDetails.getOfferDetails().get(0).getType());
                return false;
            }
        }

        if (CollectionUtils.isEmpty(emiChannelDetail.getBankOfferDetails())
                && CollectionUtils.isNotEmpty(emiChannelDetails.getBankOfferDetails())
                && (emiChannelDetails.getBankOfferDetails().get(0) != null && StringUtils.isNotBlank(emiChannelDetails
                        .getBankOfferDetails().get(0).getType()))) {
            EXT_LOGGER.customError("Invalid BankOffer details :{}", emiChannelDetails.getBankOfferDetails());
            return false;
        }

        if (CollectionUtils.isNotEmpty(emiChannelDetail.getBankOfferDetails())
                && emiChannelDetail.getBankOfferDetails().get(0) != null
                && StringUtils.isNotBlank(emiChannelDetail.getBankOfferDetails().get(0).getType())
                && (CollectionUtils.isEmpty(emiChannelDetails.getBankOfferDetails()) || (emiChannelDetails
                        .getBankOfferDetails().get(0) != null && StringUtils.isBlank(emiChannelDetails
                        .getBankOfferDetails().get(0).getType())))) {
            EXT_LOGGER.customError("Invalid BankOffer details :{}", emiChannelDetails.getBankOfferDetails());
            return false;
        }

        if (CollectionUtils.isNotEmpty(emiChannelDetail.getBankOfferDetails())
                && emiChannelDetail.getBankOfferDetails().get(0) != null
                && CollectionUtils.isNotEmpty(emiChannelDetails.getBankOfferDetails())
                && emiChannelDetails.getBankOfferDetails().get(0) != null) {

            if (emiChannelDetail.getBankOfferDetails().get(0).getAmount() != null
                    && emiChannelDetail.getBankOfferDetails().get(0).getAmount().getValue() != null)
                if (emiChannelDetails.getBankOfferDetails().get(0).getAmount() == null
                        || emiChannelDetails.getBankOfferDetails().get(0).getAmount().getValue() == null
                        || !AmountUtils.formatNumberToTwoDecimalPlaces(
                                emiChannelDetail.getBankOfferDetails().get(0).getAmount().getValue()).equals(
                                AmountUtils.formatNumberToTwoDecimalPlaces(AmountUtils
                                        .getTransactionAmountInRupee(emiChannelDetails.getBankOfferDetails().get(0)
                                                .getAmount().getValue())))) {
                    EXT_LOGGER.customError("Invalid BankOffer amount :{}",
                            emiChannelDetails.getBankOfferDetails().get(0).getAmount());
                    return false;
                }
            if (emiChannelDetail.getBankOfferDetails().get(0).getType() != null
                    && !emiChannelDetail.getBankOfferDetails().get(0).getType()
                            .equals(emiChannelDetails.getBankOfferDetails().get(0).getType())) {
                EXT_LOGGER.customError("Invalid BankOffer type :{}", emiChannelDetails.getBankOfferDetails().get(0)
                        .getType());
                return false;
            }
        }
        return true;
    }

    private boolean validateDiscountedAmount(WorkFlowTransactionBean workflowTxnBean, double discountedAmount) {
        if (discountedAmount == Double.valueOf(workflowTxnBean.getWorkFlowBean().getTxnAmount()))
            return true;
        else
            return false;
    }

    private String getPgPlanIdFromWorkFlowTxnBean(WorkFlowTransactionBean workflowTxnBean) {
        if (workflowTxnBean.getWorkFlowBean().getPaymentRequestBean().getLinkDetailsData() != null
                && workflowTxnBean.getWorkFlowBean().getPaymentRequestBean().getLinkDetailsData().getEdcEmiFields() != null
                && workflowTxnBean.getWorkFlowBean().getPaymentRequestBean().getLinkDetailsData().getEdcEmiFields()
                        .getEmiChannelDetail() != null) {
            return workflowTxnBean.getWorkFlowBean().getPaymentRequestBean().getLinkDetailsData().getEdcEmiFields()
                    .getEmiChannelDetail().getPgPlanId();
        } else {
            return null;
        }
    }

    public boolean isExemptedBank(String bankCode) {
        String banks = ConfigurationUtil.getProperty(BizConstant.EdcLinkEmiTxn.BRAND_EMI_EXEMPTED_BANK_LIST);
        if (StringUtils.isNotBlank(banks)) {
            final String[] bankList = StringUtils.split(banks, ",");
            return Arrays.stream(bankList).anyMatch(bank -> bank.equals(bankCode));
        }
        return false;
    }

    private String toQueryParamString(Map<String, String> queryParams) {
        StringBuilder sb = new StringBuilder("?");
        for (Map.Entry<String, String> entry : queryParams.entrySet()) {
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }
        return sb.substring(0, sb.length() - 1);
    }

}
