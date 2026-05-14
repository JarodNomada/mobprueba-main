package questgrupo.questmod.client.editor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import questgrupo.questmod.client.GlobalGuiSettings;

public class RightBar {

    private static final int MARGIN_TOP   = 4;
    private static final int MARGIN_RIGHT = 2;

    private static final int WIDTH        = 135;
    private static final int HEADER_H     = 22;
    private static final int BOTTOM_H     = 30;
    private static final int ITEM_H       = 27;
    private static final int THUMB_SIZE   = 16;
    private static final int MAX_VISIBLE  = 5;
    private static final int LIST_H       = ITEM_H * MAX_VISIBLE;
    private static final int PANEL_H      = HEADER_H + LIST_H + BOTTOM_H;

    private static final float TEXT_SCALE = 0.75f;

    private static int scrollOffset = 0;

    public static boolean isVisible = true;

    public static int getWidth() {
        return isVisible && GlobalGuiSettings.editorActivo ? WIDTH + MARGIN_RIGHT : 0;
    }

    public static void render(GuiGraphics g, int screenWidth, int screenHeight) {
        if (!isVisible || !GlobalGuiSettings.editorActivo) return;

        int x0    = screenWidth - WIDTH - MARGIN_RIGHT;
        int x1    = screenWidth - MARGIN_RIGHT;
        int yBase = MARGIN_TOP;

        Font font = Minecraft.getInstance().font;

        g.fill(x0, yBase, x1, yBase + PANEL_H, 0xFF5A5A5A);

        int hY0 = yBase;
        int hY1 = yBase + HEADER_H;
        g.fill(x0, hY0, x1, hY1, 0xFF3D3D3D);
        g.drawString(font, "CAPAS", x0 + 7, hY0 + (HEADER_H - 8) / 2, 0xFFFFFFFF, false);

        int plusX = x1 - 19;
        int plusY = hY0 + 2;
        renderButton(g, plusX, plusY, 16, 18, 0xFF3A3A3A, 0xFF2A2A2A);
        g.drawString(font, "+", plusX + 4, plusY + 5, 0xFFFFFFFF, false);

        int listX0 = x0 + 3;
        int listX1 = x1 - 3;
        int listY0 = hY1;
        int listY1 = listY0 + LIST_H;

        g.fill(listX0, listY0, listX1, listY1, 0xFF4A4A4A);

        java.util.List<GlobalGuiSettings.Capa> capas = capasDePageActual();
        int maxScroll = Math.max(0, capas.size() - MAX_VISIBLE);
        scrollOffset  = Math.max(0, Math.min(scrollOffset, maxScroll));

        for (int slot = 0; slot < MAX_VISIBLE; slot++) {
            int idx   = slot + scrollOffset;
            int itemY = listY0 + slot * ITEM_H;

            if (idx >= capas.size()) continue;

            GlobalGuiSettings.Capa capa = capas.get(idx);
            boolean sel = (capa.panel != null && capa.panel == GlobalGuiSettings.panelSeleccionado)
                       || (capa.texto != null && capa.texto == GlobalGuiSettings.textoSeleccionado);

            int bgColor = sel ? 0xFF6E6E6E : 0xFF5A5A5A;

            g.fill(listX0 + 1, itemY, listX1 - 1, itemY + ITEM_H, bgColor);

            g.fill(listX0, itemY + ITEM_H - 1, listX1, itemY + ITEM_H, 0xFF000000);

            dibujarOjo(g, listX0 + 4, itemY + (ITEM_H - 10) / 2, capa.visible);

            int tx = listX0 + 18;
            int ty = itemY + (ITEM_H - THUMB_SIZE) / 2;
            dibujarThumbnail(g, tx, ty);

            String nombre = (capa.nombre != null) ? capa.nombre : "Capa";
            if (nombre.length() > 10) nombre = nombre.substring(0, 8) + "..";
            int textColor = capa.visible ? 0xFFDDDDDD : 0xFF888888;
            int textX = tx + THUMB_SIZE + 5;
            int textY = itemY + (ITEM_H - (int)(8 * TEXT_SCALE)) / 2;
            dibujarTextoEscalado(g, font, nombre, textX, textY, textColor);

            dibujarHamburguesa(g, listX1 - 11, itemY + (ITEM_H - 7) / 2);
         }

        // Dibujamos el borde negro del contenedor de la lista DESPUÉS de las capas
        g.renderOutline(listX0, listY0, listX1 - listX0, listY1 - listY0, 0xFF000000);

         int botY = listY1;
        g.fill(x0, botY, x1, yBase + PANEL_H, 0xFF3D3D3D);
        renderBotonesInferiores(g, font, x0, x1, botY);

        g.renderOutline(x0, yBase, x1 - x0, PANEL_H, 0xFFCCCCCC);
        g.renderOutline(x0 - 1, yBase - 1, (x1 - x0) + 2, PANEL_H + 2, 0xFF000000);
    }

    private static void dibujarTextoEscalado(GuiGraphics g, Font font, String text, int x, int y, int color) {
        g.pose().pushPose();
        g.pose().translate(x, y, 0);
        g.pose().scale(TEXT_SCALE, TEXT_SCALE, 1f);
        g.drawString(font, text, 0, 0, color, false);
        g.pose().popPose();
    }

    public static boolean handleScroll(double mx, double my, double delta, int screenWidth, int screenHeight) {
        if (!isVisible || !GlobalGuiSettings.editorActivo) return false;
        int x0     = screenWidth - WIDTH - MARGIN_RIGHT;
        int x1     = screenWidth - MARGIN_RIGHT;
        int listY0 = MARGIN_TOP + HEADER_H + 1;

        if (mx < x0 || mx > x1) return false;
        if (my < listY0 || my > listY0 + LIST_H) return false;

        int maxScroll = Math.max(0, capasDePageActual().size() - MAX_VISIBLE);
        scrollOffset -= (int) Math.signum(delta);
        scrollOffset  = Math.max(0, Math.min(scrollOffset, maxScroll));
        return true;
    }

    public static boolean handleClick(double mx, double my, int screenWidth, int screenHeight) {
        if (!isVisible || !GlobalGuiSettings.editorActivo) return false;
        int x0    = screenWidth - WIDTH - MARGIN_RIGHT;
        int x1    = screenWidth - MARGIN_RIGHT;
        int yBase = MARGIN_TOP;

        if (mx < x0 || mx > x1 || my < yBase || my > yBase + PANEL_H) return false;

        int plusX = x1 - 19;
        if (my >= yBase + 2 && my <= yBase + 20 && mx >= plusX) {
            return true;
        }

        int botY = yBase + PANEL_H - BOTTOM_H;
        if (my >= botY) {
            int btnW = 24, gap = (WIDTH - 2 - 4 * btnW) / 5;
            for (int b = 0; b < 4; b++) {
                int bx = x0 + 1 + gap + b * (btnW + gap);
                if (mx >= bx && mx <= bx + btnW) {
                    switch (b) {
                        case 0: accionDuplicar(); break;
                        case 1: accionSubir();    break;
                        case 2: accionBajar();    break;
                        case 3: accionEliminar(); break;
                    }
                    return true;
                }
            }
            return true;
        }

        java.util.List<GlobalGuiSettings.Capa> capas = capasDePageActual();
        int listY0 = yBase + HEADER_H + 1;

        for (int slot = 0; slot < MAX_VISIBLE; slot++) {
            int idx   = slot + scrollOffset;
            int itemY = listY0 + slot * ITEM_H;

            if (my >= itemY && my < itemY + ITEM_H) {
                if (idx >= capas.size()) return true;
                GlobalGuiSettings.Capa capa = capas.get(idx);

                if (mx >= x0 + 3 && mx <= x0 + 18) {
                    capa.visible = !capa.visible;
                    if (capa.panel != null) capa.panel.visible = capa.visible;
                    if (capa.texto != null) capa.texto.visible = capa.visible;
                    return true;
                }
                if (mx >= x1 - 16) {
                    return true;
                }
                GlobalGuiSettings.panelSeleccionado = capa.panel;
                GlobalGuiSettings.textoSeleccionado = capa.texto;
                return true;
            }
        }
        return true;
    }

    private static java.util.List<GlobalGuiSettings.Capa> capasDePageActual() {
        java.util.List<GlobalGuiSettings.Capa> lista = new java.util.ArrayList<>();
        for (int i = GlobalGuiSettings.CAPAS_UI.size() - 1; i >= 0; i--) {
            GlobalGuiSettings.Capa c = GlobalGuiSettings.CAPAS_UI.get(i);
            if (c.pagina == GlobalGuiSettings.paginaActual || c.pagina == 0) lista.add(c);
        }
        return lista;
    }

    private static void renderButton(GuiGraphics g, int x, int y, int w, int h, int bg, int bord) {
        g.fill(x, y, x + w, y + h, bg);
        g.fill(x, y, x + w, y + 1, 0xFF707070);
        g.fill(x, y, x + 1, y + h, 0xFF707070);
        g.fill(x, y + h - 1, x + w, y + h, bord);
        g.fill(x + w - 1, y, x + w, y + h, bord);
    }

    private static void dibujarThumbnail(GuiGraphics g, int x, int y) {
        for (int r = 0; r < THUMB_SIZE; r += 4)
            for (int c = 0; c < THUMB_SIZE; c += 4)
                g.fill(x+c, y+r, x+c+4, y+r+4, ((r/4+c/4)%2==0) ? 0xFFCCCCCC : 0xFFAAAAAA);
        g.renderOutline(x, y, THUMB_SIZE, THUMB_SIZE, 0xFF2A2A2A);
    }

    private static void dibujarOjo(GuiGraphics g, int x, int y, boolean v) {
        int col = v ? 0xFFCCCCCC : 0xFF666666;
        g.fill(x+2,y,  x+8,y+1,col); g.fill(x+1,y+1,x+9,y+2,col);
        g.fill(x,y+2,  x+10,y+3,col); g.fill(x+1,y+3,x+9,y+4,col);
        g.fill(x+2,y+4,x+8,y+5,col);
        g.fill(x+2,y+1,x+8,y+4, v ? 0xFF5A5A5A : 0xFF4A4A4A);
        if (v) g.fill(x+4,y+1,x+6,y+4,0xFF8ECFFF);
    }

    private static void dibujarHamburguesa(GuiGraphics g, int x, int y) {
        g.fill(x,y,   x+9,y+1,0xFFAAAAAA);
        g.fill(x,y+3, x+9,y+4,0xFFAAAAAA);
        g.fill(x,y+6, x+9,y+7,0xFFAAAAAA);
    }

    private static void renderBotonesInferiores(GuiGraphics g, Font font, int x0, int x1, int botY) {
        int panelW = x1 - x0;
        int btnW = 24, btnH = 16, gap = (panelW - 2 - 4 * btnW) / 5;
        int by = botY + (BOTTOM_H - btnH) / 2;
        for (int b = 0; b < 4; b++)
            renderButton(g, x0 + 1 + gap + b * (btnW + gap), by, btnW, btnH, 0xFF4A4A4A, 0xFF2A2A2A);
        int b0 = x0 + 1 + gap;
        dibujarIconoDuplicar(g, b0 + 4,              by + 2);
        dibujarFlechaArriba( g, b0 + (btnW+gap) + 7, by + 2);
        dibujarFlechaAbajo(  g, b0 + 2*(btnW+gap)+7, by + 2);
        dibujarPapelera(     g, b0 + 3*(btnW+gap)+5, by + 1);
    }

    private static void dibujarIconoDuplicar(GuiGraphics g, int x, int y) {
        g.renderOutline(x+3,y,   9,9,0xFFCCCCCC);
        g.fill(x,y+3,x+9,y+12,0xFF4A4A4A);
        g.renderOutline(x,y+3,   9,9,0xFFCCCCCC);
    }
    private static void dibujarFlechaArriba(GuiGraphics g, int x, int y) {
        int c=0xFFCCCCCC;
        g.fill(x+3,y,x+4,y+1,c); g.fill(x+2,y+1,x+5,y+2,c);
        g.fill(x+1,y+2,x+6,y+3,c); g.fill(x,y+3,x+7,y+4,c);
        g.fill(x+3,y+4,x+4,y+11,c);
    }
    private static void dibujarFlechaAbajo(GuiGraphics g, int x, int y) {
        int c=0xFFCCCCCC;
        g.fill(x+3,y,x+4,y+7,c); g.fill(x,y+7,x+7,y+8,c);
        g.fill(x+1,y+8,x+6,y+9,c); g.fill(x+2,y+9,x+5,y+10,c);
        g.fill(x+3,y+10,x+4,y+11,c);
    }
    private static void dibujarPapelera(GuiGraphics g, int x, int y) {
        int c=0xFFCCCCCC;
        g.fill(x+1,y,x+9,y+1,c); g.fill(x+3,y-2,x+7,y,c);
        g.fill(x,y+1,x+10,y+2,c); g.fill(x,y+2,x+1,y+12,c);
        g.fill(x+9,y+2,x+10,y+12,c); g.fill(x,y+11,x+10,y+12,c);
        g.fill(x+3,y+3,x+4,y+10,c); g.fill(x+5,y+3,x+6,y+10,c); g.fill(x+7,y+3,x+8,y+10,c);
    }

    private static void accionDuplicar() { }
    private static void accionSubir()    { }
    private static void accionBajar()    { }
    private static void accionEliminar() { }
}