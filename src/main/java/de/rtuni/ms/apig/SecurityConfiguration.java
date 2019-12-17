/*
 * Copyright 2019 (C) by Julian Horner.
 * All Rights Reserved.
 */

package de.rtuni.ms.apig;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Class that handles security configuration.
 * 
 * @author Julian
 */
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
    //----------------------------------------------------------------------------------------------

    /** The configuration for the json web token. */
    @Autowired
    private JwtConfig jwtConfig;

    //----------------------------------------------------------------------------------------------

    /**
     * Overrides the default configuration.
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
            // make sure we use stateless session; session won't be used to store user's state.
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
            // handle an authorized attempts 
            .exceptionHandling().authenticationEntryPoint(
                    (req, rsp, e) -> rsp.sendError(HttpServletResponse.SC_UNAUTHORIZED)).and()
            // Add a filter to validate the tokens with every request
            .addFilterAfter(new JwtTokenAuthenticationFilter(jwtConfig),
                    UsernamePasswordAuthenticationFilter.class)
            // authorization requests config
            .authorizeRequests()
            // allow all who are accessing "auth" service
            .antMatchers(HttpMethod.POST, jwtConfig.getUri()).permitAll()
            // must be an admin if trying to access secured page (authentication is also required)
            .antMatchers("/securedPage/**").hasRole("ADMIN")
            // Any other request must be authenticated
            .anyRequest().authenticated();
    }

    //----------------------------------------------------------------------------------------------

    /**
     * Get a new <code>JwtConfig</code>.
     * 
     * @return The stated configuration
     */
    @Bean
    public JwtConfig jwtConfig() {
        return new JwtConfig();
    }

    //----------------------------------------------------------------------------------------------
}
