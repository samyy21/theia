package com.paytm.pgplus.biz.taskengine.workflow.impl;

import com.paytm.pgplus.biz.taskengine.task.AbstractTask;
import com.paytm.pgplus.biz.taskengine.task.impl.SubsSpecificPayOptionSavedCardFilter;
import com.paytm.pgplus.biz.taskengine.workflow.TaskedWorkflow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;

@Service("taskedNativeSubscriptionFetchPayOptionsFlow")
public class TaskedNativeSubscriptionFetchPayOptionsFlow extends TaskedWorkflow {

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
    @Qualifier("subsSpecificPayOptionSavedCardFilter")
    private SubsSpecificPayOptionSavedCardFilter subsSpecificPayOptionSavedCardFilter;

    @Autowired
    @Qualifier("nativeFetchUpiProfile")
    private AbstractTask nativeFetchUpiProfile;

    @Autowired
    @Qualifier("fetchMandateBankAndPspTask")
    private AbstractTask fetchMandateBankAndPspTask;

    @Autowired
    @Qualifier("filterPlatformSavedAssetsForSubscriptionTask")
    private AbstractTask filterPlatformSavedAssetsForSubscriptionTask;

    @Autowired
    @Qualifier("realTimeReconciliationTask")
    private AbstractTask realTimeReconciliationTask;

    @Autowired
    @Qualifier("fetchUserPreferencesTask")
    private AbstractTask fetchUserPreferencesTask;

    @Autowired
    @Qualifier("getSavedTokenizedCardsTask")
    private AbstractTask getSavedTokenizedCardsTask;

    @Autowired
    @Qualifier("filterTokenizedAndPlatformSavedCardsTask")
    private AbstractTask filterTokenizedAndPlatformSavedCardsTask;

    @Autowired
    @Qualifier("getPlatformAndTokenizedCardsTask")
    private AbstractTask getPlatformAndTokenizedCardsTask;

    @Autowired
    @Qualifier("filterTokenizedAndPlatformCardsForAoaTask")
    private AbstractTask filterTokenizedAndPlatformCardsForAoaTask;

    @Autowired
    @Qualifier("addNPayFilterTokenizedAndPlatformSavedCardsTask")
    private AbstractTask addNPayFilterTokenizedAndPlatformSavedCardsTask;

    @Autowired
    @Qualifier("getSavedTokenizedCardsForAddNPayTask")
    private AbstractTask getSavedTokenizedCardsForAddNPayTask;

    @Autowired
    @Qualifier("fetchSavedCardLimitsTask")
    private AbstractTask fetchSavedCardLimitsTask;

    @PostConstruct
    public void init() {
        addTask(0, validationTask);
        addTask(0, authTask);

        addTask(1, litePayViewTask);
        addTask(1, fetchSavedCardsNoFilterTask);
        addTask(1, nativeFetchUpiProfile);
        addTask(1, fetchMandateBankAndPspTask);

        // check if any of cc or dc disabled
        addTask(2, getSavedTokenizedCardsTask);
        addTask(2, getPlatformAndTokenizedCardsTask);
        // add new task for add n pay
        // filtering for only cc or dc
        addTask(3, filterTokenizedAndPlatformSavedCardsTask);
        addTask(3, filterTokenizedAndPlatformCardsForAoaTask);

        addTask(4, addNPaylitePayViewTask);
        addTask(4, fetchWalletBalance);

        addTask(5, getSavedTokenizedCardsForAddNPayTask);

        // merge cards for add n pay flow except native addnpay
        addTask(6, addNPayFilterTokenizedAndPlatformSavedCardsTask);
        addTask(6, subsSpecificPayOptionSavedCardFilter);
        addTask(6, payModeDecisionMakerTask);

        addTask(7, fetchSavedCardLimitsTask);
        addTask(7, filterSavedCardTask);
        addTask(7, filterPlatformSavedAssetsForSubscriptionTask);

        addTask(8, fetchUserPreferencesTask);
        addTask(8, realTimeReconciliationTask);
    }
}
