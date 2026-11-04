package br.com.challenge.challengeApi.dto;

import br.com.challenge.challengeApi.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserPatchDto(
        @NotBlank(message = "User name must have a value")
        String fullname,
        @NotBlank(message = "Email must have a value")
        @Email(message ="Invalid email")
        String email,
        @NotBlank(message = "")
        @Size(min = 5, max = 16, message = "password must be between 5 to 16 digits")
        String password,
        @NotBlank(message = "Must have phoneNumber")
        String phoneNumber
) {
    public UserPatchDto(User user){
        this(
                user.getFullName(),
                user.getEmail(),
                user.getPassword(),
                user.getPhoneNumber()
        );
    }
}