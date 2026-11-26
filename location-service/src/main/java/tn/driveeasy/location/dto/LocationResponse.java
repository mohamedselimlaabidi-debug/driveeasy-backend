package tn.driveeasy.location.dto;

import tn.driveeasy.location.entity.Location;
import tn.driveeasy.location.entity.StatutLocation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record LocationResponse(Long id, Long clientId, Long vehiculeId, LocalDate dateDebut,
        LocalDate dateFinPrevue, LocalDate dateRetourEffectif, Long kmDepart, Long kmRetour,
        BigDecimal prixJournalierApplique, BigDecimal montantTotal, BigDecimal penalite,
        StatutLocation statut, LocalDateTime dateCreation) {

    public static LocationResponse from(Location l) {
        return new LocationResponse(l.getId(), l.getClientId(), l.getVehiculeId(), l.getDateDebut(),
                l.getDateFinPrevue(), l.getDateRetourEffectif(), l.getKmDepart(), l.getKmRetour(),
                l.getPrixJournalierApplique(), l.getMontantTotal(), l.getPenalite(), l.getStatut(),
                l.getDateCreation());
    }
}
