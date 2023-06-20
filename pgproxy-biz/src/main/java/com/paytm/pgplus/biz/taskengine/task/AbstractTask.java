package com.paytm.pgplus.biz.taskengine.task;

import com.paytm.pgplus.biz.exception.BaseException;
import com.paytm.pgplus.biz.taskengine.common.TaskName;
import com.paytm.pgplus.biz.utils.EventUtils;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowHelper;
import com.paytm.pgplus.common.enums.EventNameEnum;
import com.paytm.pgplus.common.util.ThreadLocalUtil;
import com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.logging.ExtendedLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Abstract task governing the overall task execution semantics
 *
 * @author ankit.singhal
 * @version $Id: RegistryHelper.java, v 0.1 2018-06-13 10:38 AM ankit.singhal
 *          Exp $$
 */
public abstract class AbstractTask<I, U, R> implements Task<I, U> {
    public static final Logger LOGGER = LoggerFactory.getLogger(AbstractTask.class);
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(AbstractTask.class);

    @Autowired
    @Qualifier("workFlowHelper")
    protected WorkFlowHelper workFlowHelper;

    /**
     * Main execution method for the task
     *
     * @param input
     * @param transBean
     * @param response
     * @param map
     * @param latch
     */
    public void doExecute(I input, U transBean, R response, Map<AbstractTask<I, U, R>, GenericCoreResponseBean<?>> map,
            Optional<CountDownLatch> latch, long submitTaskTime) {

        long startTime = System.currentTimeMillis();

        try {
            // Do Preprocess
            doPreprocess(input, submitTaskTime);

            // Execute the task
            runTask(input, transBean, response, map);

            // Do PostProcess
            doPostProcess(transBean, response);

        } catch (Exception e) {
            LOGGER.error("Exception occurred while performing {} : ", getTaskName(), e);
        } finally {

            // Stop timer
            long totalTimeSpent = System.currentTimeMillis() - startTime;

            if (!"TASK_INIT".equals(map.get(this).getFailureDescription())) {
                LOGGER.info("COMPLETED Task: {}, Status: {}, Total time: {} ms", getTaskName(), map.get(this)
                        .isSuccessfullyProcessed(), totalTimeSpent);
            }

            // Clean up the MDC
            MDC.clear();

            // Clean up ThreadLocal for mock request
            ThreadLocalUtil.unsetForMockRequest();

            // Count - down latch if present
            latch.ifPresent(CountDownLatch::countDown);
        }
    }

    /**
     * Preprocessing method
     *
     * @param input
     *            business input
     */
    private void doPreprocess(I input, long submitTaskTime) {

        // Add the MDC parameters again in pool's thread
        WorkFlowRequestBean requestBean = (WorkFlowRequestBean) input;
        MDC.put(TheiaConstant.RequestParams.MID, requestBean.getPaytmMID());
        MDC.put(TheiaConstant.RequestParams.ORDER_ID, requestBean.getOrderID());
        MDC.put(TheiaConstant.RequestParams.TASK_NAME, getTaskName().getValue());
        MDC.put(TheiaConstant.RequestParams.REFERENCE_ID, requestBean.getReferenceId());

        Map<String, String> metaData = new HashMap<>();
        metaData.put("WAITING_TIME", String.valueOf(System.currentTimeMillis() - submitTaskTime));
        EventUtils.pushTheiaEvents(EventNameEnum.TASK_WAITING_TIME, metaData);

        // Set the mock context in executor thread
        if (requestBean.isMockRequest()) {
            LOGGER.info("doPreprocess: isMockRequest found true in workflowRequest Bean");
            MDC.put(TheiaConstant.RequestParams.IS_MOCK_REQUEST, Boolean.TRUE.toString());
            ThreadLocalUtil.setForMockRequest(Boolean.TRUE);
        }

        // Delegate to the task
        doBizPreProcess(input);
    }

    /**
     * Postprocessing method
     *
     */
    private void doPostProcess(U transBean, R response) {
        // Delegate to the task
        doBizPostProcess(transBean, response);
    }

    /**
     * Main task execution to be derived by sub-classes
     *
     * @param input
     * @param transBean
     * @param response
     */
    protected abstract GenericCoreResponseBean<?> doBizExecute(I input, U transBean, R response) throws BaseException;

    /**
     * Task name to be provided by individual sub-classes
     *
     * @return Taskname enum
     */
    public abstract TaskName getTaskName();

    /**
     * If the task is runnable. Set as default true. Should be overriden by
     * derived classes as per business
     *
     * @param input
     *            Input bean
     * @param transBean
     *            Transacrional bean
     * @return Whether runnable or not
     */
    public boolean isRunnable(I input, U transBean) {
        return true;
    }

    /**
     * Business post processing, if needed by sub-classes
     * 
     * @param transBean
     * @param response
     */
    protected void doBizPostProcess(U transBean, R response) {
        // Empty default implementation
    }

    /**
     * Business pre processing, if needed by sub-classes
     *
     * @param input
     */
    protected void doBizPreProcess(I input) {
        // Empty default implementation
    }

    /**
     * Abstract logical execution for the task
     *
     * @param input
     *            Inputbean
     * @param transBean
     *            Transactional bean
     * @param response
     *            Workflow response bean
     * @param map
     *            tasked map with containing response for each task at this
     *            level
     */
    private void runTask(I input, U transBean, R response, Map<AbstractTask<I, U, R>, GenericCoreResponseBean<?>> map) {
        GenericCoreResponseBean<?> responseBean = null;

        try {
            responseBean = doBizExecute(input, transBean, response);
            // Just for handling special case when we dont
            // rely on contract by workflow helper that it will always
            // return some non-nullable response bean
            if (responseBean == null) {
                // Create a new Generic response bean with specific error
                responseBean = new GenericCoreResponseBean<>(createSpecificError(this));
            }
        } catch (Exception e) {
            responseBean = new GenericCoreResponseBean<>(e.getMessage());
            LOGGER.error("Exception occurred while performing {} : {}", getTaskName(), e);
        } finally {
            // TODO remove try catch after testing
            try {
                // LOGGER.info("finally block for task :{}", getTaskName());
                EXT_LOGGER.customInfo("finally block for task :{}", getTaskName());
                if (responseBean == null) {
                    // LOGGER.info("response bean is null");
                    EXT_LOGGER.customInfo("response bean is null");
                } else if (responseBean.getResponse() == null) {
                    // LOGGER.info("response bean response is null");
                    EXT_LOGGER.customInfo("response bean response is null");
                }
                map.put(this, responseBean);
            } catch (Exception e) {
                LOGGER.error("Exception occurred while performing tasks", e);
            }
        }

    }

    private String createSpecificError(AbstractTask<I, U, R> abstractTask) {
        StringBuilder sb = new StringBuilder();
        sb.append("Response by task: ").append(abstractTask.getTaskName()).append(" ");
        sb.append("is empty which is not expected. Setting fail.");

        return sb.toString();
    }

    public String printTasks(I input, U transBean) {
        final StringBuilder sb = new StringBuilder();
        sb.append("Task[");
        sb.append(this.getTaskName());
        sb.append(",").append(getMaxExecutionTime());
        sb.append(",").append(isMandatory(input, transBean));
        sb.append(']');
        return sb.toString();
    }
}
