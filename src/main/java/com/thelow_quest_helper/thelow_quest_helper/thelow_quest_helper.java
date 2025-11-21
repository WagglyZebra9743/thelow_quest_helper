package com.thelow_quest_helper.thelow_quest_helper;


import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.thelow_quest_helper.thelow_quest_helper.chat.APIListener;
import com.thelow_quest_helper.thelow_quest_helper.chat.TitleInterceptor;
import com.thelow_quest_helper.thelow_quest_helper.clanquest.ClanQuestHUD;
import com.thelow_quest_helper.thelow_quest_helper.commands.quest_helper_cmd;
import com.thelow_quest_helper.thelow_quest_helper.config.thelow_quest_helperConfig;
import com.thelow_quest_helper.thelow_quest_helper.item.ItemHoverTracker;
import com.thelow_quest_helper.thelow_quest_helper.item.Keyclick;
import com.thelow_quest_helper.thelow_quest_helper.item.MarkerRenderer;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = "thelow_quest_helper", version = "1.5",guiFactory = "com.thelow_quest_helper.thelow_quest_helper.config.GuiFactory")
public class thelow_quest_helper {
	public static boolean enable = true;
	public static String MOD_ID;
    public static String VERSION_STRING;
    private static String key = "";
    private static String API_URL = "https://script.google.com/macros/s/"+key+"/exec";
    public static String latestver = "";
    public static int the_status = -1;
    public static String CustomMsg = "";

	@Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        File configFile = event.getSuggestedConfigurationFile();
        thelow_quest_helperConfig.loadConfig(configFile);
        //  イベントから ModMetadata を取得
        ModMetadata meta = event.getModMetadata();
        
        // modid を取得
        MOD_ID = meta.modId;
        
        //  version を文字列として取得
        VERSION_STRING = meta.version;
        List<String> keys = meta.authorList;
        if (keys != null && !keys.isEmpty()) {
            key = keys.get(0);
        }
        checkModVersion();
    }
	
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        // イベントハンドラ登録
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(new ItemHoverTracker());
        MinecraftForge.EVENT_BUS.register(new Keyclick());
        MinecraftForge.EVENT_BUS.register(new MarkerRenderer());
        MinecraftForge.EVENT_BUS.register(new APIListener());
        MinecraftForge.EVENT_BUS.register(new TitleInterceptor());
        MinecraftForge.EVENT_BUS.register(new ClanQuestHUD());
        ClientCommandHandler.instance.registerCommand(new quest_helper_cmd());
    }
    
    public void checkModVersion() {
    	
    	if(!thelow_quest_helperConfig.AutoVersionCheck)return;
    	key = APIkey.getKey(key);
        // ネットワーク処理は重たいため、必ず別スレッドで実行する
        new Thread(() -> {
            
            System.out.println("[thelow_quest_helper]最新バージョンの確認します...");
            VersionResponse apiResponse = getVersionData();

            if (apiResponse == null) {
                System.out.println("[thelow_quest_helper]バージョンのチェックに失敗しました");
                return;
            }

            latestver = apiResponse.latestVersion;
            the_status = apiResponse.currentStatus;
            CustomMsg = apiResponse.CustomMsg;

            System.out.println("[thelow_quest_helper]this_version: "+ VERSION_STRING+",status: " + the_status +",latest: " + latestver);
            
        }).start();
    }
    
    
    private static VersionResponse getVersionData() {
        HttpURLConnection conn = null; // finallyで閉じるために外で宣言
        String mcid = "no";
        if(thelow_quest_helperConfig.SendMCID&&Minecraft.getMinecraft().getSession().getUsername()!=null) {
        	mcid = Minecraft.getMinecraft().getSession().getUsername();
        }
        
        try {
        	API_URL = "https://script.google.com/macros/s/"+key+"/exec";
            final String urlStr = API_URL + "?action=get&modid=" + thelow_quest_helper.MOD_ID + "&version=" + thelow_quest_helper.VERSION_STRING + "&mcid=" + mcid;
            final URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            // レスポンスコードを確認 (200 OK 以外はエラーとして扱う)
            int responseCode = conn.getResponseCode();
            String responsemsg = conn.getResponseMessage();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                System.out.println("[thelow_quest_helper]レスポンス: " + responseCode+":"+responsemsg);
                return null; // 失敗
            }

            // --- レスポンスの読み取り ---
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder result = new StringBuilder();
            String line;

            while ((line = in.readLine()) != null) {
                result.append(line);
            }
            in.close();

            // --- ★ ここからがJSONパース処理 ---
            String jsonString = result.toString();
            if (jsonString.isEmpty()) {
                System.out.println("[thelow_quest_helper]APIレスポンスが空でした。");
                return null;
            }

            // Gsonを使ってJSON文字列を VersionResponse オブジェクトに変換
            Gson gson = new Gson();
            VersionResponse response = gson.fromJson(jsonString, VersionResponse.class);

            return response; // ★ パースしたオブジェクトを返す

        } catch (JsonSyntaxException e) {
            // JSONの形式が間違っていた場合 (例: GASがエラーHTMLを返した)
            System.out.println("[thelow_quest_helper]JSONのパースに失敗しました。");
            e.printStackTrace();
            return null; // 失敗
            
        } catch (Exception e) {
            // タイムアウト、URL間違い、ネットワーク接続なし など
            System.out.println("[thelow_quest_helper]バージョンチェック中に例外が発生しました。");
            e.printStackTrace();
            return null; // 失敗
            
        } finally {
            // 成功しても失敗しても、必ず接続を閉じる
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}

class VersionResponse {
    public String latestVersion;
    public int currentStatus;
    public int nextStatus;
    public String CustomMsg;
}
