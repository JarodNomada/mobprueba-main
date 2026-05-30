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
import questgrupo.questmod.client.DialogueColors;
import questgrupo.questmod.client.GlobalGuiSettings;
import questgrupo.questmod.client.InterfaceManager;
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

    // -- MODAL GESTOR DE GUIs --
    private boolean mostrarModalGestorGuis = false;
    private java.util.List<String> listaGuisDisponibles = new java.util.ArrayList<>();
    private String selectedGuiForLoading = null;
    private boolean mostrarPopupGuardar = false;
    private net.minecraft.client.gui.components.EditBox inputGuardarNombre;
    private Button btnGuardarConfirmar;
    private Button btnGuardarCancelar;

    // -- MODO EDITOR DE INTERFACE --
    private boolean modoInterface = false;
    private boolean savedTopBarVisible = false;
    private String interfazColorTarget = null;
    private int savedColorHerramientas = 0;
    private boolean mostrarPopupGuardarInterface = false;
    private net.minecraft.client.gui.components.EditBox inputNomInterface;
    private Button btnGuardarInterface;
    private int selectedTab = 0;
    private int interfazColorFocused = -1;
    private boolean mostrarPopupImportarInterface = false;
    private java.util.List<String> listaInterfaces = new java.util.ArrayList<>();
    private String selectedImportName = null;
    private boolean modoSeleccionElemento = false;
    private int dialogRX, dialogRY, dialogW, dialogH;

    private static final int HEADER_H = 40;
    private static final int TAB_BAR_H = 25;
    private static final int TAB_BTN_W = 90;
    private static final int ROW_H = 28;
    private static final int ROW_SWATCH = 18;
    private static final int TIP_H = 20;
    private static final int RIGHT_PANEL_W = 210;

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

        // Carga automática del layout seleccionado
        questgrupo.questmod.client.GuiLayoutManager.cargarLayoutAutomatico();

        // 4. ELEMENTOS PARA EL POPUP GUARDAR DEL GESTOR DE GUIs
        int ppX = (this.width - 160) / 2, ppY = (this.height - 90) / 2;

        this.inputGuardarNombre = new net.minecraft.client.gui.components.EditBox(this.font, ppX + 12, ppY + 24, 136, 12, net.minecraft.network.chat.Component.literal("Nombre")) {
            @Override
            public void render(net.minecraft.client.gui.GuiGraphics g, int mx, int my, float pt) {
                if (!isVisible()) return;
                String txt = font.plainSubstrByWidth(getValue(), getInnerWidth());
                int tx = getX();
                int ty = getY() + (getHeight() - 8) / 2;
                g.drawString(font, txt, tx, ty, 0xFFFFFFFF, false);
            }
        };
        this.inputGuardarNombre.setMaxLength(30);
        this.inputGuardarNombre.setBordered(false);
        this.inputGuardarNombre.setTextColor(0xFFFFFFFF);
        this.inputGuardarNombre.visible = false;
        this.addRenderableWidget(this.inputGuardarNombre);

        this.btnGuardarConfirmar = Button.builder(net.minecraft.network.chat.Component.literal("Aceptar"), b -> {
            String nombre = this.inputGuardarNombre.getValue().trim();
            if (!nombre.isEmpty()) {
                questgrupo.questmod.client.GuiLayoutManager.guardarLayout(nombre);
                this.listaGuisDisponibles = questgrupo.questmod.client.GuiLayoutManager.obtenerListaLayouts();
                this.selectedGuiForLoading = nombre;
            }
            this.mostrarPopupGuardar = false;
            this.inputGuardarNombre.visible = false;
            this.btnGuardarConfirmar.visible = false;
        }).bounds(ppX + 50, ppY + 48, 60, 20).build();
        this.btnGuardarConfirmar.visible = false;
        this.addRenderableWidget(this.btnGuardarConfirmar);

        // 5. ELEMENTOS PARA EL POPUP GUARDAR INTERFACE
        int ipX = (this.width - 200) / 2, ipY = (this.height - 90) / 2;
        this.inputNomInterface = new net.minecraft.client.gui.components.EditBox(this.font, ipX + 12, ipY + 24, 176, 12, net.minecraft.network.chat.Component.literal("Nombre")) {
            @Override
            public void render(net.minecraft.client.gui.GuiGraphics g, int mx, int my, float pt) {
                if (!isVisible()) return;
                String txt = font.plainSubstrByWidth(getValue(), getInnerWidth());
                g.drawString(font, txt, getX(), getY() + (getHeight() - 8) / 2, 0xFFFFFFFF, false);
            }
        };
        this.inputNomInterface.setMaxLength(30);
        this.inputNomInterface.setBordered(false);
        this.inputNomInterface.setTextColor(0xFFFFFFFF);
        this.inputNomInterface.visible = false;
        this.addRenderableWidget(this.inputNomInterface);

        this.btnGuardarInterface = Button.builder(net.minecraft.network.chat.Component.literal("Aceptar"), b -> {
            String nombre = this.inputNomInterface.getValue().trim();
            if (!nombre.isEmpty()) {
                questgrupo.questmod.client.InterfaceManager.guardarColoresInterface(nombre);
            }
            this.mostrarPopupGuardarInterface = false;
            this.inputNomInterface.visible = false;
            this.btnGuardarInterface.visible = false;
        }).bounds(ipX + 70, ipY + 48, 60, 20).build();
        this.btnGuardarInterface.visible = false;
        this.addRenderableWidget(this.btnGuardarInterface);

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
            // NUEVO: Inyectar botones B+/B- en la sección de herramientas de Texto para misiones (Fila de 2)
            if (pSel != null && (pSel.tipo.equals("MISION_TITULO") || pSel.tipo.equals("MISION_DESCRIPCION"))) {
                int barStartX = LeftSidebar.getSidebarWidth() + (this.width - LeftSidebar.getSidebarWidth() - TopBar.calculateBarWidth(true, false, tSel, pSel)) / 2;
                int curX = barStartX + 144 + 9; // Espacio herramientas(144) + Línea separadora(9)
                this.addRenderableWidget(Button.builder(Component.literal("B+"), b -> pSel.redondezBorde = Math.min(5, pSel.redondezBorde + 1)).bounds(curX, buttonY + 3, 18, 18).build());
                this.addRenderableWidget(Button.builder(Component.literal("B-"), b -> pSel.redondezBorde = Math.max(0, pSel.redondezBorde - 1)).bounds(curX, buttonY + 23, 18, 18).build());
            }
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
        if (interfazColorTarget != null) {
            if (TopBar.colorPickerVisible) {
                DialogueColors.apply(interfazColorTarget, GlobalGuiSettings.colorHerramientas);
            } else {
                DialogueColors.apply(interfazColorTarget, GlobalGuiSettings.colorHerramientas);
                GlobalGuiSettings.colorHerramientas = savedColorHerramientas;
                interfazColorTarget = null;
            }
        }

        if (modoInterface) {
            Font font = this.font;
            renderInterfaceEditor(g, mx, my);

            // --- POPUP GUARDAR INTERFACE ---
            if (mostrarPopupGuardarInterface) {
                int epW = 200, epH = 90;
                int epX = (this.width - epW) / 2, epY = (this.height - epH) / 2;
                g.fill(0, 0, this.width, this.height, 0x44000000);
                drawRoundedRect(g, epX - 1, epY - 1, epW + 2, epH + 2, 0xFF555555);
                drawRoundedRect(g, epX, epY, epW, epH, 0xFFC6C6C6);
                g.drawString(font, "Nombre de la Interface:", epX + 10, epY + 8, 0xFF202020, false);
                g.fill(epX + 9, epY + 21, epX + epW - 9, epY + 39, 0xFF333333);
                g.renderOutline(epX + 9, epY + 21, epW - 18, 18, 0xFF000000);
                this.inputNomInterface.visible = true;
                this.btnGuardarInterface.visible = true;
            } else {
                this.inputNomInterface.visible = false;
                this.btnGuardarInterface.visible = false;
            }

            super.render(g, mx, my, pt);
            return;
        }

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

        // --- MODAL GESTOR DE GUIs ---
        if (mostrarModalGestorGuis) {
            int mgW = 260, mgH = 220;
            int mgX = (this.width - mgW) / 2, mgY = (this.height - mgH) / 2;
            g.fill(0, 0, this.width, this.height, 0x66000000);

            drawRoundedRect(g, mgX - 1, mgY - 1, mgW + 2, mgH + 2, 0xFF555555);
            drawRoundedRect(g, mgX, mgY, mgW, mgH, 0xFFC6C6C6);

            String titulo = "Gestor GUI";
            g.drawString(font, titulo, mgX + (mgW - font.width(titulo))/2, mgY + 15, 0xFF202020, false);

            boolean hoverX = mx >= mgX + mgW - 20 && mx <= mgX + mgW - 6 && my >= mgY + 4 && my <= mgY + 18;
            g.drawString(font, "X", mgX + mgW - 16, mgY + 14, hoverX ? 0xFF000000 : 0xFF555555, false);

            int btnY = mgY + 30;
            boolean hoverImportar = mx >= mgX + 30 && mx <= mgX + 90 && my >= btnY && my <= btnY + 18;
            boolean hoverGuardar = mx >= mgX + 100 && mx <= mgX + 160 && my >= btnY && my <= btnY + 18;
            boolean hoverEliminar = mx >= mgX + 170 && mx <= mgX + 230 && my >= btnY && my <= btnY + 18;
            LeftSidebar.drawVanillaButtonHover(g, font, mgX + 30, btnY, 60, 18, "Importar", hoverImportar);
            LeftSidebar.drawVanillaButtonHover(g, font, mgX + 100, btnY, 60, 18, "Guardar", hoverGuardar);
            LeftSidebar.drawVanillaButtonHover(g, font, mgX + 170, btnY, 60, 18, "Eliminar", hoverEliminar);

            int listY = btnY + 25;
            int listH = 115;

            g.fill(mgX + 10, listY, mgX + mgW - 10, listY + listH, 0xFF1E1E1E);

            int itemH = 20;
            int maxVisible = listH / itemH;
            // Fase 1: highlights y texto
            for (int i = 0; i < this.listaGuisDisponibles.size() && i < maxVisible; i++) {
                String guiName = this.listaGuisDisponibles.get(i);
                int itemY = listY + i * itemH;
                boolean isActive = questgrupo.questmod.client.GuiLayoutManager.layoutSeleccionado.equals(guiName);
                boolean isSel = guiName.equals(this.selectedGuiForLoading);

                if (isActive) {
                    g.fill(mgX + 11, itemY, mgX + mgW - 11, itemY + itemH, 0xFF00A000);
                } else if (isSel) {
                    g.fill(mgX + 11, itemY, mgX + mgW - 11, itemY + itemH, 0xFF0055FF);
                }

                g.drawString(font, guiName, mgX + 16, itemY + 6, isActive || isSel ? 0xFFFFFFFF : 0xFFAAAAAA, false);
            }
            // Fase 2: separadores (después de highlights)
            for (int i = 0; i < this.listaGuisDisponibles.size() - 1 && i < maxVisible - 1; i++) {
                int itemY = listY + (i + 1) * itemH;
                g.fill(mgX + 11, itemY, mgX + mgW - 11, itemY + 1, 0xFF000000);
            }
            // Fase 3: outline negro (AL FINAL, sobre todo)
            g.renderOutline(mgX + 10, listY, mgW - 20, listH, 0xFF000000);

            int bottomY = mgY + mgH - 30;
            boolean hoverCargar = mx >= mgX + 50 && mx <= mgX + 120 && my >= bottomY && my <= bottomY + 20;
            boolean hoverCancelar = mx >= mgX + 140 && mx <= mgX + 210 && my >= bottomY && my <= bottomY + 20;
            LeftSidebar.drawVanillaButtonHover(g, font, mgX + 50, bottomY, 70, 20, "Cargar", hoverCargar);
            LeftSidebar.drawVanillaButtonHover(g, font, mgX + 140, bottomY, 70, 20, "Cancelar", hoverCancelar);
        }

        // --- POPUP GUARDAR (sobre el modal) ---
        if (mostrarPopupGuardar) {
            int ppW = 160, ppH = 90;
            int ppX = (this.width - ppW) / 2, ppY = (this.height - ppH) / 2;
            g.fill(0, 0, this.width, this.height, 0x44000000);
            drawRoundedRect(g, ppX - 1, ppY - 1, ppW + 2, ppH + 2, 0xFF555555);
            drawRoundedRect(g, ppX, ppY, ppW, ppH, 0xFFC6C6C6);
            g.drawString(font, "Guardar como:", ppX + 10, ppY + 8, 0xFF202020, false);
            g.fill(ppX + 9, ppY + 21, ppX + ppW - 9, ppY + 39, 0xFF333333);
            g.renderOutline(ppX + 9, ppY + 21, ppW - 18, 18, 0xFF000000);
            this.inputGuardarNombre.visible = true;
            this.btnGuardarConfirmar.visible = true;
        } else {
            this.inputGuardarNombre.visible = false;
            this.btnGuardarConfirmar.visible = false;
        }

        // --- POPUP GUARDAR INTERFACE ---
        if (mostrarPopupGuardarInterface) {
            int epW = 200, epH = 90;
            int epX = (this.width - epW) / 2, epY = (this.height - epH) / 2;
            g.fill(0, 0, this.width, this.height, 0x44000000);
            drawRoundedRect(g, epX - 1, epY - 1, epW + 2, epH + 2, 0xFF555555);
            drawRoundedRect(g, epX, epY, epW, epH, 0xFFC6C6C6);
            g.drawString(font, "Nombre de la Interface:", epX + 10, epY + 8, 0xFF202020, false);
            g.fill(epX + 9, epY + 21, epX + epW - 9, epY + 39, 0xFF333333);
            g.renderOutline(epX + 9, epY + 21, epW - 18, 18, 0xFF000000);
            this.inputNomInterface.visible = true;
            this.btnGuardarInterface.visible = true;
        } else {
            this.inputNomInterface.visible = false;
            this.btnGuardarInterface.visible = false;
        }

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

    // ─── EDITOR DE INTERFACE (MODO INTERFAZ) ───
    private static final String[] TAB_LABELS = {"INTERFAZ", "BOTONES", "HOVER"};

    private static class ColorEntry {
        String label, target;
        int tab;
        ColorEntry(String label, String target, int tab) {
            this.label = label; this.target = target; this.tab = tab;
        }
    }

    private static final ColorEntry[] COLORES = {
        new ColorEntry("Fondo Exterior", "outerBg", 0),
        new ColorEntry("Fondo Principal", "mainBg", 0),
        new ColorEntry("Trazo Exterior", "trazoExterior", 0),
        new ColorEntry("Trazo Recuadro", "trazoRecuadro", 0),
        new ColorEntry("Trazo Interior", "trazoInterior", 0),
        new ColorEntry("Separador", "separator", 0),
        new ColorEntry("Sombra Separador", "separatorSombra", 0),
        new ColorEntry("Texto NPC", "textNPC", 0),
        new ColorEntry("Boton Fondo", "btnBgNorm", 1),
        new ColorEntry("Boton Claro", "btnLight", 1),
        new ColorEntry("Boton Oscuro", "btnDark", 1),
        new ColorEntry("Boton Texto", "btnText", 1),
        new ColorEntry("Boton Contorno", "btnOutline", 1),
        new ColorEntry("Hover Fondo", "hoverGreen", 2),
        new ColorEntry("Hover Claro", "hoverLight", 2),
        new ColorEntry("Hover Oscuro", "hoverDark", 2),
    };

    private static final int SWATCH_SIZE = 18;

    private void renderInterfaceEditor(GuiGraphics g, int mx, int my) {
        renderInterfaceHeader(g, mx, my);
        renderInterfaceMain(g, mx, my);

        // 5px gap between preview area and bottom container
        int bm = calcBottomStart();
        g.fill(8, bm - 5, this.width - 8, bm, 0xFF1E1E1E);

        renderInterfaceBottom(g, mx, my);

        // ─── Preview area borders (left, right, bottom) ───
        g.fill(8, HEADER_H, 9, bm - 4, 0xFF949494);
        g.fill(this.width - 9, HEADER_H, this.width - 8, bm - 4, 0xFF949494);
        g.fill(8, bm - 5, this.width - 8, bm - 4, 0xFF949494);

        // 8px side bars
        g.fill(0, 0, 8, this.height, 0xFF1E1E1E);
        g.fill(this.width - 8, 0, this.width, this.height, 0xFF1E1E1E);

        if (mostrarPopupImportarInterface) {
            renderImportPopup(g, mx, my);
        }

        // ─── COLOR PICKER ───
        if (TopBar.colorPickerVisible) {
            g.fill(0, 0, this.width, this.height, 0x44000000);
            TopBar.render(g, this.width, 5, null, null);
        }
    }

    private void renderInterfaceHeader(GuiGraphics g, int mx, int my) {
        Font font = this.font;
        g.fill(0, 0, this.width, HEADER_H, 0xFF1E1E1E);
        g.fill(0, HEADER_H - 1, this.width, HEADER_H, 0xFF949494);

        g.drawString(font, "§lEDITOR DE INTERFAZ", 8, (HEADER_H - 8) / 2, 0xFFCCCCCC, false);

        int btnW = 76, btnH = 26, btnY = (HEADER_H - btnH) / 2;
        int gap = 6;
        int rightEdge = this.width - 10;
        int exportX = rightEdge - btnW;
        int importX = exportX - btnW - gap;

        // EXPORTAR
        boolean hovExport = mx >= exportX && mx <= exportX + btnW && my >= btnY && my <= btnY + btnH;
        g.fill(exportX, btnY, exportX + btnW, btnY + btnH, hovExport ? 0xCC4A4A4A : 0xCC333333);
        g.renderOutline(exportX, btnY, btnW, btnH, 0xFF555555);
        g.drawString(font, "§lEXPORTAR", exportX + (btnW - font.width("EXPORTAR")) / 2, btnY + (btnH - 8) / 2, 0xFFCCCCCC, false);

        // IMPORTAR
        boolean hovImport = mx >= importX && mx <= importX + btnW && my >= btnY && my <= btnY + btnH;
        g.fill(importX, btnY, importX + btnW, btnY + btnH, hovImport ? 0xCC4A4A4A : 0xCC333333);
        g.renderOutline(importX, btnY, btnW, btnH, 0xFF555555);
        g.drawString(font, "§lIMPORTAR", importX + (btnW - font.width("IMPORTAR")) / 2, btnY + (btnH - 8) / 2, 0xFFCCCCCC, false);
    }

    private void renderInterfaceMain(GuiGraphics g, int mx, int my) {
        int bottomStart = calcBottomStart();

        g.fill(0, HEADER_H, this.width, bottomStart, 0xFF2D2D2D);

        int boxW = 250, boxH = 90;
        int rx = (this.width - boxW) / 2;
        int ry = HEADER_H + (bottomStart - HEADER_H - boxH) / 2;
        renderPreviewDialogoAt(g, rx, ry, boxW, boxH);

        // Save dialog coordinates for mouse hit-testing
        this.dialogRX = rx; this.dialogRY = ry;
        this.dialogW = boxW; this.dialogH = boxH;
    }

    private void renderInterfaceBottom(GuiGraphics g, int mx, int my) {
        Font font = this.font;
        int bottomStart = calcBottomStart();
        int lastRowY = bottomStart + TAB_BAR_H;
        int contentEndY = this.height - TIP_H;
        int vistaW = 130;

        // Container background with border
        g.fill(8, bottomStart, this.width - 8, contentEndY, 0xFF303030);
        g.fill(8, bottomStart, this.width - 8, bottomStart + 1, 0xFF696969);
        g.fill(8, contentEndY - 1, this.width - 8, contentEndY, 0xFF696969);
        g.fill(8, bottomStart, 9, contentEndY, 0xFF696969);
        g.fill(this.width - 9, bottomStart, this.width - 8, contentEndY, 0xFF696969);

        int tabY = bottomStart;
        int tabH = TAB_BAR_H;
        int tabStartX = 8;
        int tabsEndX = tabStartX + TAB_LABELS.length * TAB_BTN_W;

        // Tab backgrounds (no hover glow)
        for (int i = 0; i < TAB_LABELS.length; i++) {
            int tx = tabStartX + i * TAB_BTN_W;
            int cBg = (i == selectedTab) ? 0xFF3A3A3A : 0xFF1E1E1E;
            g.fill(tx, tabY, tx + TAB_BTN_W, tabY + tabH, cBg);
        }

        // Outer border (#696969)
        int bc = 0xFF696969;
        int aStart = tabStartX + selectedTab * TAB_BTN_W;
        g.fill(tabStartX, tabY, tabsEndX, tabY + 1, bc);
        g.fill(tabStartX, tabY, tabStartX + 1, tabY + tabH, bc);
        g.fill(tabsEndX - 1, tabY, tabsEndX, tabY + tabH, bc);
        g.fill(tabsEndX - 1, tabY + tabH - 1, this.width - 8, tabY + tabH, 0xFF696969);

        // Internal vertical dividers (1px between tabs, no doubling)
        for (int i = 0; i < TAB_LABELS.length - 1; i++) {
            int divX = tabStartX + (i + 1) * TAB_BTN_W;
            g.fill(divX - 1, tabY, divX, tabY + tabH, bc);
        }

        // Active tab: green top + left boundary
        g.fill(aStart, tabY, aStart + TAB_BTN_W, tabY + 1, 0xFF40990B);
        if (selectedTab == 0) {
            g.fill(tabStartX, tabY, tabStartX + 1, tabY + tabH, 0xFF40990B);
        } else {
            int divX = tabStartX + selectedTab * TAB_BTN_W;
            g.fill(divX - 1, tabY, divX, tabY + tabH, 0xFF40990B);
        }

        // Tab labels
        for (int i = 0; i < TAB_LABELS.length; i++) {
            int tx = tabStartX + i * TAB_BTN_W;
            boolean active = i == selectedTab;
            g.drawString(font, "§l" + TAB_LABELS[i], tx + (TAB_BTN_W - font.width(TAB_LABELS[i])) / 2, tabY + (tabH - 8) / 2, active ? 0xFFFFFFFF : 0xFF888888, false);
        }

        // ─── 3-COLUMN COLOR GRID ───
        int gridX = 17;
        int gridY = lastRowY + 8;
        int gridW = this.width - vistaW - gridX * 2 - 4;
        int cols = 3;
        int cellSlot = gridW / cols;
        int cellW = cellSlot - 2;
        int BORDER_GRAY = 0xFF696969;

        int itemIdx = 0;
        for (int idx = 0; idx < COLORES.length; idx++) {
            ColorEntry ce = COLORES[idx];
            if (ce.tab != selectedTab) continue;

            int col = itemIdx % cols;
            int row = itemIdx / cols;
            int cx = gridX + col * cellSlot;
            int cy = gridY + row * ROW_H;

            int currentColor = DialogueColors.get(ce.target);
            boolean focused = idx == interfazColorFocused;
            boolean hovered = mx >= cx && mx <= cx + cellW - 2 && my >= cy && my <= cy + ROW_H;
            int rowBg = focused ? 0xFF2A3A2A : (hovered ? 0xFF333333 : 0xFF1A1A1A);
            g.fill(cx, cy, cx + cellW - 2, cy + ROW_H, rowBg);
            g.fill(cx, cy, cx + 1, cy + ROW_H, BORDER_GRAY);
            g.fill(cx + cellW - 3, cy, cx + cellW - 2, cy + ROW_H, BORDER_GRAY);
            if (row == 0) g.fill(cx, cy, cx + cellW - 2, cy + 1, BORDER_GRAY);
            g.fill(cx, cy + ROW_H - 1, cx + cellW - 2, cy + ROW_H, BORDER_GRAY);

            int swY = cy + (ROW_H - SWATCH_SIZE) / 2;
            g.fill(cx + 6, swY, cx + 6 + SWATCH_SIZE, swY + SWATCH_SIZE, currentColor);
            g.renderOutline(cx + 5, swY - 1, SWATCH_SIZE + 2, SWATCH_SIZE + 2, focused ? 0xFF4A792A : 0xFF555555);

            int textX = cx + 6 + SWATCH_SIZE + 6;
            int maxLabelW = cellW - SWATCH_SIZE - 22;
            String shortLabel = font.width(ce.label) > maxLabelW ? ce.label.substring(0, Math.min(ce.label.length(), 10)) + ".." : ce.label;
            g.drawString(font, shortLabel, textX, cy + (ROW_H - 8) / 2, 0xFFCCCCCC, false);

            itemIdx++;
        }

        // ─── VISTA PREVIA (bottom-right corner) ───
        int vistaX = this.width - vistaW - 14;
        int vistaY = lastRowY + 8;
        int vistaH = contentEndY - vistaY - 6;

        g.fill(vistaX, vistaY, vistaX + vistaW, vistaY + vistaH, 0xFF1A1A1A);
        g.renderOutline(vistaX, vistaY, vistaW, vistaH, 0xFF696969);

        int centerX = vistaX + vistaW / 2;

        g.drawString(font, "§lVISTA PREVIA", vistaX + 6, vistaY + 6, 0xFFCCCCCC, false);

        String[] vpLines = {
            "Aqui puedes ver",
            "el dialogo con",
            "los colores",
            "actuales en",
            "tiempo real."
        };
        int vpTy = vistaY + 22;
        for (String line : vpLines) {
            g.drawString(font, line, vistaX + 6, vpTy, 0xFFAAAAAA, false);
            vpTy += 10;
        }

        int divY = vpTy + 3;
        g.fill(vistaX + 6, divY, vistaX + vistaW - 6, divY + 1, 0xFF696969);

        int btnSelY = divY + 6;
        boolean hovSel = mx >= vistaX + 6 && mx <= vistaX + vistaW - 6 && my >= btnSelY && my <= btnSelY + 22;
        g.fill(vistaX + 6, btnSelY, vistaX + vistaW - 6, btnSelY + 22, hovSel || modoSeleccionElemento ? 0xCC4A792A : 0xCC333333);
        g.renderOutline(vistaX + 6, btnSelY, vistaW - 12, 22, modoSeleccionElemento ? 0xFF6A9A4A : 0xFF555555);
        String btnLabel = modoSeleccionElemento ? "§lClick en dialogo..." : "Seleccionar elemento";
        g.drawString(font, btnLabel, centerX - font.width(btnLabel) / 2, btnSelY + 7, modoSeleccionElemento ? 0xFFFFFF88 : 0xFFCCCCCC, false);

        // Tip
        g.fill(0, this.height - TIP_H, this.width, this.height, 0xFF1E1E1E);
        g.fill(0, this.height - TIP_H, this.width, this.height - TIP_H + 1, 0xFF949494);
        g.drawString(font, "§eUsa colores y contrastes similares a Minecraft para mantener la armonia visual.", 10, this.height - TIP_H + 6, 0xFFFFCC00, false);
    }

    private void renderImportPopup(GuiGraphics g, int mx, int my) {
        Font font = this.font;
        int pw = 240, ph = 200;
        int px = (this.width - pw) / 2, py = (this.height - ph) / 2;

        g.fill(0, 0, this.width, this.height, 0x66000000);
        g.fill(px, py, px + pw, py + ph, 0xFF1E1E1E);
        g.renderOutline(px, py, pw, ph, 0xFF555555);

        g.drawString(font, "§lImportar Interface", px + 10, py + 8, 0xFFCCCCCC, false);
        g.fill(px + 6, py + 20, px + pw - 6, py + 21, 0xFF444444);

        if (listaInterfaces.isEmpty()) {
            g.drawString(font, "No hay interfaces guardadas.", px + 10, py + 40, 0xFF888888, false);
        } else {
            int iy = py + 30;
            for (String name : listaInterfaces) {
                boolean hov = mx >= px + 6 && mx <= px + pw - 6 && my >= iy && my <= iy + 20;
                g.fill(px + 6, iy, px + pw - 6, iy + 20, hov ? 0xFF3A3A3A : 0xFF252525);
                if (hov) g.renderOutline(px + 6, iy, pw - 12, 20, 0xFF555555);
                g.drawString(font, name, px + 12, iy + 6, 0xFFAAAAAA, false);
                iy += 22;
            }
        }
    }

    private int calcBottomStart() {
        int count = 0;
        for (ColorEntry ce : COLORES) {
            if (ce.tab == selectedTab) count++;
        }
        int rows = (count + 2) / 3; // ceil(count / 3)
        int minVistaH = 120;
        int gridH = Math.max(rows * ROW_H + 16, minVistaH);
        return this.height - TIP_H - TAB_BAR_H - gridH;
    }

    private void renderPreviewDialogoAt(GuiGraphics g, int rx, int ry, int boxW, int boxH) {

        int outX = rx - 4; int outY = ry - 4; int outW = boxW + 8; int outH = boxH + 8;
        g.fill(outX + 4, outY + 4, outX + outW + 4, outY + outH + 4, 0x55000000);
        g.fill(outX + 3, outY, outX + outW - 3, outY + outH, DialogueColors.outerBg);
        g.fill(outX, outY + 3, outX + outW, outY + outH - 3, DialogueColors.outerBg);
        g.fill(outX + 1, outY + 1, outX + outW - 1, outY + outH - 1, DialogueColors.outerBg);
        g.fill(outX + 2, outY + 2, outX + outW - 2, outY + outH - 2, DialogueColors.outerBg);
        g.fill(outX + 4, outY, outX + outW - 3, outY + 1, DialogueColors.trazoExterior);
        g.fill(outX, outY + 4, outX + 1, outY + outH - 3, DialogueColors.trazoExterior);
        g.fill(outX + 2, outY + 1, outX + 4, outY + 2, DialogueColors.trazoExterior);
        g.fill(outX + 1, outY + 2, outX + 2, outY + 4, DialogueColors.trazoExterior);

        int inX = rx; int inY = ry; int inW = boxW; int inH = boxH;
        g.fill(inX + 3, inY - 1, inX + inW - 3, inY + inH + 1, DialogueColors.trazoRecuadro);
        g.fill(inX - 1, inY + 3, inX + inW + 1, inY + inH - 3, DialogueColors.trazoRecuadro);
        g.fill(inX + 1, inY, inX + inW - 1, inY + inH, DialogueColors.trazoRecuadro);
        g.fill(inX, inY + 1, inX + inW, inY + inH - 1, DialogueColors.trazoRecuadro);

        g.fill(inX + 3, inY, inX + inW - 3, inY + inH, DialogueColors.mainBg);
        g.fill(inX, inY + 3, inX + inW, inY + inH - 3, DialogueColors.mainBg);
        g.fill(inX + 1, inY + 1, inX + inW - 1, inY + inH - 1, DialogueColors.mainBg);
        g.fill(inX + 2, inY + 2, inX + inW - 2, inY + inH - 2, DialogueColors.mainBg);

        g.fill(inX + 4, inY, inX + inW - 3, inY + 1, DialogueColors.trazoInterior);
        g.fill(inX, inY + 4, inX + 1, inY + inH - 3, DialogueColors.trazoInterior);
        g.fill(inX + 2, inY + 1, inX + 4, inY + 2, DialogueColors.trazoInterior);
        g.fill(inX + 1, inY + 2, inX + 2, inY + 4, DialogueColors.trazoInterior);

        g.drawString(this.font, "§lAldeano de Prueba", rx + 12, ry + 10, DialogueColors.textNPC, false);

        String previewText = "Hola viajero, necesito que me ayudes con una tarea muy importante...";
        g.drawWordWrap(this.font, Component.literal(previewText), rx + 12, ry + 26, boxW - 24, DialogueColors.textNPC);

        int sepY = ry + 55;
        g.fill(rx + 8, sepY, rx + boxW - 8, sepY + 1, DialogueColors.separator);
        g.fill(rx + 8, sepY + 1, rx + boxW - 8, sepY + 2, DialogueColors.separatorSombra);

        int pad = 12, gap = 6, btnH = 22;
        int halfW = (boxW - pad * 2 - gap) / 2;
        String[] ops = {"Aceptar", "Rechazar"};
        for (int i = 0; i < 2; i++) {
            int bX = rx + pad + i * (halfW + gap);
            int bY = sepY + 8;
            int bW = halfW;
            boolean hov = i == 0;
            g.fill(bX, bY - 1, bX + bW, bY, DialogueColors.btnOutline);
            g.fill(bX, bY + btnH, bX + bW, bY + btnH + 1, DialogueColors.btnOutline);
            g.fill(bX - 1, bY, bX, bY + btnH, DialogueColors.btnOutline);
            g.fill(bX + bW, bY, bX + bW + 1, bY + btnH, DialogueColors.btnOutline);

            int cFondo = hov ? DialogueColors.hoverGreen : DialogueColors.btnBgNorm;
            g.fill(bX, bY, bX + bW, bY + btnH, cFondo);

            int cLight = hov ? DialogueColors.hoverLight : DialogueColors.btnLight;
            g.fill(bX, bY, bX + bW, bY + 1, cLight);
            g.fill(bX, bY, bX + 1, bY + btnH, cLight);

            int cDark = hov ? DialogueColors.hoverDark : DialogueColors.btnDark;
            g.fill(bX, bY + btnH - 2, bX + bW, bY + btnH, cDark);
            g.fill(bX + bW - 1, bY, bX + bW, bY + btnH, cDark);

            g.drawString(this.font, ops[i], bX + (bW - this.font.width(ops[i])) / 2, bY + (btnH - 8) / 2, DialogueColors.btnText, false);
        }
    }

    private boolean mouseClickedInterface(double mx, double my) {
        int bottomStart = calcBottomStart();
        int contentEndY = this.height - TIP_H;
        int vistaW = 130;
        int lastRowY = bottomStart + TAB_BAR_H;

        // ─── HEADER BUTTONS ───
        if (my < HEADER_H) {
            int btnW = 76, btnH = 26, btnY = (HEADER_H - btnH) / 2;
            int gap = 6;
            int rightEdge = this.width - 10;
            int exportX = rightEdge - btnW;
            int importX = exportX - btnW - gap;

            if (mx >= importX && mx <= importX + btnW && my >= btnY && my <= btnY + btnH) {
                listaInterfaces = questgrupo.questmod.client.InterfaceManager.obtenerListaInterfaces();
                mostrarPopupImportarInterface = true;
                selectedImportName = null;
                return true;
            }

            if (mx >= exportX && mx <= exportX + btnW && my >= btnY && my <= btnY + btnH) {
                mostrarPopupGuardarInterface = true;
                this.inputNomInterface.setValue("");
                this.inputNomInterface.setFocused(true);
                return true;
            }
            return false;
        }

        // ─── MAIN PREVIEW AREA (dialog zone detection) ───
        if (my >= HEADER_H && my < bottomStart) {
            if (modoSeleccionElemento && mx >= dialogRX && mx <= dialogRX + dialogW && my >= dialogRY && my <= dialogRY + dialogH) {
                int rx = dialogRX, ry = dialogRY, boxW = dialogW;
                int sepY = ry + 55;
                int pad = 12, gap = 6, btnH = 22;
                int halfW = (boxW - pad * 2 - gap) / 2;
                String target = null;
                int tab = -1;

                if (my >= ry + 10 && my < ry + 24) {
                    target = "textNPC"; tab = 0;
                } else if (my >= ry + 26 && my < ry + 54) {
                    target = "textNPC"; tab = 0;
                } else if (my >= sepY && my < sepY + 2) {
                    target = "separator"; tab = 0;
                } else if (my >= sepY + 8 && my < sepY + 8 + btnH) {
                    if (mx < rx + pad + halfW) {
                        target = "btnBgNorm"; tab = 1;
                    } else {
                        target = "hoverGreen"; tab = 2;
                    }
                }

                if (target != null) {
                    selectedTab = tab;
                    for (int idx = 0; idx < COLORES.length; idx++) {
                        if (COLORES[idx].target.equals(target)) {
                            interfazColorFocused = idx;
                            abrirPickerColor(target);
                            break;
                        }
                    }
                }
                modoSeleccionElemento = false;
                return true;
            }
            // Click in main area but not on dialog → cancel selection mode
            if (modoSeleccionElemento) {
                modoSeleccionElemento = false;
                return true;
            }
            return false;
        }

        // ─── TABS ───
        if (my >= bottomStart && my < lastRowY) {
            int tabY = bottomStart + 4;
            int tabH = TAB_BAR_H - 6;
            int tabStartX = 8;
            for (int i = 0; i < TAB_LABELS.length; i++) {
                if (mx >= tabStartX && mx <= tabStartX + TAB_BTN_W && my >= tabY && my <= tabY + tabH) {
                    selectedTab = i;
                    return true;
                }
                tabStartX += TAB_BTN_W;
            }
            return false;
        }

        // ─── BOTTOM CONTENT AREA ───
        if (my >= lastRowY && my < contentEndY) {

            // VISTA PREVIA button
            int vistaX = this.width - vistaW - 14;
            int vistaY = lastRowY + 8;
            if (mx >= vistaX && mx < vistaX + vistaW) {
                int btnSelY = vistaY + 81;
                if (mx >= vistaX + 6 && mx <= vistaX + vistaW - 6 && my >= btnSelY && my <= btnSelY + 22) {
                    modoSeleccionElemento = !modoSeleccionElemento;
                    return true;
                }
                return false;
            }

            // 3-column color grid
            int gridX = 17;
            int gridY = lastRowY + 8;
            int gridW = this.width - vistaW - gridX * 2 - 4;
            int cols = 3;
            int cellSlot = gridW / cols;
            int cellW = cellSlot - 2;

            int itemIdx = 0;
            for (int idx = 0; idx < COLORES.length; idx++) {
                ColorEntry ce = COLORES[idx];
                if (ce.tab != selectedTab) continue;

                int col = itemIdx % cols;
                int row = itemIdx / cols;
                int cx = gridX + col * cellSlot;
                int cy = gridY + row * ROW_H;

                if (mx >= cx && mx <= cx + cellW - 2 && my >= cy && my <= cy + ROW_H) {
                    interfazColorFocused = idx;
                    abrirPickerColor(ce.target);
                    return true;
                }

                itemIdx++;
            }
            return false;
        }

        return false;
    }

    private void abrirPickerColor(String target) {
        this.interfazColorTarget = target;
        this.savedColorHerramientas = GlobalGuiSettings.colorHerramientas;
        int currentColor = DialogueColors.get(target);
        TopBar.openPicker(currentColor, TopBar.BTN_TOOL_COLOR, null, null);
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
        if (modoInterface) {
            if (TopBar.colorPickerVisible) {
                if (super.mouseClicked(mx, my, btn)) return true;
                if (TopBar.handleModalClick(mx, my)) return true;
                return true;
            }
            if (mostrarPopupImportarInterface) {
                int pw = 240, ph = 200;
                int px = (this.width - pw) / 2, py = (this.height - ph) / 2;
                if (mx < px || mx > px + pw || my < py || my > py + ph) {
                    mostrarPopupImportarInterface = false;
                    return true;
                }
                int iy = py + 30;
                for (String name : listaInterfaces) {
                    if (mx >= px + 6 && mx <= px + pw - 6 && my >= iy && my <= iy + 20) {
                        questgrupo.questmod.client.InterfaceManager.cargarColoresInterface(name);
                        mostrarPopupImportarInterface = false;
                        return true;
                    }
                    iy += 22;
                }
                return true;
            }
            if (mostrarPopupGuardarInterface) {
                int ipW = 200, ipH = 90;
                int ipX = (this.width - ipW) / 2, ipY = (this.height - ipH) / 2;
                if (this.btnGuardarInterface != null && this.btnGuardarInterface.isMouseOver(mx, my)) {
                    this.btnGuardarInterface.mouseClicked(mx, my, btn); return true;
                }
                if (mx >= ipX + 9 && mx <= ipX + ipW - 9 && my >= ipY + 21 && my <= ipY + 39) {
                    this.inputNomInterface.setFocused(true);
                    this.inputNomInterface.mouseClicked(mx, my, btn);
                    return true;
                }
                this.inputNomInterface.setFocused(false);
                mostrarPopupGuardarInterface = false;
                this.inputNomInterface.visible = false;
                this.btnGuardarInterface.visible = false;
                return true;
            }
            return mouseClickedInterface(mx, my);
        }

        if (GlobalGuiSettings.editorActivo && DownBar.handleClick(mx, my, this.width, this.height)) return true;
        if (RightBar.handleClick(mx, my, this.width, this.height)) {
            this.pSel = GlobalGuiSettings.panelSeleccionado;
            this.tSel = GlobalGuiSettings.textoSeleccionado;
            this.init();
            return true;
        }

        // ¡ESTO ES CLAVE!: Si el clic ocurre en el lienzo (fuera de la RightBar), liberamos el teclado
        RightBar.capaEditandoNombre = null;

        // GENERAR LISTAS DINÁMICAS PARA CLICS (Igual que en el renderizado visual)
        java.util.List<String> misionesPrincipalesDyn = new java.util.ArrayList<>();
        java.util.List<String> misionesSecundariasDyn = new java.util.ArrayList<>();
        if (Config.questConfig != null && Config.questConfig.misiones != null) {
            for (Config.MisionData m : Config.questConfig.misiones) {
                if (m.nombre != null) {
                    boolean estaAceptada = false;
                    String mIdSufijo = "_" + m.nombre.replace(" ", "_");
                    for (String questKey : GlobalGuiSettings.misionesAceptadasCliente) {
                        if (questKey.endsWith(mIdSufijo)) {
                            estaAceptada = true;
                            break;
                        }
                    }
                    if (estaAceptada) {
                        if (m.esPrimaria) misionesPrincipalesDyn.add(m.nombre);
                        else misionesSecundariasDyn.add(m.nombre);
                    }
                }
            }
        }

        // --- 1. ESCUDO Y MANEJO DEL MODAL DE COLOR ---
        if (TopBar.colorPickerVisible) {
            if (super.mouseClicked(mx, my, btn)) return true;
            if (TopBar.handleModalClick(mx, my)) return true;
            return true; 
        }

        // --- POPUP GUARDAR (atrapa clics antes que el modal) ---
        if (mostrarPopupGuardar) {
            int ppW = 160, ppH = 90;
            int ppX = (this.width - ppW) / 2, ppY = (this.height - ppH) / 2;

            if (this.btnGuardarConfirmar != null && this.btnGuardarConfirmar.isMouseOver(mx, my)) {
                this.btnGuardarConfirmar.mouseClicked(mx, my, btn);
                return true;
            }
            if (mx >= ppX + 9 && mx <= ppX + ppW - 9 && my >= ppY + 21 && my <= ppY + 39) {
                this.inputGuardarNombre.setFocused(true);
                this.inputGuardarNombre.mouseClicked(mx, my, btn);
                return true;
            } else {
                this.inputGuardarNombre.setFocused(false);
            }
            // Clic fuera → cerrar popup
            mostrarPopupGuardar = false;
            this.inputGuardarNombre.visible = false;
            this.btnGuardarConfirmar.visible = false;
            return true;
        }

        // --- POPUP GUARDAR INTERFACE ---
        if (mostrarPopupGuardarInterface) {
            int ipW = 200, ipH = 90;
            int ipX = (this.width - ipW) / 2, ipY = (this.height - ipH) / 2;

            if (this.btnGuardarInterface != null && this.btnGuardarInterface.isMouseOver(mx, my)) {
                this.btnGuardarInterface.mouseClicked(mx, my, btn);
                return true;
            }
            if (mx >= ipX + 9 && mx <= ipX + ipW - 9 && my >= ipY + 21 && my <= ipY + 39) {
                this.inputNomInterface.setFocused(true);
                this.inputNomInterface.mouseClicked(mx, my, btn);
                return true;
            } else {
                this.inputNomInterface.setFocused(false);
            }
            mostrarPopupGuardarInterface = false;
            this.inputNomInterface.visible = false;
            this.btnGuardarInterface.visible = false;
            return true;
        }

        // --- MANEJO DEL MODAL GESTOR DE GUIs ---
        if (mostrarModalGestorGuis) {
            int mgW = 260, mgH = 220;
            int mgX = (this.width - mgW) / 2, mgY = (this.height - mgH) / 2;

            // Cerrar (X)
            if (mx >= mgX + mgW - 20 && mx <= mgX + mgW - 6 && my >= mgY + 4 && my <= mgY + 18) {
                this.selectedGuiForLoading = null;
                mostrarModalGestorGuis = false;
                return true;
            }

            // Botón Importar — abre la carpeta en el explorador
            if (mx >= mgX + 30 && mx <= mgX + 90 && my >= mgY + 30 && my <= mgY + 48) {
                try {
                    java.io.File dir = questgrupo.questmod.client.GuiLayoutManager.getGuiDir();
                    Runtime.getRuntime().exec("xdg-open " + dir.getAbsolutePath());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            }

            // Botón Guardar — abre popup
            if (mx >= mgX + 100 && mx <= mgX + 160 && my >= mgY + 30 && my <= mgY + 48) {
                this.mostrarPopupGuardar = true;
                this.inputGuardarNombre.setValue("");
                this.inputGuardarNombre.setFocused(true);
                return true;
            }

            // Botón Eliminar
            if (mx >= mgX + 170 && mx <= mgX + 230 && my >= mgY + 30 && my <= mgY + 48) {
                if (this.selectedGuiForLoading != null) {
                    String nombreEliminar = this.selectedGuiForLoading;
                    questgrupo.questmod.client.GuiLayoutManager.eliminarLayout(nombreEliminar);
                    this.listaGuisDisponibles = questgrupo.questmod.client.GuiLayoutManager.obtenerListaLayouts();
                    this.selectedGuiForLoading = null;
                }
                return true;
            }

            // Clic en la lista de items — selecciona
            int listY = mgY + 55;
            int itemH = 20;
            int listH = 115;
            for (int i = 0; i < this.listaGuisDisponibles.size(); i++) {
                int itemY = listY + i * itemH;
                if (my >= itemY && my <= itemY + itemH && mx >= mgX + 10 && mx <= mgX + mgW - 10) {
                    this.selectedGuiForLoading = this.listaGuisDisponibles.get(i);
                    return true;
                }
            }

            // Botón Cargar
            int bottomY = mgY + mgH - 30;
            if (mx >= mgX + 50 && mx <= mgX + 120 && my >= bottomY && my <= bottomY + 20) {
                if (this.selectedGuiForLoading != null) {
                    questgrupo.questmod.client.GuiLayoutManager.cargarLayout(this.selectedGuiForLoading);
                    this.init();
                }
                this.selectedGuiForLoading = null;
                mostrarModalGestorGuis = false;
                return true;
            }

            // Botón Cancelar
            if (mx >= mgX + 140 && mx <= mgX + 210 && my >= bottomY && my <= bottomY + 20) {
                this.selectedGuiForLoading = null;
                mostrarModalGestorGuis = false;
                return true;
            }

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

        // NUEVO: Calculamos el ancho exacto de la barra y su posición real en X
        int topBarW = TopBar.getWidth();
        int barInicioX = barX + (this.width - barX - topBarW) / 2;
        boolean clicEnLaBarra = mx >= barInicioX && mx <= barInicioX + topBarW;

        // AÑADIDO '&& clicEnLaBarra' para que solo bloquee si el clic ocurre DENTRO de la barra visual
        if (TopBar.isVisible() && my >= visualY && my <= visualY + topBarHeight && clicEnLaBarra) {
            
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

            // Si hizo clic en la barra pero no en un color, comprobamos si tocó un botón (+, -).
            if (super.mouseClicked(mx, my, btn)) return true;
            
            // Si llegó hasta aquí, hizo clic en un espacio vacío pero DENTRO de la barra, consumimos el clic.
            return true; 
        }

        // Iniciar dibujo con herramientas de dibujo (solo si no estamos en el panel de Config. Pincel)
        boolean enPanelBrush = LeftSidebar.selectedModule == 1 && LeftSidebar.showBrushThickness;
        boolean tocarBarra = TopBar.isVisible() && my >= visualY && my <= visualY + topBarHeight && clicEnLaBarra;
        
        if (!tocarBarra && mx > barX && !enPanelBrush) {
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
                if (p.pagina != GlobalGuiSettings.paginaActual && p.pagina != 0) continue;

                // --- NUEVO: Hacer que los recuadros creados por el "+" funcionen como botones ---
                if (p.tipo.equals("BOTON_PAGINA")) {
                    if (mx >= p.x && mx <= p.x + p.ancho && my >= p.y && my <= p.y + p.alto) {
                        try {
                            GlobalGuiSettings.paginaActual = Integer.parseInt(p.textoAsociado);
                            this.init();
                        } catch (NumberFormatException ignored) {}
                        return true;
                    }
                }

                // --- NUEVO: Recuperar funcionalidad de las tarjetas rectangulares sueltas ---
                if (p.tipo.equals("RECTANGULO") && p.textoAsociado != null && !p.textoAsociado.isEmpty() && p.iconoRL != null) {
                    if (mx >= p.x && mx <= p.x + p.ancho && my >= p.y && my <= p.y + p.alto) {
                        GlobalGuiSettings.misionSeleccionadaGlobal = p.textoAsociado;
                        return true;
                    }
                }

                if (p.tipo.startsWith("DESPLEGABLE")) {
                    if (p.tipo.equals("DESPLEGABLE_MAESTRO")) {
                        if (mx >= p.x && mx <= p.x + p.ancho && my >= p.y && my <= p.y + p.alto) {
                            int currentY = p.y + 5 - p.scrollY;
                            int marginX = 5;
                            int cardWidth = p.ancho - (marginX * 2);
                            int cardHeight = 30;

                            // 1. Cabecera de Principales
                            if (mx >= p.x + marginX && mx <= p.x + marginX + cardWidth && my >= currentY && my <= currentY + 15) {
                                p.principalesAbierto = !p.principalesAbierto;
                                return true;
                            }
                            currentY += 20;

                            // Misiones Principales
                            if (p.principalesAbierto) {
                                for (String missionName : misionesPrincipalesDyn) {
                                    if (mx >= p.x + marginX && mx <= p.x + marginX + cardWidth && my >= currentY && my <= currentY + cardHeight) {
                                        GlobalGuiSettings.misionSeleccionadaGlobal = missionName;
                                        return true;
                                    }
                                    currentY += cardHeight + 5;
                                }
                            }

                            // 2. Cabecera de Secundarias
                            if (mx >= p.x + marginX && mx <= p.x + marginX + cardWidth && my >= currentY && my <= currentY + 15) {
                                p.secundariasAbierto = !p.secundariasAbierto;
                                return true;
                            }
                            currentY += 20;

                            // Misiones Secundarias
                            if (p.secundariasAbierto) {
                                for (String missionName : misionesSecundariasDyn) {
                                    if (mx >= p.x + marginX && mx <= p.x + marginX + cardWidth && my >= currentY && my <= currentY + cardHeight) {
                                        GlobalGuiSettings.misionSeleccionadaGlobal = missionName;
                                        return true;
                                    }
                                    currentY += cardHeight + 5;
                                }
                            }
                            return true;
                        }
                    } else {
                        int headerHeight = 20;
                        if (mx >= p.x && mx <= p.x + p.ancho && my >= p.y && my <= p.y + headerHeight) {
                            p.desplegado = !p.desplegado;
                            return true;
                        }
                        if (p.desplegado && mx >= p.x && mx <= p.x + p.ancho && my >= p.y + headerHeight && my <= p.y + p.alto) {
                            int bodyY = p.y + headerHeight;
                            int currentY = bodyY + 5 - p.scrollY;
                            int marginX = 5;
                            int cardWidth = p.ancho - (marginX * 2);
                            java.util.List<String> misiones = p.tipo.equals("DESPLEGABLE_PRINCIPAL") ? misionesPrincipalesDyn : misionesSecundariasDyn;
                            for (String s : misiones) {
                                if (mx >= p.x + marginX && mx <= p.x + marginX + cardWidth && my >= currentY && my <= currentY + 30) {
                                    GlobalGuiSettings.misionSeleccionadaGlobal = s;
                                    return true;
                                }
                                currentY += 35;
                            }
                            return true;
                        }
                    }
                }
            }
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
                } else if (LeftSidebar.showGestorGuisRequested) {
                    LeftSidebar.showGestorGuisRequested = false;
                    this.mostrarModalGestorGuis = true;
                    this.selectedGuiForLoading = null;
                    this.mostrarPopupGuardar = false;
                    this.listaGuisDisponibles = questgrupo.questmod.client.GuiLayoutManager.obtenerListaLayouts();
                } else if (LeftSidebar.showInterfaceEditorRequested) {
                    LeftSidebar.showInterfaceEditorRequested = false;
                    this.modoInterface = true;
                    this.modoSeleccionElemento = false;
                    this.selectedTab = 0;
                    this.interfazColorFocused = -1;
                    savedTopBarVisible = TopBar.isVisible();
                    TopBar.setVisible(false);
                    DialogueColors.restaurarDefaults();
                } else {
                    if (GlobalGuiSettings.TEXTOS.size() > textosAnteriores) {
                        this.tSel = GlobalGuiSettings.TEXTOS.get(GlobalGuiSettings.TEXTOS.size() - 1);
                        this.pSel = null;
                    }
                    this.init(); 
                }
                return true;
            }

            // --- NUEVO: Movemos toda la lógica de capas DENTRO de este 'if' ---
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
                                    for (String mName : misionesPrincipalesDyn) {
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
                                    for (String mName : misionesSecundariasDyn) {
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
                                List<String> misiones = p.tipo.equals("DESPLEGABLE_PRINCIPAL") ? misionesPrincipalesDyn : misionesSecundariasDyn;
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
        } // --- FIN DEL IF DE MODO ON ---

        // Deselección al hacer clic en el fondo
        // NUEVO: Verificamos que realmente hayamos tocado la barra visual, no solo el "aire" al lado de ella
        boolean sobreTopBar = TopBar.isVisible() && my >= visualY && my <= visualY + topBarHeight && clicEnLaBarra;

        if (!sobreTopBar && !inputColor.isMouseOver(mx, my)) {
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
        if (modoInterface) {
            return false;
        }

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

        // Escribir en el input del gestor de GUIs
        if (this.inputGuardarNombre != null && this.inputGuardarNombre.visible && this.inputGuardarNombre.isFocused()) {
            return this.inputGuardarNombre.charTyped(codePoint, modifiers);
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
        if (modoInterface) {
            if (keyCode == 256) {
                modoInterface = false;
                TopBar.setVisible(savedTopBarVisible);
                if (TopBar.colorPickerVisible) TopBar.closePicker(false);
                return true;
            }
            if (keyCode == 257 && mostrarPopupGuardarInterface && this.inputNomInterface.isFocused()) {
                String nombre = this.inputNomInterface.getValue().trim();
                if (!nombre.isEmpty()) {
                    InterfaceManager.guardarColoresInterface(nombre);
                }
                mostrarPopupGuardarInterface = false;
                this.inputNomInterface.visible = false;
                this.btnGuardarInterface.visible = false;
                return true;
            }
            return super.keyPressed(keyCode, scanCode, modifiers);
        }

        // PRIORIDAD CERO: Manejar teclado en el buscador de texturas
        if (this.buscadorTexturas != null && this.buscadorTexturas.visible && this.buscadorTexturas.isFocused()) {
            if (keyCode == 256) { this.buscadorTexturas.setFocused(false); return true; } // ESC para soltar el buscador
            return this.buscadorTexturas.keyPressed(keyCode, scanCode, modifiers);
        }

        // Manejar teclado en el input del gestor de GUIs
        if (this.inputGuardarNombre != null && this.inputGuardarNombre.visible && this.inputGuardarNombre.isFocused()) {
            if (keyCode == 256) { this.inputGuardarNombre.setFocused(false); return true; }
            if (keyCode == 257) {
                String nombre = this.inputGuardarNombre.getValue().trim();
                if (!nombre.isEmpty()) {
                    questgrupo.questmod.client.GuiLayoutManager.guardarLayout(nombre);
                    this.listaGuisDisponibles = questgrupo.questmod.client.GuiLayoutManager.obtenerListaLayouts();
                    this.selectedGuiForLoading = nombre;
                }
                this.mostrarPopupGuardar = false;
                this.inputGuardarNombre.visible = false;
                this.btnGuardarConfirmar.visible = false;
                return true;
            }
            return this.inputGuardarNombre.keyPressed(keyCode, scanCode, modifiers);
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
            if (keyCode == InputConstants.KEY_L) {
                questgrupo.questmod.client.editor.RightBar.isVisible = !questgrupo.questmod.client.editor.RightBar.isVisible;
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
