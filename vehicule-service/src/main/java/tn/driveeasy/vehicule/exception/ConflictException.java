package tn.driveeasy.vehicule.exception;

/** Conflit avec l'état actuel (409). */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
