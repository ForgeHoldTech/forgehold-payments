
package com.forgehold.payments.controller;

import com.forgehold.payments.entity.AppUser;
import com.forgehold.payments.repository.AppUserRepository;
import com.forgehold.payments.service.JwtService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @BeforeEach
    void cleanDatabase() {
        appUserRepository.deleteAll();
    }

    @Test
    void shouldRegisterValidUser() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "username": "newuser",
                            "password": "SecurePass123!"
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.message")
                        .value("User registered successfully."));

        AppUser savedUser = appUserRepository
                .findByUsername("newuser")
                .orElseThrow();

        assertNotEquals("SecurePass123!", savedUser.getPasswordHash());
        assertTrue(passwordEncoder.matches(
                "SecurePass123!",
                savedUser.getPasswordHash()));
    }

    @Test
    void shouldRejectDuplicateUsername() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "username": "duplicateuser",
                            "password": "SecurePass123!"
                        }
                        """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "username": "duplicateuser",
                            "password": "AnotherPass123!"
                        }
                        """))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldRejectShortPassword() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "username": "shortpassuser",
                            "password": "Short123!"
                        }
                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectAccessToCustomersWithoutAuthentication()
            throws Exception {
        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowAccessToCustomersWithValidJwt()
            throws Exception {

        AppUser savedUser = new AppUser();
        savedUser.setUsername("jwtuser");
        savedUser.setPasswordHash(
                passwordEncoder.encode("SecurePass123!"));
        savedUser.setRole("USER");
        savedUser.setEnabled(true);

        AppUser user = appUserRepository.save(savedUser);

        String token = jwtService.generateToken(
                User.withUsername(user.getUsername())
                        .password(user.getPasswordHash())
                        .roles(user.getRole())
                        .build());

        mockMvc.perform(get("/api/customers")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRejectAccessToCustomersWithInvalidJwt()
            throws Exception {
        mockMvc.perform(get("/api/customers")
                .header("Authorization", "Bearer invalid.token.here"))
                .andExpect(status().isUnauthorized());
    }
}
