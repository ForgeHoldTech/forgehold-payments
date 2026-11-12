
package com.forgehold.payments.controller;

import com.forgehold.payments.entity.Customer;
import com.forgehold.payments.repository.CustomerRepository;
import org.springframework.security.test.context.support.WithMockUser;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.hamcrest.Matchers.hasItem;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(username = "testuser", roles = {"USER"})
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CustomerRepository customerRepository;

    private String uniqueEmail(String prefix) {
        return prefix + "." + System.nanoTime() + "@example.com";
    }

    private String customerJson(String name, String email, String phone) {
        return """
                {
                    "fullName": "%s",
                    "email": "%s",
                    "phone": "%s"
                }
                """.formatted(name, email, phone);
    }

    private long createCustomer(String name, String email, String phone)
            throws Exception {

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(customerJson(name, email, phone)))
                .andExpect(status().isCreated());

        return customerRepository.findByEmail(email)
                .orElseThrow()
                .getId();
    }

    @Test
    void shouldRegisterValidCustomer() throws Exception {

        String email = uniqueEmail("register");

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(customerJson(
                                "Test Customer", email, "03001234567")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.fullName").value("Test Customer"))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.phone").value("03001234567"));

        assertTrue(customerRepository.findByEmail(email).isPresent());
    }

    @Test
    void shouldRejectDuplicateEmail() throws Exception {

        String email = uniqueEmail("duplicate");

        createCustomer("First Customer", email, "03001234567");

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(customerJson(
                                "Second Customer", email, "03007654321")))
                .andExpect(status().isConflict());
    }

    
    @Test
    void shouldRejectInvalidEmail() throws Exception {

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(customerJson(
                                "Invalid Customer",
                                "not-an-email",
                                "03001234567")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.path").value("/api/customers"));
    }


    @Test
    void shouldRejectBlankCustomerName() throws Exception {

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(customerJson(
                                "   ",
                                uniqueEmail("blankname"),
                                "03001234567")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectBlankEmail() throws Exception {

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(customerJson(
                                "Test Customer", "", "03001234567")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectBlankPhone() throws Exception {

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(customerJson(
                                "Test Customer",
                                uniqueEmail("blankphone"),
                                "   ")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRetrieveCustomerById() throws Exception {

        String email = uniqueEmail("retrieve");

        long customerId = createCustomer(
                "Retrieve Test Customer", email, "03001234567");

        mockMvc.perform(get("/api/customers/" + customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(customerId))
                .andExpect(jsonPath("$.fullName")
                        .value("Retrieve Test Customer"))
                .andExpect(jsonPath("$.email").value(email));
    }

    
    @Test
    void shouldReturnNotFoundForMissingCustomer() throws Exception {

        mockMvc.perform(get("/api/customers/" + Long.MAX_VALUE))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.path")
                        .value("/api/customers/" + Long.MAX_VALUE));
    }


    @Test
    void shouldListCustomers() throws Exception {

        String email = uniqueEmail("listing");

        createCustomer("Listing Test Customer", email, "03001234567");

        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].email", hasItem(email)));
    }

    @Test
    void shouldUpdateExistingCustomer() throws Exception {

        String originalEmail = uniqueEmail("original");

        long customerId = createCustomer(
                "Original Customer", originalEmail, "03001234567");

        String updatedEmail = uniqueEmail("updated");

        mockMvc.perform(put("/api/customers/" + customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(customerJson(
                                "Updated Customer",
                                updatedEmail,
                                "03007654321")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(customerId))
                .andExpect(jsonPath("$.fullName").value("Updated Customer"))
                .andExpect(jsonPath("$.email").value(updatedEmail))
                .andExpect(jsonPath("$.phone").value("03007654321"));

        assertTrue(customerRepository.findByEmail(updatedEmail).isPresent());
        assertTrue(customerRepository.findByEmail(originalEmail).isEmpty());
    }

    @Test
    void shouldRejectUpdateWithDuplicateEmail() throws Exception {

        String firstEmail = uniqueEmail("first");
        String secondEmail = uniqueEmail("second");

        long firstCustomerId = createCustomer(
                "First Customer", firstEmail, "03001234567");

        createCustomer("Second Customer", secondEmail, "03007654321");

        mockMvc.perform(put("/api/customers/" + firstCustomerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(customerJson(
                                "Updated First Customer",
                                secondEmail,
                                "03001112222")))
                .andExpect(status().isConflict());

        Customer unchangedCustomer = customerRepository
                .findById(firstCustomerId)
                .orElseThrow();

        assertTrue(unchangedCustomer.getEmail().equals(firstEmail));
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingMissingCustomer()
            throws Exception {

        mockMvc.perform(put("/api/customers/" + Long.MAX_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(customerJson(
                                "Missing Customer",
                                uniqueEmail("missingupdate"),
                                "03001234567")))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldRejectUpdateWithInvalidEmail() throws Exception {

        String email = uniqueEmail("invalidupdate");

        long customerId = createCustomer(
                "Existing Customer", email, "03001234567");

        mockMvc.perform(put("/api/customers/" + customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(customerJson(
                                "Updated Customer",
                                "invalid-email",
                                "03007654321")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldDeleteCustomerSuccessfully() throws Exception {

        String email = uniqueEmail("delete");

        long customerId = createCustomer(
                "Customer To Delete", email, "03001234567");

        mockMvc.perform(delete("/api/customers/" + customerId))
                .andExpect(status().isNoContent());

        assertTrue(customerRepository.findById(customerId).isEmpty());

        mockMvc.perform(get("/api/customers/" + customerId))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnNotFoundWhenDeletingMissingCustomer()
            throws Exception {

        mockMvc.perform(delete("/api/customers/" + Long.MAX_VALUE))
                .andExpect(status().isNotFound());
    }
}
