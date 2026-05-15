package pt.isep.psoft.aisafe.domain.exceptions;

public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() {
        // Intencionalmente vago, exatamente como no acetato do professor!
        super("Invalid credentials");
    }
}