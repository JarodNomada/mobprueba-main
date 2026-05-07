package questgrupo.questmod.client.editor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import questgrupo.questmod.Config;
import questgrupo.questmod.Config.MisionData;
import questgrupo.questmod.client.GlobalGuiSettings;
import questgrupo.questmod.client.gui.FigurasEdit;
import questgrupo.questmod.client.gui.TextoEdit;
import java.util.ArrayList;
import java.util.List;

public class LeftSidebar {
    public static boolean sidebarVisible = true;
    public static int selectedModule = -1; // -1: none, 0: Texto, 1: Herramientas
    public static int selectedTool = -1; // -1: none, 0: Mouse, 1: Pincel, 2: Formas, 3: Linea
    public static int selectedShape = -1; // -1: none, 0: Cuadrado, 1: Rectangulo, 2: Triangulo, 3: Circulo
    public static boolean showBrushThickness = false;
    public static boolean showShapesMenu = false;
    public static boolean editColorRequested = false; // Set to true when user clicks color box
    public static final int SIDEBAR_WIDTH = 10;
    private static final int MODULE_HEIGHT = 20;
    private static final int SIDEBAR_COLOR = 0xFFA6A6A6; // #A6A6A6
    private static int subpanelWidth = 0;

    public static int getSidebarY(int screenHeight) {
        int sidebarHeight = 3 * MODULE_HEIGHT;
        return (screenHeight - sidebarHeight) / 2;
    }
    
    public static int getSidebarWidth() {
        // Always reserve full space when editor is active to prevent pushing elements
        if (!GlobalGuiSettings.editorActivo) return 0;
        // Max sidebar width: base (10) + largest subpanel (80) + 1 spacing
        return 10 + 80 + 1;
    }

    public static void render(GuiGraphics g, int screenWidth, int screenHeight) {
        if (!sidebarVisible) return;

        int sidebarHeight = 3 * MODULE_HEIGHT; // Texto, Herramientas, Funciones
        int sidebarY = (screenHeight - sidebarHeight) / 2; // Centrado verticalmente
        
        // Barra principal 10px gris #A6A6A6
        g.fill(0, sidebarY, SIDEBAR_WIDTH, sidebarY + sidebarHeight, SIDEBAR_COLOR);

        Font font = Minecraft.getInstance().font;
        // Etiquetas T y H en negro (sin sombra)
        g.drawString(font, "T", 2, sidebarY + (MODULE_HEIGHT - font.lineHeight) / 2, 0xFF000000, false);
        g.drawString(font, "H", 2, sidebarY + MODULE_HEIGHT + (MODULE_HEIGHT - font.lineHeight) / 2, 0xFF000000, false);
        g.drawString(font, "F", 2, sidebarY + 2 * MODULE_HEIGHT + (MODULE_HEIGHT - font.lineHeight) / 2, 0xFF000000, false);

        // Sin resaltado al seleccionar - botón plano

        // Sub-panel si hay módulo seleccionado
        if (selectedModule >= 0) {
            renderSubPanel(g, screenWidth, screenHeight);
        }
    }

    private static void renderSubPanel(GuiGraphics g, int screenWidth, int screenHeight) {
        int panelX = SIDEBAR_WIDTH + 1;
        int panelWidth = 80;
        subpanelWidth = panelWidth;

        int panelHeight = 0;
        Font font = Minecraft.getInstance().font;
        int sidebarY = getSidebarY(screenHeight);
        
        if (selectedModule == 0) {
            panelHeight = 20;
            g.fill(panelX, sidebarY, panelX + panelWidth, sidebarY + panelHeight, 0xFFFFFFFF);
            g.renderOutline(panelX, sidebarY, panelWidth, panelHeight, 0xFF000000);
            // Click to create new text in viewport center
            g.drawString(font, "Nuevo Texto", panelX + 5, sidebarY + 5, 0xFF000000, false);
        } else if (selectedModule == 1) {
            if (showShapesMenu) {
                panelHeight = 65;
                g.fill(panelX, sidebarY, panelX + panelWidth, sidebarY + panelHeight, 0xFFFFFFFF);
                g.renderOutline(panelX, sidebarY, panelWidth, panelHeight, 0xFF000000);
                int textY = sidebarY + 5;
                g.drawString(font, "Cuadrado", panelX + 5, textY, 0xFF000000, false); textY += 10;
                g.drawString(font, "Rect\u00e1ngulo", panelX + 5, textY, 0xFF000000, false); textY += 10;
                g.drawString(font, "Tri\u00e1ngulo", panelX + 5, textY, 0xFF000000, false); textY += 10;
                g.drawString(font, "C\u00edrculo", panelX + 5, textY, 0xFF000000, false); textY += 10;
                g.drawString(font, "Volver", panelX + 5, textY, 0xFF000000, false);
            } else if (showBrushThickness) {
                panelHeight = 80;
                g.fill(panelX, sidebarY, panelX + panelWidth, sidebarY + panelHeight, 0xFFFFFFFF);
                g.renderOutline(panelX, sidebarY, panelWidth, panelHeight, 0xFF000000);
                int textY = sidebarY + 5;
                g.drawString(font, "Grosor: " + GlobalGuiSettings.grosorPincel, panelX + 5, textY, 0xFF000000, false); textY += 15;
                g.drawString(font, "+", panelX + 30, textY, 0xFF000000, false);
                g.drawString(font, "-", panelX + 50, textY, 0xFF000000, false); textY += 15;
                g.drawString(font, "Color:", panelX + 5, textY, 0xFF000000, false);
                int colorBoxX = panelX + 40;
                int colorBoxY = textY - 2;
                g.fill(colorBoxX, colorBoxY, colorBoxX + 15, colorBoxY + 15, GlobalGuiSettings.colorHerramientas);
                g.renderOutline(colorBoxX, colorBoxY, 15, 15, 0xFF000000);
            } else {
                panelHeight = 50;
                g.fill(panelX, sidebarY, panelX + panelWidth, sidebarY + panelHeight, 0xFFFFFFFF);
                g.renderOutline(panelX, sidebarY, panelWidth, panelHeight, 0xFF000000);
                int textY = sidebarY + 5;
                g.drawString(font, "Selecci\u00f3n (Mouse)", panelX + 5, textY, 0xFF000000, false); textY += 10;
                g.drawString(font, "Pincel", panelX + 5, textY, 0xFF000000, false); textY += 10;
                g.drawString(font, "Formas", panelX + 5, textY, 0xFF000000, false); textY += 10;
                g.drawString(font, "Línea", panelX + 5, textY, 0xFF000000, false);
            }
        } else if (selectedModule == 2) {
            panelWidth = 140;
            subpanelWidth = panelWidth;
            panelHeight = 85;
            
            g.fill(panelX, sidebarY, panelX + panelWidth, sidebarY + panelHeight, 0xFFFFFFFF);
            g.renderOutline(panelX, sidebarY, panelWidth, panelHeight, 0xFF000000);
            
            int textY = sidebarY + 5;
            g.drawString(font, "[+] Contenedor Maestro", panelX + 5, textY, 0xFF000000, false); textY += 15;
            g.drawString(font, "[+] Solo Principales", panelX + 5, textY, 0xFF000000, false); textY += 15;
            g.drawString(font, "[+] Solo Secundarias", panelX + 5, textY, 0xFF000000, false); textY += 15;
            g.drawString(font, "[+] Titulo Mision", panelX + 5, textY, 0xFF000000, false); textY += 15;
            g.drawString(font, "[+] Descripcion", panelX + 5, textY, 0xFF000000, false); textY += 15;
            g.drawString(font, "[+] Objetivos", panelX + 5, textY, 0xFF000000, false);
        }
    }

    public static boolean handleClick(double mx, double my, int screenHeight, int screenWidth, GlobalGuiSettings.TextConfig tSel, GlobalGuiSettings.PanelConfig pSel) {
        if (!sidebarVisible) return false;

        int sidebarY = getSidebarY(screenHeight);
        
        if (mx >= 0 && mx <= SIDEBAR_WIDTH) {
            int section = (int)((my - sidebarY) / MODULE_HEIGHT);
            if (section >= 0 && section < 3) {
                selectedModule = (selectedModule == section) ? -1 : section;
                // Reset submenu states when switching modules
                showShapesMenu = false;
                showBrushThickness = false;
                selectedTool = -1;
                selectedShape = -1;
            }
            return true;
        }

        int panelX = SIDEBAR_WIDTH + 1;
        if (mx >= panelX && mx <= panelX + subpanelWidth) {
            // Center based on full screen - elements are now drawn without offset
            int viewportY = TopBar.getHeight();
            int centerX = screenWidth / 2;
            int centerY = viewportY + (screenHeight - viewportY) / 2;

            if (selectedModule == 0 && my >= sidebarY + 5 && my <= sidebarY + 25) {
                TextoEdit.crearNuevoTexto(centerX, centerY);
                return true;
            }
            if (selectedModule == 1) {
                int localY = (int)my - sidebarY;
                
                // If a submenu is open, handle its clicks and prevent main menu clicks
                if (showShapesMenu) {
                    if (localY >= 5 && localY <= 15) { 
                        selectedShape = 0; showShapesMenu = false; 
                        FigurasEdit.crearCuadrado(centerX - 25, centerY - 25); 
                        return true; 
                    }
                    if (localY >= 15 && localY <= 25) { 
                        selectedShape = 1; showShapesMenu = false; 
                        FigurasEdit.crearRectangulo(centerX - 30, centerY - 20); 
                        return true; 
                    }
                    if (localY >= 25 && localY <= 35) { 
                        selectedShape = 2; showShapesMenu = false; 
                        FigurasEdit.crearTriangulo(centerX, centerY); 
                        return true; 
                    }
                    if (localY >= 35 && localY <= 45) { 
                        selectedShape = 3; showShapesMenu = false; 
                        FigurasEdit.crearCirculo(centerX, centerY); 
                        return true; 
                    }
                    if (localY >= 45 && localY <= 55) { showShapesMenu = false; return true; }
                } else if (showBrushThickness) {
                    if (localY >= 5 && localY <= 20) { /* Grosor display */ return true; }
                    if (localY >= 20 && localY <= 35) { 
                        if (mx >= panelX + 30 && mx <= panelX + 40) { GlobalGuiSettings.grosorPincel = Math.min(10, GlobalGuiSettings.grosorPincel + 1); return true; } 
                        if (mx >= panelX + 50 && mx <= panelX + 60) { GlobalGuiSettings.grosorPincel = Math.max(1, GlobalGuiSettings.grosorPincel - 1); return true; } 
                    }
                    // Color box: panelX + 40 to panelX + 55, sidebarY + 35 to sidebarY + 50
                    if (localY >= 35 && localY <= 50 && mx >= panelX + 40 && mx <= panelX + 55) {
                        editColorRequested = true;
                        return true;
                    }
                } else {
                    // Main tools menu
                    if (localY >= 5 && localY <= 15) { selectedTool = 0; tSel = null; pSel = null; return true; }
                    if (localY >= 15 && localY <= 25) { 
                        selectedTool = 1; showBrushThickness = true; showShapesMenu = false; tSel = null; pSel = null; return true; 
                    }
                    if (localY >= 25 && localY <= 35) { 
                        selectedTool = 2; showShapesMenu = true; showBrushThickness = false; tSel = null; pSel = null; return true; 
                    }
                    if (localY >= 35 && localY <= 45) { selectedTool = 3; showBrushThickness = true; showShapesMenu = false; tSel = null; pSel = null; return true; }
                }
            } else if (selectedModule == 2) {
                int localY = (int)my - sidebarY;
                if (mx >= panelX && mx <= panelX + 140) {
                    if (localY >= 5 && localY <= 20) {
                        FigurasEdit.crearDesplegable(centerX - 80, centerY - 100, 2); // Maestro
                        selectedModule = -1;
                        return true;
                    }
                    if (localY >= 25 && localY <= 40) {
                        FigurasEdit.crearDesplegable(centerX - 80, centerY - 100, 0); // Principales
                        selectedModule = -1;
                        return true;
                    }
                    if (localY >= 45 && localY <= 60) {
                        FigurasEdit.crearDesplegable(centerX - 80, centerY - 100, 1); // Secundarias
                        selectedModule = -1;
                        return true;
                    }
                    if (localY >= 5 && localY <= 15) {
                        FigurasEdit.crearDesplegable(centerX - 80, centerY - 100, 2);
                        selectedModule = -1;
                        return true;
                    }
                    if (localY >= 20 && localY <= 30) {
                        FigurasEdit.crearDesplegable(centerX - 80, centerY - 100, 0);
                        selectedModule = -1;
                        return true;
                    }
                    if (localY >= 35 && localY <= 45) {
                        FigurasEdit.crearDesplegable(centerX - 80, centerY - 100, 1);
                        selectedModule = -1;
                        return true;
                    }
                    if (localY >= 50 && localY <= 60) {
                        FigurasEdit.crearPiezaMision("MISION_TITULO", centerX - 75, centerY - 15);
                        selectedModule = -1;
                        return true;
                    }
                    if (localY >= 65 && localY <= 75) {
                        FigurasEdit.crearPiezaMision("MISION_DESCRIPCION", centerX - 75, centerY - 30);
                        selectedModule = -1;
                        return true;
                    }
                    if (localY >= 80 && localY <= 90) {
                        FigurasEdit.crearPiezaMision("MISION_OBJETIVOS", centerX - 75, centerY - 50);
                        selectedModule = -1;
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
