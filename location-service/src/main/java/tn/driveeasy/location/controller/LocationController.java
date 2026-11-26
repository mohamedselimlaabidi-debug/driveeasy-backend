package tn.driveeasy.location.controller;

import tn.driveeasy.location.dto.ActiviteResponse;
import tn.driveeasy.location.dto.DepartRequest;
import tn.driveeasy.location.dto.Devis;
import tn.driveeasy.location.dto.LocationDetailResponse;
import tn.driveeasy.location.dto.LocationRequest;
import tn.driveeasy.location.dto.LocationResponse;
import tn.driveeasy.location.dto.PageResponse;
import tn.driveeasy.location.dto.RetourRequest;
import tn.driveeasy.location.dto.StatistiquesResponse;
import tn.driveeasy.location.entity.Location;
import tn.driveeasy.location.entity.StatutLocation;
import tn.driveeasy.location.service.LocationService;
import tn.driveeasy.location.service.StatistiquesService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/locations")
public class LocationController {

    private final LocationService service;
    private final StatistiquesService statistiques;

    public LocationController(LocationService service, StatistiquesService statistiques) {
        this.service = service;
        this.statistiques = statistiques;
    }

    @GetMapping
    public PageResponse<LocationResponse> lister(
            @RequestParam(required = false) StatutLocation statut,
            @RequestParam(required = false) Long clientId,
            @RequestParam(required = false) Long vehiculeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return service.lister(statut, clientId, vehiculeId, dateDebut, dateFin, page, size);
    }

    @GetMapping("/statistiques")
    public StatistiquesResponse statistiques() {
        return statistiques.calculer();
    }

    @GetMapping("/{id}")
    public LocationDetailResponse detail(@PathVariable Long id) {
        return service.detail(id);
    }

    @PostMapping
    public ResponseEntity<LocationResponse> reserver(@Valid @RequestBody LocationRequest requete) {
        Location creee = service.reserver(requete);
        return ResponseEntity.created(URI.create("/locations/" + creee.getId()))
                .body(LocationResponse.from(creee));
    }

    @PostMapping("/simulation")
    public Devis simuler(@Valid @RequestBody LocationRequest requete) {
        return service.simuler(requete);
    }

    @PatchMapping("/{id}/depart")
    public LocationResponse depart(@PathVariable Long id,
                                   @Valid @RequestBody(required = false) DepartRequest requete) {
        return LocationResponse.from(service.demarrer(id, requete));
    }

    @PatchMapping("/{id}/retour")
    public LocationResponse retour(@PathVariable Long id, @Valid @RequestBody RetourRequest requete) {
        return LocationResponse.from(service.retourner(id, requete));
    }

    @PatchMapping("/{id}/annulation")
    public LocationResponse annulation(@PathVariable Long id) {
        return LocationResponse.from(service.annuler(id));
    }

    // ----- Points d'accès utilisés par les autres microservices -----

    @GetMapping("/clients/{clientId}/active")
    public ActiviteResponse activite(@PathVariable Long clientId) {
        return new ActiviteResponse(service.clientActif(clientId));
    }

    @GetMapping("/vehicules-occupes")
    public List<Long> vehiculesOccupes(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin) {
        return service.vehiculesOccupes(dateDebut, dateFin);
    }
}
