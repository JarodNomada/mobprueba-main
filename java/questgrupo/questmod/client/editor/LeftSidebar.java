package questgrupo.questmod.client.editor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import questgrupo.questmod.client.GlobalGuiSettings;
import questgrupo.questmod.client.gui.FigurasEdit;
import questgrupo.questmod.client.gui.TextoEdit;

public class LeftSidebar {
    public static boolean sidebarVisible = true;
    public static int selectedModule = -1; // -1: none, 0: Texto, 1: Herramientas, 2: Funciones, 3: Widgets RPG
    public static int selectedTool = -1; 
    public static int selectedShape = -1; 
    public static boolean showBrushThickness = false;
    public static boolean showShapesMenu = false;
    public static boolean showInventoryMenu = false;
    public static boolean showNormalMenu = false;
    public static boolean showStatsMenu = false;
    public static boolean showProgresoMenu = false;
    public static boolean editColorRequested = false;
    public static boolean showGaleriaRequested = false;
    public static boolean showTexturasRequested = false;

    public static final int TAB_WIDTH = 26;
    public static final int TAB_HEIGHT = 26;
    public static final int TAB_GAP = 6;
    public static final int START_X = 6;
    public static final int PANEL_X = START_X + TAB_WIDTH + 4;
    public static final int SIDEBAR_WIDTH = PANEL_X;

    public static int colorBoxRectX = 0;
    public static int colorBoxRectY = 0;

    public static int getSidebarY(int screenHeight) {
        // Ahora multiplicamos por 5 botones y 4 espacios
        int totalHeight = 5 * TAB_HEIGHT + 4 * TAB_GAP;
        return (screenHeight - totalHeight) / 2;
    }

    public static int getSidebarWidth() {
        if (!GlobalGuiSettings.editorActivo) return 0;
        return SIDEBAR_WIDTH;
    }

    public static void drawVanillaPanel(GuiGraphics g, int x, int y, int width, int height) {
        g.fill(x + 1, y + 1, x + width - 1, y + height - 1, 0xFFC6C6C6);
        g.fill(x + 1, y, x + width - 1, y + 1, 0xFF000000);
        g.fill(x + 1, y + height - 1, x + width - 1, y + height, 0xFF000000);
        g.fill(x, y + 1, x + 1, y + height - 1, 0xFF000000);
        g.fill(x + width - 1, y + 1, x + width, y + height - 1, 0xFF000000);

        g.fill(x + 1, y + 1, x + width - 1, y + 2, 0xFFFFFFFF);
        g.fill(x + 1, y + 1, x + 2, y + height - 1, 0xFFFFFFFF);
        g.fill(x + 1, y + height - 2, x + width - 1, y + height - 1, 0xFF555555);
        g.fill(x + width - 2, y + 1, x + width - 1, y + height - 1, 0xFF555555);
    }

    public static void drawVanillaButton(GuiGraphics g, Font font, int x, int y, int width, int height, String text, boolean active) {
        int bgColor = active ? 0xFFAAAAAA : 0xFFC6C6C6;

        g.fill(x + 1, y + 1, x + width - 1, y + height - 1, bgColor);
        g.fill(x + 1, y, x + width - 1, y + 1, 0xFF000000);
        g.fill(x + 1, y + height - 1, x + width - 1, y + height, 0xFF000000);
        g.fill(x, y + 1, x + 1, y + height - 1, 0xFF000000);
        g.fill(x + width - 1, y + 1, x + width, y + height - 1, 0xFF000000);

        if (!active) {
            g.fill(x + 1, y + 1, x + width - 2, y + 2, 0xFFFFFFFF);
            g.fill(x + 1, y + 1, x + 2, y + height - 2, 0xFFFFFFFF);
            g.fill(x + 1, y + height - 2, x + width - 1, y + height - 1, 0xFF555555);
            g.fill(x + width - 2, y + 1, x + width - 1, y + height - 1, 0xFF555555);
        } else {
            g.fill(x + 1, y + 1, x + width - 1, y + 2, 0xFF555555);
            g.fill(x + 1, y + 1, x + 2, y + height - 1, 0xFF555555);
        }

        int textWidth = font.width(text);
        int textX = x + (width - textWidth) / 2;
        int textY = y + (height - font.lineHeight) / 2;
        g.drawString(font, text, textX, textY, 0xFF202020, false);
    }

    public static void render(GuiGraphics g, int screenWidth, int screenHeight) {
        if (!sidebarVisible) return;

        Font font = Minecraft.getInstance().font;
        int startY = getSidebarY(screenHeight);

        drawVanillaButton(g, font, START_X, startY, TAB_WIDTH, TAB_HEIGHT, "T", selectedModule == 0);
        drawVanillaButton(g, font, START_X, startY + TAB_HEIGHT + TAB_GAP, TAB_WIDTH, TAB_HEIGHT, "H", selectedModule == 1);
        drawVanillaButton(g, font, START_X, startY + 2 * (TAB_HEIGHT + TAB_GAP), TAB_WIDTH, TAB_HEIGHT, "F", selectedModule == 2);
        // NUEVO BOTÓN: W (Widgets RPG)
        drawVanillaButton(g, font, START_X, startY + 3 * (TAB_HEIGHT + TAB_GAP), TAB_WIDTH, TAB_HEIGHT, "W", selectedModule == 3);
        // NUEVO BOTÓN: Imágenes e Íconos (Renderiza un bloque de tierra real)
        drawVanillaButton(g, font, START_X, startY + 4 * (TAB_HEIGHT + TAB_GAP), TAB_WIDTH, TAB_HEIGHT, "", selectedModule == 4);
        g.renderFakeItem(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.GRASS_BLOCK), START_X + 5, startY + 4 * (TAB_HEIGHT + TAB_GAP) + 5);

        if (selectedModule >= 0) {
            renderSubPanel(g, font, screenWidth, screenHeight);
        }
    }

    private static void renderSubPanel(GuiGraphics g, Font font, int screenWidth, int screenHeight) {
        int panelY = getSidebarY(screenHeight);

        if (selectedModule == 0) {
            drawVanillaPanel(g, PANEL_X, panelY, 120, 45);
            g.drawString(font, "Texto", PANEL_X + 10, panelY + 6, 0xFF404040, false);
            drawVanillaButton(g, font, PANEL_X + 10, panelY + 20, 100, 18, "Nuevo Texto", false);

        } else if (selectedModule == 1) {
            if (showShapesMenu) {
                drawVanillaPanel(g, PANEL_X, panelY, 120, 125);
                g.drawString(font, "Formas", PANEL_X + 10, panelY + 6, 0xFF404040, false);
                String[] formas = {"Cuadrado", "Rect\u00e1ngulo", "Tri\u00e1ngulo", "C\u00edrculo", "Volver"};
                for (int i = 0; i < formas.length; i++) {
                    drawVanillaButton(g, font, PANEL_X + 10, panelY + 20 + (i * 20), 100, 18, formas[i], selectedShape == i);
                }
            } else if (showBrushThickness) {
                drawVanillaPanel(g, PANEL_X, panelY, 120, 85);
                g.drawString(font, "Config. Pincel", PANEL_X + 10, panelY + 6, 0xFF404040, false);
                g.drawString(font, "Grosor: " + GlobalGuiSettings.grosorPincel, PANEL_X + 10, panelY + 25, 0xFF202020, false);

                drawVanillaButton(g, font, PANEL_X + 70, panelY + 20, 18, 18, "-", false);
                drawVanillaButton(g, font, PANEL_X + 90, panelY + 20, 18, 18, "+", false);

                g.drawString(font, "Color:", PANEL_X + 10, panelY + 50, 0xFF202020, false);

                colorBoxRectX = PANEL_X + 45;
                colorBoxRectY = panelY + 45;
                g.fill(colorBoxRectX, colorBoxRectY, colorBoxRectX + 16, colorBoxRectY + 16, GlobalGuiSettings.colorHerramientas);
                g.renderOutline(colorBoxRectX - 1, colorBoxRectY - 1, 18, 18, 0xFF000000);

                drawVanillaButton(g, font, PANEL_X + 65, panelY + 44, 45, 18, "Volver", false);

            } else {
                drawVanillaPanel(g, PANEL_X, panelY, 120, 105);
                g.drawString(font, "Herramientas", PANEL_X + 10, panelY + 6, 0xFF404040, false);
                String[] tools = {"Selecci\u00f3n", "Pincel", "Formas", "L\u00ednea"};
                for (int i = 0; i < tools.length; i++) {
                    drawVanillaButton(g, font, PANEL_X + 10, panelY + 20 + (i * 20), 100, 18, tools[i], selectedTool == i);
                }
            }
        } else if (selectedModule == 2) {
            drawVanillaPanel(g, PANEL_X, panelY, 140, 145);
            g.drawString(font, "Nav. Misiones", PANEL_X + 10, panelY + 6, 0xFF404040, false);

            String[] func = {"Contenedor Maestro", "Solo Principales", "Solo Secundarias", "Titulo Mision", "Descripcion", "Objetivos"};
            for (int i = 0; i < func.length; i++) {
                drawVanillaButton(g, font, PANEL_X + 10, panelY + 20 + (i * 20), 120, 18, func[i], false);
            }
        } 
        // --- NUEVO MÓDULO: WIDGETS RPG ---
        else if (selectedModule == 3) {
            if (showNormalMenu) {
                drawVanillaPanel(g, PANEL_X, panelY, 130, 85);
                g.drawString(font, "Inventario", PANEL_X + 10, panelY + 6, 0xFF404040, false);
                drawVanillaButton(g, font, PANEL_X + 10, panelY + 20, 110, 18, "Cuadr\u00edcula", false);
                drawVanillaButton(g, font, PANEL_X + 10, panelY + 40, 110, 18, "Acceso R\u00e1pido", false);
                drawVanillaButton(g, font, PANEL_X + 10, panelY + 60, 110, 18, "Volver", false);
            } else if (showInventoryMenu) {
                drawVanillaPanel(g, PANEL_X, panelY, 130, 145);
                g.drawString(font, "Slots", PANEL_X + 10, panelY + 6, 0xFF404040, false);
                String[] slots = {"Casco", "Pechera", "Pantalones", "Botas", "Escudo", "Volver"};
                for (int i = 0; i < slots.length; i++) {
                    drawVanillaButton(g, font, PANEL_X + 10, panelY + 20 + (i * 20), 110, 18, slots[i], false);
                }
            } else if (showStatsMenu) {
                drawVanillaPanel(g, PANEL_X, panelY, 140, 225);
                g.drawString(font, "Estad\u00edstica", PANEL_X + 10, panelY + 6, 0xFF404040, false);
                
                String[] stats = {"Salud", "Da\u00f1o", "Defensa", "Velocidad", "Enemigos", "Bloques", "Distancia", "Muertes", "Da\u00f1o Rec.", "Volver"};
                for (int i = 0; i < stats.length; i++) {
                    drawVanillaButton(g, font, PANEL_X + 10, panelY + 20 + (i * 20), 120, 18, stats[i], false);
                }
            } else if (showProgresoMenu) {
                drawVanillaPanel(g, PANEL_X, panelY, 130, 105);
                g.drawString(font, "Progreso", PANEL_X + 10, panelY + 6, 0xFF404040, false);
                drawVanillaButton(g, font, PANEL_X + 10, panelY + 20, 110, 18, "Misiones", false);
                drawVanillaButton(g, font, PANEL_X + 10, panelY + 40, 110, 18, "Logros", false);
                drawVanillaButton(g, font, PANEL_X + 10, panelY + 60, 110, 18, "Tiempo Jugado", false);
                drawVanillaButton(g, font, PANEL_X + 10, panelY + 80, 110, 18, "Biomas", false); // ¡CAMBIADO AQUÍ!
            } else {
                drawVanillaPanel(g, PANEL_X, panelY, 130, 125); 
                g.drawString(font, "Widgets RPG", PANEL_X + 10, panelY + 6, 0xFF404040, false);
                drawVanillaButton(g, font, PANEL_X + 10, panelY + 20, 110, 18, "Maniqu\u00ed", false);
                drawVanillaButton(g, font, PANEL_X + 10, panelY + 40, 110, 18, "Inventario", false);
                drawVanillaButton(g, font, PANEL_X + 10, panelY + 60, 110, 18, "Estad\u00edstica", false);
                drawVanillaButton(g, font, PANEL_X + 10, panelY + 80, 110, 18, "Slots", false);
                drawVanillaButton(g, font, PANEL_X + 10, panelY + 100, 110, 18, "Progreso", false);
            }
        } 
        // --- NUEVO MÓDULO: IMÁGENES E ÍCONOS ---
        else if (selectedModule == 4) {
            drawVanillaPanel(g, PANEL_X, panelY, 135, 65);
            g.drawString(font, "Im\u00e1genes e \u00cdconos", PANEL_X + 10, panelY + 6, 0xFF404040, false);
            drawVanillaButton(g, font, PANEL_X + 10, panelY + 20, 115, 18, "Galer\u00eda (PNGs)", false);
            drawVanillaButton(g, font, PANEL_X + 10, panelY + 40, 115, 18, "Texturas (Juego)", false);
        }
    }

    private static boolean isHovered(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    // --- FUNCIÓN AUXILIAR PARA CREAR WIDGETS ---
    public static void crearWidget(String tipo, String nombreCapa, int x, int y, int w, int h) {
        GlobalGuiSettings.PanelConfig widget = new GlobalGuiSettings.PanelConfig(x, y, w, h);
        widget.tipo = tipo;
        widget.pagina = GlobalGuiSettings.paginaActual;
        
        if (tipo.equals("ESTADISTICA")) {
            widget.textoAsociado = "Salud"; // Por defecto, luego lo editaremos
        } else {
            widget.textoAsociado = nombreCapa; // Nombre para que se vea bonito en la RightBar
        }
        
        GlobalGuiSettings.PANELES.add(widget);
        GlobalGuiSettings.sincronizarCapas();
        GlobalGuiSettings.panelSeleccionado = widget;
    }

    public static boolean handleClick(double mx, double my, int screenHeight, int screenWidth, GlobalGuiSettings.TextConfig tSel, GlobalGuiSettings.PanelConfig pSel) {
        if (!sidebarVisible) return false;

        int startY = getSidebarY(screenHeight);

        // Detección de clics en las pestañas principales
        if (isHovered(mx, my, START_X, startY, TAB_WIDTH, TAB_HEIGHT)) {
            toggleModule(0); return true;
        }
        if (isHovered(mx, my, START_X, startY + TAB_HEIGHT + TAB_GAP, TAB_WIDTH, TAB_HEIGHT)) {
            toggleModule(1); return true;
        }
        if (isHovered(mx, my, START_X, startY + 2 * (TAB_HEIGHT + TAB_GAP), TAB_WIDTH, TAB_HEIGHT)) {
            toggleModule(2); return true;
        }
        if (isHovered(mx, my, START_X, startY + 3 * (TAB_HEIGHT + TAB_GAP), TAB_WIDTH, TAB_HEIGHT)) {
            toggleModule(3); return true; // Clic en pestaña W
        }
        if (isHovered(mx, my, START_X, startY + 4 * (TAB_HEIGHT + TAB_GAP), TAB_WIDTH, TAB_HEIGHT)) {
            toggleModule(4); return true; // Clic en pestaña Tierra
        }

        if (selectedModule >= 0) {
            int panelY = startY;
            int viewportY = TopBar.getHeight();
            int centerX = screenWidth / 2;
            int centerY = viewportY + (screenHeight - viewportY) / 2;

            int panelWidth = 0;
            int panelHeight = 0;

            if (selectedModule == 0) {
                panelWidth = 120; panelHeight = 45;
                if (isHovered(mx, my, PANEL_X + 10, panelY + 20, 100, 18)) {
                    TextoEdit.crearNuevoTexto(centerX, centerY);
                    return true;
                }
            } else if (selectedModule == 1) {
                if (showShapesMenu) {
                    panelWidth = 120; panelHeight = 125;
                    if (isHovered(mx, my, PANEL_X + 10, panelY + 20, 100, 18)) { FigurasEdit.crearCuadrado(centerX - 25, centerY - 25); selectedShape = 0; showShapesMenu = false; return true; }
                    if (isHovered(mx, my, PANEL_X + 10, panelY + 40, 100, 18)) { FigurasEdit.crearRectangulo(centerX - 30, centerY - 20); selectedShape = 1; showShapesMenu = false; return true; }
                    if (isHovered(mx, my, PANEL_X + 10, panelY + 60, 100, 18)) { FigurasEdit.crearTriangulo(centerX, centerY); selectedShape = 2; showShapesMenu = false; return true; }
                    if (isHovered(mx, my, PANEL_X + 10, panelY + 80, 100, 18)) { FigurasEdit.crearCirculo(centerX, centerY); selectedShape = 3; showShapesMenu = false; return true; }
                    if (isHovered(mx, my, PANEL_X + 10, panelY + 100, 100, 18)) { showShapesMenu = false; return true; }
                } else if (showBrushThickness) {
                    panelWidth = 120; panelHeight = 85;
                    if (isHovered(mx, my, PANEL_X, panelY, panelWidth, panelHeight)) {
                        if (isHovered(mx, my, PANEL_X + 70, panelY + 20, 18, 18)) { GlobalGuiSettings.grosorPincel = Math.max(1, GlobalGuiSettings.grosorPincel - 1); return true; }
                        if (isHovered(mx, my, PANEL_X + 90, panelY + 20, 18, 18)) { GlobalGuiSettings.grosorPincel = Math.min(10, GlobalGuiSettings.grosorPincel + 1); return true; }
                        if (isHovered(mx, my, PANEL_X + 65, panelY + 44, 45, 18)) { showBrushThickness = false; return true; }
                        if (isHovered(mx, my, colorBoxRectX, colorBoxRectY, 16, 16)) {
                            editColorRequested = true;
                            return true;
                        }
                    }
                } else {
                    panelWidth = 120; panelHeight = 105;
                    if (isHovered(mx, my, PANEL_X + 10, panelY + 20, 100, 18)) { selectedTool = 0; return true; }
                    if (isHovered(mx, my, PANEL_X + 10, panelY + 40, 100, 18)) { selectedTool = 1; showBrushThickness = true; showShapesMenu = false; return true; }
                    if (isHovered(mx, my, PANEL_X + 10, panelY + 60, 100, 18)) { selectedTool = 2; showShapesMenu = true; showBrushThickness = false; return true; }
                    if (isHovered(mx, my, PANEL_X + 10, panelY + 80, 100, 18)) { selectedTool = 3; showBrushThickness = true; showShapesMenu = false; return true; }
                }
            } else if (selectedModule == 2) {
                panelWidth = 140; panelHeight = 145;
                if (isHovered(mx, my, PANEL_X + 10, panelY + 20, 120, 18)) { FigurasEdit.crearDesplegable(centerX - 80, centerY - 100, 2); selectedModule = -1; return true; }
                if (isHovered(mx, my, PANEL_X + 10, panelY + 40, 120, 18)) { FigurasEdit.crearDesplegable(centerX - 80, centerY - 100, 0); selectedModule = -1; return true; }
                if (isHovered(mx, my, PANEL_X + 10, panelY + 60, 120, 18)) { FigurasEdit.crearDesplegable(centerX - 80, centerY - 100, 1); selectedModule = -1; return true; }
                if (isHovered(mx, my, PANEL_X + 10, panelY + 80, 120, 18)) { FigurasEdit.crearPiezaMision("MISION_TITULO", centerX - 75, centerY - 15); selectedModule = -1; return true; }
                if (isHovered(mx, my, PANEL_X + 10, panelY + 100, 120, 18)) { FigurasEdit.crearPiezaMision("MISION_DESCRIPCION", centerX - 75, centerY - 30); selectedModule = -1; return true; }
                if (isHovered(mx, my, PANEL_X + 10, panelY + 120, 120, 18)) { FigurasEdit.crearPiezaMision("MISION_OBJETIVOS", centerX - 75, centerY - 50); selectedModule = -1; return true; }
            } 
            // --- NUEVOS CLICS DE LOS WIDGETS RPG ---
            else if (selectedModule == 3) {
                if (showNormalMenu) {
                    panelWidth = 130; panelHeight = 85;
                    if (isHovered(mx, my, PANEL_X + 10, panelY + 20, 110, 18)) { crearWidget("INVENTORY_GRID", "Inventario", centerX - 90, centerY - 30, 180, 60); selectedModule = -1; showNormalMenu = false; return true; }
                    if (isHovered(mx, my, PANEL_X + 10, panelY + 40, 110, 18)) { crearWidget("HOTBAR", "Acceso Rapido", centerX - 50, centerY - 10, 100, 20); selectedModule = -1; showNormalMenu = false; return true; }
                    if (isHovered(mx, my, PANEL_X + 10, panelY + 60, 110, 18)) { showNormalMenu = false; return true; } // Volver
                } else if (showInventoryMenu) {
                    panelWidth = 130; panelHeight = 145;
                    if (isHovered(mx, my, PANEL_X + 10, panelY + 20, 110, 18)) { crearWidget("SLOT_CASCO", "Slot Casco", centerX - 10, centerY - 10, 20, 20); selectedModule = -1; showInventoryMenu = false; return true; }
                    if (isHovered(mx, my, PANEL_X + 10, panelY + 40, 110, 18)) { crearWidget("SLOT_PECHERA", "Slot Pechera", centerX - 10, centerY - 10, 20, 20); selectedModule = -1; showInventoryMenu = false; return true; }
                    if (isHovered(mx, my, PANEL_X + 10, panelY + 60, 110, 18)) { crearWidget("SLOT_PANTALON", "Slot Pantalon", centerX - 10, centerY - 10, 20, 20); selectedModule = -1; showInventoryMenu = false; return true; }
                    if (isHovered(mx, my, PANEL_X + 10, panelY + 80, 110, 18)) { crearWidget("SLOT_BOTAS", "Slot Botas", centerX - 10, centerY - 10, 20, 20); selectedModule = -1; showInventoryMenu = false; return true; }
                    if (isHovered(mx, my, PANEL_X + 10, panelY + 100, 110, 18)) { crearWidget("SLOT_ESCUDO", "Slot Escudo", centerX - 10, centerY - 10, 20, 20); selectedModule = -1; showInventoryMenu = false; return true; }
                    if (isHovered(mx, my, PANEL_X + 10, panelY + 120, 110, 18)) { showInventoryMenu = false; return true; } // Volver
                } else if (showStatsMenu) {
                    panelWidth = 140; panelHeight = 225;
                    if (isHovered(mx, my, PANEL_X + 10, panelY + 20, 120, 18)) { crearWidget("ESTADISTICA_SALUD", "Salud", centerX - 20, centerY - 10, 60, 20); selectedModule = -1; showStatsMenu = false; return true; }
                    if (isHovered(mx, my, PANEL_X + 10, panelY + 40, 120, 18)) { crearWidget("ESTADISTICA_DANO", "Da\u00f1o", centerX - 20, centerY - 10, 60, 20); selectedModule = -1; showStatsMenu = false; return true; }
                    if (isHovered(mx, my, PANEL_X + 10, panelY + 60, 120, 18)) { crearWidget("ESTADISTICA_DEFENSA", "Defensa", centerX - 20, centerY - 10, 60, 20); selectedModule = -1; showStatsMenu = false; return true; }
                    if (isHovered(mx, my, PANEL_X + 10, panelY + 80, 120, 18)) { crearWidget("ESTADISTICA_VELOCIDAD", "Velocidad", centerX - 20, centerY - 10, 60, 20); selectedModule = -1; showStatsMenu = false; return true; }
                    if (isHovered(mx, my, PANEL_X + 10, panelY + 100, 120, 18)) { crearWidget("ESTADISTICA_KILLS", "Enemigos", centerX - 20, centerY - 10, 60, 20); selectedModule = -1; showStatsMenu = false; return true; }
                    if (isHovered(mx, my, PANEL_X + 10, panelY + 120, 120, 18)) { crearWidget("ESTADISTICA_MINADOS", "Bloques", centerX - 20, centerY - 10, 60, 20); selectedModule = -1; showStatsMenu = false; return true; }
                    if (isHovered(mx, my, PANEL_X + 10, panelY + 140, 120, 18)) { crearWidget("ESTADISTICA_DISTANCIA", "Distancia", centerX - 20, centerY - 10, 60, 20); selectedModule = -1; showStatsMenu = false; return true; }
                    if (isHovered(mx, my, PANEL_X + 10, panelY + 160, 120, 18)) { crearWidget("ESTADISTICA_MUERTES", "Muertes", centerX - 20, centerY - 10, 60, 20); selectedModule = -1; showStatsMenu = false; return true; }
                    if (isHovered(mx, my, PANEL_X + 10, panelY + 180, 120, 18)) { crearWidget("ESTADISTICA_DANO_RECIBIDO", "Da\u00f1o Rec.", centerX - 20, centerY - 10, 60, 20); selectedModule = -1; showStatsMenu = false; return true; }
                    if (isHovered(mx, my, PANEL_X + 10, panelY + 200, 120, 18)) { showStatsMenu = false; return true; }
                } else if (showProgresoMenu) {
                    panelWidth = 130; panelHeight = 105;
                    if (isHovered(mx, my, PANEL_X + 10, panelY + 20, 110, 18)) { crearWidget("PROGRESO", "Misiones", centerX - 50, centerY - 10, 100, 20); GlobalGuiSettings.panelSeleccionado.progresoTipo = 0; selectedModule = -1; showProgresoMenu = false; return true; }
                    if (isHovered(mx, my, PANEL_X + 10, panelY + 40, 110, 18)) { crearWidget("PROGRESO", "Logros", centerX - 50, centerY - 10, 100, 20); GlobalGuiSettings.panelSeleccionado.progresoTipo = 1; selectedModule = -1; showProgresoMenu = false; return true; }
                    if (isHovered(mx, my, PANEL_X + 10, panelY + 60, 110, 18)) { crearWidget("PROGRESO", "Tiempo", centerX - 50, centerY - 10, 100, 20); GlobalGuiSettings.panelSeleccionado.progresoTipo = 2; selectedModule = -1; showProgresoMenu = false; return true; }
                    if (isHovered(mx, my, PANEL_X + 10, panelY + 80, 110, 18)) { crearWidget("PROGRESO", "Biomas", centerX - 50, centerY - 10, 100, 20); GlobalGuiSettings.panelSeleccionado.progresoTipo = 3; selectedModule = -1; showProgresoMenu = false; return true; } // ¡CAMBIADO AQUÍ!
                } else {
                    panelWidth = 130; panelHeight = 125; 
                    if (isHovered(mx, my, PANEL_X + 10, panelY + 20, 110, 18)) { crearWidget("MANIQUI", "Widget Maniqui", centerX - 25, centerY - 40, 50, 80); selectedModule = -1; return true; }
                    if (isHovered(mx, my, PANEL_X + 10, panelY + 40, 110, 18)) { showNormalMenu = true; return true; } 
                    if (isHovered(mx, my, PANEL_X + 10, panelY + 60, 110, 18)) { showStatsMenu = true; return true; }   
                    if (isHovered(mx, my, PANEL_X + 10, panelY + 80, 110, 18)) { showInventoryMenu = true; return true; } 
                    if (isHovered(mx, my, PANEL_X + 10, panelY + 100, 110, 18)) { showProgresoMenu = true; return true; } 
                }
            }
            // --- CLICS DE IMÁGENES E ÍCONOS ---
            else if (selectedModule == 4) {
                panelWidth = 135; panelHeight = 65;
                if (isHovered(mx, my, PANEL_X + 10, panelY + 20, 115, 18)) { 
                    showGaleriaRequested = true; selectedModule = -1; return true; 
                }
                if (isHovered(mx, my, PANEL_X + 10, panelY + 40, 115, 18)) { 
                    showTexturasRequested = true; selectedModule = -1; return true; 
                }
            }

            if (isHovered(mx, my, PANEL_X, panelY, panelWidth, panelHeight)) {
                return true;
            }
        }
        return false;
    }

    private static void toggleModule(int module) {
        selectedModule = (selectedModule == module) ? -1 : module;
        showShapesMenu = false;
        showBrushThickness = false;
        showInventoryMenu = false;
        showNormalMenu = false;
        showStatsMenu = false;
        showProgresoMenu = false;
        selectedTool = -1;
        selectedShape = -1;
    }
}