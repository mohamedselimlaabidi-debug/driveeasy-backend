package tn.driveeasy.location.dto;

import java.math.BigDecimal;

/** Détail du calcul du prix. */
public record Devis(long nombreJours, BigDecimal prixJournalier, BigDecimal coefficientCategorie,
                    BigDecimal montantBrut, BigDecimal tauxRemise, BigDecimal remise, BigDecimal montantTotal) {
}
