package fun.moystudio.openlink.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import fun.moystudio.openlink.frpc.FrpcManager;
import fun.moystudio.openlink.logic.Utils;
import net.minecraft.Util;
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
        this.addRenderableWidget(new Button(this.width/2-60, this.height/5*4-10, 120, 20, Utils.translatableText("text.openlink.openstoragedir"), button -> {
            Util.getPlatform().openFile(FrpcManager.getInstance().getFrpcStoragePathById(FrpcManager.getInstance().getCurrentFrpcId()).toFile());
        }));
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
                } catch (Exception e){
                    throw new RuntimeException(e);
                }
            }, "Frpc download thread").start();
        }
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        this.renderBackground(poseStack);
        text.renderCentered(poseStack,this.width/2,this.height/2,16,0xffffff);
        super.render(poseStack,i,j,f);
    }
}
