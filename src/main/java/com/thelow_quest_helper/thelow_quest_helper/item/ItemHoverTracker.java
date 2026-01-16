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
	
	private static final Pattern PAPER_TIME_PATTERN = Pattern.compile("残り時間:([0-9]+)分");
	public static final Pattern WOOL_TIME_PATTERN = Pattern.compile("有効期限:([0-9]+)分");
	private static final Pattern CLAN_QUEST_PATTERN = Pattern.compile("§a(.*)を攻略する");
	private static final Pattern COORD_PATTERN = Pattern.compile(".*\\((-?[0-9.]+),\\s*(-?[0-9.]+),\\s*(-?[0-9.]+)\\)");
    @SubscribeEvent
    public void onItemTooltip(ItemTooltipEvent event) {
    	final ItemStack stack = event.itemStack;
    	final Item item = stack.getItem();
    	final String id = Item.itemRegistry.getNameForObject(item).toString();
        if(id==null||(!id.equals("minecraft:book")&&!id.equals("minecraft:written_book")&&!id.equals("minecraft:wool")&&!id.equals("minecraft:paper")&&!id.equals("minecraft:writable_book")&&!id.equals("minecraft:coal_block")&&!id.equals("minecraft:gold_block")&&!id.equals("minecraft:diamond_block")&&!id.equals("minecraft:glowstone")&&!id.equals("minecraft:emerald_block")))return;

        if (stack != null && stack.hasTagCompound()) {
        	List<String> tooltip = event.toolTip;
        	final NBTTagCompound nbt = stack.getTagCompound();
        	final NBTTagCompound display = GetdisplayFromNBT(nbt);
            if(display==null)return;
            /*System.out.println(nbt);*/
            
            List<String> lore = new ArrayList<>();
                
            
            lore = GetLoreFromdisplay(display);
            if(lore==null)return;
            
            lastLore = lore;
            
             
            //紙だったらCTのクエストかをloreで確認して表示する
            if(id.equals("minecraft:paper")) {
            	final int timem = getTime(lore,"このクエストは一定時間後に再度受けられます。残り時間:",PAPER_TIME_PATTERN);
            	if(timem==-1)return;
            	final String text = time_creater.operation(timem);
                if(text==null||text.isEmpty())return;
                
                // 空行挿入して見やすくする
                tooltip.add("");
                // 説明文の下に追記
                tooltip.add("受注可能:"+text);
            }
            
            //羊毛だったらクランクエストかをloreで確認して表示する
            if(id.equals("minecraft:wool")){
            	final int timem = getTime(lore,"有効期限:",WOOL_TIME_PATTERN);
            	if(timem==-1)return;
            	final String text = time_creater.operation(timem);
            	if(text==null||text.isEmpty())return;
            	
                // 説明文の下に追記
                tooltip.add("更新予定:"+text);
                
                if(display == null||!display.hasKey("Name"))return;
                lastQuestname = display.getString("Name");
                final String dungeonname = GetClanQuestDungeonName(display);
    			if(dungeonname==null)return;
    			final Dungeon d = Dungeon.getDungeonByName(dungeonname);
    			if(d==null||!d.hasCoords())return;
    			final String info = Town.getNearestTownInfo(d.x, d.y, d.z);
   				tooltip.add(dungeonname+"§e("+d.x+","+d.y+","+d.z+")");
   				final String[] texts = info.split("\\\\n");
    			
    			for(String text1 : texts) {
   					tooltip.add("§7" + text1.replace("\n", ""));
   				}
    			tooltip.add("§a[M]キーでマーカーを設置できます");
    			tooltip.add("§a[Z]キーでルート案内を開始します");
    			tooltip.add("§a[F]キーでこのクエストにフォーカスします");
            }
            
            if((id.equals("minecraft:book")||id.equals("minecraft:written_book")||id.equals("minecraft:writable_book")||id.equals("minecraft:coal_block")||id.equals("minecraft:gold_block")||id.equals("minecraft:diamond_block")||id.equals("minecraft:glowstone")||id.equals("minecraft:emerald_block"))) {
            	
            	//もし地上世界の座標が表示されているならそこにマーカーを設置する
            	for (final String line : lore) {
            		// §や全角スペースの除去を先にする
            		final String clean = line.replaceAll("§.", "").trim();
            		if(clean.contains("地上世界")) {
            			// 緩い正規表現で数字3つを拾う
            			final Matcher matcher = COORD_PATTERN.matcher(clean);
                		if (matcher.find()) {
                			if(display.hasKey("Name")) {
                				lastQuestname = display.getString("Name");
                				lastNPCname = clean.split(" : ")[0];
                			}
                            tooltip.add("§a[M]キーでマーカーを設置できます");
                            tooltip.add("§a[Z]キーでルート案内を開始します");
                            break;
                        }
            		}
            		if((clean.contains("攻略する")||(clean.contains("クリア")&&!clean.contains("クリア条件")))) {
            			if(!clean.contains("を"))return;
            			final String dungeonName = clean.split("を")[0];
            			final Dungeon d = Dungeon.getDungeonByName(dungeonName);
                    	if(d==null||!d.hasCoords())return;
                    	final String info = Town.getNearestTownInfo(d.x, d.y, d.z);
        				if(info==null)return;
        				final String[] texts = info.split("\\\\n");
        				for(final String text1 : texts) {
        					tooltip.add("§7" + text1.replace("\n",""));
        				}
        				tooltip.add("§a[M]キーでマーカーを設置できます");
        				tooltip.add("§a[Z]キーでルート案内を開始します");
        				break;
            		}
                }
            }
        }
    }
    
    public static int getTime(List<String> lore, String startText, Pattern thePattern) {
        String loreline = "";

        // loreから対象の行を探す
        for (final String line : lore) {
        	final String loretext = line.replaceAll("§.", "");
            if (loretext != null && loretext.contains(startText)) {
                loreline = loretext;
                break;
            }
        }

        // JSONの可能性をチェック
        try {
        	final JsonObject json = new JsonParser().parse(loreline).getAsJsonObject();
            if (json.has("text")) {
                loreline = json.get("text").getAsString();
            }
        } catch (Exception ignored) {
            // JSON形式でない場合は無視
        }

        // 色コード除去
        loreline = loreline.replaceAll("§[0-9a-fk-or]", "");

        // 正規表現で時間を抽出
        final Matcher matcher = thePattern.matcher(loreline);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }

        return -1;
    }
    public static String GetClanQuestDungeonName(final NBTTagCompound display) {
    	if(display==null||!display.hasKey("Name")||!display.getString("Name").contains("を攻略する"))return null;
    	final String itemname = display.getString("Name");
    	final Matcher matcher = CLAN_QUEST_PATTERN.matcher(itemname);
    	if (matcher.find()) {
            // (.*) にマッチした部分（キャプチャグループの1番目）を返す
            return matcher.group(1).replace("§l", "").trim();
        }
    	final String dungeonname = display.getString("Name").replaceAll("§.", "").replaceAll("を攻略する","").trim();
    	return dungeonname;
    }
    public static NBTTagCompound GetdisplayFromNBT(final NBTTagCompound nbt) {
    	if(nbt==null)return null;
    	if(nbt.hasKey("display", 10)) { // 10 = NBTTagCompound
            return nbt.getCompoundTag("display");
        }
    	return null;
    }
    public static List<String> GetLoreFromdisplay(final NBTTagCompound display){
    	List<String> lore = new ArrayList<>();
    	if (display.hasKey("Lore", 9)) { // 9 = NBTTagList
            NBTTagList loreList = display.getTagList("Lore", 8); // 8 = String tag
            for (int i = 0; i < loreList.tagCount(); i++) {
                lore.add(loreList.getStringTagAt(i));
            }
            return lore;
        }
    	return null;
    }
}