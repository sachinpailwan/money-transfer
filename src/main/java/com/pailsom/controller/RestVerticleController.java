package com.pailsom.controller;

import com.google.inject.Guice;
import com.pailsom.config.AccountModule;
import com.pailsom.dto.AccountDTO;
import com.pailsom.exceptions.InvalidDepositAmountException;
import com.pailsom.exceptions.InvalidTransferException;
import com.pailsom.exceptions.NotEnoughMoneyException;
import com.pailsom.model.Account;
import com.pailsom.repository.AccountRepository;
import com.pailsom.repository.IAccountRepository;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RestVerticleController extends AbstractVerticle{

    public static final String ONE_OF_ACCOUNTS_NOT_FOUND = "One of accounts not found";
    public static final String YOU_CAN_T_TRANSFER_MONEY_FROM_ACCOUNT_TO_ITSELF = "You can't transfer money from account to itself";
    public static final String USER_WITH_ID_0_HAS_NO_ENOUGH_MONEY = "User with id {0} has no enough money";
    private IAccountRepository repository;
    int port = 8091;

    @Override
    public void init(Vertx vertx, Context context) {
        super.init(vertx, context);
        repository = Guice.createInjector(new AccountModule()).getInstance(AccountRepository.class);
    }

    @Override
    public void start(Future<Void> future) {

        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        // CORS support
        Set<String> allowHeaders = new HashSet<>();
        allowHeaders.add("x-requested-with");
        allowHeaders.add("Access-Control-Allow-Origin");
        allowHeaders.add("origin");
        allowHeaders.add("Content-Type");
        allowHeaders.add("accept");
        Set<HttpMethod> allowMethods = new HashSet<>();
        allowMethods.add(HttpMethod.GET);
        allowMethods.add(HttpMethod.POST);
        allowMethods.add(HttpMethod.DELETE);
        allowMethods.add(HttpMethod.PATCH);
        router.route().handler(CorsHandler.create("*")
                .allowedHeaders(allowHeaders)
                .allowedMethods(allowMethods));
        router.post("/account/create").handler(this::createAccount);
        router.patch("/account/:id/deposit/:amount").handler(this::deposit);
        router.post("/transfer").handler(this::transfer);
        router.get("/account/:id").handler(this::getUser);

        vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(port, result -> {
                    if (result.succeeded()) {
                        future.complete();
                    } else {
                        future.fail(result.cause());
                    }
                });
        System.out.println("vertex is available on port :"+port);
    }

    private void getUser(RoutingContext routingContext) {
        int id = Integer.parseInt(routingContext.request().getParam("id"));
        routingContext.response()
                .putHeader("content-type", "application/json")
                .setStatusCode(200)
                .end(Json.encodePrettily(repository.getUser(id)));
    }


    private void createAccount(RoutingContext routingContext) {

        JsonObject jsonObject = routingContext.getBodyAsJson();
        routingContext.response()
                .putHeader("content-type", "application/json")
                .setStatusCode(200)
                .end(Json.encodePrettily(repository.createAccount(jsonObject.getString("name"),jsonObject.getString("type"))));
    }

    private void deposit(RoutingContext routingContext) {
        int id = Integer.parseInt(routingContext.request().getParam("id"));
        BigDecimal amount = new BigDecimal(routingContext.request().getParam("amount"));

        AccountDTO accountDTO = null;
        try {
            accountDTO = repository.deposit(id,amount);
            if(null != accountDTO) {
                routingContext.response()
                        .putHeader("content-type", "application/json")
                        .setStatusCode(200)
                        .end(Json.encodePrettily(accountDTO));
            }else {
                routingContext.response()
                        .putHeader("content-type", "application/json")
                        .setStatusCode(404)
                        .end("Account "+id+" not found");
            }
        } catch (InvalidDepositAmountException e) {
            routingContext.response()
                    .putHeader("content-type", "application/json")
                    .setStatusCode(400)
                    .end("Invalid deposit "+amount+" amount received");
        }

    }

    private void transfer(RoutingContext routingContext) {
        JsonObject jsonObject = routingContext.getBodyAsJson();
        int statusCode = 404;
        String msg = "";
        try {
            List<AccountDTO> transfer = repository.transfer(jsonObject.getInteger("from")
                    ,jsonObject.getInteger("to")
                    ,new BigDecimal(String.valueOf(jsonObject.getValue("amount"))));
            if(transfer == null){
                msg = ONE_OF_ACCOUNTS_NOT_FOUND;
            }
            else {
                statusCode = 200;
                msg=Json.encodePrettily(transfer);
            }
        } catch (NotEnoughMoneyException e) {
            msg=String.format(USER_WITH_ID_0_HAS_NO_ENOUGH_MONEY,jsonObject.getInteger("from"));
        } catch (InvalidTransferException e) {
            msg = YOU_CAN_T_TRANSFER_MONEY_FROM_ACCOUNT_TO_ITSELF;
        }

        routingContext.response()
                .putHeader("content-type", "application/json")
                .setStatusCode(statusCode)
                .end(msg);
    }
}
