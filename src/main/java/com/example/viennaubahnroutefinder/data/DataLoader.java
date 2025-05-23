package com.example.viennaubahnroutefinder.data;

import com.example.viennaubahnroutefinder.model.Graph;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class DataLoader {

    public static com.example.viennaubahnroutefinder.model.Graph loadGraph() {
        com.example.viennaubahnroutefinder.model.Graph graph = new Graph();

        try (InputStream is = DataLoader.class.getResourceAsStream("/vienna_subway.csv");
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

            String line = reader.readLine();

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    String start = parts[0].trim();
                    String stop = parts[1].trim();
                    int lineNumber = Integer.parseInt(parts[2].trim());
                    String color = parts[3].trim();

                    graph.addStation(start);
                    graph.addStation(stop);
                    graph.addConnection(start, stop, lineNumber, color);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return graph;
    }
}