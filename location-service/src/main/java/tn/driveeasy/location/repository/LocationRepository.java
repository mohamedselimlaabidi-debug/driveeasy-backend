package tn.driveeasy.location.repository;

import tn.driveeasy.location.entity.Location;
import tn.driveeasy.location.entity.StatutLocation;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/** Stockage en mémoire (RAM). */
@Repository
public class LocationRepository {

    private final Map<Long, Location> donnees = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong();

    public Location save(Location location) {
        if (location.getId() == null) {
            location.setId(sequence.incrementAndGet());
        }
        donnees.put(location.getId(), location);
        return location;
    }

    public Optional<Location> findById(Long id) {
        return Optional.ofNullable(donnees.get(id));
    }

    public List<Location> findAll() {
        return donnees.values().stream().sorted(Comparator.comparing(Location::getId)).toList();
    }

    public boolean vehiculeReserve(Long vehiculeId, LocalDate debut, LocalDate fin) {
        return donnees.values().stream().anyMatch(l ->
                l.getVehiculeId().equals(vehiculeId) && l.estActive() && l.chevauche(debut, fin));
    }

    public boolean vehiculeEnCours(Long vehiculeId) {
        return donnees.values().stream().anyMatch(l ->
                l.getVehiculeId().equals(vehiculeId) && l.getStatut() == StatutLocation.EN_COURS);
    }

    public boolean clientActif(Long clientId) {
        return donnees.values().stream()
                .anyMatch(l -> l.getClientId().equals(clientId) && l.estActive());
    }

    public long nombreTerminees(Long clientId) {
        return donnees.values().stream()
                .filter(l -> l.getClientId().equals(clientId) && l.getStatut() == StatutLocation.TERMINEE)
                .count();
    }
}
