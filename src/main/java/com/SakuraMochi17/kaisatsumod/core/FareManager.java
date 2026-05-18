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

    // ★修正：会社クラスに「単価 (costPerBlock)」を移動！
    public static class CompanyConfig {
        public String companyID;
        public String companyName;
        public int costPerBlock; // 会社全体で統一された運賃比率
        public List<LineConfig> lines;

        public CompanyConfig(String companyID, String companyName, int costPerBlock, List<LineConfig> lines) {
            this.companyID = companyID;
            this.companyName = companyName;
            this.costPerBlock = costPerBlock;
            this.lines = lines;
        }
    }

    // 路線ごとの設定（JSONの中身）
    public static class LineConfig {
        public String lineID;
        public String lineName;
        public transient String companyID;   // JSONには書かない裏側の記憶用
        public transient int costPerBlock;   // ★追加：親(会社)から単価を受け継ぐための裏側変数
        public List<String> stations;

        // ★修正：コンストラクタからcostPerBlockを削除
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
            // ★サンプルの生成：会社側に単価（例：JRは5、メトロは3）を設定する
            List<LineConfig> jrLines = Arrays.asList(
                    new LineConfig("line_yamanote", "山手線", Arrays.asList("東京", "品川", "新宿")),
                    new LineConfig("line_tohoku", "東北本線", Arrays.asList("郡山", "福島", "白河"))
            );
            createDefaultJson(linesDir, "jr_east", new CompanyConfig("jr_east", "JR東日本", 5, jrLines));

            List<LineConfig> metroLines = Arrays.asList(
                    new LineConfig("line_ginza", "銀座線", Arrays.asList("渋谷", "表参道", "浅草"))
            );
            createDefaultJson(linesDir, "tokyo_metro", new CompanyConfig("tokyo_metro", "東京メトロ", 3, metroLines));

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
                            // ★重要：親（会社）のデータを、所属するすべての路線に自動でコピーして覚えさせる
                            line.companyID = company.companyID;
                            line.costPerBlock = company.costPerBlock;

                            if (lineConfigs.containsKey(line.lineID)) {
                                System.err.println("[KaisatsuMod] ⚠️警告: 路線ID '" + line.lineID + "' が重複しています！ (" + file.getName() + " のデータで上書きされます)");
                            }
                            lineConfigs.put(line.lineID, line);
                            System.out.println("[KaisatsuMod] 路線を読み込みました: " + line.lineName + " (所属: " + company.companyID + ", 単価: " + line.costPerBlock + "円/B)");
                        }
                    }
                } catch (Exception e) {
                    System.err.println("[KaisatsuMod] ❌エラー: ファイル '" + file.getName() + "' の読み込みに失敗しました！文法ミスを確認してください。");
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

    public static int calculateCrossCompanyFare(String lineID1, String lineID2, int x1, int y1, int z1, int x2, int y2, int z2) {
        LineConfig config1 = lineConfigs.get(lineID1);
        LineConfig config2 = lineConfigs.get(lineID2);
        if (config1 == null || config2 == null) return -1;
        double distance = Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2) + Math.pow(z1 - z2, 2));
        double avgCost = (config1.costPerBlock + config2.costPerBlock) / 2.0;
        int fare = (int) Math.ceil(distance * avgCost);
        return Math.max(fare, 150);
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
