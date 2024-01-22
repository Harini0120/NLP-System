package com.casestudy.app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class maincontroller {
	

@GetMapping("/")
    public String showMainPage() {
        return "main"; // Assuming your Thymeleaf template is named "mainPage.html"
}
}