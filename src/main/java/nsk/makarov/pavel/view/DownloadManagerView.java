package nsk.makarov.pavel.view;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import nsk.makarov.pavel.ctrl.DownloadManagerController;

/**
 * Created by pavel on 16.04.17.
 */
public class DownloadManagerView extends Application {
    private DownloadManagerController controller;

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/download-manager.fxml"));
        Parent root = loader.load();

        controller = loader.getController();

        stage.setScene(new Scene(root, 920, 750));
        stage.setTitle("Yet Another Download Manager");
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        /* Clear all unfinished tasks */
        controller.clear();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
