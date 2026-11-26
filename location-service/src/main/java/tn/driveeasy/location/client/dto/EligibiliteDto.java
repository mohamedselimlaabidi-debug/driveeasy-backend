package tn.driveeasy.location.client.dto;

import java.util.List;

public record EligibiliteDto(Long clientId, boolean eligible, List<String> motifs,
                             CategorieClient categorieClient) {
}
