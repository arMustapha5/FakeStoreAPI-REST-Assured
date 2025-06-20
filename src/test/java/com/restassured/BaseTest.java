package com.restassured;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;

public class BaseTest {

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = "https://fakestoreapi.com";
        RestAssured.basePath = "";

        RestAssured.filters(new AllureRestAssured());
    }
}

