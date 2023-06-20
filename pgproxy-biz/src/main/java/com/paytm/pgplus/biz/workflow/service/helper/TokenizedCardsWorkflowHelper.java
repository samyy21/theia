package com.paytm.pgplus.biz.workflow.service.helper;

import com.paytm.pgplus.biz.core.model.request.TokenizedCardsRequestBizBean;
import com.paytm.pgplus.biz.core.model.request.TokenizedCardsResponseBizBean;
import com.paytm.pgplus.biz.core.payment.service.IBizPaymentService;
import com.paytm.pgplus.biz.exception.BaseException;
import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.utils.ConfigurationUtil;
import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.theia.redis.ITheiaSessionRedisUtil;
import com.paytm.pgplus.theia.redis.ITheiaTransactionalRedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("tokenizedCardsWorkFlowHelper")
public class TokenizedCardsWorkflowHelper {

    @Autowired
    @Qualifier("commonFlowHelper")
    private WorkFlowRequestCreationHelper workRequestCreator;

    @Autowired
    @Qualifier("bizPaymentService")
    private IBizPaymentService bizPaymentService;

    @Autowired
    Ff4jUtils ff4JUtils;

    @Autowired
    @Qualifier("theiaSessionRedisUtil")
    ITheiaSessionRedisUtil theiaSessionRedisUtil;

    @Autowired
    private ITheiaTransactionalRedisUtil theiaTransactionalRedisUtil;

    public static final Logger LOGGER = LoggerFactory.getLogger(WorkFlowHelper.class);

    public GenericCoreResponseBean<TokenizedCardsResponseBizBean> fetchTokenizedCardsConsult(
            WorkFlowTransactionBean workFlowTransBean) throws BaseException {
        final long startTime = System.currentTimeMillis();
        try {
            GenericCoreResponseBean<TokenizedCardsResponseBizBean> tokenizedCardsResponseBean = null;

            final TokenizedCardsRequestBizBean tokenizedCardsRequestBean = workRequestCreator
                    .createTokenizedCardsRequestBean(workFlowTransBean);

            tokenizedCardsResponseBean = bizPaymentService.fetchTokenizedCards(tokenizedCardsRequestBean);

            if (!tokenizedCardsResponseBean.isSuccessfullyProcessed()) {
                LOGGER.error("Tokenized cards consult Failed due to reason : {} for mid {}", tokenizedCardsResponseBean
                        .getFailureMessage(), workFlowTransBean.getWorkFlowBean().getPaytmMID());
                return new GenericCoreResponseBean<>(tokenizedCardsResponseBean.getFailureMessage(),
                        tokenizedCardsResponseBean.getResponseConstant());
            }
            // Hack For Parllellization Flow to save tokenization
            // if (addAndPay) {
            // setAddAndPayTokenizedCardsConsultResponseInCache(tokenizedCardsResponseBean);
            // } else {
            // setTokenizedCardsResponseInCache(workFlowTransBean.getWorkFlowBean().getPaytmMID(),
            // tokenizedCardsResponseBean);
            // }
            // if (!addAndPay) {
            // tokenizedCardsResponseBean =
            // filterTokenizedPaymentModes(workFlowTransBean,
            // tokenizedCardsResponseBean.getResponse());
            // }
            // commenting caching for now
            /*
             * setTokenizedCardsResponseInCache(workFlowTransBean.getWorkFlowBean
             * ().getPaytmMID(), tokenizedCardsResponseBean);
             */

            LOGGER.info("fetchTokenizedCardWorkflow is successfully processed for mid : {}", workFlowTransBean
                    .getWorkFlowBean().getPaytmMID());
            return tokenizedCardsResponseBean;
        } finally {
            LOGGER.info("Total time taken for TokenizedCard Workflow API  is {} ms", System.currentTimeMillis()
                    - startTime);
        }

    }

    private GenericCoreResponseBean<TokenizedCardsResponseBizBean> fetchTokenizedCardsResponseFromCache(
            WorkFlowTransactionBean workFlowTransBean) {
        /**
         * not to call tokenized cards consult in flipkart flow for api fpov2
         * with access token
         */
        try {
            WorkFlowRequestBean requestBean = workFlowTransBean.getWorkFlowBean();
            String userId = null != workFlowTransBean.getUserDetails() ? workFlowTransBean.getUserDetails().getUserId()
                    : null != requestBean.getUserDetailsBiz() ? requestBean.getUserDetailsBiz().getUserId() : null;
            if (StringUtils.isNotBlank(requestBean.getAccessToken())
                    && ff4JUtils.featureEnabledOnMultipleKeys(requestBean.getPaytmMID(), userId,
                            BizConstant.Ff4jFeature.BLACKLIST_LPV_FPOV2_WITH_ACCESS_TOKEN, false)) {
                LOGGER.info("fetching tokenized cards from cache");
                // if (addAndPay) {
                // return getAddAndPayTokenizedCardsResponse();
                // } else {
                // GenericCoreResponseBean<TokenizedCardsResponseBizBean>
                // tokenizedCardsConsultResponseBean =
                // getTokenizedCardsConsultResponse(requestBean.getPaytmMID());
                // return filterTokenizedPaymentModes(workFlowTransBean,
                // tokenizedCardsConsultResponseBean.getResponse());
                // }
                return getTokenizedCardsConsultResponse(requestBean.getPaytmMID());
            } else {
                LOGGER.info("not fetching tokenized cards from cache since condition false");
            }
        } catch (Exception e) {
            LOGGER.error("Unable to fetch Tokenized cards from cache");
        }
        return null;
    }

    public GenericCoreResponseBean<TokenizedCardsResponseBizBean> getTokenizedCardsConsultResponse(String mid) {
        GenericCoreResponseBean<TokenizedCardsResponseBizBean> response = (GenericCoreResponseBean<TokenizedCardsResponseBizBean>) theiaSessionRedisUtil
                .get(getTokenizedCardsConsultResponseInCacheKey(mid));
        if (response == null
                && !ff4JUtils.isFeatureEnabledOnMid(mid,
                        BizConstant.Ff4jFeature.DISABLE_STATIC_SENTINEL_FOR_LPV_MID_BASED, false)) {
            response = (GenericCoreResponseBean<TokenizedCardsResponseBizBean>) theiaTransactionalRedisUtil
                    .get(getTokenizedCardsConsultResponseInCacheKey(mid));
        }
        return response;
    }

    public String getTokenizedCardsConsultResponseInCacheKey(String mid) {
        return BizConstant.TOKENIZEDCARDS_CONSULT_RESPONSE + mid;
    }

    // private GenericCoreResponseBean<TokenizedCardsResponseBizBean>
    // filterTokenizedPaymentModes(
    // WorkFlowTransactionBean workFlowTransBean,
    // TokenizedCardsResponseBizBean tokenizedCardsConsultResponseBizBean) {
    //
    // if
    // (BizParamValidator.validateInputObjectParam(tokenizedCardsConsultResponseBizBean)
    // && BizParamValidator.validateInputObjectParam(workFlowTransBean)
    // &&
    // BizParamValidator.validateInputObjectParam(workFlowTransBean.getWorkFlowBean()))
    // {
    //
    // List<PayMethodViewsBiz> paymentMethodViewsList =
    // tokenizedCardsConsultResponseBizBean.getPayMethodViews();
    // List<String> allowedPaymentModes =
    // workFlowTransBean.getWorkFlowBean().getAllowedPaymentModes();
    // List<String> disabledPaymentModes =
    // workFlowTransBean.getWorkFlowBean().getDisabledPaymentModes();
    // boolean isEnhancedCashierRequest =
    // workFlowTransBean.getWorkFlowBean().isEnhancedCashierPageRequest();
    // tokenizedCardsConsultResponseBizBean.setPayMethodViews(filterTokenizedPaymentModes(paymentMethodViewsList,
    // allowedPaymentModes, disabledPaymentModes, isEnhancedCashierRequest));
    // // cards do not come under paytm related paymodes - kept for future sync
    // purpose with LPV
    // // if (workFlowTransBean.isDefaultLiteViewFlow()) {
    // // filterPaytmRelatedPayModes(tokenizedCardsConsultResponseBizBean,
    // workFlowTransBean, allowedPaymentModes,
    // // disabledPaymentModes);
    // // }
    // }
    // return new
    // GenericCoreResponseBean<TokenizedCardsResponseBizBean>(tokenizedCardsConsultResponseBizBean);
    // }

    public GenericCoreResponseBean<TokenizedCardsResponseBizBean> getAddAndPayTokenizedCardsResponse() {
        GenericCoreResponseBean<TokenizedCardsResponseBizBean> response = (GenericCoreResponseBean<TokenizedCardsResponseBizBean>) theiaSessionRedisUtil
                .get(BizConstant.ADD_AND_PAY_TOKENIZED_CARDS_CONSULT_RESPONSE);
        if (response == null
                && !ff4JUtils.isFeatureEnabled(BizConstant.Ff4jFeature.DISABLE_STATIC_SENTINEL_FOR_LPV, false)) {
            response = (GenericCoreResponseBean<TokenizedCardsResponseBizBean>) theiaTransactionalRedisUtil
                    .get(BizConstant.ADD_AND_PAY_TOKENIZED_CARDS_CONSULT_RESPONSE);
        }
        return response;
    }

    public void setTokenizedCardsResponseInCache(String mid,
            GenericCoreResponseBean<TokenizedCardsResponseBizBean> consultResponse) {
        LOGGER.info("caching tokenized cards response: {}", consultResponse.getResponse());
        long expiry = Long.parseLong(ConfigurationUtil.getProperty(
                BizConstant.LITEPAYVIEW_CONSULT_CACHE_EXPIREY_SECONDS, "1800"));
        theiaSessionRedisUtil.set(getCacheKeyForTokenizedCardsResponse(mid), consultResponse, expiry);
        if (!ff4JUtils.isFeatureEnabledOnMid(mid, BizConstant.Ff4jFeature.DISABLE_STATIC_SENTINEL_FOR_LPV_MID_BASED,
                false)) {
            theiaTransactionalRedisUtil.set(getCacheKeyForTokenizedCardsResponse(mid), consultResponse, expiry);
        }
    }

    public String getCacheKeyForTokenizedCardsResponse(String mid) {
        return BizConstant.LITEPAYVIEW_CONSULT_RESPONSE + mid;
    }

    // public void setAddAndPayTokenizedCardsConsultResponseInCache(
    // GenericCoreResponseBean<TokenizedCardsResponseBizBean>
    // addAndPayTokenizedConsultResponse) {
    // long expiry =
    // Long.parseLong(ConfigurationUtil.getProperty(BizConstant.ADD_AND_PAY_LITEPAYVIEW_CONSULT_EXPIREY,
    // "43200"));
    // theiaSessionRedisUtil.set(BizConstant.ADD_AND_PAY_LITEPAYVIEW_CONSULT_RESPONSE,
    // addAndPayLitePayviewConsultResponse, expiry);
    // if
    // (!ff4JUtils.isFeatureEnabled(BizConstant.Ff4jFeature.DISABLE_STATIC_SENTINEL_FOR_LPV,
    // false)) {
    // theiaTransactionalRedisUtil.set(BizConstant.ADD_AND_PAY_LITEPAYVIEW_CONSULT_RESPONSE,
    // addAndPayLitePayviewConsultResponse, expiry);
    // }
    // }
    // private void setTokenizedCardsResponseInCache(WorkFlowTransactionBean
    // workFlowTransBean, boolean addAndPay,
    // GenericCoreResponseBean<TokenizedCardsResponseBizBean>
    // tokenizedCardsResponseBizBean) {
    // if (addAndPay) {
    // setAddAndPayTokenizedCardsConsultResponseInCache(tokenizedCardsResponseBizBean);
    // } else {
    // if (isCachingConfiguredForOfflineFlow(workFlowTransBean)) {
    // enableUserBalanceFlagForOffline(workFlowTransBean,
    // tokenizedCardsResponseBizBean);
    // }
    // // Hack for UPI Express
    // setTokenizedCardsResponseInCache(workFlowTransBean.getWorkFlowBean().getPaytmMID(),
    // tokenizedCardsResponseBizBean);
    // }

    // }
}
