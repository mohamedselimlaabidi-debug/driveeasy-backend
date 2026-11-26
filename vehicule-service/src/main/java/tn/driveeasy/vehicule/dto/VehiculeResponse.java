package tn.driveeasy.vehicule.dto;

import tn.driveeasy.vehicule.entity.BoiteVitesse;
import tn.driveeasy.vehicule.entity.Carburant;
import tn.driveeasy.vehicule.entity.CategorieVehicule;
import tn.driveeasy.vehicule.entity.StatutVehicule;
import tn.driveeasy.vehicule.entity.Vehicule;

import java.math.BigDecimal;

public record VehiculeResponse(Long id, String immatriculation, String marque, String modele, int annee,
        String couleur, CategorieVehicule categorie, Carburant carburant, BoiteVitesse boite, int nbPlaces,
        long kilometrage, BigDecimal prixJournalier, StatutVehicule statut, String imageUrl) {

    public static VehiculeResponse from(Vehicule v) {
        return new VehiculeResponse(v.getId(), v.getImmatriculation(), v.getMarque(), v.getModele(), v.getAnnee(),
                v.getCouleur(), v.getCategorie(), v.getCarburant(), v.getBoite(), v.getNbPlaces(),
                v.getKilometrage(), v.getPrixJournalier(), v.getStatut(), v.getImageUrl());
    }
}
