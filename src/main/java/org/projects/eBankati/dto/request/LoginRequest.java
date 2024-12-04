package org.projects.eBankati.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LoginRequest
{
    @NotBlank(message = "Votre Nom d'utilisateur est obligatoire pour se connecter")
    private String username;

    @NotNull(message = "Votre mot de passe est obligatoire pour se connecter")
    private String password;
}
