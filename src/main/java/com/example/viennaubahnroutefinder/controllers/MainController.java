package com.example.viennaubahnroutefinder.controllers;

import com.example.viennaubahnroutefinder.data.DataLoader;
import com.example.viennaubahnroutefinder.model.Graph;
import com.example.viennaubahnroutefinder.model.MapVisualizer;
import com.example.viennaubahnroutefinder.model.RouteFinder;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import java.util.*;

public class MainController {

    @FXML private ComboBox<String> startStationCombo;
    @FXML private ComboBox<String> endStationCombo;
    @FXML private ListView<String> waypointsList;
    @FXML private ComboBox<String> waypointCombo;
    @FXML private Button addWaypointBtn;
    @FXML private Button removeWaypointBtn;
    @FXML private ListView<String> avoidStationsList;
    @FXML private ComboBox<String> avoidStationCombo;
    @FXML private Button addAvoidBtn;
    @FXML private Button removeAvoidBtn;
    @FXML private Slider lineChangePenaltySlider;
    @FXML private Label penaltyLabel;
    @FXML private Button findSingleRouteBtn;
    @FXML private Button findMultipleRoutesBtn;
    @FXML private Button findShortestRouteBtn;
    @FXML private Button findShortestWithPenaltyBtn;
    @FXML private TreeView<String> routeTreeView;
    @FXML private TextArea routeDetailsArea;
    @FXML private Canvas mapCanvas;
    @FXML private Label statusLabel;

    private Graph graph;
    private RouteFinder routeFinder;
    private MapVisualizer mapVisualizer;
    private ObservableList<String> waypointsData;
    private ObservableList<String> avoidStationsData;
    private List<RouteFinder.Route> currentRoutes;

    @FXML
    public void initialize() {
        graph = DataLoader.loadGraph();
        routeFinder = new RouteFinder();
        mapVisualizer = new MapVisualizer(mapCanvas);

        waypointsData = FXCollections.observableArrayList();
        avoidStationsData = FXCollections.observableArrayList();

        List<String> stationNames = new ArrayList<>(graph.getAllStations().keySet());
        Collections.sort(stationNames);

        startStationCombo.getItems().addAll(stationNames);
        endStationCombo.getItems().addAll(stationNames);
        waypointCombo.getItems().addAll(stationNames);
        avoidStationCombo.getItems().addAll(stationNames);

        waypointsList.setItems(waypointsData);
        avoidStationsList.setItems(avoidStationsData);

        lineChangePenaltySlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            penaltyLabel.setText(String.format("%.1f km", newVal.doubleValue()));
        });

        findSingleRouteBtn.setOnAction(e -> findSingleRoute());
        findMultipleRoutesBtn.setOnAction(e -> findMultipleRoutes());
        findShortestRouteBtn.setOnAction(e -> findShortestRoute());
        findShortestWithPenaltyBtn.setOnAction(e -> findShortestRouteWithPenalty());

        addWaypointBtn.setOnAction(e -> addWaypoint());
        removeWaypointBtn.setOnAction(e -> removeWaypoint());
        addAvoidBtn.setOnAction(e -> addAvoidStation());
        removeAvoidBtn.setOnAction(e -> removeAvoidStation());

        routeTreeView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                displayRouteDetails(newVal);
            }
        });

        mapVisualizer.clearMap();
        statusLabel.setText("Ready");
    }

    private void addWaypoint() {
        String selected = waypointCombo.getValue();
        if (selected != null && !waypointsData.contains(selected)) {
            waypointsData.add(selected);
        }
    }

    private void removeWaypoint() {
        String selected = waypointsList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            waypointsData.remove(selected);
        }
    }

    private void addAvoidStation() {
        String selected = avoidStationCombo.getValue();
        if (selected != null && !avoidStationsData.contains(selected)) {
            avoidStationsData.add(selected);
        }
    }

    private void removeAvoidStation() {
        String selected = avoidStationsList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            avoidStationsData.remove(selected);
        }
    }

    private boolean validateInput() {
        if (startStationCombo.getValue() == null || endStationCombo.getValue() == null) {
            showAlert("Please select both start and end stations.");
            return false;
        }

        if (startStationCombo.getValue().equals(endStationCombo.getValue())) {
            showAlert("Start and end stations cannot be the same.");
            return false;
        }

        return true;
    }

    private void findSingleRoute() {
        if (!validateInput()) return;

        statusLabel.setText("Finding route...");

        Set<String> avoidStations = new HashSet<>(avoidStationsData);
        List<String> waypoints = new ArrayList<>(waypointsData);

        RouteFinder.Route route = routeFinder.findShortestRouteBFS(
                graph,
                startStationCombo.getValue(),
                endStationCombo.getValue(),
                avoidStations,
                waypoints
        );

        currentRoutes = new ArrayList<>();
        if (route != null) {
            currentRoutes.add(route);
            displayRoutes();
            statusLabel.setText("Route found");
        } else {
            statusLabel.setText("No route found");
            routeTreeView.setRoot(null);
            routeDetailsArea.clear();
            mapVisualizer.clearMap();
        }
    }

    private void findMultipleRoutes() {
        if (!validateInput()) return;

        statusLabel.setText("Finding routes...");

        Set<String> avoidStations = new HashSet<>(avoidStationsData);
        List<String> waypoints = new ArrayList<>(waypointsData);

        currentRoutes = routeFinder.findAllRoutesDFS(
                graph,
                startStationCombo.getValue(),
                endStationCombo.getValue(),
                avoidStations,
                waypoints
        );

        if (!currentRoutes.isEmpty()) {
            displayRoutes();
            statusLabel.setText("Found " + currentRoutes.size() + " routes");
        } else {
            statusLabel.setText("No routes found");
            routeTreeView.setRoot(null);
            routeDetailsArea.clear();
            mapVisualizer.clearMap();
        }
    }

    private void findShortestRoute() {
        if (!validateInput()) return;

        statusLabel.setText("Finding shortest route...");

        Set<String> avoidStations = new HashSet<>(avoidStationsData);
        List<String> waypoints = new ArrayList<>(waypointsData);

        RouteFinder.Route route = routeFinder.findShortestDistanceRoute(
                graph,
                startStationCombo.getValue(),
                endStationCombo.getValue(),
                avoidStations,
                waypoints,
                0
        );

        currentRoutes = new ArrayList<>();
        if (route != null) {
            currentRoutes.add(route);
            displayRoutes();
            statusLabel.setText("Shortest route found");
        } else {
            statusLabel.setText("No route found");
            routeTreeView.setRoot(null);
            routeDetailsArea.clear();
            mapVisualizer.clearMap();
        }
    }

    private void findShortestRouteWithPenalty() {
        if (!validateInput()) return;

        statusLabel.setText("Finding route with line change penalty...");

        Set<String> avoidStations = new HashSet<>(avoidStationsData);
        List<String> waypoints = new ArrayList<>(waypointsData);
        double penalty = lineChangePenaltySlider.getValue();

        RouteFinder.Route route = routeFinder.findShortestDistanceRoute(
                graph,
                startStationCombo.getValue(),
                endStationCombo.getValue(),
                avoidStations,
                waypoints,
                penalty
        );

        currentRoutes = new ArrayList<>();
        if (route != null) {
            currentRoutes.add(route);
            displayRoutes();
            statusLabel.setText("Route found with penalty");
        } else {
            statusLabel.setText("No route found");
            routeTreeView.setRoot(null);
            routeDetailsArea.clear();
            mapVisualizer.clearMap();
        }
    }

    private void displayRoutes() {
        TreeItem<String> root = new TreeItem<>("Routes (" + currentRoutes.size() + ")");

        for (int i = 0; i < currentRoutes.size(); i++) {
            RouteFinder.Route route = currentRoutes.get(i);
            String routeInfo = String.format("Route %d: %d stations, %.2f km, %d changes",
                    i + 1,
                    route.getStations().size(),
                    route.getTotalDistance(),
                    route.getLineChanges()
            );

            TreeItem<String> routeItem = new TreeItem<>(routeInfo);
            routeItem.setExpanded(true);

            for (int j = 0; j < route.getStations().size(); j++) {
                Graph.Station station = route.getStations().get(j);
                String stationInfo = station.getName();

                if (j < route.getEdges().size()) {
                    Graph.Edge edge = route.getEdges().get(j);
                    stationInfo += " → [U" + edge.getLine() + "]";
                }

                TreeItem<String> stationItem = new TreeItem<>(stationInfo);
                routeItem.getChildren().add(stationItem);
            }

            root.getChildren().add(routeItem);
        }

        root.setExpanded(true);
        routeTreeView.setRoot(root);

        if (!currentRoutes.isEmpty()) {
            mapVisualizer.drawRoute(currentRoutes.get(0));
        }
    }

    private void displayRouteDetails(TreeItem<String> selectedItem) {
        if (selectedItem == null || selectedItem.getParent() == null) {
            return;
        }

        TreeItem<String> routeItem = selectedItem;
        if (routeItem.getParent().getParent() != null) {
            routeItem = selectedItem.getParent();
        }

        int routeIndex = routeItem.getParent().getChildren().indexOf(routeItem);
        if (routeIndex >= 0 && routeIndex < currentRoutes.size()) {
            RouteFinder.Route route = currentRoutes.get(routeIndex);

            StringBuilder details = new StringBuilder();
            details.append("Route Details\n");
            details.append("=============\n\n");
            details.append("Total Stations: ").append(route.getStations().size()).append("\n");
            details.append("Total Distance: ").append(String.format("%.2f km", route.getTotalDistance())).append("\n");
            details.append("Line Changes: ").append(route.getLineChanges()).append("\n\n");

            details.append("Station Sequence:\n");
            details.append("-----------------\n");

            for (int i = 0; i < route.getStations().size(); i++) {
                Graph.Station station = route.getStations().get(i);
                details.append(String.format("%2d. %s", i + 1, station.getName()));

                if (i < route.getEdges().size()) {
                    Graph.Edge edge = route.getEdges().get(i);
                    details.append(String.format(" → U%d (%s) → ", edge.getLine(), edge.getColor()));
                }

                details.append("\n");
            }

            routeDetailsArea.setText(details.toString());
            mapVisualizer.drawRoute(route);
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Input Validation");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}