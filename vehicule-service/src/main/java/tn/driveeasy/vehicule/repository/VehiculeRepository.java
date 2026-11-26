package tn.driveeasy.vehicule.repository;

import tn.driveeasy.vehicule.entity.Vehicule;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/** Stockage en mémoire (RAM) : les données sont perdues à l'arrêt du service. */
@Repository
public class VehiculeRepository {

    private final Map<Long, Vehicule> donnees = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong();

    public Vehicule save(Vehicule entite) {
        if (entite.getId() == null) {
            entite.setId(sequence.incrementAndGet());
        }
        donnees.put(entite.getId(), entite);
        return entite;
    }

    public Optional<Vehicule> findById(Long id) {
        return Optional.ofNullable(donnees.get(id));
    }

    public List<Vehicule> findAll() {
        return donnees.values().stream().sorted(Comparator.comparing(Vehicule::getId)).toList();
    }

    public void deleteById(Long id) {
        donnees.remove(id);
    }

    /** Vrai si un AUTRE véhicule porte déjà cette immatriculation. */
    public boolean immatriculationUtilisee(String immatriculation, Long idExclu) {
        return donnees.values().stream().anyMatch(v ->
                !v.getId().equals(idExclu) && v.getImmatriculation().equalsIgnoreCase(immatriculation));
    }
}
