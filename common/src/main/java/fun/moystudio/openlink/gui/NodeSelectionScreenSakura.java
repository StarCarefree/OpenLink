package fun.moystudio.openlink.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import fun.moystudio.openlink.frpcimpl.SakuraFrpFrpcImpl;
import fun.moystudio.openlink.json.*;
import fun.moystudio.openlink.logic.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class NodeSelectionScreenSakura extends Screen {
    Screen lastscreen;
    NodeSelectionList selectionList;
    Button done;
    public NodeSelectionScreenSakura(Screen lastscreen) {
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
            if(selectionList==null||selectionList.getSelected()==null||selectionList.getSelected().node.getSecond().id==-1){
                SakuraFrpFrpcImpl.nodeId=-1;
                this.minecraft.setScreen(lastscreen);
                return;
            }
            SakuraFrpFrpcImpl.nodeId=selectionList.getSelected().node.getSecond().id;
            this.minecraft.setScreen(lastscreen);
        }).bounds(this.width / 2 - 100, this.height - 38, 200, 20).build());
        this.addWidget(selectionList);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
        super.render(guiGraphics, i, j, f);
        if(selectionList!=null){
            selectionList.render(guiGraphics,i,j,f);
            if(selectionList.userInfo!=null&&selectionList.userInfo.realname==0) {
                guiGraphics.drawString(this.minecraft.font, Utils.translatableText("text.openlink.realnametounlock"),0,this.height-this.minecraft.font.lineHeight, 0xffffff);
            }
        }
        guiGraphics.drawCenteredString(this.font,this.title,this.width/2,16,0xffffff);
        done.render(guiGraphics,i,j,f);
    }

    class NodeSelectionList extends ObjectSelectionList<NodeSelectionList.Entry>{
        public NodeSelectionList(Minecraft minecraft) {
            super(minecraft, NodeSelectionScreenSakura.this.width, NodeSelectionScreenSakura.this.height-65+4-32, 32, 40);
            JsonNodesSakura.node nothingnode = new JsonNodesSakura.node();
            nothingnode.vip = 0;
            nothingnode.flag = 1<<2;
            nothingnode.name = CommonComponents.GUI_BACK.getString();
            nothingnode.description = Utils.translatableText("text.openlink.node_autoselect").getString();
            JsonNodeStatsSakura.node_stat nothingstat = new JsonNodeStatsSakura.node_stat();
            nothingstat.id=-1;
            Entry entry = new Entry(Pair.of(nothingnode,nothingstat));
            this.addEntry(entry);
            this.setSelected(entry);
            new Thread(()->{
                List<Pair<JsonNodesSakura.node, JsonNodeStatsSakura.node_stat>> nodes;
                try {
                    nodes=SakuraFrpFrpcImpl.getNodeList();
                    userInfo = SakuraFrpFrpcImpl.getUserInfo();
                    for(Pair<JsonNodesSakura.node, JsonNodeStatsSakura.node_stat> nodePair:nodes){
                        if(SettingScreen.unavailableNodeHiding && userInfo!=null){
                            if(nodePair.getFirst().vip>userInfo.group.level||nodePair.getSecond().online!=0||(nodePair.getFirst().flag&(1<<2))==0) {
                                continue;
                            }
                        }
                        Entry entry1=new Entry(nodePair);
                        this.addEntry(entry1);
                        if(nodePair.getSecond().id==SakuraFrpFrpcImpl.nodeId){
                            this.setSelected(entry1);
                            this.centerScrollOn(entry1);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    this.minecraft.setScreen(lastscreen);
                }
            },"Request thread").start();
            if (this.getSelected() != null) {
                this.centerScrollOn(this.getSelected());
            }
        }

        public JsonUserInfoSakura userInfo = null;

        public void changePos(int width, int height, int y0, int y1){
            this.width=width;
            this.height=height;
            this.setY(y0);
        }

        @Override
        public boolean isFocused(){
            return NodeSelectionScreenSakura.this.getFocused() == this;
        }

        @Override
        public int getRowWidth() {
            return super.getRowWidth() + 50;
        }

        public class Entry extends ObjectSelectionList.Entry<Entry>{
            Pair<JsonNodesSakura.node, JsonNodeStatsSakura.node_stat> node;
            public Entry(Pair<JsonNodesSakura.node, JsonNodeStatsSakura.node_stat> node){
                this.node=node;
            }

            @Override
            public @NotNull Component getNarration() {
                return Utils.translatableText("narrator.select",this.node.getFirst().name);
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
                NodeSelectionList.this.setSelected(this);
            }

            @Override
            public void render(GuiGraphics guiGraphics, int i, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isHovered, float f) {
                guiGraphics.fill(x,y,x+entryWidth,y+entryHeight,0x8f2b2b2b);
                String group;
                switch ((int) this.node.getFirst().vip) {
                    case 0:
                        group="普通";
                        break;
                    case 3:
                        group="§a§l青铜";
                        break;
                    default:
                        group="§e§l白银";
                }
                boolean unavailable = (userInfo!=null&&node.getFirst().vip>userInfo.group.level)||node.getSecond().online!=0||(node.getFirst().flag&(1<<2))==0;
                guiGraphics.drawString(NodeSelectionScreenSakura.NodeSelectionList.this.minecraft.font, "#"+this.node.getSecond().id+" "+this.node.getFirst().name+" "+group, x + 4, y + 4, 0xffffffff);
                guiGraphics.drawString(NodeSelectionScreenSakura.NodeSelectionList.this.minecraft.font, this.node.getFirst().description==""?"通用穿透节点":this.node.getFirst().description, x + 4, y + 4 + (entryHeight-4) / 2, 0xffffffff);
                guiGraphics.drawString(NodeSelectionScreenSakura.NodeSelectionList.this.minecraft.font, unavailable?Utils.translatableText("text.openlink.node_unavailable"):Utils.translatableText("text.openlink.node_available"), x + entryWidth - 4 - NodeSelectionScreenSakura.NodeSelectionList.this.minecraft.font.width(unavailable?Utils.translatableText("text.openlink.node_unavailable"):Utils.translatableText("text.openlink.node_available")), y + 4, 0xffffffff);
                guiGraphics.drawString(NodeSelectionScreenSakura.NodeSelectionList.this.minecraft.font, this.node.getSecond().load+"%", x + entryWidth - 4 - NodeSelectionScreenSakura.NodeSelectionList.this.minecraft.font.width(this.node.getSecond().load+"%"), y + 4 + (entryHeight-4) / 2, 0xffffffff);
            }
        }
    }
}
