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
    private static final String DEFAULT_LONGQUESTENABLE_TEXT = "長いクエストの進行状況を表示するか";
    private static final int DEFAULT_LONGQUESTHUDX = 5;
    private static final int DEFAULT_LONGQUESTHUDY = 20;
    private static final String DEFAULT_LONGQUESTHUDX_TEXT = "クエスト進行状況表示X座標";
    private static final String DEFAULT_LONGQUESTHUDY_TEXT = "クエスト進行状況表示Y座標";
    private static final String DEFAULT_VERSIONCHECK_TEXT = "バージョン情報を自動で確認するかどうか";
    private static final String DEFAULT_SENDMCID_TEXT = "バージョン確認時にmcidを送信するか";

    public static int ClanQuestHUDX = 5;
    public static int ClanQuestHUDY = 20;
    public static boolean ClanQuestHUDenable = true;
    public static boolean LongQuestEnable = true;
    public static int LongQuestHUDX = 20;
    public static int LongQuestHUDY = 40;
    public static boolean AutoVersionCheck = true;
    public static boolean SendMCID = false;

    public static void loadConfig(File configFile) {
        config = new Configuration(configFile);
        config.load();

        ClanQuestHUDX = config.get(CATEGORY_GENERAL, "ClanQuestHUDX", DEFAULT_CLANQUESTHUDX, DEFAULT_CLANQUESTHUDX_TEXT).getInt();
        ClanQuestHUDY = config.get(CATEGORY_GENERAL, "ClanQuestHUDY", DEFAULT_CLANQUESTHUDY, DEFAULT_CLANQUESTHUDY_TEXT).getInt();
        ClanQuestHUDenable = config.get(CATEGORY_GENERAL, "ClanQuestHUDenable", true, DEFAULT_CLANQUESTHUDENABLE_TEXT).getBoolean();
        LongQuestEnable = config.get(CATEGORY_GENERAL, "LongQuestEnable", true, DEFAULT_LONGQUESTENABLE_TEXT).getBoolean();
        LongQuestHUDX = config.get(CATEGORY_GENERAL, "LongQuestHUDX", DEFAULT_LONGQUESTHUDX,DEFAULT_LONGQUESTHUDX_TEXT ).getInt();
        LongQuestHUDY = config.get(CATEGORY_GENERAL, "LongQuestHUDY", DEFAULT_LONGQUESTHUDY,DEFAULT_LONGQUESTHUDY_TEXT ).getInt();
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
        config.get(CATEGORY_GENERAL, "LongQuestEnable", true, DEFAULT_LONGQUESTENABLE_TEXT).set(LongQuestEnable);
        config.get(CATEGORY_GENERAL, "LongQuestHUDX", DEFAULT_LONGQUESTHUDX,DEFAULT_LONGQUESTHUDX_TEXT ).set(LongQuestHUDX);
        config.get(CATEGORY_GENERAL, "LongQuestHUDY", DEFAULT_LONGQUESTHUDY,DEFAULT_LONGQUESTHUDY_TEXT ).set(LongQuestHUDY);
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
    public static int GetLongQuestHUDplace(String type) {
    	switch(type) {
		case "X":
			return DEFAULT_LONGQUESTHUDX;
		case "Y":
			return DEFAULT_LONGQUESTHUDY;
	}
	return 0;
}
}