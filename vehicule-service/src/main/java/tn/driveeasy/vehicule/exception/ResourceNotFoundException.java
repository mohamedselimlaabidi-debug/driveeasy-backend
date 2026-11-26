package tn.driveeasy.vehicule.exception;

/** Ressource inexistante (404). */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
