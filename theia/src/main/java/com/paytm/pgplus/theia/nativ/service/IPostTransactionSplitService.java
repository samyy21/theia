package com.paytm.pgplus.theia.nativ.service;

import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.theia.nativ.model.postTransactionSplit.PostTransactionSplitRequest;
import com.paytm.pgplus.theia.nativ.model.postTransactionSplit.PostTransactionSplitResponseBody;

public interface IPostTransactionSplitService {
    public PostTransactionSplitResponseBody acquiringSplit(PostTransactionSplitRequest request)
            throws FacadeCheckedException;
}
