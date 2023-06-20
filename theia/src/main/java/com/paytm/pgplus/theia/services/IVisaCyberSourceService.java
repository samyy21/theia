package com.paytm.pgplus.theia.services;

import com.paytm.pgplus.theia.models.VisaCyberSourceRequest;
import com.paytm.pgplus.theia.models.VisaCyberSourceResponse;

public interface IVisaCyberSourceService {

    public VisaCyberSourceResponse getCardDetailFromVisaCyberSource(VisaCyberSourceRequest visaCyberSourceRequest);
}
