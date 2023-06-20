package com.paytm.pgplus.theia.accesstoken.processor;

import com.paytm.pgplus.theia.accesstoken.processor.IRequestProcessor;

/**
 * This is an AbstractRequestProcessor which provides a skeleton for process any
 * request and generate response.
 *
 * @param <Request>
 *            For ex: we can see it as Controller Request
 * @param <Response>
 *            For ex: we can see it as Controller Response
 * @param <ServiceRequest>
 *            For ex: we can see it as Service Request
 * @param <ServiceResponse>
 *            For ex: we can see it as Service Response
 */
public abstract class AbstractRequestProcessor<Request, Response, ServiceRequest, ServiceResponse> implements
        IRequestProcessor<Request, Response> {

    /**
     * Template method to process a request and generate response.
     *
     * @param request
     * @return
     * @throws Exception
     */
    @Override
    public final Response process(Request request) throws Exception {
        ServiceRequest serviceRequest = preProcess(request);
        ServiceResponse serviceResponse = onProcess(request, serviceRequest);
        Response response = postProcess(request, serviceRequest, serviceResponse);
        return response;
    }

    /**
     * This method is use to validate/enrich the request before processing the
     * request. It can be use as a hook before processing any request.
     *
     * @param request
     */
    protected abstract ServiceRequest preProcess(Request request);

    /**
     * This method is used to process the request which includes request
     * validation, processing, response generator.
     *
     * @param request
     * @param serviceRequest
     * @return
     * @throws Exception
     */
    protected abstract ServiceResponse onProcess(Request request, ServiceRequest serviceRequest) throws Exception;

    /**
     * This method is used to decorate the response based on requirement. It can
     * be use as a hook after processing any request.
     *
     * @param request
     * @param serviceRequest
     * @param serviceResponse
     * @throws Exception
     */
    protected abstract Response postProcess(Request request, ServiceRequest serviceRequest,
            ServiceResponse serviceResponse) throws Exception;

}