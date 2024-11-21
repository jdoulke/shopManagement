package com.onlinestore.jdoulke.onlinestorefx.controllers;


import com.onlinestore.jdoulke.onlinestorefx.Main;
import com.onlinestore.jdoulke.onlinestorefx.UserSession;
import com.onlinestore.jdoulke.onlinestorefx.Utils;
import com.onlinestore.jdoulke.onlinestorefx.database.DatabaseConnection;
import com.onlinestore.jdoulke.onlinestorefx.entities.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import oracle.jdbc.OracleTypes;

import java.io.IOException;
import java.sql.*;


public class LoginController {


    @FXML
    private Label notificationLabel;

    @FXML
    private TextField user_field;

    @FXML
    private PasswordField pass_field;

    @FXML
    private StackPane notificationPane;

    @FXML
    public void handleLogin() {

        if (user_field.getText().isEmpty() || pass_field.getText().isEmpty()) {

            showPopupMessage("Please fill in all fields.",3, "red", "white", true);
            return;

        }


        try {
            Connection dbconnection = DatabaseConnection.getConnection();

            CallableStatement validateStmt = dbconnection.prepareCall("{call validate_user(?, ?, ?)}");
            validateStmt.setString(1, user_field.getText());
            validateStmt.setString(2, pass_field.getText());
            validateStmt.registerOutParameter(3, Types.NUMERIC);

            validateStmt.execute();

            if (validateStmt.getObject(3) != null) {
                handleLogin(dbconnection, validateStmt);

            } else {
                showPopupMessage("Invalid credentials.", 3, "red", "white", true);

                resetFields();
                validateStmt.close();
                dbconnection.close();
            }
        }

        catch(SQLException e){
            showPopupMessage("An unexpected error occurred.", 5, "red", "white", true);
        }
    }



    public void showPopupMessage(String message, int duration, String background, String textColor, boolean playSound) {

        Utils.popUpMessage(message, duration, background, textColor, playSound, notificationPane, notificationLabel);
    }



    private void resetFields() {

        user_field.setText("");
        pass_field.setText("");
    }

    public static void loginView(Stage primaryStage) {

        UserSession.clearSession();

        try {



            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/views/login.fxml"));
            Parent root = fxmlLoader.load();

            Scene scene = new Scene(root, 640, 480);

            primaryStage.setResizable(false);
            primaryStage.setTitle("Shop Management App");
            primaryStage.setScene(scene);
            primaryStage.centerOnScreen();
            primaryStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void handleLogin(Connection dbconnection, CallableStatement validateStmt) throws SQLException {
        int userId = validateStmt.getInt(3);

        CallableStatement userDetailsStmt = dbconnection.prepareCall("{call get_user_details(?, ?)}");
        userDetailsStmt.setInt(1, userId);
        userDetailsStmt.registerOutParameter(2, OracleTypes.CURSOR);

        userDetailsStmt.execute();
        ResultSet rs = (ResultSet) userDetailsStmt.getObject(2);

        rs.next();
        User user = new User(
                rs.getInt("user_id"),
                rs.getString("username"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getInt("is_admin") == 1
        );

        UserSession.setCurrentUser(user);

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/main_view.fxml"));
        Parent root;
        try {
            root = fxmlLoader.load();

            Scene scene = new Scene(root, 1200, 720);
            Stage stage = (Stage) user_field.getScene().getWindow();
            stage.setResizable(false);
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        validateStmt.close();
        userDetailsStmt.close();
        dbconnection.close();
    }

    //for quick login proposes
    public void handleUserIconClick() throws SQLException {

        user_field.setText("admin");
        pass_field.setText("admin");
        try {
            Connection dbconnection = DatabaseConnection.getConnection();

            CallableStatement validateStmt = dbconnection.prepareCall("{call validate_user(?, ?, ?)}");
            validateStmt.setString(1, user_field.getText());
            validateStmt.setString(2, pass_field.getText());
            validateStmt.registerOutParameter(3, Types.NUMERIC);

            validateStmt.execute();

            if (validateStmt.getObject(3) != null) {
                handleLogin(dbconnection, validateStmt);
            }

            validateStmt.close();
            dbconnection.close();
        } catch (SQLException e) {
            showPopupMessage("An unexpected error occurred.", 3, "red", "white", true);
        }


    }


}
