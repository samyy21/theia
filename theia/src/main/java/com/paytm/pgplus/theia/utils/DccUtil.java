package com.paytm.pgplus.theia.utils;

import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.common.model.DccPaymentDetail;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.nativ.model.PCF.PCFFeeCharges;
import com.paytm.pgplus.theia.nativ.model.common.DccPaymentDetailRequest;
import com.paytm.pgplus.theia.nativ.processor.IRequestProcessor;
import com.paytm.pgplus.theia.nativ.processor.factory.RequestProcessorFactory;
import com.paytm.pgplus.theiacommon.utils.AmountUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import static com.paytm.pgplus.theia.constants.TheiaConstant.DccConstants.NA;
import static com.paytm.pgplus.theia.constants.TheiaConstant.DccConstants.THEIA;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestHeaders.Version_V1;

@Service("dccUtil")
public class DccUtil {

    @Autowired
    private RequestProcessorFactory requestProcessorFactory;

    @Autowired
    @Qualifier("processTransactionUtil")
    private ProcessTransactionUtil processTransactionUtil;

    public static final Logger LOGGER = LoggerFactory.getLogger(DccUtil.class);

    @Autowired
    private IMerchantPreferenceService merchantPreferenceService;

    public DccPaymentDetail fetchDccRatesFromInsta(PaymentRequestBean requestData, WorkFlowRequestBean flowRequestBean)
            throws Exception {

        DccPaymentDetailRequest dccPaymentDetailRequest = createDccPaymentRequest(requestData, flowRequestBean);
        // call to insta
        IRequestProcessor<DccPaymentDetailRequest, DccPaymentDetail> dccFetchRatesRequestProcessor = requestProcessorFactory
                .getRequestProcessor(RequestProcessorFactory.RequestType.FETCH_DCC_RATES);
        DccPaymentDetail dccPaymentDetail = null;

        dccPaymentDetail = dccFetchRatesRequestProcessor.process(dccPaymentDetailRequest);
        return dccPaymentDetail;

    }

    private DccPaymentDetailRequest createDccPaymentRequest(PaymentRequestBean requestData,
            WorkFlowRequestBean flowRequestBean) throws Exception {
        DccPaymentDetailRequest dccPaymentDetailRequest = new DccPaymentDetailRequest();
        dccPaymentDetailRequest.setVersion(Version_V1);
        dccPaymentDetailRequest.setRequestTimeStamp(Long.toString(System.currentTimeMillis()));
        dccPaymentDetailRequest.setAmount(AmountUtils.formatNumberToTwoDecimalPlaces(requestData.getTxnAmount()));
        dccPaymentDetailRequest.setClient(THEIA);
        dccPaymentDetailRequest.setBankCode(flowRequestBean.getDccServiceInstId());
        dccPaymentDetailRequest.setMid(requestData.getMid());
        dccPaymentDetailRequest.setOrderId(requestData.getOrderId());
        dccPaymentDetailRequest.setBin(flowRequestBean.getCardNo().substring(0, 6));
        dccPaymentDetailRequest.setExtendedInfo(NA);
        dccPaymentDetailRequest.setPayMode(requestData.getPaymentTypeId());

        if (merchantPreferenceService.isPostConvenienceFeesEnabled(flowRequestBean.getPaytmMID())) {
            // LOGGER.info("Calling P+ API for fetching PCF Charges");
            PCFFeeCharges pcfFeeCharges = processTransactionUtil.getPCFFeecharges(flowRequestBean.getTxnToken(),
                    EPayMethod.getPayMethodByMethod(requestData.getPaymentTypeId()), null, null);
            LOGGER.info("PCFFeeCharges : " + pcfFeeCharges);
            dccPaymentDetailRequest.setPcfAmount(AmountUtils.formatNumberToTwoDecimalPlaces(String.valueOf(Double
                    .parseDouble(pcfFeeCharges.getFeeAmount().getValue())
                    + Double.parseDouble(pcfFeeCharges.getTaxAmount().getValue()))));
        }
        return dccPaymentDetailRequest;
    }
}
