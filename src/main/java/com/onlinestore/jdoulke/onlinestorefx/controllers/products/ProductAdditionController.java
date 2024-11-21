package com.onlinestore.jdoulke.onlinestorefx.controllers.products;

import com.onlinestore.jdoulke.onlinestorefx.Utils;
import com.onlinestore.jdoulke.onlinestorefx.database.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

public class ProductAdditionController {

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
    private TextField stock_quantity_field;
    @FXML
    private TextField category_field;

    @FXML
    private void addProduct() throws SQLException {
        if (name_field.getText().isEmpty() || description_field.getText().isEmpty() ||
                price_field.getText().isEmpty() || stock_quantity_field.getText().isEmpty() ||
                category_field.getText().isEmpty()) {

            showPopupMessage("Please fill in all fields.", 3, "red", "white", true);
            return;
        }

        try {
            double price = Double.parseDouble(price_field.getText());
            int stockQuantity = Integer.parseInt(stock_quantity_field.getText());

            Connection dbconnection = DatabaseConnection.getConnection();
            CallableStatement productAdditionStmt = dbconnection.prepareCall("{call add_product(?, ?, ?, ?, ?, ?)}");

            productAdditionStmt.setString(1, name_field.getText());
            productAdditionStmt.setString(2, description_field.getText());
            productAdditionStmt.setString(3, category_field.getText());
            productAdditionStmt.setDouble(4, price);
            productAdditionStmt.setInt(5, stockQuantity);
            productAdditionStmt.registerOutParameter(6, java.sql.Types.INTEGER);

            productAdditionStmt.execute();

            int newProductId = productAdditionStmt.getInt(6);

            showPopupMessage("Product added successfully! Product ID: " + newProductId + ".", 3, "green", "white", true);


            resetFields();

            productAdditionStmt.close();
            dbconnection.close();
        } catch (NumberFormatException e) {
            showPopupMessage("Price and Stock Quantity must be valid numbers.", 3, "red", "white", true);
        } catch (SQLException e) {
            showPopupMessage("An unexpected error occurred.", 3, "red", "white", true);
            System.out.println(e.getMessage());
        }
    }

    public void showPopupMessage(String message, int duration, String background, String textColor, boolean playSound) {
        Utils.popUpMessage(message, duration, background, textColor, playSound, notificationPane, notificationLabel);
    }

    private void resetFields() {
        name_field.setText("");
        description_field.setText("");
        price_field.setText("");
        stock_quantity_field.setText("");
        category_field.setText("");
    }
}
