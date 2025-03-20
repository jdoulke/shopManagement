package com.onlinestore.jdoulke.onlinestorefx.controllers.users;


import com.onlinestore.jdoulke.onlinestorefx.Utils;
import com.onlinestore.jdoulke.onlinestorefx.database.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import static java.lang.Integer.parseInt;

public class UserSearchController {


        @FXML
        private Label notificationLabel;
        @FXML
        private StackPane notificationPane;
        @FXML
        private Label username_label;
        @FXML
        private Label password_label;
        @FXML
        private Label first_name_label;
        @FXML
        private Label last_name_label;
        @FXML
        private Label admin_label;
        @FXML
        private TextField user_id_field;



        @FXML
        public void initialize() {


            user_id_field.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.isEmpty() && newValue.matches("\\d+")) {
                    searchUserByID(newValue);
                } else {
                    resetFields(true);
                }
            });

        }


        @FXML
        private void searchUserByID(String userIdText) {
            try {
                Connection dbconnection = DatabaseConnection.getConnection();
                CallableStatement userSearchStmt = dbconnection.prepareCall("{CALL get_user(?)}");

                userSearchStmt.setInt(1, parseInt(userIdText));
                ResultSet rs = userSearchStmt.executeQuery();

                if(rs.next()) {
                    username_label.setText(rs.getString("username"));
                    password_label.setText(rs.getString("pass"));
                    first_name_label.setText(rs.getString("first_name"));
                    last_name_label.setText(rs.getString("last_name"));
                    admin_label.setText(rs.getInt("is_admin") == 1 ? "YES" : "NO");
                } else {
                    resetFields(true);
                }

                rs.close();

                userSearchStmt.close();
                dbconnection.close();
            } catch (SQLException e) {
                resetFields(true);
            }
        }



        private void resetFields(boolean isSearch) {

            if(!isSearch) user_id_field.setText("");
            username_label.setText("");
            password_label.setText("");
            first_name_label.setText("");
            last_name_label.setText("");
            admin_label.setText("");
        }


}




