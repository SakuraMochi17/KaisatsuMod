package com.SakuraMochi17.kaisatsumod.core;

import java.util.HashMap;
import java.util.Map;

public class StationRegistry {

    // キー: "ディメンションID:x:y:z", 値: 駅データ
    public static final Map<String, StationData> registry = new HashMap<>();

    public static class StationData {
        public String lineID;
        public String stationName;
        public int x, y, z;

        public StationData(String lineID, String name, int x, int y, int z) {
            this.lineID = lineID;
            this.stationName = name;
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    public static void registerStation(int dimID, int x, int y, int z, String lineID, String name) {
        String key = dimID + ":" + x + ":" + y + ":" + z;
        registry.put(key, new StationData(lineID, name, x, y, z));
    }

    public static void removeStation(int dimID, int x, int y, int z) {
        registry.remove(dimID + ":" + x + ":" + y + ":" + z);
    }

    // 改札機や券売機の座標から、最も近い（半径20ブロック以内などの）駅を検索する
    public static StationData findNearestStation(int dimID, int srcX, int srcY, int srcZ, double maxRange) {
        StationData nearest = null;
        double minDistance = maxRange;

        for (String key : registry.keySet()) {
            if (!key.startsWith(dimID + ":")) continue;

            StationData data = registry.get(key);
            double dist = Math.sqrt(Math.pow(srcX - data.x, 2) + Math.pow(srcY - data.y, 2) + Math.pow(srcZ - data.z, 2));

            if (dist < minDistance) {
                minDistance = dist;
                nearest = data;
            }
        }
        return nearest; // 見つからなければnull
    }
    // これを StationRegistry クラスの中（findNearestStation の下あたり）に追記します
    public static StationData getStationByName(String lineID, String name) {
        for (StationData data : registry.values()) {
            if (data.lineID.equals(lineID) && data.stationName.equals(name)) {
                return data;
            }
        }
        return null; // 見つからなかった場合
    }
}
