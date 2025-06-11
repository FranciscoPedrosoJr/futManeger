package com.futmaneger.presentation.response;

import com.futmaneger.domain.entity.Tecnico;

public class TecnicoResponse {
    private Long id;
    private String nome;
    private String email;

    public TecnicoResponse(Long id, String nome, String email) {
        this.id = id;
        this.nome = nome;
        this.email = email;
    }

    public TecnicoResponse(Tecnico tecnico) {
        this.id = tecnico.getId();
        this.nome = tecnico.getNome();
        this.email = tecnico.getEmail();
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }
}