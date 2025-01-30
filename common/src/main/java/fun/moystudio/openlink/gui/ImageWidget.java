package fun.moystudio.openlink.gui;

import fun.moystudio.openlink.logic.Utils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class ImageWidget extends AbstractWidget {
    public ResourceLocation texture;
    private final int textureWidth,textureHeight;
    private final float uOffset,vOffset;
    public ImageWidget(int x1, int y1, float uOffset1, float vOffset1, int width1, int height1, int textureWidth1, int textureHeight1, ResourceLocation rl){
        super(x1,y1,width1,height1, Utils.EMPTY);
        uOffset=uOffset1;
        vOffset=vOffset1;
        textureWidth=textureWidth1;
        textureHeight=textureHeight1;
        texture=rl;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
        guiGraphics.blit(RenderType::guiTextured,texture,this.getX(),this.getY(), uOffset, vOffset,width,height,textureWidth,textureHeight);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    }
}
