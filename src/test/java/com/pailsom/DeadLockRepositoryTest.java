package com.pailsom;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;
import com.pailsom.config.AccountModule;
import com.pailsom.exceptions.InvalidDepositAmountException;
import com.pailsom.exceptions.InvalidTransferException;
import com.pailsom.exceptions.NotEnoughMoneyException;
import com.pailsom.repository.AccountRepository;
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


public class DeadLockRepositoryTest {
    private static AccountRepository repository;

    @BeforeClass
    public static void setup(){
        repository = Guice.createInjector(new AccountModule()).getInstance(AccountRepository.class);
    }

    @Test
    public void deadLocKCreationTest(){
        List<Integer> accounts = Arrays.asList("A","B","C").
                stream().map(k->createAccount(k))
                .collect(Collectors.toList());
        accounts.stream().forEach(id->{
            try {
                deposit(id,100000);
            } catch (InvalidDepositAmountException e) {

            }
        });
        LongAdder counter = new LongAdder();
        List<Runnable> transferFunctions = new ArrayList<>(6000);
        IntStream.range(0,100).forEach(t->{
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
        Assert.assertEquals(600,counter.intValue());
        Assert.assertEquals(300000,accounts.stream().map(k->repository.getUser(k).getAmount()).mapToInt(t->t.intValue()).sum());
    }
    private Runnable transactionRequest(int id1, int id2) {
        return () -> {
            try {
                repository.transfer(id1,id1,BigDecimal.ONE);
            } catch (NotEnoughMoneyException | InvalidTransferException e) {

            }
            System.out.println("Account :"+id1+" has transfered amount "+1+" to Account :"+id2);
        };
    }

    public int createAccount(String name){
        return repository.createAccount(name,"saving").getId();
    }

    public void deposit(int accId,long amount) throws InvalidDepositAmountException {
        System.out.println("Deposited account"+accId+" with amount :"+amount);
        repository.deposit(accId,new BigDecimal(amount));
    }
}
