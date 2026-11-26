package tn.driveeasy.client.config;

import tn.driveeasy.client.entity.CategorieClient;
import tn.driveeasy.client.entity.Client;
import tn.driveeasy.client.repository.ClientRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/** Données fictives : 1 standard, 1 VIP, 1 permis expiré, 1 de moins de 21 ans. */
@Component
@ConditionalOnProperty(name = "app.datasource.seed-data", havingValue = "true")
public class DataInitializer implements CommandLineRunner {

    private final ClientRepository repository;

    public DataInitializer(ClientRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        ajouter("11111111", "Ben Salah", "Amine", LocalDate.of(1995, 3, 14), "amine@example.com", "20000001", "Tunis", "P-1001",
                LocalDate.now().plusYears(3), CategorieClient.STANDARD);
        ajouter("22222222", "Trabelsi", "Sarra", LocalDate.of(1990, 7, 2), "sarra@example.com", "20000002", "Sousse", "P-1002",
                LocalDate.now().plusYears(2), CategorieClient.VIP);
        ajouter("33333333", "Gharbi", "Mehdi", LocalDate.of(1988, 11, 25), "mehdi@example.com", "20000003", "Sfax", "P-1003",
                LocalDate.now().minusMonths(6), CategorieClient.STANDARD);
        ajouter("44444444", "Jlassi", "Yasmine", LocalDate.now().minusYears(19), "yasmine@example.com", "20000004", "Nabeul", "P-1004",
                LocalDate.now().plusYears(5), CategorieClient.STANDARD);
    }

    private void ajouter(String cin, String nom, String prenom, LocalDate naissance, String email,
                         String telephone, String adresse, String permis, LocalDate expiration,
                         CategorieClient categorie) {
        Client c = new Client();
        c.setCin(cin);
        c.setNom(nom);
        c.setPrenom(prenom);
        c.setDateNaissance(naissance);
        c.setEmail(email);
        c.setTelephone(telephone);
        c.setAdresse(adresse);
        c.setNumeroPermis(permis);
        c.setDateExpirationPermis(expiration);
        c.setCategorieClient(categorie);
        c.setDateInscription(LocalDate.now().minusYears(1));
        repository.save(c);
    }
}
