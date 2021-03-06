package docket;

import docket.db.DatabaseManager;
import docket.localization.lz;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Controller for the Login Screen
 */
public class LoginScreenController {

    //<editor-fold desc="Private Members">

    private Timer incorrectUserPassTimer;
    private Connection conn = null;

    //</editor-fold>

    //<editor-fold desc="FXML">

    @FXML
    private Label usernameLabel;

    @FXML
    private TextField usernameTextField;

    @FXML
    private Label passwordLabel;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Button registerButton;

    @FXML
    private Button forgotPasswordButton;

    @FXML
    private Label incorrectUserPassLabel;

    @FXML
    private ProgressIndicator loadingIndicator;

    @FXML
    private Label zoneIdLabel;

    //</editor-fold>

    //<editor-fold desc="Constructor">

    /**
     * Default constructor
     */
    public LoginScreenController() { }

    //</editor-fold>

    //<editor-fold desc="Initializers">

    /**
     * Initializer
     * @throws Exception
     */
    public void initialize() throws Exception {
        incorrectUserPassLabel.setVisible(false);
        initializeUiLanguage();
        setUserLocationLabel();
    }

    /**
     * Localization
     */
    private void initializeUiLanguage() {
        usernameLabel.setText(lz.get("Username"));
        passwordLabel.setText(lz.get("Password"));
        loginButton.setText(lz.get("Login"));
        registerButton.setText(lz.get("Register"));
        incorrectUserPassLabel.setText(lz.get("Incorrect username/password"));
        forgotPasswordButton.setText(lz.get("Forgot your password?"));
    }

    //</editor-fold>

    //<editor-fold desc="Listeners">

    /**
     * Event when Login Button is clicked
     * Lambda is used to create asynchronously task
     * @param event
     * @throws Exception
     */
    public void loginButtonListener(ActionEvent event) throws Exception {
        loadingIndicator.setVisible(true);
        setButtonsDisabled(true);

        // Create task
        Task<Boolean> task = new Task<Boolean>() {

            @Override
            protected Boolean call() throws Exception {
                return DatabaseManager.isUserPassValid(usernameTextField.getText(), passwordField.getText());
            }

        };

        // On task succeed
        task.setOnSucceeded(evt -> {

            if (task.getValue()) {
                try {
                    completeLogin(event);
                } catch (Exception ex) {
                    System.out.println(ex);
                }
            } else {
                setButtonsDisabled(false);
                loadingIndicator.setVisible(false);
                displayIncorrectUserPassLabel();
            }
        });

        // On task fail
        task.setOnFailed(evt -> {

            setButtonsDisabled(false);
        });

        // Start task
        new Thread(task).start();
    }

    /**
     * Event when Register Button is clicked
     * @throws IOException
     */
    public void registerButtonListener() throws IOException {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, lz.get("Contact your IT administrator to register."), ButtonType.OK);
        alert.showAndWait();
    }

    /**
     * Event when Forgot Password Button is clicked
     * @throws IOException
     */
    public void forgotPasswordButtonListener() throws IOException {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, lz.get("Contact your IT administrator to reset password."), ButtonType.OK);
        alert.showAndWait();
    }

    /**
     * Sets the zoneIdLabel to be the user's current Time Zone ID
     */
    private void setUserLocationLabel() {
        String zoneId = TimeZone.getDefault().getID();
        zoneIdLabel.setText(zoneId);
    }

    //</editor-fold>

    //<editor-fold desc="Private Methods">

    /**
     * Complete login after username and password is verified. Switches scene to the Main Screen
     * @param event
     * @throws IOException
     */
    private void completeLogin(ActionEvent event) throws IOException {
        Parent parent = FXMLLoader.load(getClass().getResource("MainScreen.fxml"));
        Scene parentScene = new Scene(parent);

        Stage window = (Stage) loadingIndicator.getScene().getWindow();
        window.setScene(parentScene);
        window.show();
    }

    /**
     * Displays error messages for incorrect username/password. Auto-hides after 3 seconds
     */
    private void displayIncorrectUserPassLabel() {
        // Cancel existing timer to hide error message
        if (incorrectUserPassTimer != null)
            incorrectUserPassTimer.cancel();

        incorrectUserPassLabel.setVisible(true);

        incorrectUserPassTimer = new Timer();
        incorrectUserPassTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                // Your database code here
            }
        }, 2*60*1000);

        incorrectUserPassTimer.schedule(getIncorrectLoginTimerTask(), 3000);

    }

    /**
     * Creates a timer to hide the incorrect username password label
     * @return
     */
    private TimerTask getIncorrectLoginTimerTask() {
        return new TimerTask() {
            @Override
            public void run() {
                incorrectUserPassLabel.setVisible(false);
            }
        };
    }

    /**
     * Enables and disables all interactive elements on the login screen. Used when verifying credentials and logging in
     * @param bool Enable or disable all interactive elements
     */
    private void setButtonsDisabled(boolean bool) {
        usernameTextField.setDisable(bool);
        passwordField.setDisable(bool);
        loginButton.setDisable(bool);
        registerButton.setDisable(bool);
        forgotPasswordButton.setDisable(bool);
    }

    //</editor-fold>
}
