package tn.driveeasy.client.repository;

import tn.driveeasy.client.entity.Client;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/** Stockage en mémoire (RAM) : les données sont perdues à l'arrêt du service. */
@Repository
public class ClientRepository {

    private final Map<Long, Client> donnees = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong();

    public Client save(Client entite) {
        if (entite.getId() == null) {
            entite.setId(sequence.incrementAndGet());
        }
        donnees.put(entite.getId(), entite);
        return entite;
    }

    public Optional<Client> findById(Long id) {
        return Optional.ofNullable(donnees.get(id));
    }

    public List<Client> findAll() {
        return donnees.values().stream().sorted(Comparator.comparing(Client::getId)).toList();
    }

    public void deleteById(Long id) {
        donnees.remove(id);
    }

    public boolean cinUtilisee(String cin, Long idExclu) {
        return donnees.values().stream()
                .anyMatch(c -> !c.getId().equals(idExclu) && c.getCin().equals(cin));
    }

    public boolean emailUtilise(String email, Long idExclu) {
        return donnees.values().stream()
                .anyMatch(c -> !c.getId().equals(idExclu) && c.getEmail().equalsIgnoreCase(email));
    }

    public boolean permisUtilise(String numeroPermis, Long idExclu) {
        return donnees.values().stream()
                .anyMatch(c -> !c.getId().equals(idExclu) && c.getNumeroPermis().equalsIgnoreCase(numeroPermis));
    }
}
