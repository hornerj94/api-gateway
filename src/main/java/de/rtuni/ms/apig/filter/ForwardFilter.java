/*
 * Copyright 2019 (C) by Julian Horner.
 * All Rights Reserved.
 */

package de.rtuni.ms.apig.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filter class which sets the last requested path to the response before login or auth.
 * 
 * @author Julian
 *
 */
public class ForwardFilter extends OncePerRequestFilter {
    //---------------------------------------------------------------------------------------------

    /** The login path. */
    private static final String LOGIN_PATH = "/login";
    
    /** The auth path. */
    private static final String AUTH_PATH = "/auth";

    //=============================================================================================

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        boolean isLoginPath = LOGIN_PATH.equals(request.getServletPath());
        boolean isAuthPath = AUTH_PATH.equals(request.getServletPath());

        if (!(isLoginPath || isAuthPath)) {
            Cookie forwardPageCookie = new Cookie("forwardPage", request.getServletPath());
            // Adds the last requested path to the response before login or auth.
            response.addCookie(forwardPageCookie);
        }

        filterChain.doFilter(request, response);
    }

    //---------------------------------------------------------------------------------------------
}
