package com.thelow_quest_helper.thelow_quest_helper;


import com.thelow_quest_helper.thelow_quest_helper.chat.APIListener;
import com.thelow_quest_helper.thelow_quest_helper.chat.TitleInterceptor;
import com.thelow_quest_helper.thelow_quest_helper.commands.quest_helper_cmd;
import com.thelow_quest_helper.thelow_quest_helper.item.ItemHoverTracker;
import com.thelow_quest_helper.thelow_quest_helper.item.Keyclick;
import com.thelow_quest_helper.thelow_quest_helper.item.MarkerRenderer;

import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

@Mod(modid = "thelow_quest_helper", version = "1.3")
public class thelow_quest_helper {
	public static boolean enable = true;

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        // イベントハンドラ登録
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(new ItemHoverTracker());
        MinecraftForge.EVENT_BUS.register(new Keyclick());
        MinecraftForge.EVENT_BUS.register(new MarkerRenderer());
        MinecraftForge.EVENT_BUS.register(new APIListener());
        MinecraftForge.EVENT_BUS.register(new TitleInterceptor());
        ClientCommandHandler.instance.registerCommand(new quest_helper_cmd());
    }
}