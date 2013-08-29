package com.github.patrickhousley.arrstatus_desktop.status;

import com.github.patrickhousley.arrstatus_desktop.dao.StatusDAO;
import com.github.patrickhousley.arrstatus_desktop.model.ServerRegion;
import com.github.patrickhousley.arrstatus_desktop.model.StatusModel;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TimelineBuilder;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import javafx.util.Duration;

/**
 * Controller for the status FXML view.
 *
 * @author Patrick Housley
 * @version 24AUG2013
 */
public class StatusController {

    @FXML
    private ListView nonRegionServerList;
    @FXML
    private ChoiceBox regionSelect;
    @FXML
    private ListView serverList;
    @FXML
    private ProgressIndicator refreshProgress;
    @FXML
    private Button refresh;
    private HashMap<String, StatusModel> statuses;
    private Service refresher;
    private Timeline refreshTimer;

    /**
     * Constructor sets up stage and applies event handlers.
     * @param primaryStage primary stage created by main FX thread
     * @throws IOException if the FXML file is missing
     */
    public StatusController(final Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("StatusView.fxml"));
        loader.setController(this);
        loader.load();

        Parent root = loader.getRoot();
        Scene scene = new Scene(root);

        final Stage statusStage = new Stage(StageStyle.DECORATED);
        statusStage.initModality(Modality.APPLICATION_MODAL);
        statusStage.initOwner(primaryStage);
        statusStage.setScene(scene);
        statusStage.setTitle("ARRStatus");

        statusStage.setWidth(220);
        statusStage.setMaxWidth(220);
        statusStage.setMinHeight(300);

        nonRegionServerList.setCellFactory(new Callback<ListView<StatusModel>, ListCell<StatusModel>>() {
            @Override
            public ListCell<StatusModel> call(ListView<StatusModel> param) {
                return new ServerListItem();
            }
        });

        serverList.setCellFactory(new Callback<ListView<StatusModel>, ListCell<StatusModel>>() {
            @Override
            public ListCell<StatusModel> call(ListView<StatusModel> param) {
                return new ServerListItem();
            }
        });

        statusStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                System.exit(0);
            }
        });

        refresh.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent ae) {
                unscheduleRefresh();
                scheduleRefresh();
            }
        });

        List<ServerRegion> regions = new ArrayList<>();
        for (ServerRegion sr : ServerRegion.values()) {
            if (sr != ServerRegion.NONE) {
                regions.add(sr);
            }
        }
        regionSelect.setItems(FXCollections.observableArrayList(regions));
        regionSelect.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<ServerRegion>() {
            @Override
            public void changed(ObservableValue<? extends ServerRegion> ov, ServerRegion t, ServerRegion t1) {
                updateServerList(ov.getValue());
            }
        });
        
        nonRegionServerList.setEditable(false);
        serverList.setEditable(false);
        scheduleRefresh();

        statusStage.show();
    }

    /**
     * Initializes the refresh server, sets up event handlers for the refresh
     * task, and establishes a duration between refreshes.
     */
    private void scheduleRefresh() {
        refresher = new Service() {
            @Override
            protected Task createTask() {
                Task t = new Task<HashMap<String, StatusModel>>() {
                    @Override
                    public HashMap<String, StatusModel> call() {
                        HashMap<String, StatusModel> result = null;
                        try {
                            StatusDAO.refresh();
                            result = StatusDAO.getStatus();
                        } catch (InterruptedException ie) {
                            result = null;
                        }
                        return result;
                    }
                };
                return t;
            }
        };
        refresher.stateProperty().addListener(new ChangeListener<Worker.State>() {
            @Override
            public void changed(ObservableValue<? extends Worker.State> ov,
                    Worker.State t, Worker.State t1) {
                if (t1 == Worker.State.RUNNING) {
                    nonRegionServerList.setItems(null);
                    serverList.setItems(null);
                    refreshProgress.setVisible(true);
                } else if (t1 == Worker.State.SUCCEEDED) {
                    statuses = (HashMap<String, StatusModel>) refresher.getValue();
                    if (statuses != null) {
                        ObservableList<StatusModel> nonRegion = FXCollections.observableArrayList(
                                statuses.get("Login"),
                                statuses.get("Patch"),
                                statuses.get("Lobby"));
                        nonRegionServerList.setItems(nonRegion);
                    }

                    ServerRegion sr =
                            (ServerRegion) regionSelect.getSelectionModel().selectedItemProperty().get();
                    if (sr != null) {

                        updateServerList(sr);
                    }

                    refreshProgress.setVisible(false);
                    refresher.reset();
                } else if (t1 == Worker.State.CANCELLED
                        || t1 == Worker.State.FAILED) {
                    refreshProgress.setVisible(false);
                    refresher.reset();
                }
            }
        });
        refresher.start();

        refreshTimer = TimelineBuilder.create()
                .cycleCount(Animation.INDEFINITE)
                .keyFrames(new KeyFrame(Duration.seconds(29),
                new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                try {
                    refresher.start();
                } catch (IllegalStateException ise) {
                    refresher.cancel();
                    refresher.reset();
                }
            }
        })).build();
        refreshTimer.play();
    }

    /**
     * Cancels and removes reference to the refresh service and the timeline
     * used to repeat the service.
     */
    private void unscheduleRefresh() {
        refresher.cancel();
        refreshTimer.stop();
        refresher = null;
        refreshTimer = null;
        nonRegionServerList.setItems(null);
    }

    /**
     * Refresh the region based server list based on the given region.
     * @param sr region selected
     */
    private void updateServerList(ServerRegion sr) {
        serverList.setItems(null);

        if (statuses != null && !statuses.isEmpty()) {
            ObservableList<StatusModel> regionList = FXCollections.observableArrayList();
            for (Map.Entry<String, StatusModel> e : statuses.entrySet()) {
                if (e.getValue().getRegion() == sr) {
                    regionList.add(e.getValue());
                }
            }
            FXCollections.sort(regionList, new Comparator<StatusModel>() {
                @Override
                public int compare(StatusModel o1, StatusModel o2) {
                    return o1.getName().compareTo(o2.getName());
                }
            });
            serverList.setItems(regionList);
        }
    }

    static class ServerListItem extends ListCell<StatusModel> {

        @Override
        public void updateItem(StatusModel item, boolean empty) {
            super.updateItem(item, empty);

            if (item != null) {
                HBox hb = new HBox();
                hb.setAlignment(Pos.CENTER_LEFT);
                hb.setMaxHeight(20);
                hb.setPrefHeight(20);
                hb.setSpacing(10);
                hb.getChildren().add(new Label(item.getName()));

                Pane p = new Pane();
                HBox.setHgrow(p, Priority.ALWAYS);
                hb.getChildren().add(p);

                hb.getChildren().add(new Label(item.getPing()));

                ImageView serverImage = new ImageView(new Image(item.getStatusImage()));
                hb.getChildren().add(serverImage);

                setGraphic(hb);
            }
        }
    }
}
