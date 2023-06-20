package com.paytm.pgplus.theia.services;

import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface ISeamlessDirectBankCardService {

    void processNativeRequest(HttpServletRequest request, HttpServletResponse response,
            WorkFlowRequestBean flowRequestBean) throws ServletException, IOException;

    void processNativeEnhanceRequest(HttpServletRequest request, HttpServletResponse response,
            WorkFlowRequestBean flowRequestBean) throws IOException;

    GenericCoreResponseBean<WorkFlowResponseBean> doPayment(WorkFlowRequestBean flowRequestBean);
}
