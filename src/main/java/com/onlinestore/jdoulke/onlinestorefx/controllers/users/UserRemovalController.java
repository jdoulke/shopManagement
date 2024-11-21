package com.onlinestore.jdoulke.onlinestorefx.controllers.users;

import com.onlinestore.jdoulke.onlinestorefx.Utils;
import com.onlinestore.jdoulke.onlinestorefx.database.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

public class UserRemovalController {

    @FXML
    private Label notificationLabel;
    @FXML
    private StackPane notificationPane;
    @FXML
    private Label username_label;
    @FXML
    private Label password_label;
    @FXML
    private Label first_name_label;
    @FXML
    private Label last_name_label;
    @FXML
    private Label admin_label;
    @FXML
    private TextField user_id_field;



    @FXML
    public void initialize() {


        user_id_field.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty() && newValue.matches("\\d+")) {
                searchUserByID(newValue);
            } else {
                resetFields(true);
            }
        });

    }


    @FXML
    private void searchUserByID(String userIdText) {
        try {
            Connection dbconnection = DatabaseConnection.getConnection();
            CallableStatement userSearchStmt = dbconnection.prepareCall("{call get_user(?, ?, ?, ?, ?, ?)}");

            int userId = Integer.parseInt(userIdText);
            userSearchStmt.setInt(1, userId);
            userSearchStmt.registerOutParameter(2, java.sql.Types.VARCHAR);
            userSearchStmt.registerOutParameter(3, java.sql.Types.VARCHAR);
            userSearchStmt.registerOutParameter(4, java.sql.Types.VARCHAR);
            userSearchStmt.registerOutParameter(5, java.sql.Types.VARCHAR);
            userSearchStmt.registerOutParameter(6, java.sql.Types.INTEGER);

            userSearchStmt.execute();

            if(userSearchStmt.getString(2) != null) {
                username_label.setText(userSearchStmt.getString(2));
                password_label.setText(userSearchStmt.getString(3));
                first_name_label.setText(userSearchStmt.getString(4));
                last_name_label.setText(userSearchStmt.getString(5));
                admin_label.setText(userSearchStmt.getInt(6) == 1 ? "YES" : "NO");
            } else resetFields(true);


            userSearchStmt.close();
            dbconnection.close();
        } catch (SQLException e) {
            resetFields(true);
            System.out.println(e.getMessage());
        }
    }

    @FXML
    private void removeUser() {
        if (user_id_field.getText().isEmpty()) {
            showPopupMessage("Please enter a User ID.", 3, "red", "white", true);
            return;
        }

        try {
            Connection dbconnection = DatabaseConnection.getConnection();
            CallableStatement userDeleteStmt = dbconnection.prepareCall("{call delete_user(?)}");

            int userId = Integer.parseInt(user_id_field.getText());
            userDeleteStmt.setInt(1, userId);

            int affectedRows = userDeleteStmt.executeUpdate();

            if (affectedRows > 0) {
                showPopupMessage("User deleted successfully!", 3, "green", "white", true);
                resetFields(false);
            } else {
                showPopupMessage("User not found.", 3, "red", "white", true);
            }

            userDeleteStmt.close();
            dbconnection.close();
        } catch (SQLException e) {
            showPopupMessage("An unexpected error occurred.", 3, "red", "white", true);
            System.out.println(e.getMessage());
        } catch (NumberFormatException e) {
            showPopupMessage("Invalid User ID format.", 3, "red", "white", true);
        }
    }

    public void showPopupMessage(String message, int duration, String background, String textColor, boolean playSound) {

        Utils.popUpMessage(message, duration, background, textColor, playSound, notificationPane, notificationLabel);
    }

    private void resetFields(boolean isSearch) {

        if(!isSearch) user_id_field.setText("");
        username_label.setText("");
        password_label.setText("");
        first_name_label.setText("");
        last_name_label.setText("");
        admin_label.setText("");
    }

}

