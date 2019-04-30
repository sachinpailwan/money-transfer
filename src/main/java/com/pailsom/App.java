package com.pailsom;


import com.pailsom.controller.RestVerticleController;
import io.vertx.core.Vertx;

public class App
{

    Vertx vertx;

    public static void main( String[] args ) {
        App app = new App();
        app.startServer();
    }

    public void startServer(){

        Thread t1 = new Thread(() -> {
            vertx = Vertx.vertx();
            vertx.deployVerticle(RestVerticleController.class.getName());
        });
        t1.start();
    }

    public void stopServer(){
        vertx.undeploy(RestVerticleController.class.getName());
        vertx.close();
    }

}
