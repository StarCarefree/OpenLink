package fun.moystudio.openlink.gui;

import com.mojang.blaze3d.vertex.*;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

//是哪个大聪明想出来让我写折线统计图的？？？（zirran不要删注释后面调试用，这玩意真的难写）
public class LineChartWidget extends GuiComponent implements Widget, GuiEventListener {
    public List<Pair<String,Long>> dataPoints;
    //左上——右下
    public int x1,y1,x2,y2,width,height;
    public Font font;
    public Component labelX,labelY;
    public OnTooltip onTooltip;


    public LineChartWidget(Font font, int x1, int y1, int x2, int y2, Component labelX,Component labelY, List<Pair<String,Long>> dataPoints, OnTooltip onTooltip){
        this.font=font;
        this.dataPoints=dataPoints;
        this.labelX=labelX;
        this.labelY=labelY;
        this.x1=x1;
        this.y1=y1;
        this.x2=x2;
        this.y2=y2;
        this.width=x2-x1+1;
        this.height=y2-y1+1;
        this.onTooltip=onTooltip;
    }

    private void drawLine(PoseStack poseStack,int x1, int y1, int x2, int y2, int color) {
        //I love u Bresenham!!!
        //最喜欢Bresenham的一集（感谢他发明了直线算法，不然我还要去调RenderSystem的那一堆逆天方法）
        int dx=Math.abs(x2-x1);
        int dy=Math.abs(y2-y1);
        int sx=(x1<x2)?1:-1;
        int sy=(y1<y2)?1:-1;
        int err=dx-dy;
        while(true){
            fill(poseStack,x1,y1,x1+1,y1+1,color);
            if(x1==x2&&y1==y2){
                break;
            }
            int e2=err*2;
            if(e2>-dy){
                err-=dy;
                x1+=sx;
            }
            if(e2<dx) {
                err+=dx;
                y1+=sy;
            }
        }
    }


    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        //左下——右上
        int beginX = x1, beginY = y2, endX = x2, endY = y1;
        int x1=this.x1;
        Pair<String, Long> maxDataVal = dataPoints.stream().max(Comparator.comparingLong(Pair::getSecond)).orElse(new Pair<>("nope", 1L));//最大值
        if(dataPoints.size()>1&&font.width(String.format("%.1f",(double)maxDataVal.getSecond()))>font.width("114")){
            x1+=font.width(String.format("%.1f",(double)maxDataVal.getSecond()))-font.width("114");
            beginX=x1;
            width=x2-x1+1;
        }
        hLine(poseStack, beginX, endX, beginY, 0xffffffff);//x轴
        vLine(poseStack, beginX, beginY, endY, 0xffffffff);//y轴
        if(dataPoints.size()>1){
            for (int k = 1; k <= 5; k++) {
                int y = beginY - k * (height - 5) / 5;
                hLine(poseStack, beginX, endX, y, 0x7fffffff);//y轴刻度横线
            }
            float xScale = (float) (width - 10) / (dataPoints.size() - 1);//x轴数据放大倍数（留了10的空间）
            float yScale = (float) (height - 5) / maxDataVal.getSecond();//y轴数据放大倍数（留了5的空间）
            int pointScale;
            for (int k = 0; k < dataPoints.size() - 1; k++) {//size-1条线（打OI打的：越界会RE的）
                int x11 = (int) (beginX + 5 + k * xScale);
                int y1 = (int) (beginY - dataPoints.get(k).getSecond() * yScale);
                int x2 = (int) (beginX + 5 + (k + 1) * xScale);
                int y2 = (int) (beginY - dataPoints.get(k + 1).getSecond() * yScale);
                drawLine(poseStack, x11, y1, x2, y2, 0x7f66ccff);
            }
            List<Integer> dataX = new ArrayList<>();
            boolean tooltip=false;
            int tooltipindex = 0;
            for (int k = 0; k < dataPoints.size(); k++) {//size个点
                pointScale = 2;
                int pointX = (int) (beginX + 5 + k * xScale);
                int pointY = (int) (beginY - dataPoints.get(k).getSecond() * yScale);
                dataX.add(pointX);
                if (i >= pointX - pointScale && i <= pointX + pointScale && j >= pointY - pointScale && j <= pointY + pointScale) {
                    pointScale = pointScale * 3 / 2;
                    tooltip=true;
                    tooltipindex=k;
                }
                fill(poseStack, pointX - pointScale, pointY - pointScale, pointX + pointScale, pointY + pointScale, 0xff66ccff);
            }
            if(tooltip){
                onTooltip.onTooltip(new Pair<>(dataPoints.get(tooltipindex).getFirst(), dataPoints.get(tooltipindex).getSecond()), poseStack, i, j);
            }
            for (int k = 0; k < dataX.size(); k++) {
                String toRender=dataPoints.get(k).getFirst();//x轴刻度标签
                if(toRender.contains(" ")){
                    String[] arrayToRender=toRender.split(" ");
                    drawCenteredString(poseStack, font, arrayToRender[0], dataX.get(k), y2 + 5, 0xffffff);
                    drawCenteredString(poseStack, font, arrayToRender[1], dataX.get(k), y2 + 5 + font.lineHeight, 0xffffff);
                }
                else {
                    drawCenteredString(poseStack, font, toRender, dataX.get(k), y2 + 5, 0xffffff);
                }
            }
            for (int k = 1; k <= 5; k++) {
                int y = beginY - k * (height - 5) / 5;
                drawString(poseStack, font, String.format("%.1f", k * maxDataVal.getSecond() / 5.0), x1 - font.width(String.format("%.1f", k * maxDataVal.getSecond() / 5.0)), y - 3, 0xffffff);//y轴刻度标签
            }
        } else {
            drawCenteredString(poseStack,font,new TranslatableComponent("text.openlink.nodata"),x1+(x2-x1)/2,y1+(y2-y1)/2,0x7f66ccff);
        }
        drawCenteredString(poseStack,font,labelX,x2-10,y2-10,0xffffff);//x轴标签
        drawString(poseStack,font,labelY,x1,y1-5,0xffffff);//y轴标签
    }

    public interface OnTooltip {
        void onTooltip(Pair<String,Long> dataXY, PoseStack poseStack, int i, int j);
    }
}
