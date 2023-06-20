package com.paytm.pgplus.theia.services.impl;

import com.paytm.pgplus.enums.EChannelId;
import com.paytm.pgplus.response.ResponseHeader;
import com.paytm.pgplus.theia.nativ.model.validateCard.ValidateCardResponse;
import com.paytm.pgplus.theia.nativ.model.validateCard.ValidateCardResponseBody;
import com.paytm.pgplus.theia.offline.model.common.BinData;
import com.paytm.pgplus.theia.offline.model.request.BinDetailRequest;
import com.paytm.pgplus.theia.offline.model.request.BinDetailRequestBody;
import com.paytm.pgplus.theia.offline.model.response.BinDetailResponse;
import com.paytm.pgplus.theia.offline.services.impl.BinDetailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("cardValidationHelper")
public class CardValidationHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(CardValidationHelper.class);
    private static final String PERFORMANCE_STATUS_GOOD = "GOOD";
    private static final String PERFORMANCE_STATUS_ZERO = "ZERO";
    private static final String PERFORMANCE_STATUS_POOR = "POOR";
    @Autowired
    @Qualifier("binDetailService")
    private BinDetailService binDetailService;

    public ValidateCardResponse prepareValidateCardResponse(BinDetailResponse binDetailResponse) {
        boolean isZeroSR = false;
        BinData binDetail = binDetailResponse.getBody().getBinDetail();
        ValidateCardResponse validateCardResponse = new ValidateCardResponse();
        validateCardResponse.setHead(new ResponseHeader());
        ValidateCardResponseBody validateCardResponseBody = new ValidateCardResponseBody();
        validateCardResponseBody.setBankName(binDetail.getIssuingBank());
        validateCardResponseBody.setAuthModes(binDetailResponse.getBody().getAuthModes());
        validateCardResponseBody.setIconUrl(binDetailResponse.getBody().getIconUrl());
        validateCardResponseBody.setErrorMessage(binDetailResponse.getBody().getErrorMsg());
        validateCardResponseBody.setCardScheme(binDetail.getChannelCode());
        if (binDetail.getIsIndian().equals("false") || binDetailService.isIsException() == true) {
            validateCardResponseBody.setPerformanceStatus(PERFORMANCE_STATUS_ZERO);
        } else if (binDetailResponse.getBody().getHasLowSuccessRate().getMsg()
                .contains("We are observing high failures on")
                || ("true".equals(binDetailResponse.getBody().getHasLowSuccessRate().getStatus()))) {
            validateCardResponseBody.setPerformanceStatus(PERFORMANCE_STATUS_POOR);
        } else if ("false".equals(binDetailResponse.getBody().getHasLowSuccessRate().getStatus())) {
            binDetailResponse.getBody().getHasLowSuccessRate().setMsg("Card has a fair success rate");
            validateCardResponseBody.setPerformanceStatus(PERFORMANCE_STATUS_GOOD);
        }
        validateCardResponse.setBody(validateCardResponseBody);
        return validateCardResponse;
    }

    public BinDetailRequest prepareBinDetailsRequest(String bin, EChannelId channelId) {
        BinDetailRequestBody body = new BinDetailRequestBody();
        body.setBin(bin);
        body.setChannelId(channelId);
        BinDetailRequest request = new BinDetailRequest();
        request.setBody(body);
        return request;
    }
}
