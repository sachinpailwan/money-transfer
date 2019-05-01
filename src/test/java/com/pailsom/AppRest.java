package com.pailsom;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
import com.pailsom.controller.RestVerticleController;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.internal.builders.JUnit4Builder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static com.jayway.restassured.RestAssured.get;
import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.patch;

public class AppRest {

    static App app ;


    @BeforeClass
    public static void setup() throws IOException {
        app = new App();
        app.startServer();
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = Integer.getInteger("http.port", 8091);
    }

    @AfterClass
    public static void tearDown() {
        RestAssured.reset();
        app.stopServer();
    }

    @Test
    public void createAccount(){
        Response response = given().body("{\"name\":\"Somnath\", \"type\":\"saving\"}")
                .request().post("/account/create");
        Assert.assertEquals(200,response.statusCode());
        int accountId = response.body().jsonPath().getInt("id");

        response = given().body("{\"name\":\"James\", \"type\":\"saving\"}")
                .request().post("/account/create");
        Assert.assertEquals(200,response.statusCode());
        int accountId2 = response.body().jsonPath().getInt("id");
        Assert.assertEquals(accountId+1,accountId2);
    }

    @Test
    public void deposit(){
        double depositAmount = 0.1;
        // create account
        Response response = given().body("{\"name\":\"Somnath\", \"type\":\"saving\"}")
                .request().post("/account/create");
        Assert.assertEquals(200,response.statusCode());
        int accountId = response.body().jsonPath().getInt("id");

        // deposit amount
        response = patch("/account/"+accountId+"/deposit/"+depositAmount)
                .then().contentType(ContentType.JSON).extract().response();
        Assert.assertEquals(200,response.statusCode());
        JsonPath jsonPath = response.jsonPath();
        Assert.assertEquals(accountId,jsonPath.getInt("id"));
        Assert.assertTrue(depositAmount == jsonPath.getDouble("amount"));
        Assert.assertEquals("Somnath" , jsonPath.getString("name"));

        // invalid deposit
        response = get("/account/"+accountId+"/deposit/"+-1)
                .then().contentType(ContentType.JSON).extract().response();
        Assert.assertEquals(400,response.statusCode());
        Assert.assertTrue(response.body().print().contains("Invalid deposit"));
    }

    @Test
    public void transfer(){

        // create account from
        Response response = given().body("{\"name\":\"Somnath\", \"type\":\"saving\"}")
                .request().post("/account/create");
        Assert.assertEquals(200,response.statusCode());
        int fromAccountId = response.body().jsonPath().getInt("id");

        response = patch("/account/"+fromAccountId+"/deposit/"+134.45)
                .then().contentType(ContentType.JSON).extract().response();
        Assert.assertEquals(200,response.statusCode());
        float fromAmount = response.body().jsonPath().getFloat("amount");
        // create account to
        response = given().body("{\"name\":\"Pailwan\", \"type\":\"saving\"}")
                .request().post("/account/create");
        Assert.assertEquals(200,response.statusCode());
        int toAccountId = response.body().jsonPath().getInt("id");

        response = get("/account/"+toAccountId+"/deposit/"+1000.45)
                .then().contentType(ContentType.JSON).extract().response();
        Assert.assertEquals(200,response.statusCode());
        float toAmount = response.body().jsonPath().getFloat("amount");

        // transfer
        JsonObject data = new JsonObject();
        data.put("from",fromAccountId);
        data.put("to",toAccountId);
        data.put("amount",34.45);
        response = given().body(data.getMap()).request().post("/transfer");
        Assert.assertEquals(200,response.statusCode());
        JsonPath jsonPath = response.body().jsonPath();
        List<Integer> ids = jsonPath.getList("id");
        Assert.assertTrue(fromAccountId == ids.get(0));
        Assert.assertTrue(toAccountId == ids.get(1));
        List<Float> amount = jsonPath.getList("amount");
        Assert.assertTrue((toAmount+34.45f) == amount.get(1).floatValue());
        Assert.assertEquals(amount.get(0).floatValue(),fromAmount,34.45f);

        // NotEnoughMoneyException
        data = new JsonObject();
        data.put("from",fromAccountId);
        data.put("to",toAccountId);
        data.put("amount","3000.45");
        response = given().body(data.getMap()).request().post("/transfer");
        Assert.assertEquals(404,response.statusCode());
        Assert.assertTrue(response.body().print().contains("no enough money"));

        // InvalidTransferException
        data = new JsonObject();
        data.put("from",fromAccountId);
        data.put("to",fromAccountId);
        data.put("amount","3000.45");
        response = given().body(data.getMap()).request().post("/transfer");
        Assert.assertEquals(404,response.statusCode());
        Assert.assertTrue(response.body().print().contains("transfer money from account to itself"));

        // account not found
        data = new JsonObject();
        data.put("from",100);
        data.put("to",fromAccountId);
        data.put("amount","3000.45");
        response = given().body(data.getMap()).request().post("/transfer");
        Assert.assertEquals(404,response.statusCode());
        Assert.assertTrue(response.body().print().contains("One of accounts not found"));
    }
}
