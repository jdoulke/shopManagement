package com.onlinestore.jdoulke.onlinestorefx.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL = "jdbc:mariadb://144.24.184.36:3306/shopmanagement";
    private static final String USER = "user";
    private static final String PASSWORD = "SecurePass123!";

    public static Connection getConnection() throws SQLException {

        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
