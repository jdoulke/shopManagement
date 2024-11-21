package com.onlinestore.jdoulke.onlinestorefx.controllers.orders;

import com.onlinestore.jdoulke.onlinestorefx.Utils;
import com.onlinestore.jdoulke.onlinestorefx.database.DatabaseConnection;
import com.onlinestore.jdoulke.onlinestorefx.entities.Order;
import com.onlinestore.jdoulke.onlinestorefx.entities.OrderItem;
import com.onlinestore.jdoulke.onlinestorefx.entities.Product;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import oracle.jdbc.OracleTypes;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class OrderEditController {

    private Order currentOrder;

    @FXML
    private TextField order_id_field;
    @FXML
    private Label first_name_label;
    @FXML
    private Label last_name_label;
    @FXML
    private Label user_id_label;
    @FXML
    private Label username_label;
    @FXML
    private TableView<OrderItem> cartTable;
    @FXML
    private TableColumn<OrderItem, String> cartProductNameColumn;
    @FXML
    private TableColumn<OrderItem, Integer> cartQuantityColumn;
    @FXML
    private TableColumn<OrderItem, Double> cartPriceColumn;
    @FXML
    private Label totalLabel;
    @FXML
    private StackPane notificationPane;
    @FXML
    private Label notificationLabel;
    @FXML
    private TableView<Product> productTable;
    @FXML
    private ComboBox<String> categoryDropdown;
    @FXML
    private Spinner<Integer> quantitySpinner;
    @FXML
    private TableColumn<Product, String> productNameColumn;

    @FXML
    private TableColumn<Product, Double> productPriceColumn;

    @FXML
    private TableColumn<Product, Integer> productStockColumn;

    private final ObservableList<OrderItem> cartData = FXCollections.observableArrayList();
    private final ObservableList<Product> productData = FXCollections.observableArrayList();
    private final ObservableList<OrderItem> removedItems = FXCollections.observableArrayList();

    @FXML
    private ImageView addProductButton;
    @FXML
    private ImageView removeProductButton;

    @FXML
    private void initialize() {
        setupCartTable();
        setupProductTable();
        loadCategories();

        order_id_field.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty() && newValue.matches("\\d+")) {
                loadOrderDetails(newValue);
            } else {
                resetFields();
            }
        });

        categoryDropdown.setOnAction(event -> {
            String selectedCategory = categoryDropdown.getSelectionModel().getSelectedItem();
            if (selectedCategory != null) {
                loadProductsByCategory(selectedCategory);
            }
        });

        productTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, selectedProduct) -> {
            if (selectedProduct != null) {
                int maxStock = selectedProduct.getStockQuantity();
                SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, maxStock, 1);
                quantitySpinner.setValueFactory(valueFactory);
            }
        });

        addProductButton.setOnMouseClicked(event -> addToCart());
        removeProductButton.setOnMouseClicked(event -> removeFromCart());
    }

    private void setupCartTable() {
        cartProductNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        cartQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        cartPriceColumn.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        cartTable.setItems(cartData);
    }

    @FXML
    private void editOrder() {
        if (cartData.isEmpty()) {
            showPopupMessage("Cart is empty. Please add products to update the order.", 3, "orange", "white", true);
            return;
        }

        if (order_id_field.getText().isEmpty()) {
            showPopupMessage("Please enter an Order ID.", 3, "orange", "white", true);
            return;
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            int orderId = Integer.parseInt(order_id_field.getText());

            for (OrderItem removedItem : removedItems) {
                CallableStatement increaseStockStmt = connection.prepareCall("{call increase_product_stock(?, ?)}");
                increaseStockStmt.setInt(1, removedItem.getProductId());
                increaseStockStmt.setInt(2, removedItem.getQuantity());
                increaseStockStmt.execute();
                increaseStockStmt.close();
            }

            CallableStatement deleteOrderItemsStmt = connection.prepareCall("{call delete_order_items(?)}");
            deleteOrderItemsStmt.setInt(1, orderId);
            deleteOrderItemsStmt.execute();
            deleteOrderItemsStmt.close();

            CallableStatement updateOrderItemStmt = connection.prepareCall("{call update_order_items(?, ?, ?, ?)}");
            for (OrderItem item : cartData) {
                updateOrderItemStmt.setInt(1, orderId);
                updateOrderItemStmt.setInt(2, item.getProductId());
                updateOrderItemStmt.setInt(3, item.getQuantity());
                updateOrderItemStmt.setDouble(4, item.getItemPrice());
                updateOrderItemStmt.execute();

                CallableStatement decreaseStockStmt = connection.prepareCall("{call update_product_stock(?, ?)}");
                decreaseStockStmt.setInt(1, item.getProductId());
                decreaseStockStmt.setInt(2, item.getQuantity());
                decreaseStockStmt.execute();
                decreaseStockStmt.close();
            }
            updateOrderItemStmt.close();

            removedItems.clear();

            showPopupMessage("Order updated successfully with Order ID: " + orderId + ".", 3, "green", "white", false);
        } catch (SQLException | NumberFormatException e) {
            showPopupMessage("An error occurred while updating the order.", 3, "red", "white", true);
            e.printStackTrace();
        }
    }



    private void loadOrderDetails(String orderIdText) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            int orderId = Integer.parseInt(orderIdText);

            CallableStatement callableStatement = connection.prepareCall("{call get_order_details_for_edit(?, ?, ?, ?, ?)}");
            callableStatement.setInt(1, orderId);
            callableStatement.registerOutParameter(2, OracleTypes.CURSOR);
            callableStatement.registerOutParameter(3, OracleTypes.CURSOR);
            callableStatement.registerOutParameter(4, OracleTypes.CURSOR);
            callableStatement.registerOutParameter(5, OracleTypes.CURSOR);

            callableStatement.execute();


            ResultSet orderResult = (ResultSet) callableStatement.getObject(2);
            if (orderResult.next()) {
                int customerId = orderResult.getInt("customer_id");
                int userId = orderResult.getInt("user_id");
                String orderDate = orderResult.getString("order_date");
                String status = orderResult.getString("status");
                double amount = orderResult.getDouble("amount");

                currentOrder = new Order(orderId, customerId, userId, orderDate, status, amount);
            }
            orderResult.close();


            ResultSet customerResult = (ResultSet) callableStatement.getObject(3);
            if (customerResult.next()) {
                first_name_label.setText(customerResult.getString("first_name"));
                last_name_label.setText(customerResult.getString("last_name"));
            }
            customerResult.close();


            ResultSet userResult = (ResultSet) callableStatement.getObject(4);
            if (userResult.next()) {
                user_id_label.setText(String.valueOf(userResult.getInt("user_id")));
                username_label.setText(userResult.getString("username"));
            }
            userResult.close();


            ResultSet orderItemsResult = (ResultSet) callableStatement.getObject(5);
            cartData.clear();
            while (orderItemsResult.next()) {
                int productId = orderItemsResult.getInt("product_id");
                String productName = orderItemsResult.getString("product_name");
                int quantity = orderItemsResult.getInt("quantity");
                double itemPrice = orderItemsResult.getDouble("item_price");

                cartData.add(new OrderItem(0, orderId, productId, quantity, itemPrice, productName));
            }
            orderItemsResult.close();

            callableStatement.close();

            cartTable.refresh();
            updateTotalCost();
        } catch (SQLException | NumberFormatException e) {
            resetFields();
            showPopupMessage("Failed to load order details.", 3, "red", "white", true);
            e.printStackTrace();
        }
    }

    private void resetFields() {
        first_name_label.setText("");
        last_name_label.setText("");
        user_id_label.setText("");
        username_label.setText("");
        cartData.clear();
        totalLabel.setText("0.00 €");
    }

    private void removeFromCart() {
        OrderItem selectedOrderItem = cartTable.getSelectionModel().getSelectedItem();
        if (selectedOrderItem == null) {
            showPopupMessage("Please select a product to remove from the cart.", 3, "orange", "white", true);
            return;
        }

        Product correspondingProduct = productData.stream()
                .filter(product -> product.getProductId() == selectedOrderItem.getProductId())
                .findFirst()
                .orElse(null);

        if (correspondingProduct != null) {
            correspondingProduct.setStockQuantity(correspondingProduct.getStockQuantity() + selectedOrderItem.getQuantity());
            productTable.refresh();
        }

        removedItems.add(selectedOrderItem);
        cartData.remove(selectedOrderItem);

        updateTotalCost();

        showPopupMessage("Product removed from cart.", 3, "green", "white", false);
    }



    private void loadCategories() {
        try (Connection connection = DatabaseConnection.getConnection();
             CallableStatement callableStatement = connection.prepareCall("{call get_categories(?)}")) {

            callableStatement.registerOutParameter(1, OracleTypes.CURSOR);
            callableStatement.execute();

            ResultSet resultSet = (ResultSet) callableStatement.getObject(1);
            ObservableList<String> categories = FXCollections.observableArrayList();

            while (resultSet.next()) {
                categories.add(resultSet.getString("category"));
            }

            categoryDropdown.setItems(categories);

            resultSet.close();
        } catch (SQLException e) {
            showPopupMessage("Failed to load categories.", 3, "red", "white", true);
            e.printStackTrace();
        }
    }

    private void setupProductTable() {
        productNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        productPriceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        productStockColumn.setCellValueFactory(new PropertyValueFactory<>("stockQuantity"));

        productTable.setItems(productData);
    }

    private void loadProductsByCategory(String category) {
        try (Connection connection = DatabaseConnection.getConnection();
             CallableStatement callableStatement = connection.prepareCall("{call get_products_by_category(?, ?)}")) {

            callableStatement.setString(1, category);
            callableStatement.registerOutParameter(2, OracleTypes.CURSOR);
            callableStatement.execute();

            ResultSet resultSet = (ResultSet) callableStatement.getObject(2);
            productData.clear();

            while (resultSet.next()) {
                int productId = resultSet.getInt("product_id");
                String name = resultSet.getString("name");
                double price = resultSet.getDouble("price");
                int stockQuantity = resultSet.getInt("stock_quantity");

                productData.add(new Product(productId, name, null, price, stockQuantity, category));
            }

            resultSet.close();
        } catch (SQLException e) {
            showPopupMessage("Failed to load products.", 3, "red", "white", true);
            e.printStackTrace();
        }
    }

    private void addToCart() {
        Product selectedProduct = productTable.getSelectionModel().getSelectedItem();
        if (selectedProduct == null || quantitySpinner.getValue() == 0 || quantitySpinner.getValue() == null) {
            showPopupMessage("Please select a product to add to the cart.", 3, "orange", "white", true);
            return;
        }

        int quantity = quantitySpinner.getValue();

        if (quantity > selectedProduct.getStockQuantity()) {
            showPopupMessage("Not enough stock available.", 3, "red", "white", true);
            return;
        }

        selectedProduct.setStockQuantity(selectedProduct.getStockQuantity() - quantity);
        productTable.refresh();

        boolean found = false;
        for (OrderItem item : cartData) {
            if (item.getProductId() == selectedProduct.getProductId()) {
                item.setQuantity(item.getQuantity() + quantity);
                found = true;
                break;
            }
        }

        if (!found) {
            cartData.add(new OrderItem(0, 0, selectedProduct.getProductId(), quantity, selectedProduct.getPrice(), selectedProduct.getName()));
        }

        updateTotalCost();
        cartTable.refresh();
        showPopupMessage("Product added to cart.", 3, "green", "white", false);
    }

    private void updateTotalCost() {
        double totalCost = cartData.stream()
                .mapToDouble(OrderItem::getTotalPrice)
                .sum();
        totalLabel.setText(String.format("%.2f €", totalCost));
    }

    private void showPopupMessage(String message, int duration, String background, String textColor, boolean playSound) {
        Utils.popUpMessage(message, duration, background, textColor, playSound, notificationPane, notificationLabel);
    }
}
