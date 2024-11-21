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

public class OrderSearchController {

    @FXML
    private TextField order_id_field;
    @FXML
    private TextField customer_id_field;
    @FXML
    private TextField customer_phone_field;
    @FXML
    private TextField customer_email_field;
    @FXML
    private ComboBox<String> order_selection_box;
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
    private TextField user_id_field;
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
                resetFields("order_id_field");
            }
        });

        customer_id_field.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                loadOrdersByCustomerId(newValue);
            } else {
                resetFields("customer_id_field");
            }
        });

        customer_phone_field.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                searchOrdersByPhone(newValue);
            } else {
                resetFields("phone_field");
            }
        });

        customer_email_field.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                searchOrdersByEmail(newValue);
            } else {
                resetFields("email_field");
            }
        });

        order_selection_box.setOnAction(event -> {
            String selectedOrderId = order_selection_box.getSelectionModel().getSelectedItem();
            if (selectedOrderId != null) {
                searchOrderById(selectedOrderId);
            }
        });
        user_id_field.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                searchOrdersByUserId(newValue);
            } else {
                resetFields("user_id_field");
            }
        });
    }

    private void loadOrdersByCustomerId(String customerIdText) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            int customerId = Integer.parseInt(customerIdText);

            CallableStatement callableStatement = connection.prepareCall("{call get_orders_by_customer(?, ?)}");
            callableStatement.setInt(1, customerId);
            callableStatement.registerOutParameter(2, oracle.jdbc.OracleTypes.CURSOR);

            callableStatement.execute();

            ResultSet resultSet = (ResultSet) callableStatement.getObject(2);
            order_selection_box.getItems().clear();

            while (resultSet.next()) {
                order_selection_box.getItems().add(String.valueOf(resultSet.getInt("order_id")));
            }

            if (!order_selection_box.getItems().isEmpty()) {
                order_selection_box.getSelectionModel().selectFirst();
                searchOrderById(order_selection_box.getValue());
            }

            resultSet.close();
            callableStatement.close();
        } catch (SQLException e) {
            resetFields("customer_id_field");
        } catch (NumberFormatException e) {
            showPopupMessage("Please enter a valid customer ID.", 3, "red", "white", true);
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
            resetFields("order_id_field");
        } catch (NumberFormatException e) {
            showPopupMessage("Please enter a valid order ID.", 3, "red", "white", true);
        }
    }

    private void searchOrdersByPhone(String phone) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            CallableStatement callableStatement = connection.prepareCall("{call get_orders_by_phone(?, ?)}");
            callableStatement.setString(1, phone);
            callableStatement.registerOutParameter(2, oracle.jdbc.OracleTypes.CURSOR);

            callableStatement.execute();

            ResultSet resultSet = (ResultSet) callableStatement.getObject(2);
            order_selection_box.getItems().clear();

            while (resultSet.next()) {
                int orderId = resultSet.getInt("order_id");
                order_selection_box.getItems().add(String.valueOf(orderId));
            }

            resultSet.close();
            callableStatement.close();

            if (!order_selection_box.getItems().isEmpty()) {
                order_selection_box.getSelectionModel().selectFirst();
                searchOrderById(order_selection_box.getSelectionModel().getSelectedItem());
            }
        } catch (SQLException ignored) {}
    }

    private void searchOrdersByEmail(String email) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            CallableStatement callableStatement = connection.prepareCall("{call get_orders_by_email(?, ?)}");
            callableStatement.setString(1, email);
            callableStatement.registerOutParameter(2, oracle.jdbc.OracleTypes.CURSOR);

            callableStatement.execute();

            ResultSet resultSet = (ResultSet) callableStatement.getObject(2);
            order_selection_box.getItems().clear();

            while (resultSet.next()) {
                int orderId = resultSet.getInt("order_id");
                order_selection_box.getItems().add(String.valueOf(orderId));
            }

            resultSet.close();
            callableStatement.close();

            if (!order_selection_box.getItems().isEmpty()) {
                order_selection_box.getSelectionModel().selectFirst();
                searchOrderById(order_selection_box.getSelectionModel().getSelectedItem());
            }
        } catch (SQLException ignored) {
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

    private void resetFields(String textField) {
        if (!textField.equals("order_id_field")) order_id_field.clear();
        if (!textField.equals("customer_id_field")) customer_id_field.clear();
        if (!textField.equals("user_id_field")) user_id_field.clear();
        if (!textField.equals("phone_field")) customer_phone_field.clear();
        if (!textField.equals("email_field")) customer_email_field.clear();
        order_selection_box.getItems().clear();
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
    private void searchOrdersByUserId(String userIdText) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            int userId = Integer.parseInt(userIdText);

            CallableStatement callableStatement = connection.prepareCall("{call get_orders_by_user_id(?, ?)}");
            callableStatement.setInt(1, userId);
            callableStatement.registerOutParameter(2, oracle.jdbc.OracleTypes.CURSOR);

            callableStatement.execute();

            ResultSet resultSet = (ResultSet) callableStatement.getObject(2);
            order_selection_box.getItems().clear();

            while (resultSet.next()) {
                int orderId = resultSet.getInt("order_id");
                order_selection_box.getItems().add(String.valueOf(orderId));
            }

            resultSet.close();
            callableStatement.close();

            if (!order_selection_box.getItems().isEmpty()) {
                order_selection_box.getSelectionModel().selectFirst();
                searchOrderById(order_selection_box.getSelectionModel().getSelectedItem());
            }
        } catch (SQLException ignore) {

        } catch (NumberFormatException e) {
            showPopupMessage("Please enter a valid User ID.", 3, "red", "white", true);
        }
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

    private void showPopupMessage(String message, int duration, String background, String textColor, boolean playSound) {
        Utils.popUpMessage(message, duration, background, textColor, playSound, notificationPane, notificationLabel);
    }
}
