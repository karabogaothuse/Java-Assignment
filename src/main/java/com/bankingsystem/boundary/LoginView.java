
package com.bankingsystem.boundary;

import com.bankingsystem.controller.LoginController;
import com.bankingsystem.model.Customer;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

// Boundary: Displays the login and registration screens.
public class LoginView extends VBox {

    private final LoginController loginController;
    private final Consumer<Customer> onLoginSuccess;

    private final TextField loginIdField = new TextField();
    private final PasswordField loginPasswordField = new PasswordField();

    private final TextField customerIdField = new TextField();
    private final TextField firstNameField = new TextField();
    private final TextField surnameField = new TextField();
    private final TextField addressField = new TextField();
    private final TextField phoneField = new TextField();
    private final PasswordField registrationPasswordField = new PasswordField();

    private final CheckBox employedCheckBox = new CheckBox("I am employed");

    private final Label messageLabel = new Label();

    public LoginView(LoginController loginController,
                     Consumer<Customer> onLoginSuccess) {

        this.loginController = loginController;
        this.onLoginSuccess = onLoginSuccess;

        setSpacing(10);
        setPadding(new Insets(25));
        setPrefWidth(400);

        Label title = new Label("Banking System");
        Label loginTitle = new Label("Customer Login");

        loginIdField.setPromptText("Customer ID");
        loginPasswordField.setPromptText("Password");

        Button loginButton = new Button("Login");
        loginButton.setMaxWidth(Double.MAX_VALUE);
        loginButton.setOnAction(event -> loginCustomer());

        Label registerTitle = new Label("Create an Account");

        customerIdField.setPromptText("Customer ID");
        firstNameField.setPromptText("First Name");
        surnameField.setPromptText("Surname");
        addressField.setPromptText("Address");
        phoneField.setPromptText("Phone Number");
        registrationPasswordField.setPromptText("Password (minimum 8 characters)");

        Button registerButton = new Button("Register");
        registerButton.setMaxWidth(Double.MAX_VALUE);
        registerButton.setOnAction(event -> registerCustomer());

        getChildren().addAll(
                title,
                loginTitle,
                loginIdField,
                loginPasswordField,
                loginButton,
                new Label(" "),
                registerTitle,
                customerIdField,
                firstNameField,
                surnameField,
                addressField,
                phoneField,
                registrationPasswordField,
                employedCheckBox,
                registerButton,
                messageLabel
        );
    }

    private void loginCustomer() {

        try {
            Customer customer = loginController.login(
                    loginIdField.getText(),
                    loginPasswordField.getText()
            );

            if (customer == null) {
                messageLabel.setText("Incorrect customer ID or password.");
                return;
            }

            messageLabel.setText("Login successful!");

            onLoginSuccess.accept(customer);

        } catch (IllegalArgumentException e) {
            messageLabel.setText(e.getMessage());
        }
    }

    private void registerCustomer() {

        try {
            Customer customer = loginController.registerCustomer(
                    customerIdField.getText(),
                    firstNameField.getText(),
                    surnameField.getText(),
                    addressField.getText(),
                    phoneField.getText(),
                    employedCheckBox.isSelected(),
                    registrationPasswordField.getText()
            );

            messageLabel.setText(
                    "Registration successful! You can now log in."
            );

            loginIdField.setText(customer.getCustomerId());
            loginPasswordField.clear();

        } catch (IllegalArgumentException e) {
            messageLabel.setText(e.getMessage());
        }
    }
}
