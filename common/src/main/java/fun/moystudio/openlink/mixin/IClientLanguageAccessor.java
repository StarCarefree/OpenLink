package fun.moystudio.openlink.mixin;

import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.server.packs.resources.Resource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;
import java.util.Map;

@Mixin(ClientLanguage.class)
public interface IClientLanguageAccessor {
    @Invoker("appendFrom")
    public static void invokeAppendFrom(List<Resource> list, Map<String, String> map){
        throw new AssertionError();
    }
}
