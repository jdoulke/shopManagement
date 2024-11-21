package com.onlinestore.jdoulke.onlinestorefx.controllers.orders;

import com.onlinestore.jdoulke.onlinestorefx.Utils;
import com.onlinestore.jdoulke.onlinestorefx.database.DatabaseConnection;
import com.onlinestore.jdoulke.onlinestorefx.entities.OrderItem;
import com.onlinestore.jdoulke.onlinestorefx.entities.Product;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import java.sql.Connection;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import oracle.jdbc.OracleTypes;

import static com.onlinestore.jdoulke.onlinestorefx.UserSession.getCurrentUser;

public class OrderCreationController {


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
    private ComboBox<String> categoryDropdown;

    @FXML
    private TableView<Product> productTable;

    @FXML
    private TableColumn<Product, String> productNameColumn;

    @FXML
    private TableColumn<Product, Double> productPriceColumn;

    @FXML
    private TableColumn<Product, Integer> productStockColumn;

    @FXML
    private Spinner<Integer> quantitySpinner;

    @FXML
    private ImageView addProductButton;
    @FXML
    private ImageView removeProductButton;

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
    private TextField customer_id_field;

    private final ObservableList<Product> productData = FXCollections.observableArrayList();
    private final ObservableList<OrderItem> cartData = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        loadCategories();
        setupProductTable();
        setupCartTable();

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

        customer_id_field.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty() && newValue.matches("\\d+")) {
                searchCustomer(newValue);
            } else {
                resetFields(true);
            }
        });

        addProductButton.setOnMouseClicked(event -> addToCart());
        removeProductButton.setOnMouseClicked(event -> removeFromCart());
    }

    @FXML
    private void submitOrder() {
        if (cartData.isEmpty()) {
            showPopupMessage("Cart is empty. Please add products to submit an order.", 3, "orange", "white", true);
            return;
        }

        if (customer_id_field.getText().isEmpty()) {
            showPopupMessage("Please enter a Customer ID.", 3, "orange", "white", true);
            return;
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            int customerId = Integer.parseInt(customer_id_field.getText());
            int userId = getCurrentUser().getUserId();
            String status = "Pending";
            double totalAmount = cartData.stream().mapToDouble(OrderItem::getTotalPrice).sum();

            CallableStatement createOrderStmt = connection.prepareCall("{call create_order(?, ?, ?, ?, ?)}");
            createOrderStmt.setInt(1, customerId);
            createOrderStmt.setInt(2, userId);
            createOrderStmt.setString(3, status);
            createOrderStmt.setDouble(4, totalAmount);
            createOrderStmt.registerOutParameter(5, java.sql.Types.INTEGER);
            createOrderStmt.execute();

            int orderId = createOrderStmt.getInt(5);
            createOrderStmt.close();

            CallableStatement addOrderItemStmt = connection.prepareCall("{call add_order_item(?, ?, ?, ?)}");
            CallableStatement updateStockStmt = connection.prepareCall("{call update_product_stock(?, ?)}");

            for (OrderItem item : cartData) {
                addOrderItemStmt.setInt(1, orderId);
                addOrderItemStmt.setInt(2, item.getProductId());
                addOrderItemStmt.setInt(3, item.getQuantity());
                addOrderItemStmt.setDouble(4, item.getItemPrice());
                addOrderItemStmt.execute();

                updateStockStmt.setInt(1, item.getProductId());
                updateStockStmt.setInt(2, item.getQuantity());
                updateStockStmt.execute();
            }

            addOrderItemStmt.close();
            updateStockStmt.close();

            showPopupMessage("Order submitted successfully with Order ID: " + orderId + ".", 3, "green", "white", false);
            resetOrder();
        } catch (SQLException | NumberFormatException e) {
            showPopupMessage("An error occurred while submitting the order.", 3, "red", "white", true);
            e.printStackTrace();
        }
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

    private void setupCartTable() {
        cartProductNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        cartQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        cartPriceColumn.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));

        cartTable.setItems(cartData);
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

        cartData.remove(selectedOrderItem);

        updateTotalCost();

        showPopupMessage("Product removed from cart.", 3, "green", "white", false);
    }

    private void updateTotalCost() {
        double totalCost = cartData.stream()
                .mapToDouble(OrderItem::getTotalPrice)
                .sum();
        totalLabel.setText(String.format("%.2f €", totalCost));
    }

    private void resetOrder() {
        cartData.clear();
        productTable.refresh();
        totalLabel.setText("0.00 €");
        customer_id_field.clear();
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
            customerSearchStmt.registerOutParameter(2, oracle.jdbc.internal.OracleTypes.CURSOR);

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

    private void showPopupMessage(String message, int duration, String background, String textColor, boolean playSound) {
        Utils.popUpMessage(message, duration, background, textColor, playSound, notificationPane, notificationLabel);
    }


}
