package com.paytm.pgplus.biz.workflow.edc.helper;

import com.paytm.pgplus.biz.core.model.request.CacheCardResponseBean;
import com.paytm.pgplus.biz.enums.CacheCardType;
import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.biz.workflow.model.*;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowHelper;
import com.paytm.pgplus.common.config.ConfigurationUtil;
import com.paytm.pgplus.common.model.link.EdcEmiChannelDetail;
import com.paytm.pgplus.common.model.link.EdcEmiDetails;
import com.paytm.pgplus.common.model.link.LinkDetailResponseBody;
import com.paytm.pgplus.facade.constants.FacadeConstants;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.pgproxycommon.utils.AmountUtils;
import com.paytm.pgplus.pgproxycommon.utils.DateUtils;
import com.paytm.pgplus.logging.ExtendedLogger;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.paytm.pgplus.biz.utils.BizConstant.Ff4jFeature.ENABLE_GCIN_ON_COFT_PROMO;

@Component("edcLinkHelper")
public class EdcLinkHelper implements Serializable {

    public static final String CHECKOUT_API_VERSION = "1.9.7";
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(EdcLinkHelper.class);

    @Autowired
    private Environment environment;

    @Autowired
    WorkFlowHelper workFlowHelper;

    @Autowired
    private Ff4jUtils ff4jUtils;

    private SimpleDateFormat formatter = new SimpleDateFormat(BizConstant.EdcLinkEmiTxn.EDC_LINK_API_DATE_FORMATE);
    private SimpleDateFormat offerCheckoutDateFormat = new SimpleDateFormat(
            BizConstant.EdcLinkEmiTxn.EDC_OFFER_CHECKOUT_DATE_FORMAT);

    public BankEmiRequest prepareBankEmiRequest(WorkFlowTransactionBean request) {

        BankEmiRequest bankEmiRequest = new BankEmiRequest();
        BankEmiRequestHead bankEmiHead = new BankEmiRequestHead();
        BankEmiRequestBody bankEmiBody = new BankEmiRequestBody();
        Date currentDate = new Date();
        Calendar cal = Calendar.getInstance();

        String clientId = ConfigurationUtil.getProperty(FacadeConstants.EDC_CLIENT_ID);
        String clientSecret = environment.getProperty(FacadeConstants.EDC_BANK_CLIENT_SECRET);
        bankEmiHead.setClientId(clientId);
        bankEmiHead.setClientSecret(clientSecret);

        String timeIn24Hours = formatter.format(currentDate);
        bankEmiBody.setTime(timeIn24Hours);
        bankEmiBody.setYear(String.valueOf(cal.get(Calendar.YEAR)));
        bankEmiBody.setDate(cal.get(Calendar.DATE) + String.valueOf(cal.get(Calendar.MONTH) + 1));
        bankEmiBody.setMid(request.getWorkFlowBean().getPaytmMID());

        if (request.getWorkFlowBean().isCoftTokenTxn() && request.getWorkFlowBean().getAccountRangeCardBin() != null
                && request.getWorkFlowBean().getAccountRangeCardBin().length() >= 6) {

            bankEmiBody.setBin(request.getWorkFlowBean().getAccountRangeCardBin().substring(0, 6));

        } else if (request.getWorkFlowBean().getCardNo() != null && request.getWorkFlowBean().getCardNo().length() >= 6) {
            bankEmiBody.setBin(request.getWorkFlowBean().getCardNo().substring(0, 6));
        }

        String productAmount = request.getWorkFlowBean().getPaymentRequestBean().getLinkDetailsData().getEdcEmiFields()
                .getProductAmount();

        bankEmiBody.setAmount(AmountUtils.getTransactionAmountInPaise(productAmount));
        if (request.getWorkFlowBean().getPaymentRequestBean().getLinkDetailsData() != null)
            bankEmiBody.setTid(request.getWorkFlowBean().getPaymentRequestBean().getLinkDetailsData()
                    .getMerchantReferenceId());
        String cardIndexNo = getCardIndexNumber(request);
        bankEmiBody.setCardIndexNumber(cardIndexNo);

        bankEmiRequest.setBody(bankEmiBody);
        bankEmiRequest.setHead(bankEmiHead);

        return bankEmiRequest;
    }

    public BrandEmiRequest prepareBrandEmiRequest(WorkFlowTransactionBean request) {
        BrandEmiRequest brandEmiRequest = new BrandEmiRequest();
        BrandEmiRequestHead brandEmiHead = new BrandEmiRequestHead();
        BrandEmiRequestBody brandEmiBody = new BrandEmiRequestBody();

        String clientId = ConfigurationUtil.getProperty(FacadeConstants.EDC_CLIENT_ID);
        String clientSecret = environment.getProperty(FacadeConstants.EDC_BRAND_CLIENT_SECRET);
        Date currentDate = new Date();
        Calendar cal = Calendar.getInstance();

        brandEmiHead.setClientId(clientId);
        brandEmiHead.setClientSecret(clientSecret);

        if (request.getWorkFlowBean().isCoftTokenTxn() && request.getWorkFlowBean().getAccountRangeCardBin() != null
                && request.getWorkFlowBean().getAccountRangeCardBin().length() >= 6) {

            brandEmiBody.setBin(request.getWorkFlowBean().getAccountRangeCardBin().substring(0, 6));

        } else if (request.getWorkFlowBean().getCardNo() != null && request.getWorkFlowBean().getCardNo().length() >= 6) {
            brandEmiBody.setBin(request.getWorkFlowBean().getCardNo().substring(0, 6));
        }

        String cardIndexNo = getCardIndexNumber(request);
        brandEmiBody.setCardIndexNumber(cardIndexNo);
        brandEmiBody.setMid(request.getWorkFlowBean().getPaytmMID());
        String timeIn24Hours = formatter.format(currentDate);
        brandEmiBody.setTime(timeIn24Hours);
        brandEmiBody.setYear(String.valueOf(cal.get(Calendar.YEAR)));
        brandEmiBody.setDate(cal.get(Calendar.DATE) + String.valueOf(cal.get(Calendar.MONTH) + 1));

        LinkDetailResponseBody linkDetailsData = request.getWorkFlowBean().getPaymentRequestBean().getLinkDetailsData();
        if (linkDetailsData != null && linkDetailsData.getEdcEmiFields() != null) {
            String productAmount = linkDetailsData.getEdcEmiFields().getProductAmount();
            brandEmiBody.setAmount(AmountUtils.getTransactionAmountInPaise(productAmount));
            brandEmiBody.setTid(linkDetailsData.getMerchantReferenceId());
            brandEmiBody.setBrandId(linkDetailsData.getEdcEmiFields().getBrandId());
            brandEmiBody.setProductId(linkDetailsData.getEdcEmiFields().getProductId());
            String categoryId = linkDetailsData.getEdcEmiFields().getCategoryId();
            if (StringUtils.isNotBlank(categoryId)) {
                brandEmiBody.setCategoryId(categoryId);
            }
            brandEmiBody.setModel(linkDetailsData.getEdcEmiFields().getModel());
            brandEmiBody.setIsEmiEnabled(linkDetailsData.getEdcEmiFields().getIsEmiEnabled());
            brandEmiBody.setEan(linkDetailsData.getEdcEmiFields().getEan());
            brandEmiBody.setVerticalId(linkDetailsData.getEdcEmiFields().getVerticalId());
            brandEmiBody.setQuantity(linkDetailsData.getEdcEmiFields().getQuantity());
        }
        brandEmiRequest.setBody(brandEmiBody);
        brandEmiRequest.setHead(brandEmiHead);
        return brandEmiRequest;
    }

    private String getCardIndexNumber(WorkFlowTransactionBean request) {
        if (ff4jUtils.isFeatureEnabledOnMid(request.getWorkFlowBean().getPaytmMID(), ENABLE_GCIN_ON_COFT_PROMO, false)) {
            return request.getWorkFlowBean().getGcin();
        } else {
            if (StringUtils.isNotBlank(request.getWorkFlowBean().getCardIndexNo())) {
                return request.getWorkFlowBean().getCardIndexNo();
            } else {
                final GenericCoreResponseBean<CacheCardResponseBean> createCacheCardResponse = workFlowHelper
                        .cacheCard(request, CacheCardType.SEAMLESS);
                if (createCacheCardResponse.isSuccessfullyProcessed()) {
                    return createCacheCardResponse.getResponse().getCardIndexNo();
                }
            }
        }
        return null;
    }

    public OfferCheckoutRequest prepareCheckoutRequest(WorkFlowTransactionBean request) {

        OfferCheckoutRequest offerCheckoutRequest = new OfferCheckoutRequest();
        OfferCheckoutRequestHead offerCheckoutRequestHead = new OfferCheckoutRequestHead();
        offerCheckoutRequestHead.setVersion(CHECKOUT_API_VERSION);
        String clientId = ConfigurationUtil.getProperty(FacadeConstants.EDC_CLIENT_ID);
        String clientSecret = environment.getProperty(FacadeConstants.EDC_CHECKOUT_CLIENT_SECRET);

        offerCheckoutRequestHead.setClientId(clientId);
        offerCheckoutRequestHead.setClientSecret(clientSecret);
        offerCheckoutRequest.setHead(offerCheckoutRequestHead);
        offerCheckoutRequest.setBody(populateCheckoutRequestBody(request));
        return offerCheckoutRequest;
    }

    private OfferCheckoutRequestBody populateCheckoutRequestBody(WorkFlowTransactionBean workFlowTransactionBean) {
        WorkFlowRequestBean workFlowRequestBean = workFlowTransactionBean.getWorkFlowBean();
        OfferCheckoutRequestBody offerCheckoutRequestBody = new OfferCheckoutRequestBody();
        Date currentDate = new Date();
        Calendar cal = Calendar.getInstance();

        EdcEmiDetails edcEmiFields = null;
        if (Boolean.TRUE.equals(workFlowTransactionBean.getWorkFlowBean().getEdcLinkTxn())) {
            edcEmiFields = workFlowRequestBean.getPaymentRequestBean().getLinkDetailsData().getEdcEmiFields();
        }

        offerCheckoutRequestBody.setMid(workFlowRequestBean.getPaytmMID());
        String timeIn24Hours = formatter.format(currentDate);
        offerCheckoutRequestBody.setTime(timeIn24Hours);
        offerCheckoutRequestBody.setYear(String.valueOf(cal.get(Calendar.YEAR)));
        offerCheckoutRequestBody.setDate(offerCheckoutDateFormat.format(currentDate));

        if (workFlowTransactionBean.getWorkFlowBean().getUserDetailsBiz() != null)
            offerCheckoutRequestBody.setUserMobile(workFlowRequestBean.getUserDetailsBiz().getMobileNo());
        else
            offerCheckoutRequestBody.setUserMobile(workFlowRequestBean.getPaymentRequestBean().getLinkDetailsData()
                    .getCustomerMobile());

        if (edcEmiFields != null) {

            if (StringUtils.isNotBlank(workFlowRequestBean.getPaymentRequestBean().getLinkDetailsData()
                    .getCustomerMobile()))
                offerCheckoutRequestBody.setUserMobile(workFlowRequestBean.getPaymentRequestBean().getLinkDetailsData()
                        .getCustomerMobile());
            else
                offerCheckoutRequestBody.setUserMobile(BizConstant.STR_EMPTY);

            offerCheckoutRequestBody.setOrderID(workFlowRequestBean.getOrderID());
            offerCheckoutRequestBody.setValidationKey(edcEmiFields.getValidationKey());
            offerCheckoutRequestBody.setValidationValue(edcEmiFields.getValidationValue());
            offerCheckoutRequestBody
                    .setAmount(AmountUtils.getTransactionAmountInPaise(edcEmiFields.getProductAmount()));
            offerCheckoutRequestBody.setBrandId(edcEmiFields.getBrandId());
            offerCheckoutRequestBody.setBrandName(edcEmiFields.getBrandName());
            offerCheckoutRequestBody.setCategoryId(edcEmiFields.getCategoryId());
            offerCheckoutRequestBody.setCategoryName(edcEmiFields.getCategoryName());
            offerCheckoutRequestBody.setProductId(edcEmiFields.getProductId());
            offerCheckoutRequestBody.setProductName(edcEmiFields.getProductName());
            offerCheckoutRequestBody.setModel(edcEmiFields.getModel());
            offerCheckoutRequestBody.setVerticalId(edcEmiFields.getVerticalId());
            offerCheckoutRequestBody.setIsEmiEnabled(edcEmiFields.getIsEmiEnabled());
            offerCheckoutRequestBody.setSkuCode(edcEmiFields.getSkuCode());
            offerCheckoutRequestBody.setTid(workFlowRequestBean.getPaymentRequestBean().getLinkDetailsData()
                    .getMerchantReferenceId());
            offerCheckoutRequestBody.setInvoiceNumber(edcEmiFields.getBrandInvoiceNumber());
            offerCheckoutRequestBody.setCardIndexNumber(getCardIndexNumber(workFlowTransactionBean));
            if (workFlowRequestBean.isCoftTokenTxn() && workFlowRequestBean.getAccountRangeCardBin() != null
                    && workFlowRequestBean.getAccountRangeCardBin().length() >= 6) {
                offerCheckoutRequestBody.setBin(workFlowRequestBean.getAccountRangeCardBin().substring(0, 6));
            } else if (workFlowRequestBean.getCardNo() != null && workFlowRequestBean.getCardNo().length() >= 6) {
                offerCheckoutRequestBody.setBin(workFlowRequestBean.getCardNo().substring(0, 6));
            }
            offerCheckoutRequestBody.setProductCode(edcEmiFields.getProductCode());
            offerCheckoutRequestBody.setQuantity(edcEmiFields.getQuantity());
            if (edcEmiFields.getEmiChannelDetail() != null) {

                EdcEmiChannelDetail emiChannelDetail = edcEmiFields.getEmiChannelDetail();
                offerCheckoutRequestBody.setPgPlanId(emiChannelDetail.getPgPlanId());
                offerCheckoutRequestBody.setPlanId(emiChannelDetail.getPlanId());
                offerCheckoutRequestBody.setEmiAmount(AmountUtils.getTransactionAmountInPaise(emiChannelDetail
                        .getEmiAmount().getValue()));
                offerCheckoutRequestBody.setEmiTotalAmount(AmountUtils.getTransactionAmountInPaise(edcEmiFields
                        .getEmiChannelDetail().getTotalAmount().getValue()));
                offerCheckoutRequestBody.setInterestRate(emiChannelDetail.getInterestRate());
                offerCheckoutRequestBody.setTenure(emiChannelDetail.getEmiMonths());

                if (CollectionUtils.isNotEmpty(emiChannelDetail.getOfferDetails())
                        && emiChannelDetail.getOfferDetails().get(0) != null) {
                    offerCheckoutRequestBody.setOfferId(emiChannelDetail.getOfferDetails().get(0).getOfferId());
                    offerCheckoutRequestBody.setOfferAmount(AmountUtils.getTransactionAmountInPaise(emiChannelDetail
                            .getOfferDetails().get(0).getAmount().getValue()));
                }
            }
        }
        return offerCheckoutRequestBody;
    }

    public MultivaluedMap<String, Object> prepareHeaderMap(WorkFlowTransactionBean workFlowTransactionBean) {
        final MultivaluedMap<String, Object> headerMap = new MultivaluedHashMap<>();
        headerMap.add("content-type", MediaType.APPLICATION_JSON);
        headerMap.add("X-REQUEST-ID", UUID.randomUUID().toString());
        headerMap.add("X-CLIENT", "PG");
        headerMap.add("X-CLIENT-ID", workFlowTransactionBean.getWorkFlowBean().getPaytmMID());
        return headerMap;
    }

    public void setTimestamp(BrandEmiRequestBody brandEmiBody) {
        Date currentDate = new Date();
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat("kkmmss");
        String timeIn24Hours = formatter.format(currentDate);
        brandEmiBody.setTime(timeIn24Hours);
        brandEmiBody.setYear(String.valueOf(cal.get(Calendar.YEAR)));
        brandEmiBody.setDate(cal.get(Calendar.DATE) + String.valueOf(cal.get(Calendar.MONTH)));

    }

    public Map<String, String> prepareQueryParams(WorkFlowTransactionBean workFlowTransactionBean) {
        Map<String, String> queryParams = new HashMap<>();
        // queryParams.put(BizConstant.MID,
        // workFlowTransactionBean.getWorkFlowBean().getPaytmMID());
        return queryParams;
    }

    public ValidateVelocityRequest prepareValidateVelocityRequest(WorkFlowTransactionBean workflowTxnBean) {
        String clientId = ConfigurationUtil.getProperty(FacadeConstants.EDC_CLIENT_ID);
        String clientSecret = environment.getProperty(FacadeConstants.EDC_VALIDATE_VELOCITY_CLIENT_SECRET);

        ValidateVelocityRequest validateVelocityRequest = new ValidateVelocityRequest();
        ValidateVelocityRequestHead validateVelocityRequestHead = new ValidateVelocityRequestHead();
        validateVelocityRequestHead.setVersion(CHECKOUT_API_VERSION);
        validateVelocityRequestHead.setClientId(clientId);
        validateVelocityRequestHead.setClientSecret(clientSecret);
        validateVelocityRequest.setHead(validateVelocityRequestHead);
        validateVelocityRequest.setBody(populateValidateVelocityRequest(workflowTxnBean));
        return validateVelocityRequest;
    }

    public ValidateVelocityRequestBody populateValidateVelocityRequest(WorkFlowTransactionBean workFlowTransactionBean) {
        WorkFlowRequestBean workFlowRequestBean = workFlowTransactionBean.getWorkFlowBean();
        ValidateVelocityRequestBody validateVelocityRequestBody = new ValidateVelocityRequestBody();
        Date currentDate = new Date();
        Calendar cal = Calendar.getInstance();

        EdcEmiDetails edcEmiFields = null;
        if (Boolean.TRUE.equals(workFlowTransactionBean.getWorkFlowBean().getEdcLinkTxn())) {
            edcEmiFields = workFlowRequestBean.getPaymentRequestBean().getLinkDetailsData().getEdcEmiFields();
        }

        validateVelocityRequestBody.setMid(workFlowRequestBean.getPaytmMID());
        String timeIn24Hours = formatter.format(currentDate);
        validateVelocityRequestBody.setTime(timeIn24Hours);
        validateVelocityRequestBody.setYear(String.valueOf(cal.get(Calendar.YEAR)));
        validateVelocityRequestBody.setDate(offerCheckoutDateFormat.format(currentDate));

        if (edcEmiFields != null) {
            validateVelocityRequestBody.setAmount(AmountUtils.getTransactionAmountInPaise(edcEmiFields
                    .getProductAmount()));
            validateVelocityRequestBody.setBrandId(edcEmiFields.getBrandId());
            validateVelocityRequestBody.setBrandName(edcEmiFields.getBrandName());
            validateVelocityRequestBody.setCategoryId(edcEmiFields.getCategoryId());
            validateVelocityRequestBody.setCategoryName(edcEmiFields.getCategoryName());
            validateVelocityRequestBody.setProductId(edcEmiFields.getProductId());
            validateVelocityRequestBody.setProductName(edcEmiFields.getProductName());
            validateVelocityRequestBody.setModel(edcEmiFields.getModel());
            validateVelocityRequestBody.setVerticalId(edcEmiFields.getVerticalId());
            validateVelocityRequestBody.setIsEmiEnabled(edcEmiFields.getIsEmiEnabled());
            validateVelocityRequestBody.setSkuCode(edcEmiFields.getSkuCode());
            validateVelocityRequestBody.setTid(workFlowRequestBean.getPaymentRequestBean().getLinkDetailsData()
                    .getMerchantReferenceId());
            validateVelocityRequestBody.setCardIndexNumber(getCardIndexNumber(workFlowTransactionBean));
            if (workFlowRequestBean.isCoftTokenTxn() && workFlowRequestBean.getAccountRangeCardBin() != null
                    && workFlowRequestBean.getAccountRangeCardBin().length() >= 6) {
                validateVelocityRequestBody.setBin(workFlowRequestBean.getAccountRangeCardBin().substring(0, 6));
            } else if (workFlowRequestBean.getCardNo() != null && workFlowRequestBean.getCardNo().length() >= 6) {
                validateVelocityRequestBody.setBin(workFlowRequestBean.getCardNo().substring(0, 6));
            }
            validateVelocityRequestBody.setProductCode(edcEmiFields.getProductCode());
            validateVelocityRequestBody.setQuantity(edcEmiFields.getQuantity());
            if (edcEmiFields.getEmiChannelDetail() != null) {
                // Overriding amount to discounted amount only in case of Bank
                // Offer, in other cases productAmount is to be sent
                // in amount which is already set in the
                // ValidateVelocityRequestBody initially.
                if (edcEmiFields.getEmiChannelDetail().getBankOfferDetails() != null
                        && edcEmiFields.getEmiChannelDetail().getBankOfferDetails().get(0) != null
                        && BizConstant.DISCOUNT.equalsIgnoreCase(edcEmiFields.getEmiChannelDetail()
                                .getBankOfferDetails().get(0).getType())) {
                    Double productAmount = Double.valueOf(AmountUtils.getTransactionAmountInPaise(edcEmiFields
                            .getProductAmount()));
                    Double bankOfferAmount = Double.valueOf(AmountUtils.getTransactionAmountInPaise(edcEmiFields
                            .getEmiChannelDetail().getBankOfferDetails().get(0).getAmount().getValue()));
                    validateVelocityRequestBody.setAmount(String.valueOf(productAmount - bankOfferAmount));
                }
                EdcEmiChannelDetail emiChannelDetail = edcEmiFields.getEmiChannelDetail();
                validateVelocityRequestBody.setPgPlanId(emiChannelDetail.getPgPlanId());
                validateVelocityRequestBody.setPlanId(emiChannelDetail.getPlanId());
                validateVelocityRequestBody.setEmiAmount(AmountUtils.getTransactionAmountInPaise(emiChannelDetail
                        .getEmiAmount().getValue()));
                validateVelocityRequestBody.setEmiTotalAmount(AmountUtils.getTransactionAmountInPaise(edcEmiFields
                        .getEmiChannelDetail().getTotalAmount().getValue()));
                validateVelocityRequestBody.setInterestRate(emiChannelDetail.getInterestRate());
                validateVelocityRequestBody.setTenure(emiChannelDetail.getEmiMonths());

                if (CollectionUtils.isNotEmpty(emiChannelDetail.getOfferDetails())
                        && emiChannelDetail.getOfferDetails().get(0) != null) {
                    validateVelocityRequestBody.setOfferId(emiChannelDetail.getOfferDetails().get(0).getOfferId());
                    validateVelocityRequestBody.setOfferAmount(AmountUtils.getTransactionAmountInPaise(emiChannelDetail
                            .getOfferDetails().get(0).getAmount().getValue()));
                }
            }
        }
        return validateVelocityRequestBody;
    }
}
