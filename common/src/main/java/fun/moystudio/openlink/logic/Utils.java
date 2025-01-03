package fun.moystudio.openlink.logic;

import fun.moystudio.openlink.gui.SettingScreen;
import net.minecraft.network.chat.*;

public class Utils {
    public static Component EMPTY=TextComponent.EMPTY;
    public static MutableComponent translatableText(String key, Object... objects) {
        return new TranslatableComponent(key,objects);
    }
    public static MutableComponent literalText(String string) {
        return new TextComponent(string);
    }
    public static Component proxyRestartText() {
        return ComponentUtils.wrapInSquareBrackets(translatableText("text.openlink.clicktorestart"))
                .withStyle((style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/proxyrestart"))));
    }
    public static Component proxyStartText(String connectAddress){
        return translatableText("text.openlink.frpcstartsucessfully","§n"+(SettingScreen.sensitiveInfoHiding?"§k":"")+connectAddress).withStyle((style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, connectAddress))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, literalText((SettingScreen.sensitiveInfoHiding?"§k":"")+connectAddress)))));
    }

}
