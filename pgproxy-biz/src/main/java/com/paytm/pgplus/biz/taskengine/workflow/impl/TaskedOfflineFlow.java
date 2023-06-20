package com.paytm.pgplus.biz.taskengine.workflow.impl;

import com.paytm.pgplus.biz.taskengine.task.AbstractTask;
import com.paytm.pgplus.biz.taskengine.workflow.TaskedWorkflow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * this service defines task on each level for offline flow
 */

/**
 * Created by charu on 16/07/18.
 */

@Service("offlineFlow")
public class TaskedOfflineFlow extends TaskedWorkflow {
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
    @Qualifier("fetchPaytmVPATask")
    private AbstractTask fetchPaytmVPATask;

    @Autowired
    @Qualifier("authUserTypeAttributeTask")
    private AbstractTask authUserTypeAttributeTask;

    @Autowired
    @Qualifier("fetchPostpaidBalanceTask")
    private AbstractTask fetchPostPaidBalanceTask;

    @Autowired
    @Qualifier("realTimeReconciliationTask")
    private AbstractTask realTimeReconciliationTask;

    @Autowired
    @Qualifier("filterPlatformSavedAssets")
    AbstractTask filterPlatformSavedAssets;

    @PostConstruct
    public void init() {
        addTask(0, validationTask);
        addTask(0, authTask);
        addTask(1, authUserTypeAttributeTask);
        addTask(2, litePayViewTask);
        addTask(2, fetchSavedCardsNoFilterTask);
        addTask(2, fetchPaytmVPATask);
        addTask(3, addNPaylitePayViewTask);
        addTask(3, fetchWalletBalance);
        addTask(3, fetchPostPaidBalanceTask);
        addTask(4, payModeDecisionMakerTask);
        addTask(4, filterSavedCardTask);
        addTask(4, filterPlatformSavedAssets);
        addTask(5, realTimeReconciliationTask);
    }
}
