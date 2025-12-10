package com.thelow_quest_helper.thelow_quest_helper.LongQuest;

import java.util.ArrayList;
import java.util.Iterator;
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

public class LongQuestMarker {
	
    // 内部で使用するデータ構造
    private static class Point {
        final Vector3d pos;
        final String label;
        final String ID;
        final Color color;
        
        Point(final Vector3d pos, final String label,final String ID,final String color) {
        	this.pos = pos;
        	this.label = label;
        	this.ID = ID;
        	if(color!=null&&color.equals("yellow")) {
        		this.color = yellow;
        		return;
        	}
        	this.color = gold;
        }
	}
	 

    private static class Color{
		final float R;
		final float G;
		final float B;
		final float Alpha;
		Color(final float R,final float G,final float B,final float Alpha){
			this.R = R;
			this.G = G;
			this.B = B;
			this.Alpha = Alpha;
		}
	}
    // マーカーのリスト
	private static final List<Point> points = new ArrayList<>();
	
	// 削除判定を行う距離 (ブロック単位)
	private static final double REMOVE_DISTANCE = 10.0;
	
	//色を定義
	private static final Color gold = new Color(1.0f, 0.67f, 0.0f, 0.5f);
	private static final Color yellow = new Color(1.0f, 1.0f, 0.0f, 0.5f);
	    
	private final Minecraft mc = Minecraft.getMinecraft();
	
	//通常のクエスト用マーカーを追加する
	public static void addMarker(final double x, final double y, final double z, final String label,final String ID) {
		points.add(new Point(new Vector3d(x, y, z), label,ID,"gold"));
	}
	
	//色違いのサブ用マーカー
	public static void addSubMarker(final double x, final double y, final double z, final String label,final String ID) {
		points.add(new Point(new Vector3d(x, y, z), label,ID,"yellow"));
	}
	    
	//マーカーをクリアする
	public static void clearMarkers() {
		points.clear();
	}
	
	//IDから指定マーカーを削除する
	public static void RemoveMarkerByID(final String ID) {
		if(ID==null||points.isEmpty())return;
		Iterator<Point> iterator = points.iterator();
		while (iterator.hasNext()) {
			final Point p = iterator.next();
			final String ThePID = p.ID;
			if(ThePID!=null&&ThePID.startsWith(ID)) {
				iterator.remove();
			}
		}
	}

	//マーカーを表示するメソッド
	@SubscribeEvent
	public void onRenderWorld(RenderWorldLastEvent event) {
		// マーカーがなければ何もしない
		if (points.isEmpty()) return;
		
		final RenderManager rm = mc.getRenderManager();
		if (rm == null) return;
		
		// イテレータを使用して、ループしながら安全に削除を行えるようにする
		Iterator<Point> iterator = points.iterator();
		
		while (iterator.hasNext()) {
			final Point p = iterator.next();
			
			// プレイヤー視点からの相対座標を計算
			double x = p.pos.x - rm.viewerPosX;
			double y = p.pos.y - rm.viewerPosY;
			double z = p.pos.z - rm.viewerPosZ;
			
			// 距離計算
			double distance = Math.sqrt(x * x + y * y + z * z);
			
			//近づいたら消す
			if (distance < REMOVE_DISTANCE) {
				if(!p.label.startsWith("§6[Quest]"))mc.thePlayer.addChatMessage(new ChatComponentText("§a[thelow_quest_helper]§f"+p.label+"§7に近づいたのでマーカーを削除しました"));
				// リストから削除
				iterator.remove();
				continue; // 削除したので描画処理はスキップして次のマーカーへ
			}
			
			// --- 描画処理 (MarkerRendererと見た目を統一) ---
			
			// 遠すぎる場合でも位置がわかるように座標を丸める処理
			if (distance > 100.0) {
				final double scale = 100.0 / distance;
				x *= scale;
				y *= scale;
				z *= scale;
				distance = 100.0; 
			}
			
			GlStateManager.pushMatrix();
			GlStateManager.disableDepth();
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
			
			GlStateManager.pushMatrix();
			GlStateManager.translate(x, y, z);
			
			// プレイヤーの方向を向く
			GlStateManager.rotate(-rm.playerViewY, 0F, 1F, 0F);
			GlStateManager.rotate(rm.playerViewX, 1F, 0F, 0F);
			
			// スケール調整
			double scale = 0.02 * distance;
			scale = Math.min(scale, 5.0);
			scale = Math.max(scale, 0.1);
			GlStateManager.scale(-scale, -scale, scale);
			
			// マーカー本体(四角形)の描画
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			
			// 色を可変にした
			GL11.glColor4f(p.color.R,p.color.G,p.color.B,p.color.Alpha);
			
			GL11.glBegin(GL11.GL_QUADS);
			GL11.glVertex3d(-1, 1, 0);
			GL11.glVertex3d(1, 1, 0);
			GL11.glVertex3d(1, -1, 0);
			GL11.glVertex3d(-1, -1, 0);
			GL11.glEnd();
			
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			GL11.glColor4f(1f, 1f, 1f, 1f);
			
			// テキストラベルの描画
			final String[] lines = p.label.split("\n");
			FontRenderer font = mc.fontRendererObj;
			
			GlStateManager.pushMatrix();
			final double textScale = 0.3;
			GlStateManager.scale(textScale, textScale, textScale);
			
			final int lineHeight = font.FONT_HEIGHT;
			for (int i = 0; i < lines.length; i++) {
				final String line = lines[i];
				final int width = font.getStringWidth(line);
				font.drawString(line, -width / 2, -20 + (i * lineHeight), 0xFFFFFF);
			}
			GlStateManager.popMatrix();
			GlStateManager.popMatrix();
			
			GlStateManager.disableBlend();
			GlStateManager.enableDepth();
			GlStateManager.popMatrix();
		}
	}
	
	public static boolean IsThereMarker() {
		if(points == null||points.isEmpty())return false;
		return true;
	}
}
