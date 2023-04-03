package com.example.messenger.controller;

import com.example.messenger.dto.request.RegisterRequest;
import com.example.messenger.dto.response.AuthResponse;
import com.example.messenger.repository.UserRepo;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class RegisterTest {

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private UserRepo userRepo;

    @Test
    public void testFirstRegister() {
        RegisterRequest request = new RegisterRequest();
        request.setPhoneNumber("+79677572622");
        request.setPassword("password123");
        request.setUsername("testuser");
        ResponseEntity<AuthResponse> response = restTemplate.postForEntity("/users/register", request, AuthResponse.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testSecondRegister() {
        RegisterRequest request = new RegisterRequest();
        request.setPhoneNumber("+79677572622");
        request.setPassword("password123");
        request.setUsername("testuser");
        request.setEmail("ganm@gmail.com");
        request.setName("Gordey");
        request.setSurname("Danax");
        ResponseEntity<AuthResponse> response = restTemplate.postForEntity("/users/register", request, AuthResponse.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testWithoutPhoneNumberRegister() {
        RegisterRequest request = new RegisterRequest();
        request.setPassword("passwodd1234");
        request.setUsername("testuser");
        ResponseEntity<AuthResponse> response = restTemplate.postForEntity("/users/register", request, AuthResponse.class);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void testWithoutUsernameRegister() {
        RegisterRequest request = new RegisterRequest();
        request.setPhoneNumber("+79677572622");
        request.setPassword("password123");
        ResponseEntity<AuthResponse> response = restTemplate.postForEntity("/users/register", request, AuthResponse.class);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void testWithoutPasswordRegister() {
        RegisterRequest request = new RegisterRequest();
        request.setPhoneNumber("+79777572622");
        request.setUsername("testuser1");
        ResponseEntity<AuthResponse> response = restTemplate.postForEntity("/users/register", request, AuthResponse.class);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void testBadPhoneNumberFirstRegister() {
        RegisterRequest request = new RegisterRequest();
        request.setPhoneNumber("79677572622"); // invalid format (missed symbol "+")
        request.setPassword("password123");
        request.setUsername("testuser");
        ResponseEntity<AuthResponse> response = restTemplate.postForEntity("/users/register", request, AuthResponse.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void testBadPhoneNumberSecondRegister() {
        RegisterRequest request = new RegisterRequest();
        request.setPhoneNumber("+7967757262"); // invalid format (count numbers)
        request.setPassword("password123");
        request.setUsername("testuser");
        ResponseEntity<AuthResponse> response = restTemplate.postForEntity("/users/register", request, AuthResponse.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void testBadPasswordFirstRegister() {
        RegisterRequest request = new RegisterRequest();
        request.setPhoneNumber("+79677572622");
        request.setPassword("123"); // length: 8-64
        request.setUsername("testuser");
        ResponseEntity<AuthResponse> response = restTemplate.postForEntity("/users/register", request, AuthResponse.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void testBadPasswordSecondRegister() {
        RegisterRequest request = new RegisterRequest();
        request.setPhoneNumber("+79677572622");
        request.setPassword("1".repeat(65)); // length: 8-64
        request.setUsername("testuser");
        ResponseEntity<AuthResponse> response = restTemplate.postForEntity("/users/register", request, AuthResponse.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void testBadUsernameFirstRegister() {
        RegisterRequest request = new RegisterRequest();
        request.setPhoneNumber("+79677572622");
        request.setPassword("password123");
        request.setUsername("AA"); // length: 3-20
        ResponseEntity<AuthResponse> response = restTemplate.postForEntity("/users/register", request, AuthResponse.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void testBadUsernameSecondRegister() {
        RegisterRequest request = new RegisterRequest();
        request.setPhoneNumber("+79677572622");
        request.setPassword("password123");
        request.setUsername("A".repeat(21)); // length: 3-20
        ResponseEntity<AuthResponse> response = restTemplate.postForEntity("/users/register", request, AuthResponse.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void testBadEmailRegisterFirst() {
        RegisterRequest request = new RegisterRequest();
        request.setPhoneNumber("+79677572622");
        request.setPassword("password123");
        request.setUsername("testuser");
        request.setEmail("ganmgmail.com"); // missed symbol "@"
        ResponseEntity<AuthResponse> response = restTemplate.postForEntity("/users/register", request, AuthResponse.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void testBadNameRegisterFirst() {
        RegisterRequest request = new RegisterRequest();
        request.setPhoneNumber("+79677572622");
        request.setPassword("password123");
        request.setUsername("testuser");
        request.setName("A".repeat(21)); // length: 1-20
        ResponseEntity<AuthResponse> response = restTemplate.postForEntity("/users/register", request, AuthResponse.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void testBadSurnameRegisterFirst() {
        RegisterRequest request = new RegisterRequest();
        request.setPhoneNumber("+79677572622");
        request.setPassword("password123");
        request.setUsername("testuser");
        request.setSurname("A".repeat(21)); // length: 1-20
        ResponseEntity<AuthResponse> response = restTemplate.postForEntity("/users/register", request, AuthResponse.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @AfterEach
    public void cleanup() {
        userRepo.deleteAll();
    }

}