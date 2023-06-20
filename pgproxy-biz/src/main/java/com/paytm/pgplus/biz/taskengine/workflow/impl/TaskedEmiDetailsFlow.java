package com.paytm.pgplus.biz.taskengine.workflow.impl;

import com.paytm.pgplus.biz.taskengine.task.AbstractTask;
import com.paytm.pgplus.biz.taskengine.workflow.TaskedWorkflow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service("taskedEmiDetailsFlow")
public class TaskedEmiDetailsFlow extends TaskedWorkflow {

    @Autowired
    @Qualifier("litePayViewConsultTask")
    private AbstractTask litePayViewTask;

    @Autowired
    @Qualifier("fetchMerchantEmiDetailsTask")
    private AbstractTask fetchMerchantEmiDetailsTask;

    @PostConstruct
    public void init() {

        addTask(0, litePayViewTask);
        addTask(0, fetchMerchantEmiDetailsTask);

    }
}
