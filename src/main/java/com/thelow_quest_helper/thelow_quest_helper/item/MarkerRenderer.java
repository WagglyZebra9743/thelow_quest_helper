package com.thelow_quest_helper.thelow_quest_helper.item;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Vector3d;

import org.lwjgl.opengl.GL11;

import com.thelow_quest_helper.thelow_quest_helper.chat.APIListener;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.BlockPos;
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
    private static int i = 0;

    // マーカーを追加する処理
    public static void addMarker(double x, double y, double z, String label) {
        markers.add(new Marker(new Vector3d(x, y, z), label));
        i=0;
    }

    
    //マーカー表示メソッド
    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
    	
    	
    	//マーカーが有効から無効になったか、無効から有効になったかでメッセージ分岐
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
    	
    	if(!marker_enable&&APIListener.gasya&&APIListener.can_cmd_send) {
    		BlockPos blockpos = Keyclick.getPlayerBlockPos();
    		int x1 = blockpos.getX(),y1=blockpos.getY(),z1=blockpos.getZ();
    		if(x1==-1253&&y1==85&&z1==-973){
    			mc.thePlayer.sendChatMessage("/thelow_api location");
    			System.out.println("gacha to thelow");
    			APIListener.gasya = false;
    			APIListener.can_cmd_send=false;
    		}
    	}
    	
    	//飛空艇の移動先座標でリセット
    	if(!marker_enable&&APIListener.can_cmd_send) {
    		BlockPos blockpos = Keyclick.getPlayerBlockPos();
    		int x1 = blockpos.getX(),y1=blockpos.getY(),z1=blockpos.getZ();
    		if((x1==-150&&y1==184&&z1==1293)||(x1==1319&&y1==127&&z1==-929)||(x1==-935&&y1==102&&z1==1142)||(x1==-1127&&y1==128&&z1==-794)||(x1==-37&&y1==128&&z1==909)||(x1==-382&&y1==101&&z1==-1265)||(x1==-463&&y1==152&&z1==-608)){
    			mc.thePlayer.sendChatMessage("/thelow_api location");
    			System.out.println("airship to thelow");
    			APIListener.can_cmd_send=false;
    		}
    	}
    	
    	//マーカーが無効なら表示しない
    	if(!marker_enable)return;
    	
        RenderManager rm = mc.getRenderManager();
        if (rm == null) return;
        
        //マーカー情報が無いなら終了
        if(markers==null||markers.isEmpty())return;
        //i番目のマーカーの情報をmに保存
        Marker m = markers.get(i);
        
        //マーカーの座標とプレイヤーの表示視点の座標から、マーカーを表示する座標を取得
        //ただし、これはプレイヤーから見た時の相対座標である
        double x = m.pos.x - rm.viewerPosX;
        double y = m.pos.y - rm.viewerPosY;
        double z = m.pos.z - rm.viewerPosZ;

        //三平方の定理によりマーカー表示予定地の距離を測定
        double distance = Math.sqrt(x * x + y * y + z * z);
        
        //マーカーまで近かったら次のマーカーに切り替えるか案内を終了する
        if(distance<15&&i+1==markers.size()) {//リストには通過順に格納されているのでその最後は目的地になる
        	if(markers.size()==1) {//サイズが1の時は最初の接近が最後の接近になる
        		mc.thePlayer.addChatMessage(new ChatComponentText("§a[thelow_quest_helper]§7目的地§e"+Keyclick.goalname+"("+m.pos.x+","+m.pos.y+","+m.pos.z+")"+" §7に近づいたので案内を終了します"));
        	}else{
        		mc.thePlayer.addChatMessage(new ChatComponentText("§a[thelow_quest_helper]§7目的地§e"+m.label+"("+m.pos.x+","+m.pos.y+","+m.pos.z+")"+" §7に近づいたので案内を終了します"));
        	}
        	clearMarkers();
        	i = 0;
        	return;
        }else if(distance<15) {//距離が近かったら次のマーカーに切り替える
        	i++;
        	mc.thePlayer.addChatComponentMessage(new ChatComponentText("§a[thelow_quest_helper]§7次は§e"+markers.get(i).label+"§7に向かってください"));
        	return;
        }
        
        //距離が100以上の時は100に丸める
        //120マス以上のマーカーは表示されない
        if (distance > 100.0) {
            double scale = 100.0 / distance;
            x *= scale;
            y *= scale;
            z *= scale;
            distance = 100.0; // 一応距離を更新しておく
        }
        
        GlStateManager.pushMatrix();
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

            
            
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
        

        GlStateManager.disableBlend();
        GlStateManager.enableDepth();
        GlStateManager.popMatrix();
        
    }
    
    public static void clearMarkers() {
        markers.clear();
    }
    
    //現在表示されているマーカーの名前を取得する
    public static String getMarkername() {
    	String text = markers.get(i).label;
    	if(text.contains("\n")) {
    		text = text.split("\\\\n")[1];
    	}
    	return text;
    }
    
    
    //マーカーがあったらtrue,無かったらfalse
    public static boolean IsThereMarker() {
    	if((markers==null||markers.isEmpty())) {
    		return false;
    	}else {
    		return true;
    	}
    }
}
