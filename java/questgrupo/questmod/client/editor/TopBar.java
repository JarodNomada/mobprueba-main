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

    public static boolean isDraggingOpacityImage = false;
    private static int sliderImageX = 0;

    public static final int BTN_FILL_COLOR = 10, BTN_BORDER_COLOR = 11, BTN_TEXT_COLOR = 20;
    public static final int BTN_MISSION_FILL_COLOR = 21, BTN_MISSION_BORDER_COLOR = 22;
    public static final int BTN_HEADER_FILL_COLOR = 30, BTN_HEADER_BORDER_COLOR = 31;
    public static final int BTN_TOOL_COLOR = -2;

    public static final int BTN_MINUS_TEXT_SCALE = 40, BTN_PLUS_TEXT_SCALE = 41;
    public static final int BTN_MINUS_ICON_SCALE = 42, BTN_PLUS_ICON_SCALE = 43;
    public static final int BTN_MOVE_TEXT_LEFT = 44, BTN_MOVE_TEXT_RIGHT = 45;
    public static final int BTN_MOVE_TEXT_UP = 46, BTN_MOVE_TEXT_DOWN = 47;
    public static final int BTN_MOVE_MISSION_TEXT_UP = 48, BTN_MOVE_MISSION_TEXT_DOWN = 49;
    public static final int BTN_MOVE_ICON_LEFT = 50, BTN_MOVE_ICON_RIGHT = 51;
    public static final int BTN_MOVE_ICON_UP = 52, BTN_MOVE_ICON_DOWN = 53;

    // Nuevos botones para objetivos
    public static final int BTN_OBJ_ROW_FILL = 60, BTN_OBJ_ROW_BORDER = 61;
    public static final int BTN_OBJ_ICON_FILL = 62, BTN_OBJ_ICON_BORDER = 63;
    public static final int BTN_OBJ_CHECK_FILL = 64, BTN_OBJ_CHECK_BORDER_IN = 65, BTN_OBJ_CHECK_BORDER_OUT = 66;
    public static final int BTN_SLOT_BG = 70, BTN_SLOT_DARK = 71, BTN_SLOT_LIGHT = 72;
    public static final int BTN_PROG_FILL = 80, BTN_PROG_BG = 81;

    public static Button btnProgEstilo, btnProgDiseno, btnProgBorde, btnProgEscalaMinus, btnProgEscalaPlus;

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

    private static GlobalGuiSettings.PanelConfig currentPanel = null;
    public static void setCurrentPanel(GlobalGuiSettings.PanelConfig p) { currentPanel = p; }

    public static int getHeight() {
        if (!visible) return 0;
        if (currentPanel != null && currentPanel.tipo.equals("DETALLE_MISION")) {
            return 34;
        }
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
                case BTN_OBJ_ROW_FILL: return pSel.colorFondoRenglon;
                case BTN_OBJ_ROW_BORDER: return pSel.colorBordeRenglon;
                case BTN_OBJ_ICON_FILL: return pSel.colorFondoIcono;
                case BTN_OBJ_ICON_BORDER: return pSel.colorBordeIcono;
                case BTN_OBJ_CHECK_FILL: return pSel.colorFondoCheck;
                case BTN_OBJ_CHECK_BORDER_IN: return pSel.colorBordeCheckInterno;
                case BTN_OBJ_CHECK_BORDER_OUT: return pSel.colorBordeCheckExterno;
                case BTN_SLOT_BG: return pSel.colorSlotBg;
                case BTN_SLOT_DARK: return pSel.colorSlotDark;
                case BTN_SLOT_LIGHT: return pSel.colorSlotLight;
                case BTN_PROG_FILL: return pSel.colorBarraLleno;
                case BTN_PROG_BG: return pSel.colorBarraFondo;
                case 100: return pSel.colorFondoCheck;
                case 101: return pSel.colorBordeCheckInterno;
                case 102: return pSel.colorSlotBg;
                case 103: return pSel.colorFondoBarra;
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
            hexInput.setFocused(true); // <--- Ahora se enfoca automáticamente
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
        
        if (currentEditingID == BTN_TOOL_COLOR) {
            GlobalGuiSettings.colorHerramientas = finalColor;
        } else if (panelEnEdicion != null) {
            switch (currentEditingID) {
                case BTN_FILL_COLOR: panelEnEdicion.colorARGB = finalColor; break;
                case BTN_BORDER_COLOR: panelEnEdicion.colorBorde = finalColor; break;
                case BTN_TEXT_COLOR: panelEnEdicion.colorTexto = finalColor; break;
                case BTN_MISSION_FILL_COLOR: panelEnEdicion.colorFondoMision = finalColor; break;
                case BTN_MISSION_BORDER_COLOR: panelEnEdicion.colorBordeMision = finalColor; break;
                case BTN_HEADER_FILL_COLOR: panelEnEdicion.colorFondoCabecera = finalColor; break;
                case BTN_HEADER_BORDER_COLOR: panelEnEdicion.colorBordeCabecera = finalColor; break;
                case BTN_OBJ_ROW_FILL: panelEnEdicion.colorFondoRenglon = finalColor; break;
                case BTN_OBJ_ROW_BORDER: panelEnEdicion.colorBordeRenglon = finalColor; break;
                case BTN_OBJ_ICON_FILL: panelEnEdicion.colorFondoIcono = finalColor; break;
                case BTN_OBJ_ICON_BORDER: panelEnEdicion.colorBordeIcono = finalColor; break;
                case BTN_OBJ_CHECK_FILL: panelEnEdicion.colorFondoCheck = finalColor; break;
                case BTN_OBJ_CHECK_BORDER_IN: panelEnEdicion.colorBordeCheckInterno = finalColor; break;
                case BTN_OBJ_CHECK_BORDER_OUT: panelEnEdicion.colorBordeCheckExterno = finalColor; break;
                case BTN_SLOT_BG: panelEnEdicion.colorSlotBg = finalColor; break;
                case BTN_SLOT_DARK: panelEnEdicion.colorSlotDark = finalColor; break;
                case BTN_SLOT_LIGHT: panelEnEdicion.colorSlotLight = finalColor; break;
                case BTN_PROG_FILL: panelEnEdicion.colorBarraLleno = finalColor; break;
                case BTN_PROG_BG: panelEnEdicion.colorBarraFondo = finalColor; break;
                
                case 100: panelEnEdicion.colorFondoCheck = finalColor; break;
                case 101: panelEnEdicion.colorBordeCheckInterno = finalColor; break;
                case 102: panelEnEdicion.colorSlotBg = finalColor; break;
                case 103: panelEnEdicion.colorFondoBarra = finalColor; break;
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
        // Si la barra está oculta Y el selector de color también, no dibujamos nada.
        if (!visible && !colorPickerVisible) return;

        // Si la barra superior está visible, dibujamos sus herramientas
        if (visible) {
            int barX = LeftSidebar.getSidebarWidth();
            barWidth = calculateBarWidth(textToolsVisible, drawingToolsVisible, tSel, pSel);
            int barStartX = barX + (guiWidth - barX - barWidth) / 2;
            int visualBoxHeight = getHeight(); 
            
            drawContainer(g, barStartX, y, barWidth, visualBoxHeight);

            boolean esMisionTexto = pSel != null && (pSel.tipo.equals("MISION_TITULO") || pSel.tipo.equals("MISION_DESCRIPCION"));
            boolean esEstadistica = pSel != null && pSel.tipo.startsWith("ESTADISTICA_");

            if ((textToolsVisible && tSel != null) || esMisionTexto || esEstadistica) {
                renderTextTools(g, barStartX, y, tSel, pSel);
            } else if (drawingToolsVisible) {
                renderDrawingTools(g, barStartX, y, pSel);
            }
        }

        // Si el selector de color fue activado (desde cualquier herramienta), DIBUJAMOS SU FONDO
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
        boolean esEstadistica = pSel != null && pSel.tipo.startsWith("ESTADISTICA_");
        
        if (textTools || esMisionTexto || esEstadistica) {
            int w = 4; w += esMisionTexto ? 38 : 18; w += 2; 
            int cols = esEstadistica ? 4 : 5; w += (cols * 18) + ((cols - 1) * 2); w += 4;
            return w;
        } else if (drawTools && pSel != null) {
            if (pSel.tipo.equals("DETALLE_MISION")) return 244;
            if (pSel.tipo.equals("MISION_OBJETIVOS")) return 230;
            else if (pSel.tipo.equals("BOTON_PAGINA")) return 96;
            else if (pSel.tipo.startsWith("DESPLEGABLE")) {
                int w = 6 + 80; if (pSel.textoAsociado != null && !pSel.textoAsociado.isEmpty()) w += 46 * 3; w += 6; return w;
            } else if (pSel.tipo.equals("MANIQUI") || pSel.tipo.equals("MISION_ICONO")) return 94;
            else if (pSel.tipo.equals("HOTBAR") || pSel.tipo.equals("INVENTORY_GRID")) return 280;
            else if (pSel.tipo.startsWith("SLOT")) return 160;
            else if (pSel.tipo.equals("IMAGEN_CUSTOM") || pSel.tipo.equals("TEXTURA_JUEGO")) return 200;
            else if (pSel.tipo.equals("LISTA_LOGROS")) return 265;
            else if (pSel.tipo.equals("PROGRESO")) {
                return 152;
            } else {
                int w = 6 + 20 + (pSel.tipo.equals("LINEA") || pSel.tipo.equals("TRIANGULO") ? 0 : 20);
                if (pSel.tipo.equals("CUADRADO")) w += 46;
                if (pSel.textoAsociado != null && !pSel.textoAsociado.isEmpty()) w += 46 * 2; w += 6;
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
        boolean esEstadistica = pSel != null && pSel.tipo.startsWith("ESTADISTICA_");

        if (esMisionTexto) {
            int swatch2X = startX + swatchSize + 4;
            int rowY2 = rowY + swatchSize + 4;
            drawColorSwatch(g, pSel.colorARGB, startX + 1, rowY + 1);
            drawColorSwatch(g, pSel.colorBorde, swatch2X + 1, rowY + 1);
            drawColorSwatch(g, pSel.colorTexto, startX + 1, rowY2 + 1);
        } else if (esEstadistica) {
            int rowY2 = rowY + swatchSize + 4;
            // Solo dibujamos Fondo y Color de Texto (apilados)
            drawColorSwatch(g, pSel.colorARGB, startX + 1, rowY + 1);
            drawColorSwatch(g, pSel.colorTexto, startX + 1, rowY2 + 1);
        } else if (tSel != null) {
            drawColorSwatch(g, tSel.colorARGB, startX + 1, rowY + 1);
        }
    }

    public static int getColorClick(int mx, int my, int guiWidth, int y, GlobalGuiSettings.TextConfig tSel, GlobalGuiSettings.PanelConfig pSel) {
        if (!isVisible()) return -1;
        int barX = LeftSidebar.getSidebarWidth();
        int barW = getWidth();
        int barStartX = barX + (guiWidth - barX - barW) / 2;
        boolean esMisionTexto = pSel != null && (pSel.tipo.equals("MISION_TITULO") || pSel.tipo.equals("MISION_DESCRIPCION"));
        boolean esEstadistica = pSel != null && pSel.tipo.startsWith("ESTADISTICA_");

        int swatchSize = 16;
        int rowY = y + 4;
        int startX = barStartX + 4;

        if (esMisionTexto) {
            int swatch2X = startX + swatchSize + 4;
            int rowY2 = rowY + swatchSize + 4;
            if (mx >= startX && mx <= startX + swatchSize + 2 && my >= rowY && my <= rowY + swatchSize + 2) return BTN_FILL_COLOR;
            if (mx >= swatch2X && mx <= swatch2X + swatchSize + 2 && my >= rowY && my <= rowY + swatchSize + 2) return BTN_BORDER_COLOR;
            if (mx >= startX && mx <= startX + swatchSize + 2 && my >= rowY2 && my <= rowY2 + swatchSize + 2) return BTN_TEXT_COLOR;
        } else if (esEstadistica) {
            int rowY2 = rowY + swatchSize + 4;
            if (mx >= startX && mx <= startX + swatchSize + 2 && my >= rowY && my <= rowY + swatchSize + 2) return BTN_FILL_COLOR;
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

        if (pSel.tipo.equals("BOTON_PAGINA")) {
            int rowY = y + 14;
            int curX = barStartX + 6;
            drawSectionTitle(g, font, "F", curX, y + 2, 38);
            drawColorSwatch(g, pSel.colorARGB, curX + 5, rowY);
            drawColorSwatch(g, pSel.colorBorde, curX + 23, rowY);

            curX += 42;
            drawVerticalSeparator(g, curX, y + 4, 26);
            curX += 4;
            drawSectionTitle(g, font, "T", curX, y + 2, 38);
            return;
} else if (pSel.tipo.equals("DETALLE_MISION")) {
            int rowY = y + 14;
            int curX = barStartX + 6;
            int sectionWidth = 46;
            int sectionCWidth = 90;
            int swatchOffset = (sectionWidth - 36) / 2;
            int cSwatchOffset = 10;

            drawSectionTitle(g, font, "F", curX, y + 2, sectionWidth);
            drawColorSwatch(g, pSel.colorARGB, curX + swatchOffset, rowY);
            curX += sectionWidth;
            drawVerticalSeparator(g, curX - 1, y + 4, 44);

            drawSectionTitle(g, font, "O", curX, y + 2, sectionWidth);
            drawColorSwatch(g, pSel.colorFondoIcono, curX + swatchOffset, rowY);
            curX += sectionWidth;
            drawVerticalSeparator(g, curX - 1, y + 4, 44);

            drawSectionTitle(g, font, "R", curX, y + 2, sectionWidth);
            drawColorSwatch(g, pSel.colorFondoRenglon, curX + swatchOffset, rowY);
            curX += sectionWidth;
            drawVerticalSeparator(g, curX - 1, y + 4, 44);

            drawSectionTitle(g, font, "T", curX, y + 2, sectionWidth);
            drawColorSwatch(g, pSel.colorTexto, curX + swatchOffset, rowY);
            curX += sectionWidth;
            drawVerticalSeparator(g, curX - 1, y + 4, 44);

            drawSectionTitle(g, font, "C", curX, y + 2, sectionCWidth);
            drawColorSwatch(g, pSel.colorFondoCheck, curX + cSwatchOffset, rowY);
            drawColorSwatch(g, pSel.colorBordeCheckInterno, curX + cSwatchOffset + 20, rowY);
            drawColorSwatch(g, pSel.colorBordeCheckExterno, curX + cSwatchOffset + 40, rowY);
        } else if (pSel.tipo.equals("MISION_OBJETIVOS")) {
            int curX = barStartX + 6;
            int row1Y = y + 6;
            int row2Y = y + 26;
            
            drawColorSwatch(g, pSel.colorARGB, curX, row1Y);
            drawColorSwatch(g, pSel.colorBorde, curX, row2Y);
            
            drawColorSwatch(g, pSel.colorFondoRenglon, curX + 20, row1Y);
            drawColorSwatch(g, pSel.colorBordeRenglon, curX + 20, row2Y);
            
            drawColorSwatch(g, pSel.colorFondoIcono, curX + 40, row1Y);
            drawColorSwatch(g, pSel.colorBordeIcono, curX + 40, row2Y);
            
            drawColorSwatch(g, pSel.colorFondoCheck, curX + 60, row1Y);
            drawColorSwatch(g, pSel.colorBordeCheckInterno, curX + 60, row2Y);
            
            drawColorSwatch(g, pSel.colorTexto, curX + 80, row1Y);
            
            curX += 100;
            drawVerticalSeparator(g, curX, y + 4, 40);
        } else if (pSel.tipo.startsWith("DESPLEGABLE")) {
            int colorsX = barStartX + 6;

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

        } else if (pSel.tipo.equals("MANIQUI") || pSel.tipo.equals("MISION_ICONO")) {
            int curX = barStartX + 6;
            int boxY = y + 16;
            drawColorSwatch(g, pSel.colorARGB, curX, boxY);
            curX += 20;
            drawColorSwatch(g, pSel.colorBorde, curX, boxY);
            curX += 20;
            drawVerticalSeparator(g, curX, y + 4, 40);
            curX += 4;
            drawSectionTitle(g, font, "Escala", curX, y + 2, 38);
        } else if (pSel.tipo.equals("HOTBAR") || pSel.tipo.equals("INVENTORY_GRID")) {
            int curX = barStartX + 6;
            int boxY = y + 16;
            drawSectionTitle(g, font, "Fondo", curX, y + 2, 20);
            drawColorSwatch(g, pSel.colorARGB, curX + 2, boxY);
            curX += 26;
            drawVerticalSeparator(g, curX, y + 4, 40);
            curX += 6;
            drawSectionTitle(g, font, "Slots", curX, y + 2, 60);
            drawColorSwatch(g, pSel.colorSlotBg, curX, boxY);
            drawColorSwatch(g, pSel.colorSlotDark, curX + 20, boxY);
            drawColorSwatch(g, pSel.colorSlotLight, curX + 40, boxY);
            curX += 66;
            drawVerticalSeparator(g, curX, y + 4, 40);
            curX += 6;
            drawSectionTitle(g, font, "Ajustes", curX, y + 2, 130);
        } else if (pSel.tipo.startsWith("SLOT")) {
            int curX = barStartX + 6;
            int boxY = y + 16;
            drawSectionTitle(g, font, "Fondo", curX, y + 2, 20);
            drawColorSwatch(g, pSel.colorARGB, curX + 2, boxY);
            curX += 26;
            drawVerticalSeparator(g, curX, y + 4, 40);
            curX += 6;
            drawSectionTitle(g, font, "Slot", curX, y + 2, 60);
            drawColorSwatch(g, pSel.colorSlotBg, curX, boxY);
            drawColorSwatch(g, pSel.colorSlotDark, curX + 20, boxY);
            drawColorSwatch(g, pSel.colorSlotLight, curX + 40, boxY);
            curX += 66;
            drawVerticalSeparator(g, curX, y + 4, 40);
            curX += 6;
            drawSectionTitle(g, font, "Escala", curX, y + 2, 42);
        } else if (pSel.tipo.equals("IMAGEN_CUSTOM") || pSel.tipo.equals("TEXTURA_JUEGO")) {
            int curX = barStartX + 6;
            drawSectionTitle(g, font, "Escala", curX, y + 2, 42);
            curX += 48;
            drawVerticalSeparator(g, curX, y + 4, 40);
            curX += 6;
            drawSectionTitle(g, font, "Opacidad: " + (int)(pSel.opacidad * 100) + "%", curX, y + 2, 140);
            
            sliderImageX = curX + 10;
            int sY = y + 24;
            int sW = 120;
            g.fill(sliderImageX, sY, sliderImageX + sW, sY + 6, 0xFF888888); 
            g.renderOutline(sliderImageX - 1, sY - 1, sW + 2, 8, 0xFF000000);
            int knobX = sliderImageX + (int)(pSel.opacidad * (sW - 8));
            g.fill(knobX, sY - 2, knobX + 8, sY + 8, 0xFF555555); 
            g.renderOutline(knobX, sY - 2, 8, 10, 0xFF000000);
        } else if (pSel.tipo.equals("LISTA_LOGROS")) {
            int curX = barStartX + 6;
            int row1Y = y + 6;
            int row2Y = y + 26;

            // Eliminados el Fondo y Borde Global (ya no existen)
            
            drawColorSwatch(g, pSel.colorFondoCabecera, curX, row1Y);
            drawColorSwatch(g, pSel.colorBordeCabecera, curX, row2Y);

            drawColorSwatch(g, pSel.colorFondoBarra, curX + 20, row1Y);
            drawColorSwatch(g, pSel.colorBarraLleno, curX + 20, row2Y);

            drawColorSwatch(g, pSel.colorFondoMision, curX + 40, row1Y);
            drawColorSwatch(g, pSel.colorBordeMision, curX + 40, row2Y);

            drawColorSwatch(g, pSel.colorFondoCheck, curX + 60, row1Y);
            drawColorSwatch(g, pSel.colorBordeCheckInterno, curX + 60, row2Y);

            drawColorSwatch(g, pSel.colorTexto, curX + 80, row1Y);
            drawColorSwatch(g, pSel.colorSlotBg, curX + 80, row2Y);

            curX += 122;
            drawVerticalSeparator(g, curX, y + 4, 40);
        } else if (pSel.tipo.equals("PROGRESO")) {
            int curX = barStartX + 6;
            int row1Y = y + 6;
            int row2Y = y + 26;
            int swatchSize = 16;
            
            drawColorSwatch(g, pSel.colorARGB, curX, row1Y);
            drawColorSwatch(g, pSel.colorBorde, curX + swatchSize + 4, row1Y);
            if (pSel.progresoTipo != 2 && pSel.progresoEstilo == 0) {
                drawColorSwatch(g, pSel.colorBarraLleno, curX, row2Y);
            } else {
                drawColorSwatch(g, pSel.colorTexto, curX, row2Y);
            }
            
            int btnCol1 = curX + 36 + 4;
            int btnCol2 = btnCol1 + 48 + 4;
            
            int btnY1 = row1Y - 1;
            int btnY2 = row2Y - 1;
            
            if (btnProgEstilo != null) btnProgEstilo.visible = false;
            if (btnProgDiseno != null) btnProgDiseno.visible = false;
            if (btnProgBorde != null) btnProgBorde.visible = false;
            if (btnProgEscalaMinus != null) btnProgEscalaMinus.visible = false;
            if (btnProgEscalaPlus != null) btnProgEscalaPlus.visible = false;
            
            if (pSel.progresoTipo == 2) {
                if (btnProgBorde != null) { btnProgBorde.visible = true; btnProgBorde.setX(btnCol1); btnProgBorde.setY(btnY1); }
                if (btnProgEscalaMinus != null) { btnProgEscalaMinus.visible = true; btnProgEscalaMinus.setX(btnCol1); btnProgEscalaMinus.setY(btnY2); }
                if (btnProgEscalaPlus != null) { btnProgEscalaPlus.visible = true; btnProgEscalaPlus.setX(btnCol1 + 26); btnProgEscalaPlus.setY(btnY2); }
            } else if (pSel.progresoEstilo == 0) {
                if (btnProgEstilo != null) { btnProgEstilo.visible = true; btnProgEstilo.setX(btnCol1); btnProgEstilo.setY(btnY1); }
                if (btnProgDiseno != null) { btnProgDiseno.visible = true; btnProgDiseno.setX(btnCol1); btnProgDiseno.setY(btnY2); }
                if (btnProgBorde != null) { btnProgBorde.visible = true; btnProgBorde.setX(btnCol2); btnProgBorde.setY(btnY1); }
            } else {
                if (btnProgEstilo != null) { btnProgEstilo.visible = true; btnProgEstilo.setX(btnCol1); btnProgEstilo.setY(btnY1); }
                if (btnProgBorde != null) { btnProgBorde.visible = true; btnProgBorde.setX(btnCol1); btnProgBorde.setY(btnY2); }
                if (btnProgEscalaMinus != null) { btnProgEscalaMinus.visible = true; btnProgEscalaMinus.setX(btnCol2); btnProgEscalaMinus.setY(btnY1); }
                if (btnProgEscalaPlus != null) { btnProgEscalaPlus.visible = true; btnProgEscalaPlus.setX(btnCol2 + 26); btnProgEscalaPlus.setY(btnY1); }
            }
            return;
        } else {
            int currentX = barStartX + 6;
            int boxY = y + 16; 

            drawColorSwatch(g, pSel.colorARGB, currentX, boxY);
            currentX += 20;

            if (!pSel.tipo.equals("LINEA") && !pSel.tipo.equals("TRIANGULO")) {
                drawColorSwatch(g, pSel.colorBorde, currentX, boxY);
                currentX += 20;
            }

            if (pSel.tipo.equals("CUADRADO")) {
                drawVerticalSeparator(g, currentX, y + 4, 40);
                currentX += 4;
                drawSectionTitle(g, font, "B", currentX, y + 2, 38);
                currentX += 42;
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
        if (!drawingToolsVisible || pSel == null || my < y + 14 || my > y + 32) return -1;
        int barX = LeftSidebar.getSidebarWidth();
        int expectedBarW = calculateBarWidth(false, true, null, pSel);
        int barStartX = barX + (guiWidth - barX - expectedBarW) / 2;

        if (pSel.tipo.equals("DETALLE_MISION")) {
            int rowY = y + 14;
            int curX = barStartX + 6;
            int sectionWidth = 46;
            int sectionCWidth = 90;
            int swatchOffset = (sectionWidth - 36) / 2;
            int cSwatchOffset = 10;

            if (pSel.tipo.equals("BOTON_PAGINA")) {
                if (mx >= curX + 5 && mx <= curX + 21) return BTN_FILL_COLOR;
                if (mx >= curX + 23 && mx <= curX + 39) return BTN_BORDER_COLOR;
                return -1;
            }

            if (mx >= curX + swatchOffset && mx <= curX + swatchOffset + 16) return BTN_FILL_COLOR;
            if (mx >= curX + swatchOffset + 20 && mx <= curX + swatchOffset + 36) return BTN_BORDER_COLOR;
            curX += sectionWidth + 4;
            curX += 4;

            if (mx >= curX + swatchOffset && mx <= curX + swatchOffset + 16) return BTN_OBJ_ROW_FILL;
            if (mx >= curX + swatchOffset + 20 && mx <= curX + swatchOffset + 36) return BTN_OBJ_ROW_BORDER;
            curX += sectionWidth + 4;
            curX += 4;

            if (mx >= curX + swatchOffset && mx <= curX + swatchOffset + 16) return BTN_OBJ_ICON_FILL;
            if (mx >= curX + swatchOffset + 20 && mx <= curX + swatchOffset + 36) return BTN_OBJ_ICON_BORDER;
            curX += sectionWidth + 4;
            curX += 4;

            if (mx >= curX + cSwatchOffset && mx <= curX + cSwatchOffset + 16) return BTN_OBJ_CHECK_FILL;
            if (mx >= curX + cSwatchOffset + 20 && mx <= curX + cSwatchOffset + 36) return BTN_OBJ_CHECK_BORDER_IN;
            if (mx >= curX + cSwatchOffset + 40 && mx <= curX + cSwatchOffset + 56) return BTN_OBJ_CHECK_BORDER_OUT;
        } else if (pSel.tipo.equals("MISION_OBJETIVOS")) {
            int curX = barStartX + 6;
            int row1Y = y + 6;
            int row2Y = y + 26;
            
            if (my >= row1Y && my <= row1Y + 16) {
                if (mx >= curX && mx <= curX + 16) return BTN_FILL_COLOR;
                if (mx >= curX + 20 && mx <= curX + 36) return BTN_OBJ_ROW_FILL;
                if (mx >= curX + 40 && mx <= curX + 56) return BTN_OBJ_ICON_FILL;
                if (mx >= curX + 60 && mx <= curX + 76) return BTN_OBJ_CHECK_FILL;
                if (mx >= curX + 80 && mx <= curX + 96) return BTN_TEXT_COLOR;
            }
            if (my >= row2Y && my <= row2Y + 16) {
                if (mx >= curX && mx <= curX + 16) return BTN_BORDER_COLOR;
                if (mx >= curX + 20 && mx <= curX + 36) return BTN_OBJ_ROW_BORDER;
                if (mx >= curX + 40 && mx <= curX + 56) return BTN_OBJ_ICON_BORDER;
                if (mx >= curX + 60 && mx <= curX + 76) return BTN_OBJ_CHECK_BORDER_IN;
            }
        } else if (pSel.tipo.startsWith("DESPLEGABLE")) {
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
        } else if (pSel.tipo.equals("MANIQUI") || pSel.tipo.equals("MISION_ICONO")) {
            int curX = barStartX + 6;
            int boxY = y + 16;
            if (my >= boxY && my <= boxY + 16) {
                if (mx >= curX && mx <= curX + 16) return BTN_FILL_COLOR;
                if (mx >= curX + 20 && mx <= curX + 36) return BTN_BORDER_COLOR;
            }
        } else if (pSel.tipo.equals("HOTBAR") || pSel.tipo.equals("INVENTORY_GRID")) {
            int curX = barStartX + 6;
            if (mx >= curX + 2 && mx <= curX + 18 && my >= y+16 && my <= y+32) return BTN_FILL_COLOR;
            curX += 32;
            if (mx >= curX && mx <= curX + 16 && my >= y+16 && my <= y+32) return BTN_SLOT_BG;
            if (mx >= curX + 20 && mx <= curX + 36 && my >= y+16 && my <= y+32) return BTN_SLOT_DARK;
            if (mx >= curX + 40 && mx <= curX + 56 && my >= y+16 && my <= y+32) return BTN_SLOT_LIGHT;
        } else if (pSel.tipo.startsWith("SLOT")) {
            int curX = barStartX + 6;
            if (mx >= curX + 2 && mx <= curX + 18 && my >= y+16 && my <= y+32) return BTN_FILL_COLOR;
            curX += 32;
            if (mx >= curX && mx <= curX + 16 && my >= y+16 && my <= y+32) return BTN_SLOT_BG;
            if (mx >= curX + 20 && mx <= curX + 36 && my >= y+16 && my <= y+32) return BTN_SLOT_DARK;
            if (mx >= curX + 40 && mx <= curX + 56 && my >= y+16 && my <= y+32) return BTN_SLOT_LIGHT;
        } else if (pSel.tipo.equals("IMAGEN_CUSTOM") || pSel.tipo.equals("TEXTURA_JUEGO")) {
            return -1; // Ignoramos el clic de color porque las imágenes solo tienen botones +/-
        } else if (pSel.tipo.equals("LISTA_LOGROS")) {
            int curX = barStartX + 6;
            int row1Y = y + 6;
            int row2Y = y + 26;
            
            if (my >= row1Y && my <= row1Y + 16) {
                if (mx >= curX && mx <= curX + 16) return BTN_HEADER_FILL_COLOR;
                if (mx >= curX + 20 && mx <= curX + 36) return 103; 
                if (mx >= curX + 40 && mx <= curX + 56) return BTN_MISSION_FILL_COLOR;
                if (mx >= curX + 60 && mx <= curX + 76) return 100;
                if (mx >= curX + 80 && mx <= curX + 96) return BTN_TEXT_COLOR;
            }
            if (my >= row2Y && my <= row2Y + 16) {
                if (mx >= curX && mx <= curX + 16) return BTN_HEADER_BORDER_COLOR;
                if (mx >= curX + 20 && mx <= curX + 36) return BTN_PROG_FILL; 
                if (mx >= curX + 40 && mx <= curX + 56) return BTN_MISSION_BORDER_COLOR;
                if (mx >= curX + 60 && mx <= curX + 76) return 101;
                if (mx >= curX + 80 && mx <= curX + 96) return 102;
            }
        } else if (pSel.tipo.equals("PROGRESO")) {
            int curX = barStartX + 6;
            int row1Y = y + 6;
            int row2Y = y + 26;
            int swatchSize = 16;
            
            if (mx >= curX && mx <= curX + swatchSize && my >= row1Y && my <= row1Y + swatchSize) return BTN_FILL_COLOR;
            if (mx >= curX + swatchSize + 4 && mx <= curX + swatchSize + 4 + swatchSize && my >= row1Y && my <= row1Y + swatchSize) return BTN_BORDER_COLOR;
            if (mx >= curX && mx <= curX + swatchSize && my >= row2Y && my <= row2Y + swatchSize) {
                return (pSel.progresoTipo != 2 && pSel.progresoEstilo == 0) ? BTN_PROG_FILL : BTN_TEXT_COLOR;
            }
            return -1;
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

            if (!pSel.tipo.equals("BOTON_PAGINA")) {
                adder.accept(Button.builder(Component.literal("←"), b -> pSel.offsetXIcono -= 2.0f).bounds(curX, btnY1, btnSize, btnSize).build());
                adder.accept(Button.builder(Component.literal("→"), b -> pSel.offsetXIcono += 2.0f).bounds(curX + 20, btnY1, btnSize, btnSize).build());
                adder.accept(Button.builder(Component.literal("-"), b -> pSel.escalaIcono = Math.max(0.1f, pSel.escalaIcono - 0.1f)).bounds(curX, btnY2, btnSize, btnSize).build());
                adder.accept(Button.builder(Component.literal("+"), b -> pSel.escalaIcono = Math.min(10.0f, pSel.escalaIcono + 0.1f)).bounds(curX + 20, btnY2, btnSize, btnSize).build());
            }
        }
    }

    public static void inicializarBotonesWidget(int guiWidth, int y, Consumer<Button> adder, GlobalGuiSettings.PanelConfig pSel) {
        if (pSel == null || (!pSel.tipo.equals("HOTBAR") && !pSel.tipo.equals("INVENTORY_GRID"))) return;
        int barX = LeftSidebar.getSidebarWidth();
        int expectedBarW = calculateBarWidth(false, true, null, pSel);
        int barStartX = barX + (guiWidth - barX - expectedBarW) / 2;
        int btnSize = 18;

        int curX = barStartX + 6 + 26 + 6 + 66 + 6; 
        int row1 = y + 5;
        int row2 = y + 25;
        
        if (pSel.tipo.equals("HOTBAR")) {
            adder.accept(Button.builder(Component.literal("🔄"), b -> pSel.isVertical = !pSel.isVertical).bounds(curX, row1, btnSize, btnSize).build());
            adder.accept(Button.builder(Component.literal("V-"), b -> { pSel.visibleSlots = Math.max(1, pSel.visibleSlots - 1); }).bounds(curX + 22, row1, 20, btnSize).build());
            adder.accept(Button.builder(Component.literal("V+"), b -> { pSel.visibleSlots = Math.min(9, pSel.visibleSlots + 1); }).bounds(curX + 44, row1, 20, btnSize).build());
        } else {
            adder.accept(Button.builder(Component.literal("C-"), b -> { pSel.columnas = Math.max(1, pSel.columnas - 1); }).bounds(curX, row1, 20, btnSize).build());
            adder.accept(Button.builder(Component.literal("C+"), b -> { pSel.columnas = Math.min(27, pSel.columnas + 1); }).bounds(curX + 22, row1, 20, btnSize).build());
        }

        adder.accept(Button.builder(Component.literal("T-"), b -> { pSel.slotSize = Math.max(5, pSel.slotSize - 1); }).bounds(curX + 66, row1, 20, btnSize).build());
        adder.accept(Button.builder(Component.literal("T+"), b -> { pSel.slotSize = Math.min(100, pSel.slotSize + 1); }).bounds(curX + 88, row1, 20, btnSize).build());
        
        adder.accept(Button.builder(Component.literal("G-"), b -> { pSel.gap = Math.max(0, pSel.gap - 1); }).bounds(curX + 66, row2, 20, btnSize).build());
        adder.accept(Button.builder(Component.literal("G+"), b -> { pSel.gap = Math.min(50, pSel.gap + 1); }).bounds(curX + 88, row2, 20, btnSize).build());
    }

    public static void inicializarBotonesManiqui(int guiWidth, int y, java.util.function.Consumer<Button> adder, GlobalGuiSettings.PanelConfig pSel) {
        if (pSel == null || (!pSel.tipo.equals("MANIQUI") && !pSel.tipo.equals("MISION_ICONO"))) return;
        int barX = LeftSidebar.getSidebarWidth();
        int expectedBarW = calculateBarWidth(false, true, null, pSel);
        int barStartX = barX + (guiWidth - barX - expectedBarW) / 2;
        int btnSize = 18;

        int curX = barStartX + 6 + 40 + 4; 
        int btnY = y + 20; 
        
        adder.accept(Button.builder(Component.literal("-"), b -> { pSel.escalaIcono = Math.max(0.1f, pSel.escalaIcono - 0.1f); }).bounds(curX, btnY, 18, btnSize).build());
        adder.accept(Button.builder(Component.literal("+"), b -> { pSel.escalaIcono = Math.min(10.0f, pSel.escalaIcono + 0.1f); }).bounds(curX + 20, btnY, 18, btnSize).build());
    }

    public static void inicializarBotonesSlot(int guiWidth, int y, Consumer<Button> adder, GlobalGuiSettings.PanelConfig pSel) {
        if (pSel == null || !pSel.tipo.startsWith("SLOT")) return;
        int barX = LeftSidebar.getSidebarWidth();
        int expectedBarW = calculateBarWidth(false, true, null, pSel);
        int barStartX = barX + (guiWidth - barX - expectedBarW) / 2;
        int btnSize = 18;

        int curX = barStartX + 6 + 26 + 6 + 66 + 6; 
        int btnY = y + 16; 
        
        adder.accept(Button.builder(Component.literal("-"), b -> { pSel.slotSize = Math.max(5, pSel.slotSize - 1); }).bounds(curX, btnY, 20, btnSize).build());
        adder.accept(Button.builder(Component.literal("+"), b -> { pSel.slotSize = Math.min(100, pSel.slotSize + 1); }).bounds(curX + 22, btnY, 20, btnSize).build());
    }

    public static boolean handleOpacitySliderClick(double mx, double my, int guiWidth, int y, GlobalGuiSettings.PanelConfig pSel) {
        if (!drawingToolsVisible || pSel == null) return false;
        if (!pSel.tipo.equals("IMAGEN_CUSTOM") && !pSel.tipo.equals("TEXTURA_JUEGO")) return false;
        
        int sY = y + 24;
        if (mx >= sliderImageX && mx <= sliderImageX + 120 && my >= sY - 2 && my <= sY + 10) {
            isDraggingOpacityImage = true;
            handleOpacitySliderDrag(mx, pSel);
            return true;
        }
        return false;
    }
    
    public static void handleOpacitySliderDrag(double mx, GlobalGuiSettings.PanelConfig pSel) {
        if (pSel == null) return;
        float rel = (float)(mx - sliderImageX) / 112.0f;
        pSel.opacidad = Math.max(0.0f, Math.min(1.0f, rel));
    }

    public static void inicializarBotonesImagen(int guiWidth, int y, Consumer<Button> adder, GlobalGuiSettings.PanelConfig pSel) {
        if (pSel == null || (!pSel.tipo.equals("IMAGEN_CUSTOM") && !pSel.tipo.equals("TEXTURA_JUEGO"))) return;
        int barX = LeftSidebar.getSidebarWidth();
        int expectedBarW = calculateBarWidth(false, true, null, pSel);
        int barStartX = barX + (guiWidth - barX - expectedBarW) / 2;
        int btnSize = 18;

        int curX = barStartX + 6 + 2; 
        int btnY = y + 16; 
        
        adder.accept(Button.builder(Component.literal("-"), b -> { 
            pSel.ancho = Math.max(5, pSel.ancho - 5); 
            pSel.alto = Math.max(5, pSel.alto - 5); 
        }).bounds(curX, btnY, 18, btnSize).build());
        
        adder.accept(Button.builder(Component.literal("+"), b -> { 
            pSel.ancho += 5; 
            pSel.alto += 5; 
        }).bounds(curX + 20, btnY, 18, btnSize).build());
    }

    public static void inicializarBotonesLogros(int guiWidth, int y, Consumer<Button> adder, GlobalGuiSettings.PanelConfig pSel) {
        if (pSel == null || !pSel.tipo.equals("LISTA_LOGROS")) return;
        int barX = LeftSidebar.getSidebarWidth();
        int expectedBarW = calculateBarWidth(false, true, null, pSel);
        int barStartX = barX + (guiWidth - barX - expectedBarW) / 2;
        int btnSize = 18;

        int curX = barStartX + 6 + 112; 
        int btnY1 = y + 5;
        int btnY2 = y + 25;

        adder.accept(Button.builder(Component.literal("T+"), b -> { pSel.escalaTexto = Math.min(3.0f, pSel.escalaTexto + 0.1f); }).bounds(curX, btnY1, btnSize, btnSize).build());
        adder.accept(Button.builder(Component.literal("T-"), b -> { pSel.escalaTexto = Math.max(0.4f, pSel.escalaTexto - 0.1f); }).bounds(curX, btnY2, btnSize, btnSize).build());
        
        curX += 20;
        adder.accept(Button.builder(Component.literal("D+"), b -> { pSel.escalaDesc = Math.min(3.0f, pSel.escalaDesc + 0.1f); }).bounds(curX, btnY1, btnSize, btnSize).build());
        adder.accept(Button.builder(Component.literal("D-"), b -> { pSel.escalaDesc = Math.max(0.4f, pSel.escalaDesc - 0.1f); }).bounds(curX, btnY2, btnSize, btnSize).build());

        curX += 20;
        adder.accept(Button.builder(Component.literal("C+"), b -> { 
            int oldStep = (int)(36 * (pSel.escalaIcono > 0.1f ? pSel.escalaIcono : 1.0f)) + 1;
            int idx = Math.round(pSel.scrollY / (float)oldStep); // Guarda en qué logro estás
            pSel.escalaIcono = Math.min(3.0f, pSel.escalaIcono + 0.1f); 
            int newStep = (int)(36 * pSel.escalaIcono) + 1;
            pSel.scrollY = idx * newStep; // Mueve el scroll para que encaje perfecto
        }).bounds(curX, btnY1, btnSize, btnSize).build());
        
        adder.accept(Button.builder(Component.literal("C-"), b -> { 
            int oldStep = (int)(36 * (pSel.escalaIcono > 0.1f ? pSel.escalaIcono : 1.0f)) + 1;
            int idx = Math.round(pSel.scrollY / (float)oldStep);
            pSel.escalaIcono = Math.max(0.4f, pSel.escalaIcono - 0.1f); 
            int newStep = (int)(36 * pSel.escalaIcono) + 1;
            pSel.scrollY = idx * newStep; 
        }).bounds(curX, btnY2, btnSize, btnSize).build());

        curX += 20;
        adder.accept(Button.builder(Component.literal("M+"), b -> { pSel.escalaMarco = Math.min(3.0f, pSel.escalaMarco + 0.1f); }).bounds(curX, btnY1, btnSize, btnSize).build());
        adder.accept(Button.builder(Component.literal("M-"), b -> { pSel.escalaMarco = Math.max(0.4f, pSel.escalaMarco - 0.1f); }).bounds(curX, btnY2, btnSize, btnSize).build());

        curX += 20;
        adder.accept(Button.builder(Component.literal("I+"), b -> { pSel.escalaItem = Math.min(3.0f, pSel.escalaItem + 0.1f); }).bounds(curX, btnY1, btnSize, btnSize).build());
        adder.accept(Button.builder(Component.literal("I-"), b -> { pSel.escalaItem = Math.max(0.4f, pSel.escalaItem - 0.1f); }).bounds(curX, btnY2, btnSize, btnSize).build());

        curX += 20;
        adder.accept(Button.builder(Component.literal("K+"), b -> { pSel.escalaCheck = Math.min(3.0f, pSel.escalaCheck + 0.1f); }).bounds(curX, btnY1, btnSize, btnSize).build());
        adder.accept(Button.builder(Component.literal("K-"), b -> { pSel.escalaCheck = Math.max(0.4f, pSel.escalaCheck - 0.1f); }).bounds(curX, btnY2, btnSize, btnSize).build());

        curX += 20;
        adder.accept(Button.builder(Component.literal("B+"), b -> { pSel.redondezBorde = Math.min(5, pSel.redondezBorde + 1); }).bounds(curX, btnY1, btnSize, btnSize).build());
        adder.accept(Button.builder(Component.literal("B-"), b -> { pSel.redondezBorde = Math.max(0, pSel.redondezBorde - 1); }).bounds(curX, btnY2, btnSize, btnSize).build());
    }

    public static void inicializarBotonesProgreso(int guiWidth, int y, Consumer<Button> adder, GlobalGuiSettings.PanelConfig pSel) {
        if (pSel == null || !pSel.tipo.equals("PROGRESO")) return;

        btnProgEstilo = Button.builder(Component.literal(getProgresoEstiloName(pSel.progresoEstilo)), b -> {
            pSel.progresoEstilo = (pSel.progresoEstilo + 1) % 3;
            b.setMessage(Component.literal(getProgresoEstiloName(pSel.progresoEstilo)));
        }).bounds(0, 0, 48, 18).build();
        adder.accept(btnProgEstilo);

        btnProgDiseno = Button.builder(Component.literal(getProgresoDisenoName(pSel.disenoBarra)), b -> {
            pSel.disenoBarra = (pSel.disenoBarra + 1) % 2;
            b.setMessage(Component.literal(getProgresoDisenoName(pSel.disenoBarra)));
        }).bounds(0, 0, 48, 18).build();
        adder.accept(btnProgDiseno);

        btnProgBorde = Button.builder(Component.literal("Borde " + pSel.redondezBorde), b -> {
            pSel.redondezBorde = pSel.redondezBorde >= 5 ? 0 : pSel.redondezBorde + 1;
            b.setMessage(Component.literal("Borde " + pSel.redondezBorde));
        }).bounds(0, 0, 48, 18).build();
        adder.accept(btnProgBorde);

        btnProgEscalaMinus = Button.builder(Component.literal("-"), b -> pSel.escalaTexto = Math.max(0.5f, pSel.escalaTexto - 0.1f)).bounds(0, 0, 22, 18).build();
        adder.accept(btnProgEscalaMinus);

        btnProgEscalaPlus = Button.builder(Component.literal("+"), b -> pSel.escalaTexto = Math.min(3.0f, pSel.escalaTexto + 0.1f)).bounds(0, 0, 22, 18).build();
        adder.accept(btnProgEscalaPlus);
    }

    private static String getProgresoEstiloName(int estilo) {
        switch(estilo) { case 0: return "Estilo 1"; case 1: return "Estilo 2"; case 2: return "Estilo 3"; default: return "Estilo 1"; }
    }

    private static String getProgresoDisenoName(int diseno) {
        switch(diseno) { case 0: return "Dise\u00f1o 1"; case 1: return "Dise\u00f1o 2"; default: return "Dise\u00f1o 1"; }
    }

    public static void inicializarBotonesObjetivos(int guiWidth, int y, Consumer<Button> adder, GlobalGuiSettings.PanelConfig pSel) {
        if (pSel == null || !pSel.tipo.equals("MISION_OBJETIVOS")) return;
        int barX = LeftSidebar.getSidebarWidth();
        int expectedBarW = calculateBarWidth(false, true, null, pSel);
        int barStartX = barX + (guiWidth - barX - expectedBarW) / 2;
        int btnSize = 18;

        int curX = barStartX + 6 + 105 + 5; 
        int btnY1 = y + 5;
        int btnY2 = y + 25;

        adder.accept(Button.builder(Component.literal("T+"), b -> { pSel.escalaTexto = Math.min(3.0f, pSel.escalaTexto + 0.1f); }).bounds(curX, btnY1, btnSize, btnSize).build());
        adder.accept(Button.builder(Component.literal("T-"), b -> { pSel.escalaTexto = Math.max(0.4f, pSel.escalaTexto - 0.1f); }).bounds(curX, btnY2, btnSize, btnSize).build());
        
        curX += 20;
        adder.accept(Button.builder(Component.literal("I+"), b -> { pSel.escalaItem = Math.min(3.0f, pSel.escalaItem + 0.1f); }).bounds(curX, btnY1, btnSize, btnSize).build());
        adder.accept(Button.builder(Component.literal("I-"), b -> { pSel.escalaItem = Math.max(0.4f, pSel.escalaItem - 0.1f); }).bounds(curX, btnY2, btnSize, btnSize).build());

        curX += 20;
        adder.accept(Button.builder(Component.literal("C+"), b -> { pSel.escalaCheck = Math.min(3.0f, pSel.escalaCheck + 0.1f); }).bounds(curX, btnY1, btnSize, btnSize).build());
        adder.accept(Button.builder(Component.literal("C-"), b -> { pSel.escalaCheck = Math.max(0.4f, pSel.escalaCheck - 0.1f); }).bounds(curX, btnY2, btnSize, btnSize).build());

        curX += 20;
        adder.accept(Button.builder(Component.literal("B+"), b -> { pSel.redondezBorde = Math.min(5, pSel.redondezBorde + 1); }).bounds(curX, btnY1, btnSize, btnSize).build());
        adder.accept(Button.builder(Component.literal("B-"), b -> { pSel.redondezBorde = Math.max(0, pSel.redondezBorde - 1); }).bounds(curX, btnY2, btnSize, btnSize).build());

        curX += 20;
        adder.accept(Button.builder(Component.literal("G+"), b -> { pSel.gap = Math.min(5, pSel.gap + 1); }).bounds(curX, btnY1, btnSize, btnSize).build());
        adder.accept(Button.builder(Component.literal("G-"), b -> { pSel.gap = Math.max(-1, pSel.gap - 1); }).bounds(curX, btnY2, btnSize, btnSize).build());
    }

    public static void inicializarBotonesCuadrado(int guiWidth, int y, Consumer<Button> adder, GlobalGuiSettings.PanelConfig pSel) {
        if (pSel == null || !pSel.tipo.equals("CUADRADO")) return;
        
        int barX = LeftSidebar.getSidebarWidth();
        int expectedBarW = calculateBarWidth(false, true, null, pSel);
        int barStartX = barX + (guiWidth - barX - expectedBarW) / 2;
        int btnSize = 18;

        int currentX = barStartX + 6 + 40 + 4;
        int btnY1 = y + 16; 

        adder.accept(Button.builder(Component.literal("B+"), b -> { pSel.redondezBorde = Math.min(5, pSel.redondezBorde + 1); }).bounds(currentX, btnY1, 18, btnSize).build());
        adder.accept(Button.builder(Component.literal("B-"), b -> { pSel.redondezBorde = Math.max(0, pSel.redondezBorde - 1); }).bounds(currentX + 20, btnY1, 18, btnSize).build());
    }
}