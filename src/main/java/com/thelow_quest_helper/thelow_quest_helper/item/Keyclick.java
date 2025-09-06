package com.thelow_quest_helper.thelow_quest_helper.item;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class Keyclick {
	Minecraft mc = Minecraft.getMinecraft();
	private static boolean clicked = false;
    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        // GUIが開いていないときは無視
        if(!(mc.currentScreen instanceof GuiContainer)) {
        	ItemHoverTracker.lastLore = null;
        	return;
        }

        // Wキーが押されているか？
        if(Keyboard.isKeyDown(Keyboard.KEY_W)&&!clicked) {
            List<String> lastLore = ItemHoverTracker.lastLore;
            if (lastLore == null) return;

            for (String line : lastLore) {
                String clean = line.replaceAll("§.", "").trim();
                if(clean.contains("地上世界")) {
                	Matcher matcher = Pattern.compile("\\((-?[0-9.]+), (-?[0-9.]+), (-?[0-9.]+)\\)").matcher(clean);
                    if (matcher.find()) {
                        double x = Double.parseDouble(matcher.group(1));
                        double y = Double.parseDouble(matcher.group(2));
                        double z = Double.parseDouble(matcher.group(3));
                        
                        String questname = ItemHoverTracker.lastQuestname;
                        String NPCname = ItemHoverTracker.lastNPCname;
                        
                        MarkerRenderer.clearMarkers();
                        MarkerRenderer.addMarker(x,y,z, questname+"\\n"+NPCname);
                        mc.thePlayer.addChatMessage(new ChatComponentText("§a[thelow_quest_helper]§7マーカーを設置しました§e("+x+","+y+","+z+")"));
                        clicked = true;
                        ItemHoverTracker.lastLore = null;
                        String info = Town.getNearestTownInfo(x, y, z);
                        mc.thePlayer.addChatMessage(new ChatComponentText(info));
                        break; // 複数見つけた場合は最初だけ
                    }
                }else if(clean.contains("ダンジョン[")&&clean.contains("]を攻略しよう")) {
                	String dungeonName = clean.split("\\[")[1].split("]")[0];
                	Dungeon d = Dungeon.getDungeonByName(dungeonName);
    				if(d==null||d.x==null||d.y==null||d.z==null)return;
    				MarkerRenderer.clearMarkers();
    				MarkerRenderer.addMarker(d.x,d.y,d.z,d.name);
    				String info = Town.getNearestTownInfo(d.x, d.y, d.z);
    				mc.thePlayer.addChatMessage(new ChatComponentText("§a[thelow_quest_helper]§7マーカーを設置しました§e("+d.x+","+d.y+","+d.z+")"));
    				Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(info));
    				clicked = true;
    				ItemHoverTracker.lastLore = null;
    				
                }else if(clean.contains("攻略する")) {
                	String dungeonName = clean.split("を")[0];
                	Dungeon d = Dungeon.getDungeonByName(dungeonName);
                	if(d==null||d.x==null||d.y==null||d.z==null)return;
    				MarkerRenderer.clearMarkers();
    				MarkerRenderer.addMarker(d.x,d.y,d.z,d.name);
    				String info = Town.getNearestTownInfo(d.x, d.y, d.z);
    				mc.thePlayer.addChatMessage(new ChatComponentText("§a[thelow_quest_helper]§7マーカーを設置しました§e("+d.x+","+d.y+","+d.z+")"));
    				Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(info));
    				clicked = true;
    				ItemHoverTracker.lastLore = null;
                }
            }
            
        }else if(!Keyboard.isKeyDown(Keyboard.KEY_W)) {
        	clicked = false;
        }
    }
}