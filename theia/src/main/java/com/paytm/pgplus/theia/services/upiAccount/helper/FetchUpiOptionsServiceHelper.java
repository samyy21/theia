package com.paytm.pgplus.theia.services.upiAccount.helper;

import com.paytm.pgplus.response.ResponseHeader;
import com.paytm.pgplus.response.ResultInfo;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.models.upiAccount.request.FetchUpiOptionsRequest;
import com.paytm.pgplus.theia.models.upiAccount.response.FetchUpiOptionsResponse;
import com.paytm.pgplus.theia.models.upiAccount.response.FetchUpiOptionsResponseBody;
import com.paytm.pgplus.theia.nativ.model.common.UpiPspOptions;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.enums.TokenType;
import com.paytm.pgplus.theia.offline.exceptions.RequestValidationException;
import com.paytm.pgplus.theia.paymentoffer.helper.TokenValidationHelper;
import com.paytm.pgplus.theia.taglibs.PaytmTLD;
import com.paytm.pgplus.theia.utils.ConfigurationUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.*;

@Deprecated
@Service
public class FetchUpiOptionsServiceHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(FetchUpiOptionsServiceHelper.class);

    @Autowired
    @Qualifier("merchantPreferenceService")
    private IMerchantPreferenceService merchantPreferenceService;
    @Autowired
    private TokenValidationHelper tokenValidationHelper;

    public void validateRequest(FetchUpiOptionsRequest request) {

        if (request.getHead() == null || request.getBody() == null || StringUtils.isBlank(request.getBody().getMid())) {
            throw RequestValidationException.getException("invalid request params");
        }

        if (TokenType.CHECKSUM == request.getHead().getTokenType()) {
            tokenValidationHelper.validateToken(request.getHead().getToken(), request.getHead().getTokenType(),
                    request.getBody(), request.getBody().getMid());
        } else {
            LOGGER.info("tokenType = {} is not supported", request.getHead().getTokenType());
            throw RequestValidationException.getException(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION);
        }
    }

    public FetchUpiOptionsResponse fetchUpiOptions(FetchUpiOptionsRequest fetchUpiOptionsRequest) {
        String pspIconBaseUrl = PaytmTLD.getStaticUrlPrefix();
        String upiPspAppNames = ConfigurationUtil.getProperty(UPI_PSP_NAME);
        FetchUpiOptionsResponse fetchUpiOptionsResponse = new FetchUpiOptionsResponse(new ResponseHeader(),
                new FetchUpiOptionsResponseBody());

        LOGGER.info("psp Icon Base Url ->{} and upiPspAppNames ->{} for fetchUpiOptionsRequest ", pspIconBaseUrl,
                upiPspAppNames);
        if (StringUtils.isNotBlank(pspIconBaseUrl) && StringUtils.isNotBlank(upiPspAppNames)) {
            List<UpiPspOptions> upiPspOptionsList = new ArrayList<>();
            String[] upiPspNames = upiPspAppNames.split(",");
            Arrays.stream(upiPspNames).forEach(
                    s -> upiPspOptionsList.add(new UpiPspOptions(s, pspIconBaseUrl.concat(UPI_PSP_ICON_BASE_PATH)
                            .concat(s).concat(".png"))));
            fetchUpiOptionsResponse.getBody().setUpiPspOptions(upiPspOptionsList);
            return fetchUpiOptionsResponse;
        }

        fetchUpiOptionsResponse.getBody().setResultInfo(
                new ResultInfo(ResultCode.FAILED.getResultStatus(), ResultCode.FAILED.getResultCodeId(),
                        ResultCode.FAILED.getResultMsg()));
        return fetchUpiOptionsResponse;

    }
}
