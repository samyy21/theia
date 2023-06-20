package com.paytm.pgplus.theia.accesstoken.processor;

/**
 * This is a generic interface used for process any type of request and generate
 * any type of response.
 *
 *
 * @param <Request>
 * @param <Request>
 */
public interface IRequestProcessor<Request, Response> {
    /**
     * This method need to implement to process request and generate response.
     *
     * @param request
     * @return
     * @throws Exception
     */
    public Response process(Request request) throws Exception;
}