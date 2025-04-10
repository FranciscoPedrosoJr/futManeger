package com.futmaneger.domain.entity;

public class Tecnico {
    private Long id;
    private String nome;
    private String email;
    private String senha;

    public Tecnico(Long id, String nome, String email, String senha) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
    }

    public Tecnico(String nome, String email, String senha) {
        this(null, nome, email, senha);
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

    public String getSenha() {
        return senha;
    }

    public void setId(Long id) {
        this.id = id;
    }
}