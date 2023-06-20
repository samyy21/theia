package com.paytm.pgplus.theia.nativ.service;

public interface IBalanceInfoService<Req, ServReq, Res> {

    Res fetchBalance(Req req, ServReq servReq);

}
