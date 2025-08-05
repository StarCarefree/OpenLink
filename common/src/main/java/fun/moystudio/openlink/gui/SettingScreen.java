package fun.moystudio.openlink.gui;

//也是ds写得，我也搞不懂这些玩意

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import fun.moystudio.openlink.OpenLink;
import fun.moystudio.openlink.frpcimpl.FrpcManager;
import fun.moystudio.openlink.frpcimpl.OpenFrpFrpcImpl;
import fun.moystudio.openlink.frpcimpl.SakuraFrpFrpcImpl;
import fun.moystudio.openlink.json.JsonResponseWithData;
import fun.moystudio.openlink.json.JsonUserInfo;
import fun.moystudio.openlink.json.JsonUserInfoSakura;
import fun.moystudio.openlink.json.JsonUserProxySakura;
import fun.moystudio.openlink.logic.SettingTabs;
import fun.moystudio.openlink.logic.Utils;
import fun.moystudio.openlink.logic.WebBrowser;
import fun.moystudio.openlink.mixin.IScreenAccessor;
import fun.moystudio.openlink.network.Request;
import fun.moystudio.openlink.network.Uris;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public class SettingScreen extends Screen {
    public SettingScreen(Screen last) {
        super(Utils.translatableText("gui.openlink.settingscreentitle"));
        informationList=getInformationList(FrpcManager.getInstance().getCurrentFrpcInstance().getFrpcVersion(FrpcManager.getInstance().getFrpcImplExecutableFile(FrpcManager.getInstance().getCurrentFrpcId())),OpenLink.VERSION,OpenLink.LOADER+" "+OpenLink.LOADER_VERSION);
        lastscreen=last;
    }
    MultiLineLabel title;
    Screen lastscreen;
    SettingTabs tab=SettingTabs.USER;
    SettingTabs lasttab=null;
    SettingScreenButton buttonLog,buttonInfo,buttonUser,buttonSetting;
    JsonResponseWithData<JsonUserInfo> userInfo=null;
    JsonUserInfoSakura userInfoSakura=null;
    JsonUserProxySakura userProxySakura=null;
    public List<Widget> renderableTabWidgets,tabLog=new ArrayList<>(),tabInfo=new ArrayList<>(),tabUser=new ArrayList<>(),tabLogin_User=new ArrayList<>(), tabSetting=new ArrayList<>();
    public static List<InfoObjectSelectionList.Information> informationList;
    public static final ResourceLocation BACKGROUND_SETTING=Utils.createResourceLocation("openlink","textures/gui/background_setting.png"), FRP_BUTTON = Utils.createResourceLocation("openlink", "textures/gui/frp_change_button.png"), FRP_BUTTON_HOVERED = Utils.createResourceLocation("openlink", "textures/gui/frp_change_button_hovered.png");
    public static boolean sensitiveInfoHiding, unavailableNodeHiding;

    private static List<InfoObjectSelectionList.Information> getInformationList(Object... objects) {
        String[] lines= Utils.translatableText("text.openlink.info",objects).getString().split("\n");
        List<InfoObjectSelectionList.Information> informations=new ArrayList<>();
        for (String line:lines){
            if(line.startsWith("#")||line.isEmpty()){
                continue;
            }
            if(line.startsWith("1")){
                informations.add(new InfoObjectSelectionList.Information(Utils.literalText(line.substring(1)),true));
            }
            else if(line.startsWith("0")){
                informations.add(new InfoObjectSelectionList.Information(Utils.literalText(line.substring(1)),false));
            }
            else{
                informations.add(new InfoObjectSelectionList.Information(Utils.literalText(line),false));
            }
        }
        return informations;
    }

    @Override
    public void onClose(){
        this.minecraft.setScreen(lastscreen);
    }

    @Override
    public void init(){
        title=MultiLineLabel.create(this.font, Utils.translatableText("gui.openlink.settingscreentitle"));
        int i=(this.width-10)/4;
        buttonUser=new SettingScreenButton(5,40,i,20,SettingTabs.USER.component,(button -> tab=SettingTabs.USER));
        buttonLog=new SettingScreenButton(5+i,40,i,20,SettingTabs.LOG.component,(button -> tab=SettingTabs.LOG));
        buttonInfo=new SettingScreenButton(5+i*2,40,i,20,SettingTabs.INFO.component,(button -> tab=SettingTabs.INFO));
        buttonSetting=new SettingScreenButton(5+i*3,40,i,20,SettingTabs.SETTING.component,(button -> tab=SettingTabs.SETTING));
        addRenderableWidget(buttonLog);
        addRenderableWidget(buttonInfo);
        addRenderableWidget(buttonUser);
        addRenderableWidget(buttonSetting);
        //Temp variables
        if(FrpcManager.getInstance().getCurrentFrpcId().equals("openfrp")){
            ResourceLocation lastlocationimage=!tabUser.isEmpty()?((ImageWidget)tabUser.get(0)).texture:Utils.createResourceLocation("openlink","textures/gui/default_avatar.png");
            Component lastcomponent1=tabUser.size()>=2?((ComponentWidget)tabUser.get(1)).component: Utils.emptyText();
            Component lastcomponent2=tabUser.size()>=3?((ComponentWidget)tabUser.get(2)).component: Utils.emptyText();
            Component lastcomponent3=tabUser.size()>=4?((ComponentWidget)tabUser.get(3)).component: Utils.emptyText();
            Component lastcomponent4=tabUser.size()>=5?((ComponentWidget)tabUser.get(4)).component: Utils.emptyText();
            Component lastcomponent5=tabUser.size()>=6?((ComponentWidget)tabUser.get(5)).component: Utils.emptyText();
            int lastx2=tabUser.size()>=3?((ComponentWidget)tabUser.get(2)).x:10;
            List<Pair<String,Long>> lastdatapoints=tabUser.size()>=7?((LineChartWidget)tabUser.get(6)).dataPoints:readTraffic();
            tabUser.clear();
            //UserInfo排版用
            int j=Math.min((this.width-20)/4,(this.height-75)/5*3);
            //UserInfo
            tabUser.add(new ImageWidget(10,65,0,0,j,j,j,j,lastlocationimage));
            tabUser.add(new ComponentWidget(this.font,10,65+j+5,0xffffff,lastcomponent1,false));
            tabUser.add(new ComponentWidget(this.font,lastx2,65+j+5,0xacacac,lastcomponent2,false));
            tabUser.add(new ComponentWidget(this.font,10,65+j+5+10,0xacacac,lastcomponent3,false));
            tabUser.add(new ComponentWidget(this.font,10,65+j+5+20,0xacacac,lastcomponent4,false));
            tabUser.add(new ComponentWidget(this.font,10,65+j+5+30,0xacacac,lastcomponent5,false));
            tabUser.add(new LineChartWidget(
                    this, // 传入当前 Screen 实例
                    this.font,
                    10+j+20, 65+5,
                    this.width-20, 60+this.height-75-15,
                    Utils.translatableText("text.openlink.x_axis_label"),
                    Utils.translatableText("text.openlink.y_axis_label"),
                    lastdatapoints));
            tabUser.add(new Button(10,65+j+5+40,j-25,20,Utils.translatableText("text.openlink.logout"),button -> {
                FrpcManager.getInstance().getCurrentFrpcInstance().logOut();
                this.minecraft.setScreen(new SettingScreen(lastscreen));
            }));
            tabUser.add(new ImageButtonWithHoveredState(10+j-25+5, 65+j+5+40, 20, 20, 0, 0, 20, FRP_BUTTON, FRP_BUTTON_HOVERED, 20, 20, button -> {
                this.minecraft.setScreen(new FrpcImplSelectionScreen(new SettingScreen(lastscreen)));
            }));
        } else if(FrpcManager.getInstance().getCurrentFrpcId().equals("sakurafrp")) {
            ResourceLocation lastlocationimage=!tabUser.isEmpty()?((ImageWidget)tabUser.get(0)).texture:Utils.createResourceLocation("openlink","textures/gui/sakurafrp_icon.png");
            Component lastcomponent1=tabUser.size()>=2?((ComponentWidget)tabUser.get(1)).component: Utils.emptyText();
            Component lastcomponent2=tabUser.size()>=3?((ComponentWidget)tabUser.get(2)).component: Utils.emptyText();
            Component lastcomponent3=tabUser.size()>=4?((ComponentWidget)tabUser.get(3)).component: Utils.emptyText();
            Component lastcomponent4=tabUser.size()>=5?((ComponentWidget)tabUser.get(4)).component: Utils.emptyText();
            Component lastcomponent5=tabUser.size()>=6?((ComponentWidget)tabUser.get(5)).component: Utils.emptyText();
            int lastx2=tabUser.size()>=3?((ComponentWidget)tabUser.get(2)).x:10;
            List<Pair<String,Long>> lastdatapoints=tabUser.size()>=7?((LineChartWidget)tabUser.get(6)).dataPoints:readTrafficSakura();
            tabUser.clear();
            int j=Math.min((this.width-20)/4,(this.height-75)/5*3);
            tabUser.add(new ImageWidget(10,65,0,0,j,j,j,j,lastlocationimage));
            tabUser.add(new ComponentWidget(this.font,10,65+j+5,0xffffff,lastcomponent1,false));
            tabUser.add(new ComponentWidget(this.font,lastx2,65+j+5,0xacacac,lastcomponent2,false));
            tabUser.add(new ComponentWidget(this.font,10,65+j+5+10,0xacacac,lastcomponent3,false));
            tabUser.add(new ComponentWidget(this.font,10,65+j+5+20,0xacacac,lastcomponent4,false));
            tabUser.add(new ComponentWidget(this.font,10,65+j+5+30,0xacacac,lastcomponent5,false));
            tabUser.add(new LineChartWidget(
                    this, // 传入当前 Screen 实例
                    this.font,
                    10+j+20, 65+5,
                    this.width-20, 60+this.height-75-15,
                    Utils.translatableText("text.openlink.x_axis_label"),
                    Utils.translatableText("text.openlink.y_axis_label"),
                    lastdatapoints));
            tabUser.add(new Button(10,65+j+5+40,j-25,20,Utils.translatableText("text.openlink.logout"),button -> {
                FrpcManager.getInstance().getCurrentFrpcInstance().logOut();
                this.minecraft.setScreen(new SettingScreen(lastscreen));
            }));
            tabUser.add(new ImageButtonWithHoveredState(10+j-25+5, 65+j+5+40, 20, 20, 0, 0, 20, FRP_BUTTON, FRP_BUTTON_HOVERED, 20, 20, button -> {
                this.minecraft.setScreen(new FrpcImplSelectionScreen(new SettingScreen(lastscreen)));
            }));
        } else {
            tabUser.clear();
            ResourceLocation icon = FrpcManager.getInstance().getCurrentFrpcInstance().getIcon();
            if(icon!=null){
                tabUser.add(new ImageWidget(this.width/2-32,this.height/2+50-10-64-5,0,0,64,64,64,64,icon));
            }
            tabUser.add(new Button(this.width/2-20,this.height/2+50-10,40,20,Utils.translatableText("text.openlink.logout"),button -> {
                FrpcManager.getInstance().getCurrentFrpcInstance().logOut();
                this.minecraft.setScreen(new SettingScreen(lastscreen));
            }));
        }

        LogObjectSelectionList lastlogselectionlist=!tabLog.isEmpty()?((LogObjectSelectionList)tabLog.get(0)):new LogObjectSelectionList(minecraft,this.buttonSetting.x+this.buttonSetting.getWidth()-5,this.height-5-65,5,65,this.buttonSetting.x+this.buttonSetting.getWidth(),this.height-5,40);
        lastlogselectionlist.changePos(this.buttonSetting.x+this.buttonSetting.getWidth()-5,this.height-5-65,5,65,this.buttonSetting.x+this.buttonSetting.getWidth(),this.height-5);
        InfoObjectSelectionList lastinfoselectionlist=!tabInfo.isEmpty()?((InfoObjectSelectionList)tabInfo.get(0)):new InfoObjectSelectionList(minecraft,this.buttonSetting.x+this.buttonSetting.getWidth()-5,this.height-5-65,5,65,this.buttonSetting.x+this.buttonSetting.getWidth(),this.height-5,informationList.size()*(this.minecraft.font.lineHeight+5)+5);
        lastinfoselectionlist.changePos(this.buttonSetting.x+this.buttonSetting.getWidth()-5,this.height-5-65,5,65,this.buttonSetting.x+this.buttonSetting.getWidth(),this.height-5);
        //Clear tabs
        tabLogin_User.clear();
        tabLog.clear();
        tabSetting.clear();
        tabInfo.clear();
        //UserInfo的Login分屏
        Screen loginScreen = FrpcManager.getInstance().getCurrentFrpcInstance().getLoginScreen(new SettingScreen(lastscreen));
        ResourceLocation icon = FrpcManager.getInstance().getCurrentFrpcInstance().getIcon();
        if(loginScreen != null && icon == null){
            tabLogin_User.add(new Button(this.width/2-20,(this.height-75)/2+60-10,40,20, Utils.translatableText("text.openlink.login"),(button -> this.minecraft.setScreen(loginScreen))));
        } else if(loginScreen == null && icon != null){
            tabLogin_User.add(new ImageWidget(this.width/2-32,(this.height-75)/2+60-32,0,0,64,64,64,64,icon));
        } else if(loginScreen != null) {
            tabLogin_User.add(new ImageWidget(this.width/2-20-32,(this.height-75)/2+60-32,0,0,64,64,64,64,icon));
            tabLogin_User.add(new Button(this.width/2+20,(this.height-75)/2+60-10,40,20, Utils.translatableText("text.openlink.login"),(button -> this.minecraft.setScreen(loginScreen))));
        } else {
            tabLogin_User.add(new ComponentWidget(this.font, this.width/2, (this.height-75)/2+60-10, 0xffffff, Utils.translatableText("temp.openlink.tobedone"), true));
        }
        //Log
        tabLog.add(lastlogselectionlist);
        //Info
        tabInfo.add(lastinfoselectionlist);
        //Setting
        tabSetting.add(new ChartWidget(10,65,this.buttonSetting.x+this.buttonSetting.getWidth()-10-5,40, Utils.translatableText("text.openlink.secure"),0x8f2b2b2b));
        tabSetting.add(new ChartWidget(10,65+40+10, this.buttonSetting.x+this.buttonSetting.getWidth()-10-5, 40, Utils.translatableText("text.openlink.nodes"),0x8f2b2b2b));
        tabSetting.add(new ComponentWidget(this.font,15,87,0xffffff, Utils.translatableText("setting.openlink.information_show"),false));
        tabSetting.add(new ComponentWidget(this.font,15,87+40+10,0xffffff, Utils.translatableText("setting.openlink.node_hide"),false));
        tabSetting.add(CycleButton.onOffBuilder(sensitiveInfoHiding).displayOnlyValue().create(this.buttonSetting.x+this.buttonSetting.getWidth()-75-5,80,75,20, Utils.translatableText("setting.information_show"),(cycleButton, object) -> {
            sensitiveInfoHiding = object;
            OpenLink.PREFERENCES.putBoolean("setting_sensitive_info_hiding", object);
        }));
        tabSetting.add(CycleButton.onOffBuilder(unavailableNodeHiding).displayOnlyValue().create(this.buttonSetting.x+this.buttonSetting.getWidth()-75-5,80+40+10,75,20, Utils.translatableText("setting.information_show"),(cycleButton, object) -> {
            unavailableNodeHiding = object;
            OpenLink.PREFERENCES.putBoolean("setting_unavailable_node_hiding", object);
        }));
        tabSetting.add(new Button(this.width/2-150-5,65+40+10+40+10,150,20,Utils.translatableText("text.openlink.wiki"), button -> {
            this.minecraft.keyboardHandler.setClipboard(Uris.wikiUri.toString());
            new WebBrowser(Uris.wikiUri.toString()).openBrowser();
        }));
        tabSetting.add(new Button(this.width/2+5,65+40+10+40+10,150,20,Utils.translatableText("text.openlink.openstoragedir"), button -> {
            Util.getPlatform().openFile(FrpcManager.getInstance().getFrpcStoragePathById(FrpcManager.getInstance().getCurrentFrpcId()).toFile());
        }));
        String url = FrpcManager.getInstance().getCurrentFrpcInstance().getPanelUrl();
        if(url != null) {
            tabSetting.add(new Button(this.width/2-150-5,65+40+10+40+10+20+10,150,20,Utils.translatableText("text.openlink.webpanel", FrpcManager.getInstance().getCurrentFrpcName()),button -> {
                this.minecraft.keyboardHandler.setClipboard(url);
                new WebBrowser(url).openBrowser();
            }));
            tabSetting.add(new Button(this.width/2+5, 65+40+10+40+10+20+10, 150, 20, Utils.translatableText("gui.openlink.frpcselectionscreentitle"), button -> {
                this.minecraft.setScreen(new FrpcImplSelectionScreen(new SettingScreen(lastscreen)));
            }));
        }
        else {
            tabSetting.add(new Button(this.width/2-75, 65+40+10+40+10+20+10, 150, 20, Utils.translatableText("gui.openlink.frpcselectionscreentitle"), button -> {
                this.minecraft.setScreen(new FrpcImplSelectionScreen(new SettingScreen(lastscreen)));
            }));
        }
    }

    //MouseEventsOverrideBegin
    public List<? extends GuiEventListener> getChildrenWithTabRenderables(){
        List<GuiEventListener> list=(((IScreenAccessor)this).getChildren());
        if(renderableTabWidgets!=null){
            renderableTabWidgets.forEach(widget -> {
                if (widget instanceof GuiEventListener guiEventListener) {
                    list.add(guiEventListener);
                }
            });
        }
        return list;
    }

    @Override
    public boolean mouseClicked(double d, double e, int i) {
        if(renderableTabWidgets!=null){
            for(Widget widget:renderableTabWidgets){
                if (widget instanceof GuiEventListener guiEventListener) {
                    if (guiEventListener.mouseClicked(d, e, i)) {
                        this.setFocused(guiEventListener);
                        if (i == 0) {
                            this.setDragging(true);
                        }

                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(d, e, i);
    }

    @Override
    public boolean changeFocus(boolean bl) {
        GuiEventListener guiEventListener = this.getFocused();
        boolean bl2 = guiEventListener != null;
        if (bl2 && guiEventListener.changeFocus(bl)) {
            return true;
        } else {
            List<? extends GuiEventListener> list = this.getChildrenWithTabRenderables();
            int i = list.indexOf(guiEventListener);
            int j;
            if (bl2 && i >= 0) {
                j = i + (bl ? 1 : 0);
            } else if (bl) {
                j = 0;
            } else {
                j = list.size();
            }

            ListIterator<? extends GuiEventListener> listIterator = list.listIterator(j);
            BooleanSupplier var10000;
            if (bl) {
                Objects.requireNonNull(listIterator);
                var10000 = listIterator::hasNext;
            } else {
                Objects.requireNonNull(listIterator);
                var10000 = listIterator::hasPrevious;
            }

            BooleanSupplier booleanSupplier = var10000;
            Supplier var11;
            if (bl) {
                Objects.requireNonNull(listIterator);
                var11 = listIterator::next;
            } else {
                Objects.requireNonNull(listIterator);
                var11 = listIterator::previous;
            }

            Supplier<? extends GuiEventListener> supplier = var11;

            while(booleanSupplier.getAsBoolean()) {
                GuiEventListener guiEventListener2 = supplier.get();
                if (guiEventListener2.changeFocus(bl)) {
                    this.setFocused(guiEventListener2);
                    return true;
                }
            }

            this.setFocused(null);
            return false;
        }
    }

    @Override
    public void mouseMoved(double d, double e) {
        if(renderableTabWidgets!=null){
            renderableTabWidgets.forEach(widget -> {
                if(widget instanceof GuiEventListener guiEventListener){
                    guiEventListener.mouseMoved(d,e);
                }
            });
        }
    }


    @Override
    public @NotNull Optional<GuiEventListener> getChildAt(double d, double e) {
        Optional<GuiEventListener> toReturn=super.getChildAt(d,e);
        if(toReturn.isEmpty()&&renderableTabWidgets!=null){
            for(Widget widget:renderableTabWidgets){
                if (widget instanceof GuiEventListener guiEventListener) {
                    if (guiEventListener.isMouseOver(d, e)) {
                        return Optional.of(guiEventListener);
                    }
                }
            }
        }
        return toReturn;
    }
    //End

    @Override
    public void render(PoseStack poseStack,int i,int j,float f){
        this.renderBackground(poseStack);
        RenderSystem.setShaderColor(1.0F,1.0F,1.0F,1.0F);
        RenderSystem.setShaderTexture(0,BACKGROUND_SETTING);
        blit(poseStack,0,0,0,0,this.width,this.height,this.width,this.height);
        fill(poseStack,5,60,this.buttonSetting.x+this.buttonSetting.getWidth(),this.height-5,0x8F000000);
        title.renderCentered(poseStack,this.width/2,15);
        if(tab==SettingTabs.USER) {
            if(FrpcManager.getInstance().getCurrentFrpcId().equals("openfrp")&&wrlof!=null) {
                if(wrlof.data!=null) wrlof.read();
                ImageWidget nowavatar=(ImageWidget)tabUser.get(0);
                nowavatar.texture = wrlof.location;
                wrlof = null;
            }
            if(FrpcManager.getInstance().getCurrentFrpcId().equals("sakurafrp")&&wrlsf!=null) {
                if(wrlsf.data!=null) wrlsf.read();
                ImageWidget nowavatar=(ImageWidget)tabUser.get(0);
                nowavatar.texture = wrlsf.location;
                wrlsf = null;
            }
        }

        if(renderableTabWidgets!=null) renderableTabWidgets.forEach(widget -> widget.render(poseStack,i,j,f));
        super.render(poseStack,i,j,f);
    }

    WebTextureResourceLocation wrlof,wrlsf;

    public void onTab() {
        boolean first=lasttab!=tab;
        switch(tab){
            case LOG -> {
                buttonLog.active=false;
                buttonInfo.active=true;
                buttonUser.active=true;
                buttonSetting.active=true;
                if(first) {
                    LogObjectSelectionList selectionList=(LogObjectSelectionList) tabLog.get(0);
                    new Thread(() -> {
                        List<LogObjectSelectionList.Entry> entries=new ArrayList<>();
                        Path logsPath=Path.of(OpenLink.EXECUTABLE_FILE_STORAGE_PATH+"logs"+File.separator);
                        try {
                            Files.walkFileTree(logsPath, new SimpleFileVisitor<>() {
                                @Override
                                public @NotNull FileVisitResult visitFile(Path file, @NotNull BasicFileAttributes attrs) throws IOException {
                                    File logFile = file.toFile();
                                    if (logFile.isFile() && logFile.getName().endsWith(".log")) {
                                        FileInputStream fis = new FileInputStream(logFile);
                                        String logContent = new String(fis.readAllBytes(), StandardCharsets.UTF_8);
                                        String[] lines = logContent.split("\n");
                                        entries.add(selectionList.ofEntry(logFile.getPath(), lines[0], lines[1], lines[2], lines[3], lines[4]));
                                    }
                                    return FileVisitResult.CONTINUE;
                                }
                            });
                        } catch (IOException ignored) {
                        }
                        entries.sort((o1, o2) -> {
                            if(o2.date.compareTo(o1.date)==0)
                                return o2.startTime.compareTo(o1.startTime);
                            return o2.date.compareTo(o1.date);
                        });
                        selectionList.replaceEntriesByList(entries);
                    },"Log read thread").start();

                }
                renderableTabWidgets=tabLog;
            }
            case SETTING -> {
                buttonLog.active=true;
                buttonInfo.active=true;
                buttonUser.active=true;
                buttonSetting.active=false;

                renderableTabWidgets=tabSetting;
            }
            case USER -> {
                buttonLog.active=true;
                buttonInfo.active=true;
                buttonUser.active=false;
                buttonSetting.active=true;
                if(!FrpcManager.getInstance().getCurrentFrpcInstance().isLoggedIn()){
                    renderableTabWidgets=tabLogin_User;
                    return;
                }
                if(first && FrpcManager.getInstance().getCurrentFrpcId().equals("openfrp")) {
                    ImageWidget nowavatar=(ImageWidget)tabUser.get(0);
                    ComponentWidget nowuser=(ComponentWidget)tabUser.get(1);
                    ComponentWidget nowid=(ComponentWidget)tabUser.get(2);
                    ComponentWidget nowemail=(ComponentWidget)tabUser.get(3);
                    ComponentWidget nowgroup=(ComponentWidget)tabUser.get(4);
                    ComponentWidget nowproxy=(ComponentWidget)tabUser.get(5);
                    LineChartWidget nowtraffic=(LineChartWidget)tabUser.get(6);
                    nowuser.component=Utils.translatableText("text.openlink.loading");
                    nowid.component=Utils.emptyText();
                    nowemail.component=Utils.emptyText();
                    nowgroup.component=Utils.emptyText();
                    nowproxy.component=Utils.emptyText();
                    tabUser.set(1,nowuser);
                    new Thread(() -> {
                        try {
                            userInfo = OpenFrpFrpcImpl.getUserInfo();
                            if(userInfo==null||!userInfo.flag){
                                OpenFrpFrpcImpl.Authorization=null;
                                OpenFrpFrpcImpl.writeSession();
                                throw new Exception("[OpenLink] Session expired!");
                            }
                        } catch (Exception e) {
                            OpenLink.LOGGER.error("", e);
                            renderableTabWidgets=tabLogin_User;
                            return;
                        }
                        MessageDigest messageDigest=null;
                        try {
                            messageDigest=MessageDigest.getInstance("SHA-256");
                        } catch (NoSuchAlgorithmException ignored) {}
                        StringBuilder sha256=new StringBuilder();
                        for (byte b:messageDigest.digest(userInfo.data.email.toLowerCase().getBytes(StandardCharsets.UTF_8)))
                            sha256.append(String.format("%02x",b));
                        wrlof = new WebTextureResourceLocation(Uris.weavatarUri.toString()+ sha256+".png?s=400", nowavatar.texture);
                        wrlof.load();
                        nowuser.component= Utils.literalText(userInfo.data.username);
                        nowid.component= Utils.literalText("#"+userInfo.data.id);
                        nowid.x=10+nowuser.font.width(nowuser.component)+1;
                        nowemail.component= Utils.literalText((SettingScreen.sensitiveInfoHiding?"§k":"")+userInfo.data.email);
                        nowgroup.component= Utils.literalText(userInfo.data.friendlyGroup);
                        nowproxy.component= Utils.translatableText("text.openlink.proxycount",userInfo.data.used,userInfo.data.proxies);
                        List<Pair<String,Long>> dataPoints=readTraffic();
                        dataPoints.add(new Pair<>(Utils.translatableText("text.openlink.now").getString(),userInfo.data.traffic));
                        nowtraffic.dataPoints=dataPoints;
                        tabUser.set(0,nowavatar);
                        tabUser.set(1,nowuser);
                        tabUser.set(2,nowid);
                        tabUser.set(3,nowemail);
                        tabUser.set(4,nowgroup);
                        tabUser.set(5,nowproxy);
                    }, "Request thread").start();
                } else if(first && FrpcManager.getInstance().getCurrentFrpcId().equals("sakurafrp")) {
                    ImageWidget nowavatar=(ImageWidget)tabUser.get(0);
                    ComponentWidget nowuser=(ComponentWidget)tabUser.get(1);
                    ComponentWidget nowid=(ComponentWidget)tabUser.get(2);
                    ComponentWidget nowemail=(ComponentWidget)tabUser.get(3);
                    ComponentWidget nowgroup=(ComponentWidget)tabUser.get(4);
                    ComponentWidget nowproxy=(ComponentWidget)tabUser.get(5);
                    LineChartWidget nowtraffic=(LineChartWidget)tabUser.get(6);
                    nowuser.component=Utils.translatableText("text.openlink.loading");
                    nowid.component=Utils.emptyText();
                    nowemail.component=Utils.emptyText();
                    nowgroup.component=Utils.emptyText();
                    nowproxy.component=Utils.emptyText();
                    tabUser.set(1,nowuser);
                    new Thread(() -> {
                        Gson gson = new Gson();
                        try {
                            userInfoSakura = SakuraFrpFrpcImpl.getUserInfo();
                            if(userInfoSakura==null||SakuraFrpFrpcImpl.isBadResponse(userInfoSakura)){
                                SakuraFrpFrpcImpl.token=null;
                                SakuraFrpFrpcImpl.writeSession();
                                throw new Exception("[OpenLink] Session expired!");
                            }
                            userProxySakura = gson.fromJson(Request.GET(Uris.sakuraFrpAPIUri+"tunnels", SakuraFrpFrpcImpl.getTokenHeader()).getFirst(), new TypeToken<JsonUserProxySakura>(){}.getType());
                            if(JsonUserProxySakura.isBadResponse(userProxySakura)) {
                                throw new Exception("[OpenLink] Cannot get the user tunnel list!");
                            }
                        } catch (Exception e) {
                            OpenLink.LOGGER.error("", e);
                            renderableTabWidgets=tabLogin_User;
                            return;
                        }
                        wrlsf = new WebTextureResourceLocation(userInfoSakura.avatar+"?s=400", nowavatar.texture);
                        wrlsf.load();
                        nowuser.component= Utils.literalText(userInfoSakura.name);
                        nowid.component= Utils.literalText("#"+userInfoSakura.id);
                        nowid.x=10+nowuser.font.width(nowuser.component)+1;
                        nowemail.component= Utils.literalText(userInfoSakura.speed);
                        nowgroup.component= Utils.literalText(userInfoSakura.group.name);
                        nowproxy.component= Utils.translatableText("text.openlink.proxycount",userProxySakura.size(),userInfoSakura.tunnels);
                        List<Pair<String,Long>> dataPoints=readTrafficSakura();
                        dataPoints.add(new Pair<>(Utils.translatableText("text.openlink.now").getString(),Long.valueOf((long)(SakuraFrpFrpcImpl.getUserInfo().traffic.get(1)/1048576F))));
                        nowtraffic.dataPoints=dataPoints;
                        tabUser.set(0,nowavatar);
                        tabUser.set(1,nowuser);
                        tabUser.set(2,nowid);
                        tabUser.set(3,nowemail);
                        tabUser.set(4,nowgroup);
                        tabUser.set(5,nowproxy);
                    }, "Request thread").start();
                }
                renderableTabWidgets=tabUser;
            }
            case INFO -> {
                buttonLog.active=true;
                buttonInfo.active=false;
                buttonUser.active=true;
                buttonSetting.active=true;

                renderableTabWidgets=tabInfo;
            }
        }
    }

    @Override
    public void tick(){
        try {
            onTab();
        } catch (Exception e) {
            OpenLink.LOGGER.error("", e);
            this.onClose();
        }
        lasttab=tab;
    }

    public List<Pair<String,Long>> readTraffic(){
        String origin=OpenLink.PREFERENCES.get("traffic_storage","");
        String[] spilt=origin.split(";");
        List<Pair<String,Long>> res=new ArrayList<>();
        for(String s:spilt) {
            if(!s.isEmpty()) {
                String[] split = s.split(",");
                res.add(new Pair<>(split[0], Long.parseLong(split[1])));
            }
        }
        return res;
    }

    public List<Pair<String,Long>> readTrafficSakura(){
        String origin=OpenLink.PREFERENCES.get("traffic_storage_sakura","");
        String[] spilt=origin.split(";");
        List<Pair<String,Long>> res=new ArrayList<>();
        for(String s:spilt) {
            if(!s.isEmpty()) {
                String[] split = s.split(",");
                res.add(new Pair<>(split[0], Long.parseLong(split[1])));
            }
        }
        return res;
    }

    public class LogObjectSelectionList extends ObjectSelectionList<LogObjectSelectionList.Entry>{
        public LogObjectSelectionList(Minecraft minecraft, int width, int height, int x0, int y0, int x1, int y1, int itemHeight) {
            super(minecraft, width, height, y0, y1, itemHeight);
            this.setRenderBackground(false);
            this.setRenderHeader(false,0);
            this.setRenderTopAndBottom(false);
            this.y0 = y0;
            this.y1 = y1;
            this.x0 = x0;
            this.x1 = x1;
            if (this.getSelected() != null) {
                this.centerScrollOn(this.getSelected());
            }
        }

        @Override
        public void render(PoseStack poseStack, int i, int j, float f) {
            enableScissor();
            super.render(poseStack, i, j, f);
            RenderSystem.disableScissor();
        }

        private void enableScissor() {
            Window window = this.minecraft.getWindow();
            int screenHeight = window.getScreenHeight();
            int scissorX = (int) (x0 * window.getGuiScale());
            int scissorY = (int) (screenHeight - (y1 * window.getGuiScale()));
            int scissorWidth = (int) ((x1 - x0) * window.getGuiScale());
            int scissorHeight = (int) ((y1 - y0) * window.getGuiScale());
            RenderSystem.enableScissor(scissorX, scissorY, scissorWidth, scissorHeight);
        }

        public void changePos(int width, int height, int x0, int y0, int x1, int y1){
            this.y0 = y0;
            this.y1 = y1;
            this.x0 = x0;
            this.x1 = x1;
            this.width=width;
            this.height=height;
        }

        @Override
        public int getRowWidth() {
            return this.width-20;
        }

        @Override
        public int getScrollbarPosition() {
            return this.x0+this.width-7;
        }

        public Entry ofEntry(String filePath, String levelName, String date, String startTime, String proxyid, String provider) {
            return new Entry(filePath,levelName,date,startTime,proxyid,provider);
        }

        public void replaceEntriesByList(List<Entry> entries) {
            this.clearEntries();
            entries.forEach(this::addEntry);
        }

        @Override
        protected boolean isFocused() {
            return SettingScreen.this.getFocused() == this;
        }

        public class Entry extends ObjectSelectionList.Entry<Entry> {
            public final String filePath;
            // 世界名称
            public final String levelName;
            // 日期
            public final String date;
            // 启动时间
            public final String startTime;
            // 隧道ID
            public final String port;
            // Frp服务提供商名称
            public final String provider;

            public Entry(String filePath, String levelName, String date, String startTime, String port, String provider) {
                this.filePath=filePath;
                this.levelName=levelName;
                this.date=date;
                this.startTime=startTime;
                this.port = port;
                this.provider=provider;
            }

            @Override
            public boolean mouseClicked(double d, double e, int i) {
                if (i==0) {
                    if(SettingScreen.LogObjectSelectionList.this.getSelected()==this){
                        Util.getPlatform().openFile(new File(this.filePath));
                        return true;
                    }
                    this.select();
                    return true;
                }
                return false;
            }


            @Override
            public @NotNull Component getNarration() {
                return Utils.translatableText("narrator.select", this.provider+" "+this.startTime+" "+this.levelName);
            }

            private void select() {
                SettingScreen.LogObjectSelectionList.this.setSelected(this);
            }

            @Override
            public void render(PoseStack poseStack, int i, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isHovered, float f) {
                fill(poseStack, x, y, x + entryWidth, y + entryHeight, 0x8f2b2b2b);
                drawString(poseStack, SettingScreen.LogObjectSelectionList.this.minecraft.font, this.date+" "+this.startTime, x + 4, y + 4, 0x8fffffff);
                drawString(poseStack, SettingScreen.LogObjectSelectionList.this.minecraft.font, this.levelName, x + 4, y + 4 + (entryHeight-4) / 2, 0x8fffffff);
                drawString(poseStack, SettingScreen.LogObjectSelectionList.this.minecraft.font, this.port, x + entryWidth - 4 - LogObjectSelectionList.this.minecraft.font.width(this.port), y + 4, 0x8fffffff);
                drawString(poseStack, SettingScreen.LogObjectSelectionList.this.minecraft.font, this.provider, x + entryWidth - 4 - LogObjectSelectionList.this.minecraft.font.width(this.provider), y + 4 + (entryHeight-4) / 2, 0x8fffffff);
                if(isHovered){
                    renderTooltip(poseStack, Utils.translatableText("text.openlink.doubleclick",new File(filePath).getName()), mouseX, mouseY);
                }
            }
        }
    }

    public static class InfoObjectSelectionList extends ObjectSelectionList<InfoObjectSelectionList.Entry>{
        public InfoObjectSelectionList(Minecraft minecraft, int width, int height, int x0, int y0, int x1, int y1, int itemHeight) {
            super(minecraft, width, height, y0, y1, itemHeight);
            this.setRenderBackground(false);
            this.setRenderHeader(false,0);
            this.setRenderTopAndBottom(false);
            this.addEntry(new Entry(informationList));
            this.y0 = y0;
            this.y1 = y1;
            this.x0 = x0;
            this.x1 = x1;
        }

        @Override
        public void render(PoseStack poseStack, int i, int j, float f) {
            enableScissor();
            super.render(poseStack, i, j, f);
            RenderSystem.disableScissor();
        }

        private void enableScissor() {
            Window window = Minecraft.getInstance().getWindow();
            int screenHeight = window.getScreenHeight();
            int scissorX = (int) (x0 * window.getGuiScale());
            int scissorY = (int) (screenHeight - (y1 * window.getGuiScale()));
            int scissorWidth = (int) ((x1 - x0) * window.getGuiScale());
            int scissorHeight = (int) ((y1 - y0) * window.getGuiScale());
            RenderSystem.enableScissor(scissorX, scissorY, scissorWidth, scissorHeight);
        }

        public void changePos(int width, int height, int x0, int y0, int x1, int y1){
            this.y0 = y0;
            this.y1 = y1;
            this.x0 = x0;
            this.x1 = x1;
            this.width=width;
            this.height=height;
        }

        @Override
        public int getRowWidth() {
            return this.width-20;
        }

        @Override
        public int getScrollbarPosition() {
            return this.x0+this.width-7;
        }

        public static class Information implements GuiEventListener {
            public boolean inChart;
            public Component component;
            public Information(Component component,boolean inChart){
                this.inChart=inChart;
                this.component=component;
            }
            public void render(PoseStack poseStack, int x, int y, int width){
                if(inChart){
                    GuiComponent.fill(poseStack, x, y, x + width, y + Minecraft.getInstance().font.lineHeight+5, 0x8f2b2b2b);
                }
                GuiComponent.drawString(poseStack, Minecraft.getInstance().font, this.component, x+(inChart?4:0), y+2, 0xffffffff);
            }
            @Override
            public boolean mouseClicked(double d, double e, int i) {
                if(this.component.getString().contains("§n")){
                    new WebBrowser(Uris.advertiseUri.toString()).openBrowser();
                    return true;
                }
                return false;
            }
        }

        public static class Entry extends ObjectSelectionList.Entry<Entry> {
            public List<Information> informations;

            public Entry(List<Information> informations) {
                this.informations=informations;
            }

            @Override
            public @NotNull Component getNarration() {
                MutableComponent res=(MutableComponent) Utils.emptyText();
                this.informations.forEach((info -> res.append(info.component)));
                return res;
            }

            @Override
            public void render(PoseStack poseStack, int i, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isHovered, float f) {
                for(int i1=0;i1<this.informations.size();i1++){
                    this.informations.get(i1).render(poseStack,x,y+i1*(Minecraft.getInstance().font.lineHeight+5),entryWidth);
                }
            }

            @Override
            public boolean mouseClicked(double d, double e, int i) {
                for (Information information:informations){
                    if(information.mouseClicked(d,e,i)){
                        return true;
                    }
                }
                return super.mouseClicked(d, e, i);
            }
        }
    }

}
