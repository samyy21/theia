package com.paytm.pgplus.biz.taskengine.workflow.impl;

import javax.annotation.PostConstruct;

import com.paytm.pgplus.biz.taskengine.task.AbstractTask;
import com.paytm.pgplus.biz.taskengine.workflow.TaskedWorkflow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("taskedNativeEnhancedFlow")
public class TaskedNativeEnhancedFlow extends TaskedWorkflow {
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
    @Qualifier("createOrderTask")
    private AbstractTask createOrderTask;

    @Autowired
    @Qualifier("createTopupTask")
    private AbstractTask createTopupTask;

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
    @Qualifier("validateUserForAdvanceDepositTask")
    private AbstractTask validateUserForAdvanceDepositTask;

    @Autowired
    @Qualifier("createDynamicQRTask")
    private AbstractTask createDynamicQRTask;

    @Autowired
    @Qualifier("nativeFetchUpiProfile")
    private AbstractTask nativeFetchUpiProfile;

    @Autowired
    @Qualifier("fetchMgvBalanceTask")
    private AbstractTask fetchMgvBalanceTask;

    @Autowired
    @Qualifier("realTimeReconciliationTask")
    private AbstractTask realTimeReconciliationTask;

    @Autowired
    @Qualifier("filterPlatformSavedAssets")
    AbstractTask filterPlatformSavedAssets;

    @Autowired
    @Qualifier("riskPolicyConsultForAddMoney")
    private AbstractTask riskPolicyConsultForAddMoney;

    @Autowired
    @Qualifier("nativeEmiSubventionBulkTask")
    private AbstractTask nativeEmiSubventionBulkTask;

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
    @Qualifier("fetchEmiSubventedBanksTask")
    private AbstractTask fetchEmiSubventedBanksTask;

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
        addTask(1, validateUserForAdvanceDepositTask);

        addTask(2, createTopupTask);
        addTask(2, createOrderTask);
        addTask(2, litePayViewTask);
        addTask(2, fetchSavedCardsNoFilterTask);
        addTask(2, nativefetchVPATask);
        addTask(2, nativeFetchUpiProfile);

        addTask(3, getSavedTokenizedCardsTask);
        addTask(3, getPlatformAndTokenizedCardsTask); // savedCards And
                                                      // TokenCards from
                                                      // SaveCard service

        addTask(4, filterTokenizedAndPlatformSavedCardsTask);
        addTask(4, filterTokenizedAndPlatformCardsForAoaTask);

        addTask(5, addNPaylitePayViewTask);
        addTask(5, fetchWalletBalance);
        addTask(5, fetchMgvBalanceTask);

        addTask(6, getSavedTokenizedCardsForAddNPayTask);
        addTask(6, fetchWorkflowIdWallet2FAWebTask);

        addTask(7, addNPayFilterTokenizedAndPlatformSavedCardsTask);

        addTask(8, payModeDecisionMakerTask);
        addTask(8, filterSavedCardTask);

        addTask(8, createDynamicQRTask);
        addTask(8, filterPlatformSavedAssets);
        addTask(8, fetchSavedCardLimitsTask);
        addTask(8, fetchUserPreferencesTask);

        addTask(9, fetchEmiSubventedBanksTask);
        addTask(9, realTimeReconciliationTask);

        addTask(10, nativeEmiSubventionBulkTask);
    }
}
