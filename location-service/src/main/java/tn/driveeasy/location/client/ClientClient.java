package tn.driveeasy.location.client;

import tn.driveeasy.location.client.dto.CategorieDto;
import tn.driveeasy.location.client.dto.ClientDto;
import tn.driveeasy.location.client.dto.EligibiliteDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "client-service")
public interface ClientClient {

    @GetMapping("/clients/{id}")
    ClientDto trouver(@PathVariable("id") Long id);

    @GetMapping("/clients/{id}/eligibilite")
    EligibiliteDto eligibilite(@PathVariable("id") Long id);

    @PatchMapping("/clients/{id}/categorie")
    ClientDto changerCategorie(@PathVariable("id") Long id, @RequestBody CategorieDto categorie);
}
