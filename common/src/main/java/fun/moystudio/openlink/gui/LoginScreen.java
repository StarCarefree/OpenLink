package fun.moystudio.openlink.gui;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mojang.datafixers.util.Pair;
import fun.moystudio.openlink.OpenLink;
import fun.moystudio.openlink.json.JsonResponseWithCode;
import fun.moystudio.openlink.json.JsonResponseWithData;
import fun.moystudio.openlink.logic.Utils;
import fun.moystudio.openlink.logic.WebBrowser;
import fun.moystudio.openlink.network.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.options.LanguageSelectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.*;
import java.util.List;
import java.util.Map;

public class LoginScreen extends Screen {
    public LoginScreen(Screen last) {
        super(Utils.translatableText("gui.openlink.loginscreentitle"));
        lastscreen=last;
    }
    Screen lastscreen;
    MultiLineLabel loginTips;
    EditBox username;
    EditBox password;
    String wrongmsg = "";
    Checkbox remember;
    WebBrowser browser=new WebBrowser(Uris.openidLoginUri.toString());

    @Override
    protected void init() {
        loginTips = MultiLineLabel.create(this.font, Utils.translatableText("text.openlink.logintips"), this.width - 50);

        username = new EditBox(this.font, this.width / 2 - 100, this.height / 6 + 58, 200, 20, Utils.translatableText("text.openlink.username"));
        password = new EditBox(this.font, this.width / 2 - 100, this.height / 6 + 98, 200, 20, Utils.translatableText("text.openlink.password"));
        username.setValue(OpenLink.PREFERENCES.get("last_username",""));
        password.setValue(OpenLink.PREFERENCES.get("last_password",""));
        remember=Checkbox.builder(Utils.translatableText("text.openlink.rememberuserandpassword"), this.font).pos(this.width / 2 - 100,this.height/6+133).selected(OpenLink.PREFERENCES.get("last_username",null)!=null&&OpenLink.PREFERENCES.get("last_password",null)!=null).build();
        this.addRenderableWidget(username);
        this.addRenderableWidget(password);
        this.addRenderableWidget(remember);
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (button) -> {
            if (this.username.getValue().isBlank() || this.password.getValue().isBlank()) {
                wrongmsg = Utils.translatableText("text.openlink.notcompleted").getString();
                return;
            }
            wrongmsg = "";
            Pair<String, Map<String, List<String>>> response;
            Gson gson = new Gson();
            try {
                response = Request.POST(Uris.openidLoginUri.toString() + "api/public/login", Request.DEFAULT_HEADER, "{\"user\":\"" + username.getValue() + "\",\"password\":\"" + password.getValue() + "\"}");
            } catch (Exception e) {
                wrongmsg = e.getMessage();
                e.printStackTrace();
                return;
            }

            JsonResponseWithCode<?> loginFlag = gson.fromJson(response.getFirst(), JsonResponseWithCode.class);
            if (!loginFlag.flag) {
                wrongmsg = loginFlag.msg;
                return;
            }
            Map<String, List<String>> headerWithCookie = Request.getHeaderWithCookieByResponse(response, Request.DEFAULT_HEADER);
            try {
                response = Request.POST(Uris.openidLoginUri + "api/oauth2/authorize?response_type=code&redirect_uri=" + Uris.openFrpAPIUri + "oauth_callback&client_id=openfrp", headerWithCookie, "{}");
            } catch (Exception e) {
                wrongmsg = e.getMessage();
                e.printStackTrace();
                return;
            }
            JsonResponseWithData<Map<String, String>> loginCode = gson.fromJson(response.getFirst(), new TypeToken<JsonResponseWithData<Map<String, String>>>(){}.getType()); // 返回的code（用于下一步传参）
            String code = loginCode.data.get("code");
            try {
                Request.POST(Uris.openFrpAPIUri.toString() + "oauth2/callback?code=" + code, headerWithCookie, "{}");
            } catch (Exception e) {
                wrongmsg = e.getMessage();
                e.printStackTrace();
                return;
            }
            //Authorization会在POST里写储存和放置到注册表的
            if(remember.selected()){
                OpenLink.PREFERENCES.put("last_username",this.username.getValue());
                OpenLink.PREFERENCES.put("last_password",this.password.getValue());
            }
            else {
                OpenLink.PREFERENCES.remove("last_username");
                OpenLink.PREFERENCES.remove("last_password");
            }
            this.onClose();
        }).bounds(this.width / 2 - 100, this.height / 6 + 178, 200, 20).build());

        //原版语言按钮
        SpriteIconButton spriteiconbutton = this.addRenderableWidget(CommonButtons.language(20, (arg) -> this.minecraft.setScreen(new LanguageSelectScreen(this, this.minecraft.options, this.minecraft.getLanguageManager())), true));
        spriteiconbutton.setPosition(this.width / 2 - 130, this.height / 6 + 178);

        //注册
        this.addRenderableWidget(Button.builder(Utils.translatableText("text.openlink.no_account"), (button) -> browser.openBrowser()).bounds(this.width / 2 - 100, this.height / 6 + 158 , 200, 20).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
        super.render(guiGraphics, i, j, f);
        loginTips.renderCentered(guiGraphics, this.width / 2, 15, 16, 0xffffff);
        guiGraphics.drawString(this.font, Utils.translatableText("text.openlink.username"), this.width / 2 - 100, this.height / 6 + 33, 0xffffff);
        guiGraphics.drawString(this.font, Utils.translatableText("text.openlink.password"), this.width / 2 - 100, this.height / 6 + 83, 0xffffff);
        guiGraphics.drawString(this.font, Utils.literalText(wrongmsg), this.width / 2 - 100, this.height / 6 + 123, 0xff0000);

    }

    @Override
    public void onClose(){
        this.minecraft.setScreen(lastscreen);
    }
}
