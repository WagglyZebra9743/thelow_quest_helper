package com.thelow_quest_helper.thelow_quest_helper.item;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class BeaconBeamDrawer {

    private static int waypointX;
    private static int waypointY;
    private static int waypointZ;
    private static boolean waypointActive = false;
    private static String waypointName = "";
    private static final ResourceLocation BEACON_BEAM = new ResourceLocation("textures/entity/beacon_beam.png");


    public static void displayPoint(int x, int y, int z, String name) {
        waypointX = x;
        waypointY = y;
        waypointZ = z;
        waypointActive = true;
        waypointName = name;
    }
    public static void hidePoint() {
        waypointActive = false; // ビーコンを非アクティブに
        waypointName = "";    // 名前もクリアしておくと良いでしょう (任意)
    }
    

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if (!waypointActive) return;

        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayerSP player = mc.thePlayer;
        if (player == null) return;

        World world = mc.theWorld;
        if (world == null) return;

        String dimensionName = world.provider.getDimensionName();
        if (dimensionName.equals("dungeon")) return; // "end"ディメンジョンでは描画しない

        double px = player.lastTickPosX + (player.posX - player.lastTickPosX) * event.partialTicks;
        double py = player.lastTickPosY + (player.posY - player.lastTickPosY) * event.partialTicks;
        double pz = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * event.partialTicks;

       renderBeaconBeam(waypointX, waypointY, waypointZ, px, py, pz, event.partialTicks);
       renderWaypointText(waypointX, waypointY, waypointZ, px, py, pz, event.partialTicks);
    }
    
    public static void clearGuidanceBeam() {
    	waypointActive = false;
    }

    private void renderBeaconBeam(double x, double y, double z, double px, double py, double pz, float partialTicks) {
        Minecraft mc = Minecraft.getMinecraft();
        mc.getTextureManager().bindTexture(BEACON_BEAM);

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();

        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.enableTexture2D();
        GlStateManager.depthMask(false);

       double dx = x - px;
       double dz = z - pz;
        
        double height = 256;

        float f = (float) Minecraft.getSystemTime() % 30000 / 30000.0F * 8.0F;

        float f1 = 1.0F;
        float f2 = 1.0F;
        float f3 = 1.0F;
        
        GlStateManager.pushMatrix();
        GlStateManager.translate(x - px, y - py, z - pz);

         // ビームをプレイヤーの方向に向ける
        double angle = Math.atan2(dz, dx);
        GlStateManager.rotate((float) Math.toDegrees(-angle), 0, 1, 0);
         

        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);

       // 上向きのビーム
        for (int i = 0; i < 2; i++) { // 2回描画することで、透過を少し強くする
           float startY = 0;
            float endY = (float) height;
           float f4 = 0.0F + f;
            float f5 = 1.0F + f;
            float x1 = -0.5f;
            float z1 = -0.5f;
           float x2 = 0.5f;
           float z2 = 0.5f;

            worldrenderer.pos(x1, startY, z1).tex(f4, 0.0D).color(f1, f2, f3, 0.5f).endVertex();
            worldrenderer.pos(x2, startY, z2).tex(f5, 0.0D).color(f1, f2, f3, 0.5f).endVertex();
            worldrenderer.pos(x2, endY, z2).tex(f5, height / 16D).color(f1, f2, f3, 0.5f).endVertex();
            worldrenderer.pos(x1, endY, z1).tex(f4, height / 16D).color(f1, f2, f3, 0.5f).endVertex();
       }
        tessellator.draw();

        GlStateManager.popMatrix();

        GlStateManager.depthMask(true);
        GlStateManager.disableBlend();
        GlStateManager.enableCull();
        GlStateManager.enableLighting();
    }

     private void renderWaypointText(double x, double y, double z, double px, double py, double pz, float partialTicks) {
        Minecraft mc = Minecraft.getMinecraft();
        FontRenderer fontRenderer = mc.fontRendererObj;
         RenderManager renderManager = mc.getRenderManager();

        String text = waypointName + " (" + (int)x + ", " + (int)y + ", " + (int)z + ")";

        float textYOffset = 0.5f;

       double dx = x - px;
        double dy = y - py + textYOffset;
        double dz = z - pz;

       float distance = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
       float scale = 0.015f * (distance > 1f ? distance : 1f); 

       GlStateManager.pushMatrix();
        GlStateManager.translate(dx, dy, dz);
       GlStateManager.rotate(-renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
         GlStateManager.scale(-scale, -scale, scale);

        GlStateManager.disableDepth(); // デプスバッファを無効化し、常に描画
       GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        int stringWidth = fontRenderer.getStringWidth(text);

        // 半透明の背景を描画
       GlStateManager.disableTexture2D();
       Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);

        float rectWidth = stringWidth /2f + 2;
        float rectHeight = (fontRenderer.FONT_HEIGHT + 2) / 2.0f;
        float rectX = -rectWidth/2f;
        float rectY = -2;

        worldrenderer.pos(rectX, rectY, 0).color(0.3f, 0.3f, 0.3f, 0.6f).endVertex();
        worldrenderer.pos(rectX, rectY + rectHeight, 0).color(0.3f, 0.3f, 0.3f, 0.6f).endVertex();
        worldrenderer.pos(rectX + rectWidth, rectY + rectHeight, 0).color(0.3f, 0.3f, 0.3f, 0.6f).endVertex();
        worldrenderer.pos(rectX + rectWidth, rectY, 0).color(0.3f, 0.3f, 0.3f, 0.6f).endVertex();
        tessellator.draw();
       GlStateManager.enableTexture2D();

         // 文字列を描画
        GlStateManager.pushMatrix(); // 新しいマトリクスをプッシュ
       GlStateManager.scale(0.5f, 0.5f, 0.5f); // フォントサイズを半分にする
       fontRenderer.drawString(text, -stringWidth / 2f, 0, 0xFFFFFF, true);
         GlStateManager.popMatrix(); // マトリクスをポップ

       GlStateManager.enableDepth(); // デプスバッファを戻す
       GlStateManager.disableBlend();
        GlStateManager.enableLighting();
       GlStateManager.popMatrix();
    }
}
