package fun.moystudio.openlink.mixin;

import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(Screen.class)
public interface IScreenAccessor {
    @Accessor("children")
    List<GuiEventListener> getChildren();
    @Invoker("addRenderableWidget")
    <T extends GuiEventListener & Widget & NarratableEntry> T invokeAddRenderableWidget(T guiEventListener);
}