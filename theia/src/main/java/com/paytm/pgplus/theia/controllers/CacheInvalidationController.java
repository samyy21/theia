//package com.paytm.pgplus.theia.controllers;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.ws.rs.Consumes;
//import javax.ws.rs.Produces;
//import javax.ws.rs.core.MediaType;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//import org.springframework.web.bind.annotation.RestController;
//
//import com.paytm.pgplus.applicationcache.CacheManager;
//import com.paytm.pgplus.theia.models.CacheInvalidationRequest;
//
//@RestController
//public class CacheInvalidationController {
//
//    private static final Logger LOGGER = LoggerFactory.getLogger(CreateInvoiceController.class);
//
//    @RequestMapping(value = "/invalidateCache", method = { RequestMethod.GET })
//    @Produces(MediaType.APPLICATION_JSON)
//    @Consumes(MediaType.APPLICATION_JSON)
//    public String invalidateCache(final HttpServletRequest request) {
//        // LOGGER.info("Received Hit to invalidate All Caches");
//        CacheManager.getCacheManager().flushAll();
//        LOGGER.info("Successfully cleared all Caches.");
//        return "{\"Result\":\"Success\"}";
//    }
//
//    @RequestMapping(value = "/invalidateSpecificCache", method = { RequestMethod.POST })
//    @Produces(MediaType.APPLICATION_JSON)
//    @Consumes(MediaType.APPLICATION_JSON)
//    public String invalidateCache(final HttpServletRequest request,
//            @RequestBody CacheInvalidationRequest cacheInvalidationRequest) {
//        // LOGGER.info("Received Hit to invalidate Caches : {}",
//        // cacheInvalidationRequest);
//        for (String cacheName : cacheInvalidationRequest.getCacheNames()) {
//            LOGGER.debug("Going to clear cache : {}", cacheName);
//            CacheManager.getCacheManager().flushAll(cacheName);
//        }
//        LOGGER.info("Successfully cleared all Caches.");
//        return "{\"Result\":\"Success\"}";
//    }
// }
