package fun.moystudio.openlink.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import fun.moystudio.openlink.frpc.Frpc;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.TranslatableComponent;

import java.io.IOException;

public class UpdateScreen extends Screen {
    public UpdateScreen() {
        super(new TranslatableComponent("gui.openlink.updatefrpctitle"));
    }
    public Button yes;
    public Button no;
    public MultiLineLabel text;

    @Override
    protected void init(){
        yes=new Button(this.width/4-40,this.height/5*4-10,80,20,new TranslatableComponent("text.openlink.yes"),button -> {
            this.minecraft.setScreen(new UpdatingScreen());
        });
        no=new Button(this.width/4*3-40,this.height/5*4-10,80,20,new TranslatableComponent("text.openlink.no"),button -> {
            this.onClose();
        });
        text=MultiLineLabel.create(this.font,(FormattedText) new TranslatableComponent("text.openlink.updatefrpc", Frpc.latestVersionDate,Frpc.frpcVersionDate),this.width-50);
        this.addRenderableWidget(yes);
        this.addRenderableWidget(no);
        super.init();
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        this.renderBackground(poseStack);
        text.renderCentered(poseStack,this.width/2,this.height/10,16,0xffffff);
        super.render(poseStack,i,j,f);
    }
}