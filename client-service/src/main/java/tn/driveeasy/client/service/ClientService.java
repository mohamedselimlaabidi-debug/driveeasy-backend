package tn.driveeasy.client.service;

import tn.driveeasy.client.client.LocationClient;
import tn.driveeasy.client.dto.ClientRequest;
import tn.driveeasy.client.dto.EligibiliteResponse;
import tn.driveeasy.client.entity.CategorieClient;
import tn.driveeasy.client.entity.Client;
import tn.driveeasy.client.exception.ConflictException;
import tn.driveeasy.client.exception.ResourceNotFoundException;
import tn.driveeasy.client.exception.ServiceUnavailableException;
import tn.driveeasy.client.repository.ClientRepository;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ClientService {

    private final ClientRepository repository;
    private final LocationClient locationClient;
    private final int ageMinimum;

    public ClientService(ClientRepository repository, LocationClient locationClient,
                         @Value("${app.eligibilite.age-minimum:21}") int ageMinimum) {
        this.repository = repository;
        this.locationClient = locationClient;
        this.ageMinimum = ageMinimum;
    }

    public List<Client> rechercher(String nom, String cin, String email) {
        return repository.findAll().stream()
                .filter(c -> vide(nom) || contient(c.getNom(), nom) || contient(c.getPrenom(), nom))
                .filter(c -> vide(cin) || c.getCin().equals(cin))
                .filter(c -> vide(email) || contient(c.getEmail(), email))
                .toList();
    }

    public Client trouver(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client introuvable : " + id));
    }

    public synchronized Client creer(ClientRequest requete) {
        verifierUnicite(requete, null);
        Client client = new Client();
        client.setCategorieClient(CategorieClient.STANDARD);
        client.setDateInscription(LocalDate.now());
        appliquer(requete, client);
        return repository.save(client);
    }

    public synchronized Client modifier(Long id, ClientRequest requete) {
        Client client = trouver(id);
        verifierUnicite(requete, id);
        appliquer(requete, client);
        return repository.save(client);
    }

    /** La suppression est refusée tant qu'une location est réservée ou en cours. */
    public void supprimer(Long id) {
        trouver(id);
        boolean active;
        try {
            active = locationClient.activite(id).active();
        } catch (FeignException e) {
            // Dans le doute, on ne supprime pas
            throw new ServiceUnavailableException("location-service est indisponible : suppression annulée");
        }
        if (active) {
            throw new ConflictException("Suppression refusée : le client a une location active");
        }
        repository.deleteById(id);
    }

    public EligibiliteResponse eligibilite(Long id) {
        Client client = trouver(id);
        List<String> motifs = RegleEligibilite.motifsDeRefus(client, LocalDate.now(), ageMinimum);
        return new EligibiliteResponse(id, motifs.isEmpty(), motifs, client.getCategorieClient());
    }

    public synchronized Client changerCategorie(Long id, CategorieClient categorie) {
        Client client = trouver(id);
        client.setCategorieClient(categorie);
        return repository.save(client);
    }

    private void verifierUnicite(ClientRequest r, Long idExclu) {
        if (repository.cinUtilisee(r.cin(), idExclu)) {
            throw new ConflictException("CIN déjà utilisée");
        }
        if (repository.emailUtilise(r.email(), idExclu)) {
            throw new ConflictException("Email déjà utilisé");
        }
        if (repository.permisUtilise(r.numeroPermis(), idExclu)) {
            throw new ConflictException("Numéro de permis déjà utilisé");
        }
    }

    private void appliquer(ClientRequest r, Client c) {
        c.setCin(r.cin());
        c.setNom(r.nom());
        c.setPrenom(r.prenom());
        c.setDateNaissance(r.dateNaissance());
        c.setEmail(r.email());
        c.setTelephone(r.telephone());
        c.setAdresse(r.adresse());
        c.setNumeroPermis(r.numeroPermis());
        c.setDateExpirationPermis(r.dateExpirationPermis());
    }

    private static boolean vide(String s) {
        return s == null || s.isBlank();
    }

    private static boolean contient(String valeur, String recherche) {
        return valeur.toLowerCase().contains(recherche.trim().toLowerCase());
    }
}
