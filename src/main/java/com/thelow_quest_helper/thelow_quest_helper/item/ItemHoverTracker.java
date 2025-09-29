package com.thelow_quest_helper.thelow_quest_helper.item;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ItemHoverTracker {
	
	public static String lastQuestname = null;
	public static String lastNPCname = null;
	public static List<String> lastLore;
	
    @SubscribeEvent
    public void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.itemStack;
        Item item = stack.getItem();
        String id = Item.itemRegistry.getNameForObject(item).toString();
        if(id==null||(!id.equals("minecraft:book")&&!id.equals("minecraft:written_book")&&!id.equals("minecraft:wool")&&!id.equals("minecraft:paper")&&!id.equals("minecraft:writable_book")&&!id.equals("minecraft:coal_block")&&!id.equals("minecraft:gold_block")&&!id.equals("minecraft:diamond_block")&&!id.equals("minecraft:glowstone")&&!id.equals("minecraft:emerald_block")))return;

        if (stack != null && stack.hasTagCompound()) {
            List<String> tooltip = event.toolTip;
            NBTTagCompound nbt = stack.getTagCompound();
            NBTTagCompound display = null;
            /*System.out.println(nbt);*/
            
            List<String> lore = new ArrayList<>();
                
                if(nbt.hasKey("display", 10)) { // 10 = NBTTagCompound
                    display = nbt.getCompoundTag("display");
                    if (display.hasKey("Lore", 9)) { // 9 = NBTTagList
                        NBTTagList loreList = display.getTagList("Lore", 8); // 8 = String tag
                        for (int i = 0; i < loreList.tagCount(); i++) {
                            lore.add(loreList.getStringTagAt(i));
                    }
                }
            }
                if(display==null)return;
                
            lastLore = lore;
            
             
            //紙だったらCTのクエストかをloreで確認して表示する
            if(id.equals("minecraft:paper")) {
            	int timem = getTime(lore,"このクエストは一定時間後に再度受けられます。残り時間:","残り時間:([0-9]+)分");
            	if(timem==-1)return;
            	String text = time_creater.operation(timem);
                if(text==null||text.isEmpty())return;
                
                // 空行挿入して見やすくする
                tooltip.add("");
                // 説明文の下に追記
                tooltip.add("受注可能:"+text);
            }
            
            //羊毛だったらクランクエストかをloreで確認して表示する
            if(id.equals("minecraft:wool")){
            	int timem = getTime(lore,"有効期限:","有効期限:([0-9]+)分");
            	if(timem==-1)return;
            	String text = time_creater.operation(timem);
            	if(text==null||text.isEmpty())return;
            	
                // 説明文の下に追記
                tooltip.add("更新予定:"+text);
                
                if(display != null&&display.hasKey("Name")) {
    				lastQuestname = display.getString("Name");
    				String dungeonname = display.getString("Name").replaceAll("§.", "").replaceAll("を攻略する","").trim();
    				Dungeon d = Dungeon.getDungeonByName(dungeonname);
    				if(d==null||d.x==null||d.y==null||d.z==null)return;
    				String info = Town.getNearestTownInfo(d.x, d.y, d.z);
    				tooltip.add(dungeonname+"§e("+d.x+","+d.y+","+d.z+")");
    				String[] texts = info.split("\\\\n");
    				
    				for(String text1 : texts) {
    					tooltip.add("§7" + text1.replace("\n", ""));
    				}
    				tooltip.add("§a[W]キーでマーカーを設置できます");
    				tooltip.add("§a[Z]キーでルート案内を開始します");
    			}
            }
            
            if((id.equals("minecraft:book")||id.equals("minecraft:written_book")||id.equals("minecraft:writable_book")||id.equals("minecraft:coal_block")||id.equals("minecraft:gold_block")||id.equals("minecraft:diamond_block")||id.equals("minecraft:glowstone")||id.equals("minecraft:emerald_block"))) {
            	
            	//もし地上世界の座標が表示されているならそこにマーカーを設置する
            	for (String line : lore) {
            		// §や全角スペースの除去を先にする
            		String clean = line.replaceAll("§.", "").trim();
            		if(clean.contains("地上世界")) {
            			// 緩い正規表現で数字3つを拾う
                		Matcher matcher = Pattern.compile(".*\\((-?[0-9.]+),\\s*(-?[0-9.]+),\\s*(-?[0-9.]+)\\)").matcher(clean);
                		if (matcher.find()) {
                			if(display.hasKey("Name")) {
                				lastQuestname = display.getString("Name");
                				lastNPCname = clean.split(" : ")[0];
                			}
                            tooltip.add("§a[W]キーでマーカーを設置できます");
                            tooltip.add("§a[Z]キーでルート案内を開始します");
                            break;
                        }
            		}
            		if((clean.contains("攻略する")||(clean.contains("クリア")&&!clean.contains("クリア条件")))) {
            			if(!clean.contains("を"))return;
            			String dungeonName = clean.split("を")[0];
            			Dungeon d = Dungeon.getDungeonByName(dungeonName);
                    	if(d==null||d.x==null||d.y==null||d.z==null) {
                    		System.out.println("d is null");
                    		return;
                    	}
        				String info = Town.getNearestTownInfo(d.x, d.y, d.z);
        				if(info==null)return;
        				String[] texts = info.split("\\\\n");
        				for(String text1 : texts) {
        					tooltip.add("§7" + text1.replace("\n",""));
        				}
        				tooltip.add("§a[W]キーでマーカーを設置できます");
        				tooltip.add("§a[Z]キーでルート案内を開始します");
        				break;
            		}
                }
            }
        }
    }
    
    private static int getTime(List<String> lore, String startText, String regex) {
        String loreline = "";

        // loreから対象の行を探す
        for (String line : lore) {
            String loretext = line.replaceAll("§.", "");
            if (loretext != null && loretext.contains(startText)) {
                loreline = loretext;
                break;
            }
        }

        // JSONの可能性をチェック
        try {
            JsonObject json = new JsonParser().parse(loreline).getAsJsonObject();
            if (json.has("text")) {
                loreline = json.get("text").getAsString();
            }
        } catch (Exception ignored) {
            // JSON形式でない場合は無視
        }

        // 色コード除去
        loreline = loreline.replaceAll("§[0-9a-fk-or]", "");

        // 正規表現で時間を抽出
        Matcher matcher = Pattern.compile(regex).matcher(loreline);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }

        return -1;
    }
}