package tn.driveeasy.location.client;

import tn.driveeasy.location.client.dto.StatutVehiculeDto;
import tn.driveeasy.location.client.dto.VehiculeDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "vehicule-service")
public interface VehiculeClient {

    @GetMapping("/vehicules")
    List<VehiculeDto> tous();

    @GetMapping("/vehicules/{id}")
    VehiculeDto trouver(@PathVariable("id") Long id);

    @PatchMapping("/vehicules/{id}/statut")
    VehiculeDto changerStatut(@PathVariable("id") Long id, @RequestBody StatutVehiculeDto statut);
}
