package fun.moystudio.openlink;

import fun.moystudio.openlink.frpc.Frpc;
import fun.moystudio.openlink.network.Request;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class OpenLink {
    public static final String MOD_ID = "openlink";
    public static final Logger LOGGER = LogManager.getLogger("OpenLink");

    public static void init() throws Exception {

        LOGGER.info("Initializing OpenLink!");
        Frpc.init();//安装/检查更新frpc版本
        Request.readSession();//读取以前的SessionID
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
}
