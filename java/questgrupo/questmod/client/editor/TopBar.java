package questgrupo.questmod.client.editor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import questgrupo.questmod.client.GlobalGuiSettings;
import java.util.ArrayList;
import java.util.List;

public class TopBar {
    private static boolean visible = false;
    private static boolean textToolsVisible = false;
    private static boolean drawingToolsVisible = false;
    private static int barWidth = 0;

    public static final int BTN_B = 0, BTN_I = 1, BTN_U = 2, BTN_S = 3, BTN_CASE = 4, BTN_SPACING = 5;
    public static final int BTN_FILL_COLOR = 10, BTN_BORDER_COLOR = 11;
    public static final int BTN_TEXT_COLOR = 20, BTN_MISSION_FILL_COLOR = 21, BTN_MISSION_BORDER_COLOR = 22;
    public static final int BTN_HEADER_FILL_COLOR = 30, BTN_HEADER_BORDER_COLOR = 31;
    public static final int BTN_MINUS_TEXT_SCALE = 12, BTN_PLUS_TEXT_SCALE = 13;
    public static final int BTN_MINUS_ICON_SCALE = 14, BTN_PLUS_ICON_SCALE = 15;
    public static final int BTN_MOVE_TEXT_LEFT = 16, BTN_MOVE_TEXT_RIGHT = 17;
    public static final int BTN_MOVE_ICON_LEFT = 18, BTN_MOVE_ICON_RIGHT = 19;
    public static final int BTN_MOVE_TEXT_UP = 40, BTN_MOVE_TEXT_DOWN = 41;
    public static final int BTN_MOVE_MISSION_TEXT_UP = 42, BTN_MOVE_MISSION_TEXT_DOWN = 43;
    public static final int BTN_MOVE_ICON_UP = 44, BTN_MOVE_ICON_DOWN = 45;
    public static final int BTN_MISSION_TEXT_COLOR = 46;
    public static final int BTN_LETTER_SPACING_PLUS = 60;
    public static final int BTN_LETTER_SPACING_MINUS = 61;

    public static record ButtonInfo(int x, int width, String text) {}

    public static List<ButtonInfo> getTextButtonInfos(int guiWidth, GlobalGuiSettings.TextConfig tSel) {
        List<ButtonInfo> infos = new ArrayList<>();
        if (!textToolsVisible || tSel == null) return infos;
        int barX = LeftSidebar.getSidebarWidth();
        int barW = calculateBarWidth(true, false, tSel, null);
        if (barW == 0) return infos;
        int barStartX = barX + (guiWidth - barX - barW) / 2;
        // FIX: Correct initial position: leftPadding(10) + previewWidth(15) + gapAfterPreview(5) = 30
        int curX = barStartX + 10 + 15 + 5;
        // FIX: Use fixed 20px width to match calculateBarWidth() which assumes 20px per button
        String[] btnTexts = {"B", "I", "U", "S", "aA", "↔", "Sh"};
        for (String text : btnTexts) {
            int w = 20; // Fixed width to match calculateBarWidth()
            infos.add(new ButtonInfo(curX, w, text));
            curX += w + 2; // 20px width + 2px gap
        }
        return infos;
    }

    public static void setVisible(boolean v) { visible = v; }
    public static boolean isVisible() { return visible; }

    public static int getHeight() {
        if (!visible) return 0;
        if (textToolsVisible) return 26; // 4px top + 18px button + 4px bottom = 26px
        if (drawingToolsVisible) return 45; // Original height for drawing tools
        return 0;
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

    private static void drawBarBackground(GuiGraphics g, int x, int y, int width, int height) {
        // Main gray fill
        g.fill(x + 1, y + 1, x + width - 1, y + height - 1, 0xFFC6C6C6);
        
        // 1px Black Outline with cut corners (rounded 1px/2px effect)
        int colorBorde = 0xFF000000;
        g.fill(x + 2, y, x + width - 2, y + 1, colorBorde); // Top
        g.fill(x + 2, y + height - 1, x + width - 2, y + height, colorBorde); // Bottom
        g.fill(x, y + 2, x + 1, y + height - 2, colorBorde); // Left
        g.fill(x + width - 1, y + 2, x + width, y + height - 2, colorBorde); // Right
        
        // Corner pixels for the 2px rounded effect
        g.fill(x + 1, y + 1, x + 2, y + 2, colorBorde); // Top-Left
        g.fill(x + width - 2, y + 1, x + width - 1, y + 2, colorBorde); // Top-Right
        g.fill(x + 1, y + height - 2, x + 2, y + height - 1, colorBorde); // Bottom-Left
        g.fill(x + width - 2, y + height - 2, x + width - 1, y + height - 1, colorBorde); // Bottom-Right

        // Shadow at the bottom outside
        g.fill(x + 2, y + height, x + width - 2, y + height + 2, 0x33000000);
        g.fill(x + width, y + 2, x + width + 2, y + height - 2, 0x33000000);
    }

    public static void render(GuiGraphics g, int guiWidth, int y, GlobalGuiSettings.TextConfig tSel, GlobalGuiSettings.PanelConfig pSel) {
        int barHeight = getHeight();
        if (barHeight == 0) return;

        int barX = LeftSidebar.getSidebarWidth();

        barWidth = calculateBarWidth(textToolsVisible, drawingToolsVisible, tSel, pSel);
        int barStartX = barX + (guiWidth - barX - barWidth) / 2;

        drawBarBackground(g, barStartX, y, barWidth, barHeight);

        if (textToolsVisible && tSel != null) {
            renderTextTools(g, barStartX, y, tSel);
        } else if (drawingToolsVisible) {
            renderDrawingTools(g, barStartX, y, pSel);
        }
    }

    private static int calculateBarWidth(boolean textTools, boolean drawTools, GlobalGuiSettings.TextConfig tSel, GlobalGuiSettings.PanelConfig pSel) {
        if (textTools && tSel != null) {
            // Constants for text tools bar layout
            int leftPadding = 10;
            int previewWidth = 15;
            int gapAfterPreview = 5;
            int buttonWidth = 20; // Fixed width to match getTextButtonInfos()
            int gapBetweenButtons = 2;
            int rightPadding = 10;
            int numButtons = 7; // B, I, U, S, aA, ↔, Sh
            
            // Calculate width of - and + buttons (same as TextoEdit.java)
            int minusWidth = net.minecraft.client.Minecraft.getInstance().font.width("-") + 6;
            int plusWidth = net.minecraft.client.Minecraft.getInstance().font.width("+") + 6;
            
            // Total width: leftPadding + preview + gap + 7 buttons + gaps + "-" button + gap + "+" button + rightPadding
            return leftPadding + previewWidth + gapAfterPreview 
                + (numButtons * buttonWidth) + ((numButtons - 1) * gapBetweenButtons)
                + 2 + minusWidth + 2 + plusWidth + rightPadding;
        } else if (drawTools && pSel != null) {
            if (pSel.tipo.startsWith("DESPLEGABLE")) {
                int w = 10; // Padding
                w += 91; // COLORES
                w += 10; // Gap
                w += 50; // BLOQUES
                if (pSel.textoAsociado != null && !pSel.textoAsociado.isEmpty()) {
                    w += 10; // Gap
                    w += 30; // T actions
                    w += 10; // Gap
                    w += 30; // TM actions
                    w += 10; // Gap
                    w += 30; // I actions
                }
                w += 10; // Padding
                return w;
            } else {
                int w = 10;
                w += 20; // Fill
                if (!pSel.tipo.equals("LINEA") && !pSel.tipo.equals("TRIANGULO")) {
                    w += 20; // Border
                }
                if (pSel.textoAsociado != null && !pSel.textoAsociado.isEmpty()) {
                    w += 10;
                    w += 30;
                    w += 10;
                    w += 30;
                }
                w += 10;
                return w;
            }
        }
        return 0;
    }

    private static void renderTextTools(GuiGraphics g, int barX, int y, GlobalGuiSettings.TextConfig tSel) {
        Font font = Minecraft.getInstance().font;
        int curX = barX + 10;
        int row1Y = y + 5;
        int row2Y = y + 22;

        // --- COLUMNA 1: COLOR (abarca ambas filas) ---
        g.fill(curX, row1Y, curX + 15, row2Y + 15, tSel.colorARGB);
        g.renderOutline(curX - 1, row1Y - 1, 17, 32, 0xFF000000);
        curX += 25;

        // --- FILA 1: Funciones ---
        int fX = curX;
        drawToolBtn(g, font, "B", fX, row1Y, tSel.negrita ? 0xFF00FF00 : 0xFFFFFFFF); fX += 20;
        drawToolBtn(g, font, "I", fX, row1Y, tSel.cursiva ? 0xFF00FF00 : 0xFFFFFFFF); fX += 20;
        drawToolBtn(g, font, "aA", fX, row1Y, 0xFFFFFFFF); fX += 20;
        drawToolBtn(g, font, "Sh", fX, row1Y, tSel.sombra ? 0xFF00FF00 : 0xFFFFFFFF); fX += 20;
        drawToolBtn(g, font, "—", fX, row1Y, tSel.subrayado ? 0xFF00FF00 : 0xFFFFFFFF); fX += 20;
        drawToolBtn(g, font, "+", fX, row1Y, 0xFFFFFFFF);

        // --- FILA 2: Funciones ---
        fX = curX;
        drawToolBtn(g, font, "U", fX, row2Y, 0xFFFFFFFF); fX += 20;
        drawToolBtn(g, font, "S", fX, row2Y, 0xFFFFFFFF); fX += 20;
        drawToolBtn(g, font, "-", fX, row2Y, 0xFFFFFFFF); fX += 20;
        drawToolBtn(g, font, "+", fX, row2Y, 0xFFFFFFFF); fX += 20;
        drawToolBtn(g, font, "< >", fX, row2Y, 0xFFFFFFFF); fX += 25;
        drawToolBtn(g, font, "><", fX, row2Y, 0xFFFFFFFF);
    }

    private static void drawToolBtn(GuiGraphics g, Font font, String text, int x, int y, int color) {
        g.fill(x, y, x + 18, y + 16, 0xFF333333);
        g.renderOutline(x, y, 18, 16, 0xFF000000);
        g.drawString(font, text, x + 2, y + 4, color, false);
    }

    private static void drawSectionTitle(GuiGraphics g, Font font, String title, int x, int y, int width) {
        int textW = font.width(title);
        int textX = x + (width - textW) / 2;
        g.drawString(font, title, textX, y, 0xFF444444, false);
        // Draw lines
        g.fill(x, y + 4, textX - 2, y + 5, 0xFF888888);
        g.fill(textX + textW + 2, y + 4, x + width, y + 5, 0xFF888888);
    }

    private static void drawVerticalSeparator(GuiGraphics g, int x, int y, int height) {
        g.fill(x, y, x + 1, y + height, 0xFF888888);
        g.fill(x + 1, y, x + 2, y + height, 0xFFFFFFFF);
    }

    private static void renderDrawingTools(GuiGraphics g, int barX, int y, GlobalGuiSettings.PanelConfig pSel) {
        if (pSel == null) return;
        Font font = Minecraft.getInstance().font;
        int curX = barX + 10;
        int centerY = y + (30 - 11) / 2;

        if (pSel.tipo.startsWith("DESPLEGABLE")) {
            // COLORES Section (Width 91)
            drawSectionTitle(g, font, "COLORES", curX, y + 5, 91);
            int boxY = y + 20;
            
            // 1. Header Fill
            g.fill(curX, boxY, curX + 15, boxY + 15, pSel.colorFondoCabecera);
            g.renderOutline(curX - 1, boxY - 1, 17, 17, 0xFF000000);
            curX += 19;
            
            // 2. Header Border
            g.fill(curX, boxY, curX + 15, boxY + 15, pSel.colorBordeCabecera);
            g.renderOutline(curX - 1, boxY - 1, 17, 17, 0xFF000000);
            curX += 19;
            
            // 3. Body Fill
            g.fill(curX, boxY, curX + 15, boxY + 15, pSel.colorARGB);
            g.renderOutline(curX - 1, boxY - 1, 17, 17, 0xFF000000);
            curX += 19;
            
            // 4. Body Border
            g.fill(curX, boxY, curX + 15, boxY + 15, pSel.colorBorde);
            g.renderOutline(curX - 1, boxY - 1, 17, 17, 0xFF000000);
            curX += 19;
            
            // 5. Text Color
            g.fill(curX, boxY, curX + 15, boxY + 15, pSel.colorTexto);
            g.renderOutline(curX - 1, boxY - 1, 17, 17, 0xFF000000);
            curX += 15; // end of COLORES section

            drawVerticalSeparator(g, curX + 4, y + 4, 37);
            curX += 10; // Gap to next section
            
            // BLOQUES Section (Width 50)
            drawSectionTitle(g, font, "BLOQUES", curX, y + 5, 50);
            
            int bX = curX + (50 - (15*2 + 4))/2; // Center 2 boxes
            
            // 6. Mission Fill
            g.fill(bX, boxY, bX + 15, boxY + 15, pSel.colorFondoMision);
            g.renderOutline(bX - 1, boxY - 1, 17, 17, 0xFF000000);
            bX += 19;
            
            // 7. Mission Border
            g.fill(bX, boxY, bX + 15, boxY + 15, pSel.colorBordeMision);
            g.renderOutline(bX - 1, boxY - 1, 17, 17, 0xFF000000);
            
            curX += 50;

            if (pSel.textoAsociado != null && !pSel.textoAsociado.isEmpty()) {
                drawVerticalSeparator(g, curX + 4, y + 4, 37);
                curX += 10;
                drawSectionTitle(g, font, "T", curX, y + 5, 30);
                curX += 30;

                drawVerticalSeparator(g, curX + 4, y + 4, 37);
                curX += 10;
                drawSectionTitle(g, font, "TM", curX, y + 5, 30);
                curX += 30;

                drawVerticalSeparator(g, curX + 4, y + 4, 37);
                curX += 10;
                drawSectionTitle(g, font, "I", curX, y + 5, 30);
                curX += 30;
            }

        } else {
            g.fill(curX, y + 7, curX + 15, y + 22, pSel.colorARGB);
            g.renderOutline(curX - 1, y + 6, 17, 17, 0xFF000000);
            curX += 20;

            if (!pSel.tipo.equals("LINEA") && !pSel.tipo.equals("TRIANGULO")) {
                g.fill(curX, y + 7, curX + 15, y + 22, pSel.colorBorde);
                g.renderOutline(curX - 1, y + 6, 17, 17, 0xFF000000);
                curX += 20;
            }

            if (pSel.textoAsociado != null && !pSel.textoAsociado.isEmpty()) {
                curX += 10;
                drawSectionTitle(g, font, "T", curX, y + 5, 30);
                curX += 30;

                curX += 10;
                drawSectionTitle(g, font, "I", curX, y + 5, 30);
                curX += 30;
            }
        }
    }

    public static int getDrawingButtonAt(int mx, int my, int guiWidth, int y, GlobalGuiSettings.PanelConfig pSel) {
        if (!drawingToolsVisible || pSel == null || my < y || my > y + getHeight()) return -1;

        int barX = LeftSidebar.getSidebarWidth();
        int barW = getWidth();
        int barStartX = barX + (guiWidth - barX - barW) / 2;
        int curX = barStartX + 10;
        int btnW = 20;

        if (pSel.tipo.startsWith("DESPLEGABLE")) {
            // Group 1: Header Fill
            if (mx >= curX && mx <= curX + 15) return BTN_HEADER_FILL_COLOR;
            curX += 19;
            // Group 1: Header Border
            if (mx >= curX && mx <= curX + 15) return BTN_HEADER_BORDER_COLOR;
            curX += 19;
            
            // Group 2: Body Fill
            if (mx >= curX && mx <= curX + 15) return BTN_FILL_COLOR;
            curX += 19;
            // Group 2: Body Border
            if (mx >= curX && mx <= curX + 15) return BTN_BORDER_COLOR;
            curX += 19;
            
            // Group 3: Text Color
            if (mx >= curX && mx <= curX + 15) return BTN_TEXT_COLOR;
            curX += 15; // End of COLORES section

            curX += 10; // Gap to next section
            
            // BLOQUES Section
            int bX = curX + (50 - (15*2 + 4))/2;
            
            if (mx >= bX && mx <= bX + 15) return BTN_MISSION_FILL_COLOR;
            bX += 19;
            
            if (mx >= bX && mx <= bX + 15) return BTN_MISSION_BORDER_COLOR;
            
        } else {
            if (mx >= curX && mx <= curX + 15) return BTN_FILL_COLOR;
            curX += 20;

            if (!pSel.tipo.equals("LINEA") && !pSel.tipo.equals("TRIANGULO")) {
                if (mx >= curX && mx <= curX + 15) return BTN_BORDER_COLOR;
                curX += 20;
            }
        }

        return -1;
    }

    public static int getTextButtonAt(int mx, int my, int guiWidth, int y, GlobalGuiSettings.TextConfig tSel) {
        if (!textToolsVisible || tSel == null || my < y || my > y + getHeight()) return -1;
        var btnInfos = getTextButtonInfos(guiWidth, tSel);
        int[] btnIds = {BTN_B, BTN_I, BTN_U, BTN_S, BTN_CASE, BTN_SPACING};
        for (int i = 0; i < Math.min(btnInfos.size(), btnIds.length); i++) {
            var info = btnInfos.get(i);
            if (mx >= info.x() && mx <= info.x() + info.width()) {
                return btnIds[i];
            }
        }
        return -1;
    }


}