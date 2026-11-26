package tn.driveeasy.location.service;

import tn.driveeasy.location.client.AppelDistant;
import tn.driveeasy.location.client.ClientClient;
import tn.driveeasy.location.client.VehiculeClient;
import tn.driveeasy.location.client.dto.CategorieClient;
import tn.driveeasy.location.client.dto.CategorieDto;
import tn.driveeasy.location.client.dto.ClientDto;
import tn.driveeasy.location.client.dto.EligibiliteDto;
import tn.driveeasy.location.client.dto.StatutVehicule;
import tn.driveeasy.location.client.dto.StatutVehiculeDto;
import tn.driveeasy.location.client.dto.VehiculeDto;
import tn.driveeasy.location.config.FideliteProperties;
import tn.driveeasy.location.dto.DepartRequest;
import tn.driveeasy.location.dto.Devis;
import tn.driveeasy.location.dto.LocationDetailResponse;
import tn.driveeasy.location.dto.LocationRequest;
import tn.driveeasy.location.dto.LocationResponse;
import tn.driveeasy.location.dto.PageResponse;
import tn.driveeasy.location.dto.RetourRequest;
import tn.driveeasy.location.entity.Location;
import tn.driveeasy.location.entity.StatutLocation;
import tn.driveeasy.location.exception.BadRequestException;
import tn.driveeasy.location.exception.ConflictException;
import tn.driveeasy.location.exception.ResourceNotFoundException;
import tn.driveeasy.location.exception.ServiceUnavailableException;
import tn.driveeasy.location.repository.LocationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

/**
 * Cycle de vie d'une location. Les méthodes qui écrivent sont synchronized pour
 * empêcher deux requêtes simultanées de réserver le même véhicule.
 * L'état local n'est modifié qu'APRÈS le succès des appels distants.
 */
@Service
public class LocationService {

    private static final Logger log = LoggerFactory.getLogger(LocationService.class);

    private final LocationRepository repository;
    private final ClientClient clientClient;
    private final VehiculeClient vehiculeClient;
    private final TarificationService tarification;
    private final FideliteProperties fidelite;

    public LocationService(LocationRepository repository, ClientClient clientClient,
                           VehiculeClient vehiculeClient, TarificationService tarification,
                           FideliteProperties fidelite) {
        this.repository = repository;
        this.clientClient = clientClient;
        this.vehiculeClient = vehiculeClient;
        this.tarification = tarification;
        this.fidelite = fidelite;
    }

    public PageResponse<LocationResponse> lister(StatutLocation statut, Long clientId, Long vehiculeId,
                                                 LocalDate dateDebut, LocalDate dateFin, int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new BadRequestException("page doit être >= 0 et size compris entre 1 et 100");
        }
        List<Location> filtrees = repository.findAll().stream()
                .filter(l -> statut == null || l.getStatut() == statut)
                .filter(l -> clientId == null || l.getClientId().equals(clientId))
                .filter(l -> vehiculeId == null || l.getVehiculeId().equals(vehiculeId))
                .filter(l -> l.chevauche(dateDebut, dateFin))
                .sorted(Comparator.comparing(Location::getId).reversed())
                .toList();
        List<LocationResponse> contenu = filtrees.stream()
                .skip((long) page * size)
                .limit(size)
                .map(LocationResponse::from)
                .toList();
        int totalPages = (int) Math.ceil((double) filtrees.size() / size);
        return new PageResponse<>(contenu, page, size, filtrees.size(), totalPages);
    }

    public Location trouver(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location introuvable : " + id));
    }

    public LocationDetailResponse detail(Long id) {
        Location location = trouver(id);
        List<String> avertissements = new ArrayList<>();
        ClientDto client = optionnel("client-service",
                () -> clientClient.trouver(location.getClientId()), avertissements);
        VehiculeDto vehicule = optionnel("vehicule-service",
                () -> vehiculeClient.trouver(location.getVehiculeId()), avertissements);
        return new LocationDetailResponse(LocationResponse.from(location), client, vehicule, avertissements);
    }

    /** Calcule le prix sans rien enregistrer. */
    public Devis simuler(LocationRequest requete) {
        validerPeriode(requete);
        EligibiliteDto eligibilite = eligibilite(requete.clientId());
        VehiculeDto vehicule = vehicule(requete.vehiculeId());
        return devis(requete, vehicule, eligibilite.categorieClient());
    }

    public synchronized Location reserver(LocationRequest requete) {
        validerPeriode(requete);
        EligibiliteDto eligibilite = eligibilite(requete.clientId());
        exigerEligible(eligibilite);
        VehiculeDto vehicule = vehicule(requete.vehiculeId());
        exigerDisponible(vehicule);
        if (repository.vehiculeReserve(vehicule.id(), requete.dateDebut(), requete.dateFinPrevue())) {
            throw new ConflictException("Le véhicule est déjà réservé sur cette période");
        }
        Devis devis = devis(requete, vehicule, eligibilite.categorieClient());

        Location location = new Location();
        location.setClientId(requete.clientId());
        location.setVehiculeId(requete.vehiculeId());
        location.setDateDebut(requete.dateDebut());
        location.setDateFinPrevue(requete.dateFinPrevue());
        location.setPrixJournalierApplique(vehicule.prixJournalier());
        location.setMontantTotal(devis.montantTotal());
        location.setPenalite(BigDecimal.ZERO);
        location.setStatut(StatutLocation.RESERVEE);
        location.setDateCreation(LocalDateTime.now());
        return repository.save(location);
    }

    public synchronized Location demarrer(Long id, DepartRequest requete) {
        Location location = trouver(id);
        exigerStatut(location, StatutLocation.RESERVEE);
        LocalDate aujourdHui = LocalDate.now();
        if (aujourdHui.isBefore(location.getDateDebut()) || aujourdHui.isAfter(location.getDateFinPrevue())) {
            throw new ConflictException("Le départ n'est possible que pendant la période réservée");
        }
        // Le permis a pu expirer depuis la réservation
        exigerEligible(eligibilite(location.getClientId()));
        VehiculeDto vehicule = vehicule(location.getVehiculeId());
        exigerDisponible(vehicule);
        long kmDepart = (requete == null || requete.kmDepart() == null)
                ? vehicule.kilometrage() : requete.kmDepart();
        if (kmDepart < vehicule.kilometrage()) {
            throw new BadRequestException("kmDepart est inférieur au kilométrage du véhicule");
        }

        try {
            changerStatutVehicule(location.getVehiculeId(), StatutVehicule.LOUE, kmDepart);
        } catch (ServiceUnavailableException e) {
            // La réponse a pu se perdre après la mise à jour : on compense
            libererVehicule(location.getVehiculeId());
            throw e;
        }
        location.setKmDepart(kmDepart);
        location.setStatut(StatutLocation.EN_COURS);
        return repository.save(location);
    }

    public synchronized Location retourner(Long id, RetourRequest requete) {
        Location location = trouver(id);
        exigerStatut(location, StatutLocation.EN_COURS);
        if (requete.kmRetour() < location.getKmDepart()) {
            throw new BadRequestException("kmRetour ne peut pas être inférieur à kmDepart");
        }
        LocalDate retour = requete.dateRetourEffectif() == null ? LocalDate.now() : requete.dateRetourEffectif();
        if (retour.isBefore(location.getDateDebut()) || retour.isAfter(LocalDate.now())) {
            throw new BadRequestException("La date de retour doit être comprise entre le début et aujourd'hui");
        }
        BigDecimal penalite = tarification.penalite(
                location.getDateFinPrevue(), retour, location.getPrixJournalierApplique());

        // Si cet appel échoue, la location reste EN_COURS et l'opération peut être relancée
        changerStatutVehicule(location.getVehiculeId(), StatutVehicule.DISPONIBLE, requete.kmRetour());

        location.setDateRetourEffectif(retour);
        location.setKmRetour(requete.kmRetour());
        location.setPenalite(penalite);
        location.setMontantTotal(location.getMontantTotal().add(penalite));
        location.setStatut(StatutLocation.TERMINEE);
        repository.save(location);
        actualiserFidelite(location.getClientId());
        return location;
    }

    public synchronized Location annuler(Long id) {
        Location location = trouver(id);
        exigerStatut(location, StatutLocation.RESERVEE);
        location.setStatut(StatutLocation.ANNULEE);
        return repository.save(location);
    }

    public boolean clientActif(Long clientId) {
        return repository.clientActif(clientId);
    }

    public List<Long> vehiculesOccupes(LocalDate debut, LocalDate fin) {
        return repository.findAll().stream()
                .filter(l -> l.estActive() && l.chevauche(debut, fin))
                .map(Location::getVehiculeId)
                .distinct()
                .toList();
    }

    /** Promotion FIDELE / VIP. Un échec ici n'annule pas le retour déjà enregistré. */
    private void actualiserFidelite(Long clientId) {
        long terminees = repository.nombreTerminees(clientId);
        if (terminees < fidelite.seuilFidele()) {
            return;
        }
        CategorieClient cible = terminees >= fidelite.seuilVip() ? CategorieClient.VIP : CategorieClient.FIDELE;
        try {
            ClientDto client = clientClient.trouver(clientId);
            // On ne rétrograde jamais un client
            if (client.categorieClient().compareTo(cible) < 0) {
                clientClient.changerCategorie(clientId, new CategorieDto(cible));
            }
        } catch (RuntimeException e) {
            log.warn("Catégorie du client {} non mise à jour (nouvel essai au prochain retour) : {}",
                    clientId, e.getMessage());
        }
    }

    private void libererVehicule(Long vehiculeId) {
        // Ne jamais libérer un véhicule réellement utilisé par une autre location
        if (repository.vehiculeEnCours(vehiculeId)) {
            return;
        }
        try {
            changerStatutVehicule(vehiculeId, StatutVehicule.DISPONIBLE, null);
        } catch (RuntimeException e) {
            log.error("Compensation impossible pour le véhicule {} : {}", vehiculeId, e.getMessage());
        }
    }

    private void changerStatutVehicule(Long vehiculeId, StatutVehicule statut, Long kilometrage) {
        AppelDistant.executer("vehicule-service",
                () -> vehiculeClient.changerStatut(vehiculeId, new StatutVehiculeDto(statut, kilometrage)));
    }

    private EligibiliteDto eligibilite(Long clientId) {
        return AppelDistant.executer("client-service", () -> clientClient.eligibilite(clientId));
    }

    private VehiculeDto vehicule(Long vehiculeId) {
        return AppelDistant.executer("vehicule-service", () -> vehiculeClient.trouver(vehiculeId));
    }

    private Devis devis(LocationRequest r, VehiculeDto v, CategorieClient categorie) {
        return tarification.calculer(r.dateDebut(), r.dateFinPrevue(), v.prixJournalier(),
                v.categorie(), categorie);
    }

    private <T> T optionnel(String service, Supplier<T> appel, List<String> avertissements) {
        try {
            return AppelDistant.executer(service, appel);
        } catch (RuntimeException e) {
            avertissements.add(e.getMessage());
            return null;
        }
    }

    private void validerPeriode(LocationRequest r) {
        if (r.dateDebut().isBefore(LocalDate.now())) {
            throw new BadRequestException("dateDebut ne peut pas être dans le passé");
        }
        if (r.dateFinPrevue().isBefore(r.dateDebut())) {
            throw new BadRequestException("dateFinPrevue doit être postérieure ou égale à dateDebut");
        }
    }

    private void exigerEligible(EligibiliteDto e) {
        if (!e.eligible()) {
            throw new ConflictException("Client non éligible : " + String.join(", ", e.motifs()));
        }
    }

    private void exigerDisponible(VehiculeDto v) {
        if (v.statut() != StatutVehicule.DISPONIBLE) {
            throw new ConflictException("Véhicule non disponible (statut " + v.statut() + ")");
        }
    }

    private void exigerStatut(Location location, StatutLocation attendu) {
        if (location.getStatut() != attendu) {
            throw new ConflictException("Action impossible : la location est " + location.getStatut()
                    + " (attendu : " + attendu + ")");
        }
    }
}
