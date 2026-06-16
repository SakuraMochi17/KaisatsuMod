package com.SakuraMochi17.kaisatsumod.core;

import com.SakuraMochi17.kaisatsumod.tileentity.TileEntityFareChart;
import net.minecraft.world.World;
import java.util.*;

public class KaisatsuNetworkManager {

    private static class Edge {
        String toStation; String lineID; double distance;
        Edge(String toStation, String lineID, double distance) {
            this.toStation = toStation; this.lineID = lineID; this.distance = distance;
        }
    }

    private static class State implements Comparable<State> {
        String station; String lineID; double cost;
        State(String station, String lineID, double cost) {
            this.station = station; this.lineID = lineID; this.cost = cost;
        }
        @Override
        public int compareTo(State other) { return Double.compare(this.cost, other.cost); }
    }

    public static int calculateFare(World world, String startStation, String endStation) {
        if (world == null || startStation == null || endStation == null) return -1;
        if (startStation.equals(endStation)) return 0;

        KaisatsuNetworkData data = KaisatsuNetworkData.get(world);
        if (data == null || data.companyLines == null || data.globalStations == null) return -1;

        Map<String, List<Edge>> graph = new HashMap<>();

        for (KaisatsuNetworkData.LineData line : data.companyLines.values()) {
            if (line == null || line.stationOrder == null || line.stationOrder.isEmpty()) continue;
            for (int i = 0; i < line.stationOrder.size() - 1; i++) {
                String st1 = line.stationOrder.get(i);
                String st2 = line.stationOrder.get(i + 1);
                if (st1.equals(st2)) continue;

                double dist = getDistance(st1, st2, data);
                if (dist < 0) continue;

                graph.putIfAbsent(st1, new ArrayList<Edge>());
                graph.putIfAbsent(st2, new ArrayList<Edge>());
                graph.get(st1).add(new Edge(st2, line.lineID, dist));
                graph.get(st2).add(new Edge(st1, line.lineID, dist));
            }
        }

        if (!graph.containsKey(startStation) || !graph.containsKey(endStation)) return -1;

        PriorityQueue<State> pq = new PriorityQueue<>();
        Map<String, Double> minCost = new HashMap<>();

        for (KaisatsuNetworkData.LineData line : data.companyLines.values()) {
            if (line.stationOrder.contains(startStation)) {
                pq.add(new State(startStation, line.lineID, line.baseFare));
                minCost.put(startStation + ":" + line.lineID, (double) line.baseFare);
            }
        }

        while (!pq.isEmpty()) {
            State curr = pq.poll();
            if (curr.station.equals(endStation)) return (int) Math.floor(curr.cost);
            if (curr.cost > minCost.getOrDefault(curr.station + ":" + curr.lineID, Double.MAX_VALUE)) continue;

            List<Edge> edges = graph.get(curr.station);
            if (edges == null) continue;

            for (Edge edge : edges) {
                KaisatsuNetworkData.LineData nextLine = data.companyLines.get(edge.lineID);
                if (nextLine == null) continue;
                double nextCost = curr.cost + (edge.distance * nextLine.costPerBlock);
                String stateKey = edge.toStation + ":" + edge.lineID;

                if (nextCost < minCost.getOrDefault(stateKey, Double.MAX_VALUE)) {
                    minCost.put(stateKey, nextCost);
                    pq.add(new State(edge.toStation, edge.lineID, nextCost));
                }
            }
        }
        return -1;
    }

    public static List<TileEntityFareChart.NodeData> buildRouteTree(World world, String startStation, int maxDepth) {
        List<TileEntityFareChart.NodeData> treeData = new ArrayList<>();
        KaisatsuNetworkData data = KaisatsuNetworkData.get(world);

        if (data == null || data.companyLines == null || data.companyLines.isEmpty()) {
            treeData.add(new TileEntityFareChart.NodeData(startStation, 0, "", 0, "", false, false));
            return treeData;
        }

        Queue<String> qName = new LinkedList<>();
        Queue<String> qParent = new LinkedList<>();
        Queue<Integer> qDepth = new LinkedList<>();
        Queue<String> qLine = new LinkedList<>();
        Queue<Boolean> qIsLoop = new LinkedList<>();

        Set<String> visited = new HashSet<>();

        qName.add(startStation); qParent.add(""); qDepth.add(0); qLine.add(""); qIsLoop.add(false);
        visited.add(startStation);

        List<Map.Entry<String, KaisatsuNetworkData.LineData>> sortedLines = new ArrayList<>(data.companyLines.entrySet());
        Collections.sort(sortedLines, new Comparator<Map.Entry<String, KaisatsuNetworkData.LineData>>() {
            @Override
            public int compare(Map.Entry<String, KaisatsuNetworkData.LineData> e1, Map.Entry<String, KaisatsuNetworkData.LineData> e2) {
                return e1.getKey().compareTo(e2.getKey());
            }
        });

        while (!qName.isEmpty()) {
            String current = qName.poll(); String parent = qParent.poll();
            int depth = qDepth.poll(); String lineName = qLine.poll();
            boolean isLoopNode = qIsLoop.poll();

            int fare = (depth == 0) ? 0 : calculateFare(world, startStation, current);
            int adjFare = fare > 0 ? (int) Math.ceil(fare / 10.0) * 10 : (depth == 0 ? 0 : -1);

            // ★修正：isCutoffをデフォルトでfalseとして生成
            treeData.add(new TileEntityFareChart.NodeData(current, adjFare, parent, depth, lineName, isLoopNode, false));

            if (depth < maxDepth) {
                for (Map.Entry<String, KaisatsuNetworkData.LineData> entry : sortedLines) {
                    String lineId = entry.getKey();
                    KaisatsuNetworkData.LineData line = entry.getValue();

                    if (line.stationOrder != null && line.stationOrder.contains(current)) {
                        int idx = line.stationOrder.indexOf(current);
                        int size = line.stationOrder.size();
                        boolean isLoop = size > 1 && line.stationOrder.get(0).equals(line.stationOrder.get(size - 1));
                        int uniqueSize = isLoop ? size - 1 : size;

                        if (isLoop) {
                            String fwd = line.stationOrder.get((idx + 1) % uniqueSize);
                            String bck = line.stationOrder.get((idx - 1 + uniqueSize) % uniqueSize);

                            if (!visited.contains(fwd)) {
                                visited.add(fwd); qName.add(fwd); qParent.add(current); qDepth.add(depth + 1); qLine.add(lineId + "_F"); qIsLoop.add(true);
                            }
                            if (!visited.contains(bck)) {
                                visited.add(bck); qName.add(bck); qParent.add(current); qDepth.add(depth + 1); qLine.add(lineId + "_B"); qIsLoop.add(true);
                            }
                        } else {
                            if (idx < size - 1) {
                                String fwd = line.stationOrder.get(idx + 1);
                                if (!visited.contains(fwd)) {
                                    visited.add(fwd); qName.add(fwd); qParent.add(current); qDepth.add(depth + 1); qLine.add(lineId + "_F"); qIsLoop.add(false);
                                }
                            }
                            if (idx > 0) {
                                String bck = line.stationOrder.get(idx - 1);
                                if (!visited.contains(bck)) {
                                    visited.add(bck); qName.add(bck); qParent.add(current); qDepth.add(depth + 1); qLine.add(lineId + "_B"); qIsLoop.add(false);
                                }
                            }
                        }
                    }
                }
            }
        }
        return treeData;
    }


    private static double getDistance(String st1, String st2, KaisatsuNetworkData data) {
        KaisatsuNetworkData.StationCoords c1 = data.globalStations.get(st1);
        KaisatsuNetworkData.StationCoords c2 = data.globalStations.get(st2);
        if (c1 != null && c2 != null) {
            double dx = c1.x - c2.x; double dy = c1.y - c2.y; double dz = c1.z - c2.z;
            return Math.sqrt(dx * dx + dy * dy + dz * dz);
        }
        return -1.0;
    }
}
