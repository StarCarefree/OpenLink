package fun.moystudio.openlink.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import fun.moystudio.openlink.frpcimpl.FrpcManager;
import fun.moystudio.openlink.frpcimpl.SakuraFrpFrpcImpl;
import fun.moystudio.openlink.logic.Utils;
import fun.moystudio.openlink.logic.WebBrowser;
import net.minecraft.client.Minecraft;
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
        this.addButton(token);
        this.addButton(new Button(this.width / 2 + 160, this.height / 2, 40, 20, CommonComponents.GUI_DONE, button -> {
            SakuraFrpFrpcImpl.token = token.getValue();
            if(SakuraFrpFrpcImpl.getUserInfo()!=null) {
                SakuraFrpFrpcImpl.writeSession();
                this.onClose();
            } else {
                SakuraFrpFrpcImpl.token = null;
            }
            SakuraFrpFrpcImpl.writeSession();
        }));
        this.addButton(new Button(this.width / 2 - 100, this.height / 6 * 4 , 200, 20, Utils.translatableText("text.openlink.no_account"), (button) -> new WebBrowser("https://www.natfrp.com/").openBrowser()));
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(last);
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        this.renderBackground(poseStack);
        drawCenteredString(poseStack, this.font, this.title, this.width/2, 15, 0xffffff);
        drawString(poseStack, this.font, Utils.translatableText("text.openlink.frptip", FrpcManager.getInstance().getCurrentFrpcName()),0, this.height-this.font.lineHeight, 0xffffff);
        super.render(poseStack, i, j, f);
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
