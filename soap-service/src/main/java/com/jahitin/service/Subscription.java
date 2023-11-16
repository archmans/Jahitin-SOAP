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

@WebService
@SOAPBinding(style = Style.DOCUMENT)
public class Subscription {
    @Resource
    WebServiceContext wsContext;

    @WebMethod
    public String sayHello(String name) {
        return "Hello, " + name + "!";
    }

    @WebMethod
    public String testDatabase() {
        Database db = new Database();
        Connection conn = db.getConnection();

        try {
            String query  = "SELECT * FROM logging";
            ResultSet rs = db.readQuery(query);

            StringBuilder resultStringBuilder = new StringBuilder();

            while (rs.next()) {
                String id = rs.getString("id");
                String description = rs.getString("description");
                String ip = rs.getString("ip");
                String endpoint = rs.getString("endpoint");
                String timestamp = rs.getString("timestamp");
                String formattedData = String.format("ID: %s, Description: %s, IP: %s, Endpoint: %s, Timestamp: %s%n",
                        id, description, ip, endpoint, timestamp);
                resultStringBuilder.append(formattedData);
            }

            if (resultStringBuilder.length() > 0) {
                return resultStringBuilder.toString();
            } else {
                return "No data found";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error lagi dan lagi: " + e.getMessage();
        } finally {
            db.closeConnection();
        }
    }
    

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

            // Gunakan PreparedStatement untuk mencegah SQL injection
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
    public void newSubscription(int subscriber_id) {
        Database db = new Database();
        MessageContext msgContext = wsContext.getMessageContext();
        HttpExchange httpExchange = (HttpExchange) msgContext.get("com.sun.xml.ws.http.exchange");
        String endpoint = httpExchange.getRequestURI().toString();
        try {
            if (!isSubscriptionExists(subscriber_id)) {
                this.insertLogging("SUBSCRIPTION BARU dari " + subscriber_id, endpoint);

                String query = "INSERT INTO subscription (subscriber_id) VALUES (?)";
                try (PreparedStatement preparedStatement = db.getConnection().prepareStatement(query)) {
                    preparedStatement.setInt(1, subscriber_id);
                    preparedStatement.executeUpdate();

                    System.out.println("Subscription added  with subscriber_id: " + subscriber_id);
                }
            } else {
                System.out.println("Subscription already exists for subscriber_id: " + subscriber_id);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.closeConnection();
        }
    }

    private boolean isSubscriptionExists(int subscriber_id) throws Exception {
        Database db = new Database();
        String query = "SELECT COUNT(*) FROM subscription WHERE subscriber_id = ?";
        try (PreparedStatement preparedStatement = db.getConnection().prepareStatement(query)) {
            preparedStatement.setInt(1, subscriber_id);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                resultSet.next();
                int count = resultSet.getInt(1);
                return count > 0;
            }
        }
    }

    @WebMethod
    public void updateStatus(String status, int subscriber_id) {
        Database db = new Database();

        String query = "UPDATE subscription SET status = ? WHERE subscriber_id = ?";

        try (PreparedStatement preparedStatement = db.getConnection().prepareStatement(query)) {
            preparedStatement.setString(1, status);
            preparedStatement.setInt(2, subscriber_id);
            preparedStatement.executeUpdate();
            System.out.println("Status updated to " + status + " subscriber_id: " + subscriber_id);
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    @WebMethod
    public String getAllPendingRequest() throws Exception {
        try {
            this.insertLogging("GET ALL PENDING SUBSCRIPTION REQUEST", "getAllPendingSubscriptionRequest");
            Database db = new Database();
            ResultSet rs = db.readQuery("SELECT * FROM subscription WHERE status = 'PENDING'");
            StringBuilder resultStringBuilder = new StringBuilder();

            while (rs.next()) {
                String subscriberId = rs.getString("subscriber_id");
                String status = rs.getString("status");
                String formattedData = String.format("Subscriber ID: %s, Status: %s%n", subscriberId, status);
                resultStringBuilder.append(formattedData);
            }

            if (resultStringBuilder.length() > 0) {
                System.out.println("Data found");
                return resultStringBuilder.toString();
            } else {
                System.out.println("No pending subscription requests found");
                return "No data found";
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    @WebMethod
    public String getStatus(int subscriber_id) throws Exception {
        try {
            this.insertLogging("GET STATUS dari " + subscriber_id, "getStatus");
            Database db = new Database();
            ResultSet rs = db.readQuery("SELECT status FROM subscription WHERE subscriber_id = " + subscriber_id);
            if(rs.next()){
                return (String) rs.getObject(1);
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
        String[] apiKeyList = {"PremApp", "Postman", "RestClient", "RegularApp"};
        MessageContext messageContext = this.wsContext.getMessageContext();
        HttpExchange exchange = (HttpExchange) messageContext.get("com.sun.xml.ws.http.exchange");
        String APIKey = exchange.getRequestHeaders().getFirst("X-API-KEY");
        System.out.println("APIKey: " + APIKey);
        if (APIKey == null) {
            return false;
        } else if (APIKey.equals(apiKeyList[0]) || APIKey.equals(apiKeyList[1]) || APIKey.equals(apiKeyList[2]) || APIKey.equals(apiKeyList[3])) {
            return true;
        } else {
            return false;
        }
    }


    @WebMethod
    public Boolean validate(int subscriber_id) throws Exception {
        try {
            this.insertLogging("VALIDASI dari " + subscriber_id, "validate");
            MessageContext messageContext = this.wsContext.getMessageContext();
            HttpExchange exchange = (HttpExchange) messageContext.get("com.sun.xml.ws.http.exchange");
            Headers requestHeaders = exchange.getRequestHeaders();
            requestHeaders.put("Cache-Control", Arrays.asList("max-age=1", "stale-while-revalidate=59"));
            Database db = new Database();
            ResultSet rs = db.readQuery("SELECT status FROM subscription WHERE subscriber_id = " + subscriber_id);
            if(rs.next()){
                return ((String) rs.getObject(1)).equals("ACCEPTED");
            }
            else{
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    // @WebMethod
    // public 
}
