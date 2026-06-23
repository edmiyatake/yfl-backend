package com.yfl_backend.auth;

public class RequestCodeDto {

    private String email;

    public RequestCodeDto() {
    }

    public RequestCodeDto(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}