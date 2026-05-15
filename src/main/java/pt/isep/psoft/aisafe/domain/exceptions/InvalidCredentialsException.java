package pt.isep.psoft.aisafe.domain.exceptions;

public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() {
        super("Invalid credentials");
    }
}