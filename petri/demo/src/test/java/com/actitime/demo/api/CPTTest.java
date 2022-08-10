package com.actitime.demo.api;

import com.actimind.petri.junit.Transition;
import com.actimind.petri.junit.ValidatePlace;
import com.actimind.petri.model.Token;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import javax.json.Json;
import java.util.List;

import static com.actimind.petri.junit.DSL.networkTest;
import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.preemptive;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;

public class CPTTest {


    @BeforeEach
    public void setup() {
        RestAssured.baseURI = "https://demo.actitime.com/api/v1/";
        RestAssured.authentication = preemptive().basic("admin", "manager");
        RestAssured.filters(
                List.of(new AllureRestAssured())
        );
        //prepare data
        var id = given()
                .queryParam("name", "AllureCustomer1")
                .queryParam("active", "true")
                .get("/customers")
                .body().<Integer>path("items[0].id");

        if (id != null) {
            given().delete("/customers/{id}", id);
        }
    }


    @Test
    @Timeout(120)
    public void mainSuccessScenario() {
        networkTest(new CPTNetwork(), this);
    }

    public static void main(String[] args) {
        new CPTTest().mainSuccessScenario();
    }

    private int createdCustomerId = -1;
    private int createdProjectId = -1;

    @Transition("Create customer")
    public void doCreateCustomer() {
        createdCustomerId = given()
                .contentType(ContentType.JSON)
                .body(Json.createObjectBuilder()
                        .add("name", "AllureCustomer1")
                        .build().toString())
                .post("/customers")
                .then()
                .assertThat()
                .statusCode(200)
                .extract().body().<Integer>path("id")
        ;
    }

    @Transition("Destroy customer")
    public void doDestroyCustomer() {
        given()
                .delete("/customers/{id}", createdCustomerId)
                .then()
                .assertThat()
                .statusCode(204);
        createdCustomerId = -1;
    }

    @ValidatePlace("empty")
    public void validateEmpty(List<Token> tokens) {
        if (tokens.size() > 0) {
            given().queryParam("name", "AllureCustomer1").get("/customers")
                    .then().body("items", empty());
        }
    }

    @Transition("Create project")
    public void doCreateProject() {
        createdProjectId = given()
                .contentType(ContentType.JSON)
                .body(Json.createObjectBuilder()
                        .add("name", "AllureProject1")
                        .add("customerId", createdCustomerId)
                        .build().toString())
                .post("/projects")
                .then()
                .assertThat()
                .statusCode(200)
                .extract().body().<Integer>path("id")
        ;
    }

    @Transition("Delete project")
    public void doDeleteProject() {
        given()
                .delete("/projects/{id}", createdProjectId)
                .then()
                .assertThat()
                .statusCode(204);
        createdProjectId = -1;
    }

    @Transition("Archive project")
    public void doArchiveProject() {
        given()
                .contentType(ContentType.JSON)
                .body(Json.createObjectBuilder()
                        .add("archived", true)
                        .build().toString()
                )
                .patch("/projects/{id}", createdProjectId)
                .then()
                .assertThat()
                .statusCode(200);
    }

    @Transition("Restore project")
    public void doRestoreProject() {
        given()
                .contentType(ContentType.JSON)
                .body(Json.createObjectBuilder()
                        .add("archived", false)
                        .build().toString()
                )
                .patch("/projects/{id}", createdProjectId)
                .then()
                .assertThat()
                .statusCode(200);
    }


    @Transition({"Archive customer", "Archive customer+archived project"})
    public void doArchiveCustomer() {
        given()
                .contentType(ContentType.JSON)
                .body(Json.createObjectBuilder()
                        .add("archived", true)
                        .build().toString()
                )
                .patch("/customers/{id}", createdCustomerId)
                .then()
                .assertThat()
                .statusCode(200);
    }

    @Transition("Restore customer")
    public void doRestoreCustomer() {
        given()
                .contentType(ContentType.JSON)
                .body(Json.createObjectBuilder()
                        .add("archived", false)
                        .build().toString()
                )
                .patch("/customers/{id}", createdCustomerId)
                .then()
                .assertThat()
                .statusCode(200);
    }


    @ValidatePlace("customer exists")
    public void validateCustomerExists(List<Token> tokens) {
        given()
                .queryParam("name", "AllureCustomer1")
                .get("/customers")
                .then().body("items", hasSize(tokens.size()));

    }

    @ValidatePlace("project exists")
    public void validateProjectExists(List<Token> tokens) {
        given()
                .queryParam("name", "AllureProject1")
                .get("/projects")
                .then().body("items", hasSize(tokens.size()));
    }

    @ValidatePlace("existent customer is archived")
    public void validateCustomerArchivedIfExist(List<Token> tokens) {
        //if token - check it is not exist or archived
        //if no token - check it is not exist or active
        var mustBeZeroArchived = tokens.isEmpty();
        given()
                .queryParam("name", "AllureCustomer1")
                .queryParam("archived", mustBeZeroArchived)
                .get("/customers")
                .then().body("items", empty());
    }

    @ValidatePlace("existent project is archived")
    public void validateProjectArchivedIfExist(List<Token> tokens) {
        var mustBeZeroArchived = tokens.isEmpty();
        given()
                .queryParam("name", "AllureProject1")
                .queryParam("archived", mustBeZeroArchived)
                .get("/projects")
                .then().body("items", empty());
    }
}
