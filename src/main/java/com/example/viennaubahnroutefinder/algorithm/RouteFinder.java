package com.example.viennaubahnroutefinder.algorithm;

import com.example.viennaubahnroutefinder.model.Graph;

import java.util.*;

public class RouteFinder {

    public static class Route {
        private List<Graph.Station> stations;
        private List<Graph.Edge> edges;
        private double totalDistance;

        public Route() {
            this.stations = new ArrayList<>();
            this.edges = new ArrayList<>();
            this.totalDistance = 0;
        }

        public Route(Route other) {
            this.stations = new ArrayList<>(other.stations);
            this.edges = new ArrayList<>(other.edges);
            this.totalDistance = other.totalDistance;
        }

        public void addStation(Graph.Station station, Graph.Edge edge) {
            stations.add(station);
            if (edge != null) {
                edges.add(edge);
                totalDistance += edge.getDistance();
            }
        }

        public List<Graph.Station> getStations() {
            return stations;
        }

        public List<Graph.Edge> getEdges() {
            return edges;
        }

        public double getTotalDistance() {
            return totalDistance;
        }

        public int getLineChanges() {
            int changes = 0;
            for (int i = 1; i < edges.size(); i++) {
                if (edges.get(i).getLine() != edges.get(i - 1).getLine()) {
                    changes++;
                }
            }
            return changes;
        }
    }

    public List<Route> findAllRoutesDFS(Graph graph, String start, String end,
                                        Set<String> avoidStations, List<String> waypoints) {
        List<Route> allRoutes = new ArrayList<>();
        Graph.Station startStation = graph.getStation(start);
        Graph.Station endStation = graph.getStation(end);

        if (startStation == null || endStation == null) {
            return allRoutes;
        }

        if (waypoints == null || waypoints.isEmpty()) {
            findRoutesDFS(startStation, endStation, new HashSet<>(), new Route(),
                    allRoutes, avoidStations, 15);
        } else {
            findRoutesWithWaypointsDFS(graph, start, end, waypoints, avoidStations, allRoutes);
        }

        return allRoutes;
    }

    private void findRoutesDFS(Graph.Station current, Graph.Station end,
                               Set<Graph.Station> visited, Route currentRoute,
                               List<Route> allRoutes, Set<String> avoidStations, int maxDepth) {
        if (maxDepth <= 0 || (avoidStations != null && avoidStations.contains(current.getName()))) {
            return;
        }

        visited.add(current);
        currentRoute.addStation(current, null);

        if (current.equals(end)) {
            allRoutes.add(new Route(currentRoute));
        } else {
            for (Map.Entry<Graph.Station, Graph.Edge> entry : current.getConnections().entrySet()) {
                Graph.Station neighbor = entry.getKey();
                if (!visited.contains(neighbor)) {
                    Route newRoute = new Route(currentRoute);
                    newRoute.getStations().remove(newRoute.getStations().size() - 1);
                    newRoute.addStation(current, entry.getValue());
                    findRoutesDFS(neighbor, end, visited, newRoute, allRoutes, avoidStations, maxDepth - 1);
                }
            }
        }

        visited.remove(current);
    }

    private void findRoutesWithWaypointsDFS(Graph graph, String start, String end,
                                            List<String> waypoints, Set<String> avoidStations,
                                            List<Route> allRoutes) {
        List<String> fullPath = new ArrayList<>();
        fullPath.add(start);
        fullPath.addAll(waypoints);
        fullPath.add(end);

        List<Route> currentSegmentRoutes = null;

        for (int i = 0; i < fullPath.size() - 1; i++) {
            String from = fullPath.get(i);
            String to = fullPath.get(i + 1);

            List<Route> segmentRoutes = findAllRoutesDFS(graph, from, to, avoidStations, null);

            if (segmentRoutes.isEmpty()) {
                return;
            }

            if (currentSegmentRoutes == null) {
                currentSegmentRoutes = segmentRoutes;
            } else {
                currentSegmentRoutes = combineRoutes(currentSegmentRoutes, segmentRoutes);
            }
        }

        allRoutes.addAll(currentSegmentRoutes);
    }

    private List<Route> combineRoutes(List<Route> routes1, List<Route> routes2) {
        List<Route> combined = new ArrayList<>();

        for (Route r1 : routes1) {
            for (Route r2 : routes2) {
                Route combinedRoute = new Route();

                for (int i = 0; i < r1.getStations().size() - 1; i++) {
                    combinedRoute.getStations().add(r1.getStations().get(i));
                    if (i < r1.getEdges().size()) {
                        combinedRoute.getEdges().add(r1.getEdges().get(i));
                    }
                }

                for (int i = 0; i < r2.getStations().size(); i++) {
                    combinedRoute.getStations().add(r2.getStations().get(i));
                    if (i < r2.getEdges().size()) {
                        combinedRoute.getEdges().add(r2.getEdges().get(i));
                    }
                }

                combinedRoute.totalDistance = r1.getTotalDistance() + r2.getTotalDistance();
                combined.add(combinedRoute);
            }
        }

        return combined;
    }

    public Route findShortestRouteBFS(Graph graph, String start, String end,
                                      Set<String> avoidStations, List<String> waypoints) {
        if (waypoints != null && !waypoints.isEmpty()) {
            return findRouteWithWaypoints(graph, start, end, waypoints, avoidStations, true);
        }

        Graph.Station startStation = graph.getStation(start);
        Graph.Station endStation = graph.getStation(end);

        if (startStation == null || endStation == null) {
            return null;
        }

        Queue<Route> queue = new LinkedList<>();
        Set<Graph.Station> visited = new HashSet<>();

        Route initialRoute = new Route();
        initialRoute.addStation(startStation, null);
        queue.offer(initialRoute);
        visited.add(startStation);

        while (!queue.isEmpty()) {
            Route currentRoute = queue.poll();
            Graph.Station currentStation = currentRoute.getStations().get(currentRoute.getStations().size() - 1);

            if (currentStation.equals(endStation)) {
                return currentRoute;
            }

            for (Map.Entry<Graph.Station, Graph.Edge> entry : currentStation.getConnections().entrySet()) {
                Graph.Station neighbor = entry.getKey();

                if (!visited.contains(neighbor) &&
                        (avoidStations == null || !avoidStations.contains(neighbor.getName()))) {
                    visited.add(neighbor);
                    Route newRoute = new Route(currentRoute);
                    newRoute.addStation(neighbor, entry.getValue());
                    queue.offer(newRoute);
                }
            }
        }

        return null;
    }

    public Route findShortestDistanceRoute(Graph graph, String start, String end,
                                           Set<String> avoidStations, List<String> waypoints,
                                           double lineChangePenalty) {
        if (waypoints != null && !waypoints.isEmpty()) {
            return findRouteWithWaypoints(graph, start, end, waypoints, avoidStations, false);
        }

        Graph.Station startStation = graph.getStation(start);
        Graph.Station endStation = graph.getStation(end);

        if (startStation == null || endStation == null) {
            return null;
        }

        Map<Graph.Station, Double> distances = new HashMap<>();
        Map<Graph.Station, Graph.Station> previous = new HashMap<>();
        Map<Graph.Station, Graph.Edge> previousEdge = new HashMap<>();
        PriorityQueue<NodeDistance> pq = new PriorityQueue<>();

        for (Graph.Station station : graph.getAllStations().values()) {
            distances.put(station, Double.MAX_VALUE);
        }

        distances.put(startStation, 0.0);
        pq.offer(new NodeDistance(startStation, 0.0, -1));

        while (!pq.isEmpty()) {
            NodeDistance current = pq.poll();
            Graph.Station currentStation = current.station;

            if (currentStation.equals(endStation)) {
                break;
            }

            if (current.distance > distances.get(currentStation)) {
                continue;
            }

            for (Map.Entry<Graph.Station, Graph.Edge> entry : currentStation.getConnections().entrySet()) {
                Graph.Station neighbor = entry.getKey();
                Graph.Edge edge = entry.getValue();

                if (avoidStations != null && avoidStations.contains(neighbor.getName())) {
                    continue;
                }

                double edgeWeight = edge.getDistance();

                if (lineChangePenalty > 0 && current.currentLine != -1 &&
                        current.currentLine != edge.getLine()) {
                    edgeWeight += lineChangePenalty;
                }

                double newDistance = distances.get(currentStation) + edgeWeight;

                if (newDistance < distances.get(neighbor)) {
                    distances.put(neighbor, newDistance);
                    previous.put(neighbor, currentStation);
                    previousEdge.put(neighbor, edge);
                    pq.offer(new NodeDistance(neighbor, newDistance, edge.getLine()));
                }
            }
        }

        Route route = new Route();
        List<Graph.Station> path = new ArrayList<>();
        Graph.Station current = endStation;

        while (current != null) {
            path.add(0, current);
            current = previous.get(current);
        }

        if (path.get(0).equals(startStation)) {
            for (int i = 0; i < path.size(); i++) {
                Graph.Station station = path.get(i);
                Graph.Edge edge = (i > 0) ? previousEdge.get(station) : null;
                route.addStation(station, edge);
            }
            return route;
        }

        return null;
    }

    private Route findRouteWithWaypoints(Graph graph, String start, String end,
                                         List<String> waypoints, Set<String> avoidStations,
                                         boolean useBFS) {
        List<String> fullPath = new ArrayList<>();
        fullPath.add(start);
        fullPath.addAll(waypoints);
        fullPath.add(end);

        Route combinedRoute = new Route();

        for (int i = 0; i < fullPath.size() - 1; i++) {
            String from = fullPath.get(i);
            String to = fullPath.get(i + 1);

            Route segment;
            if (useBFS) {
                segment = findShortestRouteBFS(graph, from, to, avoidStations, null);
            } else {
                segment = findShortestDistanceRoute(graph, from, to, avoidStations, null, 0);
            }

            if (segment == null) {
                return null;
            }

            int startIndex = (i == 0) ? 0 : 1;
            for (int j = startIndex; j < segment.getStations().size(); j++) {
                Graph.Edge edge = (j > 0 && j - 1 < segment.getEdges().size()) ?
                        segment.getEdges().get(j - 1) : null;
                combinedRoute.addStation(segment.getStations().get(j), edge);
            }
        }

        return combinedRoute;
    }

    private static class NodeDistance implements Comparable<NodeDistance> {
        Graph.Station station;
        double distance;
        int currentLine;

        NodeDistance(Graph.Station station, double distance, int currentLine) {
            this.station = station;
            this.distance = distance;
            this.currentLine = currentLine;
        }

        @Override
        public int compareTo(NodeDistance other) {
            return Double.compare(this.distance, other.distance);
        }
    }
}