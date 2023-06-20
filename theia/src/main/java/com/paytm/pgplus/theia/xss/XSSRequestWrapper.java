package com.paytm.pgplus.theia.xss;

import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.utils.ConfigurationUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.paytm.pgplus.theia.constants.TheiaConstant.FF4J.THEIA_CHECK_XSS_BLOCKED_PATHS;

public class XSSRequestWrapper extends HttpServletRequestWrapper {
    private static final Logger XSS_LOGGER = LoggerFactory.getLogger("XSS_LOGGER");

    private static final String XSS_BLOCKED_PATHS = "XSS_BLOCKED_PATHS";

    private static Ff4jUtils ff4jUtils;

    public void init() {
        try {
            if (ff4jUtils == null) {
                ServletContext servletContext = getServletContext();
                WebApplicationContext webApplicationContext = WebApplicationContextUtils
                        .getWebApplicationContext(servletContext);
                ff4jUtils = webApplicationContext.getBean(Ff4jUtils.class);
            }
        } catch (Exception ex) {
            XSS_LOGGER.error("Error while initializing ", ex);
        }
    }

    public XSSRequestWrapper(HttpServletRequest request) {
        super(request);
        init();
    }

    private static final Pattern[] patterns = new Pattern[] {
            // javascript:... , vbscript:...
            Pattern.compile("(javascript|vbscript)", Pattern.CASE_INSENSITIVE),

            // Script, Style fragments (eg : <script>anything</script>)
            Pattern.compile("(<style>|<script>)((.|[\\r\\n])*?)(</style>|</script>){0,10}", Pattern.CASE_INSENSITIVE),
            // src='...', href='...', style='...'
            Pattern.compile(
                    "([^a-zA-Z0-9]|^)(style|href|src)((\\s|[\\r\\n])*?)=(\\s|[\\r\\n])*(\\'|\\\")((.|[\\r\\n])*?)(\\'|\\\"){0,10}",
                    Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            // lonely script and style tags
            Pattern.compile("</script>|</style>", Pattern.CASE_INSENSITIVE),
            Pattern.compile("<script(.*?)|<style(.*?){0,10}", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE
                    | Pattern.DOTALL),
            // eval(...), expression(...) ,alert(...) ,prompt(...)
            Pattern.compile("(eval|expression|alert|prompt)\\((.*?)\\){0,10}", Pattern.CASE_INSENSITIVE
                    | Pattern.MULTILINE | Pattern.DOTALL),
            // on...(...)=...
            // Pattern.compile("on((.|[\\r\\n])*?)=((.|[\\r\\n])*?)[\\'|\\\"]((.|[\\r\\n])*?)[\\'|\\\"]{0,10}",Pattern.CASE_INSENSITIVE
            // | Pattern.MULTILINE | Pattern.DOTALL),
            // body, img, iframe, input, link, table, td, div, a ,svg tag
            Pattern.compile(
                    "<(meta|html|body|iframe|img|input|link|object|a|div|td|th|span|svg)((.|[\\r\\n])*)>{0,10}",
                    Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            // html tags
            // Pattern.compile("<((.|[\\r\\n])*?)>((.|[\\r\\n])*?)</((.|[\\r\\n])*)>{0,10}",
            // Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            // CDATA
            Pattern.compile("<!\\[CDATA((.|[\\r\\n])*?)\\]>{0,10}", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE
                    | Pattern.DOTALL),
            // everything between < & > if escaped from above patterns
            Pattern.compile("<((.|[\\r\\n])*)(>|\\\"){0,10}", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE
                    | Pattern.DOTALL) };

    private static final Pattern[] strictPatterns = new Pattern[] {
            // DOM (eg : document ,attr. etc)
            Pattern.compile(
                    "([^a-zA-Z0-9]|^)(document|attr|element|nodemap|nodelist|entity|namemap)([^a-zA-Z0-9])(.*?)[\\s|=|)]{0,1000}",
                    Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            // '...' and "..."
            Pattern.compile("(\\'|\\\")((.|[\\r\\n])*?)(\\'|\\\")", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE
                    | Pattern.DOTALL),
            // All characters given below < " ' etc
            // characters
            Pattern.compile("<|>|\\[|\\]|&|\"|'|%|\\(|\\)|\\+", Pattern.MULTILINE | Pattern.DOTALL)

    };

    private static final Pattern[] PATTERNS_ALL = ArrayUtils.addAll(patterns, strictPatterns);

    private static final Pattern CHECKSUM_PATTERN = Pattern.compile("(\"CheckSum\":\".+?\")", Pattern.CASE_INSENSITIVE
            | Pattern.MULTILINE | Pattern.DOTALL);

    public boolean isMatch() {
        if (isUrlBlocked(super.getRequestURI()))
            return true;

        Map<String, String[]> params = super.getParameterMap();

        if (MapUtils.isEmpty(params) || CollectionUtils.isEmpty(params.keySet())) {
            return false;
        }

        for (String key : params.keySet()) {

            // ignoring this key because of stackoverflow error
            if ("/theia/process/mandate".equals(this.getRequestURI()) && key.equals("MandateRespDoc")) {
                continue;
            }

            String value = params.get(key) != null ? params.get(key)[0] : null;

            if (StringUtils.isBlank(value)) {
                continue;
            }

            value = value.replace("|", "");
            value = value.replace("*", "");
            value = value.replaceAll("\n", "");

            if ("(null)".equals(value)
                    && (key.equals(TheiaConstant.RequestParams.Native.ORDER_ID) || key
                            .equals(TheiaConstant.RequestParams.ORDER_ID))) {
                continue;
            }

            Pattern[] patternsToApply;

            if (TheiaConstant.RequestParams.ORDER_ID.equals(key) || TheiaConstant.RequestParams.MID.equals(key)
                    || TheiaConstant.RequestParams.Native.ORDER_ID.equals(key)) {
                value = decode(value);
                patternsToApply = PATTERNS_ALL;
            } else {
                patternsToApply = patterns;
            }

            String sanitizedValue = sanitizeData(key, value);

            if (checkPattern(sanitizedValue, patternsToApply)) {
                XSS_LOGGER.warn("ERR_BLOCKED_BY_XSS_AUDITOR : KEY : {} & VALUE : {} ", key,
                        value.replaceAll("SSOToken.:.[a-zA-Z0-9-]+", "SSOToken\":\"******"));
                XSS_LOGGER.warn("Request: {}", this.getRequestURI());
                return true;
            }
        }
        return false;
    }

    public static String decode(String value) {
        if (StringUtils.isBlank(value)) {
            return value;
        }
        try {
            return URLDecoder.decode(value, "UTF-8");
        } catch (Exception e) {
            XSS_LOGGER.error("Exception in URL decoding", e);
            return value;
        }
    }

    private static boolean checkPattern(String value, Pattern[] patterns) {
        // Remove all sections that match a pattern
        Matcher matcher = null;
        for (Pattern scriptPattern : patterns) {
            matcher = scriptPattern.matcher(value);
            if (matcher.find()) {
                XSS_LOGGER.info("Pattern applied!" + scriptPattern + " $$ "
                        + value.replaceAll("SSOToken.:.[a-zA-Z0-9-]+", "SSOToken\":\"******") + " $$ Pattern Applied");
                return true;
            }
        }

        return false;
    }

    private String sanitizeData(String key, String value) {
        String sanitizedData = value;

        switch (key) {
        case "riskExtendedInfo":
            sanitizedData = value.replace("<unknown ssid>", "");
            break;
        case "orderid":
            sanitizedData = value.replace("<Order_ID?orderid=", "");
            break;
        }

        Matcher matcher = CHECKSUM_PATTERN.matcher(sanitizedData);

        if (matcher.find()) {
            sanitizedData = matcher.replaceFirst("\"CheckSum\":\"\"");
        }

        return sanitizedData;
    }

    private boolean isUrlBlocked(String url) {
        String urls = ConfigurationUtil.getProperty(XSS_BLOCKED_PATHS, StringUtils.EMPTY);
        if (StringUtils.isNotBlank(urls) && StringUtils.isNotBlank(url)) {
            if (ff4jUtils.isFeatureEnabled(THEIA_CHECK_XSS_BLOCKED_PATHS, false)) {
                List<String> blockedUrls = Arrays.asList(urls.split(","));
                return blockedUrls.contains(url);
            }
        }
        return false;
    }

}