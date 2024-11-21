package com.onlinestore.jdoulke.onlinestorefx.controllers.products;

import com.onlinestore.jdoulke.onlinestorefx.Utils;
import com.onlinestore.jdoulke.onlinestorefx.database.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import oracle.jdbc.OracleTypes;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ProductEditController {

    @FXML
    private Label notificationLabel;
    @FXML
    private StackPane notificationPane;
    @FXML
    private TextField name_field;
    @FXML
    private TextField description_field;
    @FXML
    private TextField price_field;
    @FXML
    private TextField stock_field;
    @FXML
    private TextField category_field;
    @FXML
    private TextField product_id_field;

    @FXML
    public void initialize() {
        product_id_field.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty() && newValue.matches("\\d+")) {
                searchProduct(newValue);
            } else {
                resetFields();
            }
        });
    }

    @FXML
    private void editProduct() {
        if (product_id_field.getText().isEmpty() || name_field.getText().isEmpty() ||
                description_field.getText().isEmpty() || price_field.getText().isEmpty() ||
                stock_field.getText().isEmpty() || category_field.getText().isEmpty()) {

            showPopupMessage("Please fill in all fields.", 3, "red", "white", true);
            return;
        }

        try {
            Connection dbconnection = DatabaseConnection.getConnection();
            CallableStatement productUpdateStmt = dbconnection.prepareCall("{call update_product(?, ?, ?, ?, ?, ?)}");

            int productId = Integer.parseInt(product_id_field.getText());
            double price = Double.parseDouble(price_field.getText());
            int stock = Integer.parseInt(stock_field.getText());

            productUpdateStmt.setInt(1, productId);
            productUpdateStmt.setString(2, name_field.getText());
            productUpdateStmt.setString(3, description_field.getText());
            productUpdateStmt.setDouble(4, price);
            productUpdateStmt.setInt(5, stock);
            productUpdateStmt.setString(6, category_field.getText());

            productUpdateStmt.execute();

            showPopupMessage("Product updated successfully!", 3, "green", "white", true);

            productUpdateStmt.close();
            dbconnection.close();
            resetFields();
            product_id_field.setText("");
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
                name_field.setText(rs.getString("name"));
                description_field.setText(rs.getString("description"));
                price_field.setText(String.valueOf(rs.getDouble("price")));
                stock_field.setText(String.valueOf(rs.getInt("stock_quantity")));
                category_field.setText(rs.getString("category"));
            } else {
                resetFields();
            }

            rs.close();
            productSearchStmt.close();
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
        name_field.setText("");
        description_field.setText("");
        price_field.setText("");
        stock_field.setText("");
        category_field.setText("");
    }
}
