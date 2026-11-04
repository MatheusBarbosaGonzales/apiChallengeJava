package br.com.challenge.challengeApi.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginDto(
        @NotBlank(message = "User email must have a value")
        @Email
        String email,

        @NotBlank(message = "Must have password")
        @Size(min = 5, max = 16, message = "password must be between 5 to 16 digits")
        String password
){
}
