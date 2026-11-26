package tn.driveeasy.client.client;

import tn.driveeasy.client.dto.ActiviteResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "location-service")
public interface LocationClient {

    @GetMapping("/locations/clients/{clientId}/active")
    ActiviteResponse activite(@PathVariable("clientId") Long clientId);
}
