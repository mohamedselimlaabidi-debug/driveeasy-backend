package tn.driveeasy.client.dto;

import tn.driveeasy.client.entity.CategorieClient;

import java.util.List;

public record EligibiliteResponse(Long clientId, boolean eligible, List<String> motifs,
                                  CategorieClient categorieClient) {
}
