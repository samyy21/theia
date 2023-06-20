package com.paytm.pgplus.theia.controllers;

import com.paytm.pgplus.cache.model.BinDetail;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;
import com.paytm.pgplus.pgproxycommon.utils.CardUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.logging.Logger;

/**
 * Created by Naman on 06/04/17.
 */
@RestController
public class TestBinLogic {

    @Autowired
    @Qualifier("cardUtils")
    private CardUtils cardUtils;

    @RequestMapping(value = "/fetchBin", method = RequestMethod.GET)
    public BinDetail fetchBinDetails(HttpServletRequest request, @RequestParam(value = "bin") String bin)
            throws PaytmValidationException {

        BinDetail binDetail = null;

        if (StringUtils.isNotBlank(bin) && bin.length() >= 6 && StringUtils.isNumeric(bin)) {

            if (bin.length() > 6) {
                bin = StringUtils.substring(bin, 0, 6);
            }

            try {
                binDetail = cardUtils.fetchBinDetails(bin);
            } catch (PaytmValidationException e) {
                return null;
            }
        }

        return binDetail;
    }
}
