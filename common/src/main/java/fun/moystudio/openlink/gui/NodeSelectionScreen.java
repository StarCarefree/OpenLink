package fun.moystudio.openlink.gui;

import fun.moystudio.openlink.frpc.Frpc;
import fun.moystudio.openlink.json.JsonNode;
import fun.moystudio.openlink.logic.Utils;
import fun.moystudio.openlink.network.Request;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class NodeSelectionScreen extends Screen {
    Screen lastscreen;
    NodeSelectionList selectionList;
    Button done;
    public NodeSelectionScreen(Screen lastscreen) {
        super(Utils.translatableText("gui.openlink.nodeselectionscreentitle"));
        this.lastscreen=lastscreen;
    }

    @Override
    public void onClose(){
        this.minecraft.setScreen(lastscreen);
    }

    @Override
    protected void init() {
        if(selectionList==null){
            selectionList=new NodeSelectionList(this.minecraft);
        }
        selectionList.changePos(this.width, this.height, 32, this.height-65+4);
        this.addWidget(done=Button.builder(CommonComponents.GUI_DONE, (button) -> {
            if(selectionList==null||selectionList.getSelected()==null||selectionList.getSelected().node.id==-1){
                Frpc.nodeId=-1;
                this.minecraft.setScreen(lastscreen);
                return;
            }
            Frpc.nodeId=selectionList.getSelected().node.id;
            this.minecraft.setScreen(lastscreen);
        }).bounds(this.width / 2 - 100, this.height - 38, 200, 20).build());
        this.addWidget(selectionList);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
        super.render(guiGraphics, i, j, f);
        this.renderBackground(guiGraphics);
        if(selectionList!=null){
            selectionList.render(guiGraphics,i,j,f);
        }
        guiGraphics.drawCenteredString(this.font,this.title,this.width/2,16,0xffffff);
        done.render(guiGraphics,i,j,f);
    }

    class NodeSelectionList extends ObjectSelectionList<NodeSelectionList.Entry>{
        public NodeSelectionList(Minecraft minecraft) {
            super(minecraft, NodeSelectionScreen.this.width, NodeSelectionScreen.this.height, 32, NodeSelectionScreen.this.height-65+4, 40);
            JsonNode nothing=new JsonNode();
            nothing.name=CommonComponents.GUI_BACK.getString();
            nothing.id=-1;
            nothing.status=200;
            nothing.description=Utils.translatableText("text.openlink.node_autoselect").getString();
            Entry entry=new Entry(nothing);
            this.addEntry(entry);
            this.setSelected(entry);
            new Thread(()->{
                List<JsonNode> nodes;
                try {
                    nodes=Request.getNodeList().data.list;
                    for(JsonNode node:nodes){
                        Entry entry1=new Entry(node);
                        this.addEntry(entry1);
                        if(node.id==Frpc.nodeId){
                            this.setSelected(entry1);
                            this.centerScrollOn(entry1);
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    this.minecraft.setScreen(lastscreen);
                }
            },"Request thread").start();
            this.setRenderBackground(false);
            if (this.getSelected() != null) {
                this.centerScrollOn(this.getSelected());
            }
        }

        public void changePos(int width, int height, int y0, int y1){
            this.width=width;
            this.height=height;
            this.y0=y0;
            this.y1=y1;
        }

        @Override
        public boolean isFocused(){
            return NodeSelectionScreen.this.getFocused() == this;
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
        public void render(GuiGraphics guiGraphics, int i, int j, float f) {
            super.render(guiGraphics, i, j, f);
        }

        public class Entry extends ObjectSelectionList.Entry<Entry>{
            JsonNode node;
            public Entry(JsonNode node){
                this.node=node;
            }

            @Override
            public @NotNull Component getNarration() {
                return Utils.translatableText("narrator.select",this.node.name);
            }

            public boolean mouseClicked(double d, double e, int i) {
                if (i == 0) {
                    this.select();
                    return true;
                } else {
                    return false;
                }
            }

            private void select(){
                NodeSelectionList.this.setSelected(this);
            }

            @Override
            public void render(GuiGraphics guiGraphics, int i, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isHovered, float f) {
                guiGraphics.fill(x,y,x+entryWidth,y+entryHeight,0x8f2b2b2b);
                String group=null;
                if(this.node.group!=null){
                    group = this.node.group.split(";")[0].toUpperCase();
                    if(group.equals("VIP")){
                        group="§e§l"+group;
                    }
                    if(group.equals("SVIP")){
                        group="§6§l"+group;
                    }
                }
                guiGraphics.drawString(NodeSelectionScreen.NodeSelectionList.this.minecraft.font, "#"+this.node.id+" "+this.node.name+(group!=null&&!group.equals("ADMIN")&&!group.equals("DEV")?" "+group:""), x + 4, y + 4, 0xffffffff);
                guiGraphics.drawString(NodeSelectionScreen.NodeSelectionList.this.minecraft.font, this.node.description, x + 4, y + 4 + (entryHeight-4) / 2, 0xffffffff);
                guiGraphics.drawString(NodeSelectionScreen.NodeSelectionList.this.minecraft.font, this.node.fullyLoaded||this.node.status!=200?Utils.translatableText("text.openlink.node_unavailable"):(this.node.needRealname?Utils.translatableText("text.openlink.node_needrealname"):Utils.translatableText("text.openlink.node_available")), x + entryWidth - 4 - NodeSelectionScreen.NodeSelectionList.this.minecraft.font.width(this.node.fullyLoaded||this.node.status!=200?Utils.translatableText("text.openlink.node_unavailable"):(this.node.needRealname?Utils.translatableText("text.openlink.node_needrealname"):Utils.translatableText("text.openlink.node_available"))), y + 4, 0xffffffff);
                guiGraphics.drawString(NodeSelectionScreen.NodeSelectionList.this.minecraft.font, this.node.bandwidth+"Mbps"+(this.node.bandwidthMagnification>1?" * "+this.node.bandwidthMagnification:""), x + entryWidth - 4 - NodeSelectionScreen.NodeSelectionList.this.minecraft.font.width(this.node.bandwidth+"Mbps"+(this.node.bandwidthMagnification>1?" * "+this.node.bandwidthMagnification:"")), y + 4 + (entryHeight-4) / 2, 0xffffffff);
            }
        }
    }
}
