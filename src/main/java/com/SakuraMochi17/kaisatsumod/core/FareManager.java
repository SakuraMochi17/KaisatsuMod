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

    // 会社クラス：単価を少数に、さらに「初乗り運賃」を追加！
    public static class CompanyConfig {
        public String companyID;
        public String companyName;
        public double costPerBlock; // ★修正: 小数点以下の運賃を設定可能に (例: 0.15)
        public int baseFare;        // ★追加: 初乗り運賃 (例: 150)
        public List<LineConfig> lines;

        public CompanyConfig(String companyID, String companyName, double costPerBlock, int baseFare, List<LineConfig> lines) {
            this.companyID = companyID;
            this.companyName = companyName;
            this.costPerBlock = costPerBlock;
            this.baseFare = baseFare;
            this.lines = lines;
        }
    }

    // 路線ごとの設定（JSONの中身）
    public static class LineConfig {
        public String lineID;
        public String lineName;
        public transient String companyID;
        public transient double costPerBlock; // ★修正: double型に
        public transient int baseFare;        // ★追加: 親から受け継ぐ初乗り運賃
        public List<String> stations;

        public LineConfig(String id, String name, List<String> stations) {
            this.lineID = id;
            this.lineName = name;
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
            // ★サンプルの生成：現実的な運賃に調整
            // 例: JRは1ブロックあたり0.15円 (1000mで150円加算)、初乗り150円
            List<LineConfig> jrLines = Arrays.asList(
                    new LineConfig("line_yamanote", "山手線", Arrays.asList("東京", "品川", "新宿")),
                    new LineConfig("line_tohoku", "東北本線", Arrays.asList("郡山", "福島", "白河"))
            );
            createDefaultJson(linesDir, "jr_east", new CompanyConfig("jr_east", "JR東日本", 0.15, 150, jrLines));

            // 例: メトロは1ブロックあたり0.12円、初乗り180円
            List<LineConfig> metroLines = Arrays.asList(
                    new LineConfig("line_ginza", "銀座線", Arrays.asList("渋谷", "表参道", "浅草"))
            );
            createDefaultJson(linesDir, "tokyo_metro", new CompanyConfig("tokyo_metro", "東京メトロ", 0.12, 180, metroLines));

            files = linesDir.listFiles((dir, name) -> name.endsWith(".json"));
        }

        lineConfigs.clear();
        if (files != null) {
            Gson gson = new Gson();
            for (File file : files) {
                try (InputStreamReader reader = new InputStreamReader(new FileInputStream(file), "UTF-8")) {
                    CompanyConfig company = gson.fromJson(reader, CompanyConfig.class);

                    if (company != null && company.lines != null) {
                        for (LineConfig line : company.lines) {
                            // ★親（会社）のデータを、所属するすべての路線にコピー
                            line.companyID = company.companyID;
                            line.costPerBlock = company.costPerBlock;
                            line.baseFare = company.baseFare; // 初乗り運賃をコピー

                            if (lineConfigs.containsKey(line.lineID)) {
                                System.err.println("[KaisatsuMod] ⚠️警告: 路線ID '" + line.lineID + "' が重複しています！ (" + file.getName() + " のデータで上書きされます)");
                            }
                            lineConfigs.put(line.lineID, line);
                            System.out.println("[KaisatsuMod] 路線を読み込み: " + line.lineName + " (初乗り: " + line.baseFare + "円, 単価: " + line.costPerBlock + "円/B)");
                        }
                    }
                } catch (Exception e) {
                    System.err.println("[KaisatsuMod] ❌エラー: ファイル '" + file.getName() + "' の読み込みに失敗しました！");
                    e.printStackTrace();
                }
            }
        }
    }

    public static int calculateFare(String lineID, int x1, int y1, int z1, int x2, int y2, int z2) {
        LineConfig config = lineConfigs.get(lineID);
        if (config == null) return -1;

        double distance = Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2) + Math.pow(z1 - z2, 2));
        // ★修正: 運賃 = 距離 × 単価 (1円未満は切り捨て)
        int fare = (int) Math.floor(distance * config.costPerBlock);

        // ★修正: 計算された運賃と初乗り運賃を比較し、高い方を適用
        return Math.max(fare, config.baseFare);
    }

    public static int calculateCrossCompanyFare(String lineID1, String lineID2, int x1, int y1, int z1, int x2, int y2, int z2) {
        LineConfig config1 = lineConfigs.get(lineID1);
        LineConfig config2 = lineConfigs.get(lineID2);
        if (config1 == null || config2 == null) return -1;

        double distance = Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2) + Math.pow(z1 - z2, 2));

        // 直線距離での会社跨ぎ計算なので、単価と初乗り運賃の「平均」を取るか「高い方」を取ります
        // 今回は初乗りは高い方を採用します
        double avgCost = (config1.costPerBlock + config2.costPerBlock) / 2.0;
        int maxBaseFare = Math.max(config1.baseFare, config2.baseFare);

        int fare = (int) Math.floor(distance * avgCost);
        return Math.max(fare, maxBaseFare);
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

    private static void createDefaultJson(File dir, String filename, CompanyConfig defaultConfig) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(new File(dir, filename + ".json")), "UTF-8")) {
            gson.toJson(defaultConfig, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
