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
    public String lineID, lineName, companyName;
    public int baseFare;
    public double costPerBlock;
    public List<String> lineStations;   // この路線に登録されている駅
    public List<String> globalStations; // ワールドに存在する全駅

    public MessageOpenLineGui() {
        lineStations = new ArrayList<>();
        globalStations = new ArrayList<>();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.x = buf.readInt(); this.y = buf.readInt(); this.z = buf.readInt();
        this.lineID = ByteBufUtils.readUTF8String(buf);
        this.lineName = ByteBufUtils.readUTF8String(buf);
        this.companyName = ByteBufUtils.readUTF8String(buf);
        this.baseFare = buf.readInt();
        this.costPerBlock = buf.readDouble();

        int lineSize = buf.readInt();
        for (int i = 0; i < lineSize; i++) this.lineStations.add(ByteBufUtils.readUTF8String(buf));

        int globalSize = buf.readInt();
        for (int i = 0; i < globalSize; i++) this.globalStations.add(ByteBufUtils.readUTF8String(buf));
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.x); buf.writeInt(this.y); buf.writeInt(this.z);
        ByteBufUtils.writeUTF8String(buf, this.lineID != null ? this.lineID : "");
        ByteBufUtils.writeUTF8String(buf, this.lineName != null ? this.lineName : "");
        ByteBufUtils.writeUTF8String(buf, this.companyName != null ? this.companyName : "");
        buf.writeInt(this.baseFare);
        buf.writeDouble(this.costPerBlock);

        buf.writeInt(this.lineStations.size());
        for (String s : this.lineStations) ByteBufUtils.writeUTF8String(buf, s);

        buf.writeInt(this.globalStations.size());
        for (String s : this.globalStations) ByteBufUtils.writeUTF8String(buf, s);
    }

    public static class Handler implements IMessageHandler<MessageOpenLineGui, IMessage> {
        @Override
        public IMessage onMessage(MessageOpenLineGui message, MessageContext ctx) {
            if (ctx.side.isClient()) {
                openGui(message);
            }
            return null;
        }

        @SideOnly(Side.CLIENT)
        private void openGui(MessageOpenLineGui message) {
            Minecraft.getMinecraft().displayGuiScreen(new GuiLineManager(message));
        }
    }
}
