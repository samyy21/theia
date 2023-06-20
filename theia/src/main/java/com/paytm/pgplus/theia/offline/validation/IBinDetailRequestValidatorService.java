package com.paytm.pgplus.theia.offline.validation;

import com.paytm.pgplus.theia.offline.model.request.BinDetailRequest;

public interface IBinDetailRequestValidatorService {

    public String validate(BinDetailRequest requestData);

}
