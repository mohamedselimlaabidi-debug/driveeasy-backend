package tn.driveeasy.client.service;

import tn.driveeasy.client.entity.Client;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegleEligibiliteTest {

    private static final LocalDate JOUR = LocalDate.of(2026, 6, 15);

    private Client client(LocalDate naissance, LocalDate expiration) {
        Client c = new Client();
        c.setDateNaissance(naissance);
        c.setDateExpirationPermis(expiration);
        return c;
    }

    @Test
    void eligibleLeJourDes21Ans() {
        Client c = client(JOUR.minusYears(21), JOUR);
        assertTrue(RegleEligibilite.motifsDeRefus(c, JOUR, 21).isEmpty());
    }

    @Test
    void refuseLaVeilleDes21Ans() {
        Client c = client(JOUR.minusYears(21).plusDays(1), JOUR.plusYears(1));
        assertEquals(1, RegleEligibilite.motifsDeRefus(c, JOUR, 21).size());
    }

    @Test
    void cumuleLesDeuxMotifs() {
        Client c = client(JOUR.minusYears(18), JOUR.minusDays(1));
        assertEquals(2, RegleEligibilite.motifsDeRefus(c, JOUR, 21).size());
    }
}
