package com.paytm.pgplus.theia.nativ.processor;

/**
 * This is a generic interface used for process any type of request and generate
 * any type of response.
 *
 *
 * @param <Req>
 * @param <Res>
 */
public interface IRequestProcessor<Req, Res> {

    /**
     * This method need to implement to process request and generate response.
     * 
     * @param request
     * @return
     * @throws Exception
     */
    public Res process(Req request) throws Exception;

}