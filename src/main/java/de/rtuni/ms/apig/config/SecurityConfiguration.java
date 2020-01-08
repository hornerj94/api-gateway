/*
 * Copyright 2019 (C) by Julian Horner.
 * All Rights Reserved.
 */

package de.rtuni.ms.apig.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import de.rtuni.ms.apig.filter.ForwardFilter;
import de.rtuni.ms.apig.filter.JWTAuthenticationFilter;

/**
 * Class that handles several security configurations.
 * 
 * @author Julian
 */
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
    //---------------------------------------------------------------------------------------------

    /** The <code>JwtConfiguration</code>. */
    @Autowired
    private JWTConfiguration jwtConfiguration;

    //---------------------------------------------------------------------------------------------

    /**
     * Get a new <code>JwtConfiguration</code>.
     * 
     * @return The stated JWT configuration
     */
    @Bean
    public JWTConfiguration jwtConfig() { return new JWTConfiguration(); }

    //---------------------------------------------------------------------------------------------

    /**
     * Configure custom security configurations.
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
        // Use stateless sessions.
        .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
        
        // Add filter to set passed cookie.
        .addFilterBefore(new ForwardFilter(), UsernamePasswordAuthenticationFilter.class)
        // Add filter to validate tokens with every request.
        .addFilterAfter(new JWTAuthenticationFilter(jwtConfiguration),
                UsernamePasswordAuthenticationFilter.class)
 
        .authorizeRequests()
        // Permit only users with ADMIN role.
        .antMatchers("/securedPage/**").hasRole("ADMIN")
        // Permit auth and login path for sending credentials. 
        .antMatchers("/auth/**", "/login").permitAll().and()
        // Configures where to forward if authentication is required.
        .formLogin().loginPage("/login");
    }

    //---------------------------------------------------------------------------------------------
}
