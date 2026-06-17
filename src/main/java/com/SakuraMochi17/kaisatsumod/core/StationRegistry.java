// src/main/java/com/SakuraMochi17/kaisatsumod/core/StationRegistry.java
package com.SakuraMochi17.kaisatsumod.core;

import java.util.HashMap;
import java.util.Map;

public class StationRegistry {

    public static final Map<String, StationData> registry = new HashMap<>();

    public static class StationData {
        public String lineID;
        public String stationName;
        public int x, y, z;
        // ★削除: nextStation1, nextStation2 は KaisatsuNetworkData に一本化したため廃止

        public StationData(String lineID, String name, int x, int y, int z) {
            this.lineID = lineID;
            this.stationName = name;
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    // ★修正: 隣接駅の引数を削除
    public static void registerStation(int dimID, int x, int y, int z, String lineID, String name) {
        String key = dimID + ":" + x + ":" + y + ":" + z;
        registry.put(key, new StationData(lineID, name, x, y, z));
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
