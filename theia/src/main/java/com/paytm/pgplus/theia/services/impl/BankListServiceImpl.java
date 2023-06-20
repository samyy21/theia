package com.paytm.pgplus.theia.services.impl;

import com.paytm.pgplus.biz.core.model.request.LitePayviewConsultRequestBizBean;
import com.paytm.pgplus.biz.core.model.request.LitePayviewConsultResponseBizBean;
import com.paytm.pgplus.biz.core.model.request.PayChannelOptionViewBiz;
import com.paytm.pgplus.biz.core.model.request.PayMethodViewsBiz;
import com.paytm.pgplus.biz.core.payment.service.IBizPaymentService;
import com.paytm.pgplus.biz.mapping.models.MappingMerchantData;
import com.paytm.pgplus.common.enums.PayMethod;
import com.paytm.pgplus.common.model.EnvInfoRequestBean;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.theia.cache.IMerchantMappingService;
import com.paytm.pgplus.theia.exceptions.PaymentRequestValidationException;
import com.paytm.pgplus.theia.models.BankListRequest;
import com.paytm.pgplus.theia.viewmodel.BankInfo;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

import static com.paytm.pgplus.common.enums.ERequestType.SEAMLESS_NB;

@Service("bankListService")
public class BankListServiceImpl {

    private static final List<String> excludedPayMethodList = new ArrayList<>();
    @Autowired
    @Qualifier("bizPaymentService")
    private IBizPaymentService bizPaymentService;
    @Autowired
    @Qualifier("merchantMappingService")
    private IMerchantMappingService merchantMappingService;

    @PostConstruct
    public void initExcludedPayMethodList() {
        for (PayMethod payMethod : PayMethod.values()) {
            String method = payMethod.getMethod();
            if (method != null && !payMethod.equals(PayMethod.NET_BANKING) && !payMethod.equals(PayMethod.ATM)) {
                excludedPayMethodList.add(method);
            }
        }
    }

    public List<BankInfo> fetchAvailableBankList(BankListRequest bankListRequest, EnvInfoRequestBean envInfoRequestBean) {

        List<BankInfo> bankInfoList = new ArrayList<>();

        String aliPayMid = fetchAlipayMID(bankListRequest.getMid());

        LitePayviewConsultRequestBizBean litePayviewConsultRequestBizBean = new LitePayviewConsultRequestBizBean(null,
                envInfoRequestBean, SEAMLESS_NB, null, aliPayMid, false, excludedPayMethodList, null, null, false);

        GenericCoreResponseBean<LitePayviewConsultResponseBizBean> responseBean = bizPaymentService
                .litePayviewConsult(litePayviewConsultRequestBizBean);

        if (responseBean.isSuccessfullyProcessed()) {

            bankInfoList.addAll(filterPayMethods(responseBean.getResponse(), PayMethod.NET_BANKING.getMethod()));
            bankInfoList.addAll(filterPayMethods(responseBean.getResponse(), PayMethod.ATM.getMethod()));
        }

        return bankInfoList;
    }

    private BankInfo createBankInfo(String bankCode, String bankName, boolean atmFlag) {

        BankInfo bankInfo = new BankInfo();
        bankInfo.setBankName(bankCode);
        bankInfo.setDisplayName(bankName);
        bankInfo.setIsAtm(BooleanUtils.toInteger(atmFlag));
        return bankInfo;
    }

    private String fetchAlipayMID(String mid) {

        final GenericCoreResponseBean<MappingMerchantData> merchantMappingResponse = merchantMappingService
                .fetchMerchanData(mid == null ? "" : mid);

        if ((merchantMappingResponse != null) && (merchantMappingResponse.getResponse() != null)) {
            return merchantMappingResponse.getResponse().getAlipayId();
        } else {
            final String error = merchantMappingResponse == null ? "Could not map merchant" : merchantMappingResponse
                    .getFailureMessage();
            throw new PaymentRequestValidationException(error, ResponseConstants.INVALID_MID);
        }
    }

    private List<BankInfo> filterPayMethods(LitePayviewConsultResponseBizBean litePayviewConsultResponseBizBean,
            String payMethod) {

        List<BankInfo> bankInfoList = new ArrayList<>();

        if (litePayviewConsultResponseBizBean == null || payMethod == null)
            return bankInfoList;

        for (PayMethodViewsBiz payMethodViewsBiz : litePayviewConsultResponseBizBean.getPayMethodViews()) {

            if (payMethodViewsBiz.getPayMethod().equals(payMethod)) {
                for (PayChannelOptionViewBiz payChannelOptionViewBiz : payMethodViewsBiz.getPayChannelOptionViews()) {
                    if (payChannelOptionViewBiz.isEnableStatus()) {
                        bankInfoList.add(createBankInfo(payChannelOptionViewBiz.getInstId(),
                                payChannelOptionViewBiz.getInstName(),
                                PayMethod.ATM.getMethod().equalsIgnoreCase(payMethod)));
                    }
                }
            }
        }

        return bankInfoList;

    }

}