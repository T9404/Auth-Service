package com.example.messenger.controller;

import com.example.messenger.dto.request.LoginRequest;
import com.example.messenger.dto.request.RegisterRequest;
import com.example.messenger.dto.response.AuthResponse;
import com.example.messenger.repository.UserRepo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class LoginTest {

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testRegisterAndLogin() {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setPhoneNumber("+79677572622");
        registerRequest.setPassword("password123");
        registerRequest.setUsername("testuser");

        ResponseEntity<AuthResponse> registerResponse = restTemplate.postForEntity("/users/register", registerRequest, AuthResponse.class);
        assertEquals(HttpStatus.OK, registerResponse.getStatusCode());
        assertNotNull(registerResponse.getHeaders().get("Set-Cookie"));

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setPhoneNumber("+79677572622");
        loginRequest.setPassword("password123");

        ResponseEntity<AuthResponse> loginResponse = restTemplate.postForEntity("/users/login", loginRequest, AuthResponse.class);
        assertEquals(HttpStatus.OK, loginResponse.getStatusCode());
        assertNotNull(loginResponse.getHeaders().get("Set-Cookie"));
    }

    @Test
    public void testBadPhoneRegisterAndLogin() {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setPhoneNumber("+79677572622");
        registerRequest.setPassword("password123");
        registerRequest.setUsername("testuser");

        ResponseEntity<AuthResponse> registerResponse = restTemplate.postForEntity("/users/register", registerRequest, AuthResponse.class);
        assertEquals(HttpStatus.OK, registerResponse.getStatusCode());
        assertNotNull(registerResponse.getHeaders().get("Set-Cookie"));

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setPhoneNumber("+79633333333"); // not equals: +79677572622
        loginRequest.setPassword("password123");

        ResponseEntity<AuthResponse> loginResponse = restTemplate.postForEntity("/users/login", loginRequest, AuthResponse.class);
        assertEquals(HttpStatus.NOT_FOUND, loginResponse.getStatusCode());
        assertNull(loginResponse.getHeaders().get("Set-Cookie"));
    }

    @AfterEach
    public void cleanup() {
        userRepo.deleteAll();
    }
}
