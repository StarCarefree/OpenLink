package fun.moystudio.openlink.gui;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CommonButtons;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.options.LanguageSelectScreen;
import net.minecraft.network.chat.Component;

public class ConfirmScreenWithLanguageButton extends ConfirmScreen {
    public ConfirmScreenWithLanguageButton(BooleanConsumer booleanConsumer, Component component, Component component2) {
        super(booleanConsumer, component, component2);
    }

    @Override
    protected void addButtons(LinearLayout linearLayout) {
        super.addButtons(linearLayout);
        linearLayout.addChild(CommonButtons.language(20, (arg) -> this.minecraft.setScreen(new LanguageSelectScreen(this, this.minecraft.options, this.minecraft.getLanguageManager())), true));
    }
}
