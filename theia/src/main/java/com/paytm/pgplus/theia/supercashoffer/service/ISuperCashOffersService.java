package com.paytm.pgplus.theia.supercashoffer.service;

import com.paytm.pgplus.theia.supercashoffer.model.SuperCashOfferRequest;
import com.paytm.pgplus.theia.supercashoffer.model.SuperCashOfferResponse;

public interface ISuperCashOffersService {

    SuperCashOfferResponse applySuperCash(SuperCashOfferRequest superCashOfferRequest, boolean offlineFlows);
}
