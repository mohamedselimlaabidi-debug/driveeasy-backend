package tn.driveeasy.vehicule.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Traduit les exceptions en réponses JSON normalisées (RFC 7807).
 * La classe parente gère les erreurs standard de Spring MVC (400, 405, 415...).
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail introuvable(ResourceNotFoundException ex) {
        return probleme(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    public ProblemDetail conflit(ConflictException ex) {
        return probleme(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(BadRequestException.class)
    public ProblemDetail requeteInvalide(BadRequestException ex) {
        return probleme(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(ServiceUnavailableException.class)
    public ProblemDetail indisponible(ServiceUnavailableException ex) {
        return probleme(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail erreurInterne(Exception ex) {
        log.error("Erreur non prévue", ex);
        return probleme(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur interne du serveur");
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Map<String, String> erreurs = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> erreurs.put(e.getField(), e.getDefaultMessage()));
        ProblemDetail detail = ex.getBody();
        detail.setDetail("Données invalides");
        detail.setProperty("erreurs", erreurs);
        return handleExceptionInternal(ex, detail, headers, status, request);
    }

    private ProblemDetail probleme(HttpStatus status, String message) {
        return ProblemDetail.forStatusAndDetail(status, message);
    }
}
