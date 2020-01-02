/*
 * Copyright 2019 (C) by Julian Horner.
 * All Rights Reserved.
 */

package de.rtuni.ms.apig.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Login controller that handles all requests for the login path.
 * 
 * @author Julian
 */
@Controller
public class LoginController {
    //---------------------------------------------------------------------------------------------

    /**
     * Catch the request for the default login page and return the name of the corresponding
     * template.
     * 
     * @return The name of the template to show
     */
    @RequestMapping("/login")
    public String login() {
        return "login";
    }

    //---------------------------------------------------------------------------------------------
}