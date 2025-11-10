package com.thelow_quest_helper.thelow_quest_helper.config;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

public class thelow_quest_helperConfig {

    private static Configuration config;

    public static final String CATEGORY_GENERAL = "general";
    private static final int DEFAULT_CLANQUESTHUDX = 5;
    private static final int DEFAULT_CLANQUESTHUDY = 20;
    private static final String DEFAULT_CLANQUESTHUDX_TEXT = "クランクエストを開いた時の表示するX座標";
    private static final String DEFAULT_CLANQUESTHUDY_TEXT = "クランクエストを開いた時の表示するY座標";
    private static final String DEFAULT_CLANQUESTHUDENABLE_TEXT = "クランクエストを開いた時の表示を変えるかどうか";
    private static final String DEFAULT_VERSIONCHECK_TEXT = "バージョン情報を自動で確認するかどうか";
    private static final String DEFAULT_SENDMCID_TEXT = "バージョン確認時にmcidを送信するか";

    public static int ClanQuestHUDX = 5;
    public static int ClanQuestHUDY = 5;
    public static boolean ClanQuestHUDenable = true;
    public static boolean AutoVersionCheck = true;
    public static boolean SendMCID = false;

    public static void loadConfig(File configFile) {
        config = new Configuration(configFile);
        config.load();

        ClanQuestHUDX = config.get(CATEGORY_GENERAL, "ClanQuestHUDX", DEFAULT_CLANQUESTHUDX, DEFAULT_CLANQUESTHUDX_TEXT).getInt();
        ClanQuestHUDY = config.get(CATEGORY_GENERAL, "ClanQuestHUDY", DEFAULT_CLANQUESTHUDY, DEFAULT_CLANQUESTHUDY_TEXT).getInt();
        ClanQuestHUDenable = config.get(CATEGORY_GENERAL, "ClanQuestHUDenable", true, DEFAULT_CLANQUESTHUDENABLE_TEXT).getBoolean();
        AutoVersionCheck = config.get(CATEGORY_GENERAL, "AutoVersionCheck", true, DEFAULT_VERSIONCHECK_TEXT).getBoolean();
        SendMCID = config.get(CATEGORY_GENERAL, "SendMCID", false, DEFAULT_SENDMCID_TEXT).getBoolean();

        if (config.hasChanged()) {
            config.save();
        }
    }

    public static void save() {
        config.get(CATEGORY_GENERAL, "ClanQuestHUDY", DEFAULT_CLANQUESTHUDX, DEFAULT_CLANQUESTHUDX_TEXT).set(ClanQuestHUDY);
        config.get(CATEGORY_GENERAL, "ClanQuestHUDY", DEFAULT_CLANQUESTHUDY, DEFAULT_CLANQUESTHUDY_TEXT).set(ClanQuestHUDY);
        config.get(CATEGORY_GENERAL, "ClanQuestHUDenable", true,DEFAULT_CLANQUESTHUDENABLE_TEXT).set(ClanQuestHUDenable);
        config.get(CATEGORY_GENERAL, "AutoVersionCheck", true,DEFAULT_VERSIONCHECK_TEXT).set(AutoVersionCheck);
        config.get(CATEGORY_GENERAL, "SendMCID", false,DEFAULT_SENDMCID_TEXT).set(SendMCID);

        
        if (config.hasChanged()) {
            config.save();
        }
    }
    
    public static Configuration getConfig() {
        return config;
    }
    public static int GetClanQuestHUDplace(String type) {
    	switch(type) {
    		case "X":
    			return DEFAULT_CLANQUESTHUDX;
    		case "Y":
    			return DEFAULT_CLANQUESTHUDY;
    	}
    	return 0;
    }
}