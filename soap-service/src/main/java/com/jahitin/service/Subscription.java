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
                // Handle null IP, mungkin dengan memberikan nilai default atau menghentikan proses lebih lanjut
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
    public void newSubscription(int penjahit_id, int subscriber_id) {
        Database db = new Database();
        try {
            // Check if the subscription already exists
            if (!isSubscriptionExists(penjahit_id, subscriber_id)) {
                this.insertLogging("SUBSCRIPTION BARU dari " + subscriber_id + " untuk " + penjahit_id, "addSubs");

                String query = "INSERT INTO subscription (penjahit_id, subscriber_id) VALUES (?, ?)";
                try (PreparedStatement preparedStatement = db.getConnection().prepareStatement(query)) {
                    preparedStatement.setInt(1, penjahit_id);
                    preparedStatement.setInt(2, subscriber_id);
                    preparedStatement.executeUpdate();

                    System.out.println("Subscription added with penjahit_id: " + penjahit_id + " and subscriber_id: " + subscriber_id);
                }
            } else {
                System.out.println("Subscription already exists for penjahit_id: " + penjahit_id + " and subscriber_id: " + subscriber_id);
                // Handle the case where the subscription already exists
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Handle the exception appropriately, log it, etc.
        } finally {
            db.closeConnection();
        }
    }

    private boolean isSubscriptionExists(int penjahit_id, int subscriber_id) throws Exception {
        Database db = new Database();
        String query = "SELECT COUNT(*) FROM subscription WHERE penjahit_id = ? AND subscriber_id = ?";
        try (PreparedStatement preparedStatement = db.getConnection().prepareStatement(query)) {
            preparedStatement.setInt(1, penjahit_id);
            preparedStatement.setInt(2, subscriber_id);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                resultSet.next();
                int count = resultSet.getInt(1);
                return count > 0;
            }
        }
    }

    @WebMethod
    public void updateStatus(String status, int penjahit_id, int subscriber_id) {
        Database db = new Database();

        String query = "UPDATE subscription SET status = ? WHERE penjahit_id = ? AND subscriber_id = ?";

        try (PreparedStatement preparedStatement = db.getConnection().prepareStatement(query)) {
            preparedStatement.setString(1, status);
            preparedStatement.setInt(2, penjahit_id);
            preparedStatement.setInt(3, subscriber_id);
            preparedStatement.executeUpdate();
            System.out.println("Status updated to " + status + " with penjahit_id: " + penjahit_id + " and subscriber_id: " + subscriber_id);
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    private boolean check() {
        MessageContext messageContext = this.wsContext.getMessageContext();
        HttpExchange exchange = (HttpExchange) messageContext.get("com.sun.xml.ws.http.exchange");
        String APIKey = exchange.getRequestHeaders().getFirst("APIKey");
        String  Source = exchange.getRequestHeaders().getFirst("Source");
        Boolean legal = (APIKey.equals("989796") && Source.equals("REST")) || (APIKey.equals("989796") && Source.equals("PHP"));
        System.out.println(APIKey + Source + legal);
        System.out.println("APIKey: " + APIKey);
        System.out.println("Source: " + Source);
        return legal;
    }

    @WebMethod
    public Boolean validate(int penjahit_id, int subscriber_id) throws Exception {
        try {
            this.insertLogging("VALIDASI dari " + subscriber_id + " untuk " + penjahit_id, "validate");
            MessageContext messageContext = this.wsContext.getMessageContext();
            HttpExchange exchange = (HttpExchange) messageContext.get("com.sun.xml.ws.http.exchange");
            Headers requestHeaders = exchange.getRequestHeaders();
            requestHeaders.put("Cache-Control", Arrays.asList("max-age=1", "stale-while-revalidate=59"));
            Database db = new Database();
            ResultSet rs = db.readQuery("SELECT status FROM subscription WHERE penjahit_id = " + penjahit_id + " AND subscriber_id = " + subscriber_id);
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

    @WebMethod
    public String getAllPendingRequest() throws Exception {
        try {
            this.insertLogging("GET ALL PENDING SUBSCRIPTION REQUEST", "getAllPendingSubscriptionRequest");
            Database db = new Database();
            ResultSet rs = db.readQuery("SELECT * FROM subscription WHERE status = 'PENDING'");
            StringBuilder resultStringBuilder = new StringBuilder();
            while (rs.next()) {
                String penjahit_id = rs.getString("penjahit_id");
                String subscriber_id = rs.getString("subscriber_id");
                String status = rs.getString("status");
                String formattedData = String.format("Penjahit ID: %s, Subscriber ID: %s, Status: %s%n",
                        penjahit_id, subscriber_id, status);
                resultStringBuilder.append(formattedData);
            }
            if (resultStringBuilder.length() > 0) {
                System.out.println("Data found");
                return resultStringBuilder.toString();
            } else {
                return "No data found";
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
