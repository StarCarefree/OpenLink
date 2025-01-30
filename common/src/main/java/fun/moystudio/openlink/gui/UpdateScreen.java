package fun.moystudio.openlink.gui;

import fun.moystudio.openlink.frpc.Frpc;
import fun.moystudio.openlink.logic.Utils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.options.LanguageSelectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.*;

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

        yes=Button.builder(CommonComponents.GUI_YES,button -> this.minecraft.setScreen(new UpdatingScreen())).bounds(this.width/4-40,this.height/5*4-10,80,20).build();
        no=Button.builder(CommonComponents.GUI_NO,button -> this.onClose()).tooltip(getTooltip()).bounds(this.width/4*3-40,this.height/5*4-10,80,20).build();
        if(Frpc.FRPC_VERSION.length()<6){
            no.active=false;
        }
        text=MultiLineLabel.create(this.font, Utils.translatableText("text.openlink.updatefrpc", Frpc.latestVersion, Frpc.FRPC_VERSION.length()<6 ? "does not exist" : Frpc.FRPC_VERSION),this.width-50);
        this.addRenderableWidget(yes);
        this.addRenderableWidget(no);
        //以下为原版语言按钮(修改了一下位置)
        SpriteIconButton spriteiconbutton = this.addRenderableWidget(CommonButtons.language(20, (arg) -> this.minecraft.setScreen(new LanguageSelectScreen(this, this.minecraft.options, this.minecraft.getLanguageManager())), true));
        spriteiconbutton.setPosition(this.width/4-70, this.height/5*4-10);
    }

    private Tooltip getTooltip(){
        List<String> strings=Arrays.asList(Utils.translatableText("text.openlink.nofrpcfile").getString().split("\n"));
        MutableComponent component= (MutableComponent) Utils.EMPTY;
        strings.forEach((String)-> component.append(Utils.literalText(String)));
        if(Frpc.FRPC_VERSION.length()<6){
            return Tooltip.create(component);
        }
        return Tooltip.create(Utils.EMPTY);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
        super.render(guiGraphics,i,j,f);
        text.renderCentered(guiGraphics,this.width/2,this.height/10,16,0xffffff);
    }
}
