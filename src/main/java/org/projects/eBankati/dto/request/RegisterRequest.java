package org.projects.eBankati.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "Le nom d'utilisateur est obligatoire")
    @Size(min = 4, max = 20, message = "Le nom doit contenir entre 4 et 20 caractères")
    private String username;

    @NotBlank(message = "L'email est obligatoire!")
    @Email(message = "Format d'email invalide!")
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$",
            message = "Le mot de passe doit contenir au moins 9 caractères, une majuscule, une minuscule, un chiffre et un caractère spécial"
    )
    private String password;

    @NotNull(message = "L'âge est obligatoire")
    @Min(value = 18, message = "Vous devez avoir au moins 18 ans")
    @Max(value = 120, message = "Age invalide")
    private Integer age;
}
