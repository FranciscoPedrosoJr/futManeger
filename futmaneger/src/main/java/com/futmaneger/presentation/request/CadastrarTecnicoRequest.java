package com.futmaneger.presentation.request;

public class CadastrarTecnicoRequest {
    private String nome;
    private String email;
    private String senha;

    public CadastrarTecnicoRequest() {}

    public CadastrarTecnicoRequest(String nome, String email, String senha) {
        this.nome = nome;
        this.email = email;
        this.senha = senha;
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
}