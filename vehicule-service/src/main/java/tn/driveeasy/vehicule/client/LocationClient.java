package tn.driveeasy.vehicule.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/** Appel à location-service, résolu par Eureka. */
@FeignClient(name = "location-service")
public interface LocationClient {

    @GetMapping("/locations/vehicules-occupes")
    List<Long> vehiculesOccupes(@RequestParam("dateDebut") String dateDebut,
                                @RequestParam("dateFin") String dateFin);
}
