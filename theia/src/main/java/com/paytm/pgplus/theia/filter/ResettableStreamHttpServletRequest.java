package com.paytm.pgplus.theia.filter;

import org.apache.commons.io.IOUtils;

import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.TreeMap;

public class ResettableStreamHttpServletRequest extends HttpServletRequestWrapper {

    private byte[] rawData;

    private HttpServletRequest request;

    private ResettableServletInputStream servletStream;

    public static ResettableStreamHttpServletRequest resettableStreamHttpServletRequest(ServletRequest servletRequest) {
        return resettableStreamHttpServletRequest((HttpServletRequest) servletRequest);
    }

    public void resetRequestBody(String data) {
        rawData = data.getBytes();
        resetInputStream();
    }

    public static ResettableStreamHttpServletRequest resettableStreamHttpServletRequest(HttpServletRequest request) {
        if (request instanceof ResettableStreamHttpServletRequest) {
            return (ResettableStreamHttpServletRequest) request;
        }
        return new ResettableStreamHttpServletRequest(request);
    }

    private ResettableStreamHttpServletRequest(HttpServletRequest request) {
        super(request);
        this.request = request;
        this.servletStream = new ResettableServletInputStream();
    }

    public void resetInputStream() {
        servletStream.stream = new ByteArrayInputStream(rawData);
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        if (rawData == null) {
            rawData = IOUtils.toByteArray(this.request.getReader());
            servletStream.stream = new ByteArrayInputStream(rawData);
        }
        return servletStream;
    }

    @Override
    public BufferedReader getReader() throws IOException {
        if (rawData == null) {
            rawData = IOUtils.toByteArray(this.request.getReader());
            servletStream.stream = new ByteArrayInputStream(rawData);
        }
        return new BufferedReader(new InputStreamReader(servletStream));
    }

    private class ResettableServletInputStream extends ServletInputStream {

        private ByteArrayInputStream stream;

        @Override
        public int read() throws IOException {
            return stream.read();
        }

    }

    @Override
    public Enumeration<String> getParameterNames() {
        return Collections.enumeration(getParameterMap().keySet());
    }

    @Override
    public String[] getParameterValues(final String name) {
        return getParameterMap().get(name);
    }

}
