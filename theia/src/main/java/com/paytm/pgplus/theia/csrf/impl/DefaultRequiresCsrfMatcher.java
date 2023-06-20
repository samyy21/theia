package com.paytm.pgplus.theia.csrf.impl;

import com.google.common.collect.ImmutableSet;
import com.paytm.pgplus.theia.csrf.RequestMatcher;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by ankitsinghal on 22/05/17.
 */
@Component
public class DefaultRequiresCsrfMatcher implements RequestMatcher {

    private final ImmutableSet<String> allowedMethods = new ImmutableSet.Builder<String>().add("GET").add("HEAD")
            .add("TRACE").add("OPTIONS").build();

    @Override
    public boolean matches(HttpServletRequest request) {
        return !this.allowedMethods.contains(request.getMethod());
    }
}