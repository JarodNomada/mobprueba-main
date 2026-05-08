package questgrupo.questmod.client.editor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import questgrupo.questmod.client.GlobalGuiSettings;
import java.util.function.Consumer;
import java.lang.Math;
import java.util.ArrayList;
import java.util.List;

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

    public static record ButtonInfo(int x, int width, String text) {}

    public static void setVisible(boolean v) { visible = v; }
    public static boolean isVisible() { return visible; }

    public static int getHeight() {
        if (!visible) return 0;
        if (textToolsVisible) return 45;
        if (drawingToolsVisible) return 45;
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
            int w = 10; // Padding
            if (esMisionTexto) {
                w += 12 * 3 + 2 * 2; // 3 colores en triángulo (recuadro, borde, texto)
            } else {
                w += 12; // Color swatch
            }
            w += 3;  // Gap
            w += 5 * 12 + 4 * 2; // 5 buttons × 12px + 4 gaps × 2px
            w += 10; // Padding
            return w;
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

private static void renderTextTools(GuiGraphics g, int barX, int y, GlobalGuiSettings.TextConfig tSel, GlobalGuiSettings.PanelConfig pSel) {
        int curX = barX + 10;
        int rowY = y + 15;

        boolean esMisionTexto = pSel != null && (pSel.tipo.equals("MISION_TITULO") || pSel.tipo.equals("MISION_DESCRIPCION"));

        if (esMisionTexto) {
            // Calcular ancho de los botones para centrar el triángulo
            int buttonsWidth = 5 * 12 + 4 * 2; // 5 columnas de botones + espacios
            int colorsWidth = 12 * 3 + 2 * 2; // 3 colores + 2 espacios
            int startOffset = (buttonsWidth - colorsWidth) / 2;
            int triStartX = barX + 10 + startOffset;

            // Fila 1: [recuadro] [borde]
            g.fill(triStartX, rowY, triStartX + 12, rowY + 12, pSel.colorARGB);
            g.renderOutline(triStartX - 1, rowY - 1, 14, 14, 0xFF000000);

            g.fill(triStartX + 14, rowY, triStartX + 14 + 12, rowY + 12, pSel.colorBorde);
            g.renderOutline(triStartX + 14 - 1, rowY - 1, 14, 14, 0xFF000000);

            // Fila 2: [texto] centrado debajo
            int rowY2 = y + 15 + 12 + 2;
            int textX = triStartX + 14 + 2; // Debajo del centro de los dos colores
            g.fill(textX, rowY2, textX + 12, rowY2 + 12, pSel.colorTexto);
            g.renderOutline(textX - 1, rowY2 - 1, 14, 14, 0xFF000000);
        } else {
            // Un solo color para texto libre
g.fill(curX, rowY, curX + 12, rowY + 12, tSel.colorARGB);
            g.renderOutline(curX - 1, rowY - 1, 14, 14, 0xFF000000);
        }
    }

    public static int getColorClick(int mx, int my, int guiWidth, int y, GlobalGuiSettings.TextConfig tSel, GlobalGuiSettings.PanelConfig pSel) {
        if (!isVisible()) return -1;

        int barX = LeftSidebar.getSidebarWidth();
        int barW = getWidth();
        int barStartX = barX + (guiWidth - barX - barW) / 2;

        boolean esMisionTexto = pSel != null && (pSel.tipo.equals("MISION_TITULO") || pSel.tipo.equals("MISION_DESCRIPCION"));

        if (esMisionTexto) {
            int rowY = y + 15;
            int rowY2 = y + 15 + 12 + 2;

            // Calcular mismas posiciones que en renderTextTools
            int buttonsWidth = 5 * 12 + 4 * 2;
            int colorsWidth = 12 * 3 + 2 * 2;
            int startOffset = (buttonsWidth - colorsWidth) / 2;
            int triStartX = barStartX + 10 + startOffset;

            // Recuadro (colorARGB)
            if (mx >= triStartX && mx <= triStartX + 12 && my >= rowY && my <= rowY + 12) {
                return 7;
            }
            // Borde (colorBorde)
            if (mx >= triStartX + 14 && mx <= triStartX + 14 + 12 && my >= rowY && my <= rowY + 12) {
                return 8;
            }
            // Texto (colorTexto)
            int textX = triStartX + 14 + 2;
            if (mx >= textX && mx <= textX + 12 && my >= rowY2 && my <= rowY2 + 12) {
                return 9;
            }
        } else if (tSel != null) {
            // Un solo color para texto libre
            int rowY = y + 15;
            if (mx >= barStartX + 10 && mx <= barStartX + 10 + 12 && my >= rowY && my <= rowY + 12) {
                return 0; // colorARGB del TextConfig
            }
        }
        return -1;
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

    public static void inicializarBotonesMision(int guiWidth, int y, Consumer<Button> adder, GlobalGuiSettings.PanelConfig pSel) {
        if (pSel == null || pSel.textoAsociado == null || pSel.textoAsociado.isEmpty()) return;

        int barX = LeftSidebar.getSidebarWidth();
        int barW = getWidth();
        int barStartX = barX + (guiWidth - barX - barW) / 2;
        int btnY = y + 22;

        // Calcular posiciones de la misma manera que renderDrawingTools
        int curX = barStartX + 10 + 91 + 10 + 50 + 10; // Después de COLORES y BLOQUES

        if (!pSel.tipo.startsWith("DESPLEGABLE")) {
            // Para no-DESPLEGABLE: T e I
            int tSectionX = curX;
            curX += 30 + 10; // T + gap

            Button btnTLeft = Button.builder(Component.literal("←"), b -> pSel.offsetXTexto -= 2.0f).bounds(tSectionX, btnY, 12, 12).build();
            Button btnTRight = Button.builder(Component.literal("→"), b -> pSel.offsetXTexto += 2.0f).bounds(tSectionX + 14, btnY, 12, 12).build();
            Button btnTMinus = Button.builder(Component.literal("-"), b -> pSel.escalaTexto = Math.max(0.1f, pSel.escalaTexto - 0.1f)).bounds(tSectionX, btnY + 14, 12, 12).build();
            Button btnTPlus = Button.builder(Component.literal("+"), b -> pSel.escalaTexto = Math.min(10.0f, pSel.escalaTexto + 0.1f)).bounds(tSectionX + 14, btnY + 14, 12, 12).build();
            adder.accept(btnTLeft); adder.accept(btnTRight); adder.accept(btnTMinus); adder.accept(btnTPlus);

            int iSectionX = curX + 30;
            Button btnILeft = Button.builder(Component.literal("←"), b -> pSel.offsetXIcono -= 2.0f).bounds(iSectionX, btnY, 12, 12).build();
            Button btnIRight = Button.builder(Component.literal("→"), b -> pSel.offsetXIcono += 2.0f).bounds(iSectionX + 14, btnY, 12, 12).build();
            Button btnIMinus = Button.builder(Component.literal("-"), b -> pSel.escalaIcono = Math.max(0.1f, pSel.escalaIcono - 0.1f)).bounds(iSectionX, btnY + 14, 12, 12).build();
            Button btnIPlus = Button.builder(Component.literal("+"), b -> pSel.escalaIcono = Math.min(10.0f, pSel.escalaIcono + 0.1f)).bounds(iSectionX + 14, btnY + 14, 12, 12).build();
            adder.accept(btnILeft); adder.accept(btnIRight); adder.accept(btnIMinus); adder.accept(btnIPlus);
        } else {
            // Para DESPLEGABLE: T, TM, I
            int tSectionX = curX;

            Button btnTLeft = Button.builder(Component.literal("←"), b -> pSel.offsetXTexto -= 2.0f).bounds(tSectionX, btnY, 12, 12).build();
            Button btnTRight = Button.builder(Component.literal("→"), b -> pSel.offsetXTexto += 2.0f).bounds(tSectionX + 14, btnY, 12, 12).build();
            Button btnTMinus = Button.builder(Component.literal("-"), b -> pSel.escalaTexto = Math.max(0.1f, pSel.escalaTexto - 0.1f)).bounds(tSectionX, btnY + 14, 12, 12).build();
            Button btnTPlus = Button.builder(Component.literal("+"), b -> pSel.escalaTexto = Math.min(10.0f, pSel.escalaTexto + 0.1f)).bounds(tSectionX + 14, btnY + 14, 12, 12).build();
            adder.accept(btnTLeft); adder.accept(btnTRight); adder.accept(btnTMinus); adder.accept(btnTPlus);

            curX += 30 + 10; // T + gap

            int tmSectionX = curX;
            Button btnTMLeft = Button.builder(Component.literal("←"), b -> pSel.offsetXTextoMision -= 2.0f).bounds(tmSectionX, btnY, 12, 12).build();
            Button btnTMRight = Button.builder(Component.literal("→"), b -> pSel.offsetXTextoMision += 2.0f).bounds(tmSectionX + 14, btnY, 12, 12).build();
            Button btnTMMinus = Button.builder(Component.literal("-"), b -> pSel.escalaTextoMision = Math.max(0.1f, pSel.escalaTextoMision - 0.1f)).bounds(tmSectionX, btnY + 14, 12, 12).build();
            Button btnTMPlus = Button.builder(Component.literal("+"), b -> pSel.escalaTextoMision = Math.min(10.0f, pSel.escalaTextoMision + 0.1f)).bounds(tmSectionX + 14, btnY + 14, 12, 12).build();
            adder.accept(btnTMLeft); adder.accept(btnTMRight); adder.accept(btnTMMinus); adder.accept(btnTMPlus);

            curX += 30 + 10; // TM + gap

            int iSectionX = curX;
            Button btnILeft = Button.builder(Component.literal("←"), b -> pSel.offsetXIcono -= 2.0f).bounds(iSectionX, btnY, 12, 12).build();
            Button btnIRight = Button.builder(Component.literal("→"), b -> pSel.offsetXIcono += 2.0f).bounds(iSectionX + 14, btnY, 12, 12).build();
            Button btnIMinus = Button.builder(Component.literal("-"), b -> pSel.escalaIcono = Math.max(0.1f, pSel.escalaIcono - 0.1f)).bounds(iSectionX, btnY + 14, 12, 12).build();
            Button btnIPlus = Button.builder(Component.literal("+"), b -> pSel.escalaIcono = Math.min(10.0f, pSel.escalaIcono + 0.1f)).bounds(iSectionX + 14, btnY + 14, 12, 12).build();
            adder.accept(btnILeft); adder.accept(btnIRight); adder.accept(btnIMinus); adder.accept(btnIPlus);
        }
    }
}
