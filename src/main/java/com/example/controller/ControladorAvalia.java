package com.example.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class ControladorAvalia {

	 @GetMapping("/login")
	    public String login() {
	        // Retorna o nome do template sem extensão
	        return "login";
	    }

		@GetMapping("/coordenador")
		public String coordenador() {
			return "coordenador";
		}

		@GetMapping("/cadastro")
		public String cadastro() {
			return "cadastro";
		}
		
	}
