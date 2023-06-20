package com.paytm.pgplus.biz.workflow.edc.Service;

import com.paytm.pgplus.biz.exception.EdcLinkBankAndBrandEmiCheckoutException;
import com.paytm.pgplus.biz.workflow.model.*;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;

import java.util.List;
import java.util.Map;

public interface IEdcLinkService {

    GenericCoreResponseBean<BankEmiResponse> getBankEmiResponse(WorkFlowTransactionBean workFlowTransactionBean)
            throws EdcLinkBankAndBrandEmiCheckoutException;

    GenericCoreResponseBean<BrandEmiResponse> getBrandEmiResponse(WorkFlowTransactionBean workFlowTransactionBean)
            throws EdcLinkBankAndBrandEmiCheckoutException;

    GenericCoreResponseBean<OfferCheckoutResponse> getOfferCheckoutResponse(
            WorkFlowTransactionBean workFlowTransactionBean, String bankVerificationCode, String brandVerificationCode,
            List<String> velocityOfferId) throws EdcLinkBankAndBrandEmiCheckoutException;

    GenericCoreResponseBean<ValidateVelocityResponse> validateVelocity(WorkFlowTransactionBean workFlowTransactionBean)
            throws EdcLinkBankAndBrandEmiCheckoutException;
}
