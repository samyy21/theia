package com.paytm.pgplus.theia.nativ.filter;

import org.apache.commons.io.IOUtils;
import org.springframework.web.util.ContentCachingRequestWrapper;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class CustomContentCachingRequestWrapper extends ContentCachingRequestWrapper {
    private byte[] body;

    public CustomContentCachingRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        super.getParameterMap(); // init cache in ContentCachingRequestWrapper
        body = super.getContentAsByteArray(); // first option for
        // application/x-www-form-urlencoded
        if (body != null && body.length == 0) {
            try {
                body = IOUtils.toByteArray(super.getInputStream()); // second
                // option
                // for other
                // body
                // formats
            } catch (IOException ex) {
                body = new byte[0];
            }
        }
    }

    public byte[] getBody() {
        return body;
    }

    @Override
    public ServletInputStream getInputStream() {
        return new RequestCachingInputStream(body);
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getInputStream(), getCharacterEncoding()));
    }

    private static class RequestCachingInputStream extends ServletInputStream {
        private final ByteArrayInputStream inputStream;

        public RequestCachingInputStream(byte[] bytes) {
            inputStream = new ByteArrayInputStream(bytes);
        }

        @Override
        public int read() throws IOException {
            return inputStream.read();
        }
    }
}
