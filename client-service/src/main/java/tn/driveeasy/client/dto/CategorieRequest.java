package tn.driveeasy.client.dto;

import tn.driveeasy.client.entity.CategorieClient;
import jakarta.validation.constraints.NotNull;

public record CategorieRequest(@NotNull CategorieClient categorieClient) {
}
