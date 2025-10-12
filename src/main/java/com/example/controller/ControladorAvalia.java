package com.example.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ControladorAvalia {

	 @GetMapping("/login")
	    public String login() {
	        // Retorna o nome do template sem extensão
	        return "login";
	    }
	}
