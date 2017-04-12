package nsk.makarov.pavel.view;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBuilder;
import javafx.scene.control.Label;
import javafx.scene.control.LabelBuilder;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumnBuilder;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFieldBuilder;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import nsk.makarov.pavel.DownloadManager;
import nsk.makarov.pavel.model.Download;
import nsk.makarov.pavel.model.DownloadException;
import nsk.makarov.pavel.model.DownloadState;

import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;

import static nsk.makarov.pavel.view.DownloadManagerViewItems.COLUMNS;


/**
 * Created by pavel on 10.04.17.
 */

class DownloadManagerViewItems {
    /* Columns list for download grid */
    static final TableColumn[] COLUMNS = {
            TableColumnBuilder.create()
                    .text("source")
                    .minWidth(300)
                    .cellValueFactory(new PropertyValueFactory("source"))
                    .build(),
            TableColumnBuilder.create()
                    .text("dest")
                    .minWidth(300)
                    .cellValueFactory(new PropertyValueFactory("dest"))
                    .build(),
            TableColumnBuilder.create()
                    .text("status")
                    .minWidth(100)
                    .cellValueFactory(new PropertyValueFactory("state"))
                    .build(),
            TableColumnBuilder.create()
                    .text("progress")
                    .minWidth(100)
                    .cellValueFactory(new PropertyValueFactory("progress"))
                    .build()
    };
    static final int IDX_COLUMN = 0;
    static final int SOURCE_COLUMN = 1;
    static final int DEST_COLUMN = 2;
    static final int STATUS_COLUMN = 3;
    static final int PROGRESS_COLUMN = 4;
}

class DownloadManagerAlertDialog extends Stage {
    private final static String TITLE = "Alert";

    public DownloadManagerAlertDialog(Stage stage, String message) {
        super();

        this.initModality(Modality.APPLICATION_MODAL);
        this.initOwner(stage);
        this.setTitle(TITLE);

        VBox dialogVbox = new VBox(20);
        dialogVbox.getChildren().add(new Text(message));

        Scene dialogScene = new Scene(dialogVbox, 200, 50);

        this.setScene(dialogScene);
    }

}

public class DownloadManagerView extends Application implements Observer {
    private DownloadManager manager = new DownloadManager();

    private TableView table = new TableView();

    private ObservableList<Download> downloads = FXCollections.observableArrayList();

    private HBox controlBox = new HBox();

    private HBox downloadBox = new HBox();

    /* Buttons list for controller */
    private static final int BUTTON_ADD = 0;
    private static final int BUTTON_DELETE = 1;
    private static final int BUTTON_STOP = 2;
    private static final int BUTTON_PAUSE = 3;
    private static final int BUTTON_RESUME = 4;

    private TextField[] ADD_TEXT_FIELDS = {
            TextFieldBuilder.create()
                    .text("")
                    .promptText("source: insert file's URL here")
                    .prefWidth(275)
                    .build(),
            TextFieldBuilder.create()
                    .text("")
                    .promptText("dest: insert path the file will be saved")
                    .prefWidth(275)
                    .build()
    };

    private static final int FIELD_SRC = 0;
    private static final int FIELD_DEST = 1;

    private HBox infoBox = new HBox();

    private TextField DOWNLOAD_INFO =
            TextFieldBuilder.create()
                    .text("")
                    .promptText("Download information")
                    .prefWidth(830)
                    .editable(false)
                    .build();

    @Override
    public void start(final Stage stage) throws Exception {
        /* Global scene values */
        Scene scene = new Scene(new Group());
        stage.setTitle("Yet Another Download Manager");
        stage.setWidth(845);
        stage.setHeight(525);

        /* Downloads table */
        table.setEditable(false);
        table.getColumns().addAll(COLUMNS);
        table.setItems(downloads);

        table.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                if (table.getSelectionModel().getSelectedItem() != null) {
                    Download download = (Download) table.getSelectionModel().getSelectedItem();
                    DOWNLOAD_INFO.setText(download.getFailure());
                }
            }
        });


        /* Control buttons */
        Button [] CTRL_BUTTONS = {
                ButtonBuilder.create()
                        .text("Add")
                        .onAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent e) {
                                try {
                                    String source = ADD_TEXT_FIELDS[FIELD_SRC].getText();
                                    String dest = ADD_TEXT_FIELDS[FIELD_DEST].getText();
                                    if (source == null || source.length() == 0) {
                                        throw new DownloadException("Source cannot be empty");
                                    } else if (dest == null || dest.length() == 0) {
                                        throw new DownloadException("Destination cannot be empty");
                                    }
                                    Download.validateSource(source);

                                    Download download = manager.add(source, dest);
                                    downloads.add(download);

                                    download.addObserver(DownloadManagerView.this);

                                    //ADD_TEXT_FIELDS[FIELD_SRC].setText("");
                                } catch (DownloadException ed) {
                                    (new DownloadManagerAlertDialog(stage, ed.getMessage())).showAndWait();
                                }
                            }
                        }).build(),
                ButtonBuilder.create()
                        .text("Delete")
                        .onAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent e) {
                                try {
                                    Download download = (Download) table.getSelectionModel().getSelectedItem();
                                    if (download == null) {
                                        throw new DownloadException("No download is selected");
                                    }
                                    manager.remove(download);
                                    downloads.remove(download);

                                    download.deleteObserver(DownloadManagerView.this);
                                } catch (DownloadException ed) {
                                    (new DownloadManagerAlertDialog(stage, ed.getMessage())).showAndWait();
                                }
                            }
                        }).build(),
        };
        Label addLabel = LabelBuilder.create()
                .text("Add your download here: ")
                .translateY(4)
                .build();

        controlBox.getChildren().add(addLabel);
        controlBox.getChildren().addAll(ADD_TEXT_FIELDS);
        controlBox.getChildren().addAll(CTRL_BUTTONS);

        Button [] DOWN_BUTTONS = {
                ButtonBuilder.create()
                        .text("Start")
                        .onAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent e) {
                                try {
                                    Download download = (Download) table.getSelectionModel().getSelectedItem();
                                    if (download == null) {
                                        throw new DownloadException("No download is selected");
                                    } else if (download.getState() == DownloadState.IN_PROGRESS) {
                                        throw new DownloadException("Download already started");
                                    }
                                    download.start();
                                } catch (DownloadException ed) {
                                    (new DownloadManagerAlertDialog(stage, ed.getMessage())).showAndWait();
                                }
                            }
                        }).build(),
                ButtonBuilder.create()
                        .text("Pause")
                        .onAction(new EventHandler<ActionEvent>() {
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
                                    (new DownloadManagerAlertDialog(stage, ed.getMessage())).showAndWait();
                                }
                            }
                        }).build(),
                ButtonBuilder.create()
                        .text("Resume")
                        .onAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent e) {
                                try {
                                    Download download = (Download) table.getSelectionModel().getSelectedItem();
                                    if (download == null) {
                                        throw new DownloadException("No download is selected");
                                    } else if (download.getState() == DownloadState.IN_PROGRESS) {
                                        throw new DownloadException("Download already started");
                                    }
                                    download.resume();
                                } catch (DownloadException ed) {
                                    (new DownloadManagerAlertDialog(stage, ed.getMessage())).showAndWait();
                                }
                            }
                        }).build(),
                ButtonBuilder.create()
                        .text("Stop")
                        .onAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent e) {
                                try {
                                    Download download = (Download) table.getSelectionModel().getSelectedItem();
                                    if (download == null) {
                                        throw new DownloadException("No download is selected");
                                    } else if (download.getState() == DownloadState.COMPLETED) {
                                        throw  new DownloadException("Download already finished");
                                    }
                                    download.cancel();
                                } catch (DownloadException ed) {
                                    (new DownloadManagerAlertDialog(stage, ed.getMessage())).showAndWait();
                                }
                            }
                        }).build()
        };

        /* Download info section */
        infoBox.getChildren().add(DOWNLOAD_INFO);

        /* Download section controls */
        downloadBox.getChildren().addAll(DOWN_BUTTONS);

        /* Adding custom elements to the View*/
        final VBox vbox = new VBox();
        vbox.setSpacing(5);
        vbox.setPadding(new Insets(5, 0, 0, 5));
        vbox.getChildren().addAll(controlBox, table, infoBox, downloadBox);

        /* Applying elements */
        ((Group) scene.getRoot()).getChildren().addAll(vbox);

        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        /* Clear all unfinished files */
        Iterator it =  manager.getDownloads().iterator();
        while (it.hasNext()) {
            Download download = (Download) it.next();
            if (download.getState() != DownloadState.CREATED && download.getState() != DownloadState.COMPLETED) {
                download.cancel();
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void update(Observable o, Object arg) {
        /* Workaround for JavaFx version less than 8u60 */
        Platform.runLater(new Runnable() {
            @Override public void run() {
                ((TableColumn)table.getColumns().get(0)).setVisible(false);
                ((TableColumn)table.getColumns().get(0)).setVisible(true);
                if (table.getSelectionModel().getSelectedItem() != null) {
                    Download download = (Download) table.getSelectionModel().getSelectedItem();
                    DOWNLOAD_INFO.setText(download.getFailure());
                }
            }
        });
    }
}
