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
     
    public static GlobalGuiSettings.Capa capaArrastrada = null;
    public static double mouseDragY = 0;
    
    private static long lastClickTime = 0;
    private static GlobalGuiSettings.Capa lastClickedCapa = null;
    public static GlobalGuiSettings.Capa capaEditandoNombre = null;

    public static boolean isVisible = true;

    public static int getWidth() {
        return isVisible && GlobalGuiSettings.editorActivo ? WIDTH + MARGIN_RIGHT : 0;
    }

    public static void render(GuiGraphics g, int screenWidth, int screenHeight, int mouseX, int mouseY) {
        if (!isVisible || !GlobalGuiSettings.editorActivo) return;

        int x0    = screenWidth - WIDTH - MARGIN_RIGHT;
        int x1    = screenWidth - MARGIN_RIGHT;
        int yBase = MARGIN_TOP;

        Font font = Minecraft.getInstance().font;

        g.fill(x0, yBase, x1, yBase + PANEL_H, 0xFFC6C6C6);
        
        g.fill(x0, yBase, x1, yBase + 1, 0xFFFFFFFF); 
        g.fill(x0, yBase, x0 + 1, yBase + PANEL_H, 0xFFFFFFFF); 
        g.fill(x0, yBase + PANEL_H - 1, x1, yBase + PANEL_H, 0xFF555555); 
        g.fill(x1 - 1, yBase, x1, yBase + PANEL_H, 0xFF555555); 
        g.renderOutline(x0 - 1, yBase - 1, (x1 - x0) + 2, PANEL_H + 2, 0xFF000000); 

        int hY0 = yBase;
        int hY1 = yBase + HEADER_H;
        
        g.drawString(font, "CAPAS " + GlobalGuiSettings.paginaActual, x0 + 7, hY0 + (HEADER_H - font.lineHeight) / 2 + 1, 0xFF202020, false);

        int plusX = x1 - 19;
        int plusY = hY0 + (HEADER_H - 16) / 2;
        boolean hoverPlus = mouseX >= plusX && mouseX <= plusX + 16 && mouseY >= plusY && mouseY <= plusY + 16;
        dibujarBotonVanilla(g, plusX, plusY, 16, 16, hoverPlus);
        g.drawString(font, "+", plusX + 5, plusY + 4, 0xFF202020, false);

        int listX0 = x0 + 3;
        int listX1 = x1 - 3;
        int listY0 = hY1;
        int listY1 = listY0 + LIST_H;

        g.fill(listX0, listY0, listX1, listY1, 0xFFC6C6C6); 

        java.util.List<GlobalGuiSettings.Capa> capas = capasDePageActual();
        int maxScroll = Math.max(0, capas.size() - MAX_VISIBLE);
        scrollOffset  = Math.max(0, Math.min(scrollOffset, maxScroll));

        for (int slot = 0; slot < MAX_VISIBLE; slot++) {
            int idx   = slot + scrollOffset;
            int itemY = listY0 + slot * ITEM_H;

            if (idx >= capas.size()) continue;
            GlobalGuiSettings.Capa capa = capas.get(idx);
            
            if (capaArrastrada != null && capa == capaArrastrada) {
                g.fill(listX0 + 1, itemY, listX1 - 1, itemY + ITEM_H, 0xFF888888);
                continue;
            }

            boolean sel = (capa.panel != null && capa.panel == GlobalGuiSettings.panelSeleccionado)
                        || (capa.texto != null && capa.texto == GlobalGuiSettings.textoSeleccionado)
                        || (capa.dibujo != null && capa.dibujo == GlobalGuiSettings.dibujoSeleccionado);

            if (sel) {
                g.fill(listX0 + 1, itemY, listX1 - 1, itemY + ITEM_H, 0xFFAAAAAA);
            }
            
            g.fill(listX0 + 1, itemY + ITEM_H - 1, listX1 - 1, itemY + ITEM_H, 0xFF888888);
            g.fill(listX0 + ITEM_H, itemY, listX0 + ITEM_H + 1, itemY + ITEM_H, 0xFF888888);

            int eyeX = listX0 + (ITEM_H - 10) / 2;
            int eyeY = itemY + (ITEM_H - 5) / 2;
            dibujarOjo(g, eyeX, eyeY, capa.visible);

            int tx = listX0 + ITEM_H + 6;
            int ty = itemY + (ITEM_H - THUMB_SIZE) / 2;
            dibujarThumbnail(g, tx, ty);

            String nombre = (capa.nombre != null) ? capa.nombre : "Capa";
            int textColor = capa.visible ? 0xFF202020 : 0xFF777777;

            if (capa == capaEditandoNombre) {
                textColor = 0xFF0055FF;
                nombre = nombre + "_";
            } else {
                if (nombre.length() > 9) nombre = nombre.substring(0, 7) + ".."; 
            }

            int textX = tx + THUMB_SIZE + 5;
            int textY = itemY + (ITEM_H - (int)(font.lineHeight * TEXT_SCALE)) / 2;
            dibujarTextoEscalado(g, font, nombre, textX, textY, textColor);

            dibujarHamburguesa(g, listX1 - 11, itemY + (ITEM_H - 7) / 2);
        }
        
        g.renderOutline(listX0, listY0, listX1 - listX0, listY1 - listY0, 0xFF000000);

        int botY = listY1;
        renderBotonesInferiores(g, font, x0, x1, botY, mouseX, mouseY);
        
        if (capaArrastrada != null) {
            int ghostY = (int) mouseDragY - (ITEM_H / 2);
            g.fill(listX0 + 1, ghostY, listX1 - 1, ghostY + ITEM_H, 0x99AAAAAA); 
            String nombre = (capaArrastrada.nombre != null) ? capaArrastrada.nombre : "Capa";
            if (nombre.length() > 9) nombre = nombre.substring(0, 7) + ".."; 
            dibujarTextoEscalado(g, font, nombre, listX0 + ITEM_H + 6 + THUMB_SIZE + 5, ghostY + (ITEM_H - (int)(font.lineHeight * TEXT_SCALE)) / 2, 0xCC202020);
        }
    }

    private static void renderBotonesInferiores(GuiGraphics g, Font font, int x0, int x1, int botY, int mx, int my) {
        int panelW = x1 - x0;
        int btnW = 24, btnH = 22, gap = (panelW - 2 - 4 * btnW) / 5;
        int by = botY + (BOTTOM_H - btnH) / 2;
        
        for (int b = 0; b < 4; b++) {
            int bx = x0 + 1 + gap + b * (btnW + gap);
            boolean hover = mx >= bx && mx <= bx + btnW && my >= by && my <= by + btnH;
            dibujarBotonVanilla(g, bx, by, btnW, btnH, hover);
        }
        
        int b0 = x0 + 1 + gap;
        int c = 0xFF202020; 
        
        dibujarIconoDuplicar(g, b0 + 6,              by + 5, c);
        dibujarFlechaArriba( g, b0 + (btnW+gap) + 8, by + 5, c);
        dibujarFlechaAbajo(  g, b0 + 2*(btnW+gap)+8, by + 5, c);
        dibujarPapelera(     g, b0 + 3*(btnW+gap)+7, by + 5, c);
    }

    private static void dibujarBotonVanilla(GuiGraphics g, int x, int y, int w, int h, boolean hovered) {
        g.fill(x, y, x + w, y + h, 0xFF000000); 
        
        int bg = hovered ? 0xFFE8E8E8 : 0xFFC6C6C6; 
        
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, bg); 
        g.fill(x + 1, y + 1, x + w - 1, y + 2, 0xFFFFFFFF); 
        g.fill(x + 1, y + 1, x + 2, y + h - 1, 0xFFFFFFFF); 
        g.fill(x + 1, y + h - 2, x + w - 1, y + h - 1, 0xFF555555); 
        g.fill(x + w - 2, y + 1, x + w - 1, y + h - 1, 0xFF555555); 
        
        g.fill(x + 1, y + h - 2, x + 2, y + h - 1, bg); 
        g.fill(x + w - 2, y + 1, x + w - 1, y + 2, bg); 
    }

    private static void dibujarIconoDuplicar(GuiGraphics g, int x, int y, int color) {
        g.renderOutline(x, y, 8, 8, color); 
        g.fill(x + 3, y + 3, x + 11, y + 11, 0xFFC6C6C6); 
        g.renderOutline(x + 3, y + 3, 8, 8, color); 
    }

    private static void dibujarFlechaArriba(GuiGraphics g, int x, int y, int color) {
        g.fill(x + 3, y, x + 5, y + 1, color);
        g.fill(x + 2, y + 1, x + 6, y + 2, color);
        g.fill(x + 1, y + 2, x + 7, y + 3, color);
        g.fill(x, y + 3, x + 8, y + 4, color);
        g.fill(x + 3, y + 4, x + 5, y + 11, color);
    }

    private static void dibujarFlechaAbajo(GuiGraphics g, int x, int y, int color) {
        g.fill(x + 3, y, x + 5, y + 7, color);
        g.fill(x, y + 7, x + 8, y + 8, color);
        g.fill(x + 1, y + 8, x + 7, y + 9, color);
        g.fill(x + 2, y + 9, x + 6, y + 10, color);
        g.fill(x + 3, y + 10, x + 5, y + 11, color);
    }

    private static void dibujarPapelera(GuiGraphics g, int x, int y, int color) {
        g.fill(x + 3, y, x + 7, y + 1, color); 
        g.fill(x, y + 1, x + 10, y + 2, color); 
        g.fill(x + 1, y + 2, x + 2, y + 10, color); 
        g.fill(x + 8, y + 2, x + 9, y + 10, color); 
        g.fill(x + 1, y + 10, x + 9, y + 11, color); 
        g.fill(x + 3, y + 3, x + 4, y + 9, color); 
        g.fill(x + 6, y + 3, x + 7, y + 9, color); 
    }

    private static void dibujarTextoEscalado(GuiGraphics g, Font font, String text, int x, int y, int color) {
        g.pose().pushPose();
        g.pose().translate(x, y, 0);
        g.pose().scale(TEXT_SCALE, TEXT_SCALE, 1f);
        g.drawString(font, text, 0, 0, color, false);
        g.pose().popPose();
    }

    private static void dibujarThumbnail(GuiGraphics g, int x, int y) {
        for (int r = 0; r < THUMB_SIZE; r += 4)
            for (int c = 0; c < THUMB_SIZE; c += 4)
                g.fill(x+c, y+r, x+c+4, y+r+4, ((r/4+c/4)%2==0) ? 0xFFE0E0E0 : 0xFFC6C6C6);
        g.renderOutline(x, y, THUMB_SIZE, THUMB_SIZE, 0xFF888888);
    }

    private static void dibujarOjo(GuiGraphics g, int x, int y, boolean v) {
        if (v) {
            int oy = y - 1; 
            int c = 0xFF000000;      
            int w = 0xFFFFFFFF;      
            int p = 0xFF1A1A1A;      
            int gColor = 0xFF555555; 

            g.fill(x + 4, oy + 1, x + 7, oy + 2, w);
            g.fill(x + 2, oy + 2, x + 9, oy + 3, w);
            g.fill(x + 1, oy + 3, x + 10, oy + 4, w);
            g.fill(x + 2, oy + 4, x + 9, oy + 5, w);
            g.fill(x + 4, oy + 5, x + 7, oy + 6, w);

            g.fill(x + 5, oy + 2, x + 6, oy + 5, p); 
            g.fill(x + 4, oy + 3, x + 7, oy + 4, p); 
            
            g.fill(x + 4, oy + 2, x + 5, oy + 3, gColor);
            g.fill(x + 6, oy + 2, x + 7, oy + 3, gColor);
            g.fill(x + 4, oy + 4, x + 5, oy + 5, gColor);
            g.fill(x + 6, oy + 4, x + 7, oy + 5, gColor);

            g.fill(x + 4, oy,     x + 7, oy + 1, c); 
            g.fill(x + 4, oy + 6, x + 7, oy + 7, c); 
            
            g.fill(x + 2, oy + 1, x + 4, oy + 2, c); 
            g.fill(x + 7, oy + 1, x + 9, oy + 2, c); 
            g.fill(x + 2, oy + 5, x + 4, oy + 6, c); 
            g.fill(x + 7, oy + 5, x + 9, oy + 6, c); 
            
            g.fill(x + 1, oy + 2, x + 2, oy + 3, c); 
            g.fill(x + 9, oy + 2, x + 10,oy + 3, c); 
            g.fill(x + 1, oy + 4, x + 2, oy + 5, c); 
            g.fill(x + 9, oy + 4, x + 10,oy + 5, c); 
            
            g.fill(x,     oy + 3, x + 1, oy + 4, c); 
            g.fill(x + 10,oy + 3, x + 11,oy + 4, c); 
            
        } else {
            g.renderOutline(x + 2, y, 7, 7, 0xFF666666); 
        }
    }

    private static void dibujarHamburguesa(GuiGraphics g, int x, int y) {
        g.fill(x,y,   x+9,y+1,0xFF444444);
        g.fill(x,y+3, x+9,y+4,0xFF444444);
        g.fill(x,y+6, x+9,y+7,0xFF444444);
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
            accionNuevaCapa();
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

                if (mx >= x0 + 3 && mx <= x0 + 3 + ITEM_H) {
                    capa.visible = !capa.visible;
                    if (capa.panel != null) capa.panel.visible = capa.visible;
                    if (capa.texto != null) capa.texto.visible = capa.visible;
                    return true;
                }
                if (mx >= x1 - 16) {
                    capaEditandoNombre = null;
                    return true; 
                }
                
                long now = System.currentTimeMillis();
                if (capa == lastClickedCapa && (now - lastClickTime) < 500) {
                    if (capa.texto == null) {
                        capaEditandoNombre = capa;
                        return true;
                    }
                }
                
                capaEditandoNombre = null;
                lastClickTime = now;
                lastClickedCapa = capa;

                GlobalGuiSettings.panelSeleccionado = capa.panel;
                GlobalGuiSettings.textoSeleccionado = capa.texto;
                GlobalGuiSettings.dibujoSeleccionado = capa.dibujo; 
                
                capaArrastrada = capa;
                mouseDragY = my;
                
                return true;
            }
        }
        return true;
    }

    private static java.util.List<GlobalGuiSettings.Capa> capasDePageActual() {
        java.util.List<GlobalGuiSettings.Capa> lista = new java.util.ArrayList<>();
        for (int i = GlobalGuiSettings.CAPAS_UI.size() - 1; i >= 0; i--) {
            GlobalGuiSettings.Capa c = GlobalGuiSettings.CAPAS_UI.get(i);
            if (c.pagina == GlobalGuiSettings.paginaActual) lista.add(c);
        }
        return lista;
    }

    private static int obtenerIndiceSeleccionado() {
        for (int i = 0; i < GlobalGuiSettings.CAPAS_UI.size(); i++) {
            GlobalGuiSettings.Capa c = GlobalGuiSettings.CAPAS_UI.get(i);
            if ((c.panel != null && c.panel == GlobalGuiSettings.panelSeleccionado) ||
                (c.texto != null && c.texto == GlobalGuiSettings.textoSeleccionado)) {
                return i;
            }
        }
        return -1;
    }

    private static void guardarOrdenCapas() {
        java.util.List<GlobalGuiSettings.PanelConfig> nuevosPaneles = new java.util.ArrayList<>();
        java.util.List<GlobalGuiSettings.TextConfig> nuevosTextos = new java.util.ArrayList<>();
        java.util.List<GlobalGuiSettings.GrupoDibujo> nuevosDibujos = new java.util.ArrayList<>();
        for (GlobalGuiSettings.Capa c : GlobalGuiSettings.CAPAS_UI) {
            if (c.panel != null) nuevosPaneles.add(c.panel);
            if (c.texto != null) nuevosTextos.add(c.texto);
            if (c.dibujo != null) nuevosDibujos.add(c.dibujo);
        }
        GlobalGuiSettings.PANELES.clear();
        GlobalGuiSettings.PANELES.addAll(nuevosPaneles);
        GlobalGuiSettings.TEXTOS.clear();
        GlobalGuiSettings.TEXTOS.addAll(nuevosTextos);
        GlobalGuiSettings.DIBUJOS.clear();
        GlobalGuiSettings.DIBUJOS.addAll(nuevosDibujos);
    }

    private static void accionDuplicar() {
        if (GlobalGuiSettings.panelSeleccionado != null) {
            GlobalGuiSettings.PanelConfig p = GlobalGuiSettings.panelSeleccionado;
            GlobalGuiSettings.PanelConfig copia = new GlobalGuiSettings.PanelConfig(p.x + 15, p.y + 15, p.ancho, p.alto);
            copia.tipo = p.tipo;
            copia.colorARGB = p.colorARGB;
            copia.colorBorde = p.colorBorde;
            copia.textoAsociado = p.textoAsociado;
            copia.pagina = p.pagina;
            GlobalGuiSettings.PANELES.add(copia);
            GlobalGuiSettings.panelSeleccionado = copia;
        } else if (GlobalGuiSettings.textoSeleccionado != null) {
            GlobalGuiSettings.TextConfig t = GlobalGuiSettings.textoSeleccionado;
            GlobalGuiSettings.TextConfig copia = new GlobalGuiSettings.TextConfig(t.contenido, t.x + 15, t.y + 15);
            copia.escala = t.escala;
            copia.colorARGB = t.colorARGB;
            copia.pagina = t.pagina;
            GlobalGuiSettings.TEXTOS.add(copia);
            GlobalGuiSettings.textoSeleccionado = copia;
        }
        GlobalGuiSettings.sincronizarCapas();
    }

    private static void accionSubir() {
        int idx = obtenerIndiceSeleccionado();
        if (idx >= 0 && idx < GlobalGuiSettings.CAPAS_UI.size() - 1) {
            GlobalGuiSettings.Capa c = GlobalGuiSettings.CAPAS_UI.remove(idx);
            GlobalGuiSettings.CAPAS_UI.add(idx + 1, c);
            guardarOrdenCapas();
        }
    }

    private static void accionBajar() {
        int idx = obtenerIndiceSeleccionado();
        if (idx > 0) {
            GlobalGuiSettings.Capa c = GlobalGuiSettings.CAPAS_UI.remove(idx);
            GlobalGuiSettings.CAPAS_UI.add(idx - 1, c);
            guardarOrdenCapas();
        }
    }

    private static void accionEliminar() {
        if (GlobalGuiSettings.panelSeleccionado != null) {
            GlobalGuiSettings.PANELES.remove(GlobalGuiSettings.panelSeleccionado);
            GlobalGuiSettings.panelSeleccionado = null;
        } else if (GlobalGuiSettings.textoSeleccionado != null) {
            GlobalGuiSettings.TEXTOS.remove(GlobalGuiSettings.textoSeleccionado);
            GlobalGuiSettings.textoSeleccionado = null;
        } else if (GlobalGuiSettings.dibujoSeleccionado != null) {
            GlobalGuiSettings.DIBUJOS.remove(GlobalGuiSettings.dibujoSeleccionado);
            GlobalGuiSettings.dibujoSeleccionado = null;
        }
        GlobalGuiSettings.sincronizarCapas();
    }

    public static boolean handleMouseDragged(double mx, double my, int button, int screenWidth, int screenHeight) {
        if (!isVisible || capaArrastrada == null) return false;
        mouseDragY = my;

        int listY0 = MARGIN_TOP + HEADER_H;
        int slotActual = (int) ((my - listY0) / ITEM_H) + scrollOffset;
        
        java.util.List<GlobalGuiSettings.Capa> capas = capasDePageActual();
        if (slotActual >= 0 && slotActual < capas.size()) {
            GlobalGuiSettings.Capa capaDestino = capas.get(slotActual);
            
            if (capaDestino != capaArrastrada) {
                int indexOrigen = GlobalGuiSettings.CAPAS_UI.indexOf(capaArrastrada);
                int indexDestino = GlobalGuiSettings.CAPAS_UI.indexOf(capaDestino);
                
                if (indexOrigen != -1 && indexDestino != -1) {
                    GlobalGuiSettings.CAPAS_UI.remove(indexOrigen);
                    GlobalGuiSettings.CAPAS_UI.add(indexDestino, capaArrastrada);
                }
            }
        }
        return true; 
    }

    public static boolean handleMouseReleased(double mx, double my, int button) {
        if (capaArrastrada != null) {
            capaArrastrada = null; 
            
            java.util.List<GlobalGuiSettings.PanelConfig> nuevosPaneles = new java.util.ArrayList<>();
            java.util.List<GlobalGuiSettings.TextConfig> nuevosTextos = new java.util.ArrayList<>();
            java.util.List<GlobalGuiSettings.GrupoDibujo> nuevosDibujos = new java.util.ArrayList<>();
            
            for (GlobalGuiSettings.Capa c : GlobalGuiSettings.CAPAS_UI) {
                if (c.panel != null) nuevosPaneles.add(c.panel);
                if (c.texto != null) nuevosTextos.add(c.texto);
                if (c.dibujo != null) nuevosDibujos.add(c.dibujo);
            }
            
            GlobalGuiSettings.PANELES.clear();
            GlobalGuiSettings.PANELES.addAll(nuevosPaneles);
            
            GlobalGuiSettings.TEXTOS.clear();
            GlobalGuiSettings.TEXTOS.addAll(nuevosTextos);
            
            GlobalGuiSettings.DIBUJOS.clear();
            GlobalGuiSettings.DIBUJOS.addAll(nuevosDibujos);
            
            return true;
        }
        return false;
    }

    private static void accionNuevaCapa() {
        GlobalGuiSettings.GrupoDibujo nuevoDibujo = new GlobalGuiSettings.GrupoDibujo();
        nuevoDibujo.pagina = GlobalGuiSettings.paginaActual;
        GlobalGuiSettings.DIBUJOS.add(nuevoDibujo);
        GlobalGuiSettings.sincronizarCapas();
        
        for (GlobalGuiSettings.Capa c : GlobalGuiSettings.CAPAS_UI) {
            if (c.dibujo == nuevoDibujo) {
                GlobalGuiSettings.panelSeleccionado = null;
                GlobalGuiSettings.textoSeleccionado = null;
                GlobalGuiSettings.dibujoSeleccionado = nuevoDibujo;
                capaEditandoNombre = c; 
                break;
            }
        }
    }
}