package nsk.makarov.pavel.ctrl;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import nsk.makarov.pavel.model.Download;
import nsk.makarov.pavel.model.DownloadException;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import static nsk.makarov.pavel.ctrl.utils.ControllerUtils.showAlert;

/**
 * Created by pavel on 17.04.17.
 */
public class AddDialogController implements Initializable {
    @FXML TextField fieldSource;
    @FXML TextField fieldDest;
    @FXML CheckBox chkExecImmediately;

    @FXML Button btnOk;
    @FXML Button btnCancel;
    @FXML Button btnChoose;

    private String source;
    private String dest;
    private boolean execImmediately;

    private boolean isOk;

    private File selectedDirectory;

    public String getSource() {
        return source;
    }

    private void setSource(String source) {
        this.source = source;
    }

    public String getDest() {
        return dest;
    }

    public void setDest(String dest) {
        this.dest = dest;
    }

    public boolean isExecImmediately() {
        return execImmediately;
    }

    private void setExecImmediately(boolean execImmediately) {
        this.execImmediately = execImmediately;
    }

    public boolean isOk() {
        return isOk;
    }

    private void setOk(boolean ok) {
        isOk = ok;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        selectedDirectory = getDest() != null ? new File(getDest()) : new File("/tmp");

        btnOk.setOnAction(new EventHandler<ActionEvent >() {
            @Override
            public void handle(ActionEvent e) {
                try {
                    Stage stage = (Stage) (((Node) e.getSource()).getScene().getWindow());

                    String source = fieldSource.getText();
                    Download.validateSource(source);
                    setSource(source);

                    String dest = fieldDest.getText();
                    Download.validateDest(dest);
                    setDest(dest);

                    boolean execImmediately = chkExecImmediately.isSelected();
                    setExecImmediately(execImmediately);

                    stage.close();
                    setOk(true);
                } catch (DownloadException ed) {
                    showAlert(ed.getMessage());
                    setOk(false);
                }
            }
        });
        btnCancel.setOnAction(new EventHandler<ActionEvent >() {
            @Override
            public void handle(ActionEvent e) {
                Stage stage = (Stage) (((Node)e.getSource()).getScene().getWindow());
                stage.close();
                setOk(false);
            }
        });
        btnChoose.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Stage stage = (Stage) (((Node)event.getSource()).getScene().getWindow());

                DirectoryChooser chooser = new DirectoryChooser();
                chooser.setTitle("Select path to store the file");

                chooser.setInitialDirectory(selectedDirectory);
                selectedDirectory = chooser.showDialog(stage);

                if (selectedDirectory != null) {
                    fieldDest.setText(selectedDirectory.getAbsolutePath());
                }
            }
        });

    }

}
