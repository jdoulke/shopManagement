package com.onlinestore.jdoulke.onlinestorefx.controllers.users;

import com.onlinestore.jdoulke.onlinestorefx.Utils;
import com.onlinestore.jdoulke.onlinestorefx.database.DatabaseConnection;

import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

public class UserAdditionController {

    @FXML
    private Label notificationLabel;
    @FXML
    private StackPane notificationPane;
    @FXML
    private ChoiceBox is_admin_box;
    @FXML
    private TextField username_field;
    @FXML
    private TextField password_field;
    @FXML
    private TextField first_name_field;
    @FXML
    private TextField last_name_field;



    @FXML
    public void initialize() {

        is_admin_box.getItems().addAll("YES", "NO");
        is_admin_box.setValue("NO");

    }

    @FXML
    private void addUser() throws SQLException {

        if (username_field.getText().isEmpty() || password_field.getText().isEmpty() || first_name_field.getText().isEmpty() || last_name_field.getText().isEmpty()) {

            showPopupMessage("Please fill in all fields.", 3, "red", "white", true);
            return;
        }

        try {
            Connection dbconnection = DatabaseConnection.getConnection();

            CallableStatement userAdditionStmt = dbconnection.prepareCall("{call add_user(?, ?, ?, ?, ?, ?)}");

            userAdditionStmt.setString(1, username_field.getText());
            userAdditionStmt.setString(2, password_field.getText());
            userAdditionStmt.setString(3, first_name_field.getText());
            userAdditionStmt.setString(4, last_name_field.getText());
            userAdditionStmt.setInt(5, is_admin_box.getValue().equals("YES") ? 1 : 0);
            userAdditionStmt.registerOutParameter(6, java.sql.Types.INTEGER);


            userAdditionStmt.execute();

            int newUserId = userAdditionStmt.getInt(6);
            resetFields();

            showPopupMessage("User created successfully! User ID: " + newUserId, 3, "green", "white", false);

            userAdditionStmt.close();
            dbconnection.close();

        } catch (SQLException e) {
            showPopupMessage("An unexpected error occurred.", 3, "red", "white", true);
            System.out.println(e.getMessage());
        }

    }

    public void showPopupMessage(String message, int duration, String background, String textColor, boolean playSound) {

        Utils.popUpMessage(message, duration, background, textColor, playSound, notificationPane, notificationLabel);
    }

    private void resetFields() {

        username_field.setText("");
        password_field.setText("");
        first_name_field.setText("");
        last_name_field.setText("");
        is_admin_box.setValue("NO");
    }

}
