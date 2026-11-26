package tn.driveeasy.location.client;

import tn.driveeasy.location.exception.BadRequestException;
import tn.driveeasy.location.exception.ConflictException;
import tn.driveeasy.location.exception.ResourceNotFoundException;
import tn.driveeasy.location.exception.ServiceUnavailableException;
import feign.FeignException;

import java.util.function.Supplier;

/** Convertit les erreurs Feign en exceptions métier (gestion des erreurs distribuées). */
public final class AppelDistant {

    private AppelDistant() {
    }

    public static <T> T executer(String service, Supplier<T> appel) {
        try {
            return appel.get();
        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException("Ressource introuvable dans " + service);
        } catch (FeignException.Conflict e) {
            throw new ConflictException("Opération refusée par " + service);
        } catch (FeignException.BadRequest e) {
            throw new BadRequestException("Requête rejetée par " + service);
        } catch (FeignException e) {
            // Délai dépassé, aucune instance disponible, erreur 5xx...
            throw new ServiceUnavailableException(service + " est indisponible, réessayez plus tard");
        }
    }
}
