package com.thelow_quest_helper.thelow_quest_helper.chat;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.thelow_quest_helper.thelow_quest_helper.thelow_quest_helper;
import com.thelow_quest_helper.thelow_quest_helper.LongQuest.LongQuest;
import com.thelow_quest_helper.thelow_quest_helper.config.thelow_quest_helperConfig;
import com.thelow_quest_helper.thelow_quest_helper.item.MarkerRenderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.event.ClickEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class APIListener {

    public static boolean isClantp = true;
    private static int TickTimer = 0;
    private static int INTERVAL = 200;
    public static boolean gasya=false;
    public static boolean can_cmd_send = true;
    private static int cmd_ct = 0;
    public static String cleardDungeonName = "ダミー";
    private static boolean version_Checked = false;
    private static int SendVersionTimer = 0;
    
    private static final Minecraft mc = Minecraft.getMinecraft();

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onChat(ClientChatReceivedEvent event) {
        
    	final String msg = event.message.getUnformattedText();
    	final String colormsg = event.message.getFormattedText();
    	if(!version_Checked&&thelow_quest_helperConfig.AutoVersionCheck&&colormsg.startsWith("§r§a倉庫データを取得しました")) {
    		SendVersionTimer = 40;
        }
    	if(colormsg!=null&&colormsg.startsWith("§r")&&colormsg.endsWith("§r")) {
    		LongQuest.UpdatePhaseByTitle(colormsg);
    	}
        
        
        if (msg.startsWith("$api")) {
            String[] split = msg.split(" ", 2);
            if (split.length != 2)return;
            try {
                final JsonObject json = new JsonParser().parse(split[1]).getAsJsonObject();
                if(!json.has("apiType")||!json.has("response"))return;
                final String apiType = json.get("apiType").getAsString();
                if("location".equals(apiType)) {
                    final JsonObject response = json.getAsJsonObject("response");
                    if(!response.has("worldName"))return;
                    final String worldName = response.get("worldName").getAsString();
                    if(worldName.equals("thelow")) {
                        MarkerRenderer.marker_enable = true;
                    }else {
                        MarkerRenderer.marker_enable = false;
                    }
                }
                if("player_status".equals(apiType)) {
                    final JsonObject response = json.getAsJsonObject("response");
                    if(!response.has("mcid"))return;
                    final String mcid = response.get("mcid").getAsString();
                    final String my_mcid = mc.thePlayer.getName();
                    if(mcid.equals(my_mcid)) {
                        if(!response.has("clanInfo")) {
                        	isClantp=false;
                        	return;
                        }
                        final JsonObject clanInfo = response.get("clanInfo").getAsJsonObject();
                        if(!clanInfo.has("clanRank")) {
                        	isClantp = false;
                        	return;
                        }
                        final String clanRank = clanInfo.get("clanRank").getAsString();
                        if("UNRANKED".equals(clanRank)||clanRank.startsWith("IRON")||clanRank.startsWith("GOLD")||clanRank.startsWith("LAPIS")||clanRank.startsWith("EMERALD")||clanRank.startsWith("REDSTONE")||clanRank.startsWith("DIAMOND")) {
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

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void APIcancel(ClientChatReceivedEvent event) {
        final String message = event.message.getUnformattedText(); // 色コードや装飾を除去したテキスト
        final String colormessage = event.message.getFormattedText();

        
        if((colormessage.startsWith("§r§a")||colormessage.startsWith("§r§b[NEW RECORDING] §r§a"))&&message.contains("の攻略時間")) {
        	final Matcher matcher = Pattern.compile("§r§a(.*)の攻略時間").matcher(colormessage);
        	if (matcher.find()) {
                // (.*) にマッチした部分（キャプチャグループの1番目）がダンジョン名
        		cleardDungeonName = matcher.group(1);
            }else {
            	final Matcher NewRecordmatcher = Pattern.compile("§r§b[NEW RECORDING] §r§a(.*)§r§aの攻略時間 ").matcher(colormessage);
            	if(NewRecordmatcher.find()) {
            		cleardDungeonName = NewRecordmatcher.group(1);
            	}
            }
        }
        
        if (message.startsWith("$api")) {
            event.setCanceled(true); // この行で表示をキャンセル
        }
    }
    
    @SubscribeEvent
    public void onJoinWorld(EntityJoinWorldEvent event) {
        if (event.entity == mc.thePlayer&&can_cmd_send) {
            can_cmd_send=false;
        }
    }
    
    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START)return;//TickEventはSTARTとENDの2回発火するので1回にする
        if(SendVersionTimer>0) {
        	SendVersionTimer--;
        	if(SendVersionTimer==0) {
        		SendVersionText();
        	}
        }
        if(!MarkerRenderer.IsThereMarker()) {
        	TickTimer = 0;
        }
        final EntityPlayer player = mc.thePlayer;
        TickTimer++;//1ずつ加算

        if(TickTimer/INTERVAL == 10) {
        	TickTimer = 0;
            if (player != null) {
            	//コマンド送信
                mc.thePlayer.sendChatMessage("/thelow_api player");
            }
        }
        if(!can_cmd_send) {
        	cmd_ct++;
        	if(cmd_ct%200==0) {
        		can_cmd_send=true;
        		cmd_ct=0;
        	}
        }
    }
    
    private static void sendchat(final String text,final EntityPlayerSP thePlayer) {
    	if (Minecraft.getMinecraft().thePlayer != null) {
            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(text));
        }
    }
    
    private static void sendClickableLink(final String url) {
    	
        IChatComponent component = new ChatComponentText(url);

        ChatStyle style = new ChatStyle();
        
        ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.OPEN_URL, url);
        style.setChatClickEvent(clickEvent);
        
        style.setColor(EnumChatFormatting.AQUA); // 水色にする
        style.setUnderlined(true); // 下線を引く

        component.setChatStyle(style);

        if (Minecraft.getMinecraft().thePlayer != null) {
            Minecraft.getMinecraft().thePlayer.addChatMessage(component);
        }
    }
    
    private static void SendVersionText() {
		if(thelow_quest_helper.latestver.equals(""))return;
		version_Checked = true;
		int status = thelow_quest_helper.the_status;
		if(thelow_quest_helper.CustomMsg!=null&&!thelow_quest_helper.CustomMsg.equals("")&&!thelow_quest_helper.CustomMsg.equals("OK")) {
			sendchat("§a[thelow_quest_helper]" + thelow_quest_helper.CustomMsg,mc.thePlayer);
		}
		if(status==-1)return;
		switch (status){
			case 0:{//安定バージョン
				sendchat("§a[thelow_quest_helper]§7新バージョンが利用可能です"+thelow_quest_helper.VERSION_STRING+"→"+thelow_quest_helper.latestver,mc.thePlayer);
				sendClickableLink("https://github.com/WagglyZebra9743/thelow_quest_helper/releases/latest");
				return;
			}
			case 1:{//特殊な使い方をすると不具合が出る
				sendchat("§a[thelow_quest_helper]§e軽微な不具合があるバージョンです",mc.thePlayer);
				sendchat("§e新バージョンが利用可能です"+thelow_quest_helper.VERSION_STRING+"→"+thelow_quest_helper.latestver,mc.thePlayer);
				sendClickableLink("https://github.com/WagglyZebra9743/thelow_quest_helper/releases/latest");
				return;
			}
			case 2:{//人によっては表示が崩れる等の不具合が出る
				sendchat("§a[thelow_quest_helper]§6中程度な不具合があるバージョンです",mc.thePlayer);
				sendchat("§6新バージョンが利用可能です"+thelow_quest_helper.VERSION_STRING+"→"+thelow_quest_helper.latestver,mc.thePlayer);
				sendClickableLink("https://github.com/WagglyZebra9743/thelow_quest_helper/releases/latest");
				return;
			}
			case 3:{//不具合が出るしクラッシュ等も起きる
				sendchat("§a[thelow_quest_helper]§c重大な不具合があるバージョンです",mc.thePlayer);
				sendchat("§c新バージョンに更新することを推奨します"+thelow_quest_helper.VERSION_STRING+"→"+thelow_quest_helper.latestver,mc.thePlayer);
				sendClickableLink("https://github.com/WagglyZebra9743/thelow_quest_helper/releases/latest");
				return;
			}
		}
    }
}
