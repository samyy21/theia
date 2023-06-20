package com.paytm.pgplus.theia.offline.services;

import com.paytm.pgplus.biz.core.model.request.LitePayviewConsultResponseBizBean;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.theia.models.bin.BinDetailsRequest;
import com.paytm.pgplus.theia.offline.exceptions.BaseException;
import com.paytm.pgplus.theia.offline.model.common.BinData;
import com.paytm.pgplus.theia.offline.model.request.BinDetailRequest;
import com.paytm.pgplus.theia.offline.model.response.BinDetailResponse;
import com.paytm.pgplus.theia.offline.model.response.BinDetailResponseBody;

import javax.servlet.http.HttpServletRequest;

public interface IBinDetailService {

    public BinDetailResponse fetchBinDetails(BinDetailRequest request);

    void validateBinDetails(BinDetailsRequest body);

    void populateSuccessRate(BinData binData, BinDetailResponseBody responseBody, boolean isZeroSR);

    void checkAndAddIDebitOption(HttpServletRequest servletRequest, BinDetailsRequest binDetailsRequest,
            BinDetailResponse response);

    public GenericCoreResponseBean<LitePayviewConsultResponseBizBean> litePayViewConsult(
            HttpServletRequest servletRequest, BinDetailsRequest request);

    String generateResponseForExceptionCases(BinDetailsRequest requestData, BaseException exception);

    BinDetailResponse fetchBinDetailsWithSuccessRateforThirdparty(BinDetailRequest request);
}
