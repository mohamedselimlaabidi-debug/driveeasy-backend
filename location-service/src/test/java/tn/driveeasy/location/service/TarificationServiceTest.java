package tn.driveeasy.location.service;

import tn.driveeasy.location.client.dto.CategorieClient;
import tn.driveeasy.location.client.dto.CategorieVehicule;
import tn.driveeasy.location.config.TarificationProperties;
import tn.driveeasy.location.dto.Devis;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TarificationServiceTest {

    private static final LocalDate J = LocalDate.of(2026, 6, 1);

    private final TarificationService service = new TarificationService(new TarificationProperties(
            Map.of(CategorieVehicule.SUV, new BigDecimal("1.30")),
            Map.of(CategorieClient.VIP, new BigDecimal("0.10")),
            new BigDecimal("1.5")));

    @Test
    void appliqueCoefficientEtRemise() {
        Devis devis = service.calculer(J, J.plusDays(3), new BigDecimal("100"),
                CategorieVehicule.SUV, CategorieClient.VIP);
        assertEquals(new BigDecimal("351.000"), devis.montantTotal());
    }

    @Test
    void factureAuMoinsUnJour() {
        assertEquals(1, service.nombreJours(J, J));
    }

    @Test
    void penaliteDeRetard() {
        assertEquals(new BigDecimal("300.000"), service.penalite(J, J.plusDays(2), new BigDecimal("100")));
    }

    @Test
    void aucunePenaliteEnAvance() {
        assertEquals(new BigDecimal("0.000"), service.penalite(J, J.minusDays(1), new BigDecimal("100")));
    }
}
