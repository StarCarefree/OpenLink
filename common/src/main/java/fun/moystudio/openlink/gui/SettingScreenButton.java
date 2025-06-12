package fun.moystudio.openlink.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import fun.moystudio.openlink.logic.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class SettingScreenButton extends Button {
    public static final ResourceLocation SETTING_WIDGET_LOCATION=Utils.createResourceLocation("openlink","textures/gui/widgets_setting.png");
    public SettingScreenButton(int i, int j, int k, int l, Component component, OnPress onPress) {
        super(i, j, k, l, component, onPress);
    }
    protected int packedFGColor = -1;

    @Override
    public void renderButton(PoseStack poseStack, int i, int j, float f) {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        minecraft.getTextureManager().bind(SETTING_WIDGET_LOCATION);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, this.alpha);
        int k = this.getYImage(this.isHovered());
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        this.blit(poseStack, this.x, this.y, 0, 46 + k * 20, this.width / 2, this.height);
        this.blit(poseStack, this.x + this.width / 2, this.y, 200 - this.width / 2, 46 + k * 20, this.width / 2, this.height);
        this.renderBg(poseStack, minecraft, i, j);
        int l = this.active ? 16777215 : 10526880;
        drawCenteredString(poseStack, font, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, l | Mth.ceil(this.alpha * 255.0F) << 24);
    }
    public int getFGColor() {
        if (this.packedFGColor != -1) {
            return this.packedFGColor;
        } else {
            return this.active ? 16777215 : 10526880;
        }
    }
}
