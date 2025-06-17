package com.futmaneger.infrastructure.handler;

import com.futmaneger.application.exception.AutenticacaoException;
import com.futmaneger.application.exception.NenhumClubeCadastradoException;
import java.net.URI;
import java.time.OffsetDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(AutenticacaoException.class)
    public ProblemDetail handleAutenticacaoException(AutenticacaoException ex, jakarta.servlet.http.HttpServletRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
        problemDetail.setTitle("Erro de autenticação");
        problemDetail.setDetail(ex.getMessage());
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setProperty("timestamp", OffsetDateTime.now());

        return problemDetail;
    }

    @ExceptionHandler(NenhumClubeCadastradoException.class)
    public ProblemDetail handleNenhumClubeCadastradoException(NenhumClubeCadastradoException ex, jakarta.servlet.http.HttpServletRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problemDetail.setTitle("Não encontrado");
        problemDetail.setDetail(ex.getMessage());
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setProperty("timestamp", OffsetDateTime.now());

        return problemDetail;
    }
}
