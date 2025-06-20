package com.restassured.product;

import com.restassured.BaseTest;
import io.qameta.allure.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.*;
        import static org.hamcrest.Matchers.*;

@Epic("FakeStore API Tests")
@Feature("Product Management")
@Story("Fetch Products")
public class ProductGetTest extends BaseTest {

    @Test
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Get all products")
    @Description("Verify that the /products endpoint returns a list of products with valid fields.")
    public void getAllProducts_shouldReturn200AndValidSchema() {
        given()
                .when()
                .get("/products")
                .then()
                .statusCode(200)
                .body("$", not(empty()))
                .body("[0].id", notNullValue())
                .body("[0].title", notNullValue())
                .body("[0].price", greaterThan(0f))
                .body("[0].category", notNullValue());
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Get product by ID")
    @Description("Verify that retrieving a product by ID returns status 200 and contains expected fields.")
    public void getProductById_shouldReturn200AndValidProductFields() {
        given()
                .when()
                .get("/products/1")
                .then()
                .log().all()
                .statusCode(200)
                .body("id", equalTo(1))
                .body("title", notNullValue())
                .body("price", greaterThan(0f))
                .body("description", notNullValue())
                .body("category", notNullValue())
                .body("image", allOf(notNullValue(), containsString("http"))); // assuming it's a URI
    }
}
