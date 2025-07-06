package fun.moystudio.openlink.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class ChartWidget extends AbstractWidget{
    public int color;
    public ChartWidget(int x, int y, int width, int height, Component title, int color) {
        super(x,y,width,height,title);
        this.color=color;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
        guiGraphics.fill(this.getX(),this.getY(),this.getX()+width,this.getY()+height,color);
        guiGraphics.drawCenteredString(Minecraft.getInstance().font,this.getMessage(),this.getX()+width/2,this.getY()+5,0xffffffff);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    }
}
