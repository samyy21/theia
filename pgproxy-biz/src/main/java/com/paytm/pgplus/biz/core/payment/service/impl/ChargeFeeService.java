package com.paytm.pgplus.biz.core.payment.service.impl;

import java.util.Map;
import java.util.concurrent.Callable;

import org.slf4j.MDC;
import org.springframework.util.Assert;

import com.paytm.pgplus.biz.core.model.request.ConsultFeeRequest;
import com.paytm.pgplus.biz.core.payment.utils.FeeHelper;
import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.facade.boss.models.request.ChargeFeeConsultRequest;
import com.paytm.pgplus.facade.boss.models.response.ChargeFeeConsultResponse;
import com.paytm.pgplus.facade.boss.services.ICharge;

/*
 * @author Santosh and Agrim
 */
@Deprecated
public class ChargeFeeService implements Callable<ChargeFeeConsultResponse> {

    private EPayMethod payMethod;

    private ICharge chargeImpl;

    private ConsultFeeRequest consultFeeRequest;
    private Map<String, String> mapMdc;

    public ChargeFeeService(EPayMethod payMethod, ConsultFeeRequest consultFeeRequest, ICharge chargeImpl,
            Map<String, String> mdcMap) {
        this.payMethod = payMethod;
        this.consultFeeRequest = consultFeeRequest;
        this.chargeImpl = chargeImpl;
        this.mapMdc = mdcMap;
    }

    public ChargeFeeConsultResponse call() throws Exception {

        MDC.setContextMap(mapMdc);
        final ChargeFeeConsultRequest chargeFeeConsultRequest = FeeHelper.getFeeConsultRequest(consultFeeRequest,
                payMethod);
        final ChargeFeeConsultResponse chargeFeeConsultResponse = chargeImpl.feeConsult(chargeFeeConsultRequest);
        Assert.notNull(chargeFeeConsultResponse, "Charge Fee Consult Response is Null");
        return chargeFeeConsultResponse;
    }

}