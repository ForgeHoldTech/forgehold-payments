
package com.forgehold.payments.controller;

import com.forgehold.payments.entity.Customer;
import com.forgehold.payments.repository.CustomerRepository;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerRepository customerRepository;

    public CustomerController(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    // Request data validation
    public record CustomerRequest(
        @NotBlank
        @Size(max = 100)
        String fullName,

        @NotBlank
        @Email
        @Size(max = 150)
        String email,

        @NotBlank
        @Size(max = 20)
        String phone
    ) {}

    // 1. Register a new customer
    @PostMapping
    public ResponseEntity<?> createCustomer(
            @Valid @RequestBody CustomerRequest request) {

        String email = request.email().trim().toLowerCase();

        if (customerRepository.findByEmail(email).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("A customer with this email already exists.");
        }

        Customer customer = new Customer();
        customer.setFullName(request.fullName().trim());
        customer.setEmail(email);
        customer.setPhone(request.phone().trim());

        Customer savedCustomer = customerRepository.save(customer);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(savedCustomer);
    }

    // 2. Get all customers
    @GetMapping
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    // 3. Get a customer by ID
    @GetMapping("/{id}")
public ResponseEntity<Customer> getCustomerById(
        @PathVariable Long id) {

    Customer customer = customerRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Customer not found."
            ));

    return ResponseEntity.ok(customer);
}

    // 4. Update an existing customer
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody CustomerRequest request) {

        Customer customer = customerRepository.findById(id)
                .orElse(null);

        if (customer == null) {
            return ResponseEntity.notFound().build();
        }

        String email = request.email().trim().toLowerCase();

        boolean emailUsedByAnotherCustomer =
                customerRepository.findByEmail(email)
                        .filter(existing ->
                                !existing.getId().equals(id))
                        .isPresent();

        if (emailUsedByAnotherCustomer) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("This email is already used by another customer.");
        }

        customer.setFullName(request.fullName().trim());
        customer.setEmail(email);
        customer.setPhone(request.phone().trim());

        Customer updatedCustomer =
                customerRepository.save(customer);

        return ResponseEntity.ok(updatedCustomer);
    }

    // 5. Delete a customer by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCustomer(
            @PathVariable Long id) {

        if (!customerRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        customerRepository.deleteById(id);

        return ResponseEntity.noContent().build();
    }
}
