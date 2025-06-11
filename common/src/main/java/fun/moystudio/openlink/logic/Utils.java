package fun.moystudio.openlink.logic;

import fun.moystudio.openlink.gui.SettingScreen;
import fun.moystudio.openlink.network.Uris;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;

public class Utils {
    public static MutableComponent emptyText() {
        return Component.empty().copy();
    }
    public static MutableComponent translatableText(String key, Object... objects) {
        return Component.translatable(key, objects);
    }
    public static MutableComponent literalText(String string) {
        return Component.literal(string);
    }
    public static Component proxyRestartText() {
        MutableComponent component = ComponentUtils.wrapInSquareBrackets(translatableText("text.openlink.clicktorestart"))
                .withStyle((style -> style.withClickEvent(new ClickEvent.RunCommand("/proxyrestart"))));
        component.append("\n").append(ComponentUtils.wrapInSquareBrackets(translatableText("text.openlink.wiki")).withStyle(style -> style.withUnderlined(true).withClickEvent(new ClickEvent.OpenUrl(Uris.wikiUri))));
        return component;
    }
    public static Component proxyStartText(String connectAddress){
        return translatableText("text.openlink.frpcstartsuccessfully","§n"+(SettingScreen.sensitiveInfoHiding?"§k":"")+connectAddress).withStyle((style -> style.withClickEvent(new ClickEvent.CopyToClipboard(connectAddress))
                        .withHoverEvent(new HoverEvent.ShowText(literalText((SettingScreen.sensitiveInfoHiding?"§k":"")+connectAddress)))));
    }
    public static ResourceLocation createResourceLocation(String namespace, String path) {
        return ResourceLocation.fromNamespaceAndPath(namespace,path);
    }

}
