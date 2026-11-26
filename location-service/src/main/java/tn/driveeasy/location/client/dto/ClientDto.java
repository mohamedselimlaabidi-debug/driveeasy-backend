package tn.driveeasy.location.client.dto;

/** Vue partielle du client : seuls les champs utiles sont lus. */
public record ClientDto(Long id, String cin, String nom, String prenom, String email, String telephone,
                        CategorieClient categorieClient) {
}
