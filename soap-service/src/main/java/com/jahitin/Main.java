package com.jahitin;

import com.jahitin.service.TestingService;

import java.util.concurrent.Flow.Subscription;

import javax.xml.ws.Endpoint;

public class Main {
    public static void main(String[] args) {
        try {
            Endpoint.publish("http://0.0.0.0:8003/ws/testing", new Subscription());
            System.out.println("Service is started");

        }
        catch (Exception e) {
            System.out.println("Something Wrong");
        }
    }
}

