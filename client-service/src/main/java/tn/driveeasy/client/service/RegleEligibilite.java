package tn.driveeasy.client.service;

import tn.driveeasy.client.entity.Client;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

/** Règle métier isolée (sans Spring) pour être testée facilement. */
public final class RegleEligibilite {

    private RegleEligibilite() {
    }

    /** Retourne les raisons du refus ; une liste vide signifie que le client est éligible. */
    public static List<String> motifsDeRefus(Client client, LocalDate aujourdHui, int ageMinimum) {
        List<String> motifs = new ArrayList<>();
        if (Period.between(client.getDateNaissance(), aujourdHui).getYears() < ageMinimum) {
            motifs.add("Le client doit avoir au moins " + ageMinimum + " ans");
        }
        // Le permis reste valide le jour même de son expiration
        if (client.getDateExpirationPermis().isBefore(aujourdHui)) {
            motifs.add("Le permis de conduire est expiré");
        }
        return motifs;
    }
}
