package com.paytm.pgplus.theia.jaxrs;

import com.paytm.pgplus.theia.models.FacebookTestRequestBean;
import com.paytm.pgplus.theia.models.response.FacebookTestResponseBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Component
@Path(value = "")
public class AsyncTestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncTestController.class);

    static {
        LOGGER.info("Him- AsyncTestController loading");
    }

    @Path(value = "/async")
    @POST
    @Consumes(value = MediaType.APPLICATION_JSON)
    // @Produces(value = MediaType.APPLICATION_JSON)
    public void testAsync(@Suspended AsyncResponse asyncResponse, FacebookTestRequestBean requestBean,
            @Context HttpServletRequest request, @Context HttpServletResponse response) throws ServletException,
            IOException {
        LOGGER.warn("Inside Async method");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            LOGGER.error("error: ", e);
        }

        FacebookTestResponseBean responseBean = new FacebookTestResponseBean();
        responseBean.setOutput(requestBean.getInput1() + requestBean.getInput2());

        // asyncResponse.resume(responseBean.getOutput().getBytes());
        String str = "/theia/processRetry?MID=1243";
        URI uri = URI.create(str);
        LOGGER.info("uri: {}", uri);
        LOGGER.info("normalize uri: {}", uri.normalize());
        LOGGER.info("absolute, {} ", uri.isAbsolute());
        asyncResponse.resume(Response.temporaryRedirect(uri).build());
        asyncResponse.resume(uri);

        RequestDispatcher dispatcher = request.getRequestDispatcher("");
        try {
            dispatcher.forward(request, response);
        } catch (ServletException e) {
            LOGGER.error("error: ", e);
        } catch (IOException e) {
            LOGGER.error("error: ", e);
        }
        LOGGER.warn("Finished Async method");
    }

    @Path(value = "/sync")
    @POST
    // @Consumes(value = MediaType.APPLICATION_JSON)
    // @Produces(value = MediaType.APPLICATION_JSON)
    public void testSync(FacebookTestRequestBean requestBean, @Context HttpServletRequest request,
            @Context HttpServletResponse response) {
        // LOGGER.warn("Inside sync method");
        FacebookTestResponseBean responseBean = new FacebookTestResponseBean();
        responseBean.setOutput(requestBean.getInput1() + requestBean.getInput2());
        LOGGER.warn("Finished sync method");
        try {
            response.getOutputStream().write(responseBean.getOutput().getBytes());
        } catch (IOException e) {
            LOGGER.error("error: ", e);
        }
    }

    @Path(value = "getMap")
    @GET
    @Produces(value = MediaType.APPLICATION_JSON)
    public Map getMap() {
        LOGGER.warn("Return map");
        Map<String, String> map = new HashMap<>();
        map.put("inevitable", "step");
        return map;
    }

}
