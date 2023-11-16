package com.jahitin.service;

import javax.annotation.Resource;
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import com.jahitin.data.Database;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import java.sql.Statement;
import java.util.Arrays;

import java.util.ArrayList;
import java.util.List;

@WebService
@SOAPBinding(style = Style.DOCUMENT)
public class Subscription {
    @Resource
    WebServiceContext wsContext;

    @WebMethod
    public void insertLogging(String description, String endpoint) {
        Database db = new Database();

        try {
            MessageContext messageContext = this.wsContext.getMessageContext();
            HttpExchange exchange = (HttpExchange) messageContext.get("com.sun.xml.ws.http.exchange");

            if (exchange.getRemoteAddress() != null) {
                System.out.println("Client IP: " + exchange.getRemoteAddress());
            } else {
                System.out.println("Client IP: " + exchange.getRemoteAddress());
                System.out.println("CLIENT IP IS NULL");
                return;
            }
            String query = "INSERT INTO logging (description, ip, endpoint, timestamp) VALUES (?, ?, ?, CURRENT_TIMESTAMP)";

            try (PreparedStatement preparedStatement = db.getConnection().prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                preparedStatement.setString(1, description);
                preparedStatement.setString(2, String.valueOf(exchange.getRemoteAddress()));
                preparedStatement.setString(3, endpoint);

                int result = preparedStatement.executeUpdate();

                if (result > 0) {
                    try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()){
                        if (generatedKeys.next()) {
                            int id = generatedKeys.getInt(1);
                            System.out.println("Data inserted with ID: " + id);
                        } else {
                            System.out.println("Data inserted, but failed to get ID");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("Data not inserted");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error inserting data: " + e.getMessage());
        }
    }

    @WebMethod
    public String newSubscription(int user_id) {
        // if (!validateAPIKey()) {
        //     return "API Key is not valid";
        // }
        Database db = new Database();
        MessageContext msgContext = wsContext.getMessageContext();
        HttpExchange httpExchange = (HttpExchange) msgContext.get("com.sun.xml.ws.http.exchange");
        String endpoint = httpExchange.getRequestURI().toString();
        try {
            if (!isSubscriptionExists(user_id)) {
                this.insertLogging("SUBSCRIPTION BARU dari " + user_id, endpoint);
    
                String query = "INSERT INTO subscription (user_id) VALUES (?)";
                try (PreparedStatement preparedStatement = db.getConnection().prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                    preparedStatement.setInt(1, user_id);
                    int result = preparedStatement.executeUpdate();
    
                    if (result > 0) {
                        try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                            if (generatedKeys.next()) {
                                int subscriber_id = generatedKeys.getInt(1);
                                return "New subscription created for user_id: " + user_id + " with subscriber_id: " + subscriber_id;
                            } else {
                                return "New subscription created, but failed to get subscriber_id";
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            return "New subscription created, but failed to get subscriber_id";
                        }
                    } else {
                        return "New subscription not created";
                    }
                }
            } else {
                return "Subscription already exists for user_id: " + user_id;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error creating subscription: " + e.getMessage();
        } finally {
            db.closeConnection();
        }
    }
    

    private boolean isSubscriptionExists(int user_id) throws Exception {
        Database db = new Database();
        String query = "SELECT COUNT(*) FROM subscription WHERE user_id = ?";
        try (PreparedStatement preparedStatement = db.getConnection().prepareStatement(query)) {
            preparedStatement.setInt(1, user_id);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                resultSet.next();
                int count = resultSet.getInt(1);
                return count > 0;
            }
        }
    }

    @WebMethod
    public String updateStatus(String status, int user_id) {
        // if (!validateAPIKey()) {
        //     return "API Key is not valid";
        // }
        MessageContext msgContext = wsContext.getMessageContext();
        HttpExchange httpExchange = (HttpExchange) msgContext.get("com.sun.xml.ws.http.exchange");
        String endpoint = httpExchange.getRequestURI().toString();
        this.insertLogging("UPDATE STATUS " + status + " dari " + user_id, endpoint);
        Database db = new Database();
        String query = "UPDATE subscription SET status = ? WHERE user_id = ?";
    
        try (PreparedStatement preparedStatement = db.getConnection().prepareStatement(query)) {
            preparedStatement.setString(1, status);
            preparedStatement.setInt(2, user_id);
    
            int affectedRows = preparedStatement.executeUpdate();
    
            if (affectedRows > 0) {
                return "Status updated to " + status + " user_id: " + user_id;
            } else {
                return "User with user_id: " + user_id + " not found";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error updating status: " + e.getMessage();
        } finally {
            db.closeConnection();
        }
    }
    

    @WebMethod
    public List<String> getAllPendingRequest() throws Exception {
        // if (!validateAPIKey()) {
        //     throw new Exception("API Key is not valid");
        // }
        List<String> resultList = new ArrayList<>();
        try {
            MessageContext msgContext = wsContext.getMessageContext();
            HttpExchange httpExchange = (HttpExchange) msgContext.get("com.sun.xml.ws.http.exchange");
            String endpoint = httpExchange.getRequestURI().toString();
            this.insertLogging("GET ALL PENDING SUBSCRIPTION REQUEST", endpoint);
            Database db = new Database();
            ResultSet rs = db.readQuery("SELECT * FROM subscription WHERE status = 'PENDING'");

            while (rs.next()) {
                String subscriberId = rs.getString("subscriber_id");
                String status = rs.getString("status");
                String formattedData = String.format("Subscriber ID: %s, Status: %s%n", subscriberId, status);
                resultList.add(formattedData);
            }

            if (!resultList.isEmpty()) {
                System.out.println("Data found");
            } else {
                System.out.println("No pending subscription requests found");
                resultList.add("No data found");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return resultList;
    }



    @WebMethod
    public String getStatus(int user_id) throws Exception {
        // if (!validateAPIKey()) {
        //     throw new Exception("API Key is not valid");
        // }
        try {
            MessageContext msgContext = wsContext.getMessageContext();
            HttpExchange httpExchange = (HttpExchange) msgContext.get("com.sun.xml.ws.http.exchange");
            String endpoint = httpExchange.getRequestURI().toString();
            this.insertLogging("GET STATUS dari " + user_id, endpoint);
            Database db = new Database();
            ResultSet rs = db.readQuery("SELECT status FROM subscription WHERE user_id = " + user_id);
            if(rs.next()){
                return user_id + " " + (String) rs.getObject(1);
            }
            else{
                return "NOT FOUND";
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private boolean validateAPIKey() {
        String[] apiKeyList = {"RestClient", "PHPClient"};
        MessageContext messageContext = this.wsContext.getMessageContext();
        HttpExchange exchange = (HttpExchange) messageContext.get("com.sun.xml.ws.http.exchange");
        String APIKey = exchange.getRequestHeaders().getFirst("X-API-KEY");
        System.out.println("APIKey: " + APIKey);
        if (APIKey == null) {
            return false;
        } else if (APIKey.equals(apiKeyList[0]) || APIKey.equals(apiKeyList[1])) {
            return true;
        } else {
            return false;
        }
    }
}
