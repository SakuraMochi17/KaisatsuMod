package com.SakuraMochi17.kaisatsumod.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.*;

public class FareManager {

    private static final Map<String, LineConfig> lineConfigs = new HashMap<>();

    public static class LineConfig {
        public String lineID;
        public String lineName;
        public String companyID;
        public int costPerBlock;
        public List<String> stations;

        public LineConfig(String id, String name, String companyID, int cost, List<String> stations) {
            this.lineID = id;
            this.lineName = name;
            this.companyID = companyID;
            this.costPerBlock = cost;
            this.stations = stations;
        }
    }

    public static void loadAllLines(File configDir) {
        File linesDir = new File(configDir, "kaisatsumod/lines");
        if (!linesDir.exists()) {
            linesDir.mkdirs();
        }

        File[] files = linesDir.listFiles((dir, name) -> name.endsWith(".json"));

        if (files == null || files.length == 0) {
            createDefaultJson(linesDir, "yamanote", new LineConfig("line_yamanote", "山手線", "jr_east", 2, Arrays.asList("東京", "品川", "新宿")));
            createDefaultJson(linesDir, "tohoku", new LineConfig("line_tohoku", "東北本線", "jr_east", 5, Arrays.asList("郡山", "福島", "白河")));
            files = linesDir.listFiles((dir, name) -> name.endsWith(".json"));
        }

        lineConfigs.clear();
        if (files != null) {
            Gson gson = new Gson();
            for (File file : files) {
                // ★修正1: Windows環境等の文字化けエラーを防ぐため、UTF-8で強制読み込みする
                try (InputStreamReader reader = new InputStreamReader(new FileInputStream(file), "UTF-8")) {
                    LineConfig config = gson.fromJson(reader, LineConfig.class);
                    if (config != null) {
                        // ★修正2: lineIDの重複チェック（被っていたらログに警告を出す）
                        if (lineConfigs.containsKey(config.lineID)) {
                            System.err.println("[KaisatsuMod] ⚠️警告: 路線ID '" + config.lineID + "' が重複しています！ (" + file.getName() + " のデータで上書きされます)");
                        }
                        lineConfigs.put(config.lineID, config);
                        System.out.println("[KaisatsuMod] 路線ファイルを読み込みました: " + config.lineName + " (会社: " + config.companyID + ")");
                    }
                } catch (Exception e) {
                    // ★修正3: JSONの書き間違いがあってもクラッシュさせず、エラー原因をログに出して次のファイルへ進む
                    System.err.println("[KaisatsuMod] ❌エラー: ファイル '" + file.getName() + "' の読み込みに失敗しました！カンマの付け忘れや「\"」の抜けなどの文法ミスがないか確認してください。");
                    e.printStackTrace();
                }
            }
        }
    }

    public static int calculateFare(String lineID, int x1, int y1, int z1, int x2, int y2, int z2) {
        LineConfig config = lineConfigs.get(lineID);
        if (config == null) return -1;
        double distance = Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2) + Math.pow(z1 - z2, 2));
        int fare = (int) Math.ceil(distance * config.costPerBlock);
        return Math.max(fare, 150);
    }
    // ▼ 既存の calculateFare メソッドの下にこれを追加します ▼
    public static int calculateCrossCompanyFare(String lineID1, String lineID2, int x1, int y1, int z1, int x2, int y2, int z2) {
        LineConfig config1 = lineConfigs.get(lineID1);
        LineConfig config2 = lineConfigs.get(lineID2);
        if (config1 == null || config2 == null) return -1;

        double distance = Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2) + Math.pow(z1 - z2, 2));

        // ★ 2つの路線の単価を足して2で割る（平均化）
        double avgCost = (config1.costPerBlock + config2.costPerBlock) / 2.0;
        int fare = (int) Math.ceil(distance * avgCost);

        return Math.max(fare, 150); // 初乗り150円は担保
    }

    public static String getLineName(String lineID) {
        return lineConfigs.containsKey(lineID) ? lineConfigs.get(lineID).lineName : "不明な路線";
    }

    public static String getCompanyID(String lineID) {
        if (!lineConfigs.containsKey(lineID)) return "unknown";
        String company = lineConfigs.get(lineID).companyID;
        return company != null ? company : "unknown";
    }

    public static List<String> getAvailableLines() {
        return new ArrayList<>(lineConfigs.keySet());
    }

    public static List<String> getStationsForLine(String lineID) {
        LineConfig config = lineConfigs.get(lineID);
        if (config != null && config.stations != null) {
            return config.stations;
        }
        return new ArrayList<>();
    }

    private static void createDefaultJson(File dir, String filename, LineConfig defaultConfig) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        // ★修正4: サンプル出力時もUTF-8を強制し、後から編集しやすくする
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(new File(dir, filename + ".json")), "UTF-8")) {
            gson.toJson(defaultConfig, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
