package tn.driveeasy.location.service;

import tn.driveeasy.location.client.dto.CategorieClient;
import tn.driveeasy.location.client.dto.CategorieVehicule;
import tn.driveeasy.location.config.TarificationProperties;
import tn.driveeasy.location.dto.Devis;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class TarificationService {

    // 3 décimales : le dinar tunisien est divisé en 1000 millimes
    private static final int DECIMALES = 3;

    private final TarificationProperties proprietes;

    public TarificationService(TarificationProperties proprietes) {
        this.proprietes = proprietes;
    }

    /** Nombre de jours facturés : écart entre les dates, avec un minimum d'un jour. */
    public long nombreJours(LocalDate debut, LocalDate fin) {
        return Math.max(1, ChronoUnit.DAYS.between(debut, fin));
    }

    /** prix = jours × prix journalier × coefficient - remise fidélité. */
    public Devis calculer(LocalDate debut, LocalDate fin, BigDecimal prixJournalier,
                          CategorieVehicule categorieVehicule, CategorieClient categorieClient) {
        long jours = nombreJours(debut, fin);
        BigDecimal coefficient = proprietes.coefficients().get(categorieVehicule);
        BigDecimal taux = proprietes.remises().get(categorieClient);
        BigDecimal brut = arrondir(prixJournalier.multiply(BigDecimal.valueOf(jours)).multiply(coefficient));
        BigDecimal remise = arrondir(brut.multiply(taux));
        return new Devis(jours, prixJournalier, coefficient, brut, taux, remise, brut.subtract(remise));
    }

    /** pénalité = 1,5 × prix journalier × jours de retard. */
    public BigDecimal penalite(LocalDate finPrevue, LocalDate retour, BigDecimal prixJournalier) {
        long retard = Math.max(0, ChronoUnit.DAYS.between(finPrevue, retour));
        return arrondir(prixJournalier.multiply(proprietes.multiplicateurPenalite())
                .multiply(BigDecimal.valueOf(retard)));
    }

    private static BigDecimal arrondir(BigDecimal montant) {
        return montant.setScale(DECIMALES, RoundingMode.HALF_UP);
    }
}
