package com.thelow_quest_helper.thelow_quest_helper.chat;

import java.lang.reflect.Field;

import com.thelow_quest_helper.thelow_quest_helper.item.MarkerRenderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class TitleInterceptor {

    private static final Minecraft mc = Minecraft.getMinecraft();

    private static Field titleField;
    

    static {
        try {
            titleField = GuiIngame.class.getDeclaredField("field_175201_x"); // obfuscated name in 1.8.9
            titleField.setAccessible(true);
        } catch (Exception e) {
            System.err.println("[thelow_quest_helper] タイトルフィールド取得に失敗しました: " + e.getMessage());
        }
    }
    private static String lasttitle = "";
    
    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.ALL) return;
        
        if (titleField != null) {
            try {
                String title = (String) titleField.get(mc.ingameGUI);
                if (title != null && !title.isEmpty() && !lasttitle.contains(title)&&MarkerRenderer.IsThereMarker()&&APIListener.can_cmd_send) {
                	mc.thePlayer.sendChatMessage("/thelow_api location");
                	APIListener.can_cmd_send=false;
                    lasttitle = title;
                }
            } catch (Exception e) {
                System.err.println("[thelow_quest_helper] タイトル取得に失敗しました: " + e.getMessage());
            }
        }
    }
	public static String GetTitle() {
		return lasttitle;
	}
}