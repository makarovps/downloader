package nsk.makarov.pavel.ctrl;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.ProgressBarTableCell;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import nsk.makarov.pavel.DownloadManager;
import nsk.makarov.pavel.model.Download;
import nsk.makarov.pavel.model.DownloadException;
import nsk.makarov.pavel.model.DownloadState;

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;
import java.util.ResourceBundle;

import static nsk.makarov.pavel.ctrl.utils.ControllerUtils.showAlert;

/**
 * Created by pavel on 17.04.17.
 */
public class DownloadManagerController implements Initializable, Observer {
    /* Model members */
    private DownloadManager manager;

    /* Model-to-view members */
    private ObservableList<Download> downloads = FXCollections.observableArrayList();

    /* View members */
    // Table with downloads
    @FXML TableView table;
    // Download columns
    @FXML TableColumn noColumn;
    @FXML TableColumn sourceColumn;
    @FXML TableColumn destColumn;
    @FXML TableColumn statusColumn;
    @FXML TableColumn progressColumn;
    // Control buttons
    @FXML Button btnAdd;
    @FXML Button btnStart;
    @FXML Button btnPause;
    @FXML Button btnStop;
    @FXML Button btnDel;
    // Download info
    @FXML TextField infoSource;
    @FXML TextField infoDest;
    @FXML TextField infoDownloaded;
    @FXML TextField infoLeft;
    @FXML TextField infoState;
    @FXML TextArea infoFailure;

    private void setDownloadInfo(Download download) {
        infoSource.setText(download.getSource());
        infoDest.setText(download.getDest());
        infoDownloaded.setText(Integer.toString(download.getCurrentsize()));
        infoLeft.setText(Integer.toString(download.getTotalsize() - download.getCurrentsize()));
        infoState.setText(download.getState().toString());
        infoFailure.setText(download.getFailure());
    }

    private void clearDownloadInfo() {
        infoSource.setText(null);
        infoDest.setText(null);
        infoDownloaded.setText(null);
        infoLeft.setText(null);
        infoState.setText(null);
        infoFailure.setText(null);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        manager = new DownloadManager();

        noColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Download, String>, ObservableValue<String>>() {
            @Override public ObservableValue<String> call(TableColumn.CellDataFeatures<Download, String> p) {
                return new ReadOnlyObjectWrapper(table.getItems().indexOf(p.getValue()) + 1);
            }
        });
        progressColumn.setCellFactory(ProgressBarTableCell.<Download>forTableColumn());

        table.setItems(downloads);
        table.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                if (table.getSelectionModel().getSelectedItem() != null) {
                    Download download = (Download) table.getSelectionModel().getSelectedItem();
                    setDownloadInfo(download);
                }
            }
        });

        /* Buttons' action listeners */
        btnStart.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                try {
                    Download download = (Download) table.getSelectionModel().getSelectedItem();
                    if (download == null) {
                        throw new DownloadException("No download is selected");
                    } else if (download.getState() == DownloadState.IN_PROGRESS) {
                        throw new DownloadException("Download is already started");
                    }
                    download.start();
                } catch (DownloadException ed) {
                    showAlert(ed.getMessage());
                }
            }
        });
        btnPause.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                try {
                    Download download = (Download) table.getSelectionModel().getSelectedItem();
                    if (download == null) {
                        throw new DownloadException("No download is selected");
                    } else if (download.getState() != DownloadState.IN_PROGRESS) {
                        throw new DownloadException("Cannot pause: download is not started yet");
                    }
                    download.pause();
                } catch (DownloadException ed) {
                    showAlert(ed.getMessage());
                }
            }
        });
        btnStop.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                try {
                    Download download = (Download) table.getSelectionModel().getSelectedItem();
                    if (download == null) {
                        throw new DownloadException("No download is selected");
                    } else if (download.getState() == DownloadState.COMPLETED) {
                        throw  new DownloadException("Download is already finished");
                    }
                    download.cancel();
                } catch (DownloadException ed) {
                    showAlert(ed.getMessage());
                }
            }
        });
        btnAdd.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/add-dialog.fxml"));
                    Parent root = loader.load();
                    AddDialogController addController = loader.getController();

                    Stage stage = new Stage();
                    stage.setScene(new Scene(root));
                    stage.setTitle("Add download...");
                    stage.initModality(Modality.WINDOW_MODAL);
                    stage.initOwner(((Node)actionEvent.getSource()).getScene().getWindow());
                    stage.showAndWait();

                    if (addController.isOk()) {
                        Download download = manager.add(addController.getSource(), addController.getDest());
                        downloads.add(download);

                        download.addObserver(DownloadManagerController.this);

                        if (addController.isExecImmediately()) {
                            download.start();
                        }
                    }
                } catch (IOException | DownloadException e) {
                    showAlert(e.getMessage());
                }
            }
        });
        btnDel.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                try {
                    Download download = (Download) table.getSelectionModel().getSelectedItem();
                    if (download == null) {
                        throw new DownloadException("No download is selected");
                    }
                    manager.remove(download);
                    downloads.remove(download);

                    download.deleteObserver(DownloadManagerController.this);

                    clearDownloadInfo();
                } catch (DownloadException ed) {
                    showAlert(ed.getMessage());
                }
            }
        });
    }

    public void clear() {
        Iterator it =  manager.getDownloads().iterator();
        while (it.hasNext()) {
            Download download = (Download) it.next();
            if (download.getState() != DownloadState.CREATED && download.getState() != DownloadState.COMPLETED) {
                download.cancel();
            }
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        /* Workaround for JavaFx */
        Platform.runLater(new Runnable() {
            @Override public void run() {
                ((TableColumn)table.getColumns().get(0)).setVisible(false);
                ((TableColumn)table.getColumns().get(0)).setVisible(true);
                if (table.getSelectionModel().getSelectedItem() != null) {
                    Download download = (Download) table.getSelectionModel().getSelectedItem();
                    setDownloadInfo(download);
                } else {
                    clearDownloadInfo();
                }
            }
        });
    }
}