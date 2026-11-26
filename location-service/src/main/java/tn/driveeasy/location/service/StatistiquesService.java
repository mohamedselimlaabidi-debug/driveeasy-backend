package tn.driveeasy.location.service;

import tn.driveeasy.location.client.AppelDistant;
import tn.driveeasy.location.client.VehiculeClient;
import tn.driveeasy.location.client.dto.StatutVehicule;
import tn.driveeasy.location.client.dto.VehiculeDto;
import tn.driveeasy.location.dto.StatistiquesResponse;
import tn.driveeasy.location.dto.TopVehicule;
import tn.driveeasy.location.entity.Location;
import tn.driveeasy.location.entity.StatutLocation;
import tn.driveeasy.location.repository.LocationRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class StatistiquesService {

    private final LocationRepository repository;
    private final VehiculeClient vehiculeClient;

    public StatistiquesService(LocationRepository repository, VehiculeClient vehiculeClient) {
        this.repository = repository;
        this.vehiculeClient = vehiculeClient;
    }

    public StatistiquesResponse calculer() {
        List<VehiculeDto> vehicules = AppelDistant.executer("vehicule-service", vehiculeClient::tous);
        List<Location> locations = repository.findAll();
        YearMonth mois = YearMonth.now();

        // Le chiffre d'affaires ne compte que les locations terminées
        List<Location> terminees = locations.stream()
                .filter(l -> l.getStatut() == StatutLocation.TERMINEE)
                .toList();
        BigDecimal total = somme(terminees);
        BigDecimal duMois = somme(terminees.stream()
                .filter(l -> YearMonth.from(l.getDateRetourEffectif()).equals(mois))
                .toList());

        long loues = compter(vehicules, StatutVehicule.LOUE);
        // Taux d'occupation instantané : véhicules loués / taille du parc
        double taux = vehicules.isEmpty() ? 0 : Math.round(loues * 1000.0 / vehicules.size()) / 10.0;

        Map<StatutLocation, Long> parStatut = new EnumMap<>(StatutLocation.class);
        for (StatutLocation statut : StatutLocation.values()) {
            parStatut.put(statut, locations.stream().filter(l -> l.getStatut() == statut).count());
        }

        return new StatistiquesResponse(total, duMois, vehicules.size(),
                compter(vehicules, StatutVehicule.DISPONIBLE), loues,
                compter(vehicules, StatutVehicule.MAINTENANCE), taux, parStatut, top(locations, vehicules));
    }

    /** Les 5 véhicules les plus loués (annulations exclues). */
    private List<TopVehicule> top(List<Location> locations, List<VehiculeDto> vehicules) {
        Map<Long, VehiculeDto> parId = vehicules.stream()
                .collect(Collectors.toMap(VehiculeDto::id, Function.identity()));
        Map<Long, Long> compte = locations.stream()
                .filter(l -> l.getStatut() != StatutLocation.ANNULEE)
                .collect(Collectors.groupingBy(Location::getVehiculeId, Collectors.counting()));
        return compte.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue(Comparator.reverseOrder())
                        .thenComparing(Map.Entry.comparingByKey()))
                .limit(5)
                .map(e -> {
                    VehiculeDto v = parId.get(e.getKey());
                    String libelle = v == null ? "Véhicule supprimé" : v.marque() + " " + v.modele();
                    return new TopVehicule(e.getKey(), libelle, e.getValue());
                })
                .toList();
    }

    private static BigDecimal somme(List<Location> locations) {
        return locations.stream().map(Location::getMontantTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static long compter(List<VehiculeDto> vehicules, StatutVehicule statut) {
        return vehicules.stream().filter(v -> v.statut() == statut).count();
    }
}
