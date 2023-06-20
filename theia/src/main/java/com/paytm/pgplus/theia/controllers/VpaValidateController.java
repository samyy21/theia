package com.paytm.pgplus.theia.controllers;

import com.paytm.pgplus.biz.mapping.models.MappingMerchantData;
import com.paytm.pgplus.facade.bankrequest.Service.BankValidateService;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.request.SecureRequestHeader;
import com.paytm.pgplus.response.SecureResponseHeader;
import com.paytm.pgplus.theia.annotation.SignedResponseBody;
import com.paytm.pgplus.theia.cache.IMerchantMappingService;
import com.paytm.pgplus.theia.models.ValidateVpaRequest;
import com.paytm.pgplus.theia.models.response.PPBLUPICollectData;
import com.paytm.pgplus.theia.models.response.VpaValidateResponse;
import com.paytm.pgplus.theia.nativ.annotations.NativeControllerAdvice;
import com.paytm.pgplus.theia.oltpu.models.MerchantRequest;
import com.paytm.pgplus.theia.utils.ChecksumService;
import com.paytm.pgplus.theia.utils.ConfigurationUtil;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.kafka.common.errors.InvalidRequestException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by anamika on 7/9/18.
 */
@Controller
@RequestMapping("/vpa")
@NativeControllerAdvice
public class VpaValidateController {

    @Autowired
    BankValidateService bankValidateService;

    @Autowired
    IMerchantMappingService merchantMappingService;

    @Autowired
    ChecksumService checksumService;

    private static final Logger LOGGER = LoggerFactory.getLogger(VpaValidateController.class);

    @RequestMapping(value = "/validate", method = RequestMethod.POST)
    @SignedResponseBody()
    public VpaValidateResponse checkValidVpa(@RequestBody ValidateVpaRequest validateVpaRequest) {

        LOGGER.info("Merchant Request received for checkValidateVpa : {}", validateVpaRequest);

        PPBLUPICollectData ppblupiCollectData = new PPBLUPICollectData();
        String flag = ConfigurationUtil.getProperty("signature.required");
        MerchantRequest merchantRequest = validateVpaRequest.getBody();
        SecureRequestHeader header = validateVpaRequest.getHead();
        SecureResponseHeader secureResponseHeader = new SecureResponseHeader();
        secureResponseHeader.setClientId(header.getClientId());

        if (StringUtils.isBlank(merchantRequest.getMid()) || StringUtils.isBlank(merchantRequest.getVpaAddress())) {
            ppblupiCollectData.setResponseMsg("Mandatory parameter is missing");
            ppblupiCollectData.setMid(merchantRequest.getMid());
            ppblupiCollectData.setOrderId(merchantRequest.getOrderId());
            ppblupiCollectData.setVpa(merchantRequest.getVpaAddress());
            ppblupiCollectData.setValid(false);
            VpaValidateResponse vpaValidateResponse = new VpaValidateResponse(secureResponseHeader, ppblupiCollectData);
            return vpaValidateResponse;
        }

        try {

            String seqNo = generateRandomString();
            ppblupiCollectData.setVpa(merchantRequest.getVpaAddress());
            ppblupiCollectData.setOrderId(merchantRequest.getOrderId());

            final GenericCoreResponseBean<MappingMerchantData> merchantMappingResponse = merchantMappingService
                    .fetchMerchanData(merchantRequest.getMid());
            if (merchantMappingResponse == null || !merchantMappingResponse.isSuccessfullyProcessed()
                    || StringUtils.isBlank(merchantMappingResponse.getResponse().getOfficialName())) {
                throw new InvalidRequestException("Merchant deatils not found");
            }

            String merchantName = getTrimmedMerchantName(merchantMappingResponse.getResponse().getOfficialName());
            JSONObject payResponse = bankValidateService.getResponse(seqNo, merchantRequest.getVpaAddress(),
                    merchantName);
            LOGGER.info("Response recieved from bank : {}", payResponse);

            Object successObj = payResponse.get("success");
            boolean success = successObj != null ? Boolean.valueOf(successObj.toString()) : false;

            Object mobileAppDataObj = payResponse.get("MobileAppData");
            String mobileAppData = mobileAppDataObj != null ? mobileAppDataObj.toString() : null;

            if (success && mobileAppData != null) {
                String[] mobileAppDataValues = mobileAppData.split(",");
                if (mobileAppDataValues.length > 1) {
                    if ("SUCCESS".equalsIgnoreCase(mobileAppDataValues[0])) {
                        ppblupiCollectData.setValid(true);
                    } else {
                        ppblupiCollectData.setValid(false);
                    }
                }
            }
            ppblupiCollectData.setMid(merchantRequest.getMid());
        } catch (Exception e) {
            LOGGER.error("SYSTEM ERROR :", e.getMessage());
        }
        ppblupiCollectData.setResponseMsg("Success");
        LOGGER.info("Response returning to merchant : {}", ppblupiCollectData);
        VpaValidateResponse vpaValidateResponse = new VpaValidateResponse(secureResponseHeader, ppblupiCollectData);

        return vpaValidateResponse;
    }

    private String getTrimmedMerchantName(String merchantName) {
        return merchantName != null ? merchantName.replace(" ", "") : merchantName;
    }

    private String generateRandomString() {
        String randStr = RandomStringUtils.random(10, true, true);
        StringBuilder sb = new StringBuilder();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddhhmm");
        sb.append("PTM").append(randStr).append(dateFormat.format(new Date()));
        return sb.toString();
    }
}
