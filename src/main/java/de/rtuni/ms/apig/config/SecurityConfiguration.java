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

import de.rtuni.ms.apig.filter.JwtTokenAuthenticationFilter;

/**
 * Class that enables custom security configuration.
 * 
 * @author Julian
 */
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
    //----------------------------------------------------------------------------------------------

    /** The <code>JwtConfig</code> for the json web token. */
    @Autowired
    private JwtConfig jwtConfig;

    //----------------------------------------------------------------------------------------------

    /**
     * Overrides the default security configuration.
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
            // make sure we use stateless session; session won't be used to store user's state.
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
            
            // Add a filter to validate the tokens with every request.
            .addFilterAfter(new JwtTokenAuthenticationFilter(jwtConfig),
                    UsernamePasswordAuthenticationFilter.class)
            
            .authorizeRequests()
            .antMatchers("/auth/**").permitAll()
            // Anyone who is trying to access the securedPage must be an ADMIN.
            // TODO can we change the path to /securedPage?
            .antMatchers("/securedPage/**").hasRole("ADMIN")
            // Permit default path. 
            .antMatchers("/login").permitAll().and()
            // Configures where to forward if authentication is required.
            .formLogin().loginPage("/login")
            // Configures url for processing of login data.
            .loginProcessingUrl("process_login") // TODO can we remove this?
            // Configures where to go if there is no previous visited page.
            .defaultSuccessUrl("/", true).and()
            // Configures url for processing of logout.
            .logout().logoutUrl("/process_logout")
            .deleteCookies("JSESSIONID"); // TODO i think we can remove this
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
