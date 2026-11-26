package tn.driveeasy.location.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Location {

    private Long id;
    private Long clientId;
    private Long vehiculeId;
    private LocalDate dateDebut;
    private LocalDate dateFinPrevue;
    private LocalDate dateRetourEffectif;
    private Long kmDepart;
    private Long kmRetour;
    private BigDecimal prixJournalierApplique;
    private BigDecimal montantTotal;
    private BigDecimal penalite;
    private StatutLocation statut;
    private LocalDateTime dateCreation;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public Long getVehiculeId() {
        return vehiculeId;
    }

    public void setVehiculeId(Long vehiculeId) {
        this.vehiculeId = vehiculeId;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDate getDateFinPrevue() {
        return dateFinPrevue;
    }

    public void setDateFinPrevue(LocalDate dateFinPrevue) {
        this.dateFinPrevue = dateFinPrevue;
    }

    public LocalDate getDateRetourEffectif() {
        return dateRetourEffectif;
    }

    public void setDateRetourEffectif(LocalDate dateRetourEffectif) {
        this.dateRetourEffectif = dateRetourEffectif;
    }

    public Long getKmDepart() {
        return kmDepart;
    }

    public void setKmDepart(Long kmDepart) {
        this.kmDepart = kmDepart;
    }

    public Long getKmRetour() {
        return kmRetour;
    }

    public void setKmRetour(Long kmRetour) {
        this.kmRetour = kmRetour;
    }

    public BigDecimal getPrixJournalierApplique() {
        return prixJournalierApplique;
    }

    public void setPrixJournalierApplique(BigDecimal prixJournalierApplique) {
        this.prixJournalierApplique = prixJournalierApplique;
    }

    public BigDecimal getMontantTotal() {
        return montantTotal;
    }

    public void setMontantTotal(BigDecimal montantTotal) {
        this.montantTotal = montantTotal;
    }

    public BigDecimal getPenalite() {
        return penalite;
    }

    public void setPenalite(BigDecimal penalite) {
        this.penalite = penalite;
    }

    public StatutLocation getStatut() {
        return statut;
    }

    public void setStatut(StatutLocation statut) {
        this.statut = statut;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    /** Une location active bloque le véhicule et le client. */
    public boolean estActive() {
        return statut == StatutLocation.RESERVEE || statut == StatutLocation.EN_COURS;
    }

    /** Chevauchement avec [debut, fin], bornes incluses ; une borne null est ignorée. */
    public boolean chevauche(LocalDate debut, LocalDate fin) {
        return (debut == null || !dateFinPrevue.isBefore(debut))
                && (fin == null || !dateDebut.isAfter(fin));
    }
}
