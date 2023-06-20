package com.paytm.pgplus.theia.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class PageDispatcherServlet extends HttpServlet {

    /**
     *
     */
    private static final long serialVersionUID = 1909703433456657032L;

    private static final List<String> forbiddenCharacters = new ArrayList<>(Arrays.asList("<", ">", "`", ")", "(",
            "$.ajax(", "$(document", "&lt;", "&gt;"));

    private static final Logger LOGGER = LoggerFactory.getLogger(PageDispatcherServlet.class);

    private static final String VIEW_BASE = "/WEB-INF/views/jsp/";

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String pathInfo = request.getPathInfo();
        response.setContentType("text/html");

        /** All the security check only handled by XSSFilter */
        if (checkForScriptedParams(request)) {
            LOGGER.info("Dirty Request on PageDispatcherServlet redirecting to error page");
            request.getRequestDispatcher(VIEW_BASE + "error.jsp").forward(request, response);
        } else {
            String url = "/WEB-INF/views/jsp" + pathInfo;
            try {
                request.getRequestDispatcher(url).forward(request, response);
            } catch (Exception e) {
                LOGGER.warn("exception occured while forwarding request");
                throw e;
            }
        }
    }

    /* To block Jsps where params include dirty values/xss/script- Security fix */
    private boolean checkForScriptedParams(HttpServletRequest request) {
        for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
            String value = entry.getValue()[0];
            for (String forbiddenCharacter : forbiddenCharacters) {
                if (value.contains(forbiddenCharacter)) {
                    String errorMessage = new StringBuilder().append("Parameter named ").append(entry.getKey())
                            .append(" is a suspect for XSS. Value received : ").append(value)
                            .append(", Suspect Char received : ").append(forbiddenCharacter).toString();
                    LOGGER.error(errorMessage);
                    return true;
                }
            }
        }
        return false;
    }
}