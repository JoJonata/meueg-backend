package br.ueg.meueg.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST) // Mapeia esta exceção para o status 400
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}