package com.paytm.pgplus.biz.taskengine.workflow.impl;

import com.paytm.pgplus.biz.taskengine.task.AbstractTask;
import com.paytm.pgplus.biz.taskengine.workflow.TaskedWorkflow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service("defaultFlow")
public class TaskedDefaultFlow extends TaskedWorkflow {

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
    @Qualifier("createOrderTask")
    private AbstractTask createOrderTask;

    @Autowired
    @Qualifier("paymentBankBalanceTask")
    private AbstractTask paymentBankBalanceTask;

    @Autowired
    @Qualifier("fetchVPATask")
    private AbstractTask fetchVpaTask;

    @Autowired
    @Qualifier("fetchSavedCardTask")
    private AbstractTask fetchSavedCards;

    @Autowired
    @Qualifier("addnPayLitePayViewConsultTask")
    private AbstractTask addNPaylitePayViewTask;

    @Autowired
    @Qualifier("payModeDecisionMakerTask")
    private AbstractTask payModeDecisionMakerTask;

    @Autowired
    @Qualifier("channelAccountQueryTask")
    private AbstractTask channelAccountQuery;

    @Autowired
    @Qualifier("authUserTypeAttributeTask")
    private AbstractTask authUserTypeAttributeTask;

    @PostConstruct
    public void init() {
        addTask(0, validationTask);
        addTask(0, authTask);

        addTask(1, authUserTypeAttributeTask);

        addTask(2, litePayViewTask);

        addTask(3, addNPaylitePayViewTask);
        addTask(3, channelAccountQuery);

        addTask(4, payModeDecisionMakerTask);

        addTask(5, fetchVpaTask);
        addTask(5, paymentBankBalanceTask);
        addTask(5, fetchSavedCards);
        addTask(5, createOrderTask);
    }

}
