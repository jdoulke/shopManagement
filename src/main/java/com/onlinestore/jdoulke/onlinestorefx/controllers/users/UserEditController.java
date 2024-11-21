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

public class UserEditController {

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
    private TextField user_id_field;



    @FXML
    public void initialize() {

        is_admin_box.getItems().addAll("YES", "NO");
        is_admin_box.setValue("NO");

        user_id_field.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty() && newValue.matches("\\d+")) {
                searchUserByID(newValue);
            } else {
                resetFields(true);
            }
        });

    }

    @FXML
    private void editUser() throws SQLException {
        if (user_id_field.getText().isEmpty() || username_field.getText().isEmpty() ||
                password_field.getText().isEmpty() || first_name_field.getText().isEmpty() ||
                last_name_field.getText().isEmpty()) {

            showPopupMessage("Please fill in all fields.", 3, "red", "white", true);
            return;
        }

        try {
            Connection dbconnection = DatabaseConnection.getConnection();
            CallableStatement userUpdateStmt = dbconnection.prepareCall("{call update_user(?, ?, ?, ?, ?, ?)}");

            int userId = Integer.parseInt(user_id_field.getText());
            userUpdateStmt.setInt(1, userId);
            userUpdateStmt.setString(2, username_field.getText());
            userUpdateStmt.setString(3, password_field.getText());
            userUpdateStmt.setString(4, first_name_field.getText());
            userUpdateStmt.setString(5, last_name_field.getText());
            userUpdateStmt.setInt(6, is_admin_box.getValue().equals("YES") ? 1 : 0);

            userUpdateStmt.execute();

            showPopupMessage("User updated successfully!", 3, "green", "white", true);

            userUpdateStmt.close();
            dbconnection.close();
            resetFields(false);
        } catch (SQLException e) {
            showPopupMessage("An unexpected error occurred.", 3, "red", "white", true);
            System.out.println(e.getMessage());
        }
    }

    @FXML
    private void searchUserByID(String userIdText) {
        try {
            Connection dbconnection = DatabaseConnection.getConnection();
            CallableStatement userSearchStmt = dbconnection.prepareCall("{call get_user(?, ?, ?, ?, ?, ?)}");

            int userId = Integer.parseInt(userIdText);
            userSearchStmt.setInt(1, userId);
            userSearchStmt.registerOutParameter(2, java.sql.Types.VARCHAR); // username
            userSearchStmt.registerOutParameter(3, java.sql.Types.VARCHAR); // password
            userSearchStmt.registerOutParameter(4, java.sql.Types.VARCHAR); // first name
            userSearchStmt.registerOutParameter(5, java.sql.Types.VARCHAR); // last name
            userSearchStmt.registerOutParameter(6, java.sql.Types.INTEGER); // is_admin

            userSearchStmt.execute();

            if(userSearchStmt.getString(2) != null) {
                username_field.setText(userSearchStmt.getString(2));
                password_field.setText(userSearchStmt.getString(3));
                first_name_field.setText(userSearchStmt.getString(4));
                last_name_field.setText(userSearchStmt.getString(5));
                is_admin_box.setValue(userSearchStmt.getInt(6) == 1 ? "YES" : "NO");
            } else resetFields(true);


            userSearchStmt.close();
            dbconnection.close();
        } catch (SQLException e) {
            resetFields(true);
            System.out.println(e.getMessage());
        }
    }

    public void showPopupMessage(String message, int duration, String background, String textColor, boolean playSound) {

        Utils.popUpMessage(message, duration, background, textColor, playSound, notificationPane, notificationLabel);
    }

    private void resetFields(boolean isSearch) {

        if(!isSearch) user_id_field.setText("");
        username_field.setText("");
        password_field.setText("");
        first_name_field.setText("");
        last_name_field.setText("");
        is_admin_box.setValue("NO");
    }

}
