package com.SakuraMochi17.kaisatsumod.core;

import net.minecraft.world.World;
import java.util.*;

public class FareManager {

    // --- グラフ探索用の内部クラス ---
    private static class Edge {
        String toStation;
        String lineID;
        double distance;

        Edge(String toStation, String lineID, double distance) {
            this.toStation = toStation;
            this.lineID = lineID;
            this.distance = distance;
        }
    }

    private static class State implements Comparable<State> {
        String station;
        String lineID;
        double cost;

        State(String station, String lineID, double cost) {
            this.station = station;
            this.lineID = lineID;
            this.cost = cost;
        }

        // コスト（運賃）が安い順に探索するための比較ルール
        @Override
        public int compareTo(State other) {
            return Double.compare(this.cost, other.cost);
        }
    }

    /**
     * 新世代運賃計算エンジン（ダイクストラ法・全路線対応）
     */
    public static int calculateFare(World world, String startStation, String endStation) {
        if (world == null || startStation == null || endStation == null) return -1;
        if (startStation.equals(endStation)) return 0;

        KaisatsuNetworkData data = KaisatsuNetworkData.get(world);
        if (data == null || data.companyLines == null || data.globalStations == null) return -1;

        // =======================================================
        // 1. 路線データから「駅の繋がり（グラフ）」を自動構築する
        // =======================================================
        Map<String, List<Edge>> graph = new HashMap<>();

        for (KaisatsuNetworkData.LineData line : data.companyLines.values()) {
            if (line == null || line.stationOrder == null || line.stationOrder.isEmpty()) continue;

            // 路線に登録されている駅を順番に繋いで「線路（Edge）」を作る
            for (int i = 0; i < line.stationOrder.size() - 1; i++) {
                String st1 = line.stationOrder.get(i);
                String st2 = line.stationOrder.get(i + 1);
                if (st1.equals(st2)) continue;

                double dist = getDistance(st1, st2, data);
                if (dist < 0) continue; // 座標が未登録の駅は無視

                graph.putIfAbsent(st1, new ArrayList<Edge>());
                graph.putIfAbsent(st2, new ArrayList<Edge>());

                // 双方向に移動可能としてエッジ（線路）を登録
                graph.get(st1).add(new Edge(st2, line.lineID, dist));
                graph.get(st2).add(new Edge(st1, line.lineID, dist));
            }
        }

        // 始点または終点がグラフ内に存在しない場合は経路なし(-1)
        if (!graph.containsKey(startStation) || !graph.containsKey(endStation)) {
            return -1;
        }

        // =======================================================
        // 2. ダイクストラ法による「最安運賃ルート」の探索
        // =======================================================
        PriorityQueue<State> pq = new PriorityQueue<>();
        Map<String, Double> minCost = new HashMap<>();

        // 乗車駅を含む「すべての路線」を初期状態としてセット（初乗り運賃がそれぞれかかる）
        for (KaisatsuNetworkData.LineData line : data.companyLines.values()) {
            if (line.stationOrder.contains(startStation)) {
                pq.add(new State(startStation, line.lineID, line.baseFare));
                minCost.put(startStation + ":" + line.lineID, (double) line.baseFare);
            }
        }

        while (!pq.isEmpty()) {
            State curr = pq.poll();

            // 目的地に到着した瞬間、それが「最安値」であることが保証される！
            if (curr.station.equals(endStation)) {
                return (int) Math.floor(curr.cost);
            }

            // すでにこれより安いルートが見つかっている場合は無視（計算の軽量化）
            if (curr.cost > minCost.getOrDefault(curr.station + ":" + curr.lineID, Double.MAX_VALUE)) {
                continue;
            }

            List<Edge> edges = graph.get(curr.station);
            if (edges == null) continue;

            // 今いる駅から繋がっている「すべての隣駅」を探索
            for (Edge edge : edges) {
                KaisatsuNetworkData.LineData nextLine = data.companyLines.get(edge.lineID);
                if (nextLine == null) continue;

                // 基本運賃：現在のコスト ＋ (駅間の距離 × その路線の1Bあたりの単価)
                double nextCost = curr.cost + (edge.distance * nextLine.costPerBlock);

                // ★乗り換えペナルティ：もし今乗っている路線と違う路線に移動する場合は「初乗り運賃」を追加！
                // ★修正：乗り換え時のB線・C線の初乗り運賃の二重取りを廃止！
                // 純粋に距離分の運賃（costPerBlock）だけが加算されていきます。
                /*
                if (!edge.lineID.equals(curr.lineID)) {
                    nextCost += nextLine.baseFare;
                }
                */

                String stateKey = edge.toStation + ":" + edge.lineID;

                // 今までのルートより安ければ更新してキューに入れる
                if (nextCost < minCost.getOrDefault(stateKey, Double.MAX_VALUE)) {
                    minCost.put(stateKey, nextCost);
                    pq.add(new State(edge.toStation, edge.lineID, nextCost));
                }
            }
        }

        // 全ての経路を探しても辿り着けなかった＝どこからも直通していない
        return -1;
    }

    // 3D直線距離を求めるヘルパーメソッド
    private static double getDistance(String st1, String st2, KaisatsuNetworkData data) {
        KaisatsuNetworkData.StationCoords c1 = data.globalStations.get(st1);
        KaisatsuNetworkData.StationCoords c2 = data.globalStations.get(st2);

        if (c1 != null && c2 != null) {
            double dx = c1.x - c2.x;
            double dy = c1.y - c2.y;
            double dz = c1.z - c2.z;
            return Math.sqrt(dx * dx + dy * dy + dz * dz);
        }
        return -1.0;
    }

    // レガシー対応（エラー回避用・中身は空でOK）
    public static void loadAllLines(java.io.File configDir) {}
}