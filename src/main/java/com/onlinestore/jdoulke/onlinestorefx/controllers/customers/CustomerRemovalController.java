package com.onlinestore.jdoulke.onlinestorefx.controllers.customers;

import com.onlinestore.jdoulke.onlinestorefx.Utils;
import com.onlinestore.jdoulke.onlinestorefx.database.DatabaseConnection;
import com.onlinestore.jdoulke.onlinestorefx.entities.Customer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import oracle.jdbc.internal.OracleTypes;

import java.sql.*;

public class CustomerRemovalController {

    @FXML
    private TableView<Customer> customerTable;
    @FXML
    private TableColumn<Customer, Integer> idColumn;
    @FXML
    private TableColumn<Customer, String> firstNameColumn;
    @FXML
    private TableColumn<Customer, String> lastNameColumn;
    @FXML
    private TextField customer_id_field;
    @FXML
    private Label first_name_label;
    @FXML
    private Label last_name_label;
    @FXML
    private Label email_label;
    @FXML
    private Label address_label;
    @FXML
    private Label phone_label;
    @FXML
    private Label notificationLabel;
    @FXML
    private StackPane notificationPane;

    private ObservableList<Customer> customerData = FXCollections.observableArrayList();



    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));

        loadCustomers();
        customer_id_field.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty() && newValue.matches("\\d+")) {
                searchCustomer(newValue);
            } else {
                resetFields(true);
            }
        });
    }

    public void loadCustomers() {
        try {
            Connection dbconnection = DatabaseConnection.getConnection();
            CallableStatement customerStmt = dbconnection.prepareCall("{call get_customers(?)}");

            customerStmt.registerOutParameter(1, OracleTypes.CURSOR);

            customerStmt.execute();
            ResultSet rs = (ResultSet) customerStmt.getObject(1);

            customerData.clear();
            while (rs.next()) {
                customerData.add(new Customer(
                        rs.getInt("customer_id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        null,
                        null,
                        null
                ));
            }

            customerTable.setItems(customerData);

            rs.close();
            customerStmt.close();
            dbconnection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void removeCustomer(){
            if (customer_id_field.getText().isEmpty()) {
                showPopupMessage("Please enter a Customer ID.", 3, "red", "white", true);
                return;
            }

            try {
                Connection dbconnection = DatabaseConnection.getConnection();
                CallableStatement customerRemoveStmt = dbconnection.prepareCall("{call delete_customer(?)}");

                int customerId = Integer.parseInt(customer_id_field.getText());
                customerRemoveStmt.setInt(1, customerId);

                int affectedRows = customerRemoveStmt.executeUpdate();

                if (affectedRows > 0) {
                    showPopupMessage("Customer removed successfully.", 3, "green", "white", false);
                    resetFields(false);
                    loadCustomers();
                } else {
                    showPopupMessage("Customer not found.", 3, "red", "white", true);
                }

                customerRemoveStmt.close();
                dbconnection.close();
            } catch (SQLException | NumberFormatException e) {
                showPopupMessage("An error occurred.", 3, "red", "white", true);
                e.printStackTrace();
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
                first_name_label.setText(rs.getString("first_name"));
                last_name_label.setText(rs.getString("last_name"));
                email_label.setText(rs.getString("email"));
                address_label.setText(rs.getString("address"));
                phone_label.setText(rs.getString("phone"));
            } else {
                resetFields(true);
            }

            rs.close();
            customerSearchStmt.close();
            dbconnection.close();

        } catch (SQLException | NumberFormatException e) {
            showPopupMessage("An error occurred.", 3, "red", "white", true);
            e.printStackTrace();
        }
    }

    private void resetFields(boolean isSearch) {

        if(!isSearch) customer_id_field.setText("");
        first_name_label.setText("");
        last_name_label.setText("");
        email_label.setText("");
        address_label.setText("");
        phone_label.setText("");
    }

    public void showPopupMessage(String message, int duration, String background, String textColor, boolean playSound) {

        Utils.popUpMessage(message, duration, background, textColor, playSound, notificationPane, notificationLabel);
    }
}
