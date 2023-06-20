package com.paytm.pgplus.biz.taskengine.workflow.impl;

import com.paytm.pgplus.biz.taskengine.task.AbstractTask;
import com.paytm.pgplus.biz.taskengine.workflow.TaskedWorkflow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service("taskedSubscriptionFlow")
public class TaskedSubscriptionFlow extends TaskedWorkflow {

    @Autowired
    @Qualifier("validationTask")
    private AbstractTask validationTask;

    @Autowired
    @Qualifier("authUserTask")
    private AbstractTask authTask;

    @Autowired
    @Qualifier("fetchSavedCardTask")
    private AbstractTask fetchSavedCardTask;

    @Autowired
    @Qualifier("createOrderTask")
    private AbstractTask createOrderTask;

    @Autowired
    @Qualifier("payModeDecisionMakerTask")
    private AbstractTask payModeDecisionMakerTask;

    @Autowired
    @Qualifier("freshSubscriptionContractTask")
    private AbstractTask freshSubscriptionContractTask;

    @Autowired
    @Qualifier("litePayViewConsultTask")
    private AbstractTask litePayViewTask;

    @Autowired
    @Qualifier("addnPayLitePayViewConsultTask")
    private AbstractTask addnPayLitePayViewConsultTask;

    @Autowired
    @Qualifier("paymentBankBalanceTask")
    private AbstractTask paymentBankBalanceTask;

    @Autowired
    @Qualifier("channelAccountQueryTask")
    private AbstractTask channelAccountQueryTask;

    @Autowired
    @Qualifier("authUserTypeAttributeTask")
    private AbstractTask authUserTypeAttributeTask;

    @Autowired
    @Qualifier("fetchUserPreferencesTask")
    private AbstractTask fetchUserPreferencesTask;

    @PostConstruct
    public void init() {

        addTask(0, validationTask);
        addTask(0, authTask);

        addTask(1, authUserTypeAttributeTask);

        addTask(2, litePayViewTask);

        addTask(3, addnPayLitePayViewConsultTask);
        addTask(3, channelAccountQueryTask);

        addTask(4, payModeDecisionMakerTask);

        addTask(5, paymentBankBalanceTask);
        addTask(5, fetchSavedCardTask);
        addTask(5, createOrderTask);
        addTask(5, freshSubscriptionContractTask);
        addTask(5, fetchUserPreferencesTask);
    }

}