package coursemanager;

import javafx.scene.control.Alert;
import javafx.scene.control.Control;

public class ValidationException extends Exception {
    private Control control;

    public ValidationException(String message, Control control) {
        super(message);
        this.control = control;
    }

    public Control getControl() {
        return control;
    }

    public void handle() {
        Alert alert = new Alert(Alert.AlertType.WARNING, getMessage());
        alert.setHeaderText("");
        alert.setTitle("Datos incorrectos");
        alert.showAndWait();
        control.requestFocus();
    }
}
