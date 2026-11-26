package tn.driveeasy.client.controller;

import tn.driveeasy.client.dto.CategorieRequest;
import tn.driveeasy.client.dto.ClientRequest;
import tn.driveeasy.client.dto.ClientResponse;
import tn.driveeasy.client.dto.EligibiliteResponse;
import tn.driveeasy.client.entity.Client;
import tn.driveeasy.client.service.ClientService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/clients")
public class ClientController {

    private final ClientService service;

    public ClientController(ClientService service) {
        this.service = service;
    }

    @GetMapping
    public List<ClientResponse> rechercher(@RequestParam(required = false) String nom,
                                           @RequestParam(required = false) String cin,
                                           @RequestParam(required = false) String email) {
        return service.rechercher(nom, cin, email).stream().map(ClientResponse::from).toList();
    }

    @GetMapping("/{id}")
    public ClientResponse detail(@PathVariable Long id) {
        return ClientResponse.from(service.trouver(id));
    }

    @PostMapping
    public ResponseEntity<ClientResponse> creer(@Valid @RequestBody ClientRequest requete) {
        Client cree = service.creer(requete);
        return ResponseEntity.created(URI.create("/clients/" + cree.getId()))
                .body(ClientResponse.from(cree));
    }

    @PutMapping("/{id}")
    public ClientResponse modifier(@PathVariable Long id, @Valid @RequestBody ClientRequest requete) {
        return ClientResponse.from(service.modifier(id, requete));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable Long id) {
        service.supprimer(id);
    }

    @GetMapping("/{id}/eligibilite")
    public EligibiliteResponse eligibilite(@PathVariable Long id) {
        return service.eligibilite(id);
    }

    @PatchMapping("/{id}/categorie")
    public ClientResponse changerCategorie(@PathVariable Long id, @Valid @RequestBody CategorieRequest requete) {
        return ClientResponse.from(service.changerCategorie(id, requete.categorieClient()));
    }
}
