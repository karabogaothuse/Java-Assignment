
package com.bankingsystem;

import com.bankingsystem.boundary.AccountView;
import com.bankingsystem.boundary.LoginView;
import com.bankingsystem.controller.AccountController;
import com.bankingsystem.controller.LoginController;
import com.bankingsystem.model.Customer;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

// Main Class: Starts the JavaFX Banking System application.
public class MainApp extends Application {

    private final LoginController loginController = new LoginController();
    private final AccountController accountController = new AccountController();

    private Stage primaryStage;

    @Override
    public void start(Stage stage) {

        primaryStage = stage;

        primaryStage.setTitle("Banking System");
        primaryStage.setMinWidth(450);
        primaryStage.setMinHeight(500);

        showLogin();

        primaryStage.show();
    }

    private void showLogin() {

        LoginView loginView = new LoginView(
                loginController,
                this::showAccountView
        );

        Scene scene = new Scene(loginView, 480, 700);

        primaryStage.setScene(scene);
        primaryStage.setWidth(480);
        primaryStage.setHeight(700);
    }

    private void showAccountView(Customer customer) {

        AccountView accountView = new AccountView(
                accountController,
                customer,
                this::showLogin
        );

        Scene scene = new Scene(accountView, 900, 750);

        primaryStage.setScene(scene);
        primaryStage.setWidth(900);
        primaryStage.setHeight(750);
    }

    public static void main(String[] args) {

        launch(args);
    }
}
