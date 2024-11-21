package com.onlinestore.jdoulke.onlinestorefx.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL = "jdbc:oracle:thin:@//192.168.6.21:1521/dblabs";
    private static final String USER = "iee2019040";
    private static final String PASSWORD = "2310210347";

    public static Connection getConnection() throws SQLException {

        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
