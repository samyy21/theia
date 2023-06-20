package com.paytm.pgplus.theia.controllers.helper;

import com.paytm.pgplus.theia.cache.IConfigurationDataService;
import com.paytm.pgplus.theia.controllers.ProcessTransactionController;
import com.paytm.pgplus.theia.services.impl.TransactionStatusServiceImpl;
import com.paytm.pgplus.theia.utils.ConfigurationUtil;
import org.apache.commons.lang.text.StrSubstitutor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Component
public class OldPgRedirectHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessTransactionController.class);

    private static String MERCHANT_HTML;
    private static String QUOTES = "\"";
    private static String HTML_QUOTES = "&quot;";
    private static String OLDPG_NON_MIGRATED_MID_LIST = "old.pg.non.migrated.mid.list";
    private String OLD_PG_BASE_URL = ConfigurationUtil.getProperty("old.pg.base.url");
    private String PROCESS_TRANSACTION = OLD_PG_BASE_URL + "oltp-web/processTransaction";
    private String CUSTOM_PROCESS_TRANSACTION = OLD_PG_BASE_URL + "oltp-web/customPT";
    private String OLD_PG_REDIRECT_HTML_FILENAME = "templates/old-pd-redirect.html";

    @Autowired
    @Qualifier("configurationDataService")
    private IConfigurationDataService configurationDataService;

    @PostConstruct
    public void init() {
        loadHtmlFile();
    }

    private void loadHtmlFile() {
        try {
            // LOGGER.info("loading merchant HTML");
            InputStream htmlFile = TransactionStatusServiceImpl.class.getClassLoader().getResourceAsStream(
                    OLD_PG_REDIRECT_HTML_FILENAME);
            MERCHANT_HTML = getFileContent(htmlFile);
            LOGGER.info(" merchant HTML loaded");
        } catch (Exception e) {
            LOGGER.error("something went wrong in loading merchant HTML {}", e);// this
                                                                                // exception
                                                                                // might
                                                                                // come
                                                                                // for
        }
    }

    public boolean isOldPGRequest(HttpServletRequest request) {
        if (request != null && request.getParameter("MID") != null) {

            String mid = request.getParameter("MID");
            String oldPGNonMigratedList = ConfigurationUtil.getProperty(OLDPG_NON_MIGRATED_MID_LIST);

            if (StringUtils.isNotBlank(oldPGNonMigratedList) && oldPGNonMigratedList.contains(mid)) {

                return true;
            }
        }
        return false;
    }

    private static String getFileContent(InputStream inStrem) throws IOException {
        if (null == inStrem) {
            return null;
        }
        int ch;
        StringBuffer strContent = new StringBuffer("");
        while ((ch = inStrem.read()) != -1) {
            strContent.append((char) ch);
        }
        return strContent.toString();
    }

    public void handleOldPgRedirect(HttpServletRequest request, final HttpServletResponse response,
            boolean isCustomPTRequest) throws IOException {
        // LOGGER.info("handling oldPg Request");
        String postParams = createPostParamString(request.getParameterMap(), false);
        LOGGER.info("PARAM MAP = {} ", postParams);

        if (MERCHANT_HTML == null) {
            loadHtmlFile();
        }

        Map<String, String> valuesMap = new HashMap<>();
        valuesMap.put("posthiddenparams", postParams);
        if (isCustomPTRequest) {
            if (StringUtils.isNotBlank(request.getQueryString())) {
                valuesMap.put("merurl", CUSTOM_PROCESS_TRANSACTION + "?" + request.getQueryString());
            } else {
                valuesMap.put("merurl", CUSTOM_PROCESS_TRANSACTION);
            }
        } else {
            if (StringUtils.isNotBlank(request.getQueryString())) {
                valuesMap.put("merurl", PROCESS_TRANSACTION + "?" + request.getQueryString());
            } else {
                valuesMap.put("merurl", PROCESS_TRANSACTION);
            }
        }

        StrSubstitutor strSubstitutor = new StrSubstitutor(valuesMap);
        strSubstitutor.replace(MERCHANT_HTML);
        response.getWriter().print(strSubstitutor.replace(MERCHANT_HTML));
        LOGGER.info(" handled oldPg Request");
    }

    private static String createPostParamString(Map<String, String[]> parameterMap, boolean isOverRideQotes) {
        Iterator<Map.Entry<String, String[]>> iterator = parameterMap.entrySet().iterator();
        String inputTag = "<INPUT TYPE=HIDDEN NAME=\"{X}\" VALUE=\"{Y}\">";
        StringBuilder postparams = new StringBuilder();

        while (((Iterator) iterator).hasNext()) {
            Map.Entry<String, String[]> entry = (Map.Entry<String, String[]>) iterator.next();
            String key = entry.getKey();
            String value = entry.getValue()[0];
            if (isOverRideQotes && StringUtils.isNotBlank(value) && value.contains(QUOTES)) {
                LOGGER.info("value Before replacing is: {}", value);
                value = value.replace(QUOTES, HTML_QUOTES);
                LOGGER.info("value After replacing is : {}", value);
            }
            postparams.append(rep(inputTag, key, value));
        }
        return postparams.toString();
    }

    private static String rep(String inputTag, String x, String y) {
        return inputTag.replace("{X}", x).replace("{Y}", y);
    }
}
