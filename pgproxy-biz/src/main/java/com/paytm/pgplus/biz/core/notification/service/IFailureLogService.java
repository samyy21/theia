package com.paytm.pgplus.biz.core.notification.service;

import com.paytm.pgplus.biz.core.model.request.FailureLogBean;

public interface IFailureLogService {
    void pushFailureLogToKafka(FailureLogBean failureLogBean);
}