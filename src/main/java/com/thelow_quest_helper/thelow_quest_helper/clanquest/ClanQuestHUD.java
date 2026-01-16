package com.thelow_quest_helper.thelow_quest_helper.clanquest;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.thelow_quest_helper.thelow_quest_helper.chat.APIListener;
import com.thelow_quest_helper.thelow_quest_helper.config.thelow_quest_helperConfig;
import com.thelow_quest_helper.thelow_quest_helper.item.Dungeon;
import com.thelow_quest_helper.thelow_quest_helper.item.ItemHoverTracker;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StringUtils;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

class ClanQuest{
	public Dungeon QuestsDungeon;
	public String DungeonName;
	public int TimeLimitMinutes;
	public long limitTime;
	public String Queststats;
	public boolean Reboot;
	
	public ClanQuest() {
        this.QuestsDungeon = null;
        this.DungeonName = "ダミー";
        this.TimeLimitMinutes = 0;
        this.limitTime = 0L;
        this.Queststats = "未完了";
        this.Reboot = false;
    }
}
class ClanQuestDungeons{
	public static final int CLAN_QUEST_COUNT = 5;
	public ClanQuest[] slots;
	public ClanQuestDungeons() {
        this.slots = new ClanQuest[CLAN_QUEST_COUNT];
        
        for (int i = 0; i < CLAN_QUEST_COUNT; i++) {
            this.slots[i] = new ClanQuest();
        }
    }
}

public class ClanQuestHUD {
	private static final Minecraft mc = Minecraft.getMinecraft();
	private static final FontRenderer font = mc.fontRendererObj;
	private static ClanQuestDungeons ClanQuestInfo = new ClanQuestDungeons();
	private static long ClanQuestInfoGetTime = 0;
	private static boolean CanGetClanQuest = false;
	private static SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");//時間のフォーマットを作る
	private static int ForcusQuestIndex = 0;
	private static boolean ForcusQuest = false;
	
	@SubscribeEvent
	public void onGuiOpenTrigger(GuiOpenEvent event) {
		//有効じゃなかったら動作する必要はない
		if(!thelow_quest_helperConfig.ClanQuestHUDenable)return;
		CanGetClanQuest = true;
	}
	
	@SubscribeEvent
    public void onGuiDraw(GuiScreenEvent.DrawScreenEvent.Pre event) {
		
		//有効じゃなかったら動作する必要はない
		if(!thelow_quest_helperConfig.ClanQuestHUDenable)return;
		
		//guiが取れないなら動作する必要はない
		if (event.gui==null||!(event.gui instanceof GuiContainer)) {
			CanGetClanQuest=false;
			return;
		}
		
		final GuiContainer gui = (GuiContainer) event.gui;
		final Container container = gui.inventorySlots;

		if(!ChecktheContainerIsClanQuest(container)) {
			CanGetClanQuest=false;
			return;//クランクエストかどうかを判定する
		}
		
		//クランクエストを取得する部分(ラグ対策で一度だけ動く)
		if(CanGetClanQuest) {
			ForcusQuest=false;
			//クランクエストは2列目の奇数番目にある
	        //0から始まり、左上から右に向かって数字が増える
	        //2列目は9から始まる
	        final int[] targetSlots = {9, 11, 13, 15, 17};
	        int ClanQuestIndex = -1;//便宜上-1にしている
	        
	        for (int slotIndex : targetSlots) {
	            
	            if (container==null||slotIndex >= container.inventorySlots.size())continue;//guiが小さくなる更新があっても大丈夫

	            //スロットのデータを取得
	            final Slot slot = container.getSlot(slotIndex);
	            if (slot==null||!slot.getHasStack())continue;
	            
	            //ItemStack(いつものアイテムのデータ)にする
	            final ItemStack stack = slot.getStack();
	            if (stack==null||!stack.hasTagCompound())continue;
	            
	            //そのアイテムのnbtを取得する
	            final NBTTagCompound nbt = stack.getTagCompound();
	            if(nbt==null)continue;
	            
	            //nbtからdisplayを取り出す
	            final NBTTagCompound display = ItemHoverTracker.GetdisplayFromNBT(nbt);
	            if(display==null)continue;
	            
	            //String配列としてloreを取得
	            List<String> lore = new ArrayList<>();
	            lore = ItemHoverTracker.GetLoreFromdisplay(display);
	            if(lore==null)continue;
	            
	            //ダンジョンの名前を取得する
	            final String dungeonname = ItemHoverTracker.GetClanQuestDungeonName(display);
	            if(dungeonname==null)continue;
	            
	            //ダンジョン情報を取得する
	            final Dungeon dungeoninfo = Dungeon.getDungeonByName(dungeonname);
	            
	            //時間を取得する
	            final int timem = ItemHoverTracker.getTime(lore,"有効期限:",ItemHoverTracker.WOOL_TIME_PATTERN);
	            if(timem==-1)continue;//上で呼び出したメソッドはデータがないとnullではなく-1を返す
	            
	            //クランクエストであることが確定する
	            ClanQuestIndex++;//格納場所を調整する
	            if(dungeoninfo != null) {//ダンジョン情報があるなら格納する
	                ClanQuestInfo.slots[ClanQuestIndex].QuestsDungeon = dungeoninfo;
	            }else {//検索でヒットしなかったらnullで置換する
	            	ClanQuestInfo.slots[ClanQuestIndex].QuestsDungeon = null;
	            }
	            //有効期限が切れる時間を取得する
	            final long now = System.currentTimeMillis();//現在時刻を取得
	            final long endTime = now + (timem * 60 * 1000);
	            
	            //攻略済みかを確認するメソッド
	            final String QuestStats = GetQuestStats(lore);
	            
	            //クエスト情報を保存する
	            ClanQuestInfo.slots[ClanQuestIndex].DungeonName = dungeonname;
	            ClanQuestInfo.slots[ClanQuestIndex].TimeLimitMinutes = timem;
	            ClanQuestInfo.slots[ClanQuestIndex].limitTime  = endTime;
	            ClanQuestInfo.slots[ClanQuestIndex].Queststats  = QuestStats;
	          //再起動を挟むかを確認
	            if(ClossRebootTime(timem))ClanQuestInfo.slots[ClanQuestIndex].Reboot = true;
	            ClanQuestInfoGetTime = System.currentTimeMillis();//取得時間を変更する
	    	}
	        //何回も動かないようにする
	        CanGetClanQuest = false;
        }
		
		//クランクエストを全部表示するメソッド(クランクエストのメニューだったら確実に呼び出す)
        AllQuestRender();
    }
	
	private static boolean ChecktheContainerIsClanQuest(Container container) {
		if (!(container instanceof ContainerChest))return false;
		
		//開いたUIの名前を取得する
		final IInventory chestInventory = ((ContainerChest) container).getLowerChestInventory();
		final String guiTitle = StringUtils.stripControlCodes(chestInventory.getDisplayName().getUnformattedText());
		
		//クランクエストのメニューかを確認
		if (!(guiTitle.startsWith("クランクエスト情報 :[")&&guiTitle.endsWith("]")))return false;
		return true;
	}
	
	private static void AllQuestRender() {
		//有効であるかどうか
		if(!thelow_quest_helperConfig.ClanQuestHUDenable)return;
		if(ClanQuestInfo==null)return;//データはあるか
		//HUDの表示座標
		final int ClanQuestMenuHUDx = thelow_quest_helperConfig.ClanQuestHUDX;
    	int ClanQuestMenuHUDy = thelow_quest_helperConfig.ClanQuestHUDY;
    	
    	//表示の横幅を取得して表示位置を揃える
		int QuestsDungeonNameMaxlength = 0;//ダンジョン名の長さの最大を格納する変数
        int QuestsDungeonPosMaxlength = 0;//座標の長さの最大を格納する変数
        //横幅をそれぞれ取得する
		for(int i=0;i<ClanQuestDungeons.CLAN_QUEST_COUNT;i++) {
			int namelength = font.getStringWidth(ClanQuestInfo.slots[i].DungeonName);
	       	if(QuestsDungeonNameMaxlength<namelength) {
	       		QuestsDungeonNameMaxlength = namelength;
	       	}
			if(ClanQuestInfo.slots[i].QuestsDungeon == null)continue; //nullは知らん
	        int poslength = font.getStringWidth("("+ClanQuestInfo.slots[i].QuestsDungeon.x+","+ClanQuestInfo.slots[i].QuestsDungeon.y+","+ClanQuestInfo.slots[i].QuestsDungeon.z+")");
	        if(QuestsDungeonPosMaxlength<poslength) {
	        	QuestsDungeonPosMaxlength = poslength;
			}
        }
		//ここから表示を開始する
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0F, 0.0F, 500.0F);//強制手前表示
        
        //表の上のタグを作る
        final long now = System.currentTimeMillis();//現在時刻を取得
        final String nowTimeText = formatter.format(now);//それをテキスト化
        
        //表示位置を調整する
        final int Dnamepos = ClanQuestMenuHUDx;
        final int Dpospos = Dnamepos+QuestsDungeonNameMaxlength;
        final int limitpos = Dpospos+QuestsDungeonPosMaxlength;
        final int timepos = limitpos+font.getStringWidth("制限時間");
        final int statspos = timepos+font.getStringWidth("未完了");
        
        //上のタグを表示する
        font.drawStringWithShadow("ダンジョン名", Dnamepos, ClanQuestMenuHUDy, 0xFFFFFF);
        font.drawStringWithShadow("|座標", Dpospos, ClanQuestMenuHUDy, 0xFFFFFF);
        font.drawStringWithShadow("|制限時間", limitpos, ClanQuestMenuHUDy, 0xFFFFFF);
        font.drawStringWithShadow("|現在:"+nowTimeText, timepos, ClanQuestMenuHUDy, 0xFFFFFF);
           
        //実際のクエスト情報を表示する部分、色々nullでも表示できるところだけでも表示する
        for(int i=0;i<ClanQuestDungeons.CLAN_QUEST_COUNT;i++) {
        	ClanQuestMenuHUDy+=10;//高さ調整
        	ClanQuest theQuest = ClanQuestInfo.slots[i];
        	DrawTheQuest(theQuest,ClanQuestMenuHUDy,Dnamepos,Dpospos,limitpos,timepos,statspos);
        }
        
        GlStateManager.popMatrix();//手前表示を元に戻す
        //表示処理終わり
	}
	
	private static void DrawTheQuest(ClanQuest theQuest,final int HUDposY ,final int Dnamepos ,final int Dpospos ,final int limitpos ,final int timepos ,final int statspos) {
		if(theQuest.DungeonName!=null) {//ダンジョン名があったら表示
			font.drawStringWithShadow(theQuest.DungeonName, Dnamepos, HUDposY, 0xFFFFFF);
    		if(theQuest.DungeonName.equals("ダミー")) {
    			CanGetClanQuest = true;//取得できてないならもう一度取得しようとしてみる
    		}
    	}
    	if(theQuest.QuestsDungeon!=null&&theQuest.QuestsDungeon.hasCoords()) {//ダンジョン座標があったら表示
    		font.drawStringWithShadow("("+theQuest.QuestsDungeon.x+","+theQuest.QuestsDungeon.y+","+theQuest.QuestsDungeon.z+")", Dpospos, HUDposY, 0xFFFFFF);
    	}
    	
    	final long now = System.currentTimeMillis();//現在時刻を取得
    	//現在時刻とクエスト情報取得時間との差を分で取る
    	final int Delta_gettime_nowtime = (int)(now - ClanQuestInfoGetTime)/60000;  
        
    	if(Delta_gettime_nowtime!=0) {
    		//終了予定時間を再計算する
        	theQuest.TimeLimitMinutes -= Delta_gettime_nowtime;
        	theQuest.limitTime = now + (long)theQuest.TimeLimitMinutes*60000;
        	ClanQuestInfoGetTime = now;
    	}
    	if(theQuest.TimeLimitMinutes<0) {
    		theQuest.Queststats = "§b更新済§r";
    	}else if(theQuest.TimeLimitMinutes<=10&&!theQuest.Queststats.equals("§a完了済§r")) {
    		theQuest.Queststats = "§c未完了§r";
    	}else if(theQuest.TimeLimitMinutes<=20&&!theQuest.Queststats.equals("§a完了済§r")) {
    		theQuest.Queststats = "§6未完了§r";
    	}else if(theQuest.TimeLimitMinutes<=20&&!theQuest.Queststats.equals("§a完了済§r")) {
    		theQuest.Queststats = "§e未完了§r";
    	}
    	
    	String RebootColor = "";
    	if(theQuest.Reboot)RebootColor = "§c";
    	
    	font.drawStringWithShadow(RebootColor+String.format("%6d分", theQuest.TimeLimitMinutes), limitpos+4, HUDposY, 0xFFFFFF);
        
        //終了予定時刻を表示
    	font.drawStringWithShadow(RebootColor+"("+formatter.format(theQuest.limitTime)+")", timepos+16, HUDposY, 0xFFFFFF);
        
        //クエストが終了しているかを表示
    	font.drawStringWithShadow("("+theQuest.Queststats+")", statspos+16, HUDposY, 0xFFFFFF);
	}
	
	public static boolean ForcusTheQuest(final String dungeonname) {
		final String Forcusdungeonname = dungeonname;
		for(int i=0;i<ClanQuestDungeons.CLAN_QUEST_COUNT;i++) {
			final ClanQuest theQuest = ClanQuestInfo.slots[i];
        	if(theQuest==null||theQuest.DungeonName==null)continue;
        	String theQuestDungeonName = theQuest.DungeonName;
        	
        	if(theQuestDungeonName.replaceAll("§.", "").equals(Forcusdungeonname.replaceAll("§.", ""))) {
        		ForcusQuestIndex = i;
        		ForcusQuest = true;
        		break;
        	}
        }
		if(ForcusQuest) {
			return true;
		}else return false;
	}
	
	public static String GetForcusQuestDName() {
		final ClanQuest theQuest = ClanQuestInfo.slots[ForcusQuestIndex];
		if(theQuest==null)return "ダミー";
		final String DungeonName = theQuest.DungeonName;
		return DungeonName;
	}
	
	public static void UnForcusTheQuest() {
		ForcusQuest = false;
	}
	
	@SubscribeEvent
	public void DrawFocusQuest(RenderGameOverlayEvent.Text event) {
		if(!ForcusQuest)return;
		final ClanQuest theQuest = ClanQuestInfo.slots[ForcusQuestIndex];
    	if(theQuest == null)return; //nullは知らん
    	
    	if(theQuest.DungeonName.replaceAll("§.", "").equals(APIListener.cleardDungeonName.replaceAll("§.", ""))) {
    		ClanQuestInfo.slots[ForcusQuestIndex].Queststats = "§a完了済§r";
    		APIListener.cleardDungeonName = "ダミー";
    		ForcusQuest = false;
    		return;
    	}
    	
    	int namelength = 10;
    	if(theQuest.DungeonName != null) {
    		namelength = font.getStringWidth(theQuest.DungeonName);
    	}
    	int poslength = 10;
        if(theQuest.QuestsDungeon!=null) {
        	poslength = font.getStringWidth("("+theQuest.QuestsDungeon.x+","+theQuest.QuestsDungeon.y+","+theQuest.QuestsDungeon.z+")");
        }
        final int ClanQuestMenuHUDx = thelow_quest_helperConfig.ClanQuestHUDX;
    	final int ClanQuestMenuHUDy = thelow_quest_helperConfig.ClanQuestHUDY;
    	//表示位置を調整する
    	final int Dnamepos = ClanQuestMenuHUDx;
    	final int Dpospos = Dnamepos+namelength;
    	final int limitpos = Dpospos+poslength;
    	final int timepos = limitpos+font.getStringWidth("(00:00)");
    	final int statspos = timepos+font.getStringWidth("(未完了)");
    	final int textHeight = font.FONT_HEIGHT;
		final int lastpos = statspos + font.getStringWidth("(未完了)")+17;
		Gui.drawRect(Dnamepos-2, ClanQuestMenuHUDy-1, lastpos, ClanQuestMenuHUDy + textHeight, 0x50000000);
    	DrawTheQuest(theQuest,ClanQuestMenuHUDy, Dnamepos, Dpospos, limitpos, timepos, statspos);
	}
	
	private static String GetQuestStats(final List<String> lore) {
		if(lore==null)return "未完了";
		for(String loreline : lore) {
			if(loreline.contains("によってクリアされました")) {
				return "§a完了済§r";
			}
		}
		return "未完了";
	}
	
	private boolean ClossRebootTime(int durationMinutes) {
	    // 1. 再起動時間を「分」に換算しておく
	    // 0:10->10, 5:10->310, 11:10->670, 16:10->970, 20:10->1210
	    final int[] rebootTimes = {10, 310, 670, 970, 1210};

	    // 2. 現在時刻を「分」に換算する
	    final Calendar cal = Calendar.getInstance();
	    final int currentTotalMinutes = (cal.get(Calendar.HOUR_OF_DAY) * 60) + cal.get(Calendar.MINUTE);
	    
	    // 3. 終了時刻（分換算）
	    final int endTotalMinutes = currentTotalMinutes + durationMinutes;

	    // 4. 判定ループ
	    for (final int rebootTime : rebootTimes) {
	        // 通常のチェック（例: 現在100分 ～ 終了150分 の間に 120分の再起動があるか）
	        if (currentTotalMinutes < rebootTime && rebootTime <= endTotalMinutes) {
	            return true;
	        }
	        
	        // 日を跨ぐ場合のチェック（例: 現在1400分(23:20) ～ 終了1460分(0:20) の間に 10分の再起動があるか）
	        // 再起動時間(10) に 1日分の分(1440) を足して判定する
	        if (currentTotalMinutes < (rebootTime + 1440) && (rebootTime + 1440) <= endTotalMinutes) {
	            return true;
	        }
	    }
	    
	    return false;
	}
}