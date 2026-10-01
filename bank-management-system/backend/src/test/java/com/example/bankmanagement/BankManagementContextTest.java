package com.example.bankmanagement;

import com.example.bankmanagement.repository.BankTransactionRepository;
import com.example.bankmanagement.repository.CustomerRepository;
import com.example.bankmanagement.repository.OfficerRepository;
import com.example.bankmanagement.security.SecurityConfig;
import com.example.bankmanagement.entity.Customer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:banktest;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.jpa.show-sql=false",
        "bank.officer.bootstrap.username=",
        "bank.officer.bootstrap.password="
})
class BankManagementContextTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void prepareCustomer() {
        customerRepository.deleteAll();
        Customer customer = new Customer();
        customer.setFullName("Test Customer");
        customer.setEmail("test@example.test");
        customer.setPhone("5550100");
        customer.setDateOfBirth(LocalDate.of(1990, 1, 1));
        customer.setAddress("Test address");
        customer.setUsername("testcustomer");
        customer.setPasswordHash(passwordEncoder.encode("TestPassword1!"));
        customer.setAccountNumber("AC1000000001");
        customer.setAccountType("Savings");
        customer.setAccountStatus("ACTIVE");
        customer.setBalance(new BigDecimal("50.00"));
        customerRepository.save(customer);
    }

    @Test
    void securityAndBankingRepositoriesLoad() {
        assertNotNull(applicationContext.getBean(SecurityConfig.class));
        assertNotNull(applicationContext.getBean(CustomerRepository.class));
        assertNotNull(applicationContext.getBean(OfficerRepository.class));
        assertNotNull(applicationContext.getBean(BankTransactionRepository.class));
    }

        @Test
        void customerLoginEndpointAuthenticatesAndReturnsNoPasswordData() throws Exception {
        String request = objectMapper.writeValueAsString(Map.of(
            "usernameOrEmail", "testcustomer", "password", "TestPassword1!"));

        mockMvc.perform(post("/api/auth/customer/login")
                .contentType(MediaType.APPLICATION_JSON).content(request))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.customer.username").value("testcustomer"))
            .andExpect(jsonPath("$.customer.accountStatus").value("ACTIVE"))
            .andExpect(jsonPath("$.customer.passwordHash").doesNotExist())
                .andExpect(request().sessionAttribute("SPRING_SECURITY_CONTEXT", org.hamcrest.Matchers.notNullValue()));
        }

        @Test
        void legacyCustomerLoginUrlRemainsAvailable() throws Exception {
        String request = objectMapper.writeValueAsString(Map.of(
            "usernameOrEmail", "testcustomer", "password", "TestPassword1!"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON).content(request))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.customer.accountStatus").value("ACTIVE"));
        }

        @Test
        void pendingCustomerGetsStatusMessageWithoutBankingSession() throws Exception {
        Customer customer = customerRepository.findByUsername("testcustomer").orElseThrow();
        customer.setAccountStatus("PENDING");
        customerRepository.save(customer);
        String request = objectMapper.writeValueAsString(Map.of(
            "usernameOrEmail", "testcustomer", "password", "TestPassword1!"));

        mockMvc.perform(post("/api/auth/customer/login")
                .contentType(MediaType.APPLICATION_JSON).content(request))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Your account is pending bank officer approval."))
                .andExpect(request().sessionAttributeDoesNotExist("SPRING_SECURITY_CONTEXT"));
        }

            @Test
            void blockedCustomerGetsStatusMessageWithoutBankingSession() throws Exception {
            Customer customer = customerRepository.findByUsername("testcustomer").orElseThrow();
            customer.setAccountStatus("BLOCKED");
            customerRepository.save(customer);
            String request = objectMapper.writeValueAsString(Map.of(
                "usernameOrEmail", "testcustomer", "password", "TestPassword1!"));

            mockMvc.perform(post("/api/auth/customer/login")
                    .contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Your account has been blocked. Please contact the bank."))
                .andExpect(request().sessionAttributeDoesNotExist("SPRING_SECURITY_CONTEXT"));
            }

        @Test
        void rejectedCustomerCannotLogIn() throws Exception {
        Customer customer = customerRepository.findByUsername("testcustomer").orElseThrow();
        customer.setAccountStatus("REJECTED");
        customerRepository.save(customer);
        String request = objectMapper.writeValueAsString(Map.of(
            "usernameOrEmail", "testcustomer", "password", "TestPassword1!"));

        mockMvc.perform(post("/api/auth/customer/login")
                .contentType(MediaType.APPLICATION_JSON).content(request))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Your account registration has been rejected."));
        }
}