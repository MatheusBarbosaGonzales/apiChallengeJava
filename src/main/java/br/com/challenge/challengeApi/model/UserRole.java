package br.com.challenge.challengeApi.model;

public enum UserRole {
    ADMIN("admin"),
    SIGNER("signer");

    private String role;

    UserRole(String role){
        this.role = role;
    }

    public String getRole(){
        return this.role;
    }
}
