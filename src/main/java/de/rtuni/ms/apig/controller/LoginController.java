/*
 * Copyright 2019 (C) by Julian Horner.
 * All Rights Reserved.
 */

package de.rtuni.ms.apig.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Class that handles all kind of requests.
 * 
 * @author Julian
 */
@Controller
public class LoginController {
    //----------------------------------------------------------------------------------------------

    /**
     * Catch the request for the login page and returns the name of the corresponding template.
     * 
     * @return The name of the template
     */
    @RequestMapping("/login")
    public String login() {
        return "login";
    }

    //----------------------------------------------------------------------------------------------
}