package fun.moystudio.openlink.logic;

import fun.moystudio.openlink.gui.SettingScreen;
import fun.moystudio.openlink.network.Uris;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;

public class Utils {
    public static MutableComponent emptyText() {
        return TextComponent.EMPTY.copy();
    }
    public static MutableComponent translatableText(String key, Object... objects) {
        return new TranslatableComponent(key,objects);
    }
    public static MutableComponent literalText(String string) {
        return new TextComponent(string);
    }
    public static Component proxyRestartText() {
        MutableComponent component = ComponentUtils.wrapInSquareBrackets(translatableText("text.openlink.clicktorestart"))
                .withStyle((style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/proxyrestart"))));
        component.append("\n").append(ComponentUtils.wrapInSquareBrackets(translatableText("text.openlink.wiki")).withStyle(style -> style.withUnderlined(true).withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, Uris.wikiUri.toString()))));
        return component;
    }
    public static Component proxyStartText(String connectAddress){
        return translatableText("text.openlink.frpcstartsuccessfully","§n"+(SettingScreen.sensitiveInfoHiding?"§k":"")+connectAddress).withStyle((style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, connectAddress))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, literalText((SettingScreen.sensitiveInfoHiding?"§k":"")+connectAddress)))));
    }
    public static ResourceLocation createResourceLocation(String namespace, String path) {
        return new ResourceLocation(namespace,path);
    }

}
