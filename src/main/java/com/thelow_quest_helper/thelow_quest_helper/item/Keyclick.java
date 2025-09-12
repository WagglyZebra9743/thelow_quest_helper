package com.thelow_quest_helper.thelow_quest_helper.item;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class Keyclick {
	Minecraft mc = Minecraft.getMinecraft();
	private static boolean wkeyClicked = false;
	private static boolean zkeyClicked = false;
    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        // GUIが開いていないときは無視
        if(!(mc.currentScreen instanceof GuiContainer)) {
        	ItemHoverTracker.lastLore = null;
        	return;
        }

        // Wキーが押されているか？
        if(Keyboard.isKeyDown(Keyboard.KEY_W)&&!wkeyClicked) {
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
                        wkeyClicked = true;
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
    				wkeyClicked = true;
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
    				wkeyClicked = true;
    				ItemHoverTracker.lastLore = null;
                }
            }
            
        }else if(Keyboard.isKeyDown(Keyboard.KEY_Z)&&!zkeyClicked) {
            List<String> lastLore = ItemHoverTracker.lastLore;
            if (lastLore == null) return;

            for (String line : lastLore) {
                String clean = line.replaceAll("§.", "").trim();
                
                int x1 = getPlayerBlockPos().getX(), y1 = getPlayerBlockPos().getY(), z1 = getPlayerBlockPos().getZ();
                String[] routeInfo = null;
                BestTeleport_mtb routeCalculator = new BestTeleport_mtb();
                
                
                if(clean.contains("地上世界")) {
                	Matcher matcher = Pattern.compile("\\((-?[0-9.]+), (-?[0-9.]+), (-?[0-9.]+)\\)").matcher(clean);
                    if (matcher.find()) {
                        int x = Integer.parseInt(matcher.group(1));
                        int y = Integer.parseInt(matcher.group(2));
                        int z = Integer.parseInt(matcher.group(3));
                        
                        String questname = ItemHoverTracker.lastQuestname;
                        String NPCname = ItemHoverTracker.lastNPCname;
                        
                        routeInfo = routeCalculator.getBestRoute(x1, y1, z1, x, y, z, true);
                        
                        zkeyClicked = true;
                        ItemHoverTracker.lastLore = null;
                        
                        mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW + "> " + routeInfo[3]));
                    	NextLocationBeacon.startGuiding(routeInfo[0], routeInfo[1], routeInfo[2], x, y, z);
                        
                        break; // 複数見つけた場合は最初だけ
                    }
                }else if(clean.contains("ダンジョン[")&&clean.contains("]を攻略しよう")) {
                	String dungeonName = clean.split("\\[")[1].split("]")[0];
                	Dungeon d = Dungeon.getDungeonByName(dungeonName);
    				if(d==null||d.x==null||d.y==null||d.z==null)return;
    				
    				zkeyClicked = true;
    				ItemHoverTracker.lastLore = null;
    				
    				routeInfo = routeCalculator.getBestRoute(x1, y1, z1, (int)Math.round(d.x), (int)Math.round(d.y), (int)Math.round(d.z), true);
    				mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW + "> " + routeInfo[3]));
                	NextLocationBeacon.startGuiding(routeInfo[0], routeInfo[1], routeInfo[2], (int)Math.round(d.x), (int)Math.round(d.y), (int)Math.round(d.z));
                	
                	
                }else if(clean.contains("攻略する")) {
                	String dungeonName = clean.split("を")[0];
                	Dungeon d = Dungeon.getDungeonByName(dungeonName);
                	if(d==null||d.x==null||d.y==null||d.z==null)return;
    				
    				zkeyClicked = true;
    				ItemHoverTracker.lastLore = null;

    				routeInfo = routeCalculator.getBestRoute(x1, y1, z1, (int)Math.round(d.x), (int)Math.round(d.y), (int)Math.round(d.z), true);
    				mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW + "> " + routeInfo[3]));
                	NextLocationBeacon.startGuiding(routeInfo[0], routeInfo[1], routeInfo[2], (int)Math.round(d.x), (int)Math.round(d.y), (int)Math.round(d.z));
                }
            }
            
            
            
        }else if(!Keyboard.isKeyDown(Keyboard.KEY_W)) {
        	wkeyClicked = false;
        	zkeyClicked = false;
        }
    }
    
    public static BlockPos getPlayerBlockPos() {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        if (player != null) {
            return player.getPosition();
        }
        return null;
    }
    
    
}