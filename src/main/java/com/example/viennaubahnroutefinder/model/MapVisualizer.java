package com.example.viennaubahnroutefinder.model;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import java.util.*;

public class MapVisualizer {

    private Canvas canvas;
    private GraphicsContext gc;
    private Map<String, MapPoint> stationPositions;
    private Map<String, Set<String>> drawnConnections;

    private static class MapPoint {
        double x;
        double y;

        MapPoint(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

    public MapVisualizer(Canvas canvas) {
        this.canvas = canvas;
        this.gc = canvas.getGraphicsContext2D();
        this.stationPositions = new HashMap<>();
        this.drawnConnections = new HashMap<>();
    }

    public void clearMap() {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setFill(Color.WHITESMOKE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        gc.setStroke(Color.LIGHTGRAY);
        gc.setLineWidth(0.5);
        for (int i = 0; i < canvas.getWidth(); i += 50) {
            gc.strokeLine(i, 0, i, canvas.getHeight());
        }
        for (int i = 0; i < canvas.getHeight(); i += 50) {
            gc.strokeLine(0, i, canvas.getWidth(), i);
        }
    }

    public void drawRoute(RouteFinder.Route route) {
        if (route == null || route.getStations().isEmpty()) {
            return;
        }

        clearMap();
        layoutStations(route);

        List<Graph.Station> stations = route.getStations();
        List<Graph.Edge> edges = route.getEdges();

        Map<String, Color> lineColors = new HashMap<>();
        for (Graph.Edge edge : edges) {
            lineColors.put(edge.getLine() + "-" + edge.getColor(), getLineColor(edge.getColor()));
        }

        gc.setLineWidth(6.0);
        gc.setLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        gc.setLineJoin(javafx.scene.shape.StrokeLineJoin.ROUND);

        for (int i = 0; i < edges.size(); i++) {
            Graph.Edge edge = edges.get(i);
            Graph.Station from = stations.get(i);
            Graph.Station to = stations.get(i + 1);

            MapPoint fromPoint = stationPositions.get(from.getName());
            MapPoint toPoint = stationPositions.get(to.getName());

            if (fromPoint != null && toPoint != null) {
                gc.setStroke(getLineColor(edge.getColor()));
                gc.strokeLine(fromPoint.x, fromPoint.y, toPoint.x, toPoint.y);
            }
        }

        gc.setLineWidth(2.0);
        gc.setFont(Font.font(11));

        for (int i = 0; i < stations.size(); i++) {
            Graph.Station station = stations.get(i);
            MapPoint point = stationPositions.get(station.getName());

            if (point != null) {
                if (i == 0) {
                    gc.setFill(Color.LIGHTGREEN);
                    gc.fillOval(point.x - 10, point.y - 10, 20, 20);
                    gc.setStroke(Color.DARKGREEN);
                    gc.strokeOval(point.x - 10, point.y - 10, 20, 20);
                    gc.setFill(Color.DARKGREEN);
                    gc.fillText("START", point.x - 20, point.y - 15);
                } else if (i == stations.size() - 1) {
                    gc.setFill(Color.LIGHTCORAL);
                    gc.fillOval(point.x - 10, point.y - 10, 20, 20);
                    gc.setStroke(Color.DARKRED);
                    gc.strokeOval(point.x - 10, point.y - 10, 20, 20);
                    gc.setFill(Color.DARKRED);
                    gc.fillText("END", point.x - 15, point.y - 15);
                } else {
                    gc.setFill(Color.WHITE);
                    gc.fillOval(point.x - 8, point.y - 8, 16, 16);
                    gc.setStroke(Color.BLACK);
                    gc.strokeOval(point.x - 8, point.y - 8, 16, 16);
                }

                gc.setFill(Color.BLACK);
                String label = station.getName();
                if (label.length() > 18) {
                    label = label.substring(0, 15) + "...";
                }

                double textX = point.x + 12;
                double textY = point.y + 4;

                gc.setFill(Color.WHITE);
                gc.fillRect(textX - 2, textY - 12, label.length() * 6.5 + 4, 16);
                gc.setFill(Color.BLACK);
                gc.fillText(label, textX, textY);
            }
        }

        drawLegend(lineColors);
        drawRouteInfo(route);
    }

    private void layoutStations(RouteFinder.Route route) {
        stationPositions.clear();

        List<Graph.Station> stations = route.getStations();
        if (stations.isEmpty()) return;

        double centerX = canvas.getWidth() / 2;
        double centerY = canvas.getHeight() / 2;
        double radius = Math.min(canvas.getWidth(), canvas.getHeight()) * 0.3;

        if (stations.size() == 1) {
            stationPositions.put(stations.get(0).getName(), new MapPoint(centerX, centerY));
            return;
        }

        if (stations.size() == 2) {
            stationPositions.put(stations.get(0).getName(), new MapPoint(centerX - 100, centerY));
            stationPositions.put(stations.get(1).getName(), new MapPoint(centerX + 100, centerY));
            return;
        }

        Map<Integer, List<Integer>> lineSegments = new HashMap<>();
        for (int i = 0; i < route.getEdges().size(); i++) {
            int line = route.getEdges().get(i).getLine();
            lineSegments.computeIfAbsent(line, k -> new ArrayList<>()).add(i);
        }

        double currentX = 100;
        double currentY = centerY;
        double segmentLength = 80;

        stationPositions.put(stations.get(0).getName(), new MapPoint(currentX, currentY));

        for (int i = 0; i < route.getEdges().size(); i++) {
            Graph.Edge edge = route.getEdges().get(i);

            double angle = 0;
            boolean isLineChange = false;

            if (i > 0 && edge.getLine() != route.getEdges().get(i - 1).getLine()) {
                isLineChange = true;
                angle = (edge.getLine() * 45) % 360;
            } else if (i % 3 == 0 && i > 0) {
                angle = ((i / 3) * 30) % 360;
            }

            if (isLineChange) {
                angle = Math.toRadians(angle);
                currentX += segmentLength * Math.cos(angle);
                currentY += segmentLength * Math.sin(angle) * 0.5;
            } else {
                currentX += segmentLength;
                if (i % 5 == 0) {
                    currentY += (Math.random() - 0.5) * 40;
                }
            }

            currentX = Math.max(50, Math.min(canvas.getWidth() - 150, currentX));
            currentY = Math.max(100, Math.min(canvas.getHeight() - 100, currentY));

            stationPositions.put(stations.get(i + 1).getName(), new MapPoint(currentX, currentY));
        }
    }

    private void drawLegend(Map<String, Color> lineColors) {
        gc.setFont(Font.font(12));
        gc.setFill(Color.BLACK);
        gc.fillText("Lines on this route:", 10, 20);

        int y = 40;
        for (Map.Entry<String, Color> entry : lineColors.entrySet()) {
            String[] parts = entry.getKey().split("-");
            gc.setFill(entry.getValue());
            gc.fillRect(10, y - 10, 20, 10);
            gc.setFill(Color.BLACK);
            gc.fillText("U" + parts[0] + " (" + parts[1] + ")", 35, y);
            y += 20;
        }
    }

    private void drawRouteInfo(RouteFinder.Route route) {
        gc.setFont(Font.font(14));
        gc.setFill(Color.BLACK);

        String info = String.format("Route: %d stations, %d line changes",
                route.getStations().size(),
                route.getLineChanges());

        double textWidth = info.length() * 8;
        double x = canvas.getWidth() - textWidth - 20;
        double y = 30;

        gc.setFill(Color.WHITE);
        gc.fillRect(x - 5, y - 20, textWidth + 10, 25);
        gc.setFill(Color.BLACK);
        gc.fillText(info, x, y);
    }

    private Color getLineColor(String colorName) {
        switch (colorName.toLowerCase()) {
            case "red": return Color.RED;
            case "purple": return Color.PURPLE;
            case "orange": return Color.ORANGE;
            case "green": return Color.GREEN;
            case "brown": return Color.BROWN;
            default: return Color.GRAY;
        }
    }
}