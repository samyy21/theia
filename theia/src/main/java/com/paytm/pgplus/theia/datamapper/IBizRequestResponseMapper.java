/**
 *
 */
package com.paytm.pgplus.theia.datamapper;

import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.cashier.cachecard.model.SavedCardRequest;
import com.paytm.pgplus.theia.exceptions.TheiaDataMappingException;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;

/**
 * @createdOn 04-Apr-2016
 * @author kesari
 */
public interface IBizRequestResponseMapper {

    WorkFlowRequestBean mapWorkFlowRequestData(PaymentRequestBean requestData) throws TheiaDataMappingException;

    void mapWorkFlowResponseToSession(PaymentRequestBean requestData, WorkFlowResponseBean responseData);

    void mapPostLoginResponseToSession(PaymentRequestBean requestData, WorkFlowResponseBean responseData);

    void mapWorkFlowResponseToSession(PaymentRequestBean requestData, WorkFlowResponseBean responseData,
            SavedCardRequest savedCardRequest);
}
