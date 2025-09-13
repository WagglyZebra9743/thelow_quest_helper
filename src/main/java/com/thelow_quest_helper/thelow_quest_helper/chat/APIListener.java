package com.thelow_quest_helper.thelow_quest_helper.chat;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.thelow_quest_helper.thelow_quest_helper.item.MarkerRenderer;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class APIListener {

    private static String latestData = null;
    public static double[] overStrength = {1.0,1.0,1.0};
    public static boolean status_getted = false;
    public static boolean tickenable = false;
    public static boolean isClantp = true;
    private static int TickTimer = 0;
    private static int INTERVAL = 200;
    public static boolean gasya=false;
    
    private static final Minecraft mc = Minecraft.getMinecraft();

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onChat(ClientChatReceivedEvent event) {
        
        String msg = event.message.getUnformattedText();
        
        
        if (msg.startsWith("$api")) {
            String[] split = msg.split(" ", 2);
            if (split.length == 2) {
                try {
                	JsonObject json = new JsonParser().parse(split[1]).getAsJsonObject();
                    String apiType = json.get("apiType").getAsString();
                    if("location".equals(apiType)) {
                    	JsonObject response = json.getAsJsonObject("response");
                        String worldName = response.get("worldName").getAsString();
                        if(worldName.equals("thelow")) {
                        	MarkerRenderer.marker_enable = true;
                        	gasya=false;
                        }else {
                        	if(!gasya) {
                        		MarkerRenderer.marker_enable = false;
                        	}
                        }
                    }
                    if("player_status".equals(apiType)) {
                    	JsonObject response = json.getAsJsonObject("response");
                        String mcid = response.get("mcid").getAsString();
                        String my_mcid = mc.thePlayer.getName();
                        if(mcid.equals(my_mcid)) {
                        	JsonObject clanInfo = response.get("clanInfo").getAsJsonObject();
                        	String clanRank = clanInfo.get("clanRank").getAsString();
                        	if("UNRANKED".contains(clanRank)||"IRON".contains(clanRank)||"GOLD".contains(clanRank)||"LAPIS".contains(clanRank)||"EMERALD".contains(clanRank)||"REDSTONE".contains(clanRank)||"DIAMOND".contains(clanRank)) {
                        		isClantp = false;
                        	}else {
                        		isClantp = true;
                        	}
                        }
                    }
                    
                }
                    
                 catch (Exception e) {
                    mc.thePlayer.addChatMessage(new ChatComponentText("§a[thelow_quest_helper]§c 解析失敗: " + e.getMessage()));
                }
            }
        }
    }

    @SubscribeEvent
    public void APIcancel(ClientChatReceivedEvent event) {
        String message = event.message.getUnformattedText(); // 色コードや装飾を除去したテキスト
        
        //killとかで死んでダンジョンから出た想定
        String mcid = mc.thePlayer.getName();
        if(message.contains(mcid)) {
        	mc.thePlayer.sendChatMessage("/thelow_api location");
        }
        //tpでダンジョンから出た想定
        if(message.contains("テレポートしました")) {
        	mc.thePlayer.sendChatMessage("/thelow_api location");
        }
        
        if(message.contains("ここに置くガチャは増えていくかもしれません。")) {
        	gasya=true;
        }
        
        if (message.startsWith("$api")) {
            event.setCanceled(true); // この行で表示をキャンセル
        }
    }
    
    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START)return;//TickEventはSTARTとENDの2回発火するので1回にする
        if(!MarkerRenderer.IsThereMarker()) {
        	TickTimer = 0;
        	return;//マーカーが無いなら処理しない
        }
        TickTimer++;//1ずつ加算
        if (TickTimer%INTERVAL == 1) {//特定の値なら
            EntityPlayer player = mc.thePlayer;
            if (player != null) {
            	//コマンド送信
            	System.out.println("location");
                mc.thePlayer.sendChatMessage("/thelow_api location");
            }
        }
        if(TickTimer/INTERVAL == 10) {
        	TickTimer = 0;
        	EntityPlayer player = mc.thePlayer;
            if (player != null) {
            	//コマンド送信
            	System.out.println("player");
                mc.thePlayer.sendChatMessage("/thelow_api player");
            }
        }
    }


    public static String getother() {
        return latestData;
    }
}
