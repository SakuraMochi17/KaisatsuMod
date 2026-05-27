package com.SakuraMochi17.kaisatsumod.core;

import java.util.HashMap;
import java.util.Map;

public class StationRegistry {

    public static final Map<String, StationData> registry = new HashMap<>();

    public static class StationData {
        public String lineID;
        public String stationName;
        public int x, y, z;
        public String nextStation1; // ★追加: 隣接駅1
        public String nextStation2; // ★追加: 隣接駅2

        public StationData(String lineID, String name, int x, int y, int z, String next1, String next2) {
            this.lineID = lineID;
            this.stationName = name;
            this.x = x;
            this.y = y;
            this.z = z;
            this.nextStation1 = next1;
            this.nextStation2 = next2;
        }
    }

    // ★修正: 隣接駅を受け取るように引数を追加
    public static void registerStation(int dimID, int x, int y, int z, String lineID, String name, String next1, String next2) {
        String key = dimID + ":" + x + ":" + y + ":" + z;
        registry.put(key, new StationData(lineID, name, x, y, z, next1, next2));
    }

    public static void removeStation(int dimID, int x, int y, int z) {
        registry.remove(dimID + ":" + x + ":" + y + ":" + z);
    }

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
        return nearest;
    }

    public static StationData getStationByName(String lineID, String name) {
        for (StationData data : registry.values()) {
            if (data.lineID.equals(lineID) && data.stationName.equals(name)) {
                return data;
            }
        }
        return null;
    }
}
