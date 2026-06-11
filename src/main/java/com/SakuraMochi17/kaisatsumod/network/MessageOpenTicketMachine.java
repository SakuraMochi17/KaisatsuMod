package com.SakuraMochi17.kaisatsumod.network;

import com.SakuraMochi17.kaisatsumod.tileentity.TileEntityTicketMachine;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class MessageOpenTicketMachine implements IMessage {
    public int x, y, z;
    public String stationName;
    public List<Integer> availableFares;

    // ★追加：サーバーから送られてきた運賃リストを一時的に記憶しておく変数
    public static List<Integer> latestFares = new ArrayList<>();

    public MessageOpenTicketMachine() {
        this.availableFares = new ArrayList<>();
    }

    public MessageOpenTicketMachine(int x, int y,int z, String stationName, List<Integer> availableFares) {
        this.x = x; this.y = y; this.z = z;
        this.stationName = stationName;
        this.availableFares = availableFares;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.x = buf.readInt(); this.y = buf.readInt(); this.z = buf.readInt();
        this.stationName = ByteBufUtils.readUTF8String(buf);

        int size = buf.readInt();
        this.availableFares = new ArrayList<>();
        for (int i = 0; i < size; i++) this.availableFares.add(buf.readInt());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.x); buf.writeInt(this.y); buf.writeInt(this.z);
        ByteBufUtils.writeUTF8String(buf, this.stationName != null ? this.stationName : "未設定");

        buf.writeInt(this.availableFares.size());
        for (int fare : this.availableFares) buf.writeInt(fare);
    }

    public static class Handler implements IMessageHandler<MessageOpenTicketMachine, IMessage> {
        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(MessageOpenTicketMachine message, MessageContext ctx) {
            // ★修正：無理やり画面を開くのをやめ、リストの保存だけを行う
            MessageOpenTicketMachine.latestFares = message.availableFares;

            Minecraft mc = Minecraft.getMinecraft();
            World world = mc.theWorld;
            TileEntity te = world.getTileEntity(message.x, message.y, message.z);
            if (te instanceof TileEntityTicketMachine) {
                ((TileEntityTicketMachine) te).stationName = message.stationName;
            }
            return null;
        }
    }
}