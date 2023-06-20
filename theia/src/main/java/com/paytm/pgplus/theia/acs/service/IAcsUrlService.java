package com.paytm.pgplus.theia.acs.service;

import com.paytm.pgplus.theia.exceptions.TheiaServiceException;

public interface IAcsUrlService {

    String generateACSUrl(String mid, String orderId, String webForm) throws TheiaServiceException;

    String resolveACSUrl(String mid, String orderId, String uniqueId) throws TheiaServiceException;

    void purgeAcsUrl(String mid, String orderId);
}
