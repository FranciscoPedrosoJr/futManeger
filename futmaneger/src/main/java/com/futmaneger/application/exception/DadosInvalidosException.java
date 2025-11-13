package com.futmaneger.application.exception;

import org.springframework.http.HttpStatus;

public class DadosInvalidosException extends RuntimeException{
    private final HttpStatus status;

    public DadosInvalidosException(String mensagem) {
        super(mensagem);
        this.status = HttpStatus.BAD_REQUEST;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
