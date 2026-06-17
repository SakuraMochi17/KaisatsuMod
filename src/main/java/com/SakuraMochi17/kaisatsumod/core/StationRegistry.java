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

}
