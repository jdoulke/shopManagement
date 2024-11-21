package com.onlinestore.jdoulke.onlinestorefx.controllers.customers;

import com.onlinestore.jdoulke.onlinestorefx.Utils;
import com.onlinestore.jdoulke.onlinestorefx.database.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import oracle.jdbc.internal.OracleTypes;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CustomerEditController {

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
    private TextField customer_id_field;


    @FXML
    public void initialize() {


        customer_id_field.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty() && newValue.matches("\\d+")) {
                searchCustomer(newValue);
            } else {
                resetFields();
            }
        });

    }
    @FXML
    private void editCustomer() throws SQLException {
        if (customer_id_field.getText().isEmpty() || email_field.getText().isEmpty() ||
                address_field.getText().isEmpty() || first_name_field.getText().isEmpty() ||
                last_name_field.getText().isEmpty() || phone_field.getText().isEmpty()) {

            showPopupMessage("Please fill in all fields.", 3, "red", "white", true);
            return;
        }

        try {
            Connection dbconnection = DatabaseConnection.getConnection();
            CallableStatement customerUpdateStmt = dbconnection.prepareCall("{call update_customer(?, ?, ?, ?, ?, ?)}");

            int userId = Integer.parseInt(customer_id_field.getText());
            customerUpdateStmt.setInt(1, userId);
            customerUpdateStmt.setString(2, first_name_field.getText());
            customerUpdateStmt.setString(3, last_name_field.getText());
            customerUpdateStmt.setString(4, email_field.getText());
            customerUpdateStmt.setString(5, phone_field.getText());
            customerUpdateStmt.setString(6, address_field.getText());

            customerUpdateStmt.execute();

            showPopupMessage("Customer updated successfully!", 3, "green", "white", true);

            customerUpdateStmt.close();
            dbconnection.close();
            resetFields();
            customer_id_field.setText("");
        } catch (SQLException e) {
            showPopupMessage("An unexpected error occurred.", 3, "red", "white", true);
            System.out.println(e.getMessage());
        }
    }


    private void searchCustomer(String customerId) {
        if (customer_id_field.getText().isEmpty()) {
            showPopupMessage("Please enter a Customer ID.", 3, "red", "white", true);
            return;
        }

        try {
            Connection dbconnection = DatabaseConnection.getConnection();
            CallableStatement customerSearchStmt = dbconnection.prepareCall("{call get_customer(?, ?)}");

            int id = Integer.parseInt(customerId);
            customerSearchStmt.setInt(1, id);
            customerSearchStmt.registerOutParameter(2, OracleTypes.CURSOR);

            customerSearchStmt.execute();
            ResultSet rs = (ResultSet) customerSearchStmt.getObject(2);

            if (rs.next()) {
                first_name_field.setText(rs.getString("first_name"));
                last_name_field.setText(rs.getString("last_name"));
                email_field.setText(rs.getString("email"));
                address_field.setText(rs.getString("address"));
                phone_field.setText(rs.getString("phone"));
            } else {
                resetFields();
            }

            rs.close();
            customerSearchStmt.close();
            dbconnection.close();

        } catch (SQLException | NumberFormatException e) {
            showPopupMessage("An error occurred.", 3, "red", "white", true);
            e.printStackTrace();
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
