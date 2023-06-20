package com.paytm.pgplus.theia.datamapper.validator.impl;

import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.springframework.stereotype.Component;

import com.paytm.pgplus.cache.model.EMIDetails;
import com.paytm.pgplus.theia.datamapper.dto.EMIDetailMethodDTO;
import com.paytm.pgplus.theia.datamapper.validator.Validator;
import com.paytm.pgplus.theia.exceptions.TheiaServiceException;

/**
 * 
 * @author ruchikagarg
 *
 */
@Component
public class EMIDetailMethodValidatorImpl implements Validator<EMIDetailMethodDTO> {

    @Override
    public void validate(EMIDetailMethodDTO emiDetailMethodDTO) throws TheiaServiceException {

        if (emiDetailMethodDTO == null || emiDetailMethodDTO.getEmiDetailList() == null) {
            throw new TheiaServiceException(
                    "Exception occured while building EMI data for emi transaction: no record found for mid");
        }
        Map<Long, EMIDetails> emiDetailMap = emiDetailMethodDTO.getEmiDetailList().getEmiDetails();
        if (MapUtils.isEmpty(emiDetailMap)) {
            throw new TheiaServiceException(
                    "Exception occured while building EMI data for emi transaction: no record found for mid");

        }
        EMIDetails emiDetail = emiDetailMap.get(emiDetailMethodDTO.getEmiPlanId());
        if (emiDetail == null)
            throw new TheiaServiceException(
                    "Exception occured while building EMI data for emi transaction: Invalid planId for merchant");
        checkMandatoryParam(emiDetail);

        if (emiDetail.getBankCode() != null && !emiDetail.getBankCode().equals(emiDetailMethodDTO.getBankCode())) {
            throw new TheiaServiceException(
                    "Exception occured while building EMI data for emi transaction: Invalid bankCode for merchant");
        }

    }

    protected void checkMandatoryParam(EMIDetails emiDetail) {
        if (emiDetail.getBankCode() == null || emiDetail.getIsSelfBank() == null || emiDetail.getMonth() == null
                || emiDetail.getInterest() == null)
            throw new TheiaServiceException(
                    "Exception occured while building EMI data for emi transaction: Mandatory Params Missing for merchant, Configuration Issue");

    }

}
