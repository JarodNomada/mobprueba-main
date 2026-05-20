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

    // -- MODALES DE IMÁGENES --
    private boolean mostrarModalGaleria = false;
    private boolean mostrarModalTexturas = false;
    private int paginaGaleria = 0;
    private int paginaTexturas = 0;
    private net.minecraft.client.gui.components.EditBox buscadorTexturas;
    private java.util.List<net.minecraft.world.item.ItemStack> texturasFiltradas = new java.util.ArrayList<>();
    private String ultimaBusqueda = "";

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

        boolean esMisionTexto = pSel != null && (pSel.tipo.equals("MISION_TITULO") || pSel.tipo.equals("MISION_DESCRIPCION") || pSel.tipo.startsWith("ESTADISTICA_"));

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
        GlobalGuiSettings.sincronizarCapas();
        GlobalGuiSettings.cargarImagenesCustom();
        GlobalGuiSettings.cargarTexturasJuego();
        Config.inyectarMisionesEnEditor();
        
        // 1. EL LIMPIADOR VA PRIMERO
        this.clearWidgets();

        // 2. EL BUSCADOR MODERNO
        this.texturasFiltradas.clear();
        this.texturasFiltradas.addAll(GlobalGuiSettings.TODAS_LAS_TEXTURAS);
        
        int modalW = 280, modalH = 210;
        int mX = (this.width - modalW) / 2, mY = (this.height - modalH) / 2;
        
        this.buscadorTexturas = new net.minecraft.client.gui.components.EditBox(this.font, mX + 45, mY + 45, 200, 16, net.minecraft.network.chat.Component.literal("Buscar..."));
        this.buscadorTexturas.setMaxLength(50);
        this.buscadorTexturas.setBordered(false);
        this.buscadorTexturas.setTextColor(0xFFFFFFFF);
        this.buscadorTexturas.setResponder(s -> {
            String query = s.toLowerCase();
            if (query.equals(this.ultimaBusqueda)) return;
            this.ultimaBusqueda = query;
            this.paginaTexturas = 0;
            this.texturasFiltradas.clear();
            for(net.minecraft.world.item.ItemStack stack : GlobalGuiSettings.TODAS_LAS_TEXTURAS) {
                String name = stack.getHoverName().getString().toLowerCase();
                String id = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(stack.getItem()).toString().toLowerCase();
                if (name.contains(query) || id.contains(query)) this.texturasFiltradas.add(stack);
            }
        });
        this.buscadorTexturas.visible = false;
        this.addRenderableWidget(this.buscadorTexturas);

        // 3. AHORA SÍ, LO DEMÁS
        this.inputColor = new EditBox(this.font, 0, 0, 60, 12, Component.literal(""));
        this.inputColor.setMaxLength(8);
this.inputColor.setResponder(s -> {
            if ((s.length() == 6 || s.length() == 8) && s.matches("[0-9a-fA-F]+")) {
                try {
                    long val = Long.parseLong(s, 16);
                    int color = s.length() == 6 ? (int)(0xFF000000 | val) : (int)val;
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
                        else if (FigurasEdit.editandoColorIndex == 9) pSel.colorBordeCabecera = color;
                        else if (FigurasEdit.editandoColorIndex == 10) pSel.colorBarraLleno = color;
                        else if (FigurasEdit.editandoColorIndex == 11) pSel.colorFondoCheck = color;
                        else if (FigurasEdit.editandoColorIndex == 12) pSel.colorBordeCheckInterno = color;
                        else if (FigurasEdit.editandoColorIndex == 13) pSel.colorSlotBg = color;
                        else if (FigurasEdit.editandoColorIndex == 14) pSel.colorFondoBarra = color;
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
                else if (FigurasEdit.editandoColorIndex == 9) pSel.colorBordeCabecera = colorOriginalGuardado;
                else if (FigurasEdit.editandoColorIndex == 10) pSel.colorBarraLleno = colorOriginalGuardado;
                else if (FigurasEdit.editandoColorIndex == 11) pSel.colorFondoCheck = colorOriginalGuardado;
                else if (FigurasEdit.editandoColorIndex == 12) pSel.colorBordeCheckInterno = colorOriginalGuardado;
                else if (FigurasEdit.editandoColorIndex == 13) pSel.colorSlotBg = colorOriginalGuardado;
                else if (FigurasEdit.editandoColorIndex == 14) pSel.colorFondoBarra = colorOriginalGuardado;
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

        LeftSidebar.sidebarVisible = GlobalGuiSettings.editorActivo;

        updateTopBarVisibility();

        if (TopBar.isTextToolsVisible() && (tSel != null || pSel != null)) {
            int barY = 5;
            int buttonY = barY;

            TextoEdit.inicializarOActualizarBotones(
                    this.width, buttonY, this::addRenderableWidget, () -> tSel, () -> pSel, () -> {
                        spacingPanelVisible = true;
                        spacingPanelX = (int)Minecraft.getInstance().mouseHandler.xpos();
                        spacingPanelY = (int)Minecraft.getInstance().mouseHandler.ypos();
                    }
            );
        }

        if (TopBar.isDrawingToolsVisible() && pSel != null) {
            if (pSel.tipo.equals("HOTBAR") || pSel.tipo.equals("INVENTORY_GRID")) {
                TopBar.inicializarBotonesWidget(this.width, 5, this::addRenderableWidget, pSel);
            } else if (pSel.tipo.equals("MANIQUI") || pSel.tipo.equals("MISION_ICONO")) {
                TopBar.inicializarBotonesManiqui(this.width, 5, this::addRenderableWidget, pSel);
            } else if (pSel.tipo.startsWith("SLOT")) {
                TopBar.inicializarBotonesSlot(this.width, 5, this::addRenderableWidget, pSel);
            } else if (pSel.tipo.equals("IMAGEN_CUSTOM") || pSel.tipo.equals("TEXTURA_JUEGO")) {
                TopBar.inicializarBotonesImagen(this.width, 5, this::addRenderableWidget, pSel);
            } else if (pSel.tipo.equals("LISTA_LOGROS")) {
                TopBar.inicializarBotonesLogros(this.width, 5, this::addRenderableWidget, pSel);
            } else if (pSel.tipo.equals("MISION_OBJETIVOS")) {
                TopBar.inicializarBotonesObjetivos(this.width, 5, this::addRenderableWidget, pSel);
            } else if (pSel.tipo.equals("PROGRESO")) {
                TopBar.inicializarBotonesProgreso(this.width, 0, this::addRenderableWidget, pSel);
            } else {
                TopBar.inicializarBotonesCuadrado(this.width, 5, this::addRenderableWidget, pSel);
                TopBar.inicializarBotonesMision(this.width, 5, this::addRenderableWidget, pSel);
            }
        }

        TopBar.initColorPickerWidgets(this.width, 10, this.font, this::addRenderableWidget);

        boolean hasPage1 = false;
        int maxPage = 1;
        for (GlobalGuiSettings.PanelConfig p : GlobalGuiSettings.PANELES) {
            if (p.pagina > maxPage) maxPage = p.pagina;
            if (p.tipo.equals("BOTON_PAGINA")) {
                if ("1".equals(p.textoAsociado)) hasPage1 = true;
                try { int num = Integer.parseInt(p.textoAsociado); if(num > maxPage) maxPage = num; } catch(Exception e){}
            }
        }
        GlobalGuiSettings.totalPaginas = maxPage;
        if (!hasPage1) FigurasEdit.crearBotonPagina(1);
    }

    private void handleDrawingButtonClick(int btnId) {
        if (pSel == null) return;
        int barX = LeftSidebar.getSidebarWidth();
        int barStartX = barX + (this.width - barX - TopBar.getWidth()) / 2;
        int colorX = barStartX + 10;

        switch (btnId) {
            case TopBar.BTN_PROG_FILL:
                FigurasEdit.editandoColorIndex = 10;
                LeftSidebar.selectedModule = -1;
                colorOriginalGuardado = pSel.colorBarraLleno;
                inputColor.setValue(String.format("%08X", pSel.colorBarraLleno));
                inputColor.visible = true; btnAceptarColor.visible = true; btnCancelarColor.visible = true;
                inputColor.setFocused(true);
                break;
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
            case 100:
                FigurasEdit.editandoColorIndex = 11; LeftSidebar.selectedModule = -1; colorOriginalGuardado = pSel.colorFondoCheck;
                inputColor.setValue(String.format("%08X", pSel.colorFondoCheck)); inputColor.visible = true; btnAceptarColor.visible = true; btnCancelarColor.visible = true; inputColor.setFocused(true); break;
            case 101:
                FigurasEdit.editandoColorIndex = 12; LeftSidebar.selectedModule = -1; colorOriginalGuardado = pSel.colorBordeCheckInterno;
                inputColor.setValue(String.format("%08X", pSel.colorBordeCheckInterno)); inputColor.visible = true; btnAceptarColor.visible = true; btnCancelarColor.visible = true; inputColor.setFocused(true); break;
            case 102:
                FigurasEdit.editandoColorIndex = 13; LeftSidebar.selectedModule = -1; colorOriginalGuardado = pSel.colorSlotBg;
                inputColor.setValue(String.format("%08X", pSel.colorSlotBg)); inputColor.visible = true; btnAceptarColor.visible = true; btnCancelarColor.visible = true; inputColor.setFocused(true); break;
            case 103:
                FigurasEdit.editandoColorIndex = 14; LeftSidebar.selectedModule = -1; colorOriginalGuardado = pSel.colorFondoBarra;
                inputColor.setValue(String.format("%08X", pSel.colorFondoBarra)); inputColor.visible = true; btnAceptarColor.visible = true; btnCancelarColor.visible = true; inputColor.setFocused(true); break;
        }
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        int sidebarReserved = LeftSidebar.getSidebarWidth();
        int viewportY = 0;
        int viewportWidth = this.width - sidebarReserved;
        int viewportHeight = this.height - viewportY;

        Viewport.renderBorder(g, sidebarReserved, viewportY, viewportWidth, viewportHeight);

        // 1. DIBUJAR EL LIENZO Y LAS CAPAS PRIMERO (Al fondo)
        g.pose().pushPose();
        g.pose().translate(0, viewportY, 0);

        float zOffset = 0;
        for (GlobalGuiSettings.Capa capa : GlobalGuiSettings.CAPAS_UI) {
            if (!capa.visible) continue;
            if (capa.pagina != GlobalGuiSettings.paginaActual && capa.pagina != 0) continue;

            g.pose().pushPose();
            g.pose().translate(0, 0, zOffset); // Empujamos cada capa un poco más hacia el frente entre sí
            
            if (capa.dibujo != null) {
                for (GlobalGuiSettings.BrushStroke stroke : capa.dibujo.trazos) renderBrushStroke(g, stroke);
                for (GlobalGuiSettings.PanelConfig linea : capa.dibujo.lineas) FigurasEdit.renderizar(g, linea, false, false);
            } else if (capa.panel != null) {
                FigurasEdit.renderizar(g, capa.panel, capa.panel == pSel, escribiendoTextoPanel && capa.panel == pSel);
            } else if (capa.texto != null) {
                TextoEdit.renderizar(g, capa.texto, this.font, (capa.texto == tSel), escribiendoTexto && capa.texto == tSel);
            }

            g.pose().popPose();
            zOffset += 0.1f; 
        }

        if (drawingBrush && currentStroke != null) {
            renderBrushStroke(g, currentStroke);
        }

        if (drawingLine && LeftSidebar.selectedTool == 3) {
            int currentX = (int)mx;
            int currentY = (int)my;
            drawLineThick(g, lineStartX, lineStartY, currentX, currentY, GlobalGuiSettings.colorHerramientas, GlobalGuiSettings.grosorPincel);
        }
        g.pose().popPose();

        // 2. DIBUJAR LA INTERFAZ DE USUARIO ENCIMA (Barras y Menús)
        g.pose().pushPose();
        g.pose().translate(0, 0, 300); // Elevamos todo el UI 300 niveles en Z para aplastar el lienzo

        LeftSidebar.render(g, this.width, this.height);
        RightBar.render(g, this.width, this.height, mx, my);

        TopBar.setCurrentPanel(pSel);
        TopBar.render(g, this.width, 5, tSel, pSel);

        if (GlobalGuiSettings.editorActivo) {
            DownBar.render(g, this.width, this.height);
        }

        // Color popup
        if (inputColor.visible) {
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
                } else if (pSel != null && pSel.tipo.equals("PROGRESO")) {
                    if (FigurasEdit.editandoColorIndex == 0) colorX = barStartX + 6;
                    else if (FigurasEdit.editandoColorIndex == 1) colorX = barStartX + 26;
                    else colorX = barStartX + 6;
                } else if (pSel != null) {
                    if (FigurasEdit.editandoColorIndex == 1) colorX += 20;
                }
                anchorX = colorX + 7;
                anchorY = 5 + TopBar.getHeight() + 2;
            }
            colorPopupX = anchorX - COLOR_POPUP_W / 2;
            colorPopupY = anchorY;
            if (colorPopupX < 2) colorPopupX = 2;
            if (colorPopupX + COLOR_POPUP_W > this.width - 2) colorPopupX = this.width - 2 - COLOR_POPUP_W;
            dibujarPopupColor(g, colorPopupX, colorPopupY);
        }

        actualizarVisibilidadYBotones();

        if (menuVisible) dibujarMenu(g, mx, my);
        if (menuCapaVisible) dibujarMenuCapa(g, mx, my);
        if (menuRotarVisible) dibujarMenuRotar(g, mx, my);
        if (spacingPanelVisible) dibujarPanelEspaciado(g, mx, my);

        // --- DIBUJO DE MODALES DE IMÁGENES (Z=400) ---
        g.pose().pushPose();
        g.pose().translate(0, 0, 400);
        
        int modalW = 280, modalH = 210;
        int mX = (this.width - modalW) / 2, mY = (this.height - modalH) / 2;

        if (mostrarModalGaleria || mostrarModalTexturas) {
            g.fill(0, 0, this.width, this.height, 0x66000000);

            drawRoundedRect(g, mX - 1, mY - 1, modalW + 2, modalH + 2, 0xFF555555);
            drawRoundedRect(g, mX, mY, modalW, modalH, 0xFFC6C6C6);

            String titulo = mostrarModalGaleria ? "Galer\u00eda Personalizada" : "Texturas del Juego";
            g.drawString(font, titulo, mX + (modalW - font.width(titulo))/2, mY + 15, 0xFF202020, false);

            boolean hoverX = mx >= mX + modalW - 30 && mx <= mX + modalW - 10 && my >= mY + 10 && my <= mY + 30;
            drawModernButton(g, font, mX + modalW - 30, mY + 10, 20, 20, "X", hoverX);

            if (mostrarModalTexturas) {
                drawRoundedRect(g, mX + 19, mY + 39, 242, 22, 0xFF000000); 
                drawRoundedRect(g, mX + 20, mY + 40, 240, 20, 0xFF333333); 
                g.renderOutline(mX + 26, mY + 45, 6, 6, 0xFFAAAAAA);
                g.fill(mX + 31, mY + 50, mX + 34, mY + 53, 0xFFAAAAAA);
                
                this.buscadorTexturas.visible = true;
                
                int maxPags = (int) Math.ceil(texturasFiltradas.size() / 10.0);
                int gridX = mX + 24, gridY = mY + 75;
                for (int i = 0; i < 10; i++) {
                    int idx = paginaTexturas * 10 + i;
                    if (idx >= texturasFiltradas.size()) break;
                    int col = i % 5, row = i / 5;
                    int itemX = gridX + (col * 48), itemY = gridY + (row * 48);
                    
                    boolean hoverItem = mx >= itemX && mx <= itemX + 40 && my >= itemY && my <= itemY + 40;
                    
                    // --- ESTILO IMAGEN 1: Doble Borde (El fondo coincide con el panel) ---
                    int bX = itemX, bY = itemY, bW = 40, bH = 40;
                    
                    drawRoundedRect(g, bX - 1, bY - 1, bW + 2, bH + 2, hoverItem ? 0xFF888888 : 0xFFAAAAAA); 
                    drawRoundedRect(g, bX, bY, bW, bH, 0xFFFFFFFF); 
                    // Fondo idéntico al panel: C6C6C6
                    drawRoundedRect(g, bX + 1, bY + 1, bW - 2, bH - 2, hoverItem ? 0xFFBDBDBD : 0xFFC6C6C6); 
                    
                    g.pose().pushPose();
                    int offset = hoverItem ? 1 : 0; 
                    g.pose().translate(itemX + 8, itemY + 8 + offset, 0);
                    g.pose().scale(1.5f, 1.5f, 1.0f);
                    g.renderFakeItem(texturasFiltradas.get(idx), 0, 0);
                    g.pose().popPose();
                }
                
                int pagY = mY + modalH - 35;
                boolean hoverPrev = mx >= mX + 30 && mx <= mX + 50 && my >= pagY && my <= pagY + 20;
                boolean hoverNext = mx >= mX + modalW - 50 && mx <= mX + modalW - 30 && my >= pagY && my <= pagY + 20;
                drawModernButton(g, font, mX + 30, pagY, 20, 20, "<", hoverPrev);
                drawModernButton(g, font, mX + modalW - 50, pagY, 20, 20, ">", hoverNext);
                
                String pagText = "P\u00e1gina " + (paginaTexturas + 1) + " de " + Math.max(1, maxPags);
                g.drawString(font, pagText, mX + (modalW - font.width(pagText)) / 2, pagY + 6, 0xFF404040, false);

            } else {
                this.buscadorTexturas.visible = false;
                
                int maxPags = (int) Math.ceil(GlobalGuiSettings.CUSTOM_IMAGES.size() / 10.0);
                int gridX = mX + 24, gridY = mY + 60; 
                for (int i = 0; i < 10; i++) {
                    int idx = paginaGaleria * 10 + i;
                    if (idx >= GlobalGuiSettings.CUSTOM_IMAGES.size()) break;
                    int col = i % 5, row = i / 5;
                    int itemX = gridX + (col * 48), itemY = gridY + (row * 48);
                    
                    boolean hoverItem = mx >= itemX && mx <= itemX + 40 && my >= itemY && my <= itemY + 40;
                    
                    // --- ESTILO IMAGEN 1: Doble Borde (El fondo coincide con el panel) ---
                    int bX = itemX, bY = itemY, bW = 40, bH = 40;
                    
                    drawRoundedRect(g, bX - 1, bY - 1, bW + 2, bH + 2, hoverItem ? 0xFF888888 : 0xFFAAAAAA); 
                    drawRoundedRect(g, bX, bY, bW, bH, 0xFFFFFFFF); 
                    // Fondo idéntico al panel: C6C6C6
                    drawRoundedRect(g, bX + 1, bY + 1, bW - 2, bH - 2, hoverItem ? 0xFFBDBDBD : 0xFFC6C6C6);
                    
                    int offset = hoverItem ? 1 : 0;
                    g.blit(GlobalGuiSettings.CUSTOM_IMAGES.get(idx), itemX + 4, itemY + 4 + offset, 0, 0, 32, 32, 32, 32);
                }
                
                int pagY = mY + modalH - 35;
                boolean hoverPrev = mx >= mX + 30 && mx <= mX + 50 && my >= pagY && my <= pagY + 20;
                boolean hoverNext = mx >= mX + modalW - 50 && mx <= mX + modalW - 30 && my >= pagY && my <= pagY + 20;
                drawModernButton(g, font, mX + 30, pagY, 20, 20, "<", hoverPrev);
                drawModernButton(g, font, mX + modalW - 50, pagY, 20, 20, ">", hoverNext);
                
                String pagText = "P\u00e1gina " + (paginaGaleria + 1) + " de " + Math.max(1, maxPags);
                g.drawString(font, pagText, mX + (modalW - font.width(pagText)) / 2, pagY + 6, 0xFF404040, false);
            }
        } else {
            this.buscadorTexturas.visible = false;
        }
        g.pose().popPose();

        // 3. Renderizamos los botones y textos nativos (Aceptar, Cancelar, Buscador) también en lo más alto
        g.pose().pushPose();
        g.pose().translate(0, 0, 500); // 300 base + 500 = 800 (Por encima de ABSOLUTAMENTE TODO)
        super.render(g, mx, my, pt);
        g.pose().popPose();

        g.pose().popPose(); // Restauramos la altura al terminar
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
            else if (FigurasEdit.editandoColorIndex == 10) previewColor = pSel.colorBarraLleno;
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
        int grosor = Math.max(1, stroke.grosor);
        
        for (int[] punto : stroke.puntos) {
            if (prevX != -1) {
                // Usa nuestro interpolador matemático para dibujar una línea sólida y continua
                drawLineThick(g, prevX, prevY, punto[0], punto[1], stroke.colorARGB, grosor);
            } else if (stroke.puntos.size() == 1) {
                // Si el jugador solo hizo un clic (un solo punto), dibujamos un cuadrito
                int halfGrosor = grosor / 2;
                g.fill(punto[0] - halfGrosor, punto[1] - halfGrosor, punto[0] - halfGrosor + grosor, punto[1] - halfGrosor + grosor, stroke.colorARGB);
            }
            prevX = punto[0];
            prevY = punto[1];
        }
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        if (DownBar.handleClick(mx, my, this.width, this.height)) return true;
        if (RightBar.handleClick(mx, my, this.width, this.height)) {
            this.pSel = GlobalGuiSettings.panelSeleccionado;
            this.tSel = GlobalGuiSettings.textoSeleccionado;
            this.init();
            return true;
        }

        // ¡ESTO ES CLAVE!: Si el clic ocurre en el lienzo (fuera de la RightBar), liberamos el teclado
        RightBar.capaEditandoNombre = null;

        // --- 1. ESCUDO Y MANEJO DEL MODAL DE COLOR ---
        if (TopBar.colorPickerVisible) {
            if (super.mouseClicked(mx, my, btn)) return true;
            if (TopBar.handleModalClick(mx, my)) return true;
            return true; 
        }

// --- LÓGICA DE CLICS DE MODALES ---
        if (mostrarModalGaleria || mostrarModalTexturas) {
            int modalW = 280, modalH = 210;
            int mX = (this.width - modalW) / 2, mY = (this.height - modalH) / 2;
            
            // Clic en la X (Cerrar)
            if (mx >= mX + modalW - 30 && mx <= mX + modalW - 10 && my >= mY + 10 && my <= mY + 30) {
                mostrarModalGaleria = false; mostrarModalTexturas = false; return true;
            }
            
            // Flechas <- y ->
            int pagY = mY + modalH - 35;
            if (mx >= mX + 30 && mx <= mX + 50 && my >= pagY && my <= pagY + 20) {
                if (mostrarModalGaleria && paginaGaleria > 0) paginaGaleria--;
                if (mostrarModalTexturas && paginaTexturas > 0) paginaTexturas--;
                return true;
            }
            if (mx >= mX + modalW - 50 && mx <= mX + modalW - 30 && my >= pagY && my <= pagY + 20) {
                if (mostrarModalGaleria && paginaGaleria < (int) Math.ceil(GlobalGuiSettings.CUSTOM_IMAGES.size() / 10.0) - 1) paginaGaleria++;
                if (mostrarModalTexturas && paginaTexturas < (int) Math.ceil(texturasFiltradas.size() / 10.0) - 1) paginaTexturas++;
                return true;
            }
            
            // Clic en los ítems
            int gridX = mX + 24;
            int gridY = mostrarModalTexturas ? mY + 75 : mY + 60;
            for (int i = 0; i < 10; i++) {
                int col = i % 5, row = i / 5;
                int itemX = gridX + (col * 48);
                int itemY = gridY + (row * 48);
                
                if (mx >= itemX && mx <= itemX + 40 && my >= itemY && my <= itemY + 40) {
                    if (mostrarModalGaleria) {
                        int idx = paginaGaleria * 10 + i;
                        if (idx < GlobalGuiSettings.CUSTOM_IMAGES.size()) {
                            LeftSidebar.crearWidget("IMAGEN_CUSTOM", "", this.width/2 - 32, this.height/2 - 32, 64, 64);
                            GlobalGuiSettings.PanelConfig newP = GlobalGuiSettings.PANELES.get(GlobalGuiSettings.PANELES.size()-1);
                            newP.recursoPath = GlobalGuiSettings.CUSTOM_IMAGES.get(idx).toString();
                            mostrarModalGaleria = false; return true;
                        }
                    } else if (mostrarModalTexturas) {
                        int idx = paginaTexturas * 10 + i;
                        if (idx < texturasFiltradas.size()) {
                            String texturaID = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(texturasFiltradas.get(idx).getItem()).toString();
                            
                            LeftSidebar.crearWidget("TEXTURA_JUEGO", "", this.width/2 - 16, this.height/2 - 16, 32, 32);
                            GlobalGuiSettings.PanelConfig newP = GlobalGuiSettings.PANELES.get(GlobalGuiSettings.PANELES.size()-1);
                            newP.recursoPath = texturaID;
                            
                            mostrarModalTexturas = false; return true;
                        }
                    }
                }
            }
            
            // Clic en la barra de búsqueda (Para enfocarla)
            if (this.buscadorTexturas.visible) {
                if (mx >= mX + 20 && mx <= mX + 260 && my >= mY + 40 && my <= mY + 60) {
                    this.buscadorTexturas.setFocused(true);
                    this.buscadorTexturas.mouseClicked(mx, my, btn);
                    return true;
                } else {
                    this.buscadorTexturas.setFocused(false);
                }
            }
            return true; // Escudo: clics en el resto del modal no hacen nada.
        }

        int barX = LeftSidebar.getSidebarWidth();
        int topBarHeight = TopBar.getHeight();
        int visualY = 10;

        if (TopBar.isVisible() && my >= visualY && my <= visualY + topBarHeight) {
            
            // Revisar si tocó el Slider de Opacidad de la Imagen
            if (pSel != null && (pSel.tipo.equals("IMAGEN_CUSTOM") || pSel.tipo.equals("TEXTURA_JUEGO"))) {
                if (TopBar.handleOpacitySliderClick(mx, my, this.width, visualY, pSel)) return true;
            }

            // AQUÍ UNIFICAMOS LA LÓGICA DE DIBUJO Y TEXTO
            int colorID = -1;
            
            if (TopBar.isDrawingToolsVisible() && pSel != null) {
                // Sacamos el ID si hizo clic en un color de dibujo
                colorID = TopBar.getDrawingButtonAt((int) mx, (int) my, this.width, visualY, pSel);
            } 
            
            if (colorID == -1) {
                // Si no, probamos si hizo clic en un color de texto/misión
                colorID = TopBar.getColorClick((int) mx, (int) my, this.width, visualY, tSel, pSel);
            }

            // SI ENCONTRAMOS UN CLIC EN CUALQUIER COLOR -> ABRIMOS EL MODAL
            if (colorID != -1) {
                int currentColor = TopBar.getColorByID(colorID, pSel, tSel);
                TopBar.openPicker(currentColor, colorID, pSel, tSel);
                return true; // Terminamos aquí, el modal ya está abierto
            }

            // Si hizo clic en la barra pero no en un color, dejamos que los botones (+, -) actúen
            return super.mouseClicked(mx, my, btn);
        }

        // Iniciar dibujo con herramientas de dibujo (solo si no estamos en el panel de Config. Pincel)
        boolean enPanelBrush = LeftSidebar.selectedModule == 1 && LeftSidebar.showBrushThickness;
        if (my > visualY + topBarHeight && mx > barX && !enPanelBrush) {
            if (LeftSidebar.selectedTool == 3) {
                drawingLine = true;
                lineStartX = (int)mx;
                lineStartY = (int)my;
                return true;
            }
            if (LeftSidebar.selectedTool == 1) {
                drawingBrush = true;
                currentStroke = new GlobalGuiSettings.BrushStroke();
                currentStroke.colorARGB = GlobalGuiSettings.colorHerramientas;
                currentStroke.grosor = GlobalGuiSettings.grosorPincel;
                currentStroke.agregarPunto((int)mx, (int)my);
                return true;
            }
        }

        // 3. Interacción en MODO OFF (Solo navegación de misiones)
        if (!GlobalGuiSettings.editorActivo) {
            for (int i = GlobalGuiSettings.PANELES.size() - 1; i >= 0; i--) {
                GlobalGuiSettings.PanelConfig p = GlobalGuiSettings.PANELES.get(i);

                if (p.tipo.equals("BOTON_PAGINA")) {
                    if (FigurasEdit.mouseSobreFigura(mx, my, p)) {
                        try {
                            GlobalGuiSettings.paginaActual = Integer.parseInt(p.textoAsociado);
                            return true;
                        } catch (Exception e) {}
                    }
                }

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
        if (GlobalGuiSettings.editorActivo) {
            int textosAnteriores = GlobalGuiSettings.TEXTOS.size();
            if (LeftSidebar.handleClick(mx, my, this.height, this.width, tSel, pSel)) {
                if (LeftSidebar.editColorRequested) {
                    LeftSidebar.editColorRequested = false;
                    TopBar.openPicker(GlobalGuiSettings.colorHerramientas, TopBar.BTN_TOOL_COLOR, null, null);
                } else if (LeftSidebar.showGaleriaRequested) {
                    LeftSidebar.showGaleriaRequested = false;
                    this.mostrarModalGaleria = true;
                    this.paginaGaleria = 0;
                    GlobalGuiSettings.cargarImagenesCustom(); // Lee la carpeta al instante
                } else if (LeftSidebar.showTexturasRequested) {
                    LeftSidebar.showTexturasRequested = false;
                    this.mostrarModalTexturas = true;
                    this.paginaTexturas = 0;
                    this.buscadorTexturas.setValue(""); // Limpia la búsqueda anterior
                } else {
                    if (GlobalGuiSettings.TEXTOS.size() > textosAnteriores) {
                        this.tSel = GlobalGuiSettings.TEXTOS.get(GlobalGuiSettings.TEXTOS.size() - 1);
                        this.pSel = null;
                    }
                    this.init(); 
                }
                return true;
            }
        }

        // --- NUEVA LÓGICA DE CAPAS PARA CLICS ---
        for (int i = GlobalGuiSettings.CAPAS_UI.size() - 1; i >= 0; i--) {
            GlobalGuiSettings.Capa capa = GlobalGuiSettings.CAPAS_UI.get(i);

            if (!capa.visible || capa.bloqueado) continue;
            if (capa.pagina != GlobalGuiSettings.paginaActual && capa.pagina != 0) continue;

            if (capa.panel != null) {
                GlobalGuiSettings.PanelConfig p = capa.panel;
                boolean isMouseOver = FigurasEdit.mouseSobreFigura(mx, my, p);
                if (p.tipo.startsWith("DESPLEGABLE") && !p.desplegado) {
                    isMouseOver = mx >= p.x && mx <= p.x + p.ancho && my >= p.y && my <= p.y + 20;
                }

                if (isMouseOver) {
                    if (pSel != p) { pSel = p; tSel = null; this.init(); }

                    if (p.tipo.startsWith("DESPLEGABLE")) {
                        if (mx >= p.x + p.ancho - 20 && mx <= p.x + p.ancho && my >= p.y && my <= p.y + 20) {
                            p.desplegado = !p.desplegado; return true;
                        }
                        if (p.desplegado && p.tipo.equals("DESPLEGABLE_MAESTRO")) {
                            int relY = (int)(my - p.y + p.scrollY);
                            int currentY = 5;
                            boolean doble = FigurasEdit.esDobleClic(p);
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
            } else if (capa.texto != null) {
                GlobalGuiSettings.TextConfig t = capa.texto;
                if (TextoEdit.mouseSobreTexto(mx, my, t, Minecraft.getInstance().font)) {
                    if (tSel != t) { tSel = t; pSel = null; this.init(); }
                    if (TextoEdit.esDobleClic(t)) { escribiendoTexto = true; arrastrando = false; }
                    else { arrastrando = true; dragX = mx - t.x; dragY = my - t.y; escribiendoTexto = false; }
                    return true;
                }
            }
        }

        // Deselección al hacer clic en el fondo
        if (my > 10 + TopBar.getHeight() && !inputColor.isMouseOver(mx, my)) {
            tSel = null; pSel = null; escribiendoTexto = false; escribiendoTextoPanel = false;
            menuVisible = false; TextoEdit.editandoColor = false; LeftSidebar.selectedModule = -1;
            inputColor.setFocused(false); inputColor.visible = false;
            this.init();
        }

        return super.mouseClicked(mx, my, btn);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int btn) {
        TopBar.stopDragging();
        TopBar.isDraggingOpacityImage = false; // <--- AÑADIR ESTO

        // Solo necesitamos el nuevo método
        if (RightBar.handleMouseReleased(mx, my, btn)) return true;

        if (drawingLine && LeftSidebar.selectedTool == 3) {
            drawingLine = false;
            GlobalGuiSettings.PanelConfig linea = new GlobalGuiSettings.PanelConfig(lineStartX, lineStartY, 0, 0);
            linea.tipo = "LINEA"; linea.x2 = (int)mx; linea.y2 = (int)my;
            linea.colorARGB = GlobalGuiSettings.colorHerramientas; linea.grosor = GlobalGuiSettings.grosorPincel;
            
            if (GlobalGuiSettings.dibujoSeleccionado != null) {
                GlobalGuiSettings.dibujoSeleccionado.lineas.add(linea);
            } else {
                GlobalGuiSettings.GrupoDibujo gd = new GlobalGuiSettings.GrupoDibujo();
                gd.pagina = GlobalGuiSettings.paginaActual; gd.lineas.add(linea);
                GlobalGuiSettings.DIBUJOS.add(gd); GlobalGuiSettings.dibujoSeleccionado = gd;
                GlobalGuiSettings.sincronizarCapas();
            }
            return true;
        }

        if (drawingBrush && LeftSidebar.selectedTool == 1) {
            drawingBrush = false;
            // CAMBIO CLAVE: Ahora guardamos el trazo incluso si es solo 1 punto (tamaño > 0 en vez de > 1)
            if (currentStroke != null && currentStroke.puntos.size() > 0) {
                if (GlobalGuiSettings.dibujoSeleccionado != null) {
                    GlobalGuiSettings.dibujoSeleccionado.trazos.add(currentStroke);
                } else {
                    GlobalGuiSettings.GrupoDibujo gd = new GlobalGuiSettings.GrupoDibujo();
                    gd.pagina = GlobalGuiSettings.paginaActual; gd.trazos.add(currentStroke);
                    GlobalGuiSettings.DIBUJOS.add(gd); GlobalGuiSettings.dibujoSeleccionado = gd;
                    GlobalGuiSettings.sincronizarCapas();
                }
            }
            currentStroke = null;
            return true;
        }

        arrastrando = false; redimensionando = false;
        return super.mouseReleased(mx, my, btn);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int btn, double dx, double dy) {
        // Si la RightBar atrapa el arrastre (para Live Drag & Drop de capas), no movemos el lienzo
        if (RightBar.handleMouseDragged(mx, my, btn, this.width, this.height)) return true;
        
        TopBar.handleModalDrag(mx);

        // NUEVO: Arrastre de Opacidad
        if (TopBar.isDraggingOpacityImage) {
            TopBar.handleOpacitySliderDrag(mx, pSel);
            return true;
        }

        if (drawingLine && LeftSidebar.selectedTool == 3) {
            return true;
        }

        if (drawingBrush && LeftSidebar.selectedTool == 1 && currentStroke != null) {
            currentStroke.agregarPunto((int)mx, (int)my);
            return true;
        }

        if (redimensionando && pSel != null) {
            int nuevoAncho = Math.max(5, (int)(mx - pSel.x));
            int nuevoAlto = Math.max(5, (int)(my - pSel.y));

            pSel.ancho = nuevoAncho;
            if (!pSel.tipo.equals("LINEA")) {
                pSel.alto = nuevoAlto;
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
        if (RightBar.handleScroll(mx, my, scrollDelta, this.width, this.height)) return true;

        for (int i = GlobalGuiSettings.PANELES.size() - 1; i >= 0; i--) {
            GlobalGuiSettings.PanelConfig p = GlobalGuiSettings.PANELES.get(i);
            if (p.pagina != GlobalGuiSettings.paginaActual && !p.tipo.equals("BOTON_PAGINA")) continue;
            if (p.tipo.startsWith("DESPLEGABLE") && p.desplegado) {
                int bodyTop = p.tipo.equals("DESPLEGABLE_MAESTRO") ? p.y : p.y + 20;
                int bodyBottom = p.y + p.alto;
                if (mx >= p.x && mx <= p.x + p.ancho && my >= bodyTop && my <= bodyBottom) {
                    p.scrollY -= scrollDelta * 15;
                    if (p.scrollY < 0) p.scrollY = 0;
                    return true;
                }
            }
            // Lógica de Scroll para HOTBAR (Carrusel deslizante)
            if (p.tipo.equals("HOTBAR") && questgrupo.questmod.client.gui.FigurasEdit.mouseSobreFigura(mx, my, p)) {
                p.scrollIndex -= (int) Math.signum(scrollDelta);
                int maxScroll = 9 - p.visibleSlots;
                if (maxScroll < 0) maxScroll = 0;
                if (p.scrollIndex < 0) p.scrollIndex = 0;
                if (p.scrollIndex > maxScroll) p.scrollIndex = maxScroll;
                return true;
            }
            
            // Lógica de Scroll para LA LISTA DE LOGROS (Snapping Dinámico y Global)
            if (p.tipo.equals("LISTA_LOGROS") && questgrupo.questmod.client.gui.FigurasEdit.mouseSobreFigura(mx, my, p)) {
                float eIcon = p.escalaIcono > 0.1f ? p.escalaIcono : 1.0f;
                // El salto vuelve a ser dinámico, adaptándose a la escala global
                int step = (int)(36 * eIcon) + 1; 
                
                p.scrollY -= Math.signum(scrollDelta) * step;
                p.scrollY = Math.round(p.scrollY / (float)step) * step;
                
                if (p.scrollY < 0) p.scrollY = 0;
                return true;
            }
        }
        return super.mouseScrolled(mx, my, scrollDelta);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        // PRIORIDAD CERO: Escribir en el buscador de texturas
        if (this.buscadorTexturas != null && this.buscadorTexturas.visible && this.buscadorTexturas.isFocused()) {
            return this.buscadorTexturas.charTyped(codePoint, modifiers);
        }

        // 1. PRIORIDAD: Escribir el nombre de una capa en la RightBar
        if (questgrupo.questmod.client.editor.RightBar.capaEditandoNombre != null) {
            questgrupo.questmod.client.editor.RightBar.capaEditandoNombre.nombre += codePoint;
            return true;
        }

        // 2. SEGUNDA PRIORIDAD: Escribir en un objeto de TEXTO haciendo doble clic en el lienzo
        // ¡CORRECCIÓN!: Volvemos a usar 'this.tSel' para que funcione perfectamente tu doble clic
        if (escribiendoTexto && this.tSel != null) {
            if (this.tSel.contenido.equals("Texto")) {
                this.tSel.contenido = "";
            }
            this.tSel.contenido += codePoint;
            questgrupo.questmod.client.GlobalGuiSettings.sincronizarCapas();
            // Sincronizamos la capa global automáticamente
            questgrupo.questmod.client.GlobalGuiSettings.textoSeleccionado = this.tSel; 
            return true;
        }

        // 3. TERCERA PRIORIDAD: Escribir el texto asociado a un PANEL
        if (escribiendoTextoPanel && this.pSel != null) {
            if (this.pSel.textoAsociado == null) {
                this.pSel.textoAsociado = "";
            }
            this.pSel.textoAsociado += codePoint;
            questgrupo.questmod.client.GlobalGuiSettings.sincronizarCapas();
            questgrupo.questmod.client.GlobalGuiSettings.panelSeleccionado = this.pSel;
            return true;
        }

        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // PRIORIDAD CERO: Manejar teclado en el buscador de texturas
        if (this.buscadorTexturas != null && this.buscadorTexturas.visible && this.buscadorTexturas.isFocused()) {
            if (keyCode == 256) { this.buscadorTexturas.setFocused(false); return true; } // ESC para soltar el buscador
            return this.buscadorTexturas.keyPressed(keyCode, scanCode, modifiers);
        } 

        // 1. PRIORIDAD: Borrar o Guardar nombre de capa en la RightBar
        if (questgrupo.questmod.client.editor.RightBar.capaEditandoNombre != null) {
            if (keyCode == 259) { // Tecla Backspace (Borrar)
                String n = questgrupo.questmod.client.editor.RightBar.capaEditandoNombre.nombre;
                if (!n.isEmpty()) {
                    questgrupo.questmod.client.editor.RightBar.capaEditandoNombre.nombre = n.substring(0, n.length() - 1);
                }
                return true;
            }
            if (keyCode == 257 || keyCode == 256) { // Tecla Enter o Escape (Guardar)
                questgrupo.questmod.client.editor.RightBar.capaEditandoNombre = null;
                return true;
            }
            return true; // Bloquea otros atajos mientras renombras
        }

        // 2. SEGUNDA PRIORIDAD: Borrar o Guardar texto en el lienzo
        if (escribiendoTexto && this.tSel != null) {
            if (keyCode == 259) { // Tecla Backspace (Borrar)
                String cont = this.tSel.contenido;
                if (!cont.isEmpty()) {
                    this.tSel.contenido = cont.substring(0, cont.length() - 1);
                    questgrupo.questmod.client.GlobalGuiSettings.sincronizarCapas();
                }
                return true;
            }
            if (keyCode == 257 || keyCode == 256) { // Tecla Enter o Escape (Guardar)
                escribiendoTexto = false;
                return true;
            }
            return true;
        }

        // 3. TERCERA PRIORIDAD: Borrar o Guardar texto asociado a un PANEL
        if (escribiendoTextoPanel && this.pSel != null) {
            if (keyCode == 259) { // Tecla Backspace (Borrar)
                String cont = this.pSel.textoAsociado;
                if (cont != null && !cont.isEmpty()) {
                    this.pSel.textoAsociado = cont.substring(0, cont.length() - 1);
                    questgrupo.questmod.client.GlobalGuiSettings.sincronizarCapas();
                }
                return true;
            }
            if (keyCode == 257 || keyCode == 256) { // Tecla Enter o Escape (Guardar)
                escribiendoTextoPanel = false;
                return true;
            }
            return true;
        }

        // ── ABAJO DE ESTA LÍNEA DEBEN SEGUIR TUS ATAJOS EXISTENTES DEL EDITOR ──
        
        // Atajos del editor (O = Editor ON, P = Editor OFF)
        if (!inputColor.isFocused() && !escribiendoTexto && !escribiendoTextoPanel) {
            if (keyCode == InputConstants.KEY_O) {
                GlobalGuiSettings.editorActivo = true;
                LeftSidebar.sidebarVisible = true;
                this.init();
                return true;
            }
            if (keyCode == InputConstants.KEY_P) {
                GlobalGuiSettings.editorActivo = false;
                LeftSidebar.sidebarVisible = false;
                this.init();
                return true;
            }
        }

        // Manejo de input de color
        if (inputColor.isFocused()) {
            if (keyCode == InputConstants.KEY_ESCAPE || keyCode == InputConstants.KEY_RETURN || keyCode == InputConstants.KEY_NUMPADENTER) {
                inputColor.setFocused(false);
                inputColor.visible = false;
                editandoColorHerramientas = false;
                btnAceptarColor.visible = false;
                btnCancelarColor.visible = false;
                return true;
            }
            if (inputColor.keyPressed(keyCode, scanCode, modifiers)) return true;
        }

        // Atajos de teclado personalizados
        if (KeyboardShortcuts.handleKeyPress(keyCode, modifiers, tSel)) return true;

        // Tecla Delete para eliminar objetos
        if (keyCode == InputConstants.KEY_DELETE) {
            if (pSel != null && pSel.tipo.equals("BOTON_PAGINA")) return true;
            if (tSel != null) { TextoEdit.eliminarTexto(tSel); tSel = null; this.init(); return true; }
            if (pSel != null) { FigurasEdit.eliminarFigura(pSel); pSel = null; this.init(); return true; }
        }
        
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void vincularMisionADetalle(String nombreMision) {
        GlobalGuiSettings.misionSeleccionadaGlobal = nombreMision;
    }

    @Override public boolean isPauseScreen() { return false; }

    private void drawRoundedRect(GuiGraphics g, int x, int y, int width, int height, int color) {
        g.fill(x + 2, y, x + width - 2, y + height, color); // Centro
        g.fill(x, y + 2, x + width, y + height - 2, color); // Lados
        g.fill(x + 1, y + 1, x + 2, y + 2, color); // Esq. Sup Izq
        g.fill(x + width - 2, y + 1, x + width - 1, y + 2, color); // Esq. Sup Der
        g.fill(x + 1, y + height - 2, x + 2, y + height - 1, color); // Esq. Inf Izq
        g.fill(x + width - 2, y + height - 2, x + width - 1, y + height - 1, color); // Esq. Inf Der
    }

    private void drawModernButton(GuiGraphics g, Font font, int x, int y, int width, int height, String text, boolean isHovered) {
        // 1. Borde exterior (gris que se oscurece al pasar el ratón)
        drawRoundedRect(g, x - 1, y - 1, width + 2, height + 2, isHovered ? 0xFF888888 : 0xFFAAAAAA);
        
        // 2. Anillo interior blanco brillante (Igual que las texturas)
        drawRoundedRect(g, x, y, width, height, 0xFFFFFFFF);
        
        // 3. Fondo Base (Mismo color que el panel: C6C6C6)
        int colorFondo = isHovered ? 0xFFBDBDBD : 0xFFC6C6C6;
        drawRoundedRect(g, x + 1, y + 1, width - 2, height - 2, colorFondo);
        
        // 4. Texto centrado (Le quitamos el hundimiento para que coincida con el estilo plano)
        int textWidth = font.width(text);
        g.drawString(font, text, x + (width - textWidth) / 2, y + (height - font.lineHeight) / 2 + 1, 0xFF202020, false);
    }
}
