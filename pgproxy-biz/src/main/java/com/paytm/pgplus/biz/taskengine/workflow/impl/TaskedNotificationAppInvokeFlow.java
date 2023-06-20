package com.paytm.pgplus.biz.taskengine.workflow.impl;

import com.paytm.pgplus.biz.taskengine.task.AbstractTask;
import com.paytm.pgplus.biz.taskengine.workflow.TaskedWorkflow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service("taskedNotificationAppInvokeFlow")
public class TaskedNotificationAppInvokeFlow extends TaskedWorkflow {

    @Autowired
    @Qualifier("sendSMSAppInvokeTask")
    private AbstractTask sendSMSAppInvokeTask;

    @Autowired
    @Qualifier("sendPushNotificationAppInvokeTask")
    private AbstractTask sendPushNotificationAppInvokeTask;

    @Autowired
    @Qualifier("urlShortenTask")
    private AbstractTask urlShortenTask;

    @PostConstruct
    public void init() {

        addTask(0, urlShortenTask);
        addTask(1, sendSMSAppInvokeTask);
        addTask(1, sendPushNotificationAppInvokeTask);

    }
}
