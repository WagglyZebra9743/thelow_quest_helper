package com.thelow_quest_helper.thelow_quest_helper.item;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.lwjgl.input.Keyboard;

import com.thelow_quest_helper.thelow_quest_helper.chat.APIListener;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class Keyclick {
	Minecraft mc = Minecraft.getMinecraft();
	//クリックしたことを記録するための変数
	private static boolean wkeyClicked = false;
	private static boolean zkeyClicked = false;
	//目的地の名前を別のクラスからも呼べるようにpublic
	public static String goalname = "";
    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        // GUIが開いていないときは無視
        if(!(mc.currentScreen instanceof GuiContainer)) {
        	ItemHoverTracker.lastLore = null;
        	return;
        }

        // Wキーが押されたときの処理
        if(Keyboard.isKeyDown(Keyboard.KEY_W)&&!wkeyClicked) {
        	//loreを取得する
            List<String> lastLore = ItemHoverTracker.lastLore;
            if (lastLore == null) return;

            for (String line : lastLore) {
            	//loreを一行ずつ取得してカラーコードを除去して空白を削除する
                String clean = line.replaceAll("§.", "").trim();
                //NPCの座標が与えられたとき
                if(clean.contains("地上世界")) {
                	//まず座標を(x,y,z)の形式であると仮定して取得するのを試みる
                	Matcher matcher = Pattern.compile("\\((-?[0-9.]+), (-?[0-9.]+), (-?[0-9.]+)\\)").matcher(clean);
                	//見つけた場合座標をそれぞれdouble(実数)として取得する
                    if (matcher.find()) {
                        double x = Double.parseDouble(matcher.group(1));
                        double y = Double.parseDouble(matcher.group(2));
                        double z = Double.parseDouble(matcher.group(3));
                        
                        //クエストの名前とNPCの名前を取得する
                        String questname = ItemHoverTracker.lastQuestname;
                        String NPCname = ItemHoverTracker.lastNPCname;
                        
                        //目的地に接近したときのメッセージを出すために保存
                        goalname = NPCname;
                        
                        //マーカーをリセットした後に目的地マーカーを設置する
                        MarkerRenderer.clearMarkers();
                        MarkerRenderer.addMarker(x,y,z, questname+"\\n"+NPCname);
                        sendchat("§a[thelow_quest_helper]§7マーカーを設置しました§e("+x+","+y+","+z+")", mc.thePlayer);
                        
                        //最寄りの町の情報を取得して表示する
                        String info = Town.getNearestTownInfo(x, y, z);
                        sendchat(info,mc.thePlayer);
                        
                        //キーを押した判定を取り説明文をリセットしておく(ループ防止)
                        wkeyClicked = true;
                        ItemHoverTracker.lastLore = null;
                        break; // 処理が終了したのでloreの取得をやめる
                    }
                    //クランクエストの形式だった時
                }else if((clean.contains("ダンジョン[")&&clean.contains("]を攻略しよう"))) {
                	//ダンジョン名を取得する
                	String dungeonName = clean.split("\\[")[1].split("]")[0];
                	//ダンジョン名からダンジョンの情報を取得する
                	Dungeon d = Dungeon.getDungeonByName(dungeonName);
                	//ダンジョンが見つからないか、座標が与えられていないならば次の行へ移行
    				if(d==null||d.x==null||d.y==null||d.z==null)return;
    				
    				//マーカーをリセットした後に目的地マーカーを設置する
    				MarkerRenderer.clearMarkers();
    				MarkerRenderer.addMarker(d.x,d.y,d.z,d.name);
    				
    				sendchat("§a[thelow_quest_helper]§7マーカーを設置しました§e("+d.x+","+d.y+","+d.z+")",mc.thePlayer);
    				
    				//目的地に接近したときのメッセージを出すために保存
    				goalname = d.name;
    				
    				//最寄りの町の情報を取得して表示する
    				String info = Town.getNearestTownInfo(d.x, d.y, d.z);
    				sendchat(info,mc.thePlayer);
    				
    				//キーを押した判定を取り説明文をリセットしておく(ループ防止)
    				wkeyClicked = true;
    				ItemHoverTracker.lastLore = null;
    				break;//処理が完了したのでloreの取得をやめる
    				
    				//ダンジョン攻略クエストやダンジョン攻略実績にありそうな単語
                }else if((clean.contains("攻略する")||(clean.contains("クリア")&&!clean.contains("クリア条件")))) {
                	//ダンジョン名が明記されるときは○○を攻略するとなってることが多い
                	//をの前の部分をダンジョンの名前だと考えて取得する
                	if(!clean.contains("を"))return;
                	String dungeonName = clean.split("を")[0];
                	//ダンジョン名からダンジョンの情報を取得する
                	Dungeon d = Dungeon.getDungeonByName(dungeonName);
                	//ダンジョンが見つからないか、座標が与えられていないならば次の行へ移行
                	if(d==null||d.x==null||d.y==null||d.z==null)return;
                	
                	//マーカーをリセットした後に目的地マーカーを設置する
    				MarkerRenderer.clearMarkers();
    				MarkerRenderer.addMarker(d.x,d.y,d.z,d.name);
    				sendchat("§a[thelow_quest_helper]§7マーカーを設置しました§e("+d.x+","+d.y+","+d.z+")",mc.thePlayer);
    				
    				//最寄りの町の情報を取得して表示する
    				String info = Town.getNearestTownInfo(d.x, d.y, d.z);
    				sendchat(info,mc.thePlayer);
    				
    				//目的地に接近したときのメッセージを出すために保存
    				goalname = d.name;
    				
    				//キーを押した判定を取り説明文をリセットしておく(ループ防止)
    				wkeyClicked = true;
    				ItemHoverTracker.lastLore = null;
    				break;//処理が完了したのでloreの取得をやめる
                }
            }
        //ここからはZキーを押したときの処理
        }else if(Keyboard.isKeyDown(Keyboard.KEY_Z)&&!zkeyClicked) {
        	//loreを取得する
            List<String> lastLore = ItemHoverTracker.lastLore;
            if (lastLore == null) return;

            for (String line : lastLore) {
            	//loreを一行ずつ取得してカラーコードを除去して空白を削除する
                String clean = line.replaceAll("§.", "").trim();
                
                //現在のプレイヤーの座標をそれぞれ取得する
                int x1 = getPlayerBlockPos().getX(), y1 = getPlayerBlockPos().getY(), z1 = getPlayerBlockPos().getZ();
                
                //ルート情報をリセットする
                String[] routeInfo = null;
                BestTeleport_mtb routeCalculator = new BestTeleport_mtb();
                
                //NPCの座標が与えられたとき
                if(clean.contains("地上世界")) {
                	//まず座標を(x,y,z)の形式であると仮定して取得するのを試みる
                	Matcher matcher = Pattern.compile("\\((-?[0-9.]+), (-?[0-9.]+), (-?[0-9.]+)\\)").matcher(clean);
                	//見つけた場合座標をそれぞれdouble(実数)として取得する
                    if (matcher.find()) {
                    	int x = (int) Double.parseDouble(matcher.group(1));
                    	int y = (int) Double.parseDouble(matcher.group(2));
                    	int z =  (int) Double.parseDouble(matcher.group(3));
                        
                    	//NPCの名前を取得して目的地の名前として保存
                        String NPCname = ItemHoverTracker.lastNPCname;
                        goalname = NPCname;
                        
                        //経路情報を取得する
                        //戻り値の4番目のデータが移動経路の情報になる
                        routeInfo = routeCalculator.getBestRoute(x1, y1, z1, x, y, z, APIListener.isClantp);
                        
                        //目的地へのルート案内開始と予定ルートを表示
                        sendchat("§a[thelow_quest_helper]§f"+goalname+"§7へのルート案内を開始します",mc.thePlayer);
                        sendchat("§e予定ルート:§a現在地" + routeInfo[3],mc.thePlayer);
                        sendchat("",mc.thePlayer);
                        
                        //最初の経由地がクランハウスだけか、ガチャ広場経由か、直接かによって分岐するメッセージ
                        if(routeInfo[3].contains("clan")) {
                        	sendchat("§a[thelow_quest_helper]§bクランtp§7をし"+(routeInfo[3].contains("ガチャ") ? "た後§bガチャ広場§7を経由して§aハルシオン§7に向かってください" : "て"+ (routeInfo[3].contains("メルトリア王国 (馬車)") ? "§a馬車乗り場§7に向かってください" : routeInfo[3].contains("メルトリア王国 (飛空艇)") ? "§a飛空艇発着場§7に向かってください" : "§f"+goalname+"§7に向かってください")), mc.thePlayer);
                        }else {
                        	sendchat("§a[thelow_quest_helper]§e"+MarkerRenderer.getMarkername()+"に向かってください",mc.thePlayer);
                        }
                        
                        //キーを押した判定を取り説明文路リセットしておく(ループ防止)
                        zkeyClicked = true;
                        ItemHoverTracker.lastLore = null;
                        break; // 処理が完了したのでloreの取得をやめる
                    }
                //クランクエストの形式だった時
                }else if(clean.contains("ダンジョン[")&&clean.contains("]を攻略しよう")) {
                	//ダンジョン名を取得する
                	String dungeonName = clean.split("\\[")[1].split("]")[0];
                	//ダンジョン名からダンジョン情報を取得する
                	Dungeon d = Dungeon.getDungeonByName(dungeonName);
                	//ダンジョン名が見つからないか座標が与えられていなければ次の行へ移行
    				if(d==null||d.x==null||d.y==null||d.z==null)return;
    				
    				//目的地に接近したときのメッセージを出すために保存
    				goalname=d.name;
    				
    				//経路情報を取得する
    				//戻り値の4番目のデータが移動経路の情報になる
    				routeInfo = routeCalculator.getBestRoute(x1, y1, z1, (int)Math.round(d.x), (int)Math.round(d.y), (int)Math.round(d.z), APIListener.isClantp);

    				//目的地へのルート案内開始と予定ルートを表示
    				sendchat("§a[thelow_quest_helper]§f"+goalname+"§7へのルート案内を開始します",mc.thePlayer);
    				sendchat("§e予定ルート:§a現在地" + routeInfo[3],mc.thePlayer);
    				sendchat("",mc.thePlayer);
                    
                    //最初の経由地がクランハウスだけか、ガチャ広場経由か、直接かによって分岐するメッセージ
                    if(routeInfo[3].contains("clan")) {
                    	sendchat("§a[thelow_quest_helper]§bクランtp§7をし"+(routeInfo[3].contains("ガチャ") ? "た後§bガチャ広場§7を経由して§aハルシオン§7に向かってください" : "て"+ (routeInfo[3].contains("メルトリア王国 (馬車)") ? "§a馬車乗り場§7に向かってください" : routeInfo[3].contains("メルトリア王国 (飛空艇)") ? "§a飛空艇発着場§7に向かってください" : "§f"+goalname+"§7に向かってください")),mc.thePlayer);
                    }else {
                    	sendchat("§a[thelow_quest_helper]§e"+MarkerRenderer.getMarkername()+"§aに向かってください",mc.thePlayer);
                    }
                    
                    //キーを押した判定を取り説明文をリセットしておく(ループ防止)
                    zkeyClicked = true;
    				ItemHoverTracker.lastLore = null;
                    break;//処理が完了したのでloreの取得をやめる
                	
                //ダンジョン攻略クエストやダンジョン攻略実績にありそうな単語
                }else if((clean.contains("攻略する")||(clean.contains("クリア")&&!clean.contains("クリア条件")))) {
                	//ダンジョン名が明記されているときは○○を攻略するとなっていることが多い
                	//をの前の部分をダンジョンの名前だと考えて取得する
                	if(!clean.contains("を"))return;
                	String dungeonName = clean.split("を")[0];
                	//ダンジョン名からダンジョン情報を取得する
                	Dungeon d = Dungeon.getDungeonByName(dungeonName);
                	//ダンジョンが見つからないか、座標が与えられていないならば次の行へ移行
                	if(d==null||d.x==null||d.y==null||d.z==null)return;
    				
                	//目的地に接近したときのメッセージを出すために保存
                	goalname=d.name;
                	
                	//経路情報を取得する
                	//戻り値の4番目のデータが移動経路の情報になる
    				routeInfo = routeCalculator.getBestRoute(x1, y1, z1, (int)Math.round(d.x), (int)Math.round(d.y), (int)Math.round(d.z), APIListener.isClantp);
    				
    				//目的地へのルート案内開始と予定ルートを表示
    				sendchat("§a[thelow_quest_helper]§f"+goalname+"§7へのルート案内を開始します",mc.thePlayer);
    				sendchat("§e予定ルート:§a現在地" + routeInfo[3],mc.thePlayer);
    				sendchat("",mc.thePlayer);
                    
                    //最初の経由地がクランハウスだけか、ガチャ広場経由か、直接かによって分岐するメッセージ
                    if(routeInfo[3].contains("clan")) {
                    	sendchat("§a[thelow_quest_helper]§bクランtp§7をし"+(routeInfo[3].contains("ガチャ") ? "た後§bガチャ広場§7を経由して§aハルシオン§7に向かってください" : "て"+ (routeInfo[3].contains("メルトリア王国 (馬車)") ? "§a馬車乗り場§7に向かってください" : routeInfo[3].contains("メルトリア王国 (飛空艇)") ? "§a飛空艇発着場§7に向かってください" : "§f"+goalname+"§7に向かってください")),mc.thePlayer);
                    }else {
                    	sendchat("§a[thelow_quest_helper]§e"+MarkerRenderer.getMarkername()+"§aに向かってください",mc.thePlayer);
                    }
                    
                    //キーを押した判定を取り説明文をリセットしておく(ループ防止)
    				zkeyClicked = true;
    				ItemHoverTracker.lastLore = null;
                    break;//処理が完了したのでloreの取得をやめる
                }
            }
            
            //WもZも押していないときは押した判定をリセットする
        }else if(!Keyboard.isKeyDown(Keyboard.KEY_W)&&!Keyboard.isKeyDown(Keyboard.KEY_Z)) {
        	wkeyClicked = false;
        	zkeyClicked = false;
        }
    }
    
    //プレイヤーの座標を取得するメソッド
    public static BlockPos getPlayerBlockPos() {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        if (player != null) {
            return player.getPosition();
        }
        return null;
    }
    
    private static void sendchat(String text,EntityPlayerSP thePlayer) {
    	Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(text));
    	
    }
}