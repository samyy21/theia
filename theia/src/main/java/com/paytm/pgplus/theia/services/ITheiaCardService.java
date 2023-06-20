package com.paytm.pgplus.theia.services;

import javax.servlet.http.HttpServletRequest;

/**
 * @createdOn 25-Nov-2016
 * @author Santosh
 */
public interface ITheiaCardService {
    String processDeleteCard(final HttpServletRequest request, String savedCardId, boolean deleteFromDB);

}
