package com.paytm.pgplus.biz.utils;

import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.facade.paymentrouter.enums.Routes;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.paytm.pgplus.facade.utils.JsonMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;

import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.FF4J.*;

@Component
public class PG2Util {
    private static final Logger LOGGER = LoggerFactory.getLogger(PG2Util.class);

    public Routes getRouteForLpvRequest(boolean isPg2FullTrafficEnabled, String mid) {
        return Routes.PG2;
    }

    public Routes getRouteForTopUpRequest(String mid) {
        return Routes.PG2;
    }
}

class PG2RollOutModes implements Serializable {
    public static class PayMethod implements Serializable {
        private String payMode;
        private List<String> channels;

        public PayMethod() {
        }

        public String getPayMode() {
            return payMode;
        }

        public void setPayMode(String payMode) {
            this.payMode = payMode;
        }

        public List<String> getChannels() {
            return channels;
        }

        public void setChannels(List<String> channels) {
            this.channels = channels;
        }
    }

    private List<PayMethod> paymentModes;

    public List<PayMethod> getPaymentModes() {
        return paymentModes;
    }

    public void setPaymentModes(List<PayMethod> paymentModes) {
        this.paymentModes = paymentModes;
    }
}
