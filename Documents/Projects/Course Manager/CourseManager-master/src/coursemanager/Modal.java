/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package coursemanager;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.function.Consumer;

/**
 *
 * @author Dani
 */
public class Modal {
    /**
     * Lanza una ventana de error
     * @param content la escrita antes del boton OK
     */
    public static void showAlert(Alert.AlertType type, String content) {
        Alert alert = new Alert(type, content);
        alert.showAndWait();
    }

    public static void showError(String content) {
        Modal.showAlert(Alert.AlertType.ERROR, content);
    }

    public static void showWarning(String content) {
        Modal.showAlert(Alert.AlertType.WARNING, content);
    }

    public static boolean showConfirmation(String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, content);
        Optional<ButtonType> response = alert.showAndWait();
        return response.isPresent() && (response.get() == ButtonType.OK);
    }

    public static ButtonType showThreeWayConfirmation(String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, content, ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
        Optional<ButtonType> response = alert.showAndWait();
        return response.orElse(ButtonType.CANCEL);
    }

    public static <Controller> void showFXML(URL fxml, Consumer<Controller> initialize, String title, Stage ownerStage) {
        FXMLLoader myLoader = new FXMLLoader(fxml);
        Parent root;
        try {
            root = myLoader.load();
        } catch (IOException e) {
            Modal.showError("¡No se ha cargado una ventana de diálogo!");
            return;
        }

        //Now we can get the controller of the main window
        Controller controller = myLoader.getController();

        Stage modalStage = new Stage();
        initialize.accept(controller);

        //We create and load the main scene
        Scene scene = new Scene(root);
        modalStage.setTitle(title);
        modalStage.setScene(scene);
        modalStage.initModality(Modality.APPLICATION_MODAL);
        modalStage.initOwner(ownerStage);
        modalStage.centerOnScreen();
        modalStage.showAndWait();
    }
}
