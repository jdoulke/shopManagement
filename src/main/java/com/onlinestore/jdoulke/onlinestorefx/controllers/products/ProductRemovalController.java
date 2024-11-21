package com.onlinestore.jdoulke.onlinestorefx.controllers.products;

import com.onlinestore.jdoulke.onlinestorefx.Utils;
import com.onlinestore.jdoulke.onlinestorefx.database.DatabaseConnection;
import com.onlinestore.jdoulke.onlinestorefx.entities.Product;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import oracle.jdbc.OracleTypes;

import java.sql.*;

public class ProductRemovalController {

    @FXML
    private TableView<Product> productTable;
    @FXML
    private TableColumn<Product, Integer> idColumn;
    @FXML
    private TableColumn<Product, String> nameColumn;
    @FXML
    private TextField product_id_field;
    @FXML
    private Label name_label;
    @FXML
    private Label description_label;
    @FXML
    private Label price_label;
    @FXML
    private Label stock_label;
    @FXML
    private Label category_label;
    @FXML
    private Label notificationLabel;
    @FXML
    private StackPane notificationPane;

    private ObservableList<Product> productData = FXCollections.observableArrayList();

    @FXML
    private TableColumn<Product, Double> priceColumn;
    @FXML
    private TableColumn<Product, Integer> stockColumn;

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("productId"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        stockColumn.setCellValueFactory(new PropertyValueFactory<>("stockQuantity"));

        loadProducts();
        product_id_field.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty() && newValue.matches("\\d+")) {
                searchProduct(newValue);
            } else {
                resetFields(true);
            }
        });
    }

    public void loadProducts() {
        try {
            Connection dbconnection = DatabaseConnection.getConnection();
            CallableStatement productStmt = dbconnection.prepareCall("{call get_products(?)}");

            productStmt.registerOutParameter(1, OracleTypes.CURSOR);

            productStmt.execute();
            ResultSet rs = (ResultSet) productStmt.getObject(1);

            productData.clear();
            while (rs.next()) {
                productData.add(new Product(
                        rs.getInt("product_id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getDouble("price"),
                        rs.getInt("stock_quantity"),
                        rs.getString("category")
                ));
            }

            productTable.setItems(productData);

            rs.close();
            productStmt.close();
            dbconnection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void removeProduct() {
        if (product_id_field.getText().isEmpty()) {
            showPopupMessage("Please enter a Product ID.", 3, "red", "white", true);
            return;
        }

        try {
            Connection dbconnection = DatabaseConnection.getConnection();
            CallableStatement productRemoveStmt = dbconnection.prepareCall("{call delete_product(?)}");

            int productId = Integer.parseInt(product_id_field.getText());
            productRemoveStmt.setInt(1, productId);

            int affectedRows = productRemoveStmt.executeUpdate();

            if (affectedRows > 0) {
                showPopupMessage("Product removed successfully.", 3, "green", "white", true);
                resetFields(false);
                loadProducts();
            } else {
                showPopupMessage("Product not found.", 3, "red", "white", true);
            }

            productRemoveStmt.close();
            dbconnection.close();
        } catch (SQLException | NumberFormatException e) {
            showPopupMessage("An error occurred.", 3, "red", "white", true);
            e.printStackTrace();
        }
    }

    private void searchProduct(String productId) {
        try {
            Connection dbconnection = DatabaseConnection.getConnection();
            CallableStatement productSearchStmt = dbconnection.prepareCall("{call get_product(?, ?)}");

            int id = Integer.parseInt(productId);
            productSearchStmt.setInt(1, id);
            productSearchStmt.registerOutParameter(2, OracleTypes.CURSOR);

            productSearchStmt.execute();
            ResultSet rs = (ResultSet) productSearchStmt.getObject(2);

            if (rs.next()) {
                name_label.setText(rs.getString("name"));
                description_label.setText(rs.getString("description"));
                price_label.setText(String.valueOf(rs.getDouble("price")));
                stock_label.setText(String.valueOf(rs.getInt("stock_quantity")));
                category_label.setText(rs.getString("category"));
            } else {
                resetFields(true);
            }

            rs.close();
            productSearchStmt.close();
            dbconnection.close();

        } catch (SQLException | NumberFormatException e) {
            showPopupMessage("An error occurred.", 3, "red", "white", true);
            e.printStackTrace();
        }
    }

    private void resetFields(boolean isSearch) {
        if (!isSearch) product_id_field.setText("");
        name_label.setText("");
        description_label.setText("");
        price_label.setText("");
        stock_label.setText("");
        category_label.setText("");
    }

    public void showPopupMessage(String message, int duration, String background, String textColor, boolean playSound) {
        Utils.popUpMessage(message, duration, background, textColor, playSound, notificationPane, notificationLabel);
    }
}
