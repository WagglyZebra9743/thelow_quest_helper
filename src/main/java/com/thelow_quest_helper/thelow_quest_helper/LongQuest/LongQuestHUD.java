package com.thelow_quest_helper.thelow_quest_helper.LongQuest;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Mouse;

import com.thelow_quest_helper.thelow_quest_helper.config.thelow_quest_helperConfig;
import com.thelow_quest_helper.thelow_quest_helper.item.ItemHoverTracker;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
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
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class LongQuestHUD {
	private static final Minecraft mc = Minecraft.getMinecraft();
	private static boolean CanGetQuestList = false;
	
	@SubscribeEvent
	public void onGuiOpenTrigger(GuiOpenEvent event) {
		//有効じゃなかったら動作する必要はない
		if(!thelow_quest_helperConfig.LongQuestEnable)return;
		CanGetQuestList = true;
	}
	
	@SubscribeEvent
    public void onGuiDraw(GuiScreenEvent.DrawScreenEvent.Pre event) {
		
		//有効じゃなかったら動作する必要はない
		if(!thelow_quest_helperConfig.LongQuestEnable)return;
		
		//guiが取れないなら動作する必要はない
		if (event.gui==null||!(event.gui instanceof GuiContainer))return;
		
		final GuiContainer gui = (GuiContainer) event.gui;
		final Container container = gui.inventorySlots;
		if(container==null)return;

		if(!ChecktheContainerIsQuestList(container)) {
			CanGetQuestList=false;
			return;//クエストリストかどうかを判定する
		}
		
		//クエストリストを取得する部分(ラグ対策で一度だけ動く)
		if(!CanGetQuestList)return;
		CanGetQuestList = false;
		//クエストリストは最大27個のスロットがある	        
	    for (int i=0;i<27;i++) {
	    if (container==null||i >= container.inventorySlots.size())continue;//guiが小さくなる更新があっても大丈夫
	    
	    //スロットのデータを取得
	    final Slot slot = container.getSlot(i);
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
	    final String questid = GetQuestIDline(lore);
	    LongQuest.CheckPhaseByID(questid);
        }
    }
	
	private static boolean ChecktheContainerIsQuestList(Container container) {
		if (!(container instanceof ContainerChest))return false;
		
		//開いたUIの名前を取得する
		final IInventory chestInventory = ((ContainerChest) container).getLowerChestInventory();
		final String guiTitle = StringUtils.stripControlCodes(chestInventory.getDisplayName().getUnformattedText());
		
		//クエストのメニューかを確認
		if (!(guiTitle.equals("QUEST VIEWER")))return false;
		return true;
	}
	
	private static String GetQuestIDline(final List<String> lore) {
		if(lore==null)return "null";
		for(String line : lore) {
			if(line.startsWith("§0id:"))return line;
		}
		return "null";
	}
	
	@SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent.Text event) {
        // 有効じゃなかったら動作する必要はない
        if (!thelow_quest_helperConfig.LongQuestEnable) return;

        final LongQuest JealousFellow = LongQuest.GetJealousFellowStats();
        final boolean HasJealousFellow = CheckTheListHasTrue(JealousFellow);

        if (HasJealousFellow) {
            final int HUD_X = thelow_quest_helperConfig.LongQuestHUDX;
            int HUD_Y = thelow_quest_helperConfig.LongQuestHUDY;

            // 1. クエスト名部分 ("クエスト名:")
            String nameText = JealousFellow.QuestName + ":";
            
            // クエスト名を描画 (ツールチップなし)
            HUD_render(nameText, HUD_X, HUD_Y, null);

            // 2. 進行状況部分 ("■□...") とツールチップの生成
            List<List<String>> tooltips = new ArrayList<>();
            String squaresText = CreatePhaseTextAndTooltips(JealousFellow, tooltips);

            // クエスト名の幅を取得して、その右側に四角形を描画
            int nameWidth = mc.fontRendererObj.getStringWidth(nameText);
            HUD_render(squaresText, HUD_X + nameWidth, HUD_Y, tooltips);

            // 次の行へ (Y座標加算)
            HUD_Y += 13;
        }
    }

    private static boolean CheckTheListHasTrue(LongQuest TheLongQuest) {
        if (TheLongQuest == null || TheLongQuest.QuestPhase == null) return false;
        for (boolean thebool : TheLongQuest.QuestPhase) {
            if (thebool) return true;
        }
        return false;
    }

    // ■□の文字列生成と同時にツールチップリストを作成するメソッド
    private static String CreatePhaseTextAndTooltips(LongQuest TheLongQuest, List<List<String>> tooltips) {
        StringBuilder sb = new StringBuilder();
        
        // QuestPhase (boolean配列) と QuestList (Phase詳細配列) のインデックスを合わせる
        for (int i = 0; i < TheLongQuest.QuestPhase.length; i++) {
            boolean isClear = TheLongQuest.QuestPhase[i];
            
            // 四角形の文字を追加
            if (isClear) {
                sb.append("§a■");
            } else {
                sb.append("§f□");
            }

            // 対応する説明文を取得してリストに追加
            List<String> tipLines = new ArrayList<>();
            
            // 配列外参照エラーを防ぐためのチェック
            if (TheLongQuest.QuestList != null && i < TheLongQuest.QuestList.length) {
                // ThePhaseオブジェクトからDescriptionを取得
                String desc = TheLongQuest.QuestList[i].Description;
                if (desc != null) {
                    tipLines.add(desc);
                }
            }
            // リストのリストに追加 (説明がない場合でも、空のリストを追加してインデックスを合わせる)
            tooltips.add(tipLines);
        }
        
        return sb.toString();
    }

    // 引数を増やし、ホバー判定と描画機能を追加したHUD_render
    private static void HUD_render(final String text, final int x, final int y, final List<List<String>> allTooltips) {
        if (text == null) return;
        FontRenderer font = mc.fontRendererObj;
        
        // 全体の幅と高さを取得
        final int totalWidth = font.getStringWidth(text);
        final int textHeight = font.FONT_HEIGHT;
        final int padding = 2;

        // 背景とテキストの描画
        Gui.drawRect(x - padding, y - padding, x + totalWidth + padding, y + textHeight + padding, 0x50000000);

        GlStateManager.pushMatrix();
        mc.fontRendererObj.drawStringWithShadow(text, x, y, 0xFFFFFF);
        GlStateManager.popMatrix();

        // チャット画面が開いているか確認
        if (!(mc.currentScreen instanceof GuiChat) || allTooltips == null || allTooltips.isEmpty())return;
        // マウス座標の計算 (解像度合わせ)
        final ScaledResolution sr = new ScaledResolution(mc);
        final int mouseX = Mouse.getX() * sr.getScaledWidth() / mc.displayWidth;
        final int mouseY = sr.getScaledHeight() - Mouse.getY() * sr.getScaledHeight() / mc.displayHeight - 1;

        // マウスがテキスト全体のY座標範囲内にあるか判定
        if (mouseY < y - padding || mouseY > y + textHeight + padding)return;
        
        int currentX = x; // 現在チェックしている文字のX座標
        int visualIndex = 0; // カラーコードを除いた文字数カウンタ
        
        for (int i = 0; i < text.length(); i++) {
        	char c = text.charAt(i);
        	
        	// カラーコード (§ + 文字) はスキップ
        	if (c == '§') {
        		i++; 
        		continue;
        	}
        	
        	int charWidth = font.getCharWidth(c);
        	
        	// マウスがこの文字の幅の中にあるか判定
        	if (mouseX >= currentX && mouseX < currentX + charWidth) {
        		// 対応する説明文を表示
        		if (visualIndex >= allTooltips.size())break;
        		List<String> tip = allTooltips.get(visualIndex);
        		if (tip == null || tip.isEmpty())break;
        		
        		//ライティング等の設定が影響しないように保護
        		GlStateManager.pushMatrix();
        		
        		GuiUtils.drawHoveringText(tip,mouseX,mouseY,sr.getScaledWidth(),sr.getScaledHeight(),-1,font);
        		
        		GlStateManager.popMatrix();
        		
        		// 描画後に必ずライティングと色設定をリセットする
        		RenderHelper.disableStandardItemLighting();
        		GlStateManager.disableRescaleNormal();
        		GlStateManager.disableLighting();
        		GlStateManager.disableDepth();
        		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        		break; // 重なり防止のためループを抜ける
        	}
        	currentX += charWidth;
        	visualIndex++;
        }
    }
}
