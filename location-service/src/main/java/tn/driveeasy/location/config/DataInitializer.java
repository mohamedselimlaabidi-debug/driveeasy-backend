package tn.driveeasy.location.config;

import tn.driveeasy.location.entity.Location;
import tn.driveeasy.location.entity.StatutLocation;
import tn.driveeasy.location.repository.LocationRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Données de démonstration, datées par rapport au jour du démarrage :
 * locations 1 à 4 terminées, 5 en cours avec 2 jours de retard, 6 réservée.
 */
@Component
@ConditionalOnProperty(name = "app.datasource.seed-data", havingValue = "true")
public class DataInitializer implements CommandLineRunner {

    private final LocationRepository repository;

    public DataInitializer(LocationRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        LocalDate j = LocalDate.now();
        // Client 1 : 4 locations terminées, la prochaine le fait passer FIDELE
        ajouter(1L, 1L, j.minusDays(60), j.minusDays(57), "90.000", "270.000", StatutLocation.TERMINEE);
        ajouter(1L, 2L, j.minusDays(45), j.minusDays(43), "110.000", "253.000", StatutLocation.TERMINEE);
        ajouter(1L, 1L, j.minusDays(30), j.minusDays(28), "90.000", "180.000", StatutLocation.TERMINEE);
        ajouter(1L, 5L, j.minusDays(15), j.minusDays(12), "100.000", "300.000", StatutLocation.TERMINEE);
        // 3 jours x 160 x 1,30 = 624
        ajouter(1L, 3L, j.minusDays(5), j.minusDays(2), "160.000", "624.000", StatutLocation.EN_COURS);
        // 3 jours x 110 x 1,15 - 10 % (VIP) = 341,550
        ajouter(2L, 2L, j, j.plusDays(3), "110.000", "341.550", StatutLocation.RESERVEE);
    }

    private void ajouter(Long clientId, Long vehiculeId, LocalDate debut, LocalDate fin, String prix,
                         String montant, StatutLocation statut) {
        Location l = new Location();
        l.setClientId(clientId);
        l.setVehiculeId(vehiculeId);
        l.setDateDebut(debut);
        l.setDateFinPrevue(fin);
        l.setPrixJournalierApplique(new BigDecimal(prix));
        l.setMontantTotal(new BigDecimal(montant));
        l.setPenalite(BigDecimal.ZERO);
        l.setStatut(statut);
        l.setDateCreation(debut.atStartOfDay().minusDays(1));
        if (statut == StatutLocation.TERMINEE) {
            l.setDateRetourEffectif(fin);
            l.setKmDepart(1000L);
            l.setKmRetour(1300L);
        }
        if (statut == StatutLocation.EN_COURS) {
            // Kilométrage du véhicule 3 dans vehicule-service
            l.setKmDepart(28000L);
        }
        repository.save(l);
    }
}
