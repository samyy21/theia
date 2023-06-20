package com.paytm.pgplus.theia.nativ.processor.impl;

import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.common.model.ResultInfo;
import com.paytm.pgplus.facade.emisubvention.models.GenericEmiSubventionResponse;
import com.paytm.pgplus.facade.emisubvention.models.Item;
import com.paytm.pgplus.facade.emisubvention.models.PlanDetail;
import com.paytm.pgplus.facade.emisubvention.models.request.TenuresRequest;
import com.paytm.pgplus.facade.emisubvention.models.response.TenuresResponse;
import com.paytm.pgplus.facade.emisubvention.service.ISubventionEmiService;
import com.paytm.pgplus.facade.emisubvention.utils.SubventionUtils;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.theia.emiSubvention.constants.ResponseCodeConstants;
import com.paytm.pgplus.theia.emiSubvention.constants.ResponseMessage;
import com.paytm.pgplus.theia.emiSubvention.helper.ChecksumValidator;
import com.paytm.pgplus.theia.emiSubvention.helper.SubventionEmiServiceHelper;
import com.paytm.pgplus.theia.emiSubvention.model.request.tenures.EmiTenuresRequest;
import com.paytm.pgplus.theia.emiSubvention.model.request.tenures.EmiTenuresRequestBody;
import com.paytm.pgplus.theia.emiSubvention.model.response.tenures.EmiTenuresResponse;
import com.paytm.pgplus.theia.emiSubvention.util.EmiSubventionUtils;
import com.paytm.pgplus.theia.nativ.processor.AbstractRequestProcessor;
import com.paytm.pgplus.theia.nativ.utils.INativeValidationService;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.enums.TokenType;
import com.paytm.pgplus.theia.offline.exceptions.RequestValidationException;
import com.paytm.pgplus.theia.paymentoffer.helper.TokenValidationHelper;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.REQUEST_ID;

@Service("tenuresEmiSubventionProcessor")
public class TenuresEmiSubventionProcessor
        extends
        AbstractRequestProcessor<EmiTenuresRequest, EmiTenuresResponse, TenuresRequest, GenericEmiSubventionResponse<TenuresResponse>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TenuresEmiSubventionProcessor.class);

    @Autowired
    private TokenValidationHelper tokenValidationHelper;

    @Autowired
    private ISubventionEmiService subventionEmiService;

    @Autowired
    private SubventionEmiServiceHelper subventionEmiServiceHelper;

    @Autowired
    private ChecksumValidator checksumValidator;

    @Autowired
    private EmiSubventionUtils emiSubventionUtils;

    @Autowired
    @Qualifier("nativeValidationService")
    private INativeValidationService nativeValidationService;

    @Override
    protected TenuresRequest preProcess(EmiTenuresRequest request) {
        // basic validations to be done
        validateRequest(request);
        if (TokenType.SSO == request.getHead().getTokenType()) {
            UserDetailsBiz userDetailsBiz = tokenValidationHelper.validateToken(request.getHead().getToken(), request
                    .getHead().getTokenType(), request.getBody(), request.getBody().getMid());
            if (StringUtils.isBlank(request.getBody().getCustomerId())) {
                request.getBody().setCustomerId(userDetailsBiz.getUserId());
            }
        } else if (TokenType.CHECKSUM == request.getHead().getTokenType()) {
            boolean isValidated = checksumValidator.validateChecksum(checksumValidator.getBodyString(), request
                    .getBody().getMid(), request.getHead().getToken());
            if (!isValidated) {
                throw new RequestValidationException(
                        new ResultInfo(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultStatus(),
                                ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultCodeId(),
                                ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultMsg(),
                                ResponseMessage.INVALID_CHECKSUM));
            }
        } else if (TokenType.ACCESS.equals(request.getHead().getTokenType())) {
            emiSubventionUtils.validateAccessToken(request.getBody().getMid(), request.getBody().getReferenceId(),
                    request.getHead().getToken());
        } else if (TokenType.TXN_TOKEN.equals(request.getHead().getTokenType()) && null != request.getHead().getToken()) {
            InitiateTransactionRequestBody orderDetail = nativeValidationService.validateTxnToken(request.getHead()
                    .getToken());
            nativeValidationService.validateMidOrderId(orderDetail.getMid(), orderDetail.getOrderId());
        } else {
            LOGGER.info("tokenType = {} is not supported", request.getHead().getTokenType());
            throw new RequestValidationException(new ResultInfo(
                    ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultStatus(),
                    ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getCode(), ResponseCodeConstants.VALIDATION_FAILED,
                    ResponseMessage.TOKEN_TYPE_NOT_SUPPORTED));
        }

        MDC.put(REQUEST_ID, request.getHead().getRequestId());
        TenuresRequest tenuresRequest = new TenuresRequest();
        if (!CollectionUtils.isEmpty(request.getBody().getItems())) {
            tenuresRequest = subventionEmiServiceHelper.prepareEmiServiceRequest(request);
        } else if (request.getBody().getSubventionAmount() > 0.0 && request.getBody().getPrice() > 0.0) {
            request.getBody().setAmountBasedSubvention(true);
            tenuresRequest = subventionEmiServiceHelper.prepareEmiServiceRequestAmountBased(request);
        }
        return tenuresRequest;
    }

    @Override
    protected GenericEmiSubventionResponse<TenuresResponse> onProcess(EmiTenuresRequest request,
            TenuresRequest serviceRequest) throws Exception {
        GenericEmiSubventionResponse<TenuresResponse> tenuresResponse = subventionEmiService
                .fetchEmiSubventionTenures(serviceRequest);
        EmiTenuresRequestBody emiTenuresRequestBody = request.getBody();
        /*
         * calling subvention service again with originalPrice of the order for
         * the tenures on which offer is not applicable.
         */
        if (!CollectionUtils.isEmpty(emiTenuresRequestBody.getApplicableTenures())) {
            serviceRequest = prepareEmiServiceRequestWithOriginalPrice(request);
            GenericEmiSubventionResponse<TenuresResponse> tenuresResponseWithOriginalPrice = subventionEmiService
                    .fetchEmiSubventionTenures(serviceRequest);
            return filterPlanDetails(tenuresResponse, tenuresResponseWithOriginalPrice, request);
        }
        return tenuresResponse;
    }

    @Override
    protected EmiTenuresResponse postProcess(EmiTenuresRequest request, TenuresRequest serviceRequest,
            GenericEmiSubventionResponse<TenuresResponse> serviceResponse) throws Exception {
        EmiTenuresResponse emiTenuresResponse = subventionEmiServiceHelper.prepareTenuresEmiResponse(serviceResponse,
                request);
        emiTenuresResponse.getBody().setResultInfo(
                new com.paytm.pgplus.response.ResultInfo(ResultCode.SUCCESS.getResultStatus(), ResultCode.SUCCESS
                        .getResultCodeId(), ResultCode.SUCCESS.getResultMsg()));
        return emiTenuresResponse;
    }

    private void validateRequest(EmiTenuresRequest request) {
        if (request.getBody().getMid() == null) {
            throw new RequestValidationException(new ResultInfo(ResultCode.MISSING_MANDATORY_ELEMENT.getResultStatus(),
                    ResultCode.MISSING_MANDATORY_ELEMENT.getResultCodeId(),
                    ResultCode.MISSING_MANDATORY_ELEMENT.getResultMsg(), ResponseMessage.EMPTY_MID));
        }
        if (!CollectionUtils.isEmpty(request.getBody().getItems())
                && (request.getBody().getSubventionAmount() > 0.0 || request.getBody().getPrice() > 0.0)) {
            throw new RequestValidationException(new ResultInfo(ResultCode.MISSING_MANDATORY_ELEMENT.getResultStatus(),
                    ResultCode.MISSING_MANDATORY_ELEMENT.getResultCodeId(),
                    ResultCode.MISSING_MANDATORY_ELEMENT.getResultMsg(),
                    ResponseMessage.BOTH_ITEM_LIST_AMOUNT_NON_EMPTY));
        }
        if ((request.getBody().getItems() == null || request.getBody().getItems().size() == 0)
                && (StringUtils.isBlank(String.valueOf(request.getBody().getSubventionAmount())) || StringUtils
                        .isBlank(String.valueOf(request.getBody().getPrice())))) {
            throw new RequestValidationException(new ResultInfo(ResultCode.MISSING_MANDATORY_ELEMENT.getResultStatus(),
                    ResultCode.MISSING_MANDATORY_ELEMENT.getResultCodeId(),
                    ResultCode.MISSING_MANDATORY_ELEMENT.getResultMsg(),
                    ResponseMessage.BOTH_LISTINGPRICE_PRICE_REQUIRED));
        }

        if (CollectionUtils.isEmpty(request.getBody().getItems())
                && StringUtils.isNotBlank(String.valueOf(request.getBody().getSubventionAmount()))
                && (StringUtils.isNotBlank(String.valueOf(request.getBody().getPrice())))
                && ((request.getBody().getSubventionAmount() <= 0.0) || (request.getBody().getPrice() <= 0.0))) {
            throw new RequestValidationException(new ResultInfo(
                    ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultStatus(),
                    ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultCodeId(),
                    ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultMsg(),
                    ResponseMessage.INVALID_AMOUNT_DETAILS));

        }

        if ((request.getBody().getSubventionAmount() <= 0.0) && (request.getBody().getPrice() <= 0.0)
                && (request.getBody().getItems() == null || request.getBody().getItems().size() == 0)) {
            throw new RequestValidationException(new ResultInfo(ResultCode.MISSING_MANDATORY_ELEMENT.getResultStatus(),
                    ResultCode.MISSING_MANDATORY_ELEMENT.getResultCodeId(),
                    ResultCode.MISSING_MANDATORY_ELEMENT.getResultMsg(), ResponseMessage.BOTH_ITEM_LIST_AMOUNT_EMPTY));
        }
        subventionEmiServiceHelper.checkIfMidExist(request.getBody().getMid());
        if (CollectionUtils.isEmpty(request.getBody().getItems())
                && (request.getBody().getSubventionAmount() > request.getBody().getPrice())) {
            throw new RequestValidationException(new ResultInfo(
                    ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultStatus(),
                    ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultCodeId(),
                    ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultMsg(),
                    ResponseMessage.SUBVENTION_AMOUNT_GREATER));
        }
        if (request.getHead().getRequestId() == null) {
            throw new RequestValidationException(new ResultInfo(ResultCode.MISSING_MANDATORY_ELEMENT.getResultStatus(),
                    ResultCode.MISSING_MANDATORY_ELEMENT.getResultCodeId(),
                    ResultCode.MISSING_MANDATORY_ELEMENT.getResultMsg(), ResponseMessage.EMPTY_REQUEST_ID));
        }

        if (request.getBody().getFilters() == null) {
            throw new RequestValidationException(new ResultInfo(ResultCode.MISSING_MANDATORY_ELEMENT.getResultStatus(),
                    ResultCode.MISSING_MANDATORY_ELEMENT.getResultCodeId(),
                    ResultCode.MISSING_MANDATORY_ELEMENT.getResultMsg(), ResponseMessage.FILTERS_MISSING));
        }

        if (CollectionUtils.isEmpty(request.getBody().getItems())) {
            request.getBody().setPrice(SubventionUtils.formatAmount(request.getBody().getPrice()));
            request.getBody()
                    .setSubventionAmount(SubventionUtils.formatAmount(request.getBody().getSubventionAmount()));
        }

    }

    private TenuresRequest prepareEmiServiceRequestWithOriginalPrice(EmiTenuresRequest request) {
        /*
         * setting price as originalPrice of the order for the tenures on which
         * offer is not applicable
         */
        EmiTenuresRequestBody emiTenuresRequestBody = request.getBody();
        TenuresRequest serviceRequest = null;
        if (emiTenuresRequestBody.isAmountBasedSubvention() && emiTenuresRequestBody.getOriginalPrice() != 0.0) {
            double discountedPrice = emiTenuresRequestBody.getPrice();
            emiTenuresRequestBody.setPrice(emiTenuresRequestBody.getOriginalPrice());
            serviceRequest = subventionEmiServiceHelper.prepareEmiServiceRequestAmountBased(request);
            request.getBody().setPrice(discountedPrice);
        } else if (!CollectionUtils.isEmpty(emiTenuresRequestBody.getItems())) {
            List<Item> itemsListWithDiscountedPrice = new ArrayList<>();
            List<Item> itemsListWithOriginalPrice = emiTenuresRequestBody.getItems();
            for (Item item : itemsListWithOriginalPrice) {
                itemsListWithDiscountedPrice.add(new Item(item));
                if (item.getOriginalPrice() != 0.0) {
                    item.setPrice(item.getOriginalPrice());
                }
            }
            serviceRequest = subventionEmiServiceHelper.prepareEmiServiceRequest(request);
            emiTenuresRequestBody.setItems(itemsListWithDiscountedPrice);
        }
        return serviceRequest;
    }

    private GenericEmiSubventionResponse<TenuresResponse> filterPlanDetails(
            GenericEmiSubventionResponse<TenuresResponse> tenuresResponse,
            GenericEmiSubventionResponse<TenuresResponse> tenuresResponseWithOriginalPrice, EmiTenuresRequest request) {
        try {
            List<PlanDetail> planDetails = null;
            List<PlanDetail> planDetails2 = null;
            Map<String, PlanDetail> planDetailsMap = null;
            Map<String, PlanDetail> planDetailsMap2 = null;
            if (tenuresResponse != null && tenuresResponse.getData() != null
                    && !CollectionUtils.isEmpty(tenuresResponse.getData().getPlanDetails())) {
                planDetails = tenuresResponse.getData().getPlanDetails();
                planDetailsMap = planDetails.stream().collect(
                        Collectors.toMap(PlanDetail::getPlanId, Function.identity()));
            }
            if (tenuresResponseWithOriginalPrice != null && tenuresResponseWithOriginalPrice.getData() != null
                    && !CollectionUtils.isEmpty(tenuresResponseWithOriginalPrice.getData().getPlanDetails())) {
                planDetails2 = tenuresResponseWithOriginalPrice.getData().getPlanDetails();
                planDetailsMap2 = planDetails2.stream().collect(
                        Collectors.toMap(PlanDetail::getPlanId, Function.identity()));
            }
            Map<String, PlanDetail> result = null;
            if (!CollectionUtils.isEmpty(planDetailsMap) && !CollectionUtils.isEmpty(planDetailsMap2)) {
                result = Stream.concat(planDetailsMap2.entrySet().stream(), planDetailsMap.entrySet().stream())
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (value1, value2) -> {
                            if (request.getBody().getApplicableTenures().contains(value1.getInterval().toString())) {
                                value1 = value2;
                            }
                            return value1;
                        }));
            }
            TreeMap<String, PlanDetail> sortedResultMap = new TreeMap<>();
            sortedResultMap.putAll(result);
            if (sortedResultMap != null) {
                List<PlanDetail> newPlanDetails = new ArrayList<>(sortedResultMap.values());
                tenuresResponse.getData().setPlanDetails(newPlanDetails);
            }
        } catch (Exception e) {
            LOGGER.error("Exception occurred : {} ", ExceptionUtils.getStackTrace(e));
        }
        return tenuresResponse;
    }
}
