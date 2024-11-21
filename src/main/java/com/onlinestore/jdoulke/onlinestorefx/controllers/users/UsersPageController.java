package com.onlinestore.jdoulke.onlinestorefx.controllers.users;


import com.onlinestore.jdoulke.onlinestorefx.Utils;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class UsersPageController {

    @FXML
    private Label notificationLabel;
    @FXML
    private StackPane notificationPane;
    @FXML
    private StackPane user_selection_pane;

    @FXML
    public void initialize() {

        loadView("add_user.fxml");

    }

    @FXML
    public void handleMenuClick(MouseEvent event) {
        Label source = (Label) event.getSource();
        String id = source.getId();

        switch (id) {
            case "add_user_label":
                loadView("add_user.fxml");
                break;
            case "remove_user_label":
                loadView("remove_user.fxml");
                break;
            case "edit_user_label":
                loadView("edit_user.fxml");
                break;
            case "search_user_label":
                loadView("search_user.fxml");
                break;
            default:
                showPopupMessage("Unknown menu item clicked", 2, "orange", "black", false);
        }
    }


    public void loadView(String fxmlFileName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/users_views/" + fxmlFileName));
            Parent newView = loader.load();
            user_selection_pane.getChildren().clear();
            user_selection_pane.getChildren().add(newView);
        } catch (IOException e) {
            e.printStackTrace();
            showPopupMessage("Error loading view: " + fxmlFileName, 3, "red", "white", true);
        }
    }

    @FXML
    public void handleMouseEnterLabel(MouseEvent event) {
        Label label = (Label) event.getSource();
        label.setStyle("-fx-text-fill: #9168e0;");
    }

    @FXML
    public void handleMouseExitLabel(MouseEvent event) {
        Label label = (Label) event.getSource();
        label.setStyle("-fx-text-fill: white;");
    }

    public void showPopupMessage(String message, int duration, String background, String textColor, boolean playSound) {

        Utils.popUpMessage(message, duration, background, textColor, playSound, notificationPane, notificationLabel);
    }

}
