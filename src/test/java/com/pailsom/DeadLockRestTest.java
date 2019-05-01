package com.pailsom;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;
import com.pailsom.exceptions.InvalidDepositAmountException;
import com.pailsom.exceptions.InvalidTransferException;
import com.pailsom.exceptions.NotEnoughMoneyException;
import io.vertx.core.json.JsonObject;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.jayway.restassured.RestAssured.get;
import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.patch;

public class DeadLockRestTest {
    static  App app;
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
    public void test(){
        List<Integer> accounts = Arrays.asList("A","B","C").
                stream().map(k->createAccount(k))
                .collect(Collectors.toList());
        accounts.stream().forEach(id->{
                deposit(id,100000);
        });
        LongAdder counter = new LongAdder();
        List<Runnable> transferFunctions = new ArrayList<>(6000);
        IntStream.range(0,1000).forEach(t->{
            transferFunctions.add(transactionRequest(accounts.get(0),accounts.get(1)));
            transferFunctions.add(transactionRequest(accounts.get(0),accounts.get(2)));
            transferFunctions.add(transactionRequest(accounts.get(2),accounts.get(1)));
            transferFunctions.add(transactionRequest(accounts.get(2),accounts.get(0)));
            transferFunctions.add(transactionRequest(accounts.get(1),accounts.get(2)));
            transferFunctions.add(transactionRequest(accounts.get(1),accounts.get(0)));
        });
        transferFunctions.parallelStream()
                .map((Function<Runnable, Void>) runnable -> {
                    runnable.run();
                    return null;
                })
                .forEach(x -> counter.increment());
        Assert.assertEquals(6000,counter.intValue());
        Assert.assertEquals(300000,accounts.stream().map(k->getUser(k)).mapToInt(t->t.intValue()).sum());
    }

    private Runnable transactionRequest(int id1, int id2) {
        return () -> {
            try {
                JsonObject data = new JsonObject();
                data.put("from", id1);
                data.put("to", id2);
                data.put("amount", 1);
                Response response = given().body(data.getMap()).request().post("/transfer");
                Assert.assertEquals(200, response.statusCode());
            }catch (Exception ex){

            }
        };
    }

    public int createAccount(String name){
        Response response = given().body("{\"name\":\""+name+"\", \"type\":\"saving\"}")
                .request().post("/account/create");
        Assert.assertEquals(200,response.statusCode());
        return response.body().jsonPath().getInt("id");
    }

    public void deposit(int accId,long amount)  {

        Response response = patch("/account/"+accId+"/deposit/"+amount)
                .then().contentType(ContentType.JSON).extract().response();
        Assert.assertEquals(200,response.statusCode());
    }
    public int getUser(int accId){
        Response response = get("/account/"+accId)
                .then().contentType(ContentType.JSON).extract().response();
        return response.body().jsonPath().getInt("amount");
    }
}
