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
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class LongQuestHUD {
	private static final Minecraft mc = Minecraft.getMinecraft();
	private static boolean CanGetQuestList = false;
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onGuiOpenTrigger(GuiOpenEvent event) {
		if(!thelow_quest_helperConfig.LongQuestEnable)return;
		CanGetQuestList = true;
	}
	
	@SubscribeEvent(priority = EventPriority.HIGH)
    public void onGuiDraw(GuiScreenEvent.DrawScreenEvent.Pre event) {
		if(!thelow_quest_helperConfig.LongQuestEnable)return;
		if (event.gui==null||!(event.gui instanceof GuiContainer))return;
		
		final GuiContainer gui = (GuiContainer) event.gui;
		final Container container = gui.inventorySlots;
		if(container==null)return;
		if (!(container instanceof ContainerChest))return;
		
		if(!ChecktheContainerIsQuestList(container)) {
			CanGetQuestList=false;
			return;
		}
		
		if(!CanGetQuestList)return;
		CanGetQuestList = false;
		LongQuest.ClearPhaseStats();
		
	    for (int i=0;i<27;i++) {
		    if (i >= container.inventorySlots.size()) break;
		    
		    final Slot slot = container.getSlot(i);
		    if (slot==null||!slot.getHasStack())continue;
		    
		    final ItemStack stack = slot.getStack();
		    if (stack==null||!stack.hasTagCompound())continue;
		        
		    final NBTTagCompound nbt = stack.getTagCompound();
		    if(nbt==null)continue;
		    
		    final NBTTagCompound display = ItemHoverTracker.GetdisplayFromNBT(nbt);
		    if(display==null)continue;
		    
		    List<String> lore = ItemHoverTracker.GetLoreFromdisplay(display);
		    if(lore==null)continue;
		    final String questid = GetQuestIDline(lore);
		    LongQuest.CheckPhaseByID(questid);
        }
    }
	
	private static boolean ChecktheContainerIsQuestList(Container container) {
		if (!(container instanceof ContainerChest))return false;
		
		final IInventory chestInventory = ((ContainerChest) container).getLowerChestInventory();
		final String guiTitle = StringUtils.stripControlCodes(chestInventory.getDisplayName().getUnformattedText()).trim();
		
		if (!guiTitle.startsWith("QUEST VIEWER")) return false;
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
        if (!thelow_quest_helperConfig.LongQuestEnable) return;
        
        final LongQuest[] LongQuestList = LongQuest.GetLongQuestList();
        final int HUD_X = thelow_quest_helperConfig.LongQuestHUDX;
        int HUD_Y = thelow_quest_helperConfig.LongQuestHUDY;
        
        FontRenderer font = mc.fontRendererObj;

        for(final LongQuest TheLongQuest : LongQuestList) {
        	if(!CheckTheListHasTrue(TheLongQuest))continue;
        	
        	// 1. クエスト名を描画
        	final String nameText = TheLongQuest.QuestName + ":";
            // クエスト名にはツールチップなし(null)で描画
            HUD_render(nameText, HUD_X, HUD_Y, null);

            // 2. ■□ を1つずつ描画していく
            // 現在の描画X座標 (クエスト名の後ろからスタート)
            int currentDrawX = HUD_X + font.getStringWidth(nameText);
            
            for (int i = 0; i < TheLongQuest.QuestPhase.length; i++) {
                boolean isClear = TheLongQuest.QuestPhase[i];
                
                // 表示する文字 (■ または □)
                String symbol = isClear ? "§a■" : "§f□";
                
                // ツールチップの準備
                List<String> tipLines = new ArrayList<>();
                if (TheLongQuest.QuestList != null && i < TheLongQuest.QuestList.length) {
                    String desc = TheLongQuest.QuestList[i].Description;
                    if (desc != null) {
                        tipLines.add(desc);
                    }
                }

                // 1文字(1ブロック)だけ描画
                HUD_render(symbol, currentDrawX, HUD_Y, tipLines);
                
                // 次の文字のためにX座標を進める
                currentDrawX += font.getStringWidth(symbol);
            }

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

    // ★大幅にシンプル化: 「1つの文字列ブロック」と「それに対応する1つのツールチップ」を受け取る形に変更
    private static void HUD_render(final String text, final int x, final int y, final List<String> tooltip) {
        if (text == null) return;
        FontRenderer font = mc.fontRendererObj;
        
        final int totalWidth = font.getStringWidth(text);
        final int textHeight = font.FONT_HEIGHT;
        final int padding = 2; // 少しパディングを減らしてもいいかもしれません(四角形が連続するので)

        // 背景描画 (ここでの重なりは許容するか、drawRectのX座標を調整して隣と繋げる)
        // 隣り合う背景を綺麗につなげるため、右側の padding を調整
        // x - padding から x + totalWidth + padding まで描画すると、隣の文字と重なる可能性がありますが、
        // 半透明黒背景(0x50000000)同士の重なりは色が濃くなるだけなので、
        // 見た目を気にするなら padding を 0 にするか、まとめて描画する必要があります。
        // 今回は「一つずつ確実に処理する」優先なので、標準的な描画を行います。
        Gui.drawRect(x, y - padding, x + totalWidth, y + textHeight + padding, 0x50000000);

        GlStateManager.pushMatrix();
        font.drawString(text, x, y, 0xFFFFFF); // 影なし
        GlStateManager.popMatrix();

        if (!(mc.currentScreen instanceof GuiChat) || tooltip == null || tooltip.isEmpty()) return;
        
        final ScaledResolution sr = new ScaledResolution(mc);
        final int mouseX = Mouse.getX() * sr.getScaledWidth() / mc.displayWidth;
        final int mouseY = sr.getScaledHeight() - Mouse.getY() * sr.getScaledHeight() / mc.displayHeight - 1;

        // マウス判定 (描画した矩形の中にマウスがあるか)
        if (mouseX >= x && mouseX < x + totalWidth &&
            mouseY >= y - padding && mouseY < y + textHeight + padding) {
            
            GlStateManager.pushMatrix();
            RenderHelper.disableStandardItemLighting();
            
            GuiUtils.drawHoveringText(tooltip, mouseX, mouseY, sr.getScaledWidth(), sr.getScaledHeight(), -1, font);
            
            GlStateManager.popMatrix();
            
            RenderHelper.disableStandardItemLighting();
            GlStateManager.disableRescaleNormal();
            GlStateManager.disableLighting();
            GlStateManager.disableDepth();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }
}