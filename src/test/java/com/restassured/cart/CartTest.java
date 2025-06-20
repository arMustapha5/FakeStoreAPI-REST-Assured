package com.restassured.cart;

import com.restassured.BaseTest;
import io.qameta.allure.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@Epic("FakeStore API Tests")
@Feature("Cart Management")
@Story("Operations on Carts")
public class CartTest extends BaseTest {

    @Test
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Get all carts")
    @Description("Verify that the /carts endpoint returns a non-empty list of carts with valid fields.")
    public void getAllCarts_shouldReturn200AndListOfCarts() {
        given()
                .when()
                .get("/carts")
                .then()
                .log().all()
                .statusCode(200)
                .body("$", not(empty()))
                .body("[0].id", notNullValue())
                .body("[0].userId", notNullValue())
                .body("[0].products", notNullValue());
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Create a new cart")
    @Description("Create a cart with a valid JSON payload and verify it returns a 200 or 201 with correct data.")
    public void createCart_withValidBody_shouldReturn201() {
        String newCartJson = """
        {
            "userId": 5,
            "products": [
                {
                    "productId": 1,
                    "quantity": 2
                },
                {
                    "productId": 2,
                    "quantity": 1
                }
            ]
        }
    """;

        given()
                .header("Content-Type", "application/json")
                .body(newCartJson)
                .when()
                .post("/carts")
                .then()
                .log().all()
                .statusCode(anyOf(is(200), is(201)))
                .body("id", not(empty()))
                .body("userId", equalTo(5))
                .body("products", not(empty()));
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Get cart by ID")
    @Description("Verify that the cart with a specific ID can be fetched and contains expected fields.")
    public void getCartById_shouldReturn200AndCartDetails() {
        given()
                .when()
                .get("/carts/1")
                .then()
                .log().all()
                .statusCode(200)
                .body("id", equalTo(1))
                .body("userId", notNullValue())
                .body("products", notNullValue());
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Update a cart")
    @Description("Update a cart with a valid payload and verify the product quantity was updated correctly.")
    public void updateCart_shouldReturn200WithUpdatedValues() {
        String updatedCartJson = """
        {
            "id": 1,
            "userId": 1,
            "products": [
                { "productId": 1, "quantity": 5 }
            ]
        }
    """;

        given()
                .header("Content-Type", "application/json")
                .body(updatedCartJson)
                .when()
                .put("/carts/1")
                .then()
                .log().all()
                .statusCode(200)
                .body("id", equalTo(1))
                .body("products[0].quantity", equalTo(5));
    }

    @Test
    @Severity(SeverityLevel.MINOR)
    @DisplayName("Delete a cart")
    @Description("Delete a cart by ID and verify the response status is 200.")
    public void deleteCart_shouldReturn200() {
        given()
                .when()
                .delete("/carts/1")
                .then()
                .log().all()
                .statusCode(200);
    }
}
