package com.example.model;

import jakarta.persistence.*;

@Entity
@Table(name = "simulados_questoes") 
public class SimuladoQuestao {

    @EmbeddedId 
    private SimuladoQuestaoId id;

    @ManyToOne
    @MapsId("idSimulado") 
    @JoinColumn(name = "id_simulado")
    private Simulado simulado;

    @ManyToOne
    @MapsId("idQuestao") 
    @JoinColumn(name = "id_questao")
    private Questao questao;

    
}