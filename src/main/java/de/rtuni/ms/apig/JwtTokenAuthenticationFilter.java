/*
 * Copyright 2019 (C) by Julian Horner.
 * All Rights Reserved.
 */

package de.rtuni.ms.apig;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

/**
 * Filter class for authentication of the provided JSON web token.
 * 
 * @author Julian
 */
public class JwtTokenAuthenticationFilter extends OncePerRequestFilter {
    //----------------------------------------------------------------------------------------------

    /** The configuration for the json web token. */
    private final JwtConfig jwtConfig;

    //----------------------------------------------------------------------------------------------

    /**
     * Set the given configuration for the token.
     * 
     * @param config The stated configuration
     */
    public JwtTokenAuthenticationFilter(final JwtConfig config) { jwtConfig = config; }

    //----------------------------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {
        
        // 1. get the authentication header. 
        // Tokens are supposed to be passed in the authentication header.
        String header = request.getHeader(jwtConfig.getHeader());
        // 2. validate the header and check the prefix
        if (header == null || !header.startsWith(jwtConfig.getPrefix())) {
            chain.doFilter(request, response); // If not valid, go to the next filter.
            
            return;
        }

        /*
         * If no token is provided, the user is not authenticated. That is okay. Maybe the user
         * accessing a public path or asking for a token. All secured paths that needs a token are
         * already defined and secured in SecurityConfiguration class. If the user tried to access
         * without access token, then he won't be authenticated and an exception will be thrown.
         */
        
        // 3. Get the token
        String token = header.replace(jwtConfig.getPrefix(), "");
        try { // Exceptions can be triggered when creating claims, e.g if the token has expired
            // 4. Validate the token
            Claims claims = Jwts.parser().setSigningKey(jwtConfig.getSecret().getBytes())
                    .parseClaimsJws(token).getBody();
            String username = claims.getSubject();
            if (username != null) {
                @SuppressWarnings("unchecked")
                List<String> authorities = (List<String>) claims.get("authorities");
                
                // 5. Create auth object
                
                /*
                 * UsernamePasswordAuthenticationToken: A built-in object, used by spring to
                 * represent the current authenticated / being authenticated user.
                 * It needs a list of authorities, which has type of GrantedAuthority interface,
                 * where SimpleGrantedAuthority is an implementation of that interface
                 */
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                username, null, authorities.stream().
                                map(SimpleGrantedAuthority::new).collect(Collectors.toList()));
                
                // 6. Authenticate the user
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        } catch (Exception e) {
            // In case of failure. Make sure it's clear; so guarantee user won't be authenticated
            SecurityContextHolder.clearContext();
        }
        // go to the next filter in the filter chain
        chain.doFilter(request, response);
    }
    
    //----------------------------------------------------------------------------------------------
}