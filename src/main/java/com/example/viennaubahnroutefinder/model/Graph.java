package com.example.viennaubahnroutefinder.model;

import java.util.*;

public class Graph {

    public static class Station {
        private String name;
        private Map<Station, Edge> connections;
        private double latitude;
        private double longitude;

        public Station(String name) {
            this.name = name;
            this.connections = new HashMap<>();
        }

        public Station(String name, double latitude, double longitude) {
            this(name);
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public String getName() {
            return name;
        }

        public Map<Station, Edge> getConnections() {
            return connections;
        }

        public void addConnection(Station destination, int line, String color, double distance) {
            connections.put(destination, new Edge(this, destination, line, color, distance));
        }

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Station station = (Station) obj;
            return name.equals(station.name);
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public static class Edge {
        private Station source;
        private Station destination;
        private int line;
        private String color;
        private double distance;

        public Edge(Station source, Station destination, int line, String color, double distance) {
            this.source = source;
            this.destination = destination;
            this.line = line;
            this.color = color;
            this.distance = distance;
        }

        public Station getSource() {
            return source;
        }

        public Station getDestination() {
            return destination;
        }

        public int getLine() {
            return line;
        }

        public String getColor() {
            return color;
        }

        public double getDistance() {
            return distance;
        }
    }

    private Map<String, Station> stations;

    public Graph() {
        this.stations = new HashMap<>();
    }

    public void addStation(String name) {
        stations.putIfAbsent(name, new Station(name));
    }

    public void addStation(String name, double latitude, double longitude) {
        stations.putIfAbsent(name, new Station(name, latitude, longitude));
    }

    public void addConnection(String from, String to, int line, String color) {
        Station fromStation = stations.get(from);
        Station toStation = stations.get(to);

        if (fromStation != null && toStation != null) {
            double distance = calculateDistance(fromStation, toStation);
            fromStation.addConnection(toStation, line, color, distance);
            toStation.addConnection(fromStation, line, color, distance);
        }
    }

    private double calculateDistance(Station s1, Station s2) {
        return 1.0;
    }

    public Station getStation(String name) {
        return stations.get(name);
    }

    public Map<String, Station> getAllStations() {
        return stations;
    }

    public int getStationCount() {
        return stations.size();
    }
}