package com.SakuraMochi17.kaisatsumod.command;

import com.SakuraMochi17.kaisatsumod.core.StationRegistry;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

public class CommandResetStations extends CommandBase {

    @Override
    public String getCommandName() {
        return "kaisatsu_reset";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/kaisatsu_reset - すべての駅データを初期化します";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2; // OP権限（チートオン）が必要
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        int count = StationRegistry.registry.size();
        // メモリ上のデータをすべて消去
        StationRegistry.registry.clear();

        sender.addChatMessage(new ChatComponentText("§a[KaisatsuMod] " + count + "件の駅データをすべて初期化しました。"));
        sender.addChatMessage(new ChatComponentText("§7現在設置されている駅ブロックは、ワールドに入り直すかブロックを更新すると再登録されます。"));
    }
}
