package com.ntst;

import java.sql.*;
import java.util.Properties;

public class Main {

    public static void main(String[] args) {

        // 1. Define your connection variables here
        String targetHost = args[0]; // Do NOT include https://
        String jdbcUrl = "jdbc:soap-ws://" + targetHost;

        String username = args[1];
        String password = args[2];
        //String envCode = args[3];

        String testQuery = args[3];

        System.out.println("Initializing Netsmart SOAP JDBC Driver Test...");
        System.out.println("Connecting to: " + jdbcUrl);

        // 2. Set up the connection properties
        Properties props = new Properties();
        props.setProperty("user", username);
        props.setProperty("password", password);
        //props.setProperty("env", envCode);

        // 3. Execute the standard JDBC lifecycle using Try-With-Resources
        try (
            // Open the connection (This calls our SoapDriver)
            Connection conn = DriverManager.getConnection(jdbcUrl, props);

            // Create the statement (This calls our SoapStatement)
            Statement stmt = conn.createStatement();

            // Execute the query (This triggers the HTTP POST and returns our SoapResultSet)
            ResultSet rs = stmt.executeQuery(testQuery)
        ) {
            System.out.println("Connection successful. Parsing response...\n");

            // 4. Retrieve MetaData to print dynamic column headers
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            // Print Headers
            for (int i = 1; i <= columnCount; i++) {
                System.out.printf("%-25s | ", metaData.getColumnName(i));
            }
            System.out.println("\n" + "-".repeat(columnCount * 28));

            // 5. Iterate through the ResultSet and print the rows
            int rowCount = 0;
            while (rs.next()) {
                rowCount++;
                for (int i = 1; i <= columnCount; i++) {
                    // Extract data just like DBeaver does
                    String cellValue = rs.getString(i);

                    // Handle nulls gracefully for the console output
                    if (rs.wasNull() || cellValue == null) {
                        cellValue = "[NULL]";
                    }

                    // Print with a fixed width for a clean table look
                    System.out.printf("%-25s | ", cellValue);
                }
                System.out.println();
            }

            System.out.println("\nTest Complete. Total rows retrieved: " + rowCount);

        } catch (SQLException e) {
            System.err.println("JDBC Error Encountered: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("General Error Encountered: " + e.getMessage());
        }
    }
}