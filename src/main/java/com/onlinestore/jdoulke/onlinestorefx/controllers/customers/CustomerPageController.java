package com.onlinestore.jdoulke.onlinestorefx.controllers.customers;


import com.onlinestore.jdoulke.onlinestorefx.Utils;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;

import java.io.IOException;


public class CustomerPageController {

    @FXML
    private Label notificationLabel;
    @FXML
    private StackPane notificationPane;
    @FXML
    private StackPane customer_selection_pane;

    @FXML
    public void initialize() {

        loadView("add_customer.fxml");

    }

    @FXML
    public void handleMenuClick(MouseEvent event) {
        Label source = (Label) event.getSource();
        String id = source.getId();

        switch (id) {
            case "add_customer_label":
                loadView("add_customer.fxml");
                break;
            case "remove_customer_label":
                loadView("remove_customer.fxml");
                break;
            case "edit_customer_label":
                loadView("edit_customer.fxml");
                break;
            case "search_customer_label":
                loadView("search_customer.fxml");
                break;
            default:
                showPopupMessage("Unknown menu item clicked", 2, "orange", "black", false);
        }
    }


    public void loadView(String fxmlFileName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/customers_views/" + fxmlFileName));
            Parent newView = loader.load();
            customer_selection_pane.getChildren().clear();
            customer_selection_pane.getChildren().add(newView);
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
