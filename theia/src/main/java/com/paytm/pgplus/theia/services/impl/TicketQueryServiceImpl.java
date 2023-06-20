package com.paytm.pgplus.theia.services.impl;

import com.paytm.pgplus.biz.mapping.models.MappingMerchantData;
import com.paytm.pgplus.common.enums.EventNameEnum;
import com.paytm.pgplus.facade.acquiring.models.TicketDetail;
import com.paytm.pgplus.facade.acquiring.models.request.TicketQueryRequest;
import com.paytm.pgplus.facade.acquiring.models.request.TicketQueryRequestBody;
import com.paytm.pgplus.facade.acquiring.models.response.TickQueryResponse;
import com.paytm.pgplus.facade.acquiring.services.IAcquiringTicket;
import com.paytm.pgplus.facade.common.model.AlipayExternalRequestHeader;
import com.paytm.pgplus.facade.enums.ApiFunctions;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.utils.RequestHeaderGenerator;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.theia.cache.IMerchantMappingService;
import com.paytm.pgplus.theia.services.ITicketQueryServiceImpl;
import com.paytm.pgplus.theia.utils.EventUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * @author Naman
 * @date 23/02/18
 */
@Service("ticketQueryServiceImpl")
public class TicketQueryServiceImpl implements ITicketQueryServiceImpl {

    private static final Logger LOGGER = LoggerFactory.getLogger(TicketQueryServiceImpl.class);

    @Autowired
    @Qualifier("merchantMappingService")
    private IMerchantMappingService merchantMappingService;

    @Autowired
    @Qualifier("acquiringTicketImpl")
    private IAcquiringTicket acquiringTicket;

    @Override
    public String fetchTicketQueryPRN(String paytmMerchantId, String acquirementId) {

        try {

            GenericCoreResponseBean<MappingMerchantData> genericCoreResponseBean = merchantMappingService
                    .fetchMerchanData(paytmMerchantId);

            if (!genericCoreResponseBean.isSuccessfullyProcessed() || genericCoreResponseBean.getResponse() == null
                    || StringUtils.isBlank(genericCoreResponseBean.getResponse().getAlipayId())) {
                LOGGER.error("Unable to fetch Merchant Alipay ID from Mapping Service");
                return null;
            }

            TicketQueryRequest ticketQueryRequest = fetchTicketQueryRequest(genericCoreResponseBean.getResponse()
                    .getAlipayId(), acquirementId);

            TickQueryResponse tickQueryResponse = acquiringTicket.ticketQuery(ticketQueryRequest);

            return fetchPRNFromTicketQueryResponse(tickQueryResponse);

        } catch (Exception exception) {
            LOGGER.error("Exception occurred while doing ticket query ::{}", exception);
        }

        return StringUtils.EMPTY;
    }

    private TicketQueryRequest fetchTicketQueryRequest(String alipayMerchantId, String acquirementId)
            throws FacadeCheckedException {

        AlipayExternalRequestHeader requestHeader = RequestHeaderGenerator.getHeader(ApiFunctions.TICKET_QUERY);

        TicketQueryRequestBody requestBody = new TicketQueryRequestBody(alipayMerchantId, acquirementId);

        return new TicketQueryRequest(requestHeader, requestBody);
    }

    String fetchPRNFromTicketQueryResponse(TickQueryResponse tickQueryResponse) {

        if (tickQueryResponse == null || tickQueryResponse.getBody() == null
                || tickQueryResponse.getBody().getResultInfo() == null
                || !StringUtils.equals(tickQueryResponse.getBody().getResultInfo().getResultCode(), "SUCCESS")
                || tickQueryResponse.getBody().getTicketDetails() == null
                || tickQueryResponse.getBody().getTicketDetails().size() < 1
                || tickQueryResponse.getBody().getTicketDetails().get(0) == null) {
            LOGGER.error("Unable to fetch Ticket Details");
            return StringUtils.EMPTY;
        }

        TicketDetail ticketDetail = tickQueryResponse.getBody().getTicketDetails().get(0);

        if (ticketDetail.getTicketType().equals("PRN") && StringUtils.isNotBlank(ticketDetail.getTicketValue())) {
            EventUtils.pushTheiaEvents(EventNameEnum.PRN_RECIEVED);
            return ticketDetail.getTicketValue();
        }

        return StringUtils.EMPTY;
    }
}
