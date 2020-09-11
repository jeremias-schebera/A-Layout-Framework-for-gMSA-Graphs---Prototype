package main.java.application.Controller;

import main.java.application.Algorithmen.GraphProjectionSugiyama;
import main.java.application.GUI.MainWindowController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Parent;

public class MainController
    extends Application {

    private MainWindowController mainWindowController;
    private static Stage primaryStage;
    public GraphProjectionSugiyama sugiyamaProjection;

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        mainWindow();
    }

    @Override
    public void stop() {
        System.out.println("Stage is closing");
        if (mainWindowController != null) {
            mainWindowController.stop();
        }
    }

    public void mainWindow() {
        System.out.println(MainController.class);
        mainWindowController = MainWindowController.getInstance();
        mainWindowController.init(this);

        primaryStage.setMinWidth(500);
        primaryStage.setMinHeight(300);
        primaryStage.setTitle("Super Genome Browser");
//            primaryStage.getIcons().add(new Image("/logo.png"));

        Parent root = mainWindowController.getRoot();
        Scene scene = new Scene(root);
//            scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }
}