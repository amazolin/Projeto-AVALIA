package com.example.controller;

import com.example.service.DisciplinaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller 
public class QuestaoController {

    @Autowired
    private DisciplinaService disciplinaService; 


    @GetMapping("/questoes") 
    public String exibirCadastroQuestao(Model model) {
        
        // Carrega a lista do BD e a insere no Model 
        model.addAttribute("listaDisciplinas", disciplinaService.findAll()); 
        
        // Retorna o template questoes.html
        return "questoes"; 
    }
}