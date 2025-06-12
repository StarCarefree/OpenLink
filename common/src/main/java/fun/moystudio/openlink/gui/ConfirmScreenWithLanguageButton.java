package fun.moystudio.openlink.gui;

import fun.moystudio.openlink.logic.Utils;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.LanguageSelectScreen;
import net.minecraft.network.chat.Component;

public class ConfirmScreenWithLanguageButton extends ConfirmScreen {
    public ConfirmScreenWithLanguageButton(BooleanConsumer booleanConsumer, Component component, Component component2) {
        super(booleanConsumer, component, component2);
    }

    @Override
    protected void init() {
        super.init();
        this.addButton(new ImageButton(this.width / 2 - 185, this.height / 6 + 96, 20, 20, 0, 106, 20, Button.WIDGETS_LOCATION, 256, 256, (button) -> this.minecraft.setScreen(new LanguageSelectScreen(this, this.minecraft.options, this.minecraft.getLanguageManager())), Utils.translatableText("narrator.button.language")));
    }

}
