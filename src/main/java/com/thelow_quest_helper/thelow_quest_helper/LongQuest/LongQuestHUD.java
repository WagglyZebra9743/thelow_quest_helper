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
		if(!ContainerHasItem(container))return;
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
	    
	    CanGetQuestList = false;
    }
	
	private static boolean ChecktheContainerIsQuestList(Container container) {
		if (!(container instanceof ContainerChest))return false;
		
		final IInventory chestInventory = ((ContainerChest) container).getLowerChestInventory();
		final String guiTitle = StringUtils.stripControlCodes(chestInventory.getDisplayName().getUnformattedText()).trim();
		
		if (!guiTitle.startsWith("QUEST VIEWER")) return false;
		return true;
	}
	
	private static boolean ContainerHasItem(Container container) {
		for (int i = 0; i < 27 && i < container.inventorySlots.size(); i++) {
            if (container.getSlot(i).getHasStack()) {
                return true;
            }
        }
		return false;
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

        // 【重要】ループを抜けた後に描画するためのリスト
        List<String> pendingTooltip = null;

        for(final LongQuest TheLongQuest : LongQuestList) {
        	if(!CheckTheListHasTrue(TheLongQuest))continue;
        	
        	final String nameText = TheLongQuest.QuestName + ":";
            // 名前部分の描画（マウス判定だけ行って、ホバーしてたら何かするならここ）
            if(HUD_render_check(nameText, HUD_X, HUD_Y)) {
                // 名前部分にツールチップを出したい場合はここに処理を書く
            }

            int currentDrawX = HUD_X + font.getStringWidth(nameText);
            
            for (int i = 0; i < TheLongQuest.QuestPhase.length; i++) {
                boolean isClear = TheLongQuest.QuestPhase[i];
                String symbol = isClear ? "§a■" : "§f□";
                
                // 1文字描画し、マウスが乗っているかチェック
                if (HUD_render_check(symbol, currentDrawX, HUD_Y)) {
                    // マウスが乗っていたら、表示すべきテキストを pendingTooltip に保存
                     if (TheLongQuest.QuestList != null && i < TheLongQuest.QuestList.length) {
                        String desc = TheLongQuest.QuestList[i].Description;
                        if (desc != null) {
                            pendingTooltip = new ArrayList<>();
                            pendingTooltip.add(desc);
                        }
                    }
                }
                
                currentDrawX += font.getStringWidth(symbol);
            }

            HUD_Y += 13;
        }
        
        // 【重要】全ての文字を描画し終わった後、一番手前にツールチップを描画する
        if (pendingTooltip != null && !pendingTooltip.isEmpty()) {
            DrawTooltip(pendingTooltip);
        }
    }

    private static boolean CheckTheListHasTrue(LongQuest TheLongQuest) {
        if (TheLongQuest == null || TheLongQuest.QuestPhase == null) return false;
        for (boolean thebool : TheLongQuest.QuestPhase) {
            if (thebool) return true;
        }
        return false;
    }

    // 描画を行い、マウスが上にあるかどうか(true/false)だけを返すメソッド
    private static boolean HUD_render_check(final String text, final int x, final int y) {
        if (text == null) return false;
        FontRenderer font = mc.fontRendererObj;
        
        final int totalWidth = font.getStringWidth(text);
        final int textHeight = font.FONT_HEIGHT;
        final int padding = 2; 

        Gui.drawRect(x, y - padding, x + totalWidth, y + textHeight + padding, 0x50000000);

        GlStateManager.pushMatrix();
        font.drawString(text, x, y, 0xFFFFFF);
        GlStateManager.popMatrix();
        
        if (!(mc.currentScreen instanceof GuiChat)) return false;

        final ScaledResolution sr = new ScaledResolution(mc);
        final int mouseX = Mouse.getX() * sr.getScaledWidth() / mc.displayWidth;
        final int mouseY = sr.getScaledHeight() - Mouse.getY() * sr.getScaledHeight() / mc.displayHeight - 1;

        return (mouseX >= x && mouseX < x + totalWidth &&
                mouseY >= y - padding && mouseY < y + textHeight + padding);
    }
    
    // ツールチップ描画専用メソッド
    private static void DrawTooltip(List<String> tooltip) {
    	if (tooltip == null || tooltip.isEmpty()) return;
        
        FontRenderer font = mc.fontRendererObj;
    	final ScaledResolution sr = new ScaledResolution(mc);
        final int mouseX = Mouse.getX() * sr.getScaledWidth() / mc.displayWidth;
        final int mouseY = sr.getScaledHeight() - Mouse.getY() * sr.getScaledHeight() / mc.displayHeight - 1;

        GlStateManager.pushMatrix();
        RenderHelper.disableStandardItemLighting(); 
        
        // ここで描画
        GuiUtils.drawHoveringText(tooltip, mouseX, mouseY, sr.getScaledWidth(), sr.getScaledHeight(), -1, font);
        
        GlStateManager.popMatrix();
        
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }
}