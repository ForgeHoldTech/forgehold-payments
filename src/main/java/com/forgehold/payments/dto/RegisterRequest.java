
package com.forgehold.payments.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

    @NotBlank(message = "Username is required.")
    @Size(min = 3, max = 50,
          message = "Username must be between 3 and 50 characters.")
    private String username;

    @NotBlank(message = "Password is required.")
    @Size(min = 12, max = 72,
          message = "Password must be between 12 and 72 characters.")
    private String password;

    public RegisterRequest() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
