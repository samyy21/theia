package com.paytm.pgplus.theia.utils;

import com.paytm.pgplus.theia.enums.UPIPollStatus;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.offline.utils.ThieaApplicationContextProvider;
import com.paytm.pgplus.theia.services.impl.TransactionStatusServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.Native.POLL_STATUS;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.Native.TXN_TOKEN;

public class TransactionStatusAsyncService implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionStatusAsyncService.class);

    private TransactionStatusServiceImpl transactionStatusServiceImpl;
    private NativeSessionUtil nativeSessionUtil;
    private HttpServletRequest httpServletRequest;

    private static TransactionStatusServiceImpl getTransactionStatusServiceImplBean(String transactionStatusServiceImpl) {
        return (TransactionStatusServiceImpl) ThieaApplicationContextProvider.getApplicationContext().getBean(
                transactionStatusServiceImpl);
    }

    private static NativeSessionUtil getNativeSessionUtilBean() {
        return ThieaApplicationContextProvider.getApplicationContext().getBean(NativeSessionUtil.class);
    }

    public TransactionStatusAsyncService(HttpServletRequest httpServletRequest) {
        this.httpServletRequest = httpServletRequest;
        this.transactionStatusServiceImpl = getTransactionStatusServiceImplBean("transactionStatusServiceImpl");
        this.nativeSessionUtil = getNativeSessionUtilBean();
    }

    @Override
    public void run() {
        try {
            Map<String, String> pollingData = transactionStatusServiceImpl.getUpiCashierResponse(httpServletRequest);
            if (pollingData != null && pollingData.containsKey(POLL_STATUS) && pollingData.containsKey(TXN_TOKEN)) {
                if (UPIPollStatus.STOP_POLLING.getMessage().equals(pollingData.get(POLL_STATUS))) {
                    nativeSessionUtil.setPollingStatus(pollingData.get(TXN_TOKEN),
                            UPIPollStatus.STOP_POLLING.getMessage());
                } else {
                    nativeSessionUtil.deleteField(pollingData.get(TXN_TOKEN), "pollingStatus");
                }
                LOGGER.info("Thread Link with Redis For UPI  polling status :- {}", pollingData.get(POLL_STATUS));
            }
        } catch (Exception e) {
            if (StringUtils.isNotBlank(httpServletRequest.getParameter(TXN_TOKEN))) {
                nativeSessionUtil.deleteField(httpServletRequest.getParameter(TXN_TOKEN), "pollingStatus");
            }
            LOGGER.error("Exception Occured while Sync for Polling status with Redis {}", e);
        }

    }
}
