package com.SakuraMochi17.kaisatsumod.network;

import com.SakuraMochi17.kaisatsumod.gui.GuiLineManager;
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

public class MessageOpenLineGui implements IMessage {
    public int x, y, z;
    public String companyName;
    public List<String> globalStations;

    // クライアント(画面)で扱うための路線情報クラス
    public static class LineInfo {
        public String lineID, lineName;
        public int baseFare;
        public double costPerBlock;
        public List<String> stations = new ArrayList<>();
    }
    public List<LineInfo> companyLines;

    public MessageOpenLineGui() {
        globalStations = new ArrayList<>();
        companyLines = new ArrayList<>();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.x = buf.readInt(); this.y = buf.readInt(); this.z = buf.readInt();
        this.companyName = ByteBufUtils.readUTF8String(buf);

        int globalSize = buf.readInt();
        for (int i = 0; i < globalSize; i++) this.globalStations.add(ByteBufUtils.readUTF8String(buf));

        int lineCount = buf.readInt();
        for (int i = 0; i < lineCount; i++) {
            LineInfo info = new LineInfo();
            info.lineID = ByteBufUtils.readUTF8String(buf);
            info.lineName = ByteBufUtils.readUTF8String(buf);
            info.baseFare = buf.readInt();
            info.costPerBlock = buf.readDouble();
            int stCount = buf.readInt();
            for (int j = 0; j < stCount; j++) info.stations.add(ByteBufUtils.readUTF8String(buf));
            this.companyLines.add(info);
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.x); buf.writeInt(this.y); buf.writeInt(this.z);
        ByteBufUtils.writeUTF8String(buf, this.companyName != null ? this.companyName : "");

        buf.writeInt(this.globalStations.size());
        for (String s : this.globalStations) ByteBufUtils.writeUTF8String(buf, s);

        buf.writeInt(this.companyLines.size());
        for (LineInfo info : this.companyLines) {
            ByteBufUtils.writeUTF8String(buf, info.lineID != null ? info.lineID : "");
            ByteBufUtils.writeUTF8String(buf, info.lineName != null ? info.lineName : "");
            buf.writeInt(info.baseFare);
            buf.writeDouble(info.costPerBlock);
            buf.writeInt(info.stations.size());
            for (String s : info.stations) ByteBufUtils.writeUTF8String(buf, s);
        }
    }

    public static class Handler implements IMessageHandler<MessageOpenLineGui, IMessage> {
        @Override
        public IMessage onMessage(MessageOpenLineGui message, MessageContext ctx) {
            if (ctx.side.isClient()) openGui(message);
            return null;
        }

        @SideOnly(Side.CLIENT)
        private void openGui(MessageOpenLineGui message) {
            Minecraft.getMinecraft().displayGuiScreen(new GuiLineManager(message));
        }
    }
}
