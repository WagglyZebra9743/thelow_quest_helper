package com.thelow_quest_helper.thelow_quest_helper.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.thelow_quest_helper.thelow_quest_helper.LongQuest.LongQuest;
import com.thelow_quest_helper.thelow_quest_helper.LongQuest.LongQuestMarker;
import com.thelow_quest_helper.thelow_quest_helper.config.thelow_quest_helperConfig;
import com.thelow_quest_helper.thelow_quest_helper.item.MarkerRenderer;

import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

public class quest_helper_cmd extends CommandBase {
	
	private static final Minecraft mc = Minecraft.getMinecraft();
	
    public static void register(FMLServerStartingEvent event) {
        event.registerServerCommand(new quest_helper_cmd());
    }

    @Override
    public String getCommandName() {
        return "quest_helper";
    }
    
    @Override
    public List<String> getCommandAliases() {
    	return Collections.singletonList("qh");
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/quest_helper <clearmarker/reload/clanhud/longhud/longreset/help>";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length == 0) {
        	sendmsg("§c使用方法: /quest_helper <clearmarker/reload/clanhud/longhud/longreset/help> ",sender);
            return;
        }
        final String sub = args[0];

        switch (sub.toLowerCase()) {
        	case "clearmarker":{
        		MarkerRenderer.clearMarkers();
        		LongQuestMarker.clearMarkers();
        		sendmsg("§a[thelow_quest_helper]§7マーカーを削除しました" , sender);
        		break;
        	}
        	
        	case "reload":{
        		mc.thePlayer.sendChatMessage("/thelow_api location");
        		sendmsg("§a[thelow_quest_helper]§7APIコマンドを送信しました" , sender);
        		break;
        	}
        	
            case "clanhud":
            	if(args.length<3) {
            		sendmsg("§c[thelow_quest_helper]使用方法:/ch clanhud <x> <y>",sender);
            		return;
            	}
            	try {
                    int x = Integer.parseInt(args[1]);
                    int y = Integer.parseInt(args[2]);

                    sendmsg("§a[thelow_quest_helper]§7表示位置を("+thelow_quest_helperConfig.ClanQuestHUDX+","+thelow_quest_helperConfig.ClanQuestHUDY+")から§e(" + x + ", " + y + ") §7に変更しました",sender);
                    thelow_quest_helperConfig.ClanQuestHUDX = x;
                    thelow_quest_helperConfig.ClanQuestHUDY = y;
                    thelow_quest_helperConfig.save();
                } catch (Exception  e) {
                    sendmsg("§c[thelow_quest_helper]使用方法:/ch clanhud <x> <y>",sender);
                    sendmsg("§7現在の表示位置は§e("+thelow_quest_helperConfig.ClanQuestHUDX+","+thelow_quest_helperConfig.ClanQuestHUDY+")§7です",sender);
                }
            	break;
            	
            case "longhud":
            	if(args.length<3) {
            		sendmsg("§c[thelow_quest_helper]使用方法:/ch longhud <x> <y>",sender);
            		return;
            	}
            	try {
                    int x = Integer.parseInt(args[1]);
                    int y = Integer.parseInt(args[2]);

                    sendmsg("§a[thelow_quest_helper]§7表示位置を("+thelow_quest_helperConfig.LongQuestHUDX+","+thelow_quest_helperConfig.LongQuestHUDY+")から§e(" + x + ", " + y + ") §7に変更しました",sender);
                    thelow_quest_helperConfig.LongQuestHUDX = x;
                    thelow_quest_helperConfig.LongQuestHUDY = y;
                    thelow_quest_helperConfig.save();
                } catch (Exception  e) {
                    sendmsg("§c[thelow_quest_helper]使用方法:/ch longhud <x> <y>",sender);
                    sendmsg("§7現在の表示位置は§e("+thelow_quest_helperConfig.LongQuestHUDX+","+thelow_quest_helperConfig.LongQuestHUDY+")§7です",sender);
                }
            	break;
        	
            case "longreset":
            	LongQuest.ClearPhaseStats();
            	sendmsg("§a[thelow_quest_helper]§7ストーリークエスト情報をリセットしました",sender);
            	break;
            	
            case "help":{
            	sendmsg("§a===thelow_quest_helperコマンド一覧===" , sender);
            	sendmsg("§7/quest_helper clearmarker - マーカーを削除する",sender);
            	sendmsg("§7/quest_helper reload - APIコマンドを送信して、情報を取得する",sender);
            	sendmsg("§7/quest_helper <clanhud/longhud> - クランクエスト、ストーリークエストのhudの位置を調整",sender);
            	sendmsg("§7/quest_helper longreset - ストーリークエストの進行状態をリセット",sender);
            	sendmsg("§7/quest_helper help - この画面を表示",sender);
            	break;
            }
            
            default:
                sendmsg("§c不明なコマンドです。/quest_helper help でヘルプを表示します。",sender);
                break;
        }

        
    }
    
    @Override
    public boolean canCommandSenderUseCommand(final ICommandSender sender) {
        return true;
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return false;
    }

    @Override
    public int compareTo(ICommand o) {
        return 0;
    }

    @Override
    public List<String> addTabCompletionOptions(final ICommandSender sender, final String[] args, BlockPos pos) {
        if (args.length == 1) {
            List<String> options = new ArrayList<>();
            options.add("clearmarker");
            options.add("reload");
            options.add("clanhud");
            options.add("longhud");
            options.add("longreset");
            options.add("help");
            return getListOfStringsMatchingLastWord(args, options.toArray(new String[0]));
        }
        return null;
    }
    
    private void sendmsg(final String msg , final ICommandSender sender) {
    	sender.addChatMessage(new ChatComponentText(msg));
    }
}