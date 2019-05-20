/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package coursemanager;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author Dani
 */
public class CourseManager extends Application {
    
    @Override
    public void start(Stage stage) throws Exception {        
        FXMLLoader myLoader = new FXMLLoader(getClass().getResource("MainWindow.fxml"));
        Parent root = myLoader.load();

        MainWindowController mainController = myLoader.getController();
        mainController.initWindow(stage);
        Scene scene = new Scene(root);
        stage.setTitle("Gestión de academia");
        stage.setScene(scene);

        scene.getStylesheets().add(getClass().getResource("/css/mainStyle.css").toExternalForm());
        stage.show();

        stage.setMinWidth(800);
        stage.setMinHeight(420);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
