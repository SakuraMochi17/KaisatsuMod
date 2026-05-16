package com.SakuraMochi17.kaisatsumod;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class MessageStationUpdate implements IMessage {
    private int x, y, z;
    private String lineID;
    private String stationName;

    // 必須の空コンストラクタ
    public MessageStationUpdate() {}

    public MessageStationUpdate(int x, int y, int z, String lineID, String stationName) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.lineID = lineID;
        this.stationName = stationName;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.x = buf.readInt();
        this.y = buf.readInt();
        this.z = buf.readInt();
        this.lineID = ByteBufUtils.readUTF8String(buf);
        this.stationName = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.x);
        buf.writeInt(this.y);
        buf.writeInt(this.z);
        ByteBufUtils.writeUTF8String(buf, this.lineID);
        ByteBufUtils.writeUTF8String(buf, this.stationName);
    }

    // サーバー側での受信処理
    public static class Handler implements IMessageHandler<MessageStationUpdate, IMessage> {
        @Override
        public IMessage onMessage(MessageStationUpdate message, MessageContext ctx) {
            World world = ctx.getServerHandler().playerEntity.worldObj;
            TileEntity te = world.getTileEntity(message.x, message.y, message.z);

            if (te instanceof TileEntityStationManager) {
                ((TileEntityStationManager) te).updateStationInfo(message.lineID, message.stationName);
            }
            return null; // 返信パケットは不要なのでnull
        }
    }
}
