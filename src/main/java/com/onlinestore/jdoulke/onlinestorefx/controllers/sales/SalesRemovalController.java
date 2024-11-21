package com.onlinestore.jdoulke.onlinestorefx.controllers.sales;

import com.onlinestore.jdoulke.onlinestorefx.Utils;
import com.onlinestore.jdoulke.onlinestorefx.database.DatabaseConnection;
import com.onlinestore.jdoulke.onlinestorefx.entities.OrderItem;
import com.onlinestore.jdoulke.onlinestorefx.entities.Sale;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import oracle.jdbc.OracleTypes;

import java.sql.*;

public class SalesRemovalController {

    @FXML
    private TextField sale_id_field;
    @FXML
    private Label sales_customer_label;
    @FXML
    private Label sales_order_id_label;
    @FXML
    private Label sales_user_label;
    @FXML
    private Label sales_total_label;
    @FXML
    private Label sales_date_label;
    @FXML
    private Label sales_payment_label;
    @FXML
    private Label first_name_label;
    @FXML
    private Label last_name_label;
    @FXML
    private Label email_label;
    @FXML
    private Label phone_label;
    @FXML
    private Label address_label;
    @FXML
    private Label username_label;
    @FXML
    private Label user_first_name_label;
    @FXML
    private Label user_last_name_label;
    @FXML
    private StackPane notificationPane;
    @FXML
    private Label notificationLabel;
    @FXML
    private TableView<OrderItem> productTable;
    @FXML
    private TableColumn<OrderItem, String> productNameColumn;
    @FXML
    private TableColumn<OrderItem, Integer> productQuantityColumn;
    @FXML
    private TableColumn<OrderItem, Double> productPriceColumn;
    @FXML
    private TableColumn<OrderItem, Integer> productIdColumn;

    private Sale currentSale;
    private ObservableList<OrderItem> orderItemsData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        productIdColumn.setCellValueFactory(new PropertyValueFactory<>("productId"));
        productNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        productQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        productPriceColumn.setCellValueFactory(new PropertyValueFactory<>("itemPrice"));

        sale_id_field.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                searchSaleById(newValue);
            } else {
                resetFields(true);
            }
        });
    }

    @FXML
    public void removeSale() {
        String saleIdText = sale_id_field.getText();

        if (saleIdText.isEmpty()) {
            showPopupMessage("Please enter a Sale ID.", 3, "orange", "white", true);
            return;
        }
        if (currentSale == null) {
            return;
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            int saleId = Integer.parseInt(saleIdText);

            CallableStatement callableStatement = connection.prepareCall("{call delete_sale(?)}");
            callableStatement.setInt(1, saleId);
            callableStatement.execute();
            callableStatement.close();

            showPopupMessage("Sale successfully removed and stock updated.", 3, "green", "white", false);
            resetFields(false);
            currentSale = null;
        } catch (SQLException e) {
            showPopupMessage("An error occurred while removing the sale.", 3, "red", "white", true);
            e.printStackTrace();
        } catch (NumberFormatException e) {
            showPopupMessage("Please enter a valid Sale ID.", 3, "red", "white", true);
        }
    }

    private void searchSaleById(String saleIdText) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            int saleId = Integer.parseInt(saleIdText);
            double totalAmount = -1;
            String saleDate = "1999-01-01";
            String paymentMethod = "Cash";
            int userId = -1;
            int customerId = -1;
            int orderId = -1;
            CallableStatement callableStatement = connection.prepareCall("{call get_sale_details(?, ?, ?, ?, ?)}");
            callableStatement.setInt(1, saleId);
            callableStatement.registerOutParameter(2, OracleTypes.CURSOR); // Sale Details
            callableStatement.registerOutParameter(3, OracleTypes.CURSOR); // Customer Details
            callableStatement.registerOutParameter(4, OracleTypes.CURSOR); // User Details
            callableStatement.registerOutParameter(5, OracleTypes.CURSOR); // Order Items

            callableStatement.execute();


            try (ResultSet saleDetailsResult = (ResultSet) callableStatement.getObject(2)) {
                if (saleDetailsResult.next()) {
                    orderId = saleDetailsResult.getInt("order_id");
                    totalAmount = saleDetailsResult.getDouble("amount");
                    saleDate = saleDetailsResult.getDate("sale_date").toString();
                    paymentMethod = saleDetailsResult.getString("payment_method");
                    sales_order_id_label.setText(String.valueOf(orderId));
                    sales_date_label.setText(saleDetailsResult.getDate("sale_date").toString());
                    sales_total_label.setText(String.format("%.2f €", totalAmount));
                    sales_payment_label.setText(paymentMethod);
                    sales_customer_label.setText(String.valueOf(saleDetailsResult.getInt("customer_id")));
                    sales_user_label.setText(String.valueOf(saleDetailsResult.getInt("user_id")));
                }
            }


            try (ResultSet customerDetailsResult = (ResultSet) callableStatement.getObject(3)) {
                if (customerDetailsResult.next()) {
                    customerId = customerDetailsResult.getInt("customer_id");
                    first_name_label.setText(customerDetailsResult.getString("first_name"));
                    last_name_label.setText(customerDetailsResult.getString("last_name"));
                    email_label.setText(customerDetailsResult.getString("email"));
                    phone_label.setText(customerDetailsResult.getString("phone"));
                    address_label.setText(customerDetailsResult.getString("address"));
                }
            }


            try (ResultSet userDetailsResult = (ResultSet) callableStatement.getObject(4)) {
                if (userDetailsResult.next()) {
                    userId = userDetailsResult.getInt("user_id");
                    username_label.setText(userDetailsResult.getString("username"));
                    user_first_name_label.setText(userDetailsResult.getString("first_name"));
                    user_last_name_label.setText(userDetailsResult.getString("last_name"));
                }
            }


            try (ResultSet orderItemsResult = (ResultSet) callableStatement.getObject(5)) {
                orderItemsData.clear();
                while (orderItemsResult.next()) {
                    int orderItemId = orderItemsResult.getInt("order_item_id");
                    int productId = orderItemsResult.getInt("product_id");
                    String productName = orderItemsResult.getString("product_name");
                    int quantity = orderItemsResult.getInt("quantity");
                    double itemPrice = orderItemsResult.getDouble("item_price");

                    orderItemsData.add(new OrderItem(orderItemId, saleId, productId, quantity, itemPrice, productName));
                }
                productTable.setItems(orderItemsData);
            }



            currentSale = new Sale(saleId, customerId, userId, totalAmount, paymentMethod, String.valueOf(saleDate));
            updateSaleDetails(saleId, customerId, userId, totalAmount, saleDate, paymentMethod);

            loadCustomerDetails(connection, customerId);
            loadUserDetails(connection, userId);
            loadOrderItems(connection, saleId);

            callableStatement.close();
        } catch (SQLException e) {
            resetFields(true);
        } catch (NumberFormatException e) {
            showPopupMessage("Please enter a valid Sale ID.", 3, "red", "white", true);
        }
    }

    private void updateSaleDetails(int saleId, int customerId, int userId, double totalAmount, String saleDate, String paymentMethod) {
        sales_order_id_label.setText(String.valueOf(saleId));
        sales_customer_label.setText(String.valueOf(customerId));
        sales_user_label.setText(String.valueOf(userId));
        sales_total_label.setText(String.format("%.2f €", totalAmount));
        sales_date_label.setText(saleDate);
        sales_payment_label.setText(paymentMethod);
    }

    private void loadCustomerDetails(Connection connection, int customerId) throws SQLException {
        CallableStatement customerDetailsStmt = connection.prepareCall("{call get_customer(?, ?)}");
        customerDetailsStmt.setInt(1, customerId);
        customerDetailsStmt.registerOutParameter(2, OracleTypes.CURSOR);

        customerDetailsStmt.execute();

        ResultSet resultSet = (ResultSet) customerDetailsStmt.getObject(2);

        if (resultSet.next()) {
            first_name_label.setText(resultSet.getString("first_name"));
            last_name_label.setText(resultSet.getString("last_name"));
            email_label.setText(resultSet.getString("email"));
            phone_label.setText(resultSet.getString("phone"));
            address_label.setText(resultSet.getString("address"));
        }

        resultSet.close();
        customerDetailsStmt.close();
    }

    private void loadUserDetails(Connection connection, int userId) throws SQLException {
        CallableStatement userDetailsStmt = connection.prepareCall("{call get_user_details(?, ?)}");
        userDetailsStmt.setInt(1, userId);
        userDetailsStmt.registerOutParameter(2, OracleTypes.CURSOR);

        userDetailsStmt.execute();

        ResultSet resultSet = (ResultSet) userDetailsStmt.getObject(2);

        if (resultSet.next()) {
            username_label.setText(resultSet.getString("username"));
            user_first_name_label.setText(resultSet.getString("first_name"));
            user_last_name_label.setText(resultSet.getString("last_name"));
        }

        resultSet.close();
        userDetailsStmt.close();
    }

    private void loadOrderItems(Connection connection, int saleId) throws SQLException {
        CallableStatement orderItemsStmt = connection.prepareCall("{call get_order_items(?, ?)}");
        orderItemsStmt.setInt(1, saleId);
        orderItemsStmt.registerOutParameter(2, OracleTypes.CURSOR);

        orderItemsStmt.execute();

        ResultSet resultSet = (ResultSet) orderItemsStmt.getObject(2);
        orderItemsData.clear();

        while (resultSet.next()) {
            int orderItemId = resultSet.getInt("order_item_id");
            int productId = resultSet.getInt("product_id");
            String productName = resultSet.getString("name");
            int quantity = resultSet.getInt("quantity");
            double itemPrice = resultSet.getDouble("item_price");

            orderItemsData.add(new OrderItem(orderItemId, saleId, productId, quantity, itemPrice, productName));
        }

        resultSet.close();
        orderItemsStmt.close();

        productTable.setItems(orderItemsData);
    }

    private void resetFields(boolean isSearch) {
        if (!isSearch) sale_id_field.clear();

        sales_customer_label.setText("");
        sales_order_id_label.setText("");
        sales_user_label.setText("");
        sales_total_label.setText("");
        sales_date_label.setText("");
        sales_payment_label.setText("");
        first_name_label.setText("");
        last_name_label.setText("");
        email_label.setText("");
        address_label.setText("");
        phone_label.setText("");
        username_label.setText("");
        user_first_name_label.setText("");
        user_last_name_label.setText("");
        productTable.getItems().clear();
    }

    private void showPopupMessage(String message, int duration, String background, String textColor, boolean playSound) {
        Utils.popUpMessage(message, duration, background, textColor, playSound, notificationPane, notificationLabel);
    }
}
