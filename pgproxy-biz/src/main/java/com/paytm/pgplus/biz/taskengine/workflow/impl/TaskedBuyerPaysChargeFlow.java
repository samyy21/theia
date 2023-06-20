package com.paytm.pgplus.biz.taskengine.workflow.impl;

import com.paytm.pgplus.biz.taskengine.task.AbstractTask;
import com.paytm.pgplus.biz.taskengine.workflow.TaskedWorkflow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * this service defines task on each level for post convience parallelization flow
 */

/**
 * Created by charu on 25/07/18.
 */

@Service("buyerPaysChargeTaskFlow")
public class TaskedBuyerPaysChargeFlow extends TaskedWorkflow {
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
    @Qualifier("fetchSavedCardTask")
    private AbstractTask fetchSavedCards;

    @Autowired
    @Qualifier("addnPayLitePayViewConsultTask")
    private AbstractTask addNPaylitePayViewTask;

    @Autowired
    @Qualifier("payModeDecisionMakerTask")
    private AbstractTask payModeDecisionMakerTask;

    @Autowired()
    @Qualifier("consultFeeTask")
    private AbstractTask consultFeeTask;

    @Autowired
    @Qualifier("authUserTypeAttributeTask")
    private AbstractTask authUserTypeAttributeTask;

    @Autowired
    @Qualifier("createDynamicQRTask")
    private AbstractTask createDynamicQRTask;

    @Autowired
    @Qualifier("fetchWalletBalanceTask")
    private AbstractTask fetchWalletBalance;

    @PostConstruct
    public void init() {
        addTask(0, validationTask);
        addTask(0, authTask);

        addTask(1, authUserTypeAttributeTask);

        addTask(2, litePayViewTask);

        addTask(3, addNPaylitePayViewTask);
        addTask(3, fetchWalletBalance);

        addTask(4, payModeDecisionMakerTask);

        addTask(5, fetchSavedCards);
        addTask(5, createOrderTask);
        addTask(5, consultFeeTask);

        addTask(6, createDynamicQRTask);
    }
}
