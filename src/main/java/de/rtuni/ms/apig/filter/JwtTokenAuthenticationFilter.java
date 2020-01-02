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

import de.rtuni.ms.apig.config.JwtConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

/**
 * Filter class for authentication of the provided JSON web token.
 * 
 * @author Julian
 */
public class JwtTokenAuthenticationFilter extends OncePerRequestFilter {
    //----------------------------------------------------------------------------------------------

    /** The <code>JwtConfig</code> for the json web token. */
    private final JwtConfig jwtConfig;

    //----------------------------------------------------------------------------------------------

    /**
     * Set the given <code>JwtConfig</code> for the token.
     * 
     * @param config The stated configuration
     */
    public JwtTokenAuthenticationFilter(final JwtConfig config) { jwtConfig = config; }

    //----------------------------------------------------------------------------------------------

    /**
     * If a token is supplied by the user the token will be decrypt and the user will be set as
     * currently authenticated user. That includes the authorities which were granted to the
     * user by the authentication service. If there is no supplied token the next filter will be
     * executed.
     * <p>
     * {@inheritDoc}
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {
        
        // Gets the authentication header.
        String bearerToken = request.getParameter("access_token");
        // Validate the header and check the prefix.
        if (bearerToken == null || !bearerToken.startsWith(jwtConfig.getPrefix())) {
            // If no token is provided the user is not authenticated
            // and we continue with the next filter. 
            // Thats okay because maybe the user is accessing a public path.
            chain.doFilter(request, response); // If not valid, go to the next filter.
            
            return;
        }

        // Removes the bearer substring from the authentication header.
        String token = bearerToken.replace(jwtConfig.getPrefix(), "");

        // Note that exceptions can be triggered when creating claims, e.g if the token has expired.
        try { 
            // Sets secret and decrypt the token.
            Claims claims = Jwts.parser().setSigningKey(jwtConfig.getSecret().getBytes())
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
                
                // Set the user as new authenticated user.
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        } catch (Exception e) {
            // In case of failure. Make sure user won't be authenticated.
            SecurityContextHolder.clearContext();
        }
        
        chain.doFilter(request, response);
    }
    
    //----------------------------------------------------------------------------------------------
}
