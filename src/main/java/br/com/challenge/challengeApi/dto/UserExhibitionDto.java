package br.com.challenge.challengeApi.dto;

import br.com.challenge.challengeApi.model.User;

public record UserExhibitionDto(
        Long id,
        String fullName,
        String email,
        String phoneNumber
) {
    public UserExhibitionDto(User user){
        this(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhoneNumber()
        );
    }
}
