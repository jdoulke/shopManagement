package com.onlinestore.jdoulke.onlinestorefx.controllers.customers;

import com.onlinestore.jdoulke.onlinestorefx.Utils;
import com.onlinestore.jdoulke.onlinestorefx.database.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

public class CustomerAdditionController {

    @FXML
    private Label notificationLabel;
    @FXML
    private StackPane notificationPane;
    @FXML
    private TextField phone_field;
    @FXML
    private TextField address_field;
    @FXML
    private TextField email_field;
    @FXML
    private TextField first_name_field;
    @FXML
    private TextField last_name_field;




    @FXML
    private void addCustomer() throws SQLException {

        if (first_name_field.getText().isEmpty() || last_name_field.getText().isEmpty() ||
                email_field.getText().isEmpty() || phone_field.getText().isEmpty() ||
                address_field.getText().isEmpty()) {

            showPopupMessage("Please fill in all fields.", 3, "red", "white", true);
            return;
        }

        try {
            Connection dbconnection = DatabaseConnection.getConnection();
            CallableStatement customerAdditionStmt = dbconnection.prepareCall("{call add_customer(?, ?, ?, ?, ?)}");

            customerAdditionStmt.setString(1, first_name_field.getText());
            customerAdditionStmt.setString(2, last_name_field.getText());
            customerAdditionStmt.setString(3, email_field.getText());
            customerAdditionStmt.setString(4, phone_field.getText());
            customerAdditionStmt.setString(5, address_field.getText());

            customerAdditionStmt.execute();

            showPopupMessage("Customer added successfully!", 3, "green", "white", true);
            resetFields();

            customerAdditionStmt.close();
            dbconnection.close();
        } catch (SQLException e) {
            showPopupMessage("An unexpected error occurred.", 3, "red", "white", true);
            System.out.println(e.getMessage());
        }
    }

    public void showPopupMessage(String message, int duration, String background, String textColor, boolean playSound) {

        Utils.popUpMessage(message, duration, background, textColor, playSound, notificationPane, notificationLabel);
    }

    private void resetFields() {
        first_name_field.setText("");
        last_name_field.setText("");
        email_field.setText("");
        phone_field.setText("");
        address_field.setText("");
    }

}
