package questgrupo.questmod.client.editor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import questgrupo.questmod.client.GlobalGuiSettings;
import java.util.function.Consumer;
import java.lang.Math;

public class TopBar {
    private static boolean visible = false;
    private static boolean textToolsVisible = false;
    private static boolean drawingToolsVisible = false;
    private static int barWidth = 0;

    public static final int BTN_FILL_COLOR = 10, BTN_BORDER_COLOR = 11;
    public static final int BTN_MINUS_TEXT_SCALE = 12, BTN_PLUS_TEXT_SCALE = 13;
    public static final int BTN_MINUS_ICON_SCALE = 14, BTN_PLUS_ICON_SCALE = 15;
    public static final int BTN_MOVE_TEXT_LEFT = 16, BTN_MOVE_TEXT_RIGHT = 17;
    public static final int BTN_MOVE_ICON_LEFT = 18, BTN_MOVE_ICON_RIGHT = 19;
    public static final int BTN_TEXT_COLOR = 20, BTN_MISSION_FILL_COLOR = 21, BTN_MISSION_BORDER_COLOR = 22;
    public static final int BTN_HEADER_FILL_COLOR = 30, BTN_HEADER_BORDER_COLOR = 31;
    public static final int BTN_MOVE_TEXT_UP = 40, BTN_MOVE_TEXT_DOWN = 41;
    public static final int BTN_MOVE_MISSION_TEXT_UP = 42, BTN_MOVE_MISSION_TEXT_DOWN = 43;
    public static final int BTN_MOVE_ICON_UP = 44, BTN_MOVE_ICON_DOWN = 45;

    public static void setVisible(boolean v) { visible = v; }
    public static boolean isVisible() { return visible; }

    public static int getHeight() {
        if (!visible) return 0;
        return 48;
    }

    public static int getVisualHeight(GlobalGuiSettings.PanelConfig pSel) {
        if (!visible) return 0;
        if (drawingToolsVisible && pSel != null && pSel.tipo.startsWith("DESPLEGABLE")) {
            return 52;
        }
        return 48;
    }

    public static int getWidth() { return barWidth; }

    public static void setTextToolsVisible(boolean v) {
        textToolsVisible = v;
        drawingToolsVisible = false;
    }

    public static boolean isTextToolsVisible() { return textToolsVisible; }
    public static boolean isDrawingToolsVisible() { return drawingToolsVisible; }

    public static void setDrawingToolsVisible(boolean v) {
        drawingToolsVisible = v;
        textToolsVisible = false;
        if (v) {
            questgrupo.questmod.client.gui.FigurasEdit.editandoColorIndex = 0;
        }
    }

    private static void drawContainer(GuiGraphics g, int x, int y, int width, int height) {
        g.fill(x + 1, y + 1, x + width - 1, y + height - 1, 0xFFC6C6C6);
        g.fill(x, y, x + width, y + 1, 0xFF000000);
        g.fill(x, y + height - 1, x + width, y + height, 0xFF000000);
        g.fill(x, y + 1, x + 1, y + height - 1, 0xFF000000);
        g.fill(x + width - 1, y + 1, x + width, y + height - 1, 0xFF000000);
        g.fill(x + 1, y + 1, x + width - 2, y + 2, 0xFFFFFFFF);
        g.fill(x + 1, y + 2, x + 2, y + height - 2, 0xFFFFFFFF);
        g.fill(x + 1, y + height - 2, x + width - 1, y + height - 1, 0xFF555555);
        g.fill(x + width - 2, y + 1, x + width - 1, y + height - 2, 0xFF555555);
    }

    private static void drawColorSwatch(GuiGraphics g, int color, int x, int y) {
        g.fill(x, y, x + 16, y + 16, color);
        g.renderOutline(x - 1, y - 1, 18, 18, 0xFF000000);
    }

    public static void render(GuiGraphics g, int guiWidth, int y, GlobalGuiSettings.TextConfig tSel, GlobalGuiSettings.PanelConfig pSel) {
        if (!visible) return;

        int barX = LeftSidebar.getSidebarWidth();
        barWidth = calculateBarWidth(textToolsVisible, drawingToolsVisible, tSel, pSel);
        int barStartX = barX + (guiWidth - barX - barWidth) / 2;

        int visualBoxHeight = getVisualHeight(pSel);

        drawContainer(g, barStartX, y, barWidth, visualBoxHeight);

        boolean esMisionTexto = pSel != null && (pSel.tipo.equals("MISION_TITULO") || pSel.tipo.equals("MISION_DESCRIPCION"));

        if ((textToolsVisible && tSel != null) || esMisionTexto) {
            renderTextTools(g, barStartX, y, tSel, pSel);
        } else if (drawingToolsVisible) {
            renderDrawingTools(g, barStartX, y, pSel);
        }
    }

    public static int calculateBarWidth(boolean textTools, boolean drawTools, GlobalGuiSettings.TextConfig tSel, GlobalGuiSettings.PanelConfig pSel) {
        boolean esMisionTexto = pSel != null && (pSel.tipo.equals("MISION_TITULO") || pSel.tipo.equals("MISION_DESCRIPCION"));

        if (textTools || esMisionTexto) {
            int w = 4;
            w += esMisionTexto ? 38 : 18;
            w += 2;
            w += (5 * 18) + (4 * 2);
            w += 4;
            return w;
        } else if (drawTools && pSel != null) {
            if (pSel.tipo.startsWith("DESPLEGABLE")) {
                int w = 6 + 80;
                if (pSel.textoAsociado != null && !pSel.textoAsociado.isEmpty()) {
                    w += 46 * 3;
                }
                w += 6;
                return w;
            } else {
                int w = 6 + 20 + (pSel.tipo.equals("LINEA") || pSel.tipo.equals("TRIANGULO") ? 0 : 20);
                if (pSel.textoAsociado != null && !pSel.textoAsociado.isEmpty()) {
                    w += 46 * 2;
                }
                w += 6;
                return w;
            }
        }
        return 0;
    }

    private static void renderTextTools(GuiGraphics g, int barStartX, int y, GlobalGuiSettings.TextConfig tSel, GlobalGuiSettings.PanelConfig pSel) {
        int swatchSize = 16;
        int startX = barStartX + 4;
        int rowY = y + 4;
        boolean esMisionTexto = pSel != null && (pSel.tipo.equals("MISION_TITULO") || pSel.tipo.equals("MISION_DESCRIPCION"));

        if (esMisionTexto) {
            int swatch2X = startX + swatchSize + 4;
            int rowY2 = rowY + swatchSize + 4;

            drawColorSwatch(g, pSel.colorARGB, startX + 1, rowY + 1);
            drawColorSwatch(g, pSel.colorBorde, swatch2X + 1, rowY + 1);
            drawColorSwatch(g, pSel.colorTexto, startX + 1, rowY2 + 1);
        } else {
            drawColorSwatch(g, tSel.colorARGB, startX + 1, rowY + 1);
        }
    }

    public static int getColorClick(int mx, int my, int guiWidth, int y, GlobalGuiSettings.TextConfig tSel, GlobalGuiSettings.PanelConfig pSel) {
        if (!isVisible()) return -1;

        int barX = LeftSidebar.getSidebarWidth();
        int barW = getWidth();
        int barStartX = barX + (guiWidth - barX - barW) / 2;
        boolean esMisionTexto = pSel != null && (pSel.tipo.equals("MISION_TITULO") || pSel.tipo.equals("MISION_DESCRIPCION"));

        int swatchSize = 16;
        int rowY = y + 4;
        int startX = barStartX + 4;

        if (esMisionTexto) {
            int swatch2X = startX + swatchSize + 4;
            int rowY2 = rowY + swatchSize + 4;

            if (mx >= startX && mx <= startX + swatchSize + 2 && my >= rowY && my <= rowY + swatchSize + 2) return 7;
            if (mx >= swatch2X && mx <= swatch2X + swatchSize + 2 && my >= rowY && my <= rowY + swatchSize + 2) return 8;
            if (mx >= startX && mx <= startX + swatchSize + 2 && my >= rowY2 && my <= rowY2 + swatchSize + 2) return 9;
        } else if (tSel != null) {
            if (mx >= startX && mx <= startX + swatchSize + 2 && my >= rowY && my <= rowY + swatchSize + 2) return 0;
        }
        return -1;
    }

    private static void drawSectionTitle(GuiGraphics g, Font font, String title, int x, int y, int width) {
        int textW = font.width(title);
        int textX = x + (width - textW) / 2;
        g.drawString(font, title, textX, y, 0xFF444444, false);
        g.fill(x, y + 4, textX - 2, y + 5, 0xFF888888);
        g.fill(textX + textW + 2, y + 4, x + width, y + 5, 0xFF888888);
    }

    private static void drawVerticalSeparator(GuiGraphics g, int x, int y, int height) {
        g.fill(x, y, x + 1, y + height, 0xFF888888);
        g.fill(x + 1, y, x + 2, y + height, 0xFFFFFFFF);
    }

    private static void renderDrawingTools(GuiGraphics g, int barStartX, int y, GlobalGuiSettings.PanelConfig pSel) {
        if (pSel == null) return;
        Font font = Minecraft.getInstance().font;

        if (pSel.tipo.startsWith("DESPLEGABLE")) {
            int colorsX = barStartX + 6;
            drawSectionTitle(g, font, "COLORES", colorsX, y + 2, 76);

            int row1Y = y + 12;
            int row2Y = y + 32;

            drawColorSwatch(g, pSel.colorFondoCabecera, colorsX, row1Y);
            drawColorSwatch(g, pSel.colorBordeCabecera, colorsX + 20, row1Y);
            drawColorSwatch(g, pSel.colorARGB, colorsX + 40, row1Y);
            drawColorSwatch(g, pSel.colorBorde, colorsX + 60, row1Y);

            drawColorSwatch(g, pSel.colorTexto, colorsX, row2Y);
            drawColorSwatch(g, pSel.colorFondoMision, colorsX + 20, row2Y);
            drawColorSwatch(g, pSel.colorBordeMision, colorsX + 40, row2Y);

            int currentX = colorsX + 80;

            if (pSel.textoAsociado != null && !pSel.textoAsociado.isEmpty()) {
                drawVerticalSeparator(g, currentX, y + 4, 44);
                currentX += 4;
                drawSectionTitle(g, font, "T", currentX, y + 2, 38);
                currentX += 46;

                drawVerticalSeparator(g, currentX - 4, y + 4, 44);
                drawSectionTitle(g, font, "TM", currentX, y + 2, 38);
                currentX += 46;

                drawVerticalSeparator(g, currentX - 4, y + 4, 44);
                drawSectionTitle(g, font, "I", currentX, y + 2, 38);
            }

        } else {
            int currentX = barStartX + 6;
            int boxY = y + 16;

            drawColorSwatch(g, pSel.colorARGB, currentX, boxY);
            currentX += 20;

            if (!pSel.tipo.equals("LINEA") && !pSel.tipo.equals("TRIANGULO")) {
                drawColorSwatch(g, pSel.colorBorde, currentX, boxY);
                currentX += 20;
            }

            if (pSel.textoAsociado != null && !pSel.textoAsociado.isEmpty()) {
                drawVerticalSeparator(g, currentX, y + 4, 40);
                currentX += 4;
                drawSectionTitle(g, font, "T", currentX, y + 2, 38);
                currentX += 46;

                drawVerticalSeparator(g, currentX - 4, y + 4, 40);
                drawSectionTitle(g, font, "I", currentX, y + 2, 38);
            }
        }
    }

    public static int getDrawingButtonAt(int mx, int my, int guiWidth, int y, GlobalGuiSettings.PanelConfig pSel) {
        if (!drawingToolsVisible || pSel == null || my < y || my > y + getVisualHeight(pSel)) return -1;

        int barX = LeftSidebar.getSidebarWidth();
        int barW = calculateBarWidth(false, true, null, pSel);
        barWidth = barW;
        int barStartX = barX + (guiWidth - barX - barW) / 2;

        if (pSel.tipo.startsWith("DESPLEGABLE")) {
            int colorsX = barStartX + 6;
            int row1Y = y + 12;
            int row2Y = y + 32;

            if (my >= row1Y && my <= row1Y + 16) {
                if (mx >= colorsX && mx <= colorsX + 16) return BTN_HEADER_FILL_COLOR;
                if (mx >= colorsX + 20 && mx <= colorsX + 36) return BTN_HEADER_BORDER_COLOR;
                if (mx >= colorsX + 40 && mx <= colorsX + 56) return BTN_FILL_COLOR;
                if (mx >= colorsX + 60 && mx <= colorsX + 76) return BTN_BORDER_COLOR;
            }
            if (my >= row2Y && my <= row2Y + 16) {
                if (mx >= colorsX && mx <= colorsX + 16) return BTN_TEXT_COLOR;
                if (mx >= colorsX + 20 && mx <= colorsX + 36) return BTN_MISSION_FILL_COLOR;
                if (mx >= colorsX + 40 && mx <= colorsX + 56) return BTN_MISSION_BORDER_COLOR;
            }
        } else {
            int currentX = barStartX + 6;
            int boxY = y + 16;
            if (my >= boxY && my <= boxY + 16) {
                if (mx >= currentX && mx <= currentX + 16) return BTN_FILL_COLOR;
                currentX += 20;
                if (!pSel.tipo.equals("LINEA") && !pSel.tipo.equals("TRIANGULO")) {
                    if (mx >= currentX && mx <= currentX + 16) return BTN_BORDER_COLOR;
                }
            }
        }
        return -1;
    }

    public static void inicializarBotonesMision(int guiWidth, int y, Consumer<Button> adder, GlobalGuiSettings.PanelConfig pSel) {
        if (pSel == null || pSel.textoAsociado == null || pSel.textoAsociado.isEmpty()) return;

        int barX = LeftSidebar.getSidebarWidth();
        int expectedBarW = calculateBarWidth(false, true, null, pSel);
        int barStartX = barX + (guiWidth - barX - expectedBarW) / 2;
        int btnSize = 18;

        if (pSel.tipo.startsWith("DESPLEGABLE")) {
            int btnY1 = y + 11;
            int btnY2 = y + 31;
            int curX = barStartX + 6 + 80 + 5;

            adder.accept(Button.builder(Component.literal("←"), b -> pSel.offsetXTexto -= 2.0f).bounds(curX, btnY1, btnSize, btnSize).build());
            adder.accept(Button.builder(Component.literal("→"), b -> pSel.offsetXTexto += 2.0f).bounds(curX + 20, btnY1, btnSize, btnSize).build());
            adder.accept(Button.builder(Component.literal("-"), b -> pSel.escalaTexto = Math.max(0.1f, pSel.escalaTexto - 0.1f)).bounds(curX, btnY2, btnSize, btnSize).build());
            adder.accept(Button.builder(Component.literal("+"), b -> pSel.escalaTexto = Math.min(10.0f, pSel.escalaTexto + 0.1f)).bounds(curX + 20, btnY2, btnSize, btnSize).build());
            curX += 46;

            adder.accept(Button.builder(Component.literal("←"), b -> pSel.offsetXTextoMision -= 2.0f).bounds(curX, btnY1, btnSize, btnSize).build());
            adder.accept(Button.builder(Component.literal("→"), b -> pSel.offsetXTextoMision += 2.0f).bounds(curX + 20, btnY1, btnSize, btnSize).build());
            adder.accept(Button.builder(Component.literal("-"), b -> pSel.escalaTextoMision = Math.max(0.1f, pSel.escalaTextoMision - 0.1f)).bounds(curX, btnY2, btnSize, btnSize).build());
            adder.accept(Button.builder(Component.literal("+"), b -> pSel.escalaTextoMision = Math.min(10.0f, pSel.escalaTextoMision + 0.1f)).bounds(curX + 20, btnY2, btnSize, btnSize).build());
            curX += 46;

            adder.accept(Button.builder(Component.literal("←"), b -> pSel.offsetXIcono -= 2.0f).bounds(curX, btnY1, btnSize, btnSize).build());
            adder.accept(Button.builder(Component.literal("→"), b -> pSel.offsetXIcono += 2.0f).bounds(curX + 20, btnY1, btnSize, btnSize).build());
            adder.accept(Button.builder(Component.literal("-"), b -> pSel.escalaIcono = Math.max(0.1f, pSel.escalaIcono - 0.1f)).bounds(curX, btnY2, btnSize, btnSize).build());
            adder.accept(Button.builder(Component.literal("+"), b -> pSel.escalaIcono = Math.min(10.0f, pSel.escalaIcono + 0.1f)).bounds(curX + 20, btnY2, btnSize, btnSize).build());

        } else {
            int btnY1 = y + 12;
            int btnY2 = y + 30;
            int curX = barStartX + 6 + 20 + (pSel.tipo.equals("LINEA") || pSel.tipo.equals("TRIANGULO") ? 0 : 20) + 4;

            adder.accept(Button.builder(Component.literal("←"), b -> pSel.offsetXTexto -= 2.0f).bounds(curX, btnY1, btnSize, btnSize).build());
            adder.accept(Button.builder(Component.literal("→"), b -> pSel.offsetXTexto += 2.0f).bounds(curX + 20, btnY1, btnSize, btnSize).build());
            adder.accept(Button.builder(Component.literal("-"), b -> pSel.escalaTexto = Math.max(0.1f, pSel.escalaTexto - 0.1f)).bounds(curX, btnY2, btnSize, btnSize).build());
            adder.accept(Button.builder(Component.literal("+"), b -> pSel.escalaTexto = Math.min(10.0f, pSel.escalaTexto + 0.1f)).bounds(curX + 20, btnY2, btnSize, btnSize).build());
            curX += 46;

            adder.accept(Button.builder(Component.literal("←"), b -> pSel.offsetXIcono -= 2.0f).bounds(curX, btnY1, btnSize, btnSize).build());
            adder.accept(Button.builder(Component.literal("→"), b -> pSel.offsetXIcono += 2.0f).bounds(curX + 20, btnY1, btnSize, btnSize).build());
            adder.accept(Button.builder(Component.literal("-"), b -> pSel.escalaIcono = Math.max(0.1f, pSel.escalaIcono - 0.1f)).bounds(curX, btnY2, btnSize, btnSize).build());
            adder.accept(Button.builder(Component.literal("+"), b -> pSel.escalaIcono = Math.min(10.0f, pSel.escalaIcono + 0.1f)).bounds(curX + 20, btnY2, btnSize, btnSize).build());
        }
    }
}