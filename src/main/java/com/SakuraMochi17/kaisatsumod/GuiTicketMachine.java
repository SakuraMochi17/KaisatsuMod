package com.SakuraMochi17.kaisatsumod;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import java.util.*;

public class GuiTicketMachine extends GuiContainer {
    private static final ResourceLocation guiTexture = new ResourceLocation("textures/gui/container/dispenser.png");
    private TileEntityTicketMachine tileEntity;
    private List<Integer> calculatedFares = new ArrayList<>();
    private String displayStationName = "未設定";

    public GuiTicketMachine(InventoryPlayer playerInv, TileEntityTicketMachine te) {
        super(new ContainerTicketMachine(playerInv, te));
        this.tileEntity = te;
        this.xSize = 176;
        this.ySize = 166;
    }

    @Override
    public void initGui() {
        super.initGui();

        // ★修正：自動検索ではなく、券売機自身の TileEntity に記録された駅座標へ直接アクセス！
        if (tileEntity.isLinked) {
            int dimID = this.mc.theWorld.provider.dimensionId;
            // 登録レジストリからピンポイントで駅データを引き出す
            String key = dimID + ":" + tileEntity.linkedX + ":" + tileEntity.linkedY + ":" + tileEntity.linkedZ;
            StationRegistry.StationData currentStation = StationRegistry.findNearestStation(dimID, tileEntity.linkedX, tileEntity.linkedY, tileEntity.linkedZ, 1.0);

            if (currentStation != null) {
                this.displayStationName = currentStation.stationName;
                TreeSet<Integer> fareSet = new TreeSet<>();

                List<String> stations = FareManager.getStationsForLine(currentStation.lineID);
                for (String stName : stations) {
                    StationRegistry.StationData targetStation = StationRegistry.getStationByName(currentStation.lineID, stName);

                    if (targetStation != null && !targetStation.stationName.equals(currentStation.stationName)) {
                        int f = FareManager.calculateFare(currentStation.lineID, currentStation.x, currentStation.y, currentStation.z, targetStation.x, targetStation.y, targetStation.z);
                        if (f > 0) fareSet.add(f);
                    }
                }

                calculatedFares = new ArrayList<>(fareSet);
                if (calculatedFares.size() > 9) calculatedFares = calculatedFares.subList(0, 9);

                // --- ボタンの配置 ---
                for (int i = 0; i < calculatedFares.size(); i++) {
                    int row = i / 3;
                    int col = i % 3;
                    int fare = calculatedFares.get(i);
                    this.buttonList.add(new GuiButton(i, this.guiLeft + 68 + (col * 22), this.guiTop + 16 + (row * 16), 20, 14, String.valueOf(fare)));
                }

                // 入場券ボタン
                this.buttonList.add(new GuiButton(100, this.guiLeft + 68, this.guiTop + 66, 64, 14, "入場券"));
            }
        } else {
            this.displayStationName = "§c未連携§r";
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 100) {
            KaisatsuModMain.network.sendToServer(new MessagePurchaseTicket(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord, 150, true));
        } else if (button.id >= 0 && button.id < calculatedFares.size()) {
            int targetFare = calculatedFares.get(button.id);
            KaisatsuModMain.network.sendToServer(new MessagePurchaseTicket(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord, targetFare, false));
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(guiTexture);
        int k = (this.width - this.xSize) / 2;
        int l = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(k, l, 0, 0, this.xSize, this.ySize);

        drawRect(k + 7, l + 13, k + 169, l + 83, 0xFFC6C6C6);

        drawSlotBg(k + 152, l + 14); // IC
        drawSlotBg(k + 152, l + 32); // Ticket
        drawSlotBg(k + 134, l + 50); drawSlotBg(k + 152, l + 50); // Change
        drawSlotBg(k + 134, l + 68); drawSlotBg(k + 152, l + 68); // Change
        for(int i=0; i<3; i++) {
            for(int j=0; j<3; j++) {
                drawSlotBg(k + 12 + j*18, l + 18 + i*18); // Money
            }
        }
    }

    private void drawSlotBg(int x, int y) {
        drawRect(x - 1, y - 1, x + 17, y + 17, 0xFF373737);
        drawRect(x, y, x + 18, y + 18, 0xFFFFFFFF);
        drawRect(x, y, x + 17, y + 17, 0xFF8B8B8B);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        // 設定された駅名を表示
        this.fontRendererObj.drawString(this.displayStationName + "駅 券売機", 8, 5, 4210752);

        this.fontRendererObj.drawString("現金", 12, 8, 4210752);
        this.fontRendererObj.drawString("運賃", 68, 8, 4210752);
        this.fontRendererObj.drawString("IC", 138, 18, 4210752);
        this.fontRendererObj.drawString("切符", 130, 36, 4210752);
        this.fontRendererObj.drawString("お釣", 112, 54, 4210752);
    }
}
