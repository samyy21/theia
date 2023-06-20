package com.paytm.pgplus.biz.taskengine.taskexecutor;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.paytm.pgplus.biz.taskengine.task.AbstractTask;
import com.paytm.pgplus.biz.taskengine.workflow.TaskedWorkflow;
import com.paytm.pgplus.biz.taskengine.workflow.WorkflowFactory;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.common.util.ThreadLocalUtil;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.paytm.pgplus.logging.ExtendedLogger;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;

/**
 * Base engine controlling all the workflow tasks in parallelistic manner
 * 
 * @author ankit.singhal
 * @version $Id: RegistryHelper.java, v 0.1 2018-06-13 10:38 AM ankit.singhal
 *          Exp $$
 */
@Service("taskExecutor")
public class TaskExecutor {

    public static final Logger LOGGER = LoggerFactory.getLogger(TaskExecutor.class);
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(TaskExecutor.class);

    @Autowired
    @Qualifier("workFlowFactory")
    private WorkflowFactory workflowFactory;

    /**
     * Main thread executor
     */
    private final ExecutorService executor = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat(
            "TaskExecutor-thread-%d").build());

    /**
     * Template execution method for overall execution
     * 
     * @param input
     *            Core input bean
     * @return final response bean
     */
    public GenericCoreResponseBean<WorkFlowResponseBean> execute(final WorkFlowRequestBean input) {

        // Start main execution
        try {
            // Execute the business
            return doExecute(input);

        } finally {
        }
    }

    /**
     * Core business execution
     * 
     * @param input
     *            Business input request
     * @return Reposne for overall engine
     */
    private GenericCoreResponseBean<WorkFlowResponseBean> doExecute(final WorkFlowRequestBean input) {
        // For shadow handling, if the ThreadLocal contains ShadowAttribute,
        // then set in context
        checkForMockRequest(input);
        WorkFlowTransactionBean workFlowTransactionBean = new WorkFlowTransactionBean();
        workFlowTransactionBean.setWorkFlowBean(input);
        WorkFlowResponseBean responseBean = new WorkFlowResponseBean();
        final TaskedWorkflow workFlow = workflowFactory.getWorkFlow(input, workFlowTransactionBean);
        List<Set<AbstractTask>> tasks = workFlow.getTasks();
        boolean latchStatus = false;
        for (int i = 0; i < tasks.size(); i++) {

            long startTime = System.currentTimeMillis();
            // Set of all tasks at ith level
            Set<AbstractTask> taskSet = tasks.get(i);

            try {
                // Create countdown latch for this level
                Set<AbstractTask> finalExecutableTaskSet = new HashSet<>();
                // filtering task that are actually meant to be executed
                for (AbstractTask t : taskSet) {
                    if (t.isRunnable(input, workFlowTransactionBean))
                        finalExecutableTaskSet.add(t);
                    else {
                        LOGGER.debug("isRunnable: false, for task: {}", t.getTaskName());
                    }
                }
                // map to maintain response at each level for post processing
                Map<AbstractTask, GenericCoreResponseBean<?>> taskResponseMapping = TaskExecutorHelper
                        .initializeResponseMapping(finalExecutableTaskSet);

                CountDownLatch latch = new CountDownLatch(finalExecutableTaskSet.size());

                // Get the max time for this level's countdown latch
                int maxAwaitTime = TaskExecutorHelper.getMaxAwaitTime(finalExecutableTaskSet, i, input,
                        workFlowTransactionBean);

                final Map<String, String> parentThreadContextMap = MDC.getCopyOfContextMap();
                // Add all tasks for this level in pool and start execution
                for (AbstractTask t : finalExecutableTaskSet) {
                    long submitTaskTime = System.currentTimeMillis();
                    executor.submit(() -> {
                        setChildThreadContextMap(parentThreadContextMap);
                        t.doExecute(input, workFlowTransactionBean, responseBean, taskResponseMapping,
                                Optional.of(latch), submitTaskTime);
                    });
                }

                // Wait on the latch till max timeout
                latchStatus = latch.await(maxAwaitTime, TimeUnit.MILLISECONDS);

                // Check if there is some mandatory failed task, then exit from
                // engine
                Optional<GenericCoreResponseBean<WorkFlowResponseBean>> failedTaskResponse = TaskExecutorHelper
                        .isTaskExecutorContinueProcessing(latchStatus, taskResponseMapping, input,
                                workFlowTransactionBean);
                if (failedTaskResponse.isPresent()) {
                    LOGGER.error("Abruptly exiting executor due to mandatory/latch failure: {}, latch status {}",
                            failedTaskResponse, latchStatus);
                    return failedTaskResponse.get();
                }

            } catch (InterruptedException e) {
                LOGGER.error("Interrupted exception occured: ", e.getMessage());
            } finally {
                long executionTime = System.currentTimeMillis() - startTime;
                EXT_LOGGER.customInfo("COMPLETED Level: {} execution, Total time: {} ms", i, executionTime);
            }
        }
        return new GenericCoreResponseBean<>(responseBean);
    }

    private void checkForMockRequest(final WorkFlowRequestBean input) {
        if (ThreadLocalUtil.getForMockRequest()) {
            // PGP-31371 Logs removal activity for theia
            LOGGER.debug("Mock flag true in ThreadLocal");
            input.setMockRequest(Boolean.TRUE);
        } else {
            // PGP-31371 Logs removal activity for theia
            LOGGER.debug("Mock flag not set in ThreadLocal");
            input.setMockRequest(Boolean.FALSE);
        }
    }

    private void setChildThreadContextMap(Map<String, String> context) {
        MDC.setContextMap(context);
    }
}
