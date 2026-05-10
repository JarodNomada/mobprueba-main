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

    // --- CORRECCIÓN: Contenedor dibujado píxel por píxel para evitar huecos en la escala de GUI ---
    private static void drawContainer(GuiGraphics g, int x, int y, int width, int height) {
        // 1. Fondo principal gris (Ocupa el interior exacto)
        g.fill(x + 1, y + 1, x + width - 1, y + height - 1, 0xFFC6C6C6);

        // 2. Borde negro perfecto de 1 píxel dibujado manualmente (Arriba, Abajo, Izq, Der)
        g.fill(x, y, x + width, y + 1, 0xFF000000);
        g.fill(x, y + height - 1, x + width, y + height, 0xFF000000);
        g.fill(x, y + 1, x + 1, y + height - 1, 0xFF000000);
        g.fill(x + width - 1, y + 1, x + width, y + height - 1, 0xFF000000);

        // 3. Brillo blanco interno
        g.fill(x + 1, y + 1, x + width - 2, y + 2, 0xFFFFFFFF); // Arriba
        g.fill(x + 1, y + 2, x + 2, y + height - 2, 0xFFFFFFFF); // Izquierda

        // 4. Sombra oscura interna
        g.fill(x + 1, y + height - 2, x + width - 1, y + height - 1, 0xFF555555); // Abajo
        g.fill(x + width - 2, y + 1, x + width - 1, y + height - 2, 0xFF555555); // Derecha
    }

    public static void render(GuiGraphics g, int guiWidth, int y, GlobalGuiSettings.TextConfig tSel, GlobalGuiSettings.PanelConfig pSel) {
        int barHeightLogico = getHeight();
        if (barHeightLogico == 0) return;

        int barX = LeftSidebar.getSidebarWidth();
        barWidth = calculateBarWidth(textToolsVisible, drawingToolsVisible, tSel, pSel);
        int barStartX = barX + (guiWidth - barX - barWidth) / 2;

int visualBoxHeight = 48;
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
            if (esMisionTexto) {
                w += 38; // 2 swatches en fila 1: (16+2) + 2 + (16+2) = 38px
            } else {
                w += 18; // 1 swatch de 18px
            }
            w += 2;  // Gap de 2px antes de los botones
            w += (5 * 18) + (4 * 2); // Botones: 5 × 18px + 4 × 2px = 98px
            w += 4;
            return w;
        } else if (drawTools && pSel != null) {
            if (pSel.tipo.startsWith("DESPLEGABLE")) {
                int w = 10;
                w += 91;
                w += 10;
                if (pSel.textoAsociado != null && !pSel.textoAsociado.isEmpty()) {
                    w += 30; w += 10; w += 30; w += 10; w += 30;
                }
                w += 10;
                return w;
            } else {
                int w = 10;
                w += 20;
                if (!pSel.tipo.equals("LINEA") && !pSel.tipo.equals("TRIANGULO")) {
                    w += 20;
                }
                if (pSel.textoAsociado != null && !pSel.textoAsociado.isEmpty()) {
                    w += 10; w += 30; w += 10; w += 30;
                }
                w += 10;
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
            int gap = 2;

            g.fill(startX + 1, rowY + 1, startX + 1 + swatchSize, rowY + 1 + swatchSize, pSel.colorARGB);
            g.renderOutline(startX, rowY, swatchSize + 2, swatchSize + 2, 0xFF000000);

            int swatch2X = startX + swatchSize + 2 + gap;
            g.fill(swatch2X + 1, rowY + 1, swatch2X + 1 + swatchSize, rowY + 1 + swatchSize, pSel.colorBorde);
            g.renderOutline(swatch2X, rowY, swatchSize + 2, swatchSize + 2, 0xFF000000);

            int rowY2 = rowY + swatchSize + 2 + gap;
            g.fill(startX + 1, rowY2 + 1, startX + 1 + swatchSize, rowY2 + 1 + swatchSize, pSel.colorTexto);
            g.renderOutline(startX, rowY2, swatchSize + 2, swatchSize + 2, 0xFF000000);
        } else {
            g.fill(startX + 1, rowY + 1, startX + 1 + swatchSize, rowY + 1 + swatchSize, tSel.colorARGB);
            g.renderOutline(startX, rowY, swatchSize + 2, swatchSize + 2, 0xFF000000);
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
        int gap = 2;

        if (esMisionTexto) {
            int swatch2X = startX + swatchSize + 2 + gap;
            int rowY2 = rowY + swatchSize + 2 + gap;

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

    private static void renderDrawingTools(GuiGraphics g, int barX, int y, GlobalGuiSettings.PanelConfig pSel) {
        if (pSel == null) return;
        Font font = Minecraft.getInstance().font;
        int curX = barX + 4;
        int boxY = y + 4;
        int boxY2 = y + 22;

        if (pSel.tipo.startsWith("DESPLEGABLE")) {
            drawSectionTitle(g, font, "COLORES", curX, y + 5, 91);

            // Fila 1: 4 swatches
            g.fill(curX, boxY, curX + 15, boxY + 15, pSel.colorFondoCabecera);
            g.renderOutline(curX - 1, boxY - 1, 17, 17, 0xFF000000);
            curX += 19;

            g.fill(curX, boxY, curX + 15, boxY + 15, pSel.colorBordeCabecera);
            g.renderOutline(curX - 1, boxY - 1, 17, 17, 0xFF000000);
            curX += 19;

            g.fill(curX, boxY, curX + 15, boxY + 15, pSel.colorARGB);
            g.renderOutline(curX - 1, boxY - 1, 17, 17, 0xFF000000);
            curX += 19;

            g.fill(curX, boxY, curX + 15, boxY + 15, pSel.colorBorde);
            g.renderOutline(curX - 1, boxY - 1, 17, 17, 0xFF000000);
            curX += 19;

            // Fila 2: 3 swatches (incluyendo los 2 de BLOQUES)
            g.fill(curX, boxY2, curX + 15, boxY2 + 15, pSel.colorTexto);
            g.renderOutline(curX - 1, boxY2 - 1, 17, 17, 0xFF000000);
            curX += 19;

            g.fill(curX, boxY2, curX + 15, boxY2 + 15, pSel.colorFondoMision);
            g.renderOutline(curX - 1, boxY2 - 1, 17, 17, 0xFF000000);
            curX += 19;

            g.fill(curX, boxY2, curX + 15, boxY2 + 15, pSel.colorBordeMision);
            g.renderOutline(curX - 1, boxY2 - 1, 17, 17, 0xFF000000);
            curX = barX + 95; // Reiniciar posición para T, TM, I

            // Botones T, TM, I a la derecha
            if (pSel.textoAsociado != null && !pSel.textoAsociado.isEmpty()) {
                drawVerticalSeparator(g, curX + 2, y + 4, 36);
                curX += 4;
                drawSectionTitle(g, font, "T", curX, y + 5, 30);
                curX += 30;

                drawVerticalSeparator(g, curX + 2, y + 4, 36);
                curX += 4;
                drawSectionTitle(g, font, "TM", curX, y + 5, 30);
                curX += 30;

                drawVerticalSeparator(g, curX + 2, y + 4, 36);
                curX += 4;
                drawSectionTitle(g, font, "I", curX, y + 5, 30);
                curX += 30;
            }

        } else {
            g.fill(curX, boxY, curX + 15, boxY + 15, pSel.colorARGB);
            g.renderOutline(curX - 1, boxY - 1, 17, 17, 0xFF000000);
            curX += 20;

            if (!pSel.tipo.equals("LINEA") && !pSel.tipo.equals("TRIANGULO")) {
                g.fill(curX, boxY, curX + 15, boxY + 15, pSel.colorBorde);
                g.renderOutline(curX - 1, boxY - 1, 17, 17, 0xFF000000);
                curX += 20;
            }

            if (pSel.textoAsociado != null && !pSel.textoAsociado.isEmpty()) {
                curX += 4;
                drawSectionTitle(g, font, "T", curX, y + 5, 30);
                curX += 30;

                curX += 4;
                drawSectionTitle(g, font, "I", curX, y + 5, 30);
                curX += 30;
            }
        }
    }

    public static int getDrawingButtonAt(int mx, int my, int guiWidth, int y, GlobalGuiSettings.PanelConfig pSel) {
        if (!drawingToolsVisible || pSel == null || my < y || my > y + getHeight()) return -1;

        int barX = LeftSidebar.getSidebarWidth();
        int barW = calculateBarWidth(false, true, null, pSel);
        barWidth = barW;
        int barStartX = barX + (guiWidth - barX - barW) / 2;
        int curX = barStartX + 10;

        if (pSel.tipo.startsWith("DESPLEGABLE")) {
            if (mx >= curX && mx <= curX + 15) return BTN_HEADER_FILL_COLOR;
            curX += 19;
            if (mx >= curX && mx <= curX + 15) return BTN_HEADER_BORDER_COLOR;
            curX += 19;

            if (mx >= curX && mx <= curX + 15) return BTN_FILL_COLOR;
            curX += 19;
            if (mx >= curX && mx <= curX + 15) return BTN_BORDER_COLOR;
            curX += 19;

            if (mx >= curX && mx <= curX + 15) return BTN_TEXT_COLOR;
            curX += 15;

            curX += 10;

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

        int expectedBarW;
        if (pSel.tipo.startsWith("DESPLEGABLE")) {
            expectedBarW = 10 + 91 + 10 + 30 + 10 + 30 + 10 + 30 + 10;
        } else {
            expectedBarW = 10 + 20 + (pSel.tipo.equals("LINEA") || pSel.tipo.equals("TRIANGULO") ? 0 : 20) + 10 + 30 + 10 + 30 + 10;
        }
        int barStartX = barX + (guiWidth - barX - expectedBarW) / 2;
        int btnY = y + 4;
        int btnSize = 18;
        int btnY2 = y + 22;

        // Posición de botones debajo de T, TM, I (después de 4 swatches + gap)
        int curX = barX + 95;

        if (!pSel.tipo.startsWith("DESPLEGABLE")) {
            int tSectionX = curX + 6;
            curX += 38;

            Button btnTLeft = Button.builder(Component.literal("←"), b -> pSel.offsetXTexto -= 2.0f).bounds(tSectionX, btnY, btnSize, btnSize).build();
            Button btnTRight = Button.builder(Component.literal("→"), b -> pSel.offsetXTexto += 2.0f).bounds(tSectionX + btnSize + 2, btnY, btnSize, btnSize).build();
            Button btnTMinus = Button.builder(Component.literal("-"), b -> pSel.escalaTexto = Math.max(0.1f, pSel.escalaTexto - 0.1f)).bounds(tSectionX, btnY2, btnSize, btnSize).build();
            Button btnTPlus = Button.builder(Component.literal("+"), b -> pSel.escalaTexto = Math.min(10.0f, pSel.escalaTexto + 0.1f)).bounds(tSectionX + btnSize + 2, btnY2, btnSize, btnSize).build();
            adder.accept(btnTLeft); adder.accept(btnTRight); adder.accept(btnTMinus); adder.accept(btnTPlus);

            int iSectionX = curX + 38 + 6;
            Button btnILeft = Button.builder(Component.literal("←"), b -> pSel.offsetXIcono -= 2.0f).bounds(iSectionX, btnY, btnSize, btnSize).build();
            Button btnIRight = Button.builder(Component.literal("→"), b -> pSel.offsetXIcono += 2.0f).bounds(iSectionX + btnSize + 2, btnY, btnSize, btnSize).build();
            Button btnIMinus = Button.builder(Component.literal("-"), b -> pSel.escalaIcono = Math.max(0.1f, pSel.escalaIcono - 0.1f)).bounds(iSectionX, btnY2, btnSize, btnSize).build();
            Button btnIPlus = Button.builder(Component.literal("+"), b -> pSel.escalaIcono = Math.min(10.0f, pSel.escalaIcono + 0.1f)).bounds(iSectionX + btnSize + 2, btnY2, btnSize, btnSize).build();
            adder.accept(btnILeft); adder.accept(btnIRight); adder.accept(btnIMinus); adder.accept(btnIPlus);
        } else {
            int tSectionX = curX + 6;

            Button btnTLeft = Button.builder(Component.literal("←"), b -> pSel.offsetXTexto -= 2.0f).bounds(tSectionX, btnY, btnSize, btnSize).build();
            Button btnTRight = Button.builder(Component.literal("→"), b -> pSel.offsetXTexto += 2.0f).bounds(tSectionX + btnSize + 2, btnY, btnSize, btnSize).build();
            Button btnTMinus = Button.builder(Component.literal("-"), b -> pSel.escalaTexto = Math.max(0.1f, pSel.escalaTexto - 0.1f)).bounds(tSectionX, btnY2, btnSize, btnSize).build();
            Button btnTPlus = Button.builder(Component.literal("+"), b -> pSel.escalaTexto = Math.min(10.0f, pSel.escalaTexto + 0.1f)).bounds(tSectionX + btnSize + 2, btnY2, btnSize, btnSize).build();
            adder.accept(btnTLeft); adder.accept(btnTRight); adder.accept(btnTMinus); adder.accept(btnTPlus);

            curX += 38;

            int tmSectionX = curX + 6;
            Button btnTMLeft = Button.builder(Component.literal("←"), b -> pSel.offsetXTextoMision -= 2.0f).bounds(tmSectionX, btnY, btnSize, btnSize).build();
            Button btnTMRight = Button.builder(Component.literal("→"), b -> pSel.offsetXTextoMision += 2.0f).bounds(tmSectionX + btnSize + 2, btnY, btnSize, btnSize).build();
            Button btnTMMinus = Button.builder(Component.literal("-"), b -> pSel.escalaTextoMision = Math.max(0.1f, pSel.escalaTextoMision - 0.1f)).bounds(tmSectionX, btnY2, btnSize, btnSize).build();
            Button btnTMPlus = Button.builder(Component.literal("+"), b -> pSel.escalaTextoMision = Math.min(10.0f, pSel.escalaTextoMision + 0.1f)).bounds(tmSectionX + btnSize + 2, btnY2, btnSize, btnSize).build();
            adder.accept(btnTMLeft); adder.accept(btnTMRight); adder.accept(btnTMMinus); adder.accept(btnTMPlus);

            curX += 38;

            int iSectionX = curX + 6;
            Button btnILeft = Button.builder(Component.literal("←"), b -> pSel.offsetXIcono -= 2.0f).bounds(iSectionX, btnY, btnSize, btnSize).build();
            Button btnIRight = Button.builder(Component.literal("→"), b -> pSel.offsetXIcono += 2.0f).bounds(iSectionX + btnSize + 2, btnY, btnSize, btnSize).build();
            Button btnIMinus = Button.builder(Component.literal("-"), b -> pSel.escalaIcono = Math.max(0.1f, pSel.escalaIcono - 0.1f)).bounds(iSectionX, btnY2, btnSize, btnSize).build();
            Button btnIPlus = Button.builder(Component.literal("+"), b -> pSel.escalaIcono = Math.min(10.0f, pSel.escalaIcono + 0.1f)).bounds(iSectionX + btnSize + 2, btnY2, btnSize, btnSize).build();
            adder.accept(btnILeft); adder.accept(btnIRight); adder.accept(btnIMinus); adder.accept(btnIPlus);
        }
    }
}