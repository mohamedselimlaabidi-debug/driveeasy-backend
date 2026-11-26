package tn.driveeasy.location.exception;

/** Service distant injoignable (503). */
public class ServiceUnavailableException extends RuntimeException {
    public ServiceUnavailableException(String message) {
        super(message);
    }
}
