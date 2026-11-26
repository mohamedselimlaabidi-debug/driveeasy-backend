package tn.driveeasy.location.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record LocationRequest(@NotNull Long clientId, @NotNull Long vehiculeId,
                              @NotNull LocalDate dateDebut, @NotNull LocalDate dateFinPrevue) {
}
