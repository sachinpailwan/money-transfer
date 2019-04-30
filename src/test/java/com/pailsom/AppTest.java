package com.pailsom;

import static org.junit.Assert.assertTrue;

import com.pailsom.controller.RestVerticleController;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import java.io.IOException;

@RunWith(VertxUnitRunner.class)
public class AppTest {
    private static Vertx vertx;
    private static Integer port;

    @BeforeClass
    public static void setup(TestContext testContext) throws IOException {
        vertx = Vertx.vertx();
        DeploymentOptions options = new DeploymentOptions()
                .setConfig(new JsonObject()
                        .put("http.port", port)
                        .put("db_name", "money-transfer")
                );
        port = 8091;
        vertx.deployVerticle(RestVerticleController.class.getName(), options,
                testContext.asyncAssertSuccess());
    }

    @AfterClass
    public static void tearDown(TestContext testContext) {
        vertx.undeploy(RestVerticleController.class.getName());
        vertx.close(testContext.asyncAssertSuccess());
    }

    @Test
    public void accountCreationTests(TestContext testContext) {
        final Async async = testContext.async();
        HttpClient httpClient = vertx.createHttpClient();
        HttpClientRequest request = httpClient.post(port, "localhost", "/account/create", response -> {
            response.handler(body -> {
                JsonObject jsonObject = new JsonObject(body);
                testContext.assertNotNull(jsonObject.getInteger("id"));
                testContext.assertTrue("somnath".equalsIgnoreCase(jsonObject.getString("name")));
                testContext.assertTrue("saving".equalsIgnoreCase(jsonObject.getString("type")));
                testContext.assertTrue(response.statusCode() == 200);
                async.complete();
            });
        });
        JsonObject jsonObject = new JsonObject();
        jsonObject.put("name", "somnath");
        jsonObject.put("type", "saving");
        request.putHeader("content-type", "application/json");
        request.end(jsonObject.toBuffer());
    }
}
