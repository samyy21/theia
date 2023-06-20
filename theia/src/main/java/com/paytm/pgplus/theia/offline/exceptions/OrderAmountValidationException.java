package com.paytm.pgplus.theia.offline.exceptions;

import com.paytm.pgplus.common.model.ResultInfo;

/**
 * Created by rahulverma on 6/9/17.
 */
public class OrderAmountValidationException extends BaseException {
    /**
	 * 
	 */
    private static final long serialVersionUID = 4024201801983601167L;

    public OrderAmountValidationException(ResultInfo resultInfo) {
        super(resultInfo);
    }

    public OrderAmountValidationException() {
        super();
    }
}
