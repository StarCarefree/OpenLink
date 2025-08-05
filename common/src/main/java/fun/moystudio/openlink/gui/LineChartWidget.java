package fun.moystudio.openlink.gui;

//其实都是ds写得，我可搞不懂这些玩意

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import fun.moystudio.openlink.logic.Utils;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

//不小心把terry感谢Bresenham的注释删了awa
public class LineChartWidget extends GuiComponent implements Widget, GuiEventListener {
    public List<Pair<String, Long>> dataPoints;
    public int x1, y1, x2, y2, width, height;
    public Font font;
    public Component labelX, labelY;
    private final Screen parentScreen;

    public LineChartWidget(Screen parentScreen, Font font, int x1, int y1, int x2, int y2,
                           Component labelX, Component labelY,
                           List<Pair<String, Long>> dataPoints) {
        this.parentScreen = parentScreen;
        this.font = font;
        this.dataPoints = dataPoints;
        this.labelX = labelX;
        this.labelY = labelY;
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.width = x2 - x1 + 1;
        this.height = y2 - y1 + 1;
    }

    private void drawLine(PoseStack poseStack, int x1, int y1, int x2, int y2, int color) {
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int sx = (x1 < x2) ? 1 : -1;
        int sy = (y1 < y2) ? 1 : -1;
        int err = dx - dy;

        while(true) {
            fill(poseStack, x1, y1, x1 + 1, y1 + 1, color);
            if(x1 == x2 && y1 == y2) break;

            int e2 = err * 2;
            if(e2 > -dy) {
                err -= dy;
                x1 += sx;
            }
            if(e2 < dx) {
                err += dx;
                y1 += sy;
            }
        }
    }

    private double ceilToNiceNumber(double value) {
        if (value <= 0) return 10;

        int exponent = (int) Math.floor(Math.log10(value));
        double base = Math.pow(10, exponent);
        double normalized = value / base;

        double[] niceNumbers = {1, 5, 10};
        for (double nice : niceNumbers) {
            if (normalized <= nice) {
                return nice * base;
            }
        }
        return 1 * Math.pow(10, exponent + 1);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        // 左下——右上坐标系
        int beginX = x1, beginY = y2, endX = x2, endY = y1;
        int adjustedX1 = this.x1;

        // 计算最大值
        Pair<String, Long> maxDataVal = dataPoints.stream()
                .max(Comparator.comparingLong(Pair::getSecond))
                .orElse(new Pair<>("nope", 1L));

        // 单位转换逻辑
        double maxValue = maxDataVal.getSecond();
        String unit = "MiB";
        if (maxValue > 1024) {
            maxValue /= 1024.0; // 转换为GiB
            unit = "GiB";
        }

        // 数值取整
        double ceiledMax = ceilToNiceNumber(maxValue);
        String maxValueStr = String.format("%.1f", ceiledMax);

        // 调整Y轴位置防止溢出
        if(dataPoints.size() > 1 && font.width(maxValueStr + " " + unit) > font.width("114")) {
            adjustedX1 += font.width(maxValueStr + " " + unit) - font.width("114");
            beginX = adjustedX1;
            width = x2 - adjustedX1 + 1;
        }

        // 绘制坐标轴
        hLine(poseStack, beginX, endX, beginY, 0xffffffff); // x轴
        vLine(poseStack, beginX, beginY, endY, 0xffffffff); // y轴

        if(dataPoints.size() > 1) {
            // 绘制刻度线
            for (int k = 1; k <= 5; k++) {
                int y = beginY - k * (height - 5) / 5;
                hLine(poseStack, beginX, endX, y, 0x7fffffff);
            }

            // 计算比例尺（考虑单位转换）
            float yScale;
            if (unit.equals("GiB")) {
                yScale = (float) (height - 5) / (float) ceiledMax;
            } else {
                yScale = (float) (height - 5) / maxDataVal.getSecond();
            }

            float xScale = (float) (width - 10) / (dataPoints.size() - 1);

            // 绘制折线
            for (int k = 0; k < dataPoints.size() - 1; k++) {
                double val1 = unit.equals("GiB") ?
                        dataPoints.get(k).getSecond() / 1024.0 :
                        dataPoints.get(k).getSecond();

                double val2 = unit.equals("GiB") ?
                        dataPoints.get(k + 1).getSecond() / 1024.0 :
                        dataPoints.get(k + 1).getSecond();

                int x11 = (int) (beginX + 5 + k * xScale);
                int y1 = (int) (beginY - val1 * yScale);
                int x22 = (int) (beginX + 5 + (k + 1) * xScale);
                int y2 = (int) (beginY - val2 * yScale);

                drawLine(poseStack, x11, y1, x22, y2, 0x7f66ccff);
            }

            // 绘制数据点和交互
            List<Integer> dataX = new ArrayList<>();
            boolean tooltip = false;
            int tooltipIndex = 0;
            Component tooltipText = null;

            for (int k = 0; k < dataPoints.size(); k++) {
                double val = unit.equals("GiB") ?
                        dataPoints.get(k).getSecond() / 1024.0 :
                        dataPoints.get(k).getSecond();

                int pointScale = 2;
                int pointX = (int) (beginX + 5 + k * xScale);
                int pointY = (int) (beginY - val * yScale);
                dataX.add(pointX);

                // 检测鼠标悬停
                if (mouseX >= pointX - pointScale && mouseX <= pointX + pointScale &&
                        mouseY >= pointY - pointScale && mouseY <= pointY + pointScale) {

                    pointScale = pointScale * 3 / 2;
                    tooltip = true;
                    tooltipIndex = k;

                    // 准备工具提示文本
                    double displayValue = unit.equals("GiB") ?
                            dataPoints.get(k).getSecond() / 1024.0 :
                            dataPoints.get(k).getSecond();

                    tooltipText = Utils.literalText(
                            String.format("%s, %.1f %s",
                                    dataPoints.get(k).getFirst(),
                                    displayValue,
                                    unit)
                    );
                }

                fill(poseStack,
                        pointX - pointScale, pointY - pointScale,
                        pointX + pointScale, pointY + pointScale,
                        0xff66ccff);
            }

            // 显示工具提示 - 使用Screen的renderTooltip方法
            if (tooltip && tooltipText != null && parentScreen != null) {
                parentScreen.renderTooltip(poseStack, tooltipText, mouseX, mouseY);
            }

            // 绘制X轴标签
            for (int k = 0; k < dataX.size(); k++) {
                String toRender = dataPoints.get(k).getFirst();
                if (toRender.contains(" ")) {
                    String[] parts = toRender.split(" ");
                    drawCenteredString(poseStack, font, parts[0], dataX.get(k), y2 + 5, 0xffffff);
                    drawCenteredString(poseStack, font, parts[1], dataX.get(k), y2 + 5 + font.lineHeight, 0xffffff);
                } else {
                    drawCenteredString(poseStack, font, toRender, dataX.get(k), y2 + 5, 0xffffff);
                }
            }
            //Y轴标签
            for (int k = 1; k <= 5; k++) {
                int y = beginY - k * (height - 5) / 5;
                double value = k * ceiledMax / 5.0;
                // 修改：只显示整数，不带单位
                String label = String.format("%d", (int) value);
                drawString(poseStack, font, label,
                        adjustedX1 - font.width(label),
                        y - 3,
                        0xffffff);
            }
        } else {
            // 无数据提示
            drawCenteredString(poseStack, font,
                    Utils.translatableText("text.openlink.nodata"),
                    x1 + (x2 - x1) / 2,
                    y1 + (y2 - y1) / 2,
                    0x7f66ccff);
        }

        // 绘制坐标轴标签
        drawCenteredString(poseStack, font, labelX, x2 - 10, y2 - 10, 0xffffff);
        drawString(poseStack, font, labelY, adjustedX1, y1 - 5, 0xffffff);

        Component fullLabelY = Utils.translatableText("text.openlink.y_axis_label_with_unit",
                labelY.getString(), unit);
        drawString(poseStack, font, fullLabelY, adjustedX1, y1 - 5, 0xffffff);
    }
}