package com.thelow_quest_helper.thelow_quest_helper.item;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Vector3d;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class MarkerRenderer {

    private static class Marker {
    	Vector3d pos;
        String label;

        Marker(Vector3d pos, String label) {
            this.pos = pos;
            this.label = label;
        }
    }

    private static final List<Marker> markers = new ArrayList<>();
    
    public static boolean marker_enable = true;
    private static boolean current_enable = true;
    Minecraft mc = Minecraft.getMinecraft();

    /** マーカーを追加する */
    public static void addMarker(double x, double y, double z, String label) {
        markers.add(new Marker(new Vector3d(x, y, z), label));
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
    	
    	if (marker_enable != current_enable) {
    	    if (marker_enable) {
    	        if(markers!=null&&!markers.isEmpty()) {
    	        	mc.thePlayer.addChatMessage(new ChatComponentText("§a[thelow_quest_helper]§7マーカーが有効化されました"));
    	        }
    	    } else {
    	    	if(markers!=null&&!markers.isEmpty()) {
    	    		mc.thePlayer.addChatMessage(new ChatComponentText("§a[thelow_quest_helper]§7マーカーが一時的に無効化されました"));
    	    		mc.thePlayer.addChatMessage(new ChatComponentText("§7地上ワールドでのみ有効です"));
    	    	}
    	    }
    	    current_enable = marker_enable; // 状態を更新
    	}
    	
    	if(!marker_enable)return;
    	
        
        RenderManager rm = mc.getRenderManager();
        if (rm == null) return;
        

        GlStateManager.pushMatrix();
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        for (Marker m : markers) {
        	
            double x = m.pos.x - rm.viewerPosX;
            double y = m.pos.y - rm.viewerPosY;
            double z = m.pos.z - rm.viewerPosZ;

            double distance = Math.sqrt(x * x + y * y + z * z);
            
            if(distance<5) {
            	mc.thePlayer.addChatMessage(new ChatComponentText("§a[thelow_quest_helper]§7目的地§e("+m.pos.x+","+m.pos.y+","+m.pos.z+")"+"§7に近づいたので案内を終了します"));
            	clearMarkers();
            	break;
            }
            
            if (distance > 100.0) {
                double scale = 100.0 / distance;
                x *= scale;
                y *= scale;
                z *= scale;
                distance = 100.0; // 一応距離を更新しておく
            }
            
            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z);

            // プレイヤー視点に常に向く
            GlStateManager.rotate(-rm.playerViewY, 0F, 1F, 0F);
            GlStateManager.rotate(rm.playerViewX, 1F, 0F, 0F);

            double scale = 0.02 * distance;

            // 過剰に大きくならないように上限を設定
            scale = Math.min(scale, 5.0);
            scale = Math.max(scale, 0.1);
            GlStateManager.scale(-scale, -scale, scale);

            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glColor4f(0f, 1f, 0f, 0.5f);
            GL11.glBegin(GL11.GL_QUADS);
            GL11.glVertex3d(-1, 1, 0);
            GL11.glVertex3d(1, 1, 0);
            GL11.glVertex3d(1, -1, 0);
            GL11.glVertex3d(-1, -1, 0);
            GL11.glEnd();
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glColor4f(1f, 1f, 1f, 1f);

            // ラベル描画
            // ラベルを行ごとに分割
            String[] lines = m.label.split("\\\\n"); // 重要: \n を文字列として扱うのでエスケープ2重

            FontRenderer font = mc.fontRendererObj;
            
            
            GlStateManager.pushMatrix();
            scale = 0.3;
            GlStateManager.scale(scale, scale, scale);
            // 行ごとに描画
            int lineHeight = font.FONT_HEIGHT; // 行間を少し空ける
            for (int i = 0; i < lines.length; i++) {
            	
            	
                String line = lines[i];
                int width = font.getStringWidth(line);
                font.drawString(line, -width / 2, -20 + (i * lineHeight), 0xFFFFFF);
                
            }
            GlStateManager.popMatrix();
            
            
            GlStateManager.popMatrix();
        }

        GlStateManager.disableBlend();
        GlStateManager.enableDepth();
        GlStateManager.popMatrix();
        
    }
    
    public static void clearMarkers() {
        markers.clear();
    }
}
