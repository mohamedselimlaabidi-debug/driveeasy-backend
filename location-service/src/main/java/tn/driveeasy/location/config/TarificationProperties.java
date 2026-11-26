package tn.driveeasy.location.config;

import tn.driveeasy.location.client.dto.CategorieClient;
import tn.driveeasy.location.client.dto.CategorieVehicule;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;
import java.util.Map;

/** Propriétés métier chargées depuis le Config Server. */
@ConfigurationProperties("app.tarification")
public record TarificationProperties(Map<CategorieVehicule, BigDecimal> coefficients,
                                     Map<CategorieClient, BigDecimal> remises,
                                     BigDecimal multiplicateurPenalite) {
}
