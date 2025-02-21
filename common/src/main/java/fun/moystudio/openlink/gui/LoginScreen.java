package fun.moystudio.openlink.gui;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mojang.blaze3d.vertex.PoseStack;
import fun.moystudio.openlink.json.JsonResponseWithData;
import fun.moystudio.openlink.logic.Utils;
import fun.moystudio.openlink.logic.WebBrowser;
import fun.moystudio.openlink.network.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;

public class LoginScreen extends Screen {
    public LoginScreen(Screen last) {
        super(Utils.translatableText("gui.openlink.loginscreentitle"));
        lastscreen=last;
    }
    Screen lastscreen;
    MultiLineLabel loginTips;
    EditBox authorization=new EditBox(Minecraft.getInstance().font, this.width / 2 - 100, this.height / 6 * 3, 150, 20, Utils.translatableText("text.openlink.authorization"));
    WebBrowser browser=new WebBrowser(Uris.openidLoginUri.toString());

    @Override
    protected void init() {
        loginTips = MultiLineLabel.create(this.font, Utils.translatableText("text.openlink.logintips"), this.width - 50);
        this.addRenderableWidget(new Button(this.width / 2 - 100, this.height / 6 * 2, 200, 20, Utils.translatableText("text.openlink.fastlogin"), (button) ->{
            Gson gson = new Gson();
            LoginGetCodeHttpServer codeHttpServer = new LoginGetCodeHttpServer();
            codeHttpServer.start();
            try {
                JsonResponseWithData<String> response = gson.fromJson(Request.POST("https://api.openfrp.net/oauth2/login?redirect_url=http://localhost:"+codeHttpServer.port,Request.DEFAULT_HEADER, "{}").getFirst(),new TypeToken<JsonResponseWithData<String>>(){}.getType());
                new WebBrowser(response.data).openBrowser();
                this.minecraft.keyboardHandler.setClipboard(response.data);
                button.active=false;
                this.minecraft.setScreen(new ConfirmScreenWithLanguageButton(confirmed -> {if(Request.Authorization!=null){this.onClose();}}, Utils.translatableText("text.openlink.fastlogin"), Utils.translatableText("text.openlink.fastloginconfirm")));
            } catch (Exception e) {
                e.printStackTrace();
                this.onClose();
            }
        }));
        authorization.setX(this.width / 2 - 100);
        authorization.y=this.height/2;
        this.addRenderableWidget(authorization);
        this.addRenderableWidget(new Button(this.width / 2 + 60, this.height / 2, 40, 20, CommonComponents.GUI_DONE, button -> {
            Request.Authorization = authorization.getValue();
            try {
                if(Request.getUserInfo()!=null&&Request.getUserInfo().flag){
                    this.onClose();
                } else {
                    Request.Authorization = null;
                }
            } catch (Exception e) {
                Request.Authorization = null;
            }
            Request.writeSession();
        }));
        //注册
        this.addRenderableWidget(new Button(this.width / 2 - 100, this.height / 6 * 4 , 200, 20, Utils.translatableText("text.openlink.no_account"), (button) -> browser.openBrowser()));
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        this.renderBackground(poseStack);
        loginTips.renderCentered(poseStack, this.width / 2, 15, 16, 0xffffff);
        super.render(poseStack, i, j, f);
    }

    @Override
    public void tick(){
        if(authorization.getValue().isBlank()){
            authorization.setSuggestion(Utils.translatableText("text.openlink.authorization").getString());
        } else {
            authorization.setSuggestion("");
        }
    }

    @Override
    public void onClose(){
        this.minecraft.setScreen(lastscreen);
    }
}
