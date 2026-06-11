package com.SakuraMochi17.kaisatsumod.gui;

import com.SakuraMochi17.kaisatsumod.KaisatsuModMain;
import com.SakuraMochi17.kaisatsumod.item.ItemCertificate;
import com.SakuraMochi17.kaisatsumod.item.ItemICCard;
import com.SakuraMochi17.kaisatsumod.item.ItemMagicICCard;
import com.SakuraMochi17.kaisatsumod.network.MessageStaffTerminalAdjust;
import com.SakuraMochi17.kaisatsumod.tileentity.TileEntityStaffTerminal;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class GuiStaffTerminal extends GuiContainer {
    private static final ResourceLocation texture = new ResourceLocation("textures/gui/container/generic_54.png");
    private TileEntityStaffTerminal terminal;
    private int targetX, targetY, targetZ;

    public GuiStaffTerminal(InventoryPlayer playerInv, TileEntityStaffTerminal te) {
        super(new ContainerStaffTerminal(playerInv, te));
        this.terminal = te;
        this.targetX = te.xCoord; this.targetY = te.yCoord; this.targetZ = te.zCoord;
        this.xSize = 176; this.ySize = 222;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void initGui() {
        super.initGui();
        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;
        this.buttonList.add(new GuiButton(0, x + 42, y + 105, 90, 20, "精算して出場する"));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 0) {
            KaisatsuModMain.network.sendToServer(new MessageStaffTerminalAdjust(targetX, targetY, targetZ));
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String title = "窓口精算機 - " + (terminal.stationName != null ? terminal.stationName : "未設定");
        this.fontRendererObj.drawString(title, 8, 5, 4210752);

        this.fontRendererObj.drawString("対象", 18, 15, 4210752);
        this.fontRendererObj.drawString("支払IC", 15, 65, 4210752);
        this.fontRendererObj.drawString("現金投入", 115, 15, 4210752);
        this.fontRendererObj.drawString("釣銭", 115, 65, 4210752);

        int cashTotal = 0;
        for (int i = 2; i <= 5; i++) {
            ItemStack stack = terminal.getStackInSlot(i);
            if (stack != null) cashTotal += KaisatsuModMain.getMoneyValue(stack) * stack.stackSize;
        }

        // ★修正：魔法のICカードを判定する
        int icBalance = 0;
        boolean isMagic = false;
        ItemStack icStack = terminal.getStackInSlot(1);
        if (icStack != null) {
            if (icStack.getItem() instanceof ItemMagicICCard) {
                isMagic = true;
            } else if (icStack.getItem() instanceof ItemICCard && icStack.stackTagCompound != null) {
                icBalance = icStack.stackTagCompound.getInteger("balance");
            }
        }

        ItemStack targetItem = terminal.getStackInSlot(0);
        if (targetItem != null && targetItem.getItem() instanceof ItemCertificate && targetItem.stackTagCompound != null) {
            String entryStation = targetItem.stackTagCompound.getString("issueStation");
            int fare = ((ContainerStaffTerminal) this.inventorySlots).clientFare;

            if (fare == -1) {
                this.fontRendererObj.drawString("乗車: " + entryStation, 42, 35, 0x000000);
                this.fontRendererObj.drawString("運賃: 経路エラー", 42, 50, 0xFF0000);
                this.fontRendererObj.drawString("※精算不可", 42, 65, 0xFF0000);
            } else {
                this.fontRendererObj.drawString("乗車: " + entryStation, 42, 35, 0x000000);
                this.fontRendererObj.drawString("運賃: " + fare + "円", 42, 50, 0x000000);

                // ★追加：魔法のカードなら無条件で決済可にする
                if (isMagic) {
                    this.fontRendererObj.drawString("魔法パス適用中 (決済可)", 42, 70, 0x0000FF);
                } else {
                    int totalAvailable = cashTotal + icBalance;
                    int shortage = fare - totalAvailable;

                    if (shortage > 0) {
                        this.fontRendererObj.drawString("不足: " + shortage + "円", 42, 70, 0xFF0000);
                    } else {
                        this.fontRendererObj.drawString("不足: 0円", 42, 70, 0x0000FF);
                    }
                }
            }
        } else if (targetItem != null) {
            this.fontRendererObj.drawString("※証明書専用です", 42, 50, 0xFF0000);
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(texture);
        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;

        this.drawTexturedModalRect(x, y, 0, 0, this.xSize, this.ySize);
        net.minecraft.client.gui.Gui.drawRect(x + 7, y + 13, x + 169, y + 128, 0xFFC6C6C6);

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(texture);

        this.drawTexturedModalRect(x + 19, y + 24, 7, 17, 18, 18);
        this.drawTexturedModalRect(x + 19, y + 74, 7, 17, 18, 18);

        this.drawTexturedModalRect(x + 114, y + 24, 7, 17, 18, 18);
        this.drawTexturedModalRect(x + 134, y + 24, 7, 17, 18, 18);
        this.drawTexturedModalRect(x + 114, y + 44, 7, 17, 18, 18);
        this.drawTexturedModalRect(x + 134, y + 44, 7, 17, 18, 18);

        this.drawTexturedModalRect(x + 114, y + 74, 7, 17, 18, 18);
        this.drawTexturedModalRect(x + 134, y + 74, 7, 17, 18, 18);
        this.drawTexturedModalRect(x + 114, y + 94, 7, 17, 18, 18);
        this.drawTexturedModalRect(x + 134, y + 94, 7, 17, 18, 18);
    }
}
