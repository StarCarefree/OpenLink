package fun.moystudio.openlink.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import fun.moystudio.openlink.logic.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

import java.util.function.Supplier;

public class SettingScreenButton extends Button {
    public SettingScreenButton(int i, int j, int k, int l, Component component, OnPress onPress) {
        super(i, j, k, l, component, onPress, Supplier::get);
    }

    public static final WidgetSprites SPRITES = new WidgetSprites(Utils.createResourceLocation("openlink", "widget/setting_screen_button"), Utils.createResourceLocation("openlink", "widget/setting_screen_button_disabled"), Utils.createResourceLocation("openlink", "widget/setting_screen_button_highlighted"));

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
        Minecraft minecraft = Minecraft.getInstance();
        guiGraphics.blitSprite(RenderType::guiTextured, SPRITES.get(this.active, this.isHoveredOrFocused()), this.getX(), this.getY(), this.getWidth(), this.getHeight(), ARGB.white(this.alpha));
        int k = this.active ? 16777215 : 10526880;
        this.renderString(guiGraphics, minecraft.font, k | Mth.ceil(this.alpha * 255.0F) << 24);
    }

}
