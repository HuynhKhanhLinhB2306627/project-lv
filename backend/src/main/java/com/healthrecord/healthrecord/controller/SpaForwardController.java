package com.healthrecord.healthrecord.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaForwardController {

    @GetMapping({
            "/", "/home", "/login", "/register", "/dashboard",
            "/appointments", "/medications", "/ai-prediction",
            "/forum", "/chat", "/documents", "/vaccinations",
            "/insurance", "/body-metrics", "/facilities-map",
            "/ai-consulting", "/profiles", "/admin", "/profiles/{id}"
    })
    public String forwardToSpa() {
        return "forward:/index.html";
    }
}
