package tn.driveeasy.vehicule.dto;

import tn.driveeasy.vehicule.entity.StatutVehicule;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/** Changement de statut ; le kilométrage est optionnel. */
public record StatutRequest(@NotNull StatutVehicule statut, @PositiveOrZero Long kilometrage) {
}
