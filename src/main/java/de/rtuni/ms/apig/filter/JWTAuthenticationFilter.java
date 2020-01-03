/*
 * Copyright 2019 (C) by Julian Horner.
 * All Rights Reserved.
 */

package de.rtuni.ms.apig.filter;

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

import de.rtuni.ms.apig.config.JWTConfiguration;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

/**
 * Filter class for authentication of the user via the JWT.
 * 
 * @author Julian
 */
public class JWTAuthenticationFilter extends OncePerRequestFilter {
    //---------------------------------------------------------------------------------------------

    /** The <code>JwtConfiguration</code>. */
    private JWTConfiguration jwtConfiguration;

    //---------------------------------------------------------------------------------------------

    /**
     * Constructor that sets the given <code>JwtConfiguration</code>.
     * 
     * @param config The stated configuration
     */
    public JWTAuthenticationFilter(final JWTConfiguration config) { jwtConfiguration = config; }

    //---------------------------------------------------------------------------------------------

    /**
     * If a token is supplied by the user the token will be decrypted and the user will be set as
     * currently authenticated user. That includes the authorities which were granted to the
     * user by the auth service. If there is no supplied token the next filter will be
     * executed.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {
        // Gets the access_token parameter.
        String bearerToken = request.getParameter("access_token");
        // Validate the header and check the prefix.
        if (bearerToken == null || !bearerToken.startsWith(jwtConfiguration.getPrefix())) {
            // If there's no token the user isn't authenticated and we execute the next filter. 
            chain.doFilter(request, response); // If not valid, go to the next filter.
            
            return;
        }
        // Removes the bearer substring from the authentication header.
        String token = bearerToken.replace(jwtConfiguration.getPrefix(), "");

        // Exceptions can be triggered when creating claims, e.g if the token has expired.
        try { 
            // Sets secret and decrypts the token.
            Claims claims = Jwts.parser().setSigningKey(jwtConfiguration.getSecret().getBytes())
                    .parseClaimsJws(token).getBody();
            
            String username = claims.getSubject();
            if (username != null) {
                // Gets the authorities which were added to the token by the auth-service.
                @SuppressWarnings("unchecked")
                List<String> authorities = (List<String>) claims.get("authorities");
               
                // Create an UsernamePasswordAuthenticationToken which represents the 
                // authenticated user or the user who is being authenticated currently.
                //
                // Because we need a list of authorities, which are from the type GrantedAuthority
                // we have to convert the Strings to SimpleGrantedAuthority which is an 
                // implementation.
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        username, null, authorities.stream()
                                .map(SimpleGrantedAuthority::new).collect(Collectors.toList()));
                
                // Sets user as the currently authenticated.
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        } catch (Exception e) {
            // In case of failure make sure user won't be authenticated.
            SecurityContextHolder.clearContext();
        }
        
        chain.doFilter(request, response);
    }
    
    //---------------------------------------------------------------------------------------------
}
