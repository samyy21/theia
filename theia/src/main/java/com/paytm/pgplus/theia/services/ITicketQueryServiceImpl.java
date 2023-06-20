package com.paytm.pgplus.theia.services;

/**
 * @author Naman
 * @date 23/02/18
 */

public interface ITicketQueryServiceImpl {

    String fetchTicketQueryPRN(String paytmMerchantId, String acquirementId);

}
