package fun.moystudio.openlink.gui;

import fun.moystudio.openlink.frpcimpl.FrpcManager;
import fun.moystudio.openlink.frpcimpl.SakuraFrpFrpcImpl;
import fun.moystudio.openlink.logic.Utils;
import fun.moystudio.openlink.logic.WebBrowser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;

public class LoginScreenSakura extends Screen {
    Screen last;
    public LoginScreenSakura(Screen lastscreen) {
        super(Utils.translatableText("text.openlink.logintips_sakura"));
        last=lastscreen;
    }

    EditBox token = new EditBox(Minecraft.getInstance().font, this.width/2-200, this.height/2, 355, 20, Utils.translatableText("text.openlink.token"));

    @Override
    protected void init() {
        token = new EditBox(this.minecraft.font, this.width/2-200, this.height/2, 355, 20, token, Utils.translatableText("text.openlink.token"));
        token.setMaxLength(100);
        token.setSuggestion(Utils.translatableText("text.openlink.token").getString());
        this.addRenderableWidget(token);
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> {
            SakuraFrpFrpcImpl.token = token.getValue();
            if(SakuraFrpFrpcImpl.getUserInfo()!=null) {
                SakuraFrpFrpcImpl.writeSession();
                this.onClose();
            } else {
                SakuraFrpFrpcImpl.token = null;
            }
            SakuraFrpFrpcImpl.writeSession();
        }).bounds(this.width / 2 + 160, this.height / 2, 40, 20).build());
        this.addRenderableWidget(Button.builder(Utils.translatableText("text.openlink.no_account"), (button) -> new WebBrowser("https://www.natfrp.com/").openBrowser()).bounds(this.width / 2 - 100, this.height / 6 * 4 , 200, 20).build());
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(last);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
        super.render(guiGraphics, i, j, f);
        guiGraphics.drawCenteredString(this.font, this.title, this.width/2, 15, 0xffffff);
        guiGraphics.drawString(this.font, Utils.translatableText("text.openlink.frptip", FrpcManager.getInstance().getCurrentFrpcName()),0, this.height-this.font.lineHeight, 0xffffff);

    }

    @Override
    public void tick() {
        if(token.getValue().isEmpty()||token.getValue().isBlank()) {
            token.setSuggestion(Utils.translatableText("text.openlink.token").getString());
        }
        else {
            token.setSuggestion("");
        }
    }
}
