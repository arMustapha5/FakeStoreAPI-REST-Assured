package com.restassured.product;

import com.restassured.BaseTest;
import io.qameta.allure.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@Epic("FakeStore API Tests")
@Feature("Product Management")
@Story("Add, Update, and Delete Products")
public class ProductAddTest extends BaseTest {

    @Test
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Create product with valid data")
    @Description("Ensure that creating a product with valid fields returns 201 or 200 and the correct field values.")
    public void createProduct_withValidBody_shouldReturn201AndMatchFieldsExceptId() {
        String validProductJson = """
        {
            "title": "Test Product",
            "price": 29.99,
            "description": "A test product",
            "category": "electronics",
            "image": "https://example.com/image.jpg"
        }
        """;

        given()
                .header("Content-Type", "application/json")
                .body(validProductJson)
                .when()
                .post("/products")
                .then()
                .log().all()
                .statusCode(anyOf(is(200), is(201)))
                .body("title", equalTo("Test Product"))
                .body("price", equalTo(29.99f))
                .body("description", equalTo("A test product"))
                .body("category", equalTo("electronics"))
                .body("image", equalTo("https://example.com/image.jpg"));
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Update existing product")
    @Description("Ensure that updating a product by ID returns status 200 and reflects updated data.")
    public void updateProduct_withValidBody_shouldReturn200AndUpdatedFields() {
        String updatedProductJson = """
        {
            "id": 5,
            "title": "Updated Product",
            "price": 99.99,
            "description": "This product has been updated",
            "category": "home",
            "image": "https://example.com/updated-image.jpg"
        }
        """;

        given()
                .header("Content-Type", "application/json")
                .body(updatedProductJson)
                .when()
                .put("/products/5")
                .then()
                .log().all()
                .statusCode(200)
                .body("id", equalTo(5))
                .body("title", equalTo("Updated Product"))
                .body("price", equalTo(99.99f))
                .body("description", equalTo("This product has been updated"))
                .body("category", equalTo("home"))
                .body("image", equalTo("https://example.com/updated-image.jpg"));
    }

    @Test
    @Severity(SeverityLevel.MINOR)
    @DisplayName("Delete product by ID")
    @Description("Ensure that deleting a product by ID returns 200 and product is removed.")
    public void deleteProduct_byId_shouldReturn200() {
        given()
                .when()
                .delete("/products/5")
                .then()
                .log().all()
                .statusCode(200);
    }
}
