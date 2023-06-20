package com.paytm.pgplus.biz.taskengine.task;

public interface Task<I, U> {

    boolean isMandatory(I inputBean, U transBean);

    boolean isRunnable(I inputBean, U transBean);

    int getMaxExecutionTime();

}
