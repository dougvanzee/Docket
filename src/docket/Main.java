package docket;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.image.Image;

/**
 * Main application
 */
public class Main extends Application {

    /**
     * Show Login Screen
     * @param primaryStage
     * @throws Exception
     */
    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("LoginScreen.fxml"));
        primaryStage.setTitle("Docket");
        primaryStage.getIcons().add(new Image("Icon.png"));
        primaryStage.setScene(new Scene(root, 1000, 800));
        primaryStage.show();
    }

    /**
     * Start of application
     * @param args
     */
    public static void main(String[] args) {
        launch(args);
    }
}
