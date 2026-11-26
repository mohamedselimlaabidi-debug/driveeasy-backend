package tn.driveeasy.vehicule.exception;

/** Donnée invalide (400). */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
