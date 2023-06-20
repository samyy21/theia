package com.paytm.pgplus.biz.taskengine.workflow.impl;

import com.paytm.pgplus.biz.taskengine.task.AbstractTask;
import com.paytm.pgplus.biz.taskengine.workflow.TaskedWorkflow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * Created by charu on 21/06/18.
 */

@Service("taskedNativeFetchPayOptionsFlow")
public class TaskedNativeFetchPayOptionsFlow extends TaskedWorkflow {

    @Autowired
    @Qualifier("validationTask")
    private AbstractTask validationTask;

    @Autowired
    @Qualifier("authUserTask")
    private AbstractTask authTask;

    @Autowired
    @Qualifier("litePayViewConsultTask")
    private AbstractTask litePayViewTask;

    @Autowired
    @Qualifier("fetchSavedCardNoFilterTask")
    private AbstractTask fetchSavedCardsNoFilterTask;

    @Autowired
    @Qualifier("filterSavedCardTask")
    private AbstractTask filterSavedCardTask;

    @Autowired
    @Qualifier("addnPayLitePayViewConsultTask")
    private AbstractTask addNPaylitePayViewTask;

    @Autowired
    @Qualifier("payModeDecisionMakerTask")
    private AbstractTask payModeDecisionMakerTask;

    @Autowired
    @Qualifier("fetchWalletBalanceTask")
    private AbstractTask fetchWalletBalance;

    @Autowired
    @Qualifier("nativeFetchVPATask")
    private AbstractTask nativefetchVPATask;

    @Autowired
    @Qualifier("authUserTypeAttributeTask")
    private AbstractTask authUserTypeAttributeTask;

    @Autowired
    @Qualifier("riskPolicyConsultForAddMoney")
    private AbstractTask riskPolicyConsultForAddMoney;

    @Autowired
    @Qualifier("fetchMgvBalanceTask")
    private AbstractTask fetchMgvBalanceTask;

    @Autowired
    @Qualifier("totalMerchantLimitTask")
    private AbstractTask totalMerchantLimitTask;

    @Autowired
    @Qualifier("accumulatedMerchantLimitTask")
    private AbstractTask accumulatedMerchantLimitTask;
    @Autowired
    @Qualifier("fetchEmiSubventedBanksTask")
    private AbstractTask fetchEmiSubventedBanksTask;

    @Autowired
    @Qualifier("nativeFetchCardIndexBulkTask")
    private AbstractTask nativeFetchCardIndexBulkTask;

    @Autowired
    @Qualifier("nativeEmiSubventionBulkTask")
    private AbstractTask nativeEmiSubventionBulkTask;

    @Autowired
    @Qualifier("nativeFetchUpiProfile")
    private AbstractTask nativeFetchUpiProfile;

    @Autowired
    @Qualifier("fetchChannelDetailsTask")
    private AbstractTask fetchChannelDetailsTask;

    @Autowired
    @Qualifier("realTimeReconciliationTask")
    private AbstractTask realTimeReconciliationTask;

    @Autowired
    @Qualifier("filterPlatformSavedAssets")
    AbstractTask filterPlatformSavedAssets;

    @Autowired
    @Qualifier("merchantPaymodesLimitTask")
    AbstractTask merchantPaymodesLimitTask;

    @Autowired
    @Qualifier("createDynamicQRTask")
    private AbstractTask createDynamicQRTask;

    @Autowired
    @Qualifier("fetchPostpaidBalanceTask")
    private AbstractTask fetchPostpaidBalanceTask;

    @Autowired
    @Qualifier("fetchPPBLBalanceTask")
    private AbstractTask fetchPPBLBalanceTask;

    @Autowired
    @Qualifier("getSavedTokenizedCardsTask")
    private AbstractTask getSavedTokenizedCardsTask;

    @Autowired
    @Qualifier("filterTokenizedAndPlatformSavedCardsTask")
    private AbstractTask filterTokenizedAndPlatformSavedCardsTask;

    @Autowired
    @Qualifier("addNPayFilterTokenizedAndPlatformSavedCardsTask")
    private AbstractTask addNPayFilterTokenizedAndPlatformSavedCardsTask;

    @Autowired
    @Qualifier("fetchUserPreferencesTask")
    private AbstractTask fetchUserPreferencesTask;

    @Autowired
    @Qualifier("getPlatformAndTokenizedCardsTask")
    private AbstractTask getPlatformAndTokenizedCardsTask;

    @Autowired
    @Qualifier("filterTokenizedAndPlatformCardsForAoaTask")
    private AbstractTask filterTokenizedAndPlatformCardsForAoaTask;

    @Autowired
    @Qualifier("getSavedTokenizedCardsForAddNPayTask")
    private AbstractTask getSavedTokenizedCardsForAddNPayTask;

    @Autowired
    @Qualifier("fetchLimitConsumptionTask")
    private AbstractTask fetchLimitConsumptionTask;

    @Autowired
    @Qualifier("fetchSavedCardLimitsTask")
    private AbstractTask fetchSavedCardLimitsTask;

    @Autowired
    @Qualifier("fetchWorkflowIdWallet2FATask")
    private AbstractTask fetchWorkflowIdWallet2FAWebTask;

    @PostConstruct
    public void init() {
        addTask(0, validationTask);
        addTask(0, authTask);

        addTask(1, authUserTypeAttributeTask);

        addTask(2, litePayViewTask);
        addTask(2, fetchSavedCardsNoFilterTask);
        addTask(2, riskPolicyConsultForAddMoney);
        addTask(2, totalMerchantLimitTask);
        addTask(2, nativefetchVPATask);
        addTask(2, nativeFetchUpiProfile);
        addTask(2, fetchChannelDetailsTask);
        // check if any of cc or dc disabled
        addTask(3, getSavedTokenizedCardsTask);
        addTask(3, getPlatformAndTokenizedCardsTask);
        // fetch limit consumption details for pg2 fully migrated merchants
        addTask(3, fetchLimitConsumptionTask);
        // add new task for add n pay
        // filtering for only cc or dc
        addTask(4, filterTokenizedAndPlatformSavedCardsTask);
        addTask(4, filterTokenizedAndPlatformCardsForAoaTask);

        addTask(5, addNPaylitePayViewTask);
        addTask(5, fetchWalletBalance);
        addTask(5, fetchMgvBalanceTask);
        addTask(5, accumulatedMerchantLimitTask);
        addTask(5, merchantPaymodesLimitTask);

        addTask(6, getSavedTokenizedCardsForAddNPayTask);
        addTask(6, fetchWorkflowIdWallet2FAWebTask);

        // merge cards for add n pay flow except native addnpay
        addTask(7, addNPayFilterTokenizedAndPlatformSavedCardsTask);
        addTask(7, fetchPostpaidBalanceTask);
        addTask(7, fetchPPBLBalanceTask);

        addTask(8, payModeDecisionMakerTask);
        addTask(8, filterSavedCardTask);
        addTask(8, createDynamicQRTask);
        addTask(8, fetchSavedCardLimitsTask);
        addTask(8, filterPlatformSavedAssets);

        addTask(9, nativeFetchCardIndexBulkTask);
        addTask(9, fetchEmiSubventedBanksTask);
        addTask(9, realTimeReconciliationTask);
        addTask(9, fetchUserPreferencesTask);

        addTask(10, nativeEmiSubventionBulkTask);
    }
}
