package fun.moystudio.openlink.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import fun.moystudio.openlink.OpenLink;
import fun.moystudio.openlink.frpc.FrpcManager;
import fun.moystudio.openlink.logic.Utils;
import net.minecraft.Util;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.LanguageSelectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UpdateScreen extends Screen {
    public UpdateScreen() {
        super(Utils.translatableText("gui.openlink.updatefrpctitle"));
    }
    public Button yes;
    public Button no;
    public MultiLineLabel text;

    @Override
    protected void init(){
        List<String> strings=Arrays.asList(Utils.translatableText("text.openlink.nofrpcfile").getString().split("\n"));
        List<Component> list = new ArrayList<>();
        strings.forEach((String)-> list.add(Utils.literalText(String)));
        yes=new Button(this.width/4-40,this.height/5*4-10,80,20,CommonComponents.GUI_YES,button -> this.minecraft.setScreen(new UpdatingScreen()));
        no=new Button(this.width/4*3-40,this.height/5*4-10,80,20,CommonComponents.GUI_NO,button -> {if(!FrpcManager.getInstance().isExecutableFileExist(FrpcManager.getInstance().getCurrentFrpcId()))OpenLink.disabled=true;this.onClose();}, (button, poseStack, i, j) -> {
            if(!FrpcManager.getInstance().isExecutableFileExist(FrpcManager.getInstance().getCurrentFrpcId())){
                renderComponentTooltip(poseStack, list, i, j);
            }
        });
        text=MultiLineLabel.create(this.font, Utils.translatableText("text.openlink.updatefrpc"),this.width-50);
        this.addButton(yes);
        this.addButton(no);
        this.addButton(new Button(this.width/2-60, this.height/5*4-10, 120, 20, Utils.translatableText("text.openlink.openstoragedir"), button -> {
            Util.getPlatform().openFile(FrpcManager.getInstance().getFrpcStoragePathById(FrpcManager.getInstance().getCurrentFrpcId()).toFile());
        }));
        //以下为原版语言按钮(修改了一下位置)
        this.addButton(new ImageButton(this.width/4-70, this.height/5*4-10, 20, 20, 0, 106, 20, Button.WIDGETS_LOCATION, 256, 256, (button) -> this.minecraft.setScreen(new LanguageSelectScreen(this, this.minecraft.options, this.minecraft.getLanguageManager())), Utils.translatableText("narrator.button.language")));
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        this.renderBackground(poseStack);
        text.renderCentered(poseStack,this.width/2,this.height/10,16,0xffffff);
        super.render(poseStack,i,j,f);
    }

    @Override
    public void onClose(){
        this.minecraft.setScreen(null);
    }
}
