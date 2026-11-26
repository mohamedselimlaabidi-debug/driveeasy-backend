package tn.driveeasy.location.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDate;

/** dateRetourEffectif absente : aujourd'hui. */
public record RetourRequest(@NotNull @PositiveOrZero Long kmRetour, LocalDate dateRetourEffectif) {
}
