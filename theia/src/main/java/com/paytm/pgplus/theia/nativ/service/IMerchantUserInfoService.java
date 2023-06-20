package com.paytm.pgplus.theia.nativ.service;

public interface IMerchantUserInfoService<Req, Res> {

    Res fetchMerchantUserInfo(Req req);

}
