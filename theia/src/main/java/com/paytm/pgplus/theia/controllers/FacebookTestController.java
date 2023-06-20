//package com.paytm.pgplus.theia.controllers;
//
//import com.paytm.pgplus.theia.models.FacebookTestRequestBean;
//import com.paytm.pgplus.theia.models.response.FacebookTestResponseBean;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//import org.springframework.web.bind.annotation.RestController;
//
//import javax.ws.rs.Produces;
//import javax.ws.rs.core.MediaType;
//
///**
// * Created by ankitsinghal on 09/07/17.
// */
//@RestController
//@RequestMapping("/testing")
//public class FacebookTestController {
//
//    @RequestMapping(value = "/encryption", method = RequestMethod.POST)
//    @Produces(MediaType.APPLICATION_JSON)
//    public FacebookTestResponseBean testDecrypt(@RequestBody FacebookTestRequestBean requestBean) {
//        FacebookTestResponseBean responseBean = new FacebookTestResponseBean();
//        responseBean.setOutput(requestBean.getInput1() + requestBean.getInput2());
//        return responseBean;
//    }
//
// }
