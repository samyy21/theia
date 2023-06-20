package com.paytm.pgplus.biz.taskengine.taskexecutor;

import com.paytm.pgplus.biz.taskengine.task.AbstractTask;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Helper class for TaskExecutor
 * 
 * @author ankit.singhal
 * @version $Id: TaskExecutorHelper.java, v 0.1 2018-06-13 4:47 PM ankit.singhal
 *          Exp $$
 */
public class TaskExecutorHelper {

    public static final Logger LOGGER = LoggerFactory.getLogger(TaskExecutorHelper.class);

    /**
     * Checks if any of the mandatory task fails at some level, then the engine
     * should be stopped
     * 
     * @param latchSucceed
     *            Latch status if the level is fully completed or latch expired
     *            in between
     * @param taskResponseMapping
     *            Response of each task at the current level
     * @return Optional error response determining the failure (if any),
     *         otherwise empty
     */
    static Optional<GenericCoreResponseBean<WorkFlowResponseBean>> isTaskExecutorContinueProcessing(
            boolean latchSucceed, Map<AbstractTask, GenericCoreResponseBean<?>> taskResponseMapping,
            WorkFlowRequestBean requestBean, WorkFlowTransactionBean transBean) {
        // If level failed then send error from here itself without full engine
        // execution
        Optional<Map.Entry> mandatoryFailedTask = getMandatoryFailedTask(taskResponseMapping, requestBean, transBean);
        if (mandatoryFailedTask.isPresent()) {
            GenericCoreResponseBean<?> failedTaskResponse = (GenericCoreResponseBean<?>) mandatoryFailedTask.get()
                    .getValue();
            if (latchSucceed) {
                // Create error response bean and return
                return Optional.of(new GenericCoreResponseBean<>(failedTaskResponse.getFailureDescription(),
                        failedTaskResponse.getResponseConstant()));
            } else {
                // Latch already expired while some tasks are still in execution
                return Optional.of(new GenericCoreResponseBean<>("Latch expired", ResponseConstants.SYSTEM_ERROR));

            }
        }
        return Optional.empty();
    }

    /**
     * Initialize with default responses to avoid latch wait timeout before
     * tasks finish execution
     * 
     * @param taskSet
     *            Current level task set
     * @return Default response mapping
     */
    static Map<AbstractTask, GenericCoreResponseBean<?>> initializeResponseMapping(Set<AbstractTask> taskSet) {
        Map<AbstractTask, GenericCoreResponseBean<?>> taskMapping = new ConcurrentHashMap<>();
        for (AbstractTask abstractTask : taskSet) {
            taskMapping.put(abstractTask, new GenericCoreResponseBean("TASK_INIT"));
        }

        return taskMapping;
    }

    /**
     * Gets the mandatory task which is failed at this level
     * 
     * @param taskResponseMapping
     *            Map containing task and its responses
     * @return Failed task (if any)
     */
    static Optional<Map.Entry> getMandatoryFailedTask(
            Map<AbstractTask, GenericCoreResponseBean<?>> taskResponseMapping, WorkFlowRequestBean requestBean,
            WorkFlowTransactionBean transBean) {
        for (Map.Entry entry : taskResponseMapping.entrySet()) {
            AbstractTask task = (AbstractTask) entry.getKey();
            GenericCoreResponseBean<?> taskResponse = null;
            if (entry.getValue() != null) {
                taskResponse = (GenericCoreResponseBean<?>) entry.getValue();
            }
            if (taskResponse != null && !taskResponse.isSuccessfullyProcessed()
                    && task.isMandatory(requestBean, transBean)) {
                LOGGER.error("Mandatory Task {} failed! Exiting Task Executor!", task.getTaskName());
                return Optional.of(entry);
            }
        }
        return Optional.empty();
    }

    /**
     * Helper method to determine the overall level's max latch time based on
     * all tasks's max exectuion time
     * 
     * @param taskSet
     *            This level's task set
     * @param level
     *            Level id
     * @return Maximum latch time which should be configured for this level
     */
    static int getMaxAwaitTime(Set<AbstractTask> taskSet, int level, WorkFlowRequestBean input,
            WorkFlowTransactionBean transBean) {
        int maxAwaitTime = 0;
        StringBuilder sb = new StringBuilder();
        for (AbstractTask t : taskSet) {
            if (maxAwaitTime < t.getMaxExecutionTime()) {
                maxAwaitTime = t.getMaxExecutionTime();
            }
            sb.append(t.printTasks(input, transBean));
        }
        LOGGER.debug("Maximum latch time for level {} = {}, TaskSet: {}", level, maxAwaitTime, sb);
        return maxAwaitTime;
    }
}