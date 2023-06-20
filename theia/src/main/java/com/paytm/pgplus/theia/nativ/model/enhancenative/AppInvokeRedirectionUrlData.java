package com.paytm.pgplus.theia.nativ.model.enhancenative;

import com.paytm.pgplus.common.validator.GenericBeanValidator;
import com.paytm.pgplus.theia.utils.ConfigurationUtil;
import com.paytm.pgplus.theia.utils.EnvInfoUtil;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.net.MalformedURLException;
import java.net.URL;

import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.*;

public class AppInvokeRedirectionUrlData {

    @NotNull(message = "mid cannot be null")
    private String mid;

    @NotNull(message = "orderId cannot be null")
    private String orderId;

    @NotNull(message = "txnToken cannot be null")
    private String txnToken;

    @NotNull(message = "amount cannot be null")
    private String amount;

    @NotNull(message = "sourceName cannot be null")
    private String sourceName;

    @NotNull(message = "sourceUrl cannot be null")
    private String sourceUrl;

    private String appInvokeFrom;

    private AppInvokeRedirectionUrlData(final Builder builder) {
        this.mid = builder.mid;
        this.orderId = builder.orderId;
        this.txnToken = builder.txnToken;
        this.amount = builder.amount;
        this.sourceName = builder.sourceName;
        this.sourceUrl = builder.sourceUrl;
        this.appInvokeFrom = builder.appInvokeFrom;
    }

    private static StringBuilder getAppInvokeBaseUrl(String appInvokeUrl) throws MalformedURLException {
        HttpServletRequest request = EnvInfoUtil.httpServletRequest();
        URL url = new URL(request.getRequestURL().toString());
        String port = url.getPort() == -1 ? "" : ":" + url.getPort();
        return new StringBuilder().append("https").append("://").append(url.getHost()).append(port)
                .append(appInvokeUrl);
    }

    public String getRedirectionUrl() throws MalformedURLException {
        return getBaseUrl().append(getQueryParamForRedirectionUrl()).toString();
    }

    public String getV3RedirectionUrl() throws MalformedURLException {
        return getV3BaseUrl().append(getQueryParamForRedirectionUrl()).toString();
    }

    private StringBuilder getQueryParamForRedirectionUrl() {
        StringBuilder queryParam = new StringBuilder("?");
        queryParam.append("mid=").append(mid).append('&');
        queryParam.append("orderId=").append(orderId).append('&');
        queryParam.append("txnToken=").append(txnToken).append('&');
        queryParam.append("amount=").append(amount).append('&');
        queryParam.append("sourceName=").append(sourceName).append('&');
        queryParam.append("sourceUrl=").append(sourceUrl).append('&');
        queryParam.append("isAppLink=true").append('&'); // To identify as an
        // Applink
        queryParam.append("appInvokeFrom=").append(appInvokeFrom);
        return queryParam;
    }

    public static StringBuilder getBaseUrl() throws MalformedURLException {
        return getAppInvokeBaseUrl(ConfigurationUtil.getProperty(APP_INVOKE_URL_CONFIG, NATIVE_APP_INVOKE_URL));
    }

    public static StringBuilder getV3BaseUrl() throws MalformedURLException {
        return getAppInvokeBaseUrl(ConfigurationUtil.getProperty(APP_INVOKE_URL_CONFIG_V3, NATIVE_APP_INVOKE_URL));
    }

    public static class Builder implements com.paytm.pgplus.facade.common.interfaces.Builder {
        private String mid;
        private String orderId;
        private String txnToken;
        private String amount;
        private String sourceName;
        private String sourceUrl;
        private String appInvokeFrom;

        public Builder mid(String mid) {
            this.mid = mid;
            return this;
        }

        public Builder orderId(String orderId) {
            this.orderId = orderId;
            return this;
        }

        public Builder txnToken(String txnToken) {
            this.txnToken = txnToken;
            return this;
        }

        public Builder amount(String amount) {
            this.amount = amount;
            return this;
        }

        public Builder sourceName(String sourceName) {
            this.sourceName = sourceName;
            return this;
        }

        public Builder sourceUrl(String sourceUrl) {
            this.sourceUrl = sourceUrl;
            return this;
        }

        public Builder appInvokeFrom(String appInvokeFrom) {
            this.appInvokeFrom = appInvokeFrom;
            return this;
        }

        @Override
        public AppInvokeRedirectionUrlData build() {
            AppInvokeRedirectionUrlData data = new AppInvokeRedirectionUrlData(this);
            if (validate(data)) {
                return data;
            }
            return null;
        }

        private boolean validate(AppInvokeRedirectionUrlData request) {
            final GenericBeanValidator<AppInvokeRedirectionUrlData> bean = new GenericBeanValidator<>(request);
            return bean.validate();
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AppInvokeRedirectionUrlData{");
        sb.append("mid='").append(mid).append('\'');
        sb.append(", orderId='").append(orderId).append('\'');
        sb.append(", txnToken='").append(txnToken).append('\'');
        sb.append(", amount='").append(amount).append('\'');
        sb.append(", sourceName='").append(sourceName).append('\'');
        sb.append(", sourceUrl='").append(sourceUrl).append('\'');
        sb.append(", appInvokeFrom='").append(appInvokeFrom).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
