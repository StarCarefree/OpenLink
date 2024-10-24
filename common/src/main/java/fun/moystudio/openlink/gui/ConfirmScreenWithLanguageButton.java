package fun.moystudio.openlink.gui;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.LanguageSelectScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

public class ConfirmScreenWithLanguageButton extends ConfirmScreen {
    public ConfirmScreenWithLanguageButton(BooleanConsumer booleanConsumer, Component component, Component component2) {
        super(booleanConsumer, component, component2);
    }

    public ConfirmScreenWithLanguageButton(BooleanConsumer booleanConsumer, Component component, Component component2, Component component3, Component component4) {
        super(booleanConsumer, component, component2, component3, component4);
    }

    @Override
    protected void addButtons(int i) {
        this.addExitButton(new Button(this.width / 2 - 155, i, 150, 20, this.yesButton, (button) -> {
            this.callback.accept(true);
        }));
        this.addExitButton(new Button(this.width / 2 - 155 + 160, i, 150, 20, this.noButton, (button) -> {
            this.callback.accept(false);
        }));
        this.addRenderableWidget(new ImageButton(this.width / 2 - 185, i, 20, 20, 0, 106, 20, Button.WIDGETS_LOCATION, 256, 256, (button) -> {
            this.minecraft.setScreen(new LanguageSelectScreen(this, this.minecraft.options, this.minecraft.getLanguageManager()));
        }, new TranslatableComponent("narrator.button.language")));
    }
}