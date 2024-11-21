package com.onlinestore.jdoulke.onlinestorefx;

import javafx.animation.FadeTransition;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import javafx.util.Duration;

import java.awt.*;


public class Utils {

    public static void popUpMessage(String message, int duration, String background, String textColor, boolean playSound, StackPane notificationPane, Label notificationLabel) {
        notificationPane.setVisible(true);
        notificationPane.setOpacity(1.0);
        notificationPane.toFront();

        notificationLabel.setText(message);
        notificationLabel.setStyle("-fx-background-color: " + background + "; -fx-text-fill: " + textColor + "; -fx-padding: 10px; -fx-font-size: 14px; -fx-background-radius: 10; -fx-alignment: center");

        if(playSound) playSound();


        FadeTransition fadeOut = new FadeTransition(Duration.seconds(2), notificationPane);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setDelay(Duration.seconds(duration));

        fadeOut.setOnFinished(event -> notificationPane.setVisible(false));


        fadeOut.play();
    }

    public static void playSound() {
        Toolkit.getDefaultToolkit().beep();
    }



}
