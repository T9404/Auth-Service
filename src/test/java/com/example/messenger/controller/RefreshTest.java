package com.example.messenger.controller;

import com.example.messenger.dto.request.RegisterRequest;
import com.example.messenger.dto.response.AuthResponse;
import com.example.messenger.repository.RefreshTokenRepo;
import com.example.messenger.repository.UserRepo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class RefreshTest {
    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private RefreshTokenRepo tokenRepo;
    @Autowired
    private UserRepo userRepo;
    final TestTools testTools = new TestTools();

    @Test
    public void testRefresh() {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setPhoneNumber("+79677572622");
        registerRequest.setPassword("password123");
        registerRequest.setUsername("testuser");
        ResponseEntity<AuthResponse> registerResponse = restTemplate.postForEntity("/users/register", registerRequest, AuthResponse.class);
        assertEquals(HttpStatus.OK, registerResponse.getStatusCode());
        assertNotNull(registerResponse.getHeaders().get(HttpHeaders.SET_COOKIE));

        HttpHeaders headers = testTools.getHandlerForCookieDefault(registerResponse);
        ResponseEntity<AuthResponse> refreshResponse = restTemplate.exchange("/users/refresh", HttpMethod.POST, new HttpEntity<>(null, headers), AuthResponse.class);
        assertEquals(HttpStatus.OK, refreshResponse.getStatusCode());
        assertNotNull(refreshResponse.getHeaders().get(HttpHeaders.SET_COOKIE));
    }

    @Test
    public void testBadRefresh() {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setPhoneNumber("+79677572622");
        registerRequest.setPassword("password123");
        registerRequest.setUsername("testuser");
        ResponseEntity<AuthResponse> registerResponse = restTemplate.postForEntity("/users/register", registerRequest, AuthResponse.class);
        assertEquals(HttpStatus.OK, registerResponse.getStatusCode());
        assertNotNull(registerResponse.getHeaders().get(HttpHeaders.SET_COOKIE));

        HttpHeaders headers = testTools.getHandlerForCookieMaxAge(registerResponse, -1L);
        ResponseEntity<AuthResponse> refreshResponse = restTemplate.exchange("/users/refresh", HttpMethod.POST, new HttpEntity<>(null, headers), AuthResponse.class);
        assertEquals(HttpStatus.FORBIDDEN, refreshResponse.getStatusCode());
        assertNull(refreshResponse.getHeaders().get(HttpHeaders.SET_COOKIE));
    }

    @AfterEach
    public void cleanup() {
        userRepo.deleteAll();
        tokenRepo.deleteAll();
    }
}
