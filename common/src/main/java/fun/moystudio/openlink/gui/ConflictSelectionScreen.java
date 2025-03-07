package fun.moystudio.openlink.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import fun.moystudio.openlink.OpenLink;
import fun.moystudio.openlink.logic.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class ConflictSelectionScreen extends Screen {
    public ConflictSelectionScreen(String conflictModId) {
        super(Utils.translatableText("text.openlink.conflict", conflictModId));
    }
    public MultiLineLabel text;
    ConflictSelectionList conflictSelectionList;
    public static Pair<String,Class<?>> canOpen;
    @Override
    protected void init() {
        canOpen=null;
        text=MultiLineLabel.create(this.font, this.title, this.width-50);
        conflictSelectionList=new ConflictSelectionList(this.minecraft);
        this.addWidget(conflictSelectionList);
        this.addRenderableWidget(new Button(this.width / 2 - 100, this.height - 38, 200, 20, CommonComponents.GUI_DONE, (button) -> {
            ConflictSelectionList.Entry entry = this.conflictSelectionList.getSelected();
            if (entry != null) {
                try {
                    canOpen=Pair.of(entry.modid,entry.clazz);
                    this.minecraft.setScreen((Screen) entry.clazz.getDeclaredConstructor(Screen.class).newInstance((Screen) null));
                } catch (Exception e) {
                    canOpen=null;
                    throw new RuntimeException(e);
                }
            }
        }));
    }
    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        conflictSelectionList.render(poseStack,i,j,f);
        text.renderCentered(poseStack,this.width/2,16,16,0xffffff);
        drawCenteredString(poseStack,this.font,Utils.translatableText("text.openlink.conflict_tip"),this.width/2,this.height-58,0xffffff);
        super.render(poseStack,i,j,f);
    }

    class ConflictSelectionList extends ObjectSelectionList<ConflictSelectionList.Entry>{
        public ConflictSelectionList(Minecraft minecraft) {
            super(minecraft, ConflictSelectionScreen.this.width, ConflictSelectionScreen.this.height, 32, ConflictSelectionScreen.this.height - 65 + 4, 18);

            for (Pair<String,Class<?>> classPair : OpenLink.CONFLICT_CLASS){
                this.addEntry(new Entry(classPair.getFirst(),classPair.getSecond()));
            }

            this.setSelected(this.children().get(this.addEntry(new Entry("openlink",NewShareToLanScreen.class))));

            if (this.getSelected() != null) {
                this.centerScrollOn(this.getSelected());
            }
        }

        @Override
        protected int getScrollbarPosition() {
            return super.getScrollbarPosition() + 20;
        }

        @Override
        public int getRowWidth() {
            return super.getRowWidth() + 50;
        }

        protected void renderBackground(PoseStack poseStack) {
            ConflictSelectionScreen.this.renderBackground(poseStack);
        }

        protected boolean isFocused() {
            return ConflictSelectionScreen.this.getFocused() == this;
        }

        public class Entry extends ObjectSelectionList.Entry<Entry>{

            public Entry(String modid,Class<?> clazz){
                this.modid=modid;
                this.clazz=clazz;
            }

            public String modid;
            public Class<?> clazz;

            @Override
            public @NotNull Component getNarration() {
                return Utils.translatableText("narrator.select",this.modid);
            }

            public boolean mouseClicked(double d, double e, int i) {
                if (i == 0) {
                    this.select();
                    return true;
                } else {
                    return false;
                }
            }


            private void select() {
                ConflictSelectionList.this.setSelected(this);
            }

            @Override
            public void render(PoseStack poseStack, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
                GuiComponent.drawCenteredString(poseStack,ConflictSelectionScreen.this.font, modid+": "+clazz.getSimpleName(),ConflictSelectionList.this.width/2,j+1, 0xffffff);
            }
        }
    }
}
