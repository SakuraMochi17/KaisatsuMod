package com.SakuraMochi17.kaisatsumod.command;

import com.SakuraMochi17.kaisatsumod.core.StationRegistry;
import com.SakuraMochi17.kaisatsumod.core.KaisatsuNetworkData;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

public class CommandResetStations extends CommandBase {

    @Override
    public String getCommandName() {
        return "kaisatsu_reset";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/kaisatsu_reset - すべての駅・路線データを初期化します";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2; // OP権限
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        // 1. メモリ上の駅データを消去
        int count = StationRegistry.registry.size();
        StationRegistry.registry.clear();

        // 2. セーブデータ上の路線データを完全消去
        World world = sender.getEntityWorld();
        KaisatsuNetworkData data = KaisatsuNetworkData.get(world);
        int lineCount = 0;
        if (data != null) {
            lineCount = data.companyLines.size();
            data.companyLines.clear();
            data.markDirty();
        }

        sender.addChatMessage(new ChatComponentText("§a[KaisatsuMod] " + count + "件の駅データと、" + lineCount + "件の路線データを【完全消去】しました。"));
        sender.addChatMessage(new ChatComponentText("§7※路線管理ブロックの「設定を保存して終了」を押し直して、路線を再登録してください。"));
    }
}
