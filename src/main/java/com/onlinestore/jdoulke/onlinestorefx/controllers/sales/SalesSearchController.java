package com.onlinestore.jdoulke.onlinestorefx.controllers.sales;

import com.onlinestore.jdoulke.onlinestorefx.Utils;
import com.onlinestore.jdoulke.onlinestorefx.database.DatabaseConnection;
import com.onlinestore.jdoulke.onlinestorefx.entities.OrderItem;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import oracle.jdbc.OracleTypes;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SalesSearchController {

    @FXML
    private TextField sale_id_field;
    @FXML
    private TextField customer_id_field;
    @FXML
    private TextField customer_phone_field;
    @FXML
    private TextField customer_email_field;
    @FXML
    private ComboBox<String> sale_selection_box;
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
    @FXML
    private TextField user_id_field;

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
                resetFields("sale_id_field");
            }
        });

        customer_id_field.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                loadSalesByCustomerId(newValue);
            } else {
                resetFields("customer_id_field");
            }
        });

        customer_phone_field.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                searchSalesByPhone(newValue);
            } else {
                resetFields("phone_field");
            }
        });

        customer_email_field.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                searchSalesByEmail(newValue);
            } else {
                resetFields("email_field");
            }
        });

        user_id_field.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                loadSalesByUserId(newValue);
            } else {
                resetFields("user_id_field");
            }
        });

        sale_selection_box.setOnAction(event -> {
            String selectedSaleId = sale_selection_box.getSelectionModel().getSelectedItem();
            if (selectedSaleId != null) {
                searchSaleById(selectedSaleId);
            }
        });

        sale_selection_box.setOnAction(event -> {
            String selectedSale = sale_selection_box.getSelectionModel().getSelectedItem();
            if (selectedSale != null) {
                int saleId = Integer.parseInt(selectedSale);
                loadSaleDetails(saleId);
            }
        });

    }

    private void searchSaleById(String saleIdText) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            int saleId = Integer.parseInt(saleIdText);

            CallableStatement callableStatement = connection.prepareCall("{call get_sale_details(?, ?, ?, ?, ?)}");
            callableStatement.setInt(1, saleId);
            callableStatement.registerOutParameter(2, OracleTypes.CURSOR);
            callableStatement.registerOutParameter(3, OracleTypes.CURSOR);
            callableStatement.registerOutParameter(4, OracleTypes.CURSOR);
            callableStatement.registerOutParameter(5, OracleTypes.CURSOR);

            callableStatement.execute();

            try (ResultSet saleDetailsResult = (ResultSet) callableStatement.getObject(2)) {
                if (saleDetailsResult.next()) {
                    sales_order_id_label.setText(String.valueOf(saleDetailsResult.getInt("sale_id")));
                    sales_date_label.setText(saleDetailsResult.getDate("sale_date").toString());
                    sales_total_label.setText(String.format("%.2f €", saleDetailsResult.getDouble("amount")));
                    sales_payment_label.setText(saleDetailsResult.getString("payment_method"));
                    sales_customer_label.setText(String.valueOf(saleDetailsResult.getInt("customer_id")));
                    sales_user_label.setText(String.valueOf(saleDetailsResult.getInt("user_id")));
                }
            }

            loadCustomerDetails(connection, callableStatement);
            loadUserDetails(connection, callableStatement);
            loadOrderItems(connection, callableStatement);

            callableStatement.close();
        } catch (SQLException e) {
            resetFields("sale_id_field");
            e.printStackTrace();
        } catch (NumberFormatException e) {
            showPopupMessage("Please enter a valid Sale ID.", 3, "red", "white", true);
        }
    }

    private void loadSalesByCustomerId(String customerIdText) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            int customerId = Integer.parseInt(customerIdText);

            CallableStatement callableStatement = connection.prepareCall("{call get_sales_by_customer(?, ?)}");
            callableStatement.setInt(1, customerId);
            callableStatement.registerOutParameter(2, OracleTypes.CURSOR);

            callableStatement.execute();

            ResultSet resultSet = (ResultSet) callableStatement.getObject(2);
            sale_selection_box.getItems().clear();

            while (resultSet.next()) {
                int saleId = resultSet.getInt("sale_id");
                String saleDate = resultSet.getDate("sale_date").toString();
                double amount = resultSet.getDouble("amount");

                sale_selection_box.getItems().add(String.valueOf(saleId));
            }

            if (!sale_selection_box.getItems().isEmpty()) {
                sale_selection_box.getSelectionModel().selectFirst();
                loadSaleDetails(Integer.parseInt(sale_selection_box.getValue().split(" \\| ")[0]));
            }

            resultSet.close();
            callableStatement.close();
        } catch (SQLException | NumberFormatException e) {
            resetFields("customer_id_field");
        }
    }


    private void searchSalesByPhone(String phone) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            CallableStatement callableStatement = connection.prepareCall("{call get_sales_by_phone(?, ?)}");
            callableStatement.setString(1, phone);
            callableStatement.registerOutParameter(2, OracleTypes.CURSOR);

            callableStatement.execute();

            ResultSet resultSet = (ResultSet) callableStatement.getObject(2);
            sale_selection_box.getItems().clear();

            while (resultSet.next()) {
                sale_selection_box.getItems().add(String.valueOf(resultSet.getInt("sale_id")));
            }

            if (!sale_selection_box.getItems().isEmpty()) {
                sale_selection_box.getSelectionModel().selectFirst();
                searchSaleById(sale_selection_box.getValue());
            }

            resultSet.close();
            callableStatement.close();
        } catch (SQLException ignored) {
        }
    }

    private void searchSalesByEmail(String email) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            CallableStatement callableStatement = connection.prepareCall("{call get_sales_by_email(?, ?)}");
            callableStatement.setString(1, email);
            callableStatement.registerOutParameter(2, OracleTypes.CURSOR);

            callableStatement.execute();

            ResultSet resultSet = (ResultSet) callableStatement.getObject(2);
            sale_selection_box.getItems().clear();

            while (resultSet.next()) {
                sale_selection_box.getItems().add(String.valueOf(resultSet.getInt("sale_id")));
            }

            if (!sale_selection_box.getItems().isEmpty()) {
                sale_selection_box.getSelectionModel().selectFirst();
                searchSaleById(sale_selection_box.getValue());
            }

            resultSet.close();
            callableStatement.close();
        } catch (SQLException ignored) {
        }
    }
    private void loadSalesByUserId(String userIdText) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            int userId = Integer.parseInt(userIdText);

            CallableStatement callableStatement = connection.prepareCall("{call get_sales_by_user_id(?, ?)}");
            callableStatement.setInt(1, userId);
            callableStatement.registerOutParameter(2, OracleTypes.CURSOR);

            callableStatement.execute();

            ResultSet resultSet = (ResultSet) callableStatement.getObject(2);
            sale_selection_box.getItems().clear();

            while (resultSet.next()) {
                int saleId = resultSet.getInt("sale_id");
                String saleDate = resultSet.getDate("sale_date").toString();
                double amount = resultSet.getDouble("amount");

                sale_selection_box.getItems().add(String.valueOf(saleId));
            }

            if (!sale_selection_box.getItems().isEmpty()) {
                sale_selection_box.getSelectionModel().selectFirst();
                String selectedSale = sale_selection_box.getSelectionModel().getSelectedItem();
                int saleId = Integer.parseInt(selectedSale.split(" \\| ")[0]);
                loadSaleDetails(saleId);
            }

            resultSet.close();
            callableStatement.close();
        } catch (SQLException e) {
            resetFields("user_id_field");
            showPopupMessage("An error occurred while loading sales by user.", 3, "red", "white", true);
        } catch (NumberFormatException e) {
            showPopupMessage("Please enter a valid User ID.", 3, "red", "white", true);
        }
    }


    private void loadCustomerDetails(Connection connection, CallableStatement callableStatement) throws SQLException {
        try (ResultSet customerDetailsResult = (ResultSet) callableStatement.getObject(3)) {
            if (customerDetailsResult.next()) {
                first_name_label.setText(customerDetailsResult.getString("first_name"));
                last_name_label.setText(customerDetailsResult.getString("last_name"));
                email_label.setText(customerDetailsResult.getString("email"));
                phone_label.setText(customerDetailsResult.getString("phone"));
                address_label.setText(customerDetailsResult.getString("address"));
            }
        }
    }

    private void loadUserDetails(Connection connection, CallableStatement callableStatement) throws SQLException {
        try (ResultSet userDetailsResult = (ResultSet) callableStatement.getObject(4)) {
            if (userDetailsResult.next()) {
                username_label.setText(userDetailsResult.getString("username"));
                user_first_name_label.setText(userDetailsResult.getString("first_name"));
                user_last_name_label.setText(userDetailsResult.getString("last_name"));
            }
        }
    }

    private void loadOrderItems(Connection connection, CallableStatement callableStatement) throws SQLException {
        try (ResultSet orderItemsResult = (ResultSet) callableStatement.getObject(5)) {
            orderItemsData.clear();

            while (orderItemsResult.next()) {
                int orderItemId = orderItemsResult.getInt("order_item_id");
                int productId = orderItemsResult.getInt("product_id");
                String productName = orderItemsResult.getString("product_name");
                int quantity = orderItemsResult.getInt("quantity");
                double itemPrice = orderItemsResult.getDouble("item_price");

                orderItemsData.add(new OrderItem(orderItemId, 0, productId, quantity, itemPrice, productName));
            }
            productTable.setItems(orderItemsData);
        }
    }

    private void loadSaleDetails(int saleId) {
        try (Connection connection = DatabaseConnection.getConnection()) {

            CallableStatement callableStatement = connection.prepareCall("{call get_sale_details(?, ?, ?, ?, ?)}");
            callableStatement.setInt(1, saleId);
            callableStatement.registerOutParameter(2, OracleTypes.CURSOR); // Sale Details
            callableStatement.registerOutParameter(3, OracleTypes.CURSOR); // Customer Details
            callableStatement.registerOutParameter(4, OracleTypes.CURSOR); // User Details
            callableStatement.registerOutParameter(5, OracleTypes.CURSOR); // Order Items

            callableStatement.execute();


            try (ResultSet saleDetailsResult = (ResultSet) callableStatement.getObject(2)) {
                if (saleDetailsResult.next()) {
                    sales_order_id_label.setText(String.valueOf(saleDetailsResult.getInt("order_id")));
                    sales_customer_label.setText(String.valueOf(saleDetailsResult.getInt("customer_id")));
                    sales_user_label.setText(String.valueOf(saleDetailsResult.getInt("user_id")));
                    sales_total_label.setText(String.format("%.2f €", saleDetailsResult.getDouble("amount")));
                    sales_date_label.setText(saleDetailsResult.getDate("sale_date").toString());
                    sales_payment_label.setText(saleDetailsResult.getString("payment_method"));
                }
            }


            try (ResultSet customerDetailsResult = (ResultSet) callableStatement.getObject(3)) {
                if (customerDetailsResult.next()) {
                    first_name_label.setText(customerDetailsResult.getString("first_name"));
                    last_name_label.setText(customerDetailsResult.getString("last_name"));
                    email_label.setText(customerDetailsResult.getString("email"));
                    phone_label.setText(customerDetailsResult.getString("phone"));
                    address_label.setText(customerDetailsResult.getString("address"));
                }
            }


            try (ResultSet userDetailsResult = (ResultSet) callableStatement.getObject(4)) {
                if (userDetailsResult.next()) {
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

            callableStatement.close();
        } catch (SQLException e) {
            showPopupMessage("Failed to load sale details.", 3, "red", "white", true);
            e.printStackTrace();
        }
    }



    private void resetFields(String textField) {
        if (!textField.equals("sale_id_field")) sale_id_field.clear();
        if (!textField.equals("customer_id_field")) customer_id_field.clear();
        if (!textField.equals("phone_field")) customer_phone_field.clear();
        if (!textField.equals("email_field")) customer_email_field.clear();
        sale_selection_box.getItems().clear();
        sales_customer_label.setText("");
        sales_order_id_label.setText("");
        sales_user_label.setText("");
        sales_total_label.setText("");
        sales_date_label.setText("");
        sales_payment_label.setText("");
        first_name_label.setText("");
        last_name_label.setText("");
        email_label.setText("");
        phone_label.setText("");
        address_label.setText("");
        username_label.setText("");
        user_first_name_label.setText("");
        user_last_name_label.setText("");
        productTable.getItems().clear();
        sale_selection_box.getItems().clear();
    }

    private void showPopupMessage(String message, int duration, String background, String textColor, boolean playSound) {
        Utils.popUpMessage(message, duration, background, textColor, playSound, notificationPane, notificationLabel);
    }
}
