package com.paytm.pgplus.theia.nativ.filter;

import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.logging.ExtendedLogger;
import com.paytm.pgplus.theia.filter.MultiReadHttpServletRequestWrapper;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.FilterChain;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import static com.paytm.pgplus.biz.utils.BizConstant.Ff4jFeature.THEIA_ENABLE_RESPONSE_COMPRESSION;
import static com.paytm.pgplus.biz.utils.BizConstant.Ff4jFeature.THEIA_FPO_RESPONSE_COMPRESSION;

public class ResponseCompressionFilter extends OncePerRequestFilter {
    private static Ff4jUtils ff4jUtils;
    private static final Logger LOGGER = LoggerFactory.getLogger(ResponseCompressionFilter.class);
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(ResponseCompressionFilter.class);

    public void init() {
        try {
            if (ff4jUtils == null) {
                ServletContext servletContext = getServletContext();
                WebApplicationContext webApplicationContext = WebApplicationContextUtils
                        .getWebApplicationContext(servletContext);
                ff4jUtils = webApplicationContext.getBean(Ff4jUtils.class);
            }
        } catch (Exception ex) {
            LOGGER.error("Error while initializing ", ex);
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        init();
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        try {
            if (!(request instanceof MultiReadHttpServletRequestWrapper)) {
                request = new MultiReadHttpServletRequestWrapper(request);
            }
        } catch (Exception ex) {
            LOGGER.error("Exception occur while Wrapping HttpServletRequest in MultiReadHttpServletWrapper ", ex);
        }

        filterChain.doFilter(request, responseWrapper);

        try {
            String encoding = getEncoding(request);

            if (StringUtils.isNotBlank(encoding) && isValidResponseCompressionType(encoding)
                    && isResponseEncodingAllowed(request)) {
                compressResponse(encoding, response, responseWrapper);
                return;
            }
        } catch (Exception ex) {
            LOGGER.error("Exception occur while Compressing Response ", ex);
        }

        response.getOutputStream().write(responseWrapper.getContentAsByteArray());
        response.getOutputStream().close();
        EXT_LOGGER.customInfo("Content Length of Response without compressing: {}", responseWrapper.getContentSize());

    }

    private String getEncoding(HttpServletRequest request) {
        if (request.getRequestURI().contains("fetchPaymentOptions")) {
            return request.getHeader("Accept-Compression");
        } else {
            return request.getHeader("Accept-Encoding");
        }
    }

    private boolean isResponseEncodingAllowed(HttpServletRequest request) {
        if (request.getRequestURI().contains("fetchPaymentOptions")) {
            String mid = request.getParameter("mid");
            return isFeatureFlagEnabled(THEIA_FPO_RESPONSE_COMPRESSION, mid);
        } else if (request.getRequestURI().contains("fetchQRPaymentDetails")) {
            String content = ((MultiReadHttpServletRequestWrapper) request).getMessageBody();
            JSONObject json = new JSONObject(content);
            JSONObject body = json.getJSONObject("body");
            if (body.has("qrCodeId")) {
                String qrCodeId = body.getString("qrCodeId");
                return isFeatureFlagEnabled(THEIA_ENABLE_RESPONSE_COMPRESSION, qrCodeId);
            }
        }
        return false;
    }

    private boolean isFeatureFlagEnabled(String ff4jKey, String val) {
        if (ObjectUtils.notEqual(ff4jUtils, null) && StringUtils.isNotBlank(val)
                && ff4jUtils.isFeatureEnabledOnMid(val, ff4jKey, true)) {
            return true;
        } else {
            return false;
        }
    }

    private void compressResponse(String encoding, HttpServletResponse response,
            ContentCachingResponseWrapper wrappedResponse) throws IOException {
        response.addHeader("Content-Type", "application/json");
        if ((encoding.indexOf("gzip") != -1)) {
            response.addHeader("Content-Encoding", "gzip");
            ExtendedGZIPOutputStream gzipOutputStream = new ExtendedGZIPOutputStream(response.getOutputStream());
            int compressionLevel = stringToValidCompressionLevel(ff4jUtils.getPropertyAsStringWithDefault(
                    "response.compression.level", "-1"));
            gzipOutputStream.setCompressionLevel(compressionLevel);
            gzipOutputStream.write(wrappedResponse.getContentAsByteArray());
            gzipOutputStream.close();
            response.getOutputStream().close();
            EXT_LOGGER.customInfo("Content Length of Response after gzip compression: {}",
                    response.getHeader("Content-Length"));
        } else if (encoding.indexOf("deflate") != -1) {
            response.addHeader("Content-Encoding", "deflate");
            response.getOutputStream().write(deflateCompression(wrappedResponse.getContentAsByteArray()));
            response.getOutputStream().close();
            EXT_LOGGER.customInfo("Content Length of Response after deflate compression: {}",
                    response.getHeader("Content-Length"));
        }
    }

    private byte[] deflateCompression(byte[] bytes) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int compressionLevel = stringToValidCompressionLevel(ff4jUtils.getPropertyAsStringWithDefault(
                "response.compression.level", "-1"));
        Deflater def = new Deflater(compressionLevel);
        DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream(byteArrayOutputStream, def);
        deflaterOutputStream.write(bytes);
        deflaterOutputStream.close();
        byte[] compressedBytes = byteArrayOutputStream.toByteArray();
        byteArrayOutputStream.close();
        return compressedBytes;
    }

    private static int stringToValidCompressionLevel(String number) {
        if (StringUtils.isNotBlank(number) && number.matches("[-+]?\\d+")) {
            int numberInt = Integer.parseInt(number);
            if (numberInt >= -1 && numberInt <= 9) {
                return numberInt;
            }
        }
        return -1;
    }

    private boolean isValidResponseCompressionType(String encoding) {
        return encoding.indexOf("gzip") != -1 || encoding.indexOf("deflate") != -1;
    }
}
