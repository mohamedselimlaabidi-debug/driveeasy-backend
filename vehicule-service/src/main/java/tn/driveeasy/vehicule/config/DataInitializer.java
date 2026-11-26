package tn.driveeasy.vehicule.config;

import tn.driveeasy.vehicule.entity.BoiteVitesse;
import tn.driveeasy.vehicule.entity.Carburant;
import tn.driveeasy.vehicule.entity.CategorieVehicule;
import tn.driveeasy.vehicule.entity.StatutVehicule;
import tn.driveeasy.vehicule.entity.Vehicule;
import tn.driveeasy.vehicule.repository.VehiculeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/** Jeu de données de démonstration. Le véhicule 3 est loué (location 5). */
@Component
@ConditionalOnProperty(name = "app.datasource.seed-data", havingValue = "true")
public class DataInitializer implements CommandLineRunner {

    private final VehiculeRepository repository;

    public DataInitializer(VehiculeRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        ajouter("120 TU 1001", "Renault", "Clio", 2022, "Blanc", CategorieVehicule.CITADINE, Carburant.ESSENCE,
                BoiteVitesse.MANUELLE, 5, 35000, "90.000", StatutVehicule.DISPONIBLE);
        ajouter("134 TU 2002", "Peugeot", "301", 2021, "Gris", CategorieVehicule.BERLINE, Carburant.DIESEL,
                BoiteVitesse.MANUELLE, 5, 62000, "110.000", StatutVehicule.DISPONIBLE);
        ajouter("201 TU 3003", "Kia", "Sportage", 2023, "Noir", CategorieVehicule.SUV, Carburant.DIESEL,
                BoiteVitesse.AUTOMATIQUE, 5, 28000, "160.000", StatutVehicule.LOUE);
        ajouter("188 TU 4004", "Volkswagen", "Golf", 2022, "Bleu", CategorieVehicule.BERLINE, Carburant.ESSENCE,
                BoiteVitesse.AUTOMATIQUE, 5, 41000, "130.000", StatutVehicule.DISPONIBLE);
        ajouter("215 TU 5005", "Toyota", "Yaris", 2024, "Rouge", CategorieVehicule.CITADINE, Carburant.HYBRIDE,
                BoiteVitesse.AUTOMATIQUE, 5, 12000, "100.000", StatutVehicule.DISPONIBLE);
        ajouter("176 TU 6006", "Mercedes", "Classe C", 2022, "Noir", CategorieVehicule.LUXE, Carburant.DIESEL,
                BoiteVitesse.AUTOMATIQUE, 5, 54000, "280.000", StatutVehicule.MAINTENANCE);
        ajouter("159 TU 7007", "Citroën", "Berlingo", 2020, "Blanc", CategorieVehicule.UTILITAIRE, Carburant.DIESEL,
                BoiteVitesse.MANUELLE, 3, 89000, "120.000", StatutVehicule.DISPONIBLE);
        ajouter("222 TU 8008", "Hyundai", "Tucson", 2024, "Gris", CategorieVehicule.SUV, Carburant.HYBRIDE,
                BoiteVitesse.AUTOMATIQUE, 5, 9000, "170.000", StatutVehicule.DISPONIBLE);
    }

    private void ajouter(String immatriculation, String marque, String modele, int annee, String couleur,
                         CategorieVehicule categorie, Carburant carburant, BoiteVitesse boite, int places,
                         long kilometrage, String prix, StatutVehicule statut) {
        Vehicule v = new Vehicule();
        v.setImmatriculation(immatriculation);
        v.setMarque(marque);
        v.setModele(modele);
        v.setAnnee(annee);
        v.setCouleur(couleur);
        v.setCategorie(categorie);
        v.setCarburant(carburant);
        v.setBoite(boite);
        v.setNbPlaces(places);
        v.setKilometrage(kilometrage);
        v.setPrixJournalier(new BigDecimal(prix));
        v.setStatut(statut);
        repository.save(v);
    }
}
