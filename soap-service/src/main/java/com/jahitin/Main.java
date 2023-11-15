package com.jahitin;

import javax.xml.ws.Endpoint;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    public static void main(String[] args) {
        try {
            Endpoint.publish("http://0.0.0.0:8003/ws/testing", new TestingService());
            System.out.println("Service is started");

        }
        catch (Exception e) {
            System.out.println("Something Wrong");
        }
    }
}

