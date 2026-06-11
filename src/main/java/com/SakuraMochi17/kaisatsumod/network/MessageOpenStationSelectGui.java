package com.SakuraMochi17.kaisatsumod.network;

import com.SakuraMochi17.kaisatsumod.gui.GuiStationSelect;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

public class MessageOpenStationSelectGui implements IMessage {
    public int x, y, z;
    public String currentStation; // ★追加：現在の設定駅
    public List<String> stations;

    public MessageOpenStationSelectGui() { this.stations = new ArrayList<>(); }

    public MessageOpenStationSelectGui(int x, int y, int z, String currentStation, List<String> stations) {
        this.x = x; this.y = y; this.z = z;
        this.currentStation = currentStation;
        this.stations = stations;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.x = buf.readInt(); this.y = buf.readInt(); this.z = buf.readInt();
        this.currentStation = ByteBufUtils.readUTF8String(buf);
        int size = buf.readInt();
        this.stations = new ArrayList<>();
        for (int i = 0; i < size; i++) this.stations.add(ByteBufUtils.readUTF8String(buf));
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.x); buf.writeInt(this.y); buf.writeInt(this.z);
        ByteBufUtils.writeUTF8String(buf, this.currentStation != null ? this.currentStation : "未設定");
        buf.writeInt(this.stations.size());
        for (String s : this.stations) ByteBufUtils.writeUTF8String(buf, s);
    }

    public static class Handler implements IMessageHandler<MessageOpenStationSelectGui, IMessage> {
        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(MessageOpenStationSelectGui message, MessageContext ctx) {
            Minecraft.getMinecraft().displayGuiScreen(new GuiStationSelect(message.x, message.y, message.z, message.currentStation, message.stations));
            return null;
        }
    }
}
