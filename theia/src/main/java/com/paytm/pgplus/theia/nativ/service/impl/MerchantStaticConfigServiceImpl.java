package com.paytm.pgplus.theia.nativ.service.impl;

import com.paytm.pgplus.cache.model.MerchantStaticConfig;
import com.paytm.pgplus.mappingserviceclient.service.IConfigurationService;
import com.paytm.pgplus.response.ResponseHeader;
import com.paytm.pgplus.response.ResultInfo;
import com.paytm.pgplus.logging.ExtendedLogger;
import com.paytm.pgplus.theia.nativ.enums.ResultCode;
import com.paytm.pgplus.theia.nativ.model.merchantuserinfo.MerchantStaticConfigResponse;
import com.paytm.pgplus.theia.nativ.model.merchantuserinfo.MerchantStaticConfigResponseBody;
import com.paytm.pgplus.theia.nativ.model.merchantuserinfo.MerchantStaticConfigServiceRequest;
import com.paytm.pgplus.theia.nativ.service.IMerchantStaticConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("merchantStaticConfigServiceImpl")
public class MerchantStaticConfigServiceImpl implements IMerchantStaticConfigService {

    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(MerchantStaticConfigServiceImpl.class);

    @Autowired
    @Qualifier("configurationService")
    private IConfigurationService configurationServiceImpl;

    @Override
    public MerchantStaticConfigResponse getMerchantStaticConfig(
            MerchantStaticConfigServiceRequest merchantStaticConfigServiceRequest) {
        MerchantStaticConfigResponse merchantStaticConfigResponse = new MerchantStaticConfigResponse(
                new ResponseHeader(), new MerchantStaticConfigResponseBody());
        MerchantStaticConfig merchantStaticConfig;
        try {
            merchantStaticConfig = configurationServiceImpl.getMerchantStaticConfig(merchantStaticConfigServiceRequest
                    .getMid());
            EXT_LOGGER.customInfo("Merchant Static Config from MS: {}", merchantStaticConfig);
            merchantStaticConfigResponse.getBody().setMerchantStaticConfig(merchantStaticConfig);
        } catch (Exception e) {
            EXT_LOGGER.error("Error in MerchantStaticConfigService: {}" + e.getMessage());
            merchantStaticConfigResponse.getBody().setResultInfo(
                    new ResultInfo(ResultCode.SYSTEM_ERROR.getResultStatus(), ResultCode.SYSTEM_ERROR.getResultCode(),
                            ResultCode.SYSTEM_ERROR.getResultMsg()));
        }
        return merchantStaticConfigResponse;
    }
}
