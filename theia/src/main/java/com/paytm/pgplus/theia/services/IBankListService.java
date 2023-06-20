package com.paytm.pgplus.theia.services;

import com.paytm.pgplus.common.model.EnvInfoRequestBean;
import com.paytm.pgplus.theia.models.BankListRequest;
import com.paytm.pgplus.theia.viewmodel.BankInfo;

import java.io.IOException;
import java.util.List;

public interface IBankListService {

    List<BankInfo> fetchAvailableBankList(BankListRequest request, EnvInfoRequestBean envInfoRequestBean)
            throws IOException;

}
