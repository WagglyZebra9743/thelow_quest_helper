package com.thelow_quest_helper.thelow_quest_helper.config;

import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;

public class thelow_quest_helperGuiConfig extends GuiConfig {
	
    public thelow_quest_helperGuiConfig(GuiScreen parentScreen) {
        super(parentScreen,
                getConfigElements(),
                "thelow_quest_helper", // modid
                false,
                false,
                "thelow_quest_helper Config");
    }

    private static List<IConfigElement> getConfigElements() {
        return new ConfigElement(thelow_quest_helperConfig.getConfig().getCategory(thelow_quest_helperConfig.CATEGORY_GENERAL))
                .getChildElements();
    }
    
    @Override
    public void onGuiClosed() {
        super.onGuiClosed();

        // GUIで変更された値を ConfigHandler のフィールドに反映
        thelow_quest_helperConfig.ClanQuestHUDX = thelow_quest_helperConfig.getConfig().get(thelow_quest_helperConfig.CATEGORY_GENERAL, "ClanQuestHUDX", thelow_quest_helperConfig.GetClanQuestHUDplace("X")).getInt();
        thelow_quest_helperConfig.ClanQuestHUDY = thelow_quest_helperConfig.getConfig().get(thelow_quest_helperConfig.CATEGORY_GENERAL, "ClanQuestHUDY", thelow_quest_helperConfig.GetClanQuestHUDplace("Y")).getInt();
        thelow_quest_helperConfig.ClanQuestHUDenable = thelow_quest_helperConfig.getConfig().get(thelow_quest_helperConfig.CATEGORY_GENERAL, "ClanQuestHUDenable", true).getBoolean();
        thelow_quest_helperConfig.LongQuestHUDX = thelow_quest_helperConfig.getConfig().get(thelow_quest_helperConfig.CATEGORY_GENERAL, "LongQuestHUDX", thelow_quest_helperConfig.GetLongQuestHUDplace("X")).getInt();
        thelow_quest_helperConfig.LongQuestHUDY = thelow_quest_helperConfig.getConfig().get(thelow_quest_helperConfig.CATEGORY_GENERAL, "LongQuestHUDY", thelow_quest_helperConfig.GetLongQuestHUDplace("Y")).getInt();
        thelow_quest_helperConfig.LongQuestEnable = thelow_quest_helperConfig.getConfig().get(thelow_quest_helperConfig.CATEGORY_GENERAL, "LongQuestEnable", true).getBoolean();
        thelow_quest_helperConfig.AutoVersionCheck = thelow_quest_helperConfig.getConfig().get(thelow_quest_helperConfig.CATEGORY_GENERAL, "AutoVersionCheck", true).getBoolean();
        thelow_quest_helperConfig.SendMCID = thelow_quest_helperConfig.getConfig().get(thelow_quest_helperConfig.CATEGORY_GENERAL, "SendMCID", false).getBoolean();

        // 反映された値をファイルに保存
        thelow_quest_helperConfig.save();
        
    }
}
