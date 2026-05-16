package com.SakuraMochi17.kaisatsumod;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class FareManager {

    private static final Map<String, LineConfig> lineConfigs = new HashMap<>();

    // 路線ごとの設定を保持する構造
    public static class LineConfig {
        public String lineID;
        public String lineName;
        public int costPerBlock;      // 1ブロックあたりの運賃（円）
        public List<String> stations; // この路線に所属する駅名一覧（バリデーション用）

        public LineConfig(String id, String name, int cost, List<String> stations) {
            this.lineID = id;
            this.lineName = name;
            this.costPerBlock = cost;
            this.stations = stations;
        }
    }

    // config/kaisatsumod/lines/ 内のすべてのJSONを読み込む
    public static void loadAllLines(File configDir) {
        File linesDir = new File(configDir, "kaisatsumod/lines");
        if (!linesDir.exists()) {
            linesDir.mkdirs();
            // サンプルファイルの生成
            createDefaultJson(linesDir, "yamanote", new LineConfig("line_yamanote", "山手線", 2, Arrays.asList("東京", "品川", "新宿")));
            createDefaultJson(linesDir, "tohoku", new LineConfig("line_tohoku", "東北本線", 5, Arrays.asList("郡山", "福島", "白河")));
        }

        Gson gson = new Gson();
        File[] files = linesDir.listFiles((dir, name) -> name.endsWith(".json"));

        lineConfigs.clear();
        if (files != null) {
            for (File file : files) {
                try (FileReader reader = new FileReader(file)) {
                    LineConfig config = gson.fromJson(reader, LineConfig.class);
                    if (config != null) {
                        lineConfigs.put(config.lineID, config);
                        System.out.println("[KaisatsuMod] 路線ファイルを読み込みました: " + config.lineName);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // 座標の差（三次元の直線距離）から運賃を計算するメソッド
    public static int calculateFare(String lineID, int x1, int y1, int z1, int x2, int y2, int z2) {
        LineConfig config = lineConfigs.get(lineID);
        if (config == null) return -1; // 路線がない

        // 直線距離の計算 (Math.sqrt)
        double distance = Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2) + Math.pow(z1 - z2, 2));

        // 運賃 = 距離 × 1ブロックあたりのコスト (端数は切り上げ)
        int fare = (int) Math.ceil(distance * config.costPerBlock);

        // 初乗り運賃（例: 最低150円）の担保
        return Math.max(fare, 150);
    }

    public static String getLineName(String lineID) {
        return lineConfigs.containsKey(lineID) ? lineConfigs.get(lineID).lineName : "不明な路線";
    }

    // ★新しく追加：登録されている全路線IDのリストをGUI用に取得するメソッド
    public static List<String> getAvailableLines() {
        return new ArrayList<>(lineConfigs.keySet());
    }

    // ★新しく追加：特定の路線に属する駅名のリストをGUI用に取得するメソッド
    public static List<String> getStationsForLine(String lineID) {
        LineConfig config = lineConfigs.get(lineID);
        if (config != null && config.stations != null) {
            return config.stations;
        }
        return new ArrayList<>();
    }

    private static void createDefaultJson(File dir, String filename, LineConfig defaultConfig) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter(new File(dir, filename + ".json"))) {
            gson.toJson(defaultConfig, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
