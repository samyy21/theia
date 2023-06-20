package com.paytm.pgplus.theia.datamapper;

import com.paytm.pgplus.biz.core.model.request.ExtendedInfoRequestBean;
import com.paytm.pgplus.biz.core.payment.service.IBizPaymentService;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.cache.model.EMIDetailList;
import com.paytm.pgplus.cache.model.EMIDetails;
import com.paytm.pgplus.cache.model.EmiDetailRequest;
import com.paytm.pgplus.common.model.ResultInfo;
import com.paytm.pgplus.facade.enums.CardAcquiringMode;
import com.paytm.pgplus.logging.ExtendedLogger;
import com.paytm.pgplus.mappingserviceclient.exception.MappingServiceClientException;
import com.paytm.pgplus.mappingserviceclient.service.IEMIDetails;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.theia.constants.ResponseCodeConstant;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.datamapper.dto.EMIDetailMethodDTO;
import com.paytm.pgplus.theia.datamapper.dto.EMIPayMethodDTO;
import com.paytm.pgplus.theia.datamapper.validator.Validator;
import com.paytm.pgplus.theia.exceptions.TheiaServiceException;
import com.paytm.pgplus.theia.offline.exceptions.PaymentRequestProcessingException;
import com.paytm.pgplus.theia.offline.exceptions.RequestValidationException;
import com.paytm.pgplus.theia.services.ITheiaSessionDataService;
import com.paytm.pgplus.theia.utils.EmiBinValidationUtil;
import com.paytm.pgplus.theia.utils.helper.EMIPaymentHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author ruchikagarg
 */
@Component
public class EMIPayMethodBuilder extends PayMethodBuilder<EMIPayMethodDTO> {

    public static final Logger LOGGER = LoggerFactory.getLogger(EMIPayMethodBuilder.class);
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(EMIPayMethodBuilder.class);

    @Autowired
    private Validator<EMIPayMethodDTO> emiPayMethodValidator;

    @Autowired
    private Validator<EMIDetailMethodDTO> emiDetailListValidator;

    @Autowired
    @Qualifier("bizPaymentService")
    private IBizPaymentService bizPaymentService;

    @Autowired
    private IEMIDetails emiDetailsClient;

    @Autowired
    @Qualifier("theiaSessionDataService")
    private ITheiaSessionDataService theiaSessionDataService;

    @Autowired
    @Qualifier("emiBinValidationUtil")
    private EmiBinValidationUtil emiBinValidationUtil;

    @Override
    public void build() throws RequestValidationException, PaymentRequestProcessingException {
        try {
            emiPayMethodValidator.validate(obj);
        } catch (TheiaServiceException exception) {
            LOGGER.error("Error while validating emi pay details:{}", exception);
            ResultInfo resultInfo = new ResultInfo();
            resultInfo.setRedirect(false);
            resultInfo.setResultCode(ResponseCodeConstant.INVALID_PAYMENT_DETAILS);
            resultInfo.setResultMsg("Found Invalid details for this EMI plan");
            throw new RequestValidationException(resultInfo);
        }
        WorkFlowRequestBean flowRequestBean = obj.getWorkFlowRequestBean();
        PaymentRequestBean paymentRequestBean = obj.getPaymentRequestBean();

        if (flowRequestBean.getChannelInfo() == null) {
            flowRequestBean.setChannelInfo(new HashMap<>());
        }

        ExtendedInfoRequestBean extendedInfoRequestBean = flowRequestBean.getExtendInfo();
        if (extendedInfoRequestBean == null) {
            extendedInfoRequestBean = new ExtendedInfoRequestBean();
        }
        extendedInfoRequestBean.setEmiPlanId(paymentRequestBean.getEmiPlanID());

        EMIDetails emiDetail = null;

        try {
            LOGGER.info("Fetching emi details for  mapping for mid:{}, planId:{}", paymentRequestBean.getMid(),
                    paymentRequestBean.getEmiPlanID());

            EmiDetailRequest emiDetailRequest = new EmiDetailRequest();
            emiDetailRequest.setMid(paymentRequestBean.getMid());
            emiDetailRequest.setStatus(Boolean.TRUE);
            EMIDetailList emiDetailList = emiDetailsClient.getEMIByMid(emiDetailRequest);
            EXT_LOGGER.customInfo("Mapping response - EMIDetailList :: {} for EMIDetailRequest :: {}", emiDetailList,
                    emiDetailRequest);

            final Long planId = Long.parseLong(paymentRequestBean.getEmiPlanID());

            LOGGER.debug("Validate Mapping Request Params:{}", emiDetailList);
            EMIDetailMethodDTO emiDetailMethodDTO = new EMIDetailMethodDTO();
            emiDetailMethodDTO.setEmiDetailList(emiDetailList);
            emiDetailMethodDTO.setEmiPlanId(planId);
            emiDetailMethodDTO.setBankCode(paymentRequestBean.getBankCode());
            emiDetailListValidator.validate(emiDetailMethodDTO);
            emiBinValidationUtil.validateEmiDetailsForPaytmExpress(flowRequestBean.getInstId(),
                    flowRequestBean.getBankName(), emiDetailMethodDTO);

            LOGGER.debug("Successfully Validated Mapping Request Params!");

            emiDetail = emiDetailList.getEmiDetails().get(planId);
            LOGGER.debug("EMI Details object:{}", emiDetail);

        } catch (MappingServiceClientException | TheiaServiceException e) {
            LOGGER.error("Error while validating emi pay details:mid:{}, orderId:{}", paymentRequestBean.getMid(),
                    paymentRequestBean.getOrderId(), e);
            ResultInfo resultInfo = new ResultInfo();
            resultInfo.setRedirect(false);
            resultInfo.setResultCode(ResponseCodeConstant.INVALID_PAYMENT_DETAILS);
            resultInfo.setResultMsg("Found Invalid details for this EMI plan");
            throw new RequestValidationException(resultInfo);
        }

        Map<String, String> channelInfo = flowRequestBean.getChannelInfo();
        channelInfo.put(TheiaConstant.ChannelInfoKeys.IS_EMI,
                EMIPaymentHelper.isEmi(flowRequestBean.getPaymentTypeId()));

        channelInfo.put(TheiaConstant.ExtendedInfoKeys.ChannelInfoKeys.CARD_HOLDER_NAME,
                TheiaConstant.ExtendedInfoKeys.ChannelInfoDefaultValues.CARD_HOLDER_NAME);
        channelInfo.put(TheiaConstant.ExtendedInfoKeys.ChannelInfoKeys.MOBILE_NO,
                TheiaConstant.ExtendedInfoKeys.ChannelInfoDefaultValues.MOBILE_NO);
        channelInfo.put(TheiaConstant.ExtendedInfoKeys.ChannelInfoKeys.SHIPPING_ADDR_1,
                TheiaConstant.ExtendedInfoKeys.ChannelInfoKeys.SHIPPING_ADDR_1);
        channelInfo.put(TheiaConstant.ExtendedInfoKeys.ChannelInfoKeys.SHIPPING_ADDR_2,
                TheiaConstant.ExtendedInfoKeys.ChannelInfoKeys.SHIPPING_ADDR_2);

        try {

            channelInfo.put(TheiaConstant.ChannelInfoKeys.EMI_PLANID,
                    EMIPaymentHelper.getEmiPlanId(emiDetail.getMonth().toString(), paymentRequestBean.getBankCode()));
            if (emiDetail.getIsSelfBank())
                channelInfo.put(TheiaConstant.ChannelInfoKeys.EMI_ONUS, CardAcquiringMode.OFFUS.name());
            else
                channelInfo.put(TheiaConstant.ChannelInfoKeys.EMI_ONUS, CardAcquiringMode.ONUS.name());

            channelInfo.put(TheiaConstant.ChannelInfoKeys.EMI_MONTHS, emiDetail.getMonth().toString());
            channelInfo.put(TheiaConstant.ChannelInfoKeys.EMI_INTEREST, emiDetail.getInterest().toString());
            LOGGER.debug("Successfully set emi info in channelInfo:{}", channelInfo);

        } catch (NullPointerException e) {
            LOGGER.error("Error while setting EMIsm, mid:{},orderId:{}:", paymentRequestBean.getMid(),
                    paymentRequestBean.getOrderId(), e);
            ResultInfo resultInfo = new ResultInfo();
            resultInfo.setRedirect(false);
            resultInfo.setResultCode(ResponseCodeConstant.SYSTEM_ERROR);
            resultInfo.setResultMsg("Issue with setting EMI details");
            throw new PaymentRequestProcessingException(resultInfo);
        }

    }

}
