package fun.moystudio.openlink;

import com.mojang.datafixers.util.Pair;
import fun.moystudio.openlink.frpc.Frpc;
import fun.moystudio.openlink.gui.SettingScreen;
import fun.moystudio.openlink.logic.LanConfig;
import fun.moystudio.openlink.network.Request;
import fun.moystudio.openlink.network.SSLUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.SSLHandshakeException;
import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.prefs.Preferences;
import java.util.stream.Stream;

public final class OpenLink {
    public static final String MOD_ID = "openlink";
    public static final Logger LOGGER = LogManager.getLogger("OpenLink");
    public static final String CONFIG_DIR = "config" + File.separator + MOD_ID + File.separator;
    public static final Preferences PREFERENCES = Preferences.userNodeForPackage(OpenLink.class);
    public static final String EXECUTABLE_FILE_STORAGE_PATH = Path.of(getLocalStoragePos()).resolve(".openlink").toString()+File.separator;
    public static boolean disabled=false;
    public static String VERSION,LOADER, LOADER_VERSION;
    public static List<Pair<String,Class<?>>> CONFLICT_CLASS=new ArrayList<>();
    private static final List<Pair<String,String>> CONFLICT_CLASS_NAME=Arrays.asList(//Do NOT use Class object here!!!!!!!(By Terry_MC)
            Pair.of("mcwifipnp","io.github.satxm.mcwifipnp.ShareToLanScreenNew"),
            Pair.of("lanserverproperties","rikka.lanserverproperties.ModifyLanScreen"),
            Pair.of("easylan","org.xiaoxian.gui.GuiShareToLanEdit.GuiShareToLanModified")
    );


    public static void init(String version,String loader,String loader_version) throws Exception {
        VERSION=version;
        LOADER=loader;
        LOADER_VERSION=loader_version;
        LOGGER.info("Initializing OpenLink on "+loader+" "+loader_version);
        File configdir=new File(CONFIG_DIR);
        File exedir=new File(EXECUTABLE_FILE_STORAGE_PATH);
        File logdir=new File(EXECUTABLE_FILE_STORAGE_PATH+File.separator+"logs"+File.separator);
        configdir.mkdirs();
        exedir.mkdirs();
        logdir.mkdirs();
        //跳过ssl功能
        try{
            Frpc.init();//安装/检查更新frpc版本
            Request.readSession();//读取以前的SessionID
        } catch (SSLHandshakeException e) {
            e.printStackTrace();
            LOGGER.error("SSL Handshake Error! Ignoring SSL(Not Secure)");
            SSLUtils.ignoreSsl();
        } catch (SocketException e){
            e.printStackTrace();
            disabled=true;
            LOGGER.error("Socket Error! Are you still connecting to the network? All the features will be disabled!");
            return;
        } catch (IOException e) {
            e.printStackTrace();
            disabled = true;
            LOGGER.error("IO Error! Are you still connecting to the network? All the features will be disabled!");
            return;
        } catch (Exception e){
            throw new RuntimeException(e);
        }
        if(SSLUtils.sslIgnored){
            LOGGER.warn("SSL is ignored. The confirm screen will show after the main game screen loaded.");
        }

        //LanConfigs Reading
        LanConfig.readConfig();
        LanConfig.writeConfig();

        //Settings Reading
        SettingScreen.sensitiveInfoHiding=PREFERENCES.getBoolean("setting_sensitive_info_hiding", false);
        PREFERENCES.putBoolean("setting_sensitive_info_hiding", SettingScreen.sensitiveInfoHiding);

        //Conflict Class Name Detecting
        CONFLICT_CLASS_NAME.forEach(className->{
            try {
                Class<?> clazz = Class.forName(className.getSecond());
                CONFLICT_CLASS.add(Pair.of(className.getFirst(),clazz));
            } catch (Exception ignored) {
            }
        });

        //直接用mixin打开更新屏幕就行
        LOGGER.info("\n   ____                       _       _         _    \n" +
                "  / __ \\                     | |     (_)       | |   \n" +
                " | |  | | _ __    ___  _ __  | |      _  _ __  | | __\n" +
                " | |  | || '_ \\  / _ \\| '_ \\ | |     | || '_ \\ | |/ /\n" +
                " | |__| || |_) ||  __/| | | || |____ | || | | ||   < \n" +
                "  \\____/ | .__/  \\___||_| |_||______||_||_| |_||_|\\_\\\n" +
                "         | |                                         \n" +
                "         |_|                                         ");
    }

    private static String getLocalStoragePos() {
        Path userHome = Paths.get(System.getProperty("user.home"));
        Path oldPath = userHome.resolve(".openlink");
        if (Files.exists(oldPath)) {
            return userHome.toString();
        }

        String macAppSupport = System.getProperty("os.name").contains("OS X") ? userHome.resolve("Library/Application Support").toString() : null;
        String localAppData = System.getenv("LocalAppData");

        String xdgDataHome = System.getenv("XDG_DATA_HOME");
        if (xdgDataHome == null) {
            xdgDataHome = userHome.resolve(".local/share").toString();
        }

        return Stream.of(localAppData, macAppSupport).filter(Objects::nonNull).findFirst().orElse(xdgDataHome);
    }
}
