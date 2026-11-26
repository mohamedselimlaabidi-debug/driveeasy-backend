package tn.driveeasy.location.dto;

import tn.driveeasy.location.client.dto.ClientDto;
import tn.driveeasy.location.client.dto.VehiculeDto;

import java.util.List;

/** Vue agrégée ; client ou vehicule vaut null si son service ne répond pas. */
public record LocationDetailResponse(LocationResponse location, ClientDto client, VehiculeDto vehicule,
                                     List<String> avertissements) {
}
