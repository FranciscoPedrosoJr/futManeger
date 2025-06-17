package com.futmaneger.application.exception;

import org.springframework.http.HttpStatus;

public class NenhumClubeCadastradoException extends RuntimeException {
    private final HttpStatus status;

    public NenhumClubeCadastradoException(String mensagem) {
        super(mensagem);
        this.status = HttpStatus.NOT_FOUND;
    }

    public HttpStatus getStatus() {
        return status;
    }
}