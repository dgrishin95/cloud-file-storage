package com.mysite.cloudfilestorage.controller;

import com.mysite.cloudfilestorage.dto.AuthRequest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthControllerTest {

    @LocalServerPort
    private Integer port;

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14.6");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setup() {
        RestAssured.baseURI = "http://localhost:" + port + "/api/auth";
        jdbcTemplate.execute("TRUNCATE TABLE users RESTART IDENTITY CASCADE");
    }

    @Test
    @DisplayName("POST /auth/sign-up - 201 successful create")
    void testSignUp_SuccessfulRegister() {
        Response response = successfulRegisterResponse();

        Assertions.assertEquals("user_1", response.jsonPath().getString("username"));
    }

    @Test
    @DisplayName("POST /auth/sign-up - 400 if the username is too short")
    void testSignUp_UsernameTooShort() {
        usernameTooShortResponse("/sign-up");
    }

    @Test
    @DisplayName("POST /auth/sign-up - 400 if the password is too short")
    void testSignUp_PasswordTooShort() {
        passwordTooShortResponse("/sign-up");
    }

    @Test
    @DisplayName("POST /auth/sign-up - 409 if the username is already exists")
    void testSignUp_UsernameAlreadyExists() {
        successfulRegisterResponse();

        given()
                .contentType(ContentType.JSON)
                .body(validAuthRequest())
                .when()
                .post("/sign-up")
                .then()
                .statusCode(HttpStatus.CONFLICT.value())
                .body("message", equalTo("Username is busy"));
    }

    @Test
    @DisplayName("POST /auth/sign-in - 200 successful login")
    void testSignIn_SuccessfulLogin() {
        successfulRegisterResponse();

        Response response = successfulLoginResponse();

        Assertions.assertEquals("user_1", response.jsonPath().getString("username"));
    }

    @Test
    @DisplayName("POST /auth/sign-in - 400 if the username is too short")
    void testSignIn_UsernameTooShort() {
        usernameTooShortResponse("/sign-in");
    }

    @Test
    @DisplayName("POST /auth/sign-in - 400 if the password is too short")
    void testSignIn_PasswordTooShort() {
        passwordTooShortResponse("/sign-in");
    }

    @Test
    @DisplayName("POST /auth/sign-in - 401 there is no user")
    void testSignIn_ThereIsNoUser() {
        Response response = given()
                .contentType(ContentType.JSON)
                .body(validAuthRequest())
                .when()
                .post("/sign-in")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .extract()
                .response();

        Assertions.assertEquals("There is no such user, or the password is incorrect",
                response.jsonPath().getString("message"));
    }

    @Test
    @DisplayName("POST /auth/sign-out - 204 successful logout")
    void testSignOut_SuccessfulLogout() {
        successfulRegisterResponse();
        Response response = successfulLoginResponse();

        String sessionId = response.sessionId();
        String cookie = response.cookie("JSESSIONID");

        given()
                .when()
                .sessionId(sessionId)
                .cookie(cookie)
                .post("/sign-out")
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value())
                .extract()
                .response();
    }

    @Test
    @DisplayName("POST /auth/sign-out - 401 unauthorized user")
    void testSignOut_UnauthorizedUser() {
        given()
                .when()
                .post("/sign-out")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .body("message", equalTo("The request is executed by an unauthorized user"));
    }

    @Test
    @DisplayName("GET /user/me - 200 successful response")
    void testGettingMessage() {
        successfulRegisterResponse();
        Response loginResponse = successfulLoginResponse();

        String sessionId = loginResponse.sessionId();
        String cookie = loginResponse.cookie("JSESSIONID");

        given()
                .when()
                .sessionId(sessionId)
                .cookie(cookie)
                .get("http://localhost:" + port + "/api/user/me")
                .then()
                .statusCode(200)
                .body("username", equalTo(validAuthRequest().getUsername()));
    }

    private Response successfulRegisterResponse() {
        return given()
                .contentType(ContentType.JSON)
                .body(validAuthRequest())
                .when()
                .post("/sign-up")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract()
                .response();
    }

    private Response successfulLoginResponse() {
        return given()
                .contentType(ContentType.JSON)
                .body(validAuthRequest())
                .when()
                .post("/sign-in")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .response();
    }

    private AuthRequest validAuthRequest() {
        return new AuthRequest("user_1", "password");
    }

    private void usernameTooShortResponse(String methodUrl) {
        AuthRequest authRequest = new AuthRequest("us", "password");

        given()
                .contentType(ContentType.JSON)
                .body(authRequest)
                .when()
                .post(methodUrl)
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("message", equalTo("Username must be between 3 and 50 characters"));
    }

    private void passwordTooShortResponse(String methodUrl) {
        AuthRequest authRequest = new AuthRequest("user_1", "pa");

        given()
                .contentType(ContentType.JSON)
                .body(authRequest)
                .when()
                .post(methodUrl)
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("message", equalTo("Password must be at least 5 characters"));
    }
}
