package tn.driveeasy.location.client.dto;

import java.math.BigDecimal;

public record VehiculeDto(Long id, String immatriculation, String marque, String modele,
                          CategorieVehicule categorie, long kilometrage, BigDecimal prixJournalier,
                          StatutVehicule statut) {
}
