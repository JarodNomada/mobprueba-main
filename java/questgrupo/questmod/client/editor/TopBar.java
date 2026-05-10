package questgrupo.questmod.client.editor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import questgrupo.questmod.client.GlobalGuiSettings;
import java.util.function.Consumer;

public class TopBar {
    private static boolean visible = false;
    private static boolean textToolsVisible = false;
    private static boolean drawingToolsVisible = false;
    private static int barWidth = 0;

    public static final int BTN_FILL_COLOR = 10, BTN_BORDER_COLOR = 11, BTN_TEXT_COLOR = 20;
    public static final int BTN_MISSION_FILL_COLOR = 21, BTN_MISSION_BORDER_COLOR = 22;
    public static final int BTN_HEADER_FILL_COLOR = 30, BTN_HEADER_BORDER_COLOR = 31;

    public static final int BTN_MINUS_TEXT_SCALE = 40, BTN_PLUS_TEXT_SCALE = 41;
    public static final int BTN_MINUS_ICON_SCALE = 42, BTN_PLUS_ICON_SCALE = 43;
    public static final int BTN_MOVE_TEXT_LEFT = 44, BTN_MOVE_TEXT_RIGHT = 45;
    public static final int BTN_MOVE_TEXT_UP = 46, BTN_MOVE_TEXT_DOWN = 47;
    public static final int BTN_MOVE_MISSION_TEXT_UP = 48, BTN_MOVE_MISSION_TEXT_DOWN = 49;
    public static final int BTN_MOVE_ICON_LEFT = 50, BTN_MOVE_ICON_RIGHT = 51;
    public static final int BTN_MOVE_ICON_UP = 52, BTN_MOVE_ICON_DOWN = 53;

    // --- ESTADO DEL SELECTOR DE COLOR MODAL ---
    public static boolean colorPickerVisible = false;
    private static int originalColorPacked = 0xFFFFFFFF; // Copia de seguridad si cancela
    private static int editingColorPacked = 0xFFFFFFFF; 
    private static float currentOpacity = 1.0f;
    private static int currentEditingID = -1;
    private static boolean isDraggingSlider = false;
    
    private static GlobalGuiSettings.PanelConfig panelEnEdicion = null;
    private static GlobalGuiSettings.TextConfig textoEnEdicion = null;

    // Dimensiones y posiciones
    private static final int MODAL_W = 160;
    private static final int MODAL_H = 120;
    private static final int SLIDER_W = 140;
    private static int modalX = 0;
    private static int modalY = 0;

    // --- COMPONENTES NATIVOS ---
    private static EditBox hexInput;
    private static Button btnAccept;
    private static Button btnCancel;

    public static void setVisible(boolean v) { visible = v; }
    public static boolean isVisible() { return visible; }

    public static int getHeight() {
        if (!visible) return 0;
        return 48; 
    }

    public static int getWidth() { return barWidth; }

    public static void initColorPickerWidgets(int guiWidth, int y, Font font, Consumer<AbstractWidget> adder) {
        modalX = (guiWidth - MODAL_W) / 2;
        modalY = y + 55;

        hexInput = new EditBox(font, modalX + 32, modalY + 31, 80, 16, Component.literal("Hex"));
        hexInput.setMaxLength(6); 
        hexInput.setResponder(TopBar::onHexChanged);
        hexInput.visible = colorPickerVisible; 
        adder.accept(hexInput);

        btnAccept = Button.builder(Component.literal("Aceptar"), b -> closePicker(true))
                .bounds(modalX + 10, modalY + 92, 65, 20).build();
        btnAccept.visible = colorPickerVisible;
        adder.accept(btnAccept);

        btnCancel = Button.builder(Component.literal("Cancelar"), b -> closePicker(false))
                .bounds(modalX + 85, modalY + 92, 65, 20).build();
        btnCancel.visible = colorPickerVisible;
        adder.accept(btnCancel);
    }

    private static void onHexChanged(String text) {
        try {
            if (text.isEmpty()) return;
            int rgb = Integer.parseInt(text, 16);
            editingColorPacked = (editingColorPacked & 0xFF000000) | (rgb & 0x00FFFFFF);
            applyFinalColor(); // Aplica el cambio mientras escribes
        } catch (NumberFormatException ignored) {} 
    }

    public static int getColorByID(int id, GlobalGuiSettings.PanelConfig pSel, GlobalGuiSettings.TextConfig tSel) {
        if (pSel != null) {
            switch(id) {
                case BTN_FILL_COLOR: return pSel.colorARGB;
                case BTN_BORDER_COLOR: return pSel.colorBorde;
                case BTN_TEXT_COLOR: return pSel.colorTexto;
                case BTN_MISSION_FILL_COLOR: return pSel.colorFondoMision;
                case BTN_MISSION_BORDER_COLOR: return pSel.colorBordeMision;
                case BTN_HEADER_FILL_COLOR: return pSel.colorFondoCabecera;
                case BTN_HEADER_BORDER_COLOR: return pSel.colorBordeCabecera;
            }
        } else if (tSel != null) {
            if (id == BTN_TEXT_COLOR) return tSel.colorARGB;
        }
        return 0xFFFFFFFF;
    }

    public static void openPicker(int color, int id, GlobalGuiSettings.PanelConfig pSel, GlobalGuiSettings.TextConfig tSel) {
        originalColorPacked = color;
        editingColorPacked = color;
        currentEditingID = id;
        currentOpacity = ((color >> 24) & 0xFF) / 255.0f;
        panelEnEdicion = pSel;
        textoEnEdicion = tSel;
        colorPickerVisible = true;

        if (hexInput != null) {
            hexInput.setValue(String.format("%06X", editingColorPacked & 0x00FFFFFF));
            hexInput.visible = true;
            hexInput.setFocused(false);
        }
        if (btnAccept != null) btnAccept.visible = true;
        if (btnCancel != null) btnCancel.visible = true;
    }

    public static void closePicker(boolean apply) {
        if (!apply) {
            // Si cancela, volvemos a la normalidad
            editingColorPacked = originalColorPacked;
            currentOpacity = ((originalColorPacked >> 24) & 0xFF) / 255.0f;
            applyFinalColor(); 
        }

        colorPickerVisible = false;
        isDraggingSlider = false;
        
        if (hexInput != null) {
            hexInput.visible = false;
            hexInput.setFocused(false);
        }
        if (btnAccept != null) btnAccept.visible = false;
        if (btnCancel != null) btnCancel.visible = false;
        panelEnEdicion = null;
        textoEnEdicion = null;
    }

    private static void applyFinalColor() {
        int finalColor = (Math.round(currentOpacity * 255.0f) << 24) | (editingColorPacked & 0x00FFFFFF);
        
        if (panelEnEdicion != null) {
            switch (currentEditingID) {
                case BTN_FILL_COLOR: panelEnEdicion.colorARGB = finalColor; break;
                case BTN_BORDER_COLOR: panelEnEdicion.colorBorde = finalColor; break;
                case BTN_TEXT_COLOR: panelEnEdicion.colorTexto = finalColor; break;
                case BTN_MISSION_FILL_COLOR: panelEnEdicion.colorFondoMision = finalColor; break;
                case BTN_MISSION_BORDER_COLOR: panelEnEdicion.colorBordeMision = finalColor; break;
                case BTN_HEADER_FILL_COLOR: panelEnEdicion.colorFondoCabecera = finalColor; break;
                case BTN_HEADER_BORDER_COLOR: panelEnEdicion.colorBordeCabecera = finalColor; break;
            }
        } else if (textoEnEdicion != null && currentEditingID == BTN_TEXT_COLOR) {
            textoEnEdicion.colorARGB = finalColor;
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
        int visualBoxHeight = getHeight(); 
        
        drawContainer(g, barStartX, y, barWidth, visualBoxHeight);

        boolean esMisionTexto = pSel != null && (pSel.tipo.equals("MISION_TITULO") || pSel.tipo.equals("MISION_DESCRIPCION"));

        if ((textToolsVisible && tSel != null) || esMisionTexto) {
            renderTextTools(g, barStartX, y, tSel, pSel);
        } else if (drawingToolsVisible) {
            renderDrawingTools(g, barStartX, y, pSel);
        }

        if (colorPickerVisible) {
            renderModal(g);
        }
    }

    private static void renderModal(GuiGraphics g) {
        Font font = Minecraft.getInstance().font;
        drawContainer(g, modalX, modalY, MODAL_W, MODAL_H);

        int tW = font.width("SELECTOR DE COLOR");
        g.drawString(font, "SELECTOR DE COLOR", modalX + (MODAL_W - tW) / 2, modalY + 6, 0xFF000000, false);
        g.fill(modalX + 6, modalY + 16, modalX + MODAL_W - 6, modalY + 17, 0xFF000000); 

        g.drawString(font, "CODIGO DE COLOR", modalX + 8, modalY + 22, 0xFF000000, false);
        
        int pX = modalX + 10;
        int pY = modalY + 31;
        g.fill(pX, pY, pX + 8, pY + 8, 0xFFFFFFFF);
        g.fill(pX + 8, pY, pX + 16, pY + 8, 0xFFAAAAAA);
        g.fill(pX, pY + 8, pX + 8, pY + 16, 0xFFAAAAAA);
        g.fill(pX + 8, pY + 8, pX + 16, pY + 16, 0xFFFFFFFF);

        int colorPreview = (Math.round(currentOpacity * 255.0f) << 24) | (editingColorPacked & 0x00FFFFFF);
        g.fill(pX, pY, pX + 16, pY + 16, colorPreview);
        g.renderOutline(pX - 1, pY - 1, 18, 18, 0xFF000000);

        g.fill(modalX + 6, modalY + 54, modalX + MODAL_W - 6, modalY + 55, 0xFFAAAAAA);

        g.drawString(font, "OPACIDAD", modalX + 8, modalY + 60, 0xFF000000, false);
        
        int sX = modalX + 10;
        int sY = modalY + 72;
        g.fill(sX, sY, sX + SLIDER_W, sY + 6, 0xFF888888); 
        g.renderOutline(sX - 1, sY - 1, SLIDER_W + 2, 8, 0xFF000000);
        
        int knobX = sX + (int)(currentOpacity * (SLIDER_W - 8));
        g.fill(knobX, sY - 2, knobX + 8, sY + 8, 0xFF555555); 
        g.renderOutline(knobX, sY - 2, 8, 10, 0xFF000000);
        
        g.drawString(font, Math.round(currentOpacity * 100) + "%", sX + SLIDER_W - 6, sY - 10, 0xFF333333, false);

        g.fill(modalX + 6, modalY + 86, modalX + MODAL_W - 6, modalY + 87, 0xFFAAAAAA);
    }

    public static boolean handleModalClick(double mx, double my) {
        if (!colorPickerVisible) return false;

        // Slider
        if (mx >= modalX + 10 && mx <= modalX + 10 + SLIDER_W && my >= modalY + 70 && my <= modalY + 80) {
            isDraggingSlider = true;
            updateOpacity(mx, modalX + 10);
            if (hexInput != null) hexInput.setFocused(false);
            return true;
        }

        // Si hacemos clic en el fondo del modal (no en un widget), consumimos el clic y quitamos el cursor
        if (mx >= modalX && mx <= modalX + MODAL_W && my >= modalY && my <= modalY + MODAL_H) {
            if (hexInput != null) hexInput.setFocused(false);
            return true;
        }

        return false;
    }

    public static void handleModalDrag(double mx) {
        if (colorPickerVisible && isDraggingSlider) {
            updateOpacity(mx, modalX + 10);
        }
    }

    public static void stopDragging() { isDraggingSlider = false; }

    private static void updateOpacity(double mx, int startX) {
        float rel = (float)(mx - startX) / (float)(SLIDER_W - 8);
        currentOpacity = Math.max(0.0f, Math.min(1.0f, rel));
        applyFinalColor(); 
    }

    public static void setTextToolsVisible(boolean v) {
        textToolsVisible = v;
        drawingToolsVisible = false;
    }
    public static boolean isTextToolsVisible() { return textToolsVisible; }
    public static boolean isDrawingToolsVisible() { return drawingToolsVisible; }
    public static void setDrawingToolsVisible(boolean v) {
        drawingToolsVisible = v;
        textToolsVisible = false;
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
            if (mx >= startX && mx <= startX + swatchSize + 2 && my >= rowY && my <= rowY + swatchSize + 2) return BTN_FILL_COLOR;
            if (mx >= swatch2X && mx <= swatch2X + swatchSize + 2 && my >= rowY && my <= rowY + swatchSize + 2) return BTN_BORDER_COLOR;
            if (mx >= startX && mx <= startX + swatchSize + 2 && my >= rowY2 && my <= rowY2 + swatchSize + 2) return BTN_TEXT_COLOR;
        } else if (tSel != null) {
            if (mx >= startX && mx <= startX + swatchSize + 2 && my >= rowY && my <= rowY + swatchSize + 2) return BTN_TEXT_COLOR;
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
        if (!drawingToolsVisible || pSel == null || my < y || my > y + getHeight()) return -1;
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
            int btnY1 = y + 12;
            int btnY2 = y + 32; 
            int curX = barStartX + 6 + 80 + 4; 

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