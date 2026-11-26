package tn.driveeasy.location.dto;

import jakarta.validation.constraints.PositiveOrZero;

/** kmDepart absent : on reprend le kilométrage connu du véhicule. */
public record DepartRequest(@PositiveOrZero Long kmDepart) {
}
