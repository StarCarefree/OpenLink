package fun.moystudio.openlink.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import fun.moystudio.openlink.frpcimpl.FrpcManager;
import fun.moystudio.openlink.logic.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class FrpcImplSelectionScreen extends Screen {
    Screen lastscreen;
    Button done, update;
    FrpcImplSelectionList selectionList;

    protected FrpcImplSelectionScreen(Screen last) {
        super(Utils.translatableText("gui.openlink.frpcselectionscreentitle"));
        lastscreen = last;
    }

    @Override
    protected void init() {
        if(selectionList==null){
            selectionList=new FrpcImplSelectionList(this.minecraft);
        }
        selectionList.changePos(this.width, this.height, 32, this.height-65+4);
        this.addWidget(selectionList);
        this.addWidget(done = new Button(this.width / 2 + 5, this.height - 38, 150, 20, CommonComponents.GUI_DONE, (button) -> {
            if (selectionList.getSelected() != null) {
                FrpcManager.getInstance().setCurrentFrpcId(selectionList.getSelected().id);
            }
            this.onClose();
        }));
        this.addWidget(update = new Button(this.width / 2 - 150 - 5, this.height - 38, 150, 20, Utils.translatableText("text.openlink.updatebutton"), button -> {
            if (selectionList.getSelected() != null) {
                FrpcManager.getInstance().updateFrpcByIds(selectionList.getSelected().id);
                this.minecraft.setScreen(new FrpcImplSelectionScreen(lastscreen));
            }
        }));
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        super.render(poseStack,i,j,f);
        this.renderBackground(poseStack);
        if(selectionList!=null){
            selectionList.render(poseStack,i,j,f);
        }
        drawCenteredString(poseStack,this.font,this.title,this.width/2,16,0xffffff);
        done.render(poseStack,i,j,f);
        update.render(poseStack,i,j,f);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(lastscreen);
    }

    @Override
    public void tick() {
        if(this.update!=null && selectionList.getSelected() != null) {
            this.update.active = selectionList.getSelected().isOutdated;
        }
    }

    class FrpcImplSelectionList extends ObjectSelectionList<FrpcImplSelectionScreen.FrpcImplSelectionList.Entry> {
        public FrpcImplSelectionList(Minecraft minecraft) {
            super(minecraft, FrpcImplSelectionScreen.this.width, FrpcImplSelectionScreen.this.height, 32, FrpcImplSelectionScreen.this.height-65+4, 40);
            List<Pair<Pair<String, String>, Pair<String,Boolean>>> list = FrpcManager.getInstance().getFrpcImplDetailList();
            AtomicReference<Entry> now = new AtomicReference<>();
            list.forEach(detail -> {
                Entry entry = new Entry(detail.getFirst().getFirst(), detail.getFirst().getSecond(), detail.getSecond().getFirst(), detail.getSecond().getSecond());
                if(detail.getFirst().getFirst().equals(FrpcManager.getInstance().getCurrentFrpcId())) {
                    now.set(entry);
                }
                this.addEntry(entry);
            });
            if(now.get()!=null) {
                this.setSelected(now.get());
                this.centerScrollOn(now.get());
            }
        }

        public void changePos(int width, int height, int y0, int y1){
            this.width=width;
            this.height=height;
            this.y0=y0;
            this.y1=y1;
        }

        @Override
        protected boolean isFocused(){
            return FrpcImplSelectionScreen.this.getFocused() == this;
        }

        @Override
        protected int getScrollbarPosition() {
            return super.getScrollbarPosition() + 20;
        }

        @Override
        public int getRowWidth() {
            return super.getRowWidth() + 50;
        }

        @Override
        public void render(PoseStack poseStack, int i, int j, float f) {
            super.render(poseStack, i, j, f);
        }

        public class Entry extends ObjectSelectionList.Entry<FrpcImplSelectionScreen.FrpcImplSelectionList.Entry> {

            String id,name,version;
            boolean isOutdated;

            public Entry(String id, String name, String version, boolean isOutdated) {
                this.id = id;
                this.name = name;
                this.version = version;
                this.isOutdated = isOutdated;
            }

            @Override
            public boolean mouseClicked(double d, double e, int i) {
                if (i == 0) {
                    this.select();
                    return true;
                } else {
                    return false;
                }
            }

            private void select(){
                FrpcImplSelectionScreen.FrpcImplSelectionList.this.setSelected(this);
            }

            @Override
            public void render(PoseStack poseStack, int i, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isHovered, float f) {
                fill(poseStack,x,y,x+entryWidth,y+entryHeight,0x8f2b2b2b);
                drawString(poseStack, FrpcImplSelectionScreen.FrpcImplSelectionList.this.minecraft.font, name, x + 4, y + 4, 0xffffffff);
                drawString(poseStack, FrpcImplSelectionScreen.FrpcImplSelectionList.this.minecraft.font, id, x + 4, y + 4 + (entryHeight-4) / 2, 0xffffffff);
                drawString(poseStack, FrpcImplSelectionScreen.FrpcImplSelectionList.this.minecraft.font, version, x + entryWidth - 4 - FrpcImplSelectionScreen.FrpcImplSelectionList.this.minecraft.font.width(version), y + 4, 0xffffffff);
                drawString(poseStack, FrpcImplSelectionScreen.FrpcImplSelectionList.this.minecraft.font, isOutdated?Utils.translatableText("text.openlink.outdated"):Utils.translatableText("text.openlink.latest"), x + entryWidth - 4 - FrpcImplSelectionScreen.FrpcImplSelectionList.this.minecraft.font.width(isOutdated?Utils.translatableText("text.openlink.outdated"):Utils.translatableText("text.openlink.latest")), y + 4 + (entryHeight-4) / 2, 0xffffffff);

            }
        }
    }
}
