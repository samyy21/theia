package com.paytm.pgplus.cashier.workflow.util;

import com.paytm.pgplus.cashier.workflow.AbstractCashierWorkflow;

/**
 * Spring based Service Locator for Workflow Beans
 * 
 * @author Lalit Mehra
 * @since March 7, 2016
 *
 */
public interface CashierWorkflowFactory {

    public AbstractCashierWorkflow getCashierWorkflow(String service);

}
