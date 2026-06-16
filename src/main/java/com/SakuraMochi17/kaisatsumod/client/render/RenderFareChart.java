package com.SakuraMochi17.kaisatsumod.client.render;

import com.SakuraMochi17.kaisatsumod.tileentity.TileEntityFareChart;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import org.lwjgl.opengl.GL11;

import java.util.*;

public class RenderFareChart extends TileEntitySpecialRenderer {

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTicks) {
        if (!(te instanceof TileEntityFareChart)) return;
        TileEntityFareChart chartTE = (TileEntityFareChart) te;
        int meta = te.getBlockMetadata();

        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5);

        if (meta == 0) GL11.glRotatef(0, 0, 1, 0);
        if (meta == 1) GL11.glRotatef(270, 0, 1, 0);
        if (meta == 2) GL11.glRotatef(180, 0, 1, 0);
        if (meta == 3) GL11.glRotatef(90, 0, 1, 0);

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_CULL_FACE);

        // ==========================================
        // 1. モニター筐体の描画
        // ==========================================
        GL11.glBegin(GL11.GL_QUADS);
        float X_L = -0.5F, X_R = 0.5F, Y_T = 0.5F, Y_B = -0.5F;
        float Z_BACK = 0.5F, Z_FRONT = -0.5F, Z_MID = 0.0F;

        GL11.glColor4f(0.7F, 0.7F, 0.7F, 1.0F);
        GL11.glVertex3d(X_L, Y_T, Z_FRONT); GL11.glVertex3d(X_R, Y_T, Z_FRONT); GL11.glVertex3d(X_R, Y_B, Z_MID); GL11.glVertex3d(X_L, Y_B, Z_MID);
        GL11.glVertex3d(X_R, Y_T, Z_BACK); GL11.glVertex3d(X_L, Y_T, Z_BACK); GL11.glVertex3d(X_L, Y_B, Z_BACK); GL11.glVertex3d(X_R, Y_B, Z_BACK);

        GL11.glColor4f(0.5F, 0.5F, 0.5F, 1.0F);
        GL11.glVertex3d(X_L, Y_T, Z_BACK); GL11.glVertex3d(X_R, Y_T, Z_BACK); GL11.glVertex3d(X_R, Y_T, Z_FRONT); GL11.glVertex3d(X_L, Y_T, Z_FRONT);
        GL11.glVertex3d(X_R, Y_B, Z_BACK); GL11.glVertex3d(X_L, Y_B, Z_BACK); GL11.glVertex3d(X_L, Y_B, Z_MID); GL11.glVertex3d(X_R, Y_B, Z_MID);
        GL11.glVertex3d(X_L, Y_T, Z_BACK); GL11.glVertex3d(X_L, Y_T, Z_FRONT); GL11.glVertex3d(X_L, Y_B, Z_MID); GL11.glVertex3d(X_L, Y_B, Z_BACK);
        GL11.glVertex3d(X_R, Y_T, Z_FRONT); GL11.glVertex3d(X_R, Y_T, Z_BACK); GL11.glVertex3d(X_R, Y_B, Z_BACK); GL11.glVertex3d(X_R, Y_B, Z_MID);

        GL11.glColor4f(0.05F, 0.05F, 0.05F, 1.0F);
        float sX_L = -0.45F, sX_R = 0.45F, sY_T = 0.45F, sY_B = -0.45F;
        float sZ_TOP = -0.48F, sZ_BOTTOM = -0.03F;
        GL11.glVertex3d(sX_R, sY_T, sZ_TOP); GL11.glVertex3d(sX_L, sY_T, sZ_TOP); GL11.glVertex3d(sX_L, sY_B, sZ_BOTTOM); GL11.glVertex3d(sX_R, sY_B, sZ_BOTTOM);
        GL11.glEnd();

        GL11.glEnable(GL11.GL_TEXTURE_2D);

        // ==========================================
        // 2. 最適化された路線図レイアウトの描画
        // ==========================================
        FontRenderer font = this.func_147498_b();
        if (font != null && !chartTE.nodeList.isEmpty()) {
            GL11.glPushMatrix();
            GL11.glTranslatef(0.0F, 0.0F, -0.26F);
            GL11.glRotatef(-26.565F, 1.0F, 0.0F, 0.0F);
            GL11.glTranslatef(0.0F, 0.0F, -0.015F);

            float scale = 0.005F;
            GL11.glScalef(-scale, -scale, scale);
            GL11.glDepthMask(false);

            List<TileEntityFareChart.NodeData> sortedNodes = new ArrayList<>(chartTE.nodeList);
            Collections.sort(sortedNodes, new Comparator<TileEntityFareChart.NodeData>() {
                public int compare(TileEntityFareChart.NodeData n1, TileEntityFareChart.NodeData n2) {
                    return Integer.compare(n1.depth, n2.depth);
                }
            });

            boolean isLoopMode = false;
            String mainLoopLine = "";
            for (TileEntityFareChart.NodeData n : sortedNodes) {
                if (n.depth == 1 && n.isLoop) {
                    isLoopMode = true;
                    mainLoopLine = n.lineName.replaceAll("_F$|_B$", "");
                    break;
                }
            }

            Map<String, Float[]> posMap = new HashMap<>();
            List<String> loopOrder = new ArrayList<>();

            // [A] 環状線モード
            if (isLoopMode) {
                List<TileEntityFareChart.NodeData> loopNodes = new ArrayList<>();
                for (TileEntityFareChart.NodeData n : chartTE.nodeList) {
                    if (n.isLoop && n.lineName.replaceAll("_F$|_B$", "").equals(mainLoopLine)) loopNodes.add(n);
                }

                List<TileEntityFareChart.NodeData> fwdChain = new ArrayList<>();
                String curr = chartTE.stationName;
                while (true) {
                    TileEntityFareChart.NodeData next = null;
                    for (TileEntityFareChart.NodeData n : loopNodes) { if (n.parent.equals(curr) && n.lineName.endsWith("_F")) { next = n; break; } }
                    if (next != null) { fwdChain.add(next); curr = next.name; } else break;
                }

                List<TileEntityFareChart.NodeData> bckChain = new ArrayList<>();
                curr = chartTE.stationName;
                while (true) {
                    TileEntityFareChart.NodeData next = null;
                    for (TileEntityFareChart.NodeData n : loopNodes) { if (n.parent.equals(curr) && n.lineName.endsWith("_B")) { next = n; break; } }
                    if (next != null) { bckChain.add(next); curr = next.name; } else break;
                }

                for (int i = bckChain.size() - 1; i >= 0; i--) loopOrder.add(bckChain.get(i).name);
                loopOrder.add(chartTE.stationName);
                for (TileEntityFareChart.NodeData n : fwdChain) loopOrder.add(n.name);

                int N = loopOrder.size();
                float w = 110.0F, h = 50.0F;
                float totalLen = 2 * w + 2 * h;
                float offset = (1.5F * w + h) - (bckChain.size() * totalLen / N);

                for (int i = 0; i < N; i++) {
                    float dist = ((i * totalLen / N) + offset) % totalLen;
                    if (dist < 0) dist += totalLen;

                    float px, py;
                    if (dist <= w) { px = -w/2 + dist; py = -h/2; }
                    else if (dist <= w + h) { px = w/2; py = -h/2 + (dist - w); }
                    else if (dist <= 2*w + h) { px = w/2 - (dist - (w+h)); py = h/2; }
                    else { px = -w/2; py = h/2 - (dist - (2*w+h)); }

                    posMap.put(loopOrder.get(i), new Float[]{px, py});
                }

                GL11.glDisable(GL11.GL_TEXTURE_2D);
                GL11.glColor4f(0.8F, 0.8F, 0.8F, 1.0F);
                GL11.glLineWidth(2.0F);
                GL11.glBegin(GL11.GL_LINE_LOOP);
                GL11.glVertex3d(-w/2, -h/2, 0); GL11.glVertex3d(w/2, -h/2, 0);
                GL11.glVertex3d(w/2, h/2, 0); GL11.glVertex3d(-w/2, h/2, 0);
                GL11.glEnd();
                GL11.glEnable(GL11.GL_TEXTURE_2D);

                Map<String, Float[]> branchDirs = new HashMap<>();
                for (TileEntityFareChart.NodeData n : sortedNodes) {
                    if (!posMap.containsKey(n.name)) {
                        Float[] pPos = posMap.get(n.parent);
                        if (pPos != null) {
                            float dx = 35.0F, dy = 0.0F;
                            if (loopOrder.contains(n.parent)) {
                                float px = pPos[0], py = pPos[1];
                                if (py <= -h/2 + 1) { dx = 0; dy = -30; }
                                else if (py >= h/2 - 1) { dx = 0; dy = 30; }
                                else if (px <= -w/2 + 1) { dx = -35; dy = 0; }
                                else if (px >= w/2 - 1) { dx = 35; dy = 0; }
                            } else if (branchDirs.containsKey(n.lineName)) {
                                Float[] dir = branchDirs.get(n.lineName);
                                dx = dir[0]; dy = dir[1];
                            }
                            branchDirs.put(n.lineName, new Float[]{dx, dy});
                            posMap.put(n.name, new Float[]{pPos[0] + dx, pPos[1] + dy});
                        }
                    }
                }

            } else {
                // [B] 直線分岐モード
                List<String> uniqueLines = new ArrayList<>();
                for (TileEntityFareChart.NodeData n : sortedNodes) {
                    if (!n.lineName.isEmpty() && !uniqueLines.contains(n.lineName)) uniqueLines.add(n.lineName);
                }
                Collections.sort(uniqueLines);

                float[] yOffsets = {0.0F, -30.0F, 30.0F, -60.0F, 60.0F, -90.0F, 90.0F};
                Map<String, Float> lineY = new HashMap<>();
                for (int i = 0; i < uniqueLines.size(); i++) {
                    lineY.put(uniqueLines.get(i), yOffsets[i % yOffsets.length]);
                }

                posMap.put(chartTE.stationName, new Float[]{-80.0F, 15.0F});

                for (TileEntityFareChart.NodeData node : sortedNodes) {
                    if (node.depth == 0) continue;
                    Float[] pPos = posMap.get(node.parent);
                    if (pPos != null) {
                        float px = pPos[0] + 35.0F;
                        float targetY = 15.0F + lineY.getOrDefault(node.lineName, 0.0F);
                        posMap.put(node.name, new Float[]{px, targetY});
                    }
                }
            }

            // ----------------------------------------------------
            // [共通] 白いルート線とテキストの描画
            // ----------------------------------------------------
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glLineWidth(2.0F);
            GL11.glBegin(GL11.GL_LINES);
            for (TileEntityFareChart.NodeData node : sortedNodes) {
                boolean isMainLoop = isLoopMode && node.isLoop && node.lineName.replaceAll("_F$|_B$", "").equals(mainLoopLine);
                if (!isMainLoop && node.parent != null && !node.parent.isEmpty() && posMap.containsKey(node.parent) && posMap.containsKey(node.name)) {
                    Float[] pPos = posMap.get(node.parent);
                    Float[] mPos = posMap.get(node.name);

                    // ★追加: 運賃オーバーの場合は「線の先（長さ半分）」だけを描いて途切れる表現にする
                    if (node.isCutoff) {
                        float midX = pPos[0] + (mPos[0] - pPos[0]) * 0.5F; // 50%の長さ
                        float midY = pPos[1] + (mPos[1] - pPos[1]) * 0.5F;
                        GL11.glVertex3d(pPos[0], pPos[1], 0);
                        GL11.glVertex3d(midX, midY, 0);
                    } else {
                        GL11.glVertex3d(pPos[0], pPos[1], 0);
                        GL11.glVertex3d(mPos[0], mPos[1], 0);
                    }
                }
            }
            GL11.glEnd();
            GL11.glEnable(GL11.GL_TEXTURE_2D);

            for (TileEntityFareChart.NodeData node : sortedNodes) {
                if (node.isCutoff) continue; // ★追加：運賃オーバーの駅は、点も文字も描画しない！

                Float[] mPos = posMap.get(node.name);
                if (mPos == null) continue;
                float px = mPos[0]; float py = mPos[1];
                boolean isCurrent = (node.depth == 0);
                int pointColor = isCurrent ? 0xFF5555 : 0xFFFFFF;

                font.drawString("●", (int)px - 2, (int)py - 4, pointColor);

                GL11.glPushMatrix();
                GL11.glTranslatef(px, py - 6.0F, 0.0F);
                GL11.glRotatef(-45.0F, 0.0F, 0.0F, 1.0F);
                font.drawString(node.name, 2, -font.FONT_HEIGHT, pointColor);
                GL11.glPopMatrix();

                if (isCurrent) {
                    String downStr = "当駅";
                    font.drawString(downStr, (int)px - font.getStringWidth(downStr) / 2, (int)py + 8, 0xFF5555);
                } else {
                    String fStr = (node.fare > 0) ? (node.fare + "円") : "---";
                    font.drawString(fStr, (int)px - font.getStringWidth(fStr) / 2, (int)py + 8, 0xFFFF55);
                }
            }
            GL11.glDepthMask(true);
            GL11.glPopMatrix();
        }

        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glPopMatrix();
    }
}
