package com.paytm.pgplus.theia.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.paytm.pgplus.common.enums.TxnState;
import com.paytm.pgplus.theia.services.IUITrackService;
import com.paytm.pgplus.transactionlogger.annotation.Loggable;

@Service("uiTrackService")
public class UITrackServiceImpl implements IUITrackService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UITrackServiceImpl.class);

    @Override
    @Loggable(logLevel = Loggable.INFO, state = TxnState.PAGE_LOAD_DETECTED)
    public void logUIData(String requestData) {
        LOGGER.info("Data Received From UI is : {} ", requestData);
    }

    @Override
    @Loggable(logLevel = Loggable.INFO, state = TxnState.UI_EXCEPTION_DETECTED)
    public void logUIException(String requestData) {
        LOGGER.error("Exception Detected at UI : {} ", requestData);
    }

}
