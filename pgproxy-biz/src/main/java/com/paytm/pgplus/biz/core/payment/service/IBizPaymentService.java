/*
 * This File is  the sole property of Paytm(One97 Communications Limited)
 */
package com.paytm.pgplus.biz.core.payment.service;

import com.paytm.pgplus.biz.core.model.oauth.BizWalletConsultResponse;
import com.paytm.pgplus.biz.core.model.request.*;
import com.paytm.pgplus.common.enums.TxnState;
import com.paytm.pgplus.facade.common.model.BankFormOptimizationParams;
import com.paytm.pgplus.facade.fund.models.request.ConsultWalletLimitsRequest;
import com.paytm.pgplus.facade.payment.models.request.FetchCardLimitsRequest;
import com.paytm.pgplus.facade.payment.models.response.FetchCardLimitsResponse;
import com.paytm.pgplus.facade.payment.models.response.UPIPushInitiateResponse;
import com.paytm.pgplus.facade.wallet.models.WalletBalanceRequest;
import com.paytm.pgplus.facade.wallet.models.WalletBalanceResponse;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.pgproxycommon.models.QueryPaymentStatus;
import com.paytm.pgplus.transactionlogger.annotation.Loggable;

/**
 * @author amitdubey
 * @date Jan 16, 2017
 */
public interface IBizPaymentService {

    GenericCoreResponseBean<ConsultPayViewResponseBizBean> consultPayView(ConsultPayViewRequestBizBean consutRequestBean);

    GenericCoreResponseBean<LitePayviewConsultResponseBizBean> litePayviewConsult(
            LitePayviewConsultRequestBizBean consutRequestBean);

    GenericCoreResponseBean<LitePayviewConsultResponseBizBean> litePayviewPayMethodConsult(
            LitePayviewConsultRequestBizBean consutRequestBean);

    GenericCoreResponseBean<TokenizedCardsResponseBizBean> fetchTokenizedCards(
            TokenizedCardsRequestBizBean tokenizedCardsRequestBean);

    @Loggable(logLevel = Loggable.INFO, state = TxnState.CASHIER_QUERY_FOR_PAYMENT_STATUS)
    GenericCoreResponseBean<QueryPaymentStatus> queryPayResultForPaymentStatusInOneCall(
            QueryPayResultRequestBean queryPayResultRequestBean);

    boolean walletLimitsConsult(ConsultWalletLimitsRequest walletConsultRequestBean);

    BizWalletConsultResponse walletLimitsConsultV2(ConsultWalletLimitsRequest walletConsultRequestBean);

    @Deprecated
    GenericCoreResponseBean<ConsultFeeResponse> consultConvenienceFees(ConsultFeeRequest consultFeeRequest);

    GenericCoreResponseBean<ConsultFeeResponse> consultBulkConvenienceFees(ConsultFeeRequest consultFeeRequest);

    GenericCoreResponseBean<QueryPaymentStatus> queryPayResultForBankForm(
            QueryPayResultRequestBean queryPayResultRequestBean, String modifiedLooperTimeout,
            BankFormOptimizationParams bankFormOptimizationParams);

    GenericCoreResponseBean<QueryPaymentStatus> queryPayResultForPaymentStatus(
            QueryPayResultRequestBean queryPayResultRequestBean);

    GenericCoreResponseBean<BizPayResponse> pay(BizPayRequest bizPayRequest);

    GenericCoreResponseBean<BizPayResponse> aoaPay(BizAoaPayRequest bizAoaPayRequest);

    GenericCoreResponseBean<UPIPushInitiateResponse> initiateUpiPushTransaction(
            final UPIPushInitiateRequestBean initiateRequestBean);

    GenericCoreResponseBean<ChannelAccountQueryResponseBizBean> channelAccountQuery(
            ChannelAccountQueryRequestBizBean channelAccountQueryRequest);

    WalletBalanceResponse fetchWalletBalance(WalletBalanceRequest fetchWalletBalanceRequest, String userId);

    FetchCardLimitsResponse fetchCardLimit(FetchCardLimitsRequest fetchCardLimitsRequest);

}
