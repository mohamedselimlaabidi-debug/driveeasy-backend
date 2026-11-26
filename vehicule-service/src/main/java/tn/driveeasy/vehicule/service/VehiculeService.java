package tn.driveeasy.vehicule.service;

import tn.driveeasy.vehicule.client.LocationClient;
import tn.driveeasy.vehicule.dto.StatutRequest;
import tn.driveeasy.vehicule.dto.VehiculeRequest;
import tn.driveeasy.vehicule.entity.Carburant;
import tn.driveeasy.vehicule.entity.CategorieVehicule;
import tn.driveeasy.vehicule.entity.StatutVehicule;
import tn.driveeasy.vehicule.entity.Vehicule;
import tn.driveeasy.vehicule.exception.BadRequestException;
import tn.driveeasy.vehicule.exception.ConflictException;
import tn.driveeasy.vehicule.exception.ResourceNotFoundException;
import tn.driveeasy.vehicule.exception.ServiceUnavailableException;
import tn.driveeasy.vehicule.repository.VehiculeRepository;
import feign.FeignException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class VehiculeService {

    private final VehiculeRepository repository;
    private final LocationClient locationClient;

    public VehiculeService(VehiculeRepository repository, LocationClient locationClient) {
        this.repository = repository;
        this.locationClient = locationClient;
    }

    /** Chaque filtre est ignoré lorsqu'il est absent (null). */
    public List<Vehicule> rechercher(CategorieVehicule categorie, Carburant carburant,
                                     StatutVehicule statut, BigDecimal prixMax) {
        return repository.findAll().stream()
                .filter(v -> categorie == null || v.getCategorie() == categorie)
                .filter(v -> carburant == null || v.getCarburant() == carburant)
                .filter(v -> statut == null || v.getStatut() == statut)
                .filter(v -> prixMax == null || v.getPrixJournalier().compareTo(prixMax) <= 0)
                .toList();
    }

    public Vehicule trouver(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Véhicule introuvable : " + id));
    }

    // synchronized : le contrôle d'unicité et l'enregistrement sont indivisibles
    public synchronized Vehicule creer(VehiculeRequest requete) {
        valider(requete, null);
        Vehicule vehicule = new Vehicule();
        vehicule.setStatut(StatutVehicule.DISPONIBLE);
        appliquer(requete, vehicule);
        return repository.save(vehicule);
    }

    public synchronized Vehicule modifier(Long id, VehiculeRequest requete) {
        Vehicule vehicule = trouver(id);
        valider(requete, id);
        appliquer(requete, vehicule);
        return repository.save(vehicule);
    }

    public synchronized void supprimer(Long id) {
        if (trouver(id).getStatut() == StatutVehicule.LOUE) {
            throw new ConflictException("Suppression refusée : le véhicule est loué");
        }
        repository.deleteById(id);
    }

    public synchronized Vehicule changerStatut(Long id, StatutRequest requete) {
        Vehicule vehicule = trouver(id);
        StatutVehicule cible = requete.statut();
        if (cible == StatutVehicule.LOUE && vehicule.getStatut() != StatutVehicule.DISPONIBLE) {
            throw new ConflictException("Seul un véhicule DISPONIBLE peut être loué");
        }
        if (cible == StatutVehicule.MAINTENANCE && vehicule.getStatut() == StatutVehicule.LOUE) {
            throw new ConflictException("Un véhicule loué ne peut pas passer en maintenance");
        }
        if (requete.kilometrage() != null) {
            if (requete.kilometrage() < vehicule.getKilometrage()) {
                throw new BadRequestException("Le kilométrage ne peut pas diminuer");
            }
            vehicule.setKilometrage(requete.kilometrage());
        }
        vehicule.setStatut(cible);
        return repository.save(vehicule);
    }

    /** Véhicules au statut DISPONIBLE et sans location active sur la période. */
    public List<Vehicule> disponibles(LocalDate dateDebut, LocalDate dateFin) {
        if (dateFin.isBefore(dateDebut)) {
            throw new BadRequestException("dateFin doit être postérieure ou égale à dateDebut");
        }
        Set<Long> occupes;
        try {
            occupes = new HashSet<>(locationClient.vehiculesOccupes(dateDebut.toString(), dateFin.toString()));
        } catch (FeignException e) {
            throw new ServiceUnavailableException("location-service est indisponible");
        }
        return repository.findAll().stream()
                .filter(v -> v.getStatut() == StatutVehicule.DISPONIBLE)
                .filter(v -> !occupes.contains(v.getId()))
                .toList();
    }

    private void valider(VehiculeRequest r, Long idExclu) {
        if (r.annee() > Year.now().getValue()) {
            throw new BadRequestException("L'année ne peut pas dépasser l'année courante");
        }
        if (r.prixJournalier().signum() <= 0) {
            throw new BadRequestException("Le prix journalier doit être strictement positif");
        }
        if (repository.immatriculationUtilisee(r.immatriculation().trim(), idExclu)) {
            throw new ConflictException("Immatriculation déjà utilisée : " + r.immatriculation());
        }
    }

    private void appliquer(VehiculeRequest r, Vehicule v) {
        v.setImmatriculation(r.immatriculation().trim());
        v.setMarque(r.marque());
        v.setModele(r.modele());
        v.setAnnee(r.annee());
        v.setCouleur(r.couleur());
        v.setCategorie(r.categorie());
        v.setCarburant(r.carburant());
        v.setBoite(r.boite());
        v.setNbPlaces(r.nbPlaces());
        v.setKilometrage(r.kilometrage());
        v.setPrixJournalier(r.prixJournalier());
        v.setImageUrl(r.imageUrl());
    }
}
