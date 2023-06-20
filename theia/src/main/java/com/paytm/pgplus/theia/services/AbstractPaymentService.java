/**
 * 
 */
package com.paytm.pgplus.theia.services;

import com.paytm.pgplus.common.enums.EventNameEnum;
import com.paytm.pgplus.common.enums.TxnState;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.theia.datamapper.IBizRequestResponseMapper;
import com.paytm.pgplus.theia.utils.ChecksumService;
import com.paytm.pgplus.theia.utils.EventUtils;
import com.paytm.pgplus.transactionlogger.annotation.Loggable;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.paytm.pgplus.theia.datamapper.IBizRequestResponseMapper;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.theia.utils.ChecksumService;

/**
 * @createdOn 04-Apr-2016
 * @author kesari
 */
public abstract class AbstractPaymentService implements IPaymentService {

    /**
	 * 
	 */
    private static final long serialVersionUID = 3162244150111425243L;

    @Autowired
    @Qualifier("theiaSessionDataService")
    protected ITheiaSessionDataService theiaSessionDataService;

    @Autowired
    @Qualifier("bizService")
    protected IBizService bizService;

    @Autowired
    @Qualifier("bizRequestResponseMapper")
    protected IBizRequestResponseMapper bizRequestResponseMapper;

    @Autowired
    protected ChecksumService checksumService;

    @Loggable(logLevel = Loggable.INFO, state = TxnState.CHECKSUM_VALIDATION)
    protected boolean validateChecksum(PaymentRequestBean requestData) {
        boolean isChecksumValid = checksumService.validateChecksum(requestData);
        if (!isChecksumValid) {
            EventUtils.pushTheiaEvents(EventNameEnum.CHECKSUM_VALIDATION_FAILED, new ImmutablePair<>("REQUEST_TYPE",
                    requestData.getRequestType()));
        }
        return isChecksumValid;
    }
}