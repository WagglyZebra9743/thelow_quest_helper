package com.thelow_quest_helper.thelow_quest_helper.commands;

import java.util.ArrayList;
import java.util.List;

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
    public String getCommandUsage(ICommandSender sender) {
        return "/quest_helper <clearmarker/reload/help>";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length == 0) {
        	sendmsg("§c使用方法: /quest_helper <clearmarker/reload/help> ",sender);
            return;
        }
        String sub = args[0];

        switch (sub.toLowerCase()) {
        	case "clearmarker":{
        		MarkerRenderer.clearMarkers();
        		sendmsg("§a[thelow_quest_helper]§7マーカーを削除しました" , sender);
        		break;
        	}
        	
        	case "reload":{
        		mc.thePlayer.sendChatMessage("/thelow_api location");
        		sendmsg("§a[thelow_quest_helper]§7APIコマンドを送信しました" , sender);
        		break;
        	}
        	
            case "help":{
            	sendmsg("§a===thelow_quest_helperコマンド一覧===" , sender);
            	sendmsg("§7/quest_helper clearmarker - マーカーを削除する",sender);
            	sendmsg("§7/quest_helper reload - APIコマンドを送信して、情報を取得する",sender);
            	sendmsg("§7/quest_helper help - この画面を表示",sender);
            	break;
            }
            
            default:
                sendmsg("§c不明なコマンドです。/quest_helper help でヘルプを表示します。",sender);
                break;
        }

        
    }
    
    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
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
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        if (args.length == 1) {
            List<String> options = new ArrayList<>();
            options.add("clearmarker");
            options.add("reload");
            options.add("help");
            return getListOfStringsMatchingLastWord(args, options.toArray(new String[0]));
        }
        return null;
    }
    
    private static void sendmsg(String msg , ICommandSender sender) {
    	sender.addChatMessage(new ChatComponentText(msg));
    }
}