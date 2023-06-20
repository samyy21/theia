package com.paytm.pgplus.theia.services.impl;

import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.service.IWorkFlow;
import com.paytm.pgplus.cashier.cachecard.model.SavedCardRequest;
import com.paytm.pgplus.common.enums.EventNameEnum;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams;
import com.paytm.pgplus.theia.constants.TheiaConstant.RetryConstants;
import com.paytm.pgplus.theia.datamapper.IBizRequestResponseMapper;
import com.paytm.pgplus.theia.exceptions.TheiaDataMappingException;
import com.paytm.pgplus.theia.exceptions.TheiaServiceException;
import com.paytm.pgplus.theia.services.IBizService;
import com.paytm.pgplus.theia.services.IRetryPaymentService;
import com.paytm.pgplus.theia.services.helper.RetryServiceHelper;
import com.paytm.pgplus.theia.session.utils.RetryPaymentInfoSessionUtil;
import com.paytm.pgplus.theia.utils.EventUtils;
import com.paytm.pgplus.theia.utils.MerchantResponseService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

/**
 * @author manojpal
 *
 */
@Service("retryPaymentServiceImpl")
public class RetryPaymentServiceImpl implements IRetryPaymentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RetryPaymentServiceImpl.class);

    @Autowired
    @Qualifier("retryPaymentFlowUserLoggedIn")
    private IWorkFlow retryPaymentFlowUserLoggedIn;

    @Autowired
    @Qualifier("retryPaymentFlowUserNotLoggedIn")
    private IWorkFlow retryPaymentFlowUserNotLoggedIn;

    @Autowired
    @Qualifier("retryBuyerPaysChargeFlow")
    private IWorkFlow retryBuyerPaysChargeFlow;

    @Autowired
    @Qualifier("retryBuyerPaysChargeUserLoggedInFlow")
    private IWorkFlow retryBuyerPaysChargeUserLoggedInFlow;

    @Autowired
    @Qualifier("retryAddMoneyFlow")
    private IWorkFlow retryAddMoneyFlow;

    @Autowired
    @Qualifier("retryPaymentInfoSessionUtil")
    private RetryPaymentInfoSessionUtil retryPaymentInfoSessionUtil;

    @Autowired
    @Qualifier("bizService")
    protected IBizService bizService;

    @Autowired
    @Qualifier("bizRequestResponseMapper")
    protected IBizRequestResponseMapper bizRequestResponseMapper;

    @Autowired
    @Qualifier("merchantResponseService")
    private MerchantResponseService merchantResponseService;

    @Autowired
    private RetryServiceHelper retryServiceHelper;

    @Autowired
    @Qualifier("merchantPreferenceService")
    private IMerchantPreferenceService merchantPreferenceService;

    @Override
    public boolean processPaymentRequest(HttpServletRequest request, SavedCardRequest savedCardRequest)
            throws TheiaServiceException {
        WorkFlowRequestBean workFlowRequestBean = null;
        WorkFlowResponseBean workFlowResponseBean;

        // Fetch Merchant ID from PaymentStatus#extendInfo
        // LOGGER.info("Processing retry payment request for mid :{}",
        // request.getParameter(ExtraConstants.MERCHANT_ID));

        // Fetching transId
        String transId = request.getParameter(RetryConstants.TRANS_ID);
        LOGGER.info("Processing retry payment request for transId :{}", transId);

        // Need to fetch PaymentRequestBean for original request from cache
        PaymentRequestBean originalRequestData = null;
        try {
            originalRequestData = retryServiceHelper.getRequestDataFromCache(transId);
        } catch (Exception e) {
            LOGGER.error("Exception occured while fetching Requestdata from cache", e);
            throw new TheiaServiceException("Exception occured while fetching Requestdata from cache");
        }

        if (originalRequestData != null) {
            // Setting current request into originalRequestData
            originalRequestData.setRequest(request);

            try {
                workFlowRequestBean = bizRequestResponseMapper.mapWorkFlowRequestData(originalRequestData);
            } catch (TheiaDataMappingException e) {
                throw new TheiaServiceException(e);
            }
            // Setting Channel_Id & Theme in current HttpRequest
            request.setAttribute(RequestParams.CHANNEL_ID,
                    originalRequestData.getChannelId() != null ? originalRequestData.getChannelId()
                            : workFlowRequestBean.getChannelID());
            request.setAttribute(RequestParams.THEME, originalRequestData.getTheme());
            request.setAttribute(RequestParams.REQUEST_TYPE, originalRequestData.getRequestType());
        } else {
            LOGGER.error("OriginalRequestdata not fetched from cache");
        }
        LOGGER.debug("WorkFlowRequestBean is : {}", workFlowRequestBean);

        if (workFlowRequestBean == null) {
            throw new TheiaServiceException("WorkFlowRequestBean is null");
        }

        // Setting transId
        workFlowRequestBean.setTransID(transId);

        GenericCoreResponseBean<WorkFlowResponseBean> bizResponseBean = processBizWorkFlow(workFlowRequestBean);

        /*
         * Check if biz Response Failed and do we have to send response to
         * merchant
         */
        if (!bizResponseBean.isSuccessfullyProcessed() && bizResponseBean.getResponseConstant() != null
                && bizResponseBean.getResponseConstant().isResponseToMerchant()) {
            LOGGER.info("Sending response to merchant , Biz Call Unsuccessfull due to... {}",
                    bizResponseBean.getFailureDescription());

        }

        workFlowResponseBean = bizResponseBean.getResponse();
        LOGGER.debug("WorkFlowResponseBean :{}", workFlowResponseBean);

        if (workFlowResponseBean == null) {
            LOGGER.error("SYSTEM_ERROR, Reason :: {}", bizResponseBean.getFailureDescription());
            EventUtils.pushTheiaEventforbizError(EventNameEnum.NATIVE, workFlowRequestBean.getPaytmMID(),
                    workFlowRequestBean.getOrderID(), bizResponseBean.getFailureDescription());
            throw new TheiaServiceException("WorkFlowResponseBean is null, Reason : "
                    + bizResponseBean.getFailureDescription());
        }
        if (savedCardRequest != null) {
            bizRequestResponseMapper.mapWorkFlowResponseToSession(originalRequestData, workFlowResponseBean,
                    savedCardRequest);
        } else {
            bizRequestResponseMapper.mapWorkFlowResponseToSession(originalRequestData, workFlowResponseBean);
        }

        // Setting retry payment details also into session
        retryPaymentInfoSessionUtil.setRetryPaymentInfoIntoSession(originalRequestData, workFlowResponseBean);

        return true;
    }

    private GenericCoreResponseBean<WorkFlowResponseBean> processBizWorkFlow(WorkFlowRequestBean workFlowRequestBean) {
        return bizService.processWorkFlow(workFlowRequestBean, fetchWorkflow(workFlowRequestBean));

    }

    private IWorkFlow fetchWorkflow(final WorkFlowRequestBean workFlowRequestBean) {
        IWorkFlow workflow;

        if (workFlowRequestBean.getRequestType().getType().equals(TheiaConstant.RequestTypes.ADD_MONEY)) {
            workflow = retryAddMoneyFlow;
        } else if (merchantPreferenceService.isPostConvenienceFeesEnabled(workFlowRequestBean.getPaytmMID())) {
            if (StringUtils.isNotBlank(workFlowRequestBean.getToken())) {
                workflow = retryBuyerPaysChargeUserLoggedInFlow;
            } else {
                workflow = retryBuyerPaysChargeFlow;
            }
        } else {
            if (StringUtils.isNotBlank(workFlowRequestBean.getToken())) {
                workflow = retryPaymentFlowUserLoggedIn;
            } else {
                workflow = retryPaymentFlowUserNotLoggedIn;
            }
        }

        return workflow;
    }

}