package com.jahitin;

import javax.jws.WebMethod;
import javax.jws.WebService;

@WebService
public class TestingService {
    @WebMethod
    public String sayHello(String name) {
        return "Hello, " + name + "!";
    }
}
