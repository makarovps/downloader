package nsk.makarov.pavel.ctrl.utils;

import javafx.scene.control.Alert;

/**
 * Created by pavel on 17.04.17.
 */
public class ControllerUtils {
    /* Show alert dialog on an exception occured */
    public static void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Alert!");
        alert.setHeaderText("An error occured...");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
