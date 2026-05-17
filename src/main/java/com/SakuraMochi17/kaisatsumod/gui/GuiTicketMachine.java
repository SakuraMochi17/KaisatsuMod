package com.SakuraMochi17.kaisatsumod.gui;

import com.SakuraMochi17.kaisatsumod.core.FareManager;
import com.SakuraMochi17.kaisatsumod.KaisatsuModMain;
import com.SakuraMochi17.kaisatsumod.network.MessagePurchaseTicket;
import com.SakuraMochi17.kaisatsumod.core.StationRegistry;
import com.SakuraMochi17.kaisatsumod.tileentity.TileEntityTicketMachine;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import java.util.*;

public class GuiTicketMachine extends GuiContainer {
    private static final ResourceLocation guiTexture = new ResourceLocation("textures/gui/container/dispenser.png");
    private final TileEntityTicketMachine tileEntity;
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

        if (tileEntity.isLinked) {
            int dimID = this.mc.theWorld.provider.dimensionId;
            StationRegistry.StationData currentStation = StationRegistry.findNearestStation(dimID, tileEntity.linkedX, tileEntity.linkedY, tileEntity.linkedZ, 1.0);

            if (currentStation != null) {
                this.displayStationName = currentStation.stationName;
                String currentCompany = FareManager.getCompanyID(currentStation.lineID); // 自駅の会社IDを取得
                TreeSet<Integer> fareSet = new TreeSet<>();

                // ★大改修：レジストリ内の全駅をスキャンし、「同じ会社（同社線）」の駅を網羅する
                for (StationRegistry.StationData targetStation : StationRegistry.registry.values()) {
                    // 自分自身の駅は除外
                    if (!targetStation.stationName.equals(currentStation.stationName)) {
                        String targetCompany = FareManager.getCompanyID(targetStation.lineID);

                        // ★同社線制限：会社IDが一致する場合のみボタン候補にする
                        if (currentCompany.equals(targetCompany)) {
                            int f = FareManager.calculateFare(currentStation.lineID, currentStation.x, currentStation.y, currentStation.z, targetStation.x, targetStation.y, targetStation.z);
                            if (f > 0) {
                                // ★改善：運賃の1の位を切り上げて10円単位にする処理
                                int roundedFare = (int) Math.ceil(f / 10.0) * 10;
                                fareSet.add(roundedFare);
                            }
                        }
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
        drawSlotBg(k + 134, l + 50);
        drawSlotBg(k + 152, l + 50); // Change
        drawSlotBg(k + 134, l + 68);
        drawSlotBg(k + 152, l + 68); // Change
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                drawSlotBg(k + 12 + j * 18, l + 18 + i * 18); // Money
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
        // ★修正：「 券売機」の文字を削り、駅名が長くても「運賃」とかぶらないようにしました
        this.fontRendererObj.drawString(this.displayStationName + "駅", 8, 4, 4210752);

        this.fontRendererObj.drawString("運賃", 68, 6, 4210752);
        this.fontRendererObj.drawString("IC", 154, 4, 4210752);
        this.fontRendererObj.drawString("切符", 130, 36, 4210752);
        this.fontRendererObj.drawString("お釣", 112, 54, 4210752);

        this.fontRendererObj.drawString("現金", 12, 74, 4210752);
    }
}
