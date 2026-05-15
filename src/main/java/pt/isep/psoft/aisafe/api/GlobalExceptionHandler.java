package pt.isep.psoft.aisafe.api; // Fica na pasta da API, junto aos Controllers

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import pt.isep.psoft.aisafe.domain.exceptions.InvalidCredentialsException;

import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    // 1. Apanha a tua exceção customizada de login falhado (Devolve 401 Unauthorized)
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Object> handleInvalidCredentials(InvalidCredentialsException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", ex.getMessage())); // Devolve a mensagem "Invalid credentials"
    }

    // 2. Apanha o erro do Spring quando um utilizador não tem permissão (Devolve 403 Forbidden)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", "Access denied: you don't have enough permissions."));
    }

    // (Opcional) Apanhar Argumentos Inválidos (ex: NullPointers ou asserts do domínio)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }
}