package tn.driveeasy.location.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.fidelite")
public record FideliteProperties(int seuilFidele, int seuilVip) {
}
