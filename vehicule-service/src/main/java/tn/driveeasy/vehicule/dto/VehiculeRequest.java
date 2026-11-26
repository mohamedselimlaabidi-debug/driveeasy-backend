package tn.driveeasy.vehicule.dto;

import tn.driveeasy.vehicule.entity.BoiteVitesse;
import tn.driveeasy.vehicule.entity.Carburant;
import tn.driveeasy.vehicule.entity.CategorieVehicule;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

/** Données reçues à la création et à la modification (sans id ni statut). */
public record VehiculeRequest(
        @NotBlank String immatriculation,
        @NotBlank String marque,
        @NotBlank String modele,
        @NotNull @Min(1900) Integer annee,
        @NotBlank String couleur,
        @NotNull CategorieVehicule categorie,
        @NotNull Carburant carburant,
        @NotNull BoiteVitesse boite,
        @NotNull @Min(1) @Max(60) Integer nbPlaces,
        @NotNull @PositiveOrZero Long kilometrage,
        @NotNull @Positive BigDecimal prixJournalier,
        String imageUrl) {
}
