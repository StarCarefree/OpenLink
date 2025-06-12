package fun.moystudio.openlink.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.resources.ResourceLocation;

public class ImageButtonWithHoveredState extends ImageButton {
    private final ResourceLocation resourceLocation;
    private final int yTexStart;
    private final int xTexStart;
    private final int yDiffTex;
    private final int textureWidth;
    private final int textureHeight;
    private final ResourceLocation resourceLocationHovered;

    public ImageButtonWithHoveredState(int i, int j, int k, int l, int m, int n, int o, ResourceLocation resourceLocation, ResourceLocation rl2, int p, int q, OnPress onPress) {
        super(i, j, k, l, m, n, o, resourceLocation, p, q, onPress);
        this.textureWidth = p;
        this.textureHeight = q;
        this.xTexStart = m;
        this.yTexStart = n;
        this.yDiffTex = o;
        this.resourceLocation = resourceLocation;
        this.resourceLocationHovered = rl2;
    }

    @Override
    public void renderButton(PoseStack poseStack, int i, int j, float f) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.getTextureManager().bind(this.resourceLocation);
        int k = this.yTexStart;
        if (this.isHovered()) {
            k += this.yDiffTex;
            minecraft.getTextureManager().bind(this.resourceLocationHovered);
        }

        RenderSystem.enableDepthTest();
        blit(poseStack, this.x, this.y, (float)this.xTexStart, (float)k, this.width, this.height, this.textureWidth, this.textureHeight);
        if (this.isHovered()) {
            this.renderToolTip(poseStack, i, j);
        }

    }
}
