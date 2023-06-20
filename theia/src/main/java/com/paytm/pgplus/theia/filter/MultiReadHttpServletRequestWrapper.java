package com.paytm.pgplus.theia.filter;

import com.paytm.pgplus.logging.ExtendedLogger;
import com.paytm.pgplus.theia.nativ.FilterHelper;
import jodd.util.URLDecoder;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public class MultiReadHttpServletRequestWrapper extends HttpServletRequestWrapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(MultiReadHttpServletRequestWrapper.class);
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(MultiReadHttpServletRequestWrapper.class);
    private final String messageBody;

    public MultiReadHttpServletRequestWrapper(ServletRequest request) throws IOException {
        super((HttpServletRequest) request);
        this.messageBody = IOUtils.toString(request.getInputStream(), Charsets.UTF_8.name());
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getInputStream()));
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(messageBody.getBytes());
        return new ServletInputStream() {
            @Override
            public int read() {
                return byteArrayInputStream.read();
            }
        };
    }

    public String getMessageBody() {
        return messageBody;
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        Map<String, String[]> allParameters = super.getParameterMap();
        if (MapUtils.isEmpty(allParameters)
                && Boolean.TRUE.equals(FilterHelper.isGettingQueryParamFromQueryStringEnabled())
                && StringUtils.isNotBlank(getQueryString())) {
            allParameters = new TreeMap<String, String[]>();
            allParameters.putAll(splitQuery(getQueryString()));
            return Collections.unmodifiableMap(allParameters);
        }
        return allParameters;
    }

    @Override
    public String getParameter(final String name) {
        String[] strings = getParameterMap().get(name);
        if (strings != null) {
            return strings[0];
        }
        return super.getParameter(name);
    }

    public Map<String, String[]> splitQuery(String queryString) {
        EXT_LOGGER.customInfo("Split query :{}", queryString);
        Map<String, String[]> queryParams = new TreeMap<>();
        try {
            String[] pairs = queryString.split("&");
            for (String pair : pairs) {
                String[] keyValuePair = pair.split("=");

                if (keyValuePair.length >= 2) {
                    queryParams.put(URLDecoder.decode(keyValuePair[0], StandardCharsets.UTF_8.name()),
                            new String[] { URLDecoder.decode(keyValuePair[1], StandardCharsets.UTF_8.name()) });
                }

            }
        } catch (Exception e) {
            LOGGER.error("Exception in parsing queryParam from url :{} , exception:{}", queryString, e);
        }
        return queryParams;
    }

}
