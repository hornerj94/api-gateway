/*
 * Copyright 2019 (C) by Julian Horner.
 * All Rights Reserved.
 */

package de.rtuni.ms.apig;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;

/**
 * Class for starting an api gateway as a zuul server.
 * 
 * @author Julian
 *
 */
@SpringBootApplication
@EnableEurekaClient
@EnableZuulProxy
public class Application {
    //---------------------------------------------------------------------------------------------
    
    /**
     * Start the application.
     * 
     * @param args The arguments
     */
    public static void main(String[] args) { SpringApplication.run(Application.class, args); }

    //---------------------------------------------------------------------------------------------
}
