package br.com.challenge.challengeApi.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserCreationDto {
    @NotBlank(message = "User name must have a value")
    String fullName;

    @NotBlank(message = "Email must have a value")
    @Email(message ="Invalid email")
    String email;

    @NotBlank(message = "Must have password")
    @Size(min = 5, max = 16, message = "password must be between 5 to 16 digits")
    String password;

    @NotBlank(message = "Must have phoneNumber")
    String phoneNumber;
}
