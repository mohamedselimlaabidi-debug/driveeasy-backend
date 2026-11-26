package tn.driveeasy.location.dto;

import tn.driveeasy.location.entity.StatutLocation;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record StatistiquesResponse(BigDecimal chiffreAffairesTotal, BigDecimal chiffreAffairesMoisCourant,
        long nombreVehicules, long vehiculesDisponibles, long vehiculesLoues, long vehiculesEnMaintenance,
        double tauxOccupation, Map<StatutLocation, Long> locationsParStatut, List<TopVehicule> topVehicules) {
}
