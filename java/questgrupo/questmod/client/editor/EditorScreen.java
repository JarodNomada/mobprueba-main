package questgrupo.questmod.client.editor;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import questgrupo.questmod.Config;
import questgrupo.questmod.client.GlobalGuiSettings;
import questgrupo.questmod.client.gui.FigurasEdit;
import questgrupo.questmod.client.gui.TextoEdit;

import java.util.List;

public class EditorScreen extends Screen {
    private final Screen lastScreen;
    private GlobalGuiSettings.TextConfig tSel = null;
    private GlobalGuiSettings.PanelConfig pSel = null;

    private Button btnAceptarColor, btnCancelarColor;
    private int colorOriginalGuardado = 0;
    private int colorPopupX, colorPopupY;
    private static final int COLOR_POPUP_W = 120, COLOR_POPUP_H = 58;

    private boolean escribiendoTexto = false, escribiendoTextoPanel = false, arrastrando = false, redimensionando = false;
    private double dragX, dragY;
    private EditBox inputColor;
    private boolean editandoColorHerramientas = false;

    private boolean menuVisible = false;
    private boolean menuCapaVisible = false, menuRotarVisible = false;
    private int menuX, menuY, capaMenuX, capaMenuY, rotarMenuX, rotarMenuY;

    private boolean spacingPanelVisible = false;
    private int spacingPanelX, spacingPanelY;

    private boolean drawingLine = false;
    private int lineStartX, lineStartY;
    private boolean drawingBrush = false;
    private GlobalGuiSettings.BrushStroke currentStroke = null;

    public EditorScreen(Screen lastScreen) {
        super(Component.literal("Editor 1.20.1"));
        this.lastScreen = lastScreen;
    }

    private void updateTopBarVisibility() {
        if (!GlobalGuiSettings.editorActivo) {
            TopBar.setVisible(false);
            return;
        }

        boolean esMisionTexto = pSel != null && (pSel.tipo.equals("MISION_TITULO") || pSel.tipo.equals("MISION_DESCRIPCION"));

        if (tSel != null) {
            TopBar.setVisible(true);
            TopBar.setTextToolsVisible(true);
        } else if (esMisionTexto) {
            TopBar.setVisible(true);
            TopBar.setTextToolsVisible(true);
        } else if (pSel != null) {
            TopBar.setVisible(true);
            TopBar.setDrawingToolsVisible(true);
        } else {
            TopBar.setVisible(false);
        }
    }

    @Override
    protected void init() {
        // Repoblar las misiones desde las aceptadas cada vez que se abre el editor
        Config.inyectarMisionesEnEditor();
        
        this.clearWidgets();

        this.inputColor = new EditBox(this.font, 0, 0, 60, 12, Component.literal(""));
        this.inputColor.setMaxLength(8);
        this.inputColor.setResponder(s -> {
            if (s.length() >= 8 && s.matches("[0-9a-fA-F]+")) {
                try {
                    int color = (int) Long.parseLong(s, 16);
                    if (tSel != null) tSel.colorARGB = color;
                    if (pSel != null) {
                        if (FigurasEdit.editandoColorIndex == 0) pSel.colorARGB = color;
                        else if (FigurasEdit.editandoColorIndex == 1) pSel.colorBorde = color;
                        else if (FigurasEdit.editandoColorIndex == 2) pSel.colorTexto = color;
                        else if (FigurasEdit.editandoColorIndex == 3) pSel.colorFondoMision = color;
                        else if (FigurasEdit.editandoColorIndex == 4) pSel.colorBordeMision = color;
                        else if (FigurasEdit.editandoColorIndex == 5) pSel.colorFondoCabecera = color;
                        else if (FigurasEdit.editandoColorIndex == 6) pSel.colorBordeCabecera = color;
                        else if (FigurasEdit.editandoColorIndex == 7) pSel.colorARGB = color;
                        else if (FigurasEdit.editandoColorIndex == 8) pSel.colorBorde = color;
                        else if (FigurasEdit.editandoColorIndex == 9) pSel.colorTexto = color;
                    }
                    if (editandoColorHerramientas) {
                        GlobalGuiSettings.colorHerramientas = color;
                    }
                } catch (NumberFormatException e) {
                }
            }
});
        this.inputColor.visible = false;
        this.addRenderableWidget(inputColor);

        btnAceptarColor = Button.builder(Component.literal("Aceptar"), b -> {
            inputColor.visible = false;
            inputColor.setFocused(false);
            editandoColorHerramientas = false;
            btnAceptarColor.visible = false;
            btnCancelarColor.visible = false;
        }).bounds(0, 0, 52, 18).build();
        btnAceptarColor.visible = false;
        this.addRenderableWidget(btnAceptarColor);

        btnCancelarColor = Button.builder(Component.literal("Cancelar"), b -> {
            if (tSel != null) tSel.colorARGB = colorOriginalGuardado;
            else if (pSel != null) {
                if (FigurasEdit.editandoColorIndex == 0) pSel.colorARGB = colorOriginalGuardado;
                else if (FigurasEdit.editandoColorIndex == 1) pSel.colorBorde = colorOriginalGuardado;
                else if (FigurasEdit.editandoColorIndex == 2) pSel.colorTexto = colorOriginalGuardado;
                else if (FigurasEdit.editandoColorIndex == 3) pSel.colorFondoMision = colorOriginalGuardado;
                else if (FigurasEdit.editandoColorIndex == 4) pSel.colorBordeMision = colorOriginalGuardado;
                else if (FigurasEdit.editandoColorIndex == 5) pSel.colorFondoCabecera = colorOriginalGuardado;
                else if (FigurasEdit.editandoColorIndex == 6) pSel.colorBordeCabecera = colorOriginalGuardado;
                else if (FigurasEdit.editandoColorIndex == 7) pSel.colorARGB = colorOriginalGuardado;
                else if (FigurasEdit.editandoColorIndex == 8) pSel.colorBorde = colorOriginalGuardado;
                else if (FigurasEdit.editandoColorIndex == 9) pSel.colorTexto = colorOriginalGuardado;
            } else if (editandoColorHerramientas) {
                GlobalGuiSettings.colorHerramientas = colorOriginalGuardado;
            }
            inputColor.visible = false;
            inputColor.setFocused(false);
            editandoColorHerramientas = false;
            btnAceptarColor.visible = false;
            btnCancelarColor.visible = false;
        }).bounds(0, 0, 52, 18).build();
        btnCancelarColor.visible = false;
        this.addRenderableWidget(btnCancelarColor);

        // Botón ON/OFF en la esquina superior derecha
        Button btnEditorOnOff = Button.builder(
            Component.literal(GlobalGuiSettings.editorActivo ? "OFF" : "ON"),
            b -> {
                GlobalGuiSettings.editorActivo = !GlobalGuiSettings.editorActivo;
                LeftSidebar.sidebarVisible = GlobalGuiSettings.editorActivo;
                this.init();
            }
        ).bounds(this.width - 80, 5, 75, 20).build();
        this.addRenderableWidget(btnEditorOnOff);

        LeftSidebar.sidebarVisible = GlobalGuiSettings.editorActivo;

        updateTopBarVisibility();

        if (TopBar.isTextToolsVisible() && (tSel != null || pSel != null)) {
            int barY = 5;
            int buttonY = barY + 9;

            TextoEdit.inicializarOActualizarBotones(
                    this.width, buttonY, this::addRenderableWidget, () -> tSel, () -> pSel, () -> {
                        spacingPanelVisible = true;
                        spacingPanelX = (int)Minecraft.getInstance().mouseHandler.xpos();
                        spacingPanelY = (int)Minecraft.getInstance().mouseHandler.ypos();
                    }
            );
        }

        if (TopBar.isDrawingToolsVisible() && pSel != null && pSel.textoAsociado != null && !pSel.textoAsociado.isEmpty()) {
            TopBar.inicializarBotonesMision(this.width, 5, this::addRenderableWidget, pSel);
        }
    }

    private void handleDrawingButtonClick(int btnId) {
        if (pSel == null) return;
        int barX = LeftSidebar.getSidebarWidth();
        int barStartX = barX + (this.width - barX - TopBar.getWidth()) / 2;
        int colorX = barStartX + 10;

        switch (btnId) {
            case TopBar.BTN_HEADER_FILL_COLOR:
                FigurasEdit.editandoColorIndex = 5;
                LeftSidebar.selectedModule = -1;
                colorOriginalGuardado = pSel.colorFondoCabecera;
                inputColor.setValue(String.format("%08X", pSel.colorFondoCabecera));
                inputColor.visible = true; btnAceptarColor.visible = true; btnCancelarColor.visible = true;
                inputColor.setFocused(true);
                break;
            case TopBar.BTN_HEADER_BORDER_COLOR:
                FigurasEdit.editandoColorIndex = 6;
                LeftSidebar.selectedModule = -1;
                colorOriginalGuardado = pSel.colorBordeCabecera;
                inputColor.setValue(String.format("%08X", pSel.colorBordeCabecera));
                inputColor.visible = true; btnAceptarColor.visible = true; btnCancelarColor.visible = true;
                inputColor.setFocused(true);
                break;
            case TopBar.BTN_FILL_COLOR:
                FigurasEdit.editandoColorIndex = 0;
                LeftSidebar.selectedModule = -1;
                colorOriginalGuardado = pSel.colorARGB;
                inputColor.setValue(String.format("%08X", pSel.colorARGB));
                inputColor.visible = true; btnAceptarColor.visible = true; btnCancelarColor.visible = true;
                inputColor.setFocused(true);
                break;
            case TopBar.BTN_BORDER_COLOR:
                FigurasEdit.editandoColorIndex = 1;
                LeftSidebar.selectedModule = -1;
                colorOriginalGuardado = pSel.colorBorde;
                inputColor.setValue(String.format("%08X", pSel.colorBorde));
                inputColor.visible = true; btnAceptarColor.visible = true; btnCancelarColor.visible = true;
                inputColor.setFocused(true);
                break;
            case TopBar.BTN_TEXT_COLOR:
                if (pSel.tipo.equals("DETALLE_MISION")) {
                    switch (pSel.subElementoSel) {
                        case 1: colorOriginalGuardado = pSel.colorTexto; break;
                        case 3: colorOriginalGuardado = pSel.colorDesc; break;
                        case 4: colorOriginalGuardado = pSel.colorObj; break;
                        case 5: colorOriginalGuardado = pSel.colorRec; break;
                        default: colorOriginalGuardado = pSel.colorTexto;
                    }
                } else {
                    colorOriginalGuardado = pSel.colorTexto;
                }
                FigurasEdit.editandoColorIndex = 2;
                LeftSidebar.selectedModule = -1;
                inputColor.setValue(String.format("%08X", colorOriginalGuardado));
                inputColor.visible = true; btnAceptarColor.visible = true; btnCancelarColor.visible = true;
                inputColor.setFocused(true);
                break;
            case TopBar.BTN_MISSION_FILL_COLOR:
                if (pSel.tipo.equals("DETALLE_MISION")) {
                    switch (pSel.subElementoSel) {
                        case 3: colorOriginalGuardado = pSel.colorDesc; break;
                        case 4: colorOriginalGuardado = pSel.colorObj; break;
                        case 5: colorOriginalGuardado = pSel.colorRec; break;
                        default: colorOriginalGuardado = pSel.colorFondoMision;
                    }
                } else {
                    colorOriginalGuardado = pSel.colorFondoMision;
                }
                FigurasEdit.editandoColorIndex = 3;
                LeftSidebar.selectedModule = -1;
                inputColor.setValue(String.format("%08X", colorOriginalGuardado));
                inputColor.visible = true; btnAceptarColor.visible = true; btnCancelarColor.visible = true;
                inputColor.setFocused(true);
                break;
            case TopBar.BTN_MISSION_BORDER_COLOR:
                FigurasEdit.editandoColorIndex = 4;
                LeftSidebar.selectedModule = -1;
                colorOriginalGuardado = pSel.colorBordeMision;
                inputColor.setValue(String.format("%08X", pSel.colorBordeMision));
                inputColor.visible = true; btnAceptarColor.visible = true; btnCancelarColor.visible = true;
                inputColor.setFocused(true);
                break;
            case TopBar.BTN_MINUS_TEXT_SCALE:
                if (pSel.tipo.equals("DETALLE_MISION")) {
                    switch (pSel.subElementoSel) {
                        case 1: pSel.escalaTexto = Math.max(0.1f, pSel.escalaTexto - 0.1f); break;
                        case 2: pSel.scaleIcon = Math.max(0.1f, pSel.scaleIcon - 0.1f); break;
                        case 3: pSel.scaleDesc = Math.max(0.1f, pSel.scaleDesc - 0.1f); break;
                        case 4: pSel.scaleObj = Math.max(0.1f, pSel.scaleObj - 0.1f); break;
                        case 5: pSel.scaleRec = Math.max(0.1f, pSel.scaleRec - 0.1f); break;
                        default: pSel.escalaTexto = Math.max(0.1f, pSel.escalaTexto - 0.1f);
                    }
                } else {
                    pSel.escalaTexto = Math.max(0.1f, pSel.escalaTexto - 0.1f);
                }
                break;
            case TopBar.BTN_PLUS_TEXT_SCALE:
                if (pSel.tipo.equals("DETALLE_MISION")) {
                    switch (pSel.subElementoSel) {
                        case 1: pSel.escalaTexto = Math.min(10.0f, pSel.escalaTexto + 0.1f); break;
                        case 2: pSel.scaleIcon = Math.min(10.0f, pSel.scaleIcon + 0.1f); break;
                        case 3: pSel.scaleDesc = Math.min(10.0f, pSel.scaleDesc + 0.1f); break;
                        case 4: pSel.scaleObj = Math.min(10.0f, pSel.scaleObj + 0.1f); break;
                        case 5: pSel.scaleRec = Math.min(10.0f, pSel.scaleRec + 0.1f); break;
                        default: pSel.escalaTexto = Math.min(10.0f, pSel.escalaTexto + 0.1f);
                    }
                } else {
                    pSel.escalaTexto = Math.min(10.0f, pSel.escalaTexto + 0.1f);
                }
                break;
            case TopBar.BTN_MINUS_ICON_SCALE:
                if (pSel.tipo.equals("DETALLE_MISION")) {
                    switch (pSel.subElementoSel) {
                        case 2: pSel.scaleIcon = Math.max(0.1f, pSel.scaleIcon - 0.1f); break;
                        default: pSel.escalaIcono = Math.max(0.1f, pSel.escalaIcono - 0.1f);
                    }
                } else {
                    pSel.escalaIcono = Math.max(0.1f, pSel.escalaIcono - 0.1f);
                }
                break;
            case TopBar.BTN_PLUS_ICON_SCALE:
                if (pSel.tipo.equals("DETALLE_MISION")) {
                    switch (pSel.subElementoSel) {
                        case 2: pSel.scaleIcon = Math.min(10.0f, pSel.scaleIcon + 0.1f); break;
                        default: pSel.escalaIcono = Math.min(10.0f, pSel.escalaIcono + 0.1f);
                    }
                } else {
                    pSel.escalaIcono = Math.min(10.0f, pSel.escalaIcono + 0.1f);
                }
                break;
            case TopBar.BTN_MOVE_TEXT_LEFT:
                if (pSel.tipo.equals("DETALLE_MISION")) {
                    switch (pSel.subElementoSel) {
                        case 1: pSel.offsetXTexto -= 2.0f; break;
                        case 2: pSel.offXIcon -= 2.0f; break;
                        case 3: pSel.offXDesc -= 2.0f; break;
                        case 4: pSel.offXObj -= 2.0f; break;
                        case 5: pSel.offXRec -= 2.0f; break;
                        default: pSel.offsetXTexto -= 2.0f;
                    }
                } else {
                    pSel.offsetXTexto -= 2.0f;
                }
                break;
            case TopBar.BTN_MOVE_TEXT_RIGHT:
                if (pSel.tipo.equals("DETALLE_MISION")) {
                    switch (pSel.subElementoSel) {
                        case 1: pSel.offsetXTexto += 2.0f; break;
                        case 2: pSel.offXIcon += 2.0f; break;
                        case 3: pSel.offXDesc += 2.0f; break;
                        case 4: pSel.offXObj += 2.0f; break;
                        case 5: pSel.offXRec += 2.0f; break;
                        default: pSel.offsetXTexto += 2.0f;
                    }
                } else {
                    pSel.offsetXTexto += 2.0f;
                }
                break;
            case TopBar.BTN_MOVE_TEXT_UP:
                if (pSel.tipo.equals("DETALLE_MISION")) {
                    switch (pSel.subElementoSel) {
                        case 1: pSel.offsetYTexto -= 2.0f; break;
                        case 2: pSel.offYIcon -= 2.0f; break;
                        case 3: pSel.offYDesc -= 2.0f; break;
                        case 4: pSel.offYObj -= 2.0f; break;
                        case 5: pSel.offYRec -= 2.0f; break;
                        default: pSel.offsetYTexto -= 2.0f;
                    }
                } else {
                    pSel.offsetYTexto -= 2.0f;
                }
                break;
            case TopBar.BTN_MOVE_TEXT_DOWN:
                if (pSel.tipo.equals("DETALLE_MISION")) {
                    switch (pSel.subElementoSel) {
                        case 1: pSel.offsetYTexto += 2.0f; break;
                        case 2: pSel.offYIcon += 2.0f; break;
                        case 3: pSel.offYDesc += 2.0f; break;
                        case 4: pSel.offYObj += 2.0f; break;
                        case 5: pSel.offYRec += 2.0f; break;
                        default: pSel.offsetYTexto += 2.0f;
                    }
                } else {
                    pSel.offsetYTexto += 2.0f;
                }
                break;
            case TopBar.BTN_MOVE_MISSION_TEXT_UP:
                if (pSel.tipo.equals("DETALLE_MISION")) {
                    switch (pSel.subElementoSel) {
                        case 3: pSel.offYDesc -= 2.0f; break;
                        case 4: pSel.offYObj -= 2.0f; break;
                        case 5: pSel.offYRec -= 2.0f; break;
                    }
                } else {
                    pSel.offsetYTextoMision -= 2.0f;
                }
                break;
            case TopBar.BTN_MOVE_MISSION_TEXT_DOWN:
                if (pSel.tipo.equals("DETALLE_MISION")) {
                    switch (pSel.subElementoSel) {
                        case 3: pSel.offYDesc += 2.0f; break;
                        case 4: pSel.offYObj += 2.0f; break;
                        case 5: pSel.offYRec += 2.0f; break;
                    }
                } else {
                    pSel.offsetYTextoMision += 2.0f;
                }
                break;
            case TopBar.BTN_MOVE_ICON_LEFT:
                if (pSel.tipo.equals("DETALLE_MISION")) {
                    switch (pSel.subElementoSel) {
                        case 2: pSel.offXIcon -= 2.0f; break;
                        default: pSel.offsetXIcono -= 2.0f;
                    }
                } else {
                    pSel.offsetXIcono -= 2.0f;
                }
                break;
            case TopBar.BTN_MOVE_ICON_RIGHT:
                if (pSel.tipo.equals("DETALLE_MISION")) {
                    switch (pSel.subElementoSel) {
                        case 2: pSel.offXIcon += 2.0f; break;
                        default: pSel.offsetXIcono += 2.0f;
                    }
                } else {
                    pSel.offsetXIcono += 2.0f;
                }
                break;
            case TopBar.BTN_MOVE_ICON_UP:
                if (pSel.tipo.equals("DETALLE_MISION")) {
                    switch (pSel.subElementoSel) {
                        case 2: pSel.offYIcon -= 2.0f; break;
                        default: pSel.offsetYIcono -= 2.0f;
                    }
                } else {
                    pSel.offsetYIcono -= 2.0f;
                }
                break;
            case TopBar.BTN_MOVE_ICON_DOWN:
                if (pSel.tipo.equals("DETALLE_MISION")) {
                    switch (pSel.subElementoSel) {
                        case 2: pSel.offYIcon += 2.0f; break;
                        default: pSel.offsetYIcono += 2.0f;
                    }
                } else {
                    pSel.offsetYIcono += 2.0f;
                }
                break;
        }
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        int sidebarReserved = LeftSidebar.getSidebarWidth();
        int viewportY = 0;
        int viewportWidth = this.width - sidebarReserved;
        int viewportHeight = this.height - viewportY;

        // No canvas background - show Minecraft world directly

        Viewport.renderBorder(g, sidebarReserved, viewportY, viewportWidth, viewportHeight);

        LeftSidebar.render(g, this.width, this.height);

        g.pose().pushPose();
        // Draw elements at fixed position - sidebarReserved affects only click detection, not rendering
        g.pose().translate(0, viewportY, 0);

        for (GlobalGuiSettings.BrushStroke stroke : GlobalGuiSettings.TRAZOS) {
            renderBrushStroke(g, stroke);
        }

        if (drawingBrush && currentStroke != null) {
            renderBrushStroke(g, currentStroke);
        }

        if (drawingLine && LeftSidebar.selectedTool == 3) {
            int currentX = (int)mx;
            int currentY = (int)my;
            drawLineThick(g, lineStartX, lineStartY, currentX, currentY, GlobalGuiSettings.colorHerramientas, GlobalGuiSettings.grosorPincel);
        }

        for (GlobalGuiSettings.PanelConfig p : GlobalGuiSettings.PANELES) {
            FigurasEdit.renderizar(g, p, p == pSel, escribiendoTextoPanel && p == pSel);
        }
        for (GlobalGuiSettings.TextConfig t : GlobalGuiSettings.TEXTOS) {
            TextoEdit.renderizar(g, t, this.font, (t == tSel), escribiendoTexto && t == tSel);
        }

        g.pose().popPose();

        TopBar.render(g, this.width, 5, tSel, pSel);

        // Color popup: styled Minecraft window with title, EditBox, and buttons
        if (inputColor.visible) {
            // Calculate popup anchor position based on context
            int anchorX;
            int anchorY;
            if (editandoColorHerramientas) {
                anchorX = LeftSidebar.colorBoxRectX + 8;
                anchorY = LeftSidebar.colorBoxRectY + 20;
            } else {
                int barX = LeftSidebar.getSidebarWidth();
                int barStartX = barX + (this.width - barX - TopBar.getWidth()) / 2;
                int colorX = barStartX + 10;
                if (pSel != null && pSel.tipo.startsWith("DESPLEGABLE")) {
                    if (FigurasEdit.editandoColorIndex == 5) colorX += 0;
                    else if (FigurasEdit.editandoColorIndex == 6) colorX += 19;
                    else if (FigurasEdit.editandoColorIndex == 0) colorX += 19 * 2;
                    else if (FigurasEdit.editandoColorIndex == 1) colorX += 19 * 3;
                    else if (FigurasEdit.editandoColorIndex == 2) colorX += 19 * 4;
                    else if (FigurasEdit.editandoColorIndex == 3) colorX = barStartX + 10 + 91 + 10 + (50 - 34)/2;
                    else if (FigurasEdit.editandoColorIndex == 4) colorX = barStartX + 10 + 91 + 10 + (50 - 34)/2 + 19;
                } else if (pSel != null) {
                    if (FigurasEdit.editandoColorIndex == 1) colorX += 20;
                }
                anchorX = colorX + 7;
                anchorY = 5 + TopBar.getHeight() + 2;
            }
            colorPopupX = anchorX - COLOR_POPUP_W / 2;
            colorPopupY = anchorY;
            // Clamp to screen
            if (colorPopupX < 2) colorPopupX = 2;
            if (colorPopupX + COLOR_POPUP_W > this.width - 2) colorPopupX = this.width - 2 - COLOR_POPUP_W;
            dibujarPopupColor(g, colorPopupX, colorPopupY);
        }

        // Always update button visibility for scale buttons based on pSel
        if (pSel != null && pSel.textoAsociado != null && !pSel.textoAsociado.isEmpty()) {
            int barX = LeftSidebar.getSidebarWidth();
            int barW = TopBar.getWidth(); 
            int barStartX = barX + (this.width - barX - barW) / 2;
            
            int curX = barStartX + 10;
            if (pSel.tipo.startsWith("DESPLEGABLE")) {
                curX += 155; 
            } else {
                curX += 20; // Skip Relleno
                if (!pSel.tipo.equals("LINEA") && !pSel.tipo.equals("TRIANGULO")) {
                    curX += 20; // Skip Borde
                }
            }

            curX += 10; // Gap before T
        }

        actualizarVisibilidadYBotones();

        if (menuVisible) dibujarMenu(g, mx, my);
        if (menuCapaVisible) dibujarMenuCapa(g, mx, my);
        if (menuRotarVisible) dibujarMenuRotar(g, mx, my);
        if (spacingPanelVisible) dibujarPanelEspaciado(g, mx, my);

        super.render(g, mx, my, pt);
    }

    private void drawLine1px(GuiGraphics g, int x0, int y0, int x1, int y1, int color) {
        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int err = dx - dy;
        while (true) {
            g.fill(x0, y0, x0 + 1, y0 + 1, color);
            if (x0 == x1 && y0 == y1) break;
            int e2 = 2 * err;
            if (e2 > -dy) { err -= dy; x0 += sx; }
            if (e2 < dx) { err += dx; y0 += sy; }
        }
    }

    private void drawLineThick(GuiGraphics g, int x0, int y0, int x1, int y1, int color, int grosor) {
        if (grosor <= 1) { // Fallback to 1px line for grosor 1
            drawLine1px(g, x0, y0, x1, y1, color);
            return;
        }

        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int err = dx - dy;
        int halfGrosor = grosor / 2;
        
        while (true) {
            // Draw a thick point (square) centered at (x0, y0)
            g.fill(x0 - halfGrosor, y0 - halfGrosor, x0 - halfGrosor + grosor, y0 - halfGrosor + grosor, color);
            
            if (x0 == x1 && y0 == y1) break;
            int e2 = 2 * err;
            if (e2 > -dy) { err -= dy; x0 += sx; }
            if (e2 < dx) { err += dx; y0 += sy; }
        }
    }

    private void dibujarMenu(GuiGraphics g, int mx, int my) {
        if (tSel != null || pSel != null) {
            int menuHeight = tSel != null ? 32 : 16;
            g.fill(menuX, menuY, menuX + 80, menuY + menuHeight, 0xEE111111);
            g.renderOutline(menuX, menuY, 80, menuHeight, 0xFFFFFFFF);
            g.drawString(font, "Capa", menuX + 5, menuY + 5, 0xFFFFFFFF);
            if (tSel != null) g.drawString(font, "Rotar", menuX + 5, menuY + 18, 0xFFFFFFFF);
        }
    }

    private void dibujarMenuCapa(GuiGraphics g, int mx, int my) {
        g.fill(capaMenuX, capaMenuY, capaMenuX + 100, capaMenuY + 64, 0xEE444444);
        g.renderOutline(capaMenuX, capaMenuY, 100, 64, 0xFFFFFFFF);
        g.drawString(font, "Alfrente", capaMenuX + 5, capaMenuY + 5, 0xFFFFFFFF);
        g.drawString(font, "Delante", capaMenuX + 5, capaMenuY + 18, 0xFFFFFFFF);
        g.drawString(font, "Detras", capaMenuX + 5, capaMenuY + 31, 0xFFFFFFFF);
        g.drawString(font, "Al fondo", capaMenuX + 5, capaMenuY + 44, 0xFFFFFFFF);
    }

    private void dibujarMenuRotar(GuiGraphics g, int mx, int my) {
        g.fill(rotarMenuX, rotarMenuY, rotarMenuX + 100, rotarMenuY + 84, 0xEE444444);
        g.renderOutline(rotarMenuX, rotarMenuY, 100, 84, 0xFFFFFFFF);
        g.drawString(font, "0°", rotarMenuX + 5, rotarMenuY + 5, 0xFFFFFFFF);
        g.drawString(font, "90°", rotarMenuX + 5, rotarMenuY + 20, 0xFFFFFFFF);
        g.drawString(font, "180°", rotarMenuX + 5, rotarMenuY + 35, 0xFFFFFFFF);
        g.drawString(font, "270°", rotarMenuX + 5, rotarMenuY + 50, 0xFFFFFFFF);
        g.drawString(font, "Voltear", rotarMenuX + 5, rotarMenuY + 65, 0xFFFFFFFF);
    }

    private void dibujarPanelEspaciado(GuiGraphics g, int mx, int my) {
        g.fill(spacingPanelX, spacingPanelY, spacingPanelX + 150, spacingPanelY + 60, 0xEE444444);
        g.renderOutline(spacingPanelX, spacingPanelY, 150, 60, 0xFFFFFFFF);
        g.drawString(font, "Interletrado: " + String.format("%.1f", tSel != null ? tSel.interletrado : 0), spacingPanelX + 5, spacingPanelY + 5, 0xFFFFFFFF);
        g.drawString(font, "Interlineado: " + String.format("%.1f", tSel != null ? tSel.interlineado : 1), spacingPanelX + 5, spacingPanelY + 25, 0xFFFFFFFF);
    }

    private void dibujarPopupColor(GuiGraphics g, int px, int py) {
        int w = COLOR_POPUP_W;
        int h = COLOR_POPUP_H;

        // Background - Minecraft gray style (same as TopBar)
        g.fill(px + 1, py + 1, px + w - 1, py + h - 1, 0xFFC6C6C6);

        // 1px black outline with 2px rounded corners
        int cb = 0xFF000000;
        g.fill(px + 2, py, px + w - 2, py + 1, cb);           // Top
        g.fill(px + 2, py + h - 1, px + w - 2, py + h, cb);   // Bottom
        g.fill(px, py + 2, px + 1, py + h - 2, cb);            // Left
        g.fill(px + w - 1, py + 2, px + w, py + h - 2, cb);    // Right
        // Corner pixels
        g.fill(px + 1, py + 1, px + 2, py + 2, cb);
        g.fill(px + w - 2, py + 1, px + w - 1, py + 2, cb);
        g.fill(px + 1, py + h - 2, px + 2, py + h - 1, cb);
        g.fill(px + w - 2, py + h - 2, px + w - 1, py + h - 1, cb);

        // Shadow
        g.fill(px + 2, py + h, px + w - 2, py + h + 2, 0x33000000);
        g.fill(px + w, py + 2, px + w + 2, py + h - 2, 0x33000000);

        // Title "Código del color"
        g.drawString(font, "Código del color", px + (w - font.width("Código del color")) / 2, py + 4, 0xFF333333, false);

        // Color preview square (15x15) to the left of the EditBox
        int previewColor = 0xFF000000;
        if (editandoColorHerramientas) {
            previewColor = GlobalGuiSettings.colorHerramientas;
        } else if (tSel != null) {
            previewColor = tSel.colorARGB;
        } else if (pSel != null) {
            if (FigurasEdit.editandoColorIndex == 0) previewColor = pSel.colorARGB;
            else if (FigurasEdit.editandoColorIndex == 1) previewColor = pSel.colorBorde;
            else if (FigurasEdit.editandoColorIndex == 2) previewColor = pSel.colorTexto;
            else if (FigurasEdit.editandoColorIndex == 3) previewColor = pSel.colorFondoMision;
            else if (FigurasEdit.editandoColorIndex == 4) previewColor = pSel.colorBordeMision;
            else if (FigurasEdit.editandoColorIndex == 5) previewColor = pSel.colorFondoCabecera;
            else if (FigurasEdit.editandoColorIndex == 6) previewColor = pSel.colorBordeCabecera;
        }
        int pvX = px + 5;
        int pvY = py + 16;
        g.fill(pvX, pvY, pvX + 15, pvY + 15, previewColor);
        g.renderOutline(pvX - 1, pvY - 1, 17, 17, 0xFF000000);

        // Position the EditBox inside the popup, to the right of preview
        inputColor.setX(pvX + 18);
        inputColor.setY(pvY + 1);

        // Position Aceptar/Cancelar buttons at bottom of popup
        int btnY = py + 35;
        int totalBtnW = 52 + 4 + 52; // two buttons + gap
        int btnStartX = px + (w - totalBtnW) / 2;
        btnAceptarColor.setX(btnStartX);
        btnAceptarColor.setY(btnY);
        btnCancelarColor.setX(btnStartX + 56);
        btnCancelarColor.setY(btnY);
    }

    private void actualizarVisibilidadYBotones() {
        TextoEdit.actualizarEstadoBotones(tSel, pSel);
    }

    private void renderBrushStroke(GuiGraphics g, GlobalGuiSettings.BrushStroke stroke) {
        int prevX = -1, prevY = -1;
        for (int[] punto : stroke.puntos) {
            if (prevX != -1) {
                for (int dx = -stroke.grosor/2; dx <= stroke.grosor/2; dx++) {
                    for (int dy = -stroke.grosor/2; dy <= stroke.grosor/2; dy++) {
                        g.fill(prevX + dx, prevY + dy, punto[0] + dx, punto[1] + dy, stroke.colorARGB);
                    }
                }
            }
            prevX = punto[0];
            prevY = punto[1];
        }
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        // 1. Manejo del Popup de Color (Mantenemos tu lógica actual)
        if (inputColor.visible) {
            boolean insidePopup = mx >= colorPopupX && mx <= colorPopupX + COLOR_POPUP_W
                    && my >= colorPopupY && my <= colorPopupY + COLOR_POPUP_H + 2;
            if (insidePopup) return super.mouseClicked(mx, my, btn);
            else {
                // Cerrar popup si se hace clic fuera
                if (tSel != null) tSel.colorARGB = colorOriginalGuardado;
                else if (pSel != null) {
                    if (FigurasEdit.editandoColorIndex == 0) pSel.colorARGB = colorOriginalGuardado;
                    else if (FigurasEdit.editandoColorIndex == 1) pSel.colorBorde = colorOriginalGuardado;
                    else if (FigurasEdit.editandoColorIndex == 2) pSel.colorTexto = colorOriginalGuardado;
                    else if (FigurasEdit.editandoColorIndex == 3) pSel.colorFondoMision = colorOriginalGuardado;
                    else if (FigurasEdit.editandoColorIndex == 4) pSel.colorBordeMision = colorOriginalGuardado;
                    else if (FigurasEdit.editandoColorIndex == 5) pSel.colorFondoCabecera = colorOriginalGuardado;
                    else if (FigurasEdit.editandoColorIndex == 6) pSel.colorBordeCabecera = colorOriginalGuardado;
                    else if (FigurasEdit.editandoColorIndex == 7) pSel.colorARGB = colorOriginalGuardado;
                    else if (FigurasEdit.editandoColorIndex == 8) pSel.colorBorde = colorOriginalGuardado;
                    else if (FigurasEdit.editandoColorIndex == 9) pSel.colorTexto = colorOriginalGuardado;
                } else if (editandoColorHerramientas) {
                    GlobalGuiSettings.colorHerramientas = colorOriginalGuardado;
                }
                inputColor.setFocused(false); inputColor.visible = false;
                editandoColorHerramientas = false; btnAceptarColor.visible = false; btnCancelarColor.visible = false;
                return true;
            }
        }

        int sidebarReserved = LeftSidebar.getSidebarWidth();
        int topBarBottom = TopBar.getHeight() > 0 ? 5 + TopBar.getHeight() : 0;

        // 2. Interacción con la TopBar (Herramientas de Texto y Dibujo)
        if (my >= 5 && my <= topBarBottom && mx >= sidebarReserved) {
            boolean esMisionTexto = pSel != null && (pSel.tipo.equals("MISION_TITULO") || pSel.tipo.equals("MISION_DESCRIPCION"));

            if (tSel != null || esMisionTexto) {
                int colorClick = TopBar.getColorClick((int)mx, (int)my, this.width, 5, tSel, pSel);
                if (colorClick != -1) {
                    FigurasEdit.editandoColorIndex = colorClick;
                    LeftSidebar.selectedModule = -1;
                    if (colorClick == 0 && tSel != null) {
                        colorOriginalGuardado = tSel.colorARGB;
                        inputColor.setValue(String.format("%08X", tSel.colorARGB));
                    } else if (esMisionTexto) {
                        if (colorClick == 7) colorOriginalGuardado = pSel.colorARGB;
                        else if (colorClick == 8) colorOriginalGuardado = pSel.colorBorde;
                        else if (colorClick == 9) colorOriginalGuardado = pSel.colorTexto;
                        if (colorClick == 7) inputColor.setValue(String.format("%08X", pSel.colorARGB));
                        else if (colorClick == 8) inputColor.setValue(String.format("%08X", pSel.colorBorde));
                        else if (colorClick == 9) inputColor.setValue(String.format("%08X", pSel.colorTexto));
                    }
                    inputColor.visible = true; btnAceptarColor.visible = true; btnCancelarColor.visible = true;
                    inputColor.setFocused(true);
                    return true;
                }
            }
            if (super.mouseClicked(mx, my, btn)) return true;

            int btnId = TopBar.getDrawingButtonAt((int)mx, (int)my, this.width, 5, pSel);
            if (btnId != -1) { handleDrawingButtonClick(btnId); return true; }
        }

        // 3. Interacción en MODO OFF (Solo navegación de misiones)
        if (!GlobalGuiSettings.editorActivo) {
            for (int i = GlobalGuiSettings.PANELES.size() - 1; i >= 0; i--) {
                GlobalGuiSettings.PanelConfig p = GlobalGuiSettings.PANELES.get(i);
                if (!p.tipo.startsWith("DESPLEGABLE")) continue;

                boolean isOver = FigurasEdit.mouseSobreFigura(mx, my, p);
                if (!p.desplegado) isOver = mx >= p.x && mx <= p.x + p.ancho && my >= p.y && my <= p.y + 20;
                if (!isOver) continue;

                if (p.desplegado) {
                    int bodyY = p.tipo.equals("DESPLEGABLE_MAESTRO") ? p.y : p.y + 20;
                    if (p.tipo.equals("DESPLEGABLE_MAESTRO")) {
                        int relY = (int)(my - p.y + p.scrollY);
                        int currentY = 5;

                        if (mx >= p.x + 5 && mx <= p.x + p.ancho - 5 && relY >= currentY && relY <= currentY + 15) {
                            p.principalesAbierto = !p.principalesAbierto; return true;
                        }
                        currentY += 20;
                        if (p.principalesAbierto) {
                            for (String mName : p.listaPrincipales) {
                                if (mx >= p.x + 5 && mx <= p.x + p.ancho - 5 && relY >= currentY && relY <= currentY + 30) {
                                    vincularMisionADetalle(mName); return true;
                                }
                                currentY += 35;
                            }
                        }

                        if (mx >= p.x + 5 && mx <= p.x + p.ancho - 5 && relY >= currentY && relY <= currentY + 15) {
                            p.secundariasAbierto = !p.secundariasAbierto; return true;
                        }
                        currentY += 20;
                        if (p.secundariasAbierto) {
                            for (String mName : p.listaSecundarias) {
                                if (mx >= p.x + 5 && mx <= p.x + p.ancho - 5 && relY >= currentY && relY <= currentY + 30) {
                                    vincularMisionADetalle(mName); return true;
                                }
                                currentY += 35;
                            }
                        }
                    } else {
                        int rY = (int)(my - bodyY + p.scrollY);
                        int curY = 5;
                        List<String> misiones = p.tipo.equals("DESPLEGABLE_PRINCIPAL") ? p.listaPrincipales : p.listaSecundarias;
                        for (String mName : misiones) {
                            if (mx >= p.x + 5 && mx <= p.x + p.ancho - 5 && rY >= curY && rY <= curY + 30) {
                                vincularMisionADetalle(mName); return true;
                            }
                            curY += 35;
                        }
                    }
                }
            }
            return super.mouseClicked(mx, my, btn);
        }

        // 4. Interacción en MODO ON (Editor activo)
        // El menú de la izquierda ahora es flotante, procesamos sus clics primero.
        if (GlobalGuiSettings.editorActivo) {
            if (LeftSidebar.handleClick(mx, my, this.height, this.width, tSel, pSel)) {
                if (!GlobalGuiSettings.TEXTOS.isEmpty()) {
                    this.tSel = GlobalGuiSettings.TEXTOS.get(GlobalGuiSettings.TEXTOS.size() - 1);
                    this.pSel = null;
                }
                this.init();
                return true;
            }
        }

        // --- CORRECCIÓN 2: SELECCIÓN E INTERACCIÓN UNIFICADA ---
        for (int i = GlobalGuiSettings.PANELES.size() - 1; i >= 0; i--) {
            GlobalGuiSettings.PanelConfig p = GlobalGuiSettings.PANELES.get(i);
            boolean isMouseOver = FigurasEdit.mouseSobreFigura(mx, my, p);
            if (p.tipo.startsWith("DESPLEGABLE") && !p.desplegado) {
                isMouseOver = mx >= p.x && mx <= p.x + p.ancho && my >= p.y && my <= p.y + 20;
            }

            if (isMouseOver) {
                // Seleccionar inmediatamente si no lo estaba
                if (pSel != p) { pSel = p; tSel = null; this.init(); }

                if (p.tipo.startsWith("DESPLEGABLE")) {
                    // Flecha principal
                    if (mx >= p.x + p.ancho - 20 && mx <= p.x + p.ancho && my >= p.y && my <= p.y + 20) {
                        p.desplegado = !p.desplegado; return true;
                    }

                    if (p.desplegado && p.tipo.equals("DESPLEGABLE_MAESTRO")) {
                        int relY = (int)(my - p.y + p.scrollY);
                        int currentY = 5;
                        boolean doble = FigurasEdit.esDobleClic(p);

                        // Click/Rename Principales
                        if (mx >= p.x + 5 && mx <= p.x + p.ancho - 5 && relY >= currentY && relY <= currentY + 15) {
                            if (doble) { FigurasEdit.EditandoMaestroTitle = 1; escribiendoTextoPanel = true; arrastrando = false; }
                            else p.principalesAbierto = !p.principalesAbierto;
                            return true;
                        }
                        currentY += 20;
                        if (p.principalesAbierto) {
                            for (String mName : p.listaPrincipales) {
                                if (mx >= p.x + 5 && mx <= p.x + p.ancho - 5 && relY >= currentY && relY <= currentY + 30) {
                                    vincularMisionADetalle(mName); return true;
                                }
                                currentY += 35;
                            }
                        }

                        // Click/Rename Secundarias
                        if (mx >= p.x + 5 && mx <= p.x + p.ancho - 5 && relY >= currentY && relY <= currentY + 15) {
                            if (doble) { FigurasEdit.EditandoMaestroTitle = 2; escribiendoTextoPanel = true; arrastrando = false; }
                            else p.secundariasAbierto = !p.secundariasAbierto;
                            return true;
                        }
                        currentY += 20;
                        if (p.secundariasAbierto) {
                            for (String mName : p.listaSecundarias) {
                                if (mx >= p.x + 5 && mx <= p.x + p.ancho - 5 && relY >= currentY && relY <= currentY + 30) {
                                    vincularMisionADetalle(mName); return true;
                                }
                                currentY += 35;
                            }
                        }
                    }

                    // Listas individuales en Editor
                    if (!p.tipo.equals("DESPLEGABLE_MAESTRO") && p.desplegado) {
                        int rY = (int)(my - (p.y + 20) + p.scrollY);
                        int curY = 5;
                        List<String> misiones = p.tipo.equals("DESPLEGABLE_PRINCIPAL") ? p.listaPrincipales : p.listaSecundarias;
                        for (String mName : misiones) {
                            if (mx >= p.x + 5 && mx <= p.x + p.ancho - 5 && rY >= curY && rY <= curY + 30) {
                                vincularMisionADetalle(mName); return true;
                            }
                            curY += 35;
                        }
                    }
                }

                if (FigurasEdit.sobreEsquinaRedimension(mx, my, p) && (!p.tipo.startsWith("DESPLEGABLE") || p.desplegado)) redimensionando = true;
                else { arrastrando = true; dragX = mx - p.x; dragY = my - p.y; }
                return true;
            }
        }

        // Selección de textos (Mantenemos la misma lógica unificada)
        for (int i = GlobalGuiSettings.TEXTOS.size() - 1; i >= 0; i--) {
            GlobalGuiSettings.TextConfig t = GlobalGuiSettings.TEXTOS.get(i);
            if (TextoEdit.mouseSobreTexto(mx, my, t, Minecraft.getInstance().font)) {
                if (tSel != t) { tSel = t; pSel = null; this.init(); }
                if (TextoEdit.esDobleClic(t)) { escribiendoTexto = true; arrastrando = false; }
                else { arrastrando = true; dragX = mx - t.x; dragY = my - t.y; escribiendoTexto = false; }
                return true;
            }
        }

        // Deselección al hacer clic en el fondo
        if (my > topBarBottom && !inputColor.isMouseOver(mx, my)) {
            tSel = null; pSel = null; escribiendoTexto = false; escribiendoTextoPanel = false;
            menuVisible = false; TextoEdit.editandoColor = false; LeftSidebar.selectedModule = -1;
            inputColor.setFocused(false); inputColor.visible = false;
            this.init();
        }

        return super.mouseClicked(mx, my, btn);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int btn) {
        if (drawingLine && LeftSidebar.selectedTool == 3) {
            drawingLine = false;
            int endX = (int)mx;
            int endY = (int)my;
            GlobalGuiSettings.PanelConfig linea = new GlobalGuiSettings.PanelConfig(lineStartX, lineStartY, 0, 0);
            linea.tipo = "LINEA";
            linea.x2 = endX;
            linea.y2 = endY;
            linea.colorARGB = GlobalGuiSettings.colorHerramientas;
            linea.grosor = GlobalGuiSettings.grosorPincel; // Save the thickness
            GlobalGuiSettings.PANELES.add(linea);
            return true;
        }

        if (drawingBrush && LeftSidebar.selectedTool == 1) {
            drawingBrush = false;
            if (currentStroke != null && currentStroke.puntos.size() > 1) {
                GlobalGuiSettings.TRAZOS.add(currentStroke);
            }
            currentStroke = null;
            return true;
        }

        arrastrando = false; redimensionando = false;
        return super.mouseReleased(mx, my, btn);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int btn, double dx, double dy) {
        if (drawingLine && LeftSidebar.selectedTool == 3) {
            return true;
        }

        if (drawingBrush && LeftSidebar.selectedTool == 1 && currentStroke != null) {
            currentStroke.agregarPunto((int)mx, (int)my);
            return true;
        }

        if (redimensionando && pSel != null) {
            pSel.ancho = Math.max(5, (int)(mx - pSel.x));
            if (!pSel.tipo.equals("LINEA")) {
                pSel.alto = Math.max(5, (int)(my - pSel.y));
            }
            return true;
        }
        if (arrastrando) {
            if (tSel != null) { tSel.x = (int)(mx - dragX); tSel.y = (int)(my - dragY); return true; }
            if (pSel != null) { pSel.x = (int)(mx - dragX); pSel.y = (int)(my - dragY); return true; }
        }
        return super.mouseDragged(mx, my, btn, dx, dy);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double scrollDelta) {
        for (int i = GlobalGuiSettings.PANELES.size() - 1; i >= 0; i--) {
            GlobalGuiSettings.PanelConfig p = GlobalGuiSettings.PANELES.get(i);
            if (p.tipo.startsWith("DESPLEGABLE") && p.desplegado) {
                int bodyTop = p.tipo.equals("DESPLEGABLE_MAESTRO") ? p.y : p.y + 20;
                int bodyBottom = p.y + p.alto;
                if (mx >= p.x && mx <= p.x + p.ancho && my >= bodyTop && my <= bodyBottom) {
                    p.scrollY -= scrollDelta * 15;
                    if (p.scrollY < 0) p.scrollY = 0;
                    return true;
                }
            }
        }
        return super.mouseScrolled(mx, my, scrollDelta);
    }

    @Override
    public boolean keyPressed(int key, int sc, int mod) {
        if (inputColor.isFocused()) {
            if (key == InputConstants.KEY_ESCAPE || key == InputConstants.KEY_RETURN || key == InputConstants.KEY_NUMPADENTER) {
                inputColor.setFocused(false);
                inputColor.visible = false;
                editandoColorHerramientas = false;
                btnAceptarColor.visible = false;
                btnCancelarColor.visible = false;
                return true;
            }
            if (inputColor.keyPressed(key, sc, mod)) return true;
        }

        if (KeyboardShortcuts.handleKeyPress(key, mod, tSel)) return true;

        if (key == InputConstants.KEY_DELETE) {
            if (tSel != null) { TextoEdit.eliminarTexto(tSel); tSel = null; this.init(); return true; }
            if (pSel != null) { FigurasEdit.eliminarFigura(pSel); pSel = null; this.init(); return true; }
        }

        if (escribiendoTextoPanel && pSel != null && pSel.tipo.startsWith("DESPLEGABLE")) {
            if (key == InputConstants.KEY_BACKSPACE) {
                if (FigurasEdit.EditandoMaestroTitle == 1 && pSel.tituloPrincipales != null && pSel.tituloPrincipales.length() > 0) {
                    pSel.tituloPrincipales = pSel.tituloPrincipales.substring(0, pSel.tituloPrincipales.length() - 1);
                } else if (FigurasEdit.EditandoMaestroTitle == 2 && pSel.tituloSecundarias != null && pSel.tituloSecundarias.length() > 0) {
                    pSel.tituloSecundarias = pSel.tituloSecundarias.substring(0, pSel.tituloSecundarias.length() - 1);
                } else if (FigurasEdit.EditandoMaestroTitle == 0 && pSel.textoAsociado != null && pSel.textoAsociado.length() > 0) {
                    pSel.textoAsociado = pSel.textoAsociado.substring(0, pSel.textoAsociado.length() - 1);
                }
            } else if (key == InputConstants.KEY_RETURN || key == InputConstants.KEY_ESCAPE) {
                escribiendoTextoPanel = false;
            }
            return true;
        }

        if (escribiendoTexto && tSel != null) {
            if (key == InputConstants.KEY_BACKSPACE && tSel.contenido.length() > 0) {
                tSel.contenido = tSel.contenido.substring(0, tSel.contenido.length() - 1);
            } else if (key == InputConstants.KEY_RETURN || key == InputConstants.KEY_ESCAPE) {
                escribiendoTexto = false;
            }
            return true;
        }
        return super.keyPressed(key, sc, mod);
    }

    @Override
    public boolean charTyped(char c, int m) {
        if (inputColor.isFocused() && inputColor.charTyped(c, m)) return true;
        if (escribiendoTextoPanel && pSel != null && pSel.tipo.startsWith("DESPLEGABLE")) {
            if (FigurasEdit.EditandoMaestroTitle == 1) {
                if (pSel.tituloPrincipales == null) pSel.tituloPrincipales = "";
                pSel.tituloPrincipales += c;
            } else if (FigurasEdit.EditandoMaestroTitle == 2) {
                if (pSel.tituloSecundarias == null) pSel.tituloSecundarias = "";
                pSel.tituloSecundarias += c;
            } else {
                if (pSel.textoAsociado == null) pSel.textoAsociado = "";
                pSel.textoAsociado += c;
            }
            return true;
        }
        if (escribiendoTexto && tSel != null) { tSel.contenido += c; return true; }
        return super.charTyped(c, m);
    }

    @Override public boolean isPauseScreen() { return false; }

    private void vincularMisionADetalle(String nombreMision) {
        GlobalGuiSettings.misionSeleccionadaGlobal = nombreMision;
    }
}
