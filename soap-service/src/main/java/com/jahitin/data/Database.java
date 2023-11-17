package com.jahitin.data;

import java.sql.*;

import io.github.cdimascio.dotenv.Dotenv;

public class Database {
    protected Connection connection;
    private Dotenv dotenv = Dotenv.load();
    private String DB_URL = dotenv.get("DB_URL");
    private String DB_USERNAME = dotenv.get("DB_USERNAME");
    private String DB_PASSWORD = dotenv.get("DB_PASSWORD");

    public Database() {
        try {
            this.connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
            System.out.println("Database connected");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error connecting to database");
        }
    }    

    public Connection getConnection(){
        return connection;
    }

    public void closeConnection() {
        try {
            connection.close();
            System.out.println("Connection is closed");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ResultSet readQuery(String query) throws Exception{
        System.out.println(query);
        try {
            Statement statement = getConnection().createStatement();
            ResultSet rs = statement.executeQuery(query);
            return rs;
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Error reading data: " + e.getMessage());
        }
    }
}
