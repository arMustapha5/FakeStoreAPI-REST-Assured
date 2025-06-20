package com.restassured.user;

import com.restassured.BaseTest;
import io.qameta.allure.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@Epic("FakeStore API Tests")
@Feature("User Management")
@Story("User CRUD Operations")
public class UserTest extends BaseTest {

    @Test
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Get all users")
    @Description("Ensure /users returns a non-empty list of users with essential fields.")
    public void getAllUsers_shouldReturn200AndUserList() {
        given()
                .when()
                .get("/users")
                .then()
                .log().all()
                .statusCode(200)
                .body("$", not(empty()))
                .body("[0].id", notNullValue())
                .body("[0].username", notNullValue())
                .body("[0].email", notNullValue())
                .body("[0].password", notNullValue());
    }

    @Test
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Create a new user")
    @Description("Create a user using valid JSON and verify a 200/201 response with an ID.")
    public void createUser_withValidBody_shouldReturn201Or200() {
        String newUser = """
        {
            "username": "rauf",
            "email": "rauf@gmail.com",
            "password": "secure123"
        }
        """;

        given()
                .header("Content-Type", "application/json")
                .body(newUser)
                .when()
                .post("/users")
                .then()
                .log().all()
                .statusCode(anyOf(is(200), is(201)))
                .body("id", notNullValue());
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Get user by ID")
    @Description("Fetch a user by ID and verify it contains expected fields.")
    public void getUserById_shouldReturn200AndUserDetails() {
        given()
                .when()
                .get("/users/1")
                .then()
                .log().all()
                .statusCode(200)
                .body("id", equalTo(1))
                .body("username", notNullValue())
                .body("email", notNullValue())
                .body("password", notNullValue());
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Update a user")
    @Description("Update a user's details with valid input and confirm updated fields.")
    public void updateUser_shouldReturn200WithUpdatedValues() {
        String updatedUser = """
        {
            "id": 1,
            "username": "rauf_updated",
            "email": "rauf_updated@gmail.com",
            "password": "newpass456"
        }
        """;

        given()
                .header("Content-Type", "application/json")
                .body(updatedUser)
                .when()
                .put("/users/1")
                .then()
                .log().all()
                .statusCode(200)
                .body("id", equalTo(1))
                .body("username", equalTo("rauf_updated"))
                .body("email", equalTo("rauf_updated@gmail.com"));
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Delete a user")
    @Description("Delete a user by ID and verify successful deletion with 200 status.")
    public void deleteUser_shouldReturn200() {
        given()
                .when()
                .delete("/users/1")
                .then()
                .log().all()
                .statusCode(200);
    }

    @Test
    @Severity(SeverityLevel.MINOR)
    @DisplayName("Create user with invalid JSON")
    @Description("Verify API returns 400 when given an invalid JSON payload for user creation.")
    public void createUser_withInvalidJson_shouldReturn400() {
        String invalidUserJson = """
        {
            "username": "rauf"
            "email": "rauf@gmail.com"
            "password": "secure123"
        }
        """;

        given()
                .header("Content-Type", "application/json")
                .body(invalidUserJson)
                .when()
                .post("/users")
                .then()
                .log().all()
                .statusCode(400);
    }

    @Test
    @Severity(SeverityLevel.MINOR)
    @DisplayName("Update user with invalid JSON")
    @Description("Verify API returns 400 when given an invalid JSON payload for user update.")
    public void updateUser_withInvalidJson_shouldReturn400() {
        String invalidUpdateJson = """
        {
            "id": 1
            "username": "invalid"
            "email": "bademail.com"
            "password": "123"
        }
        """;

        given()
                .header("Content-Type", "application/json")
                .body(invalidUpdateJson)
                .when()
                .put("/users/1")
                .then()
                .log().all()
                .statusCode(400);
    }
}
