package fun.moystudio.openlink.gui;

import fun.moystudio.openlink.OpenLink;
import fun.moystudio.openlink.frpc.Frpc;
import fun.moystudio.openlink.logic.LanConfig;
import fun.moystudio.openlink.logic.OnlineModeTabs;
import fun.moystudio.openlink.logic.Utils;
import fun.moystudio.openlink.logic.UUIDFixer;
import fun.moystudio.openlink.network.Request;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.HttpUtil;
import net.minecraft.world.level.GameType;

import java.util.Collections;

public class NewShareToLanScreen extends Screen {
    private static final Component ALLOW_COMMANDS_LABEL = Utils.translatableText("selectWorld.allowCommands");
    private static final Component GAME_MODE_LABEL = Utils.translatableText("selectWorld.gameMode");
    private static final Component INFO_TEXT = Utils.translatableText("lanServer.otherPlayers");
    private final Screen lastScreen;
    private GameType gameMode;

    private static final ResourceLocation SETTING = Utils.createResourceLocation("openlink", "textures/gui/setting_button.png");

    private static final ResourceLocation SETTING_HOVERED = Utils.createResourceLocation("openlink", "textures/gui/setting_button_hovered.png");

    EditBox editBox;

    EditBox editBox2;

    CycleButton<Boolean> usingfrp;

    CycleButton<OnlineModeTabs> onlinemode;

    CycleButton<Boolean> allowpvp;

    Button nodeselection;

    boolean couldShare=true;

    public NewShareToLanScreen(Screen screen) {
        super(Utils.translatableText("lanServer.title"));
        this.gameMode = GameType.SURVIVAL;
        this.lastScreen = screen;
    }

    @Override
    public void tick(){
        couldShare=true;
        String val2=editBox2.getValue();
        if(val2.isBlank()){
            editBox2.setSuggestion(Utils.translatableText("text.openlink.local_port").getString());
        } else {
            editBox2.setSuggestion("");
        }
        if((val2.length()<4||val2.length()>5)&&!val2.isBlank()){
            couldShare=false;
        }
        boolean _0=true;
        for(int i=0;i<val2.length();i++){
            if(i==0&&val2.charAt(i)=='0'){
                couldShare=false;
            }
            if(val2.charAt(i)!='0') _0=false;
            if(!Character.isDigit(val2.charAt(i))){
                couldShare=false;
            }
        }
        if(_0&&!val2.isBlank()){
            couldShare=false;
        }
        if(OpenLink.disabled) return;
        String val = editBox.getValue();
        editBox.setVisible(LanConfig.cfg.use_frp);
        if(Request.Authorization==null){
            LanConfig.cfg.use_frp=false;
            editBox.setValue("");
            usingfrp.setValue(false);
            usingfrp.active=false;
            editBox.active=false;
            nodeselection.active=false;
            return;
        } else {
            usingfrp.active=true;
        }
        if(val.isBlank()){
            editBox.setSuggestion(Utils.translatableText("text.openlink.remote_port").getString());
            return;
        } else {
            editBox.setSuggestion("");
        }
        if(LanConfig.cfg.use_frp) {
            if (val.length() != 5) {
                couldShare = false;
                return;
            }
            _0 = true;
            for (int i = 0; i < val.length(); i++) {
                if (i == 0 && val.charAt(i) == '0') {
                    couldShare = false;
                    return;
                }
                if (val.charAt(i) != '0') _0 = false;
                if (!Character.isDigit(val.charAt(i))) {
                    couldShare = false;
                    return;
                }
            }
            if (_0) {
                couldShare = false;
            }
        }
    }

    protected void init() {
        this.addRenderableWidget(CycleButton.builder(GameType::getShortDisplayName).withValues(GameType.SURVIVAL, GameType.SPECTATOR, GameType.CREATIVE, GameType.ADVENTURE).withInitialValue(this.gameMode).create(this.width / 2 - 155, 100, 150, 20, GAME_MODE_LABEL, (cycleButton, gameType) -> this.gameMode = gameType));
        this.addRenderableWidget(CycleButton.onOffBuilder(LanConfig.cfg.allow_commands).create(this.width / 2 + 5, 100, 150, 20, ALLOW_COMMANDS_LABEL, (cycleButton, boolean_) -> LanConfig.cfg.allow_commands = boolean_));
        this.addRenderableWidget(Button.builder(Utils.translatableText("lanServer.start"), (button1) -> {
            if(!this.couldShare)return;
            this.minecraft.setScreen(null);
            int i = editBox2.getValue().isBlank()?HttpUtil.getAvailablePort():Integer.parseInt(editBox2.getValue());
            if (this.minecraft.getSingleplayerServer().publishServer(this.gameMode, LanConfig.cfg.allow_commands, i)) {
                this.minecraft.gui.getChat().addMessage(Utils.translatableText("commands.publish.started", i));
                this.minecraft.updateTitle();
            } else {
                this.minecraft.gui.getChat().addMessage(Utils.translatableText("commands.publish.failed"));
                this.minecraft.updateTitle();
                return;
            }

            this.minecraft.getSingleplayerServer().setUsesAuthentication(LanConfig.getAuthMode()==OnlineModeTabs.ONLINE_MODE);
            this.minecraft.getSingleplayerServer().setPvpAllowed(LanConfig.cfg.allow_pvp);
            UUIDFixer.EnableUUIDFixer=LanConfig.getAuthMode()==OnlineModeTabs.OFFLINE_FIXUUID;
            UUIDFixer.ForceOfflinePlayers=Collections.emptyList();//暂时用着，后面再改
            //以上是(被我修改了一点的)原版的代码，以下是OpenLink的Frpc启动及隧道创建，节点选择等主要功能
            if(OpenLink.disabled) return;
            if(!LanConfig.cfg.use_frp){
                return;
            }
            Frpc.openFrp(i,editBox.getValue());
            try {
                LanConfig.writeConfig();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).bounds(this.width / 2 - 155, this.height - 28, 150, 20).tooltip(getToolTip()).build());
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, (button) -> this.minecraft.setScreen(this.lastScreen)).bounds(this.width / 2 + 5, this.height - 28, 150, 20).build());
        editBox2=new EditBox(this.font,this.width/2-(OpenLink.disabled||!LanConfig.cfg.use_frp?75:155),OpenLink.disabled?160:190,150,20,Utils.translatableText("text.openlink.local_port"));
        editBox2.setSuggestion(Utils.translatableText("text.openlink.local_port").getString());
        this.addRenderableWidget(editBox2);
        onlinemode=CycleButton.builder((OnlineModeTabs o)-> o.component)
                .withValues(OnlineModeTabs.values())
                .withInitialValue(LanConfig.getAuthMode())
                .create(this.width / 2 - 155, 130, 150, 20, Utils.translatableText("text.openlink.onlinemodebutton"),   (button, o)->LanConfig.setAuthMode(o));
        allowpvp=CycleButton.onOffBuilder(LanConfig.cfg.allow_pvp).create(this.width / 2 + 5, 130, 150, 20, Utils.translatableText("mco.configure.world.pvp"),(cycleButton, object) -> LanConfig.cfg.allow_pvp=object);
        this.addRenderableWidget(onlinemode);
        this.addRenderableWidget(allowpvp);
        if(OpenLink.disabled) return;
        editBox=new EditBox(this.font,this.width / 2 + 5, 190, 150, 20, Utils.translatableText("text.openlink.remote_port"));
        editBox.setSuggestion(Utils.translatableText("text.openlink.remote_port").getString());
        editBox.setValue(LanConfig.cfg.last_port_value);
        this.addRenderableWidget(editBox);
        nodeselection=Button.builder(Utils.translatableText("gui.openlink.nodeselectionscreentitle"),(button)-> this.minecraft.setScreen(new NodeSelectionScreen(this))).bounds(this.width/2+5,160,150,20).build();
        nodeselection.active=LanConfig.cfg.use_frp;
        usingfrp=CycleButton.onOffBuilder(LanConfig.cfg.use_frp).create(this.width / 2 - 155, 160, 150, 20, Utils.translatableText("text.openlink.usingfrp"),((cycleButton, bool) -> {
            LanConfig.cfg.use_frp=bool;
            editBox.active=bool;
            nodeselection.active=bool;
            editBox2.setX(this.width/2-(OpenLink.disabled||!bool?75:155));
        }));
        this.addRenderableWidget(nodeselection);
        this.addRenderableWidget(usingfrp);
        this.addRenderableWidget(new ImageButtonWithHoveredState(this.width / 2 + 5 + 150 + 10, this.height - 28,
                20, 20, 0, 0, 20, SETTING, SETTING_HOVERED, 20, 20, (button) -> this.minecraft.setScreen(new SettingScreen(this))));
    }

    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
        this.renderBackground(guiGraphics);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 50, 16777215);
        guiGraphics.drawCenteredString(this.font, INFO_TEXT, this.width / 2, 82, 16777215);
        super.render(guiGraphics, i, j, f);
    }

    private Tooltip getToolTip(){
        if(OpenLink.disabled) return Tooltip.create(Utils.EMPTY);
        if(Request.Authorization==null){
            MutableComponent component=(MutableComponent) Utils.EMPTY;
            if(component.getSiblings().isEmpty()){
                String[] list1=Utils.translatableText("text.openlink.lanlogintips").getString().split("\n");
                for(String s:list1){
                    component.append(Utils.literalText(s));
                }
                return Tooltip.create(component);
            }
        }
        return Tooltip.create(Utils.EMPTY);
    }
}
