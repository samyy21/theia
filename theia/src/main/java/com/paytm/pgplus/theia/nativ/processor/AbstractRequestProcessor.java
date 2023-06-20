package com.paytm.pgplus.theia.nativ.processor;

/**
 * This is an AbstractRequestProcessor which provides a skeleton for process any
 * request and generate response.
 *
 * @param <Req>
 * @param <Res>
 * 
 * @param <Req>
 *            For ex: we can see it as Controller Request
 * @param <Res>
 *            For ex: we can see it as Controller Response
 * @param <ServiceReq>
 *            For ex: we can see it as Service Request
 * @param <ServiceRes>
 *            For ex: we can see it as Service Response
 */
public abstract class AbstractRequestProcessor<Req, Res, ServiceReq, ServiceRes> implements IRequestProcessor<Req, Res> {

    /**
     * Template method to process a request and generate response.
     * 
     * @param request
     * @return
     * @throws Exception
     */
    @Override
    public final Res process(Req request) throws Exception {
        ServiceReq serviceReq = preProcess(request);
        ServiceRes serviceResp = onProcess(request, serviceReq);
        Res response = postProcess(request, serviceReq, serviceResp);
        return response;
    }

    /**
     * This method is use to validate/enrich the request before processing the
     * request. It can be use as a hook before processing any request.
     *
     * @param request
     */
    protected abstract ServiceReq preProcess(Req request) throws Exception;

    /**
     * This method is used to process the request which includes request
     * validation, processing, response generator.
     * 
     * @param request2
     *
     * @param request
     * @return
     * @throws Exception
     */
    protected abstract ServiceRes onProcess(Req request, ServiceReq serviceReq) throws Exception;

    /**
     * This method is used to decorate the response based on requirement. It can
     * be use as a hook after processing any request.
     *
     * @param response
     * @param request
     * @param serviceReq
     * @throws Exception
     */
    protected abstract Res postProcess(Req request, ServiceReq serviceReq, ServiceRes serviceRes) throws Exception;

}