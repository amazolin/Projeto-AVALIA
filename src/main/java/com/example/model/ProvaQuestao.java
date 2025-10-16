package com.example.model;

import jakarta.persistence.*;

@Entity
@Table(name = "provas_questoes") 
public class ProvaQuestao {

    @EmbeddedId 
    private ProvaQuestaoId id;

    
    @ManyToOne
    @MapsId("idProva") 
    @JoinColumn(name = "id_prova") 
    private Prova prova;

    
    @ManyToOne
    @MapsId("idQuestao") 
    @JoinColumn(name = "id_questao")
    private Questao questao;

   
}