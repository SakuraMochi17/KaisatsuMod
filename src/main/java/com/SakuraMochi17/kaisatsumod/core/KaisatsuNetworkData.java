package com.SakuraMochi17.kaisatsumod.core;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraftforge.common.util.Constants;

import java.util.*;

public class KaisatsuNetworkData extends WorldSavedData {
    private static final String DATA_NAME = "KaisatsuNetworkData";

    // 1. 全世界の駅名 ⇄ 座標のマップ (駅管理ブロックが登録)
    public final Map<String, StationCoords> globalStations = new HashMap<>();

    // 2. 路線ID ⇄ 路線データのマップ (路線管理ブロックが登録)
    public final Map<String, LineData> companyLines = new HashMap<>();

    // 座標保持用の内部クラス
    public static class StationCoords {
        public final int x, y, z;
        public StationCoords(int x, int y, int z) {
            this.x = x; this.y = y; this.z = z;
        }
    }

    // 路線データ保持用の内部クラス
    // KaisatsuNetworkData.java 内の LineData クラスを修正
    // 路線データ保持用の内部クラス
    public static class LineData {
        public String lineID;
        public String lineName;
        public String companyName;
        public int baseFare;
        public double costPerBlock;
        public boolean isExpress;
        public int expressFare;
        public final List<String> stationOrder = new ArrayList<>();

        // ==========================================
        // ① 追加: 古いコードからの呼び出しに対応するための5引数コンストラクタ（特急なし扱い）
        // ==========================================
        public LineData(String lineID, String lineName, String companyName, int baseFare, double costPerBlock) {
            this(lineID, lineName, companyName, baseFare, costPerBlock, false, 0); // 特急フラグをfalse、料金を0としてメインコンストラクタへ渡す
        }

        // ==========================================
        // ② 既存: 特急機能に対応した7引数のメインコンストラクタ
        // ==========================================
        public LineData(String lineID, String lineName, String companyName, int baseFare, double costPerBlock, boolean isExpress, int expressFare) {
            this.lineID = lineID;
            this.lineName = lineName;
            this.companyName = companyName;
            this.baseFare = baseFare;
            this.costPerBlock = costPerBlock;
            this.isExpress = isExpress;
            this.expressFare = expressFare;
        }
    }



    public KaisatsuNetworkData(String name) {
        super(name);
    }

    // ワールドからデータを取得するための静的メソッド
    public static KaisatsuNetworkData get(World world) {
        if (world == null || world.isRemote) return null;

        KaisatsuNetworkData instance = (KaisatsuNetworkData) world.loadItemData(KaisatsuNetworkData.class, DATA_NAME);
        if (instance == null) {
            instance = new KaisatsuNetworkData(DATA_NAME);
            world.setItemData(DATA_NAME, instance);
        }
        return instance;
    }

    // ==========================================
    // 💾 セーブデータの読み込み (Load)
    // ==========================================
    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        // ① 駅座標データの読み込み
        globalStations.clear();
        NBTTagList stationList = nbt.getTagList("GlobalStations", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < stationList.tagCount(); i++) {
            NBTTagCompound sNbt = stationList.getCompoundTagAt(i);
            String name = sNbt.getString("Name");
            int x = sNbt.getInteger("X");
            int y = sNbt.getInteger("Y");
            int z = sNbt.getInteger("Z");
            globalStations.put(name, new StationCoords(x, y, z));
        }

        // ② 路線データの読み込み
        companyLines.clear();
        NBTTagList lineList = nbt.getTagList("CompanyLines", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < lineList.tagCount(); i++) {
            NBTTagCompound lNbt = lineList.getCompoundTagAt(i);
            String id = lNbt.getString("LineID");
            String name = lNbt.getString("LineName");
            String comp = lNbt.getString("CompanyName");
            int base = lNbt.getInteger("BaseFare");
            double cost = lNbt.getDouble("CostPerBlock");

            // readFromNBT メソッド内の路線読み込み部分に追加:
            boolean isExpress = lNbt.getBoolean("IsExpress");
            int expressFare = lNbt.getInteger("ExpressFare");
            LineData line = new LineData(id, name, comp, base, cost, isExpress, expressFare);

            // 駅順リストの読み込み
            NBTTagList orderList = lNbt.getTagList("StationOrder", Constants.NBT.TAG_STRING);
            for (int j = 0; j < orderList.tagCount(); j++) {
                line.stationOrder.add(orderList.getStringTagAt(j));
            }
            companyLines.put(id, line);
        }


    }

    // ==========================================
    // 💾 セーブデータの書き込み (Save)
    // ==========================================
    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        // ① 駅座標データの保存
        NBTTagList stationList = new NBTTagList();
        for (Map.Entry<String, StationCoords> entry : globalStations.entrySet()) {
            NBTTagCompound sNbt = new NBTTagCompound();
            sNbt.setString("Name", entry.getKey());
            sNbt.setInteger("X", entry.getValue().x);
            sNbt.setInteger("Y", entry.getValue().y);
            sNbt.setInteger("Z", entry.getValue().z);
            stationList.appendTag(sNbt);
        }
        nbt.setTag("GlobalStations", stationList);



        // ② 路線データの保存
        NBTTagList lineList = new NBTTagList();
        for (LineData line : companyLines.values()) {
            NBTTagCompound lNbt = new NBTTagCompound();
            lNbt.setString("LineID", line.lineID);
            lNbt.setString("LineName", line.lineName);
            lNbt.setString("CompanyName", line.companyName);
            lNbt.setInteger("BaseFare", line.baseFare);
            lNbt.setDouble("CostPerBlock", line.costPerBlock);

            // 駅順リストの保存
            NBTTagList orderList = new NBTTagList();
            for (String stName : line.stationOrder) {
                orderList.appendTag(new NBTTagString(stName));
            }
            lNbt.setTag("StationOrder", orderList);
            // writeToNBT メソッド内の路線保存部分に追加:
            lNbt.setBoolean("IsExpress", line.isExpress);
            lNbt.setInteger("ExpressFare", line.expressFare);
            lineList.appendTag(lNbt);
        }
        nbt.setTag("CompanyLines", lineList);

    }
}
