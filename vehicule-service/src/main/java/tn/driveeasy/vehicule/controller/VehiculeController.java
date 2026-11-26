package tn.driveeasy.vehicule.controller;

import tn.driveeasy.vehicule.dto.StatutRequest;
import tn.driveeasy.vehicule.dto.VehiculeRequest;
import tn.driveeasy.vehicule.dto.VehiculeResponse;
import tn.driveeasy.vehicule.entity.Carburant;
import tn.driveeasy.vehicule.entity.CategorieVehicule;
import tn.driveeasy.vehicule.entity.StatutVehicule;
import tn.driveeasy.vehicule.entity.Vehicule;
import tn.driveeasy.vehicule.service.VehiculeService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
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

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/vehicules")
public class VehiculeController {

    private final VehiculeService service;

    public VehiculeController(VehiculeService service) {
        this.service = service;
    }

    @GetMapping
    public List<VehiculeResponse> rechercher(@RequestParam(required = false) CategorieVehicule categorie,
                                             @RequestParam(required = false) Carburant carburant,
                                             @RequestParam(required = false) StatutVehicule statut,
                                             @RequestParam(required = false) BigDecimal prixMax) {
        return versReponses(service.rechercher(categorie, carburant, statut, prixMax));
    }

    @GetMapping("/disponibles")
    public List<VehiculeResponse> disponibles(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin) {
        return versReponses(service.disponibles(dateDebut, dateFin));
    }

    @GetMapping("/{id}")
    public VehiculeResponse detail(@PathVariable Long id) {
        return VehiculeResponse.from(service.trouver(id));
    }

    @PostMapping
    public ResponseEntity<VehiculeResponse> creer(@Valid @RequestBody VehiculeRequest requete) {
        Vehicule cree = service.creer(requete);
        return ResponseEntity.created(URI.create("/vehicules/" + cree.getId()))
                .body(VehiculeResponse.from(cree));
    }

    @PutMapping("/{id}")
    public VehiculeResponse modifier(@PathVariable Long id, @Valid @RequestBody VehiculeRequest requete) {
        return VehiculeResponse.from(service.modifier(id, requete));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable Long id) {
        service.supprimer(id);
    }

    @PatchMapping("/{id}/statut")
    public VehiculeResponse changerStatut(@PathVariable Long id, @Valid @RequestBody StatutRequest requete) {
        return VehiculeResponse.from(service.changerStatut(id, requete));
    }

    private List<VehiculeResponse> versReponses(List<Vehicule> vehicules) {
        return vehicules.stream().map(VehiculeResponse::from).toList();
    }
}
