package fun.moystudio.openlink.gui;

import fun.moystudio.openlink.logic.Utils;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CommonButtons;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.options.LanguageSelectScreen;
import net.minecraft.network.chat.Component;

public class ConfirmScreenWithLanguageButton extends ConfirmScreen {
    public ConfirmScreenWithLanguageButton(BooleanConsumer booleanConsumer, Component component, Component component2) {
        super(booleanConsumer, component, component2);
    }

    @Override
    protected void addButtons(int i) {
        this.addExitButton(Button.builder(this.yesButton, (button) -> this.callback.accept(true)).bounds(this.width / 2 - 155, i, 150, 20).build());
        this.addExitButton(Button.builder(this.noButton, (button) -> this.callback.accept(false)).bounds(this.width / 2 - 155 + 160, i, 150, 20).build());
        SpriteIconButton spriteIconButton = this.addRenderableWidget(CommonButtons.language(20, (button) -> this.minecraft.setScreen(new net.minecraft.client.gui.screens.options.LanguageSelectScreen(this, this.minecraft.options, this.minecraft.getLanguageManager())), true));
        spriteIconButton.setPosition(this.width / 2 - 185, i);
    }
}
