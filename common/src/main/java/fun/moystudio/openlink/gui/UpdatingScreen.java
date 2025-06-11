package fun.moystudio.openlink.gui;

import fun.moystudio.openlink.OpenLink;
import fun.moystudio.openlink.frpcimpl.FrpcManager;
import fun.moystudio.openlink.logic.EventCallbacks;
import fun.moystudio.openlink.logic.Utils;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Unique;

public class UpdatingScreen extends Screen {
    @Unique
    public int tickCount=0;
    public UpdatingScreen() {
        super(Utils.translatableText("text.openlink.updatefrpc"));
    }
    public MultiLineLabel text;
    boolean updated=false;
    @Override
    protected void init() {
        text=MultiLineLabel.create(this.font, Utils.translatableText("text.openlink.updatingfrpc"),this.width-50);
        this.addRenderableWidget(Button.builder(Utils.translatableText("text.openlink.openstoragedir"), button -> {
            Util.getPlatform().openFile(FrpcManager.getInstance().getFrpcStoragePathById(FrpcManager.getInstance().getCurrentFrpcId()).toFile());
        }).bounds(this.width/2-60, this.height/5*4-10, 120, 20).build());
    }
    @Override
    public void tick(){
        tickCount++;
        if(updated){
            this.onClose();
        }
        if(tickCount==5){
            new Thread(()->{
                try {
                    FrpcManager.getInstance().updateFrpcByIds(FrpcManager.getInstance().getCurrentFrpcId());
                    updated=true;
                    EventCallbacks.hasUpdate=false;
                } catch (Exception e){
                    throw new RuntimeException(e);
                }
                OpenLink.disabled = false;
            }, "Frpc download thread").start();
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
        super.render(guiGraphics,i,j,f);
        text.renderCentered(guiGraphics,this.width/2,this.height/2,16,0xffffff);
    }
}
