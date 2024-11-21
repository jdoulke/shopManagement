package com.onlinestore.jdoulke.onlinestorefx.controllers;


import com.onlinestore.jdoulke.onlinestorefx.Utils;
import com.onlinestore.jdoulke.onlinestorefx.entities.User;


import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;


import com.onlinestore.jdoulke.onlinestorefx.UserSession;
import javafx.stage.Stage;


import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;


import static com.onlinestore.jdoulke.onlinestorefx.controllers.LoginController.loginView;

public class MainController implements Initializable {

    @FXML
    private Label notificationLabel;
    @FXML
    private StackPane notificationPane;
    @FXML
    private ImageView users_button;
    @FXML
    private StackPane main_page;

    private User user;


    @FXML
    public void initialize(URL location, ResourceBundle resources) {

        user = UserSession.getCurrentUser();
        String message = "Welcome " + user.getFirstName() + " " + user.getLastName() + "!";
        showPopupMessage(message, 3, "green", "white", false);

        users_button.setVisible(user.isAdmin());

        loadView("home_page.fxml");
        Platform.runLater(() -> {
            Stage stage = (Stage) main_page.getScene().getWindow();
            stage.setTitle("Shop Management App: " + user.getFirstName() + " " + user.getLastName());
        });


    }

    @FXML
    public void handleMenuClick(MouseEvent event) {
        ImageView source = (ImageView) event.getSource();
        String id = source.getId();

        switch (id) {
            case "home_button":
                loadView("home_page.fxml");
                break;
            case "customers_button":
                loadView("customers_page.fxml");
                break;
            case "orders_button":
                loadView("orders_page.fxml");
                break;
            case "products_button":
                loadView("products_page.fxml");
                break;
            case "sales_button":
                loadView("sales_page.fxml");
                break;
            case "users_button":
                loadView("users_page.fxml");
                break;
            case "logout_button":
                handleLogout();
                break;
            case "info_button":
                handleInfo();
                break;
            default:
                showPopupMessage("Unknown menu item clicked", 2, "orange", "black", false);
        }
    }


    public void loadView(String fxmlFileName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/" + fxmlFileName));
            Parent newView = loader.load();
            main_page.getChildren().clear();
            main_page.getChildren().add(newView);



        } catch (IOException e) {
            e.printStackTrace();
            showPopupMessage("Error loading view: " + fxmlFileName, 3, "red", "white", true);
        }
    }



    public void showPopupMessage(String message, int duration, String background, String textColor, boolean playSound) {

        Utils.popUpMessage(message, duration, background, textColor, playSound, notificationPane, notificationLabel);
    }

    @FXML
    public void handleMouseEnter(MouseEvent event) {
        ImageView imageView = (ImageView) event.getSource();
        imageView.setFitWidth(imageView.getFitWidth() * 1.2);
        imageView.setFitHeight(imageView.getFitHeight() * 1.2);
    }

    @FXML
    public void handleMouseExit(MouseEvent event) {
        ImageView imageView = (ImageView) event.getSource();
        imageView.setFitWidth(imageView.getFitWidth() / 1.2);
        imageView.setFitHeight(imageView.getFitHeight() / 1.2);
    }

    private void handleInfo() {

        Alert infoAlert = new Alert(Alert.AlertType.INFORMATION);
        infoAlert.setTitle("Application Information");
        infoAlert.setHeaderText("About Online Store Application");
        infoAlert.setContentText("This application is designed for the management of a store. "
                + "It allows the management of customers, products, orders, sales, and employees. "
                + "It uses JavaFX for the GUI and Oracle Database for the backend."
                + "It was developed as a project for the Database Technology course by Ioannis Doulkeridis.");

        Stage stage = (Stage) infoAlert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/store.png")));
        stage.setAlwaysOnTop(true);
        stage.toFront();

        Utils.playSound();

        infoAlert.showAndWait();

    }


    public void handleLogout() {

        loginView((Stage) main_page.getScene().getWindow());
        HomeController.resetFirstTime();

    }
}
