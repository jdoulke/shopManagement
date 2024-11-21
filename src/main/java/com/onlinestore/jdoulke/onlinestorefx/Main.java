package com.onlinestore.jdoulke.onlinestorefx;


import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;



import static com.onlinestore.jdoulke.onlinestorefx.controllers.LoginController.loginView;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {

        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/store.png")));
        primaryStage.setTitle("Shop Management App");

        loginView(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }


}