package questgrupo.questmod.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import questgrupo.questmod.Config;
import questgrupo.questmod.client.GlobalGuiSettings;
import java.util.Collections;
import java.util.List;

public class FigurasEdit {
    public static int editandoColorIndex = 0; // 0=Fondo, 1=Borde, 2=Texto, 3=FondoMision, 4=BordeMision
    public static int EditandoMaestroTitle = 0; // 0=Normal, 1=Principales, 2=Secundarias

    private static long ultimoClicPanel = 0;
    private static GlobalGuiSettings.PanelConfig ultimoPanelClickeado = null;

    public static boolean esDobleClic(GlobalGuiSettings.PanelConfig p) {
        long ahora = System.currentTimeMillis();
        boolean esDoble = (p == ultimoPanelClickeado && (ahora - ultimoClicPanel) < 250);
        ultimoClicPanel = ahora;
        ultimoPanelClickeado = p;
        return esDoble;
    }

    public static void crearCuadrado(int x, int y) {
        GlobalGuiSettings.PanelConfig p = new GlobalGuiSettings.PanelConfig(x, y, 50, 50);
        p.tipo = "CUADRADO";
        GlobalGuiSettings.PANELES.add(p);
    }

    public static void crearRectangulo(int x, int y) {
        GlobalGuiSettings.PanelConfig p = new GlobalGuiSettings.PanelConfig(x, y, 80, 40);
        p.tipo = "RECTANGULO";
        GlobalGuiSettings.PANELES.add(p);
    }

    public static void crearTriangulo(int x, int y) {
        GlobalGuiSettings.PanelConfig p = new GlobalGuiSettings.PanelConfig(x - 25, y - 20, 50, 40);
        p.tipo = "TRIANGULO";
        p.grosor = 1; // For pixelated style
        GlobalGuiSettings.PANELES.add(p);
    }

    public static void crearCirculo(int x, int y) {
        GlobalGuiSettings.PanelConfig p = new GlobalGuiSettings.PanelConfig(x - 20, y - 20, 40, 40);
        p.tipo = "CIRCULO";
        GlobalGuiSettings.PANELES.add(p);
    }

    public static void crearMisionTarjeta(int x, int y, String nombreMision, net.minecraft.resources.ResourceLocation icono) {
        GlobalGuiSettings.PanelConfig p = new GlobalGuiSettings.PanelConfig(x, y, 120, 40);
        p.tipo = "RECTANGULO";
        p.textoAsociado = nombreMision;
        p.iconoRL = icono;
        p.colorARGB = 0xFF444444; // Dark gray background
        p.colorBorde = 0xFFAAAAAA; // Light gray border
        GlobalGuiSettings.PANELES.add(p);
    }

    public static void crearDesplegable(int x, int y, int type) {
        GlobalGuiSettings.PanelConfig p = new GlobalGuiSettings.PanelConfig(x, y, 160, 200);
        if (type == 2) {
            p.tipo = "DESPLEGABLE_MAESTRO";
            p.textoAsociado = "Libro de Misiones";
        } else {
            p.tipo = type == 0 ? "DESPLEGABLE_PRINCIPAL" : "DESPLEGABLE_SECUNDARIA";
            p.textoAsociado = type == 0 ? "Misiones Principales" : "Misiones Secundarias";
        }
        p.colorARGB = 0xAA000000;
        p.colorBorde = 0xFFFFFFFF;
        p.iconoRL = net.minecraft.resources.ResourceLocation.parse("minecraft:textures/item/book.png");
        GlobalGuiSettings.PANELES.add(p);
    }

    public static void crearDetalleMision(int x, int y) {
        GlobalGuiSettings.PanelConfig p = new GlobalGuiSettings.PanelConfig(x, y, 220, 240);
        p.tipo = "DETALLE_MISION";
        p.textoAsociado = "Selecciona una mision";
        p.colorARGB = 0xEE1A1A1A;
        p.colorBorde = 0xFFA6A6A6;
        GlobalGuiSettings.PANELES.add(p);
    }

    public static void crearPiezaMision(String tipo, int x, int y) {
        int ancho = 150;
        int alto = 30;
        if (tipo.equals("MISION_DESCRIPCION")) alto = 60;
        if (tipo.equals("MISION_OBJETIVOS")) alto = 100;
        
        GlobalGuiSettings.PanelConfig p = new GlobalGuiSettings.PanelConfig(x, y, ancho, alto);
        p.tipo = tipo;
        p.colorARGB = 0xAA222222;
        p.colorBorde = 0xFFA6A6A6;
        GlobalGuiSettings.PANELES.add(p);
    }

    private static void drawLine1px(GuiGraphics g, int x0, int y0, int x1, int y1, int color) {
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

    private static void drawLineThick(GuiGraphics g, int x0, int y0, int x1, int y1, int color, int grosor) {
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
            g.fill(x0 - halfGrosor, y0 - halfGrosor, x0 - halfGrosor + grosor, y0 - halfGrosor + grosor, color);
            
            if (x0 == x1 && y0 == y1) break;
            int e2 = 2 * err;
            if (e2 > -dy) { err -= dy; x0 += sx; }
            if (e2 < dx) { err += dx; y0 += sy; }
        }
    }

    private static void drawDummyMission(GuiGraphics g, net.minecraft.client.gui.Font font, GlobalGuiSettings.PanelConfig p, int cardY, int marginX, int cardWidth, int cardHeight, String text) {
        g.fill(p.x + marginX, cardY, p.x + marginX + cardWidth, cardY + cardHeight, p.colorFondoMision);
        g.renderOutline(p.x + marginX, cardY, cardWidth, cardHeight, p.colorBordeMision);
        float textX = p.x + marginX + 5;
        if (p.iconoRL != null) {
            float baseIconSize = 16;
            float scaledIconSize = baseIconSize * p.escalaIcono;
            float iconBaseX = p.x + marginX + 5; 
            float iconX = iconBaseX + p.offsetXIcono;
            float iconY = cardY + (cardHeight - scaledIconSize) / 2;
            g.pose().pushPose();
            g.pose().translate(iconX, iconY, 0);
            g.pose().scale(p.escalaIcono, p.escalaIcono, 1.0f);
            g.blit(p.iconoRL, 0, 0, 0, 0, (int)baseIconSize, (int)baseIconSize, (int)baseIconSize, (int)baseIconSize);
            g.pose().popPose();
            textX = iconBaseX + scaledIconSize + 5;
        }
        g.pose().pushPose();
        g.pose().translate(textX + p.offsetXTextoMision, cardY + (cardHeight - font.lineHeight * p.escalaTextoMision) / 2, 0);
        g.pose().scale(p.escalaTextoMision, p.escalaTextoMision, 1.0f);
        g.drawString(font, text, 0, 0, 0xFFFFFFFF, false);
        g.pose().popPose();
    }

    public static void renderizar(GuiGraphics g, GlobalGuiSettings.PanelConfig p, boolean seleccionado, boolean escribiendo) {
        if (p.tipo.equals("LINEA")) {
            // Draw line from (x,y) to (x2,y2) using Bresenham
            if (p.x2 != 0 || p.y2 != 0) {
                drawLineThick(g, p.x, p.y, p.x2, p.y2, p.colorARGB, p.grosor);
            }
        } else if (p.tipo.equals("CUADRADO") || p.tipo.equals("RECTANGULO")) {
            g.fill(p.x, p.y, p.x + p.ancho, p.y + p.alto, p.colorARGB);
            int t = 1;
            g.fill(p.x, p.y, p.x + p.ancho, p.y + t, p.colorBorde);
            g.fill(p.x, p.y + p.alto - t, p.x + p.ancho, p.y + p.alto, p.colorBorde);
            g.fill(p.x, p.y, p.x + t, p.y + p.alto, p.colorBorde);
            g.fill(p.x + p.ancho - t, p.y, p.x + p.ancho, p.y + p.alto, p.colorBorde);
        } else if (p.tipo.equals("TRIANGULO")) {
            // Minecraft-style pixelated triangle (staircase diagonal)
            int centerX = p.x + p.ancho / 2;
            int bottomY = p.y + p.alto;
            // Draw from bottom corners to top center
            for (int dy = 0; dy < p.alto; dy++) {
                int progress = dy * p.ancho / 2 / p.alto;
                int lineStart = centerX - progress;
                int lineEnd = centerX + progress;
                if (lineStart < p.x) lineStart = p.x;
                if (lineEnd > p.x + p.ancho) lineEnd = p.x + p.ancho;
                g.fill(lineStart, bottomY - dy, lineEnd, bottomY - dy + 1, p.colorARGB);
            }
        } else if (p.tipo.equals("CIRCULO")) {
            // Minecraft-style pixel circle (midpoint circle algorithm simplified)
            int centerX = p.x + p.ancho/2;
            int centerY = p.y + p.alto/2;
            int radius = Math.min(p.ancho, p.alto)/2;
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dx = -radius; dx <= radius; dx++) {
                    if (dx*dx + dy*dy <= radius*radius) {
                        g.fill(centerX + dx, centerY + dy, centerX + dx + 1, centerY + dy + 1, p.colorARGB);
                    }
                }
            }
        } else if (p.tipo.equals("DETALLE_MISION")) {
            net.minecraft.client.gui.Font font = net.minecraft.client.Minecraft.getInstance().font;
            Config.MisionData data = null;
            
            for (java.util.List<Config.MisionData> lista : Config.misionesCargadas.values()) {
                for (Config.MisionData m : lista) {
                    if (m.nombre != null && m.nombre.equals(p.textoAsociado)) { data = m; break; }
                }
            }

            g.fill(p.x, p.y, p.x + p.ancho, p.y + p.alto, p.colorARGB);
            g.renderOutline(p.x, p.y, p.ancho, p.alto, p.colorBorde);

            if (data == null) {
                g.drawCenteredString(font, "Mision no seleccionada", p.x + p.ancho/2, p.y + p.alto/2, 0xFF888888);
                return;
            }

            // --- 1. NOMBRE (Centrado y único) ---
            g.pose().pushPose();
            float nX = p.x + (p.ancho / 2f) - (font.width(data.nombre) * p.escalaTexto / 2f) + p.offsetXTexto;
            float nY = p.y + 10 + p.offsetYTexto;
            g.pose().translate(nX, nY, 0);
            g.pose().scale(p.escalaTexto, p.escalaTexto, 1);
            g.drawString(font, data.nombre, 0, 0, p.colorTexto, false);
            if (GlobalGuiSettings.editorActivo && p.subElementoSel == 1) g.renderOutline(-2, -2, font.width(data.nombre) + 4, 12, 0xFFFF0000);
            g.pose().popPose();

            // --- 2. ICONO ---
            if (data.iconoRL != null) {
                g.pose().pushPose();
                float iX = p.x + 10 + p.offXIcon;
                float iY = p.y + 35 + p.offYIcon;
                g.pose().translate(iX, iY, 0);
                g.pose().scale(p.scaleIcon, p.scaleIcon, 1);
                g.blit(data.iconoRL, 0, 0, 0, 0, 32, 32, 32, 32);
                if (GlobalGuiSettings.editorActivo && p.subElementoSel == 2) g.renderOutline(-2, -2, 34, 34, 0xFFFF0000);
                g.pose().popPose();
            }

            // --- 3. DESCRIPCIÓN ---
            g.pose().pushPose();
            float dX = p.x + 50 + p.offXDesc;
            float dY = p.y + 35 + p.offYDesc;
            g.pose().translate(dX, dY, 0);
            g.pose().scale(p.scaleDesc, p.scaleDesc, 1);
            g.drawWordWrap(font, net.minecraft.network.chat.Component.literal(data.descripcion), 0, 0, (int)((p.ancho - 60) / p.scaleDesc), p.colorDesc);
            if (GlobalGuiSettings.editorActivo && p.subElementoSel == 3) g.renderOutline(-2, -2, (int)((p.ancho-60)/p.scaleDesc), 30, 0xFFFF0000);
            g.pose().popPose();

            // --- 4. OBJETIVOS (Con fix de superposición y color verde) ---
            g.pose().pushPose();
            g.pose().translate(p.x + 10 + p.offXObj, p.y + 80 + p.offYObj, 0);
            g.pose().scale(p.scaleObj, p.scaleObj, 1);
            int oY = 0;
            for (Config.Objetivo obj : data.objetivos) {
                int cantJugador = net.minecraft.client.Minecraft.getInstance().player.getInventory().countItem(obj.itemReal);
                boolean completado = cantJugador >= obj.cantidad;
                
                g.fill(0, oY, p.ancho - 20, oY + 22, completado ? 0x6600FF00 : 0x22FFFFFF);
                
                if (obj.itemReal != null) g.renderFakeItem(new net.minecraft.world.item.ItemStack(obj.itemReal), 2, oY + 3);
                
                String txtObj = obj.itemReal.getDescription().getString();
                String txtCant = cantJugador + " / " + obj.cantidad;
                
                g.drawString(font, txtObj, 22, oY + 7, p.colorObj);
                int cantWidth = font.width(txtCant);
                g.drawString(font, (completado ? "§a" : "§f") + txtCant, p.ancho - 45 - cantWidth, oY + 7, 0xFFFFFFFF);
                
                if (completado) g.drawString(font, "§a✔", p.ancho - 35, oY + 7, 0xFFFFFFFF);
                oY += 25;
            }
            g.pose().popPose();

            // --- 5. RECOMPENSAS ---
            g.pose().pushPose();
            g.pose().translate(p.x + 10 + p.offXRec, p.y + 180 + p.offYRec, 0);
            g.pose().scale(p.scaleRec, p.scaleRec, 1);
            int rX = 0;
            for (Config.Recompensa rec : data.recompensas) {
                g.fill(rX, 0, rX + 40, 20, 0x44000000);
                if (rec.itemReal != null) {
                    g.renderFakeItem(new net.minecraft.world.item.ItemStack(rec.itemReal), rX + 2, 2);
                    g.drawString(font, "x" + rec.cantidad, rX + 20, 10, p.colorRec);
                }
                rX += 45;
            }
            g.pose().popPose();
        } else if (p.tipo.equals("MISION_TITULO")) {
            net.minecraft.client.gui.Font font = net.minecraft.client.Minecraft.getInstance().font;
            Config.MisionData data = Config.getMisionPorNombre(GlobalGuiSettings.misionSeleccionadaGlobal);
            String texto = (data != null) ? data.nombre : "Titulo (Toca una mision)";
            
            g.fill(p.x, p.y, p.x + p.ancho, p.y + p.alto, p.colorARGB);
            g.renderOutline(p.x, p.y, p.ancho, p.alto, p.colorBorde);
            
            g.pose().pushPose();
            float nScale = p.escalaTexto;
            float nX = p.x + (p.ancho / 2f) - (font.width(texto) * nScale / 2f) + p.offsetXTexto;
            float nY = p.y + (p.alto / 2f) - (font.lineHeight * nScale / 2f) + p.offsetYTexto;
            g.pose().translate(nX, nY, 0);
            g.pose().scale(nScale, nScale, 1);
            g.drawString(font, texto, 0, 0, p.colorTexto, false);
            g.pose().popPose();
        } else if (p.tipo.equals("MISION_DESCRIPCION")) {
            net.minecraft.client.gui.Font font = net.minecraft.client.Minecraft.getInstance().font;
            Config.MisionData data = Config.getMisionPorNombre(GlobalGuiSettings.misionSeleccionadaGlobal);
            String texto = (data != null) ? data.descripcion : "Descripcion (Toca una mision)";
            
            g.fill(p.x, p.y, p.x + p.ancho, p.y + p.alto, p.colorARGB);
            g.renderOutline(p.x, p.y, p.ancho, p.alto, p.colorBorde);
            
            g.pose().pushPose();
            g.pose().translate(p.x + 5, p.y + 5, 0);
            g.pose().scale(p.escalaTexto, p.escalaTexto, 1);
            g.drawWordWrap(font, net.minecraft.network.chat.Component.literal(texto), 0, 0, (int)(p.ancho / p.escalaTexto), p.colorTexto);
            g.pose().popPose();
} else if (p.tipo.equals("MISION_OBJETIVOS")) {
            net.minecraft.client.gui.Font font = net.minecraft.client.Minecraft.getInstance().font;
            Config.MisionData data = Config.getMisionPorNombre(GlobalGuiSettings.misionSeleccionadaGlobal);
            
            g.fill(p.x, p.y, p.x + p.ancho, p.y + p.alto, p.colorARGB);
            g.renderOutline(p.x, p.y, p.ancho, p.alto, p.colorBorde);
            
            if (data != null) {
                int oY = 0;
                g.pose().pushPose();
                g.pose().translate(p.x + 5, p.y + 5, 0);
                g.pose().scale(p.escalaTexto, p.escalaTexto, 1);
                for (Config.Objetivo obj : data.objetivos) {
                    int cant = net.minecraft.client.Minecraft.getInstance().player.getInventory().countItem(obj.itemReal);
                    boolean ok = cant >= obj.cantidad;
                    
                    g.fill(0, oY, p.ancho - 10, oY + 22, ok ? 0x6600FF00 : 0x22FFFFFF);
                    g.renderOutline(0, oY, p.ancho - 10, 22, p.colorBorde);
                    
                    if (obj.itemReal != null) g.renderFakeItem(new net.minecraft.world.item.ItemStack(obj.itemReal), 2, oY + 3);
                    
                    String txt = cant + "/" + obj.cantidad;
                    g.drawString(font, txt, p.ancho - 45, oY + 8, 0xFFFFFFFF);
                    oY += 26;
                }
                g.pose().popPose();
            } else {
                g.drawCenteredString(font, "Lista de Objetivos", p.x + p.ancho/2, p.y + p.alto/2, 0xFF888888);
            }
        } else if (p.tipo.startsWith("DESPLEGABLE")) {
            int t = 1;
            net.minecraft.client.gui.Font font = net.minecraft.client.Minecraft.getInstance().font;

            if (p.tipo.equals("DESPLEGABLE_MAESTRO")) {
                // Maestro has no main header. It's just a background box.
                g.fill(p.x, p.y, p.x + p.ancho, p.y + p.alto, p.colorARGB);
                g.fill(p.x, p.y + p.alto - t, p.x + p.ancho, p.y + p.alto, p.colorBorde); // Bottom
                g.fill(p.x, p.y, p.x + p.ancho, p.y + t, p.colorBorde); // Top
                g.fill(p.x, p.y, p.x + t, p.y + p.alto, p.colorBorde); // Left
                g.fill(p.x + p.ancho - t, p.y, p.x + p.ancho, p.y + p.alto, p.colorBorde); // Right

                // Scissor test to clip contents
                g.enableScissor(p.x + t, p.y + t, p.x + p.ancho - t, p.y + p.alto - t);
                
                g.pose().pushPose();
                g.pose().translate(0, -p.scrollY, 0);

                int currentY = p.y + 5;
                int marginX = 5;
                int cardHeight = 30;
                int cardWidth = p.ancho - (marginX * 2);

                float titleScale = p.escalaTexto;
                float titleOffsetX = p.offsetXTexto;

                // Sub-header Principales
                g.fill(p.x + marginX, currentY, p.x + marginX + cardWidth, currentY + 15, p.colorFondoCabecera);
                g.renderOutline(p.x + marginX, currentY, cardWidth, 15, p.colorBordeCabecera);
                
                g.pose().pushPose();
                g.pose().translate(p.x + marginX + 5 + titleOffsetX, currentY + (15 - font.lineHeight * titleScale) / 2, 0);
                g.pose().scale(titleScale, titleScale, 1.0f);
                g.drawString(font, p.tituloPrincipales, 0, 0, p.colorTexto, false);
                if (escribiendo && EditandoMaestroTitle == 1 && (System.currentTimeMillis() % 1000 < 500)) {
                    g.drawString(font, "|", font.width(p.tituloPrincipales) + 1, 0, p.colorTexto, false);
                }
                g.pose().popPose();

                g.drawString(font, p.principalesAbierto ? "▼" : "▶", p.x + p.ancho - marginX - 12, currentY + 4, p.colorTexto, false);
                currentY += 20;

                if (p.principalesAbierto) {
                    List<String> principales = p.listaPrincipales != null ? p.listaPrincipales : List.of();
                    for (String missionName : principales) {
                        if (missionName != null && !missionName.isEmpty()) {
                            drawDummyMission(g, font, p, currentY, marginX, cardWidth, cardHeight, missionName);
                            currentY += cardHeight + 5;
                        }
                    }
                }

                // Sub-header Secundarias
                g.fill(p.x + marginX, currentY, p.x + marginX + cardWidth, currentY + 15, p.colorFondoCabecera);
                g.renderOutline(p.x + marginX, currentY, cardWidth, 15, p.colorBordeCabecera);

                g.pose().pushPose();
                g.pose().translate(p.x + marginX + 5 + titleOffsetX, currentY + (15 - font.lineHeight * titleScale) / 2, 0);
                g.pose().scale(titleScale, titleScale, 1.0f);
                g.drawString(font, p.tituloSecundarias, 0, 0, p.colorTexto, false);
                if (escribiendo && EditandoMaestroTitle == 2 && (System.currentTimeMillis() % 1000 < 500)) {
                    g.drawString(font, "|", font.width(p.tituloSecundarias) + 1, 0, p.colorTexto, false);
                }
                g.pose().popPose();

                g.drawString(font, p.secundariasAbierto ? "▼" : "▶", p.x + p.ancho - marginX - 12, currentY + 4, p.colorTexto, false);
                currentY += 20;

                if (p.secundariasAbierto) {
                    List<String> secundarias = p.listaSecundarias != null ? p.listaSecundarias : List.of();
                    for (String missionName : secundarias) {
                        if (missionName != null && !missionName.isEmpty()) {
                            drawDummyMission(g, font, p, currentY, marginX, cardWidth, cardHeight, missionName);
                            currentY += cardHeight + 5;
                        }
                    }
                }

                g.pose().popPose();
                g.disableScissor();

                // Invisible Scroll logic (no scrollbar drawn)
                int totalContentHeight = currentY - (p.y + 5);
                int bodyHeight = p.alto - 10;
                if (totalContentHeight > bodyHeight) {
                    int maxScrollY = totalContentHeight - bodyHeight;
                    if (p.scrollY < 0) p.scrollY = 0;
                    if (p.scrollY > maxScrollY) p.scrollY = maxScrollY;
                } else {
                    p.scrollY = 0;
                }

            } else {
                // Classic DESPLEGABLE_PRINCIPAL / SECUNDARIA
                int headerHeight = 20;
                int renderHeight = p.desplegado ? p.alto : headerHeight;

                // Draw header background and border
                g.fill(p.x, p.y, p.x + p.ancho, p.y + headerHeight, p.colorFondoCabecera);
                g.fill(p.x, p.y, p.x + p.ancho, p.y + t, p.colorBordeCabecera); // Top
                g.fill(p.x, p.y + headerHeight - t, p.x + p.ancho, p.y + headerHeight, p.colorBordeCabecera); // Bottom
                g.fill(p.x, p.y, p.x + t, p.y + headerHeight, p.colorBordeCabecera); // Left
                g.fill(p.x + p.ancho - t, p.y, p.x + p.ancho, p.y + headerHeight, p.colorBordeCabecera); // Right

                if (p.desplegado && p.alto > headerHeight) {
                    int bodyY = p.y + headerHeight;
                    int bodyHeight = p.alto - headerHeight;
                    g.fill(p.x, bodyY, p.x + p.ancho, bodyY + bodyHeight, p.colorARGB);
                    g.fill(p.x, bodyY + bodyHeight - t, p.x + p.ancho, bodyY + bodyHeight, p.colorBorde); // Bottom
                    g.fill(p.x, bodyY, p.x + t, bodyY + bodyHeight, p.colorBorde); // Left
                    g.fill(p.x + p.ancho - t, bodyY, p.x + p.ancho, bodyY + bodyHeight, p.colorBorde); // Right

                    g.enableScissor(p.x + t, bodyY, p.x + p.ancho - t, bodyY + bodyHeight - t);
                    g.pose().pushPose();
                    g.pose().translate(0, -p.scrollY, 0);

int currentY = bodyY + 5;
                    int marginX = 5;
                    int cardHeight = 30;
                    int cardWidth = p.ancho - (marginX * 2);
                    
                    List<String> misiones = p.tipo.equals("DESPLEGABLE_PRINCIPAL") 
                        ? p.listaPrincipales 
                        : p.listaSecundarias;
                    
                    for (String missionName : misiones) {
                        if (missionName != null && !missionName.isEmpty()) {
                            drawDummyMission(g, font, p, currentY, marginX, cardWidth, cardHeight, missionName);
                            currentY += cardHeight + 5;
                        }
                    }

                    g.pose().popPose();
                    g.disableScissor();

                    int totalContentHeight = currentY - (bodyY + 5);
                    if (totalContentHeight > bodyHeight) {
                        int maxScrollY = totalContentHeight - bodyHeight;
                        if (p.scrollY < 0) p.scrollY = 0;
                        if (p.scrollY > maxScrollY) p.scrollY = maxScrollY;
                    } else {
                        p.scrollY = 0;
                    }
                }

                // Draw arrow at right
                String flecha = p.desplegado ? "▼" : "▶";
                g.drawString(font, flecha, p.x + p.ancho - 15, p.y + 6, p.colorTexto, false);

                // Draw title at left
                float titleScale = p.escalaTexto;
                float titleOffsetX = p.offsetXTexto;
                g.pose().pushPose();
                g.pose().translate(p.x + 5 + titleOffsetX, p.y + (20 - font.lineHeight * titleScale) / 2, 0);
                g.pose().scale(titleScale, titleScale, 1.0f);
                g.drawString(font, p.textoAsociado, 0, 0, p.colorTexto, false);
                if (escribiendo && EditandoMaestroTitle == 0 && (System.currentTimeMillis() % 1000 < 500)) {
                    g.drawString(font, "|", font.width(p.textoAsociado) + 1, 0, p.colorTexto, false);
                }
                g.pose().popPose();
            }
        } else {
            g.fill(p.x, p.y, p.x + p.ancho, p.y + p.alto, p.colorARGB);
        }

        if (p.textoAsociado != null && (!p.textoAsociado.isEmpty() || escribiendo) && !p.tipo.startsWith("DESPLEGABLE")) {
            net.minecraft.client.gui.Font font = net.minecraft.client.Minecraft.getInstance().font;
            
            float scaledTextHeight = font.lineHeight * p.escalaTexto;
            float baseIconSize = 16;
            float scaledIconSize = baseIconSize * p.escalaIcono;
            
            // Icono: Posición base a la izquierda + offset manual
            if (p.iconoRL != null && !p.tipo.startsWith("DESPLEGABLE")) {
                float iconBaseX = p.x + 10; // 10px desde el borde izquierdo del panel
                float iconX = iconBaseX + p.offsetXIcono;
                float iconY = p.y + (p.alto - scaledIconSize) / 2;
                
                g.pose().pushPose();
                g.pose().translate(iconX, iconY, 0);
                g.pose().scale(p.escalaIcono, p.escalaIcono, 1.0f);
                g.blit(p.iconoRL, 0, 0, 0, 0, (int)baseIconSize, (int)baseIconSize, (int)baseIconSize, (int)baseIconSize);
                g.pose().popPose();
            }
            
            // Texto: Posición base central/derecha + offset manual
            // Ponemos el texto en una posición fija (ej: 35px desde la izquierda) para que no sea empujado
            float textBaseX = p.x + 35; 
            float textX = textBaseX + p.offsetXTexto;
            float textY = p.y + (p.alto - scaledTextHeight) / 2;
            if (p.tipo.startsWith("DESPLEGABLE")) {
                textY = p.y + (20 - scaledTextHeight) / 2; // 20 is headerHeight
            }
            
            g.pose().pushPose();
            g.pose().translate(textX, textY, 0);
            g.pose().scale(p.escalaTexto, p.escalaTexto, 1.0f);
            g.drawString(font, p.textoAsociado, 0, 0, p.colorTexto, false);
            if (escribiendo && (System.currentTimeMillis() % 1000 < 500)) {
                int textWidth = font.width(p.textoAsociado);
                g.drawString(font, "|", textWidth + 1, 0, p.colorTexto, false);
            }
            g.pose().popPose();
        }

        if (seleccionado) {
            g.renderOutline(p.x - 1, p.y - 1, p.ancho + 2, p.alto + 2, 0xFFFFFF00);
            if (p.tipo.equals("CUADRADO") || p.tipo.equals("RECTANGULO") || p.tipo.startsWith("DESPLEGABLE")) {
                g.fill(p.x + p.ancho - 5, p.y + p.alto - 5, p.x + p.ancho, p.y + p.alto, 0xFFFFFFFF);
            }
        }
    }

    public static boolean mouseSobreFigura(double mx, double my, GlobalGuiSettings.PanelConfig p) {
        return mx >= p.x && mx <= p.x + p.ancho && my >= p.y && my <= p.y + p.alto;
    }

    public static boolean sobreEsquinaRedimension(double mx, double my, GlobalGuiSettings.PanelConfig p) {
        return mx >= p.x + p.ancho - 7 && mx <= p.x + p.ancho && my >= p.y + p.alto - 7 && my <= p.y + p.alto;
    }

    public static void eliminarFigura(GlobalGuiSettings.PanelConfig p) {
        if (p != null) {
            GlobalGuiSettings.PANELES.remove(p);
        }
    }

    public static void moverFiguraAlFrente(GlobalGuiSettings.PanelConfig p) {
        if (p == null) return;
        GlobalGuiSettings.PANELES.remove(p);
        GlobalGuiSettings.PANELES.add(p);
    }

    public static void moverFiguraAlFondo(GlobalGuiSettings.PanelConfig p) {
        if (p == null) return;
        GlobalGuiSettings.PANELES.remove(p);
        GlobalGuiSettings.PANELES.add(0, p);
    }

    public static void moverFiguraAdelante(GlobalGuiSettings.PanelConfig p) {
        if (p == null) return;
        int idx = GlobalGuiSettings.PANELES.indexOf(p);
        if (idx > -1 && idx < GlobalGuiSettings.PANELES.size() - 1) {
            Collections.swap(GlobalGuiSettings.PANELES, idx, idx + 1);
        }
    }

    public static void moverFiguraAtras(GlobalGuiSettings.PanelConfig p) {
        if (p == null) return;
        int idx = GlobalGuiSettings.PANELES.indexOf(p);
        if (idx > 0) {
            Collections.swap(GlobalGuiSettings.PANELES, idx, idx - 1);
        }
    }

    public static void drawStringCustom(GuiGraphics g, net.minecraft.client.gui.Font font, String text, float x, float y, int color, float spacing) {
        float curX = x;
        for (char c : text.toCharArray()) {
            String s = String.valueOf(c);
            g.drawString(font, s, (int)curX, (int)y, color, false);
            curX += font.width(s) + spacing;
        }
    }
}
