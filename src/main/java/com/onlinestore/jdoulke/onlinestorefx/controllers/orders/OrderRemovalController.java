package com.onlinestore.jdoulke.onlinestorefx.controllers.orders;

import com.onlinestore.jdoulke.onlinestorefx.Utils;
import com.onlinestore.jdoulke.onlinestorefx.database.DatabaseConnection;
import com.onlinestore.jdoulke.onlinestorefx.entities.OrderItem;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class OrderRemovalController {

    @FXML
    private TextField order_id_field;
    @FXML
    private Label order_customer_label;
    @FXML
    private Label order_id_label;
    @FXML
    private Label order_user_label;
    @FXML
    private Label order_total_label;
    @FXML
    private Label order_date_label;
    @FXML
    private Label order_status_label;
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

    private ObservableList<OrderItem> orderItemsData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {

        productIdColumn.setCellValueFactory(new PropertyValueFactory<>("productId"));
        productNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        productQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        productPriceColumn.setCellValueFactory(new PropertyValueFactory<>("itemPrice"));

        order_id_field.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                searchOrderById(newValue);
            } else {
                resetFields(true);
            }
        });

    }

    @FXML
    public void removeOrder() {
        String orderIdText = order_id_field.getText();

        if (orderIdText.isEmpty()) {
            showPopupMessage("Please enter an Order ID.", 3, "orange", "white", true);
            return;
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            int orderId = Integer.parseInt(orderIdText);


            CallableStatement getOrderItemsStmt = connection.prepareCall("{call get_order_items(?, ?)}");
            getOrderItemsStmt.setInt(1, orderId);
            getOrderItemsStmt.registerOutParameter(2, oracle.jdbc.OracleTypes.CURSOR);
            getOrderItemsStmt.execute();

            ResultSet orderItemsResultSet = (ResultSet) getOrderItemsStmt.getObject(2);

            while (orderItemsResultSet.next()) {
                int productId = orderItemsResultSet.getInt("product_id");
                int quantity = orderItemsResultSet.getInt("quantity");


                CallableStatement increaseStockStmt = connection.prepareCall("{call increase_product_stock(?, ?)}");
                increaseStockStmt.setInt(1, productId);
                increaseStockStmt.setInt(2, quantity);
                increaseStockStmt.execute();
                increaseStockStmt.close();
            }

            orderItemsResultSet.close();
            getOrderItemsStmt.close();


            CallableStatement deleteOrderItemsStmt = connection.prepareCall("{call delete_order_items(?)}");
            deleteOrderItemsStmt.setInt(1, orderId);
            deleteOrderItemsStmt.execute();
            deleteOrderItemsStmt.close();


            CallableStatement deleteOrderStmt = connection.prepareCall("{call delete_order(?)}");
            deleteOrderStmt.setInt(1, orderId);
            deleteOrderStmt.execute();
            deleteOrderStmt.close();

            showPopupMessage("Order successfully removed and stock updated.", 3, "green", "white", false);
            order_id_field.clear();

        } catch (SQLException e) {
            showPopupMessage("An error occurred while removing the order.", 3, "red", "white", true);
            e.printStackTrace();
        } catch (NumberFormatException e) {
            showPopupMessage("Please enter a valid Order ID.", 3, "red", "white", true);
        }
    }

    private void searchOrderById(String orderIdText) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            int orderId = Integer.parseInt(orderIdText);

            CallableStatement callableStatement = connection.prepareCall("{call get_order_details(?, ?, ?, ?, ?, ?)}");
            callableStatement.setInt(1, orderId);
            callableStatement.registerOutParameter(2, java.sql.Types.INTEGER);
            callableStatement.registerOutParameter(3, java.sql.Types.INTEGER);
            callableStatement.registerOutParameter(4, java.sql.Types.DECIMAL);
            callableStatement.registerOutParameter(5, java.sql.Types.DATE);
            callableStatement.registerOutParameter(6, java.sql.Types.VARCHAR);

            callableStatement.execute();

            int customerId = callableStatement.getInt(2);
            int userId = callableStatement.getInt(3);
            double totalAmount = callableStatement.getDouble(4);
            java.sql.Date orderDate = callableStatement.getDate(5);
            String status = callableStatement.getString(6);

            updateOrderDetails(orderId, customerId, userId, totalAmount, orderDate, status);

            loadCustomerDetails(connection, customerId);
            loadUserDetails(connection, userId);
            loadOrderItems(connection, orderId);

            callableStatement.close();
        } catch (SQLException e) {
            resetFields(true);
        } catch (NumberFormatException e) {
            showPopupMessage("Please enter a valid order ID.", 3, "red", "white", true);
        }
    }

    private void updateOrderDetails(int orderId, int customerId, int userId, double totalAmount, java.sql.Date orderDate, String status) {
        order_id_label.setText(String.valueOf(orderId));
        order_customer_label.setText(String.valueOf(customerId));
        order_user_label.setText(String.valueOf(userId));
        order_total_label.setText(String.format("%.2f €", totalAmount));
        order_date_label.setText(orderDate.toString());
        order_status_label.setText(status);
    }

    private void loadCustomerDetails(Connection connection, int customerId) throws SQLException {
        CallableStatement customerDetailsStmt = connection.prepareCall("{call get_customer(?, ?)}");
        customerDetailsStmt.setInt(1, customerId);
        customerDetailsStmt.registerOutParameter(2, oracle.jdbc.OracleTypes.CURSOR);

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
        userDetailsStmt.registerOutParameter(2, oracle.jdbc.OracleTypes.CURSOR);

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


    private void loadOrderItems(Connection connection, int orderId) throws SQLException {
        CallableStatement orderItemsStmt = connection.prepareCall("{call get_order_items(?, ?)}");
        orderItemsStmt.setInt(1, orderId);
        orderItemsStmt.registerOutParameter(2, oracle.jdbc.OracleTypes.CURSOR);

        orderItemsStmt.execute();

        ResultSet resultSet = (ResultSet) orderItemsStmt.getObject(2);
        orderItemsData.clear();

        while (resultSet.next()) {
            int orderItemId = resultSet.getInt("order_item_id");
            int productId = resultSet.getInt("product_id");
            String productName = resultSet.getString("name");
            int quantity = resultSet.getInt("quantity");
            double itemPrice = resultSet.getDouble("item_price");

            orderItemsData.add(new OrderItem(orderItemId, orderId, productId, quantity, itemPrice, productName));
        }

        resultSet.close();
        orderItemsStmt.close();

        productTable.setItems(orderItemsData);
    }

    private void resetFields(boolean isSearch) {
        if (!isSearch) order_id_field.clear();

        order_customer_label.setText("");
        order_id_label.setText("");
        order_user_label.setText("");
        order_total_label.setText("");
        order_date_label.setText("");
        order_status_label.setText("");
        username_label.setText("");
        user_first_name_label.setText("");
        user_last_name_label.setText("");
        first_name_label.setText("");
        last_name_label.setText("");
        email_label.setText("");
        address_label.setText("");
        phone_label.setText("");
        productTable.getItems().clear();
    }

    private void showPopupMessage(String message, int duration, String background, String textColor, boolean playSound) {
        Utils.popUpMessage(message, duration, background, textColor, playSound, notificationPane, notificationLabel);
    }
}
