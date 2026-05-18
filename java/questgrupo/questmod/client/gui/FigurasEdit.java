package questgrupo.questmod.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import questgrupo.questmod.Config;
import questgrupo.questmod.client.GlobalGuiSettings;
import net.minecraft.client.Minecraft;
import java.util.Collections;
import java.util.List;

public class FigurasEdit {
    public static int editandoColorIndex = 0; // 0=Fondo, 1=Borde, 2=Texto, 3=FondoMision, 4=BordeMision, 7=RecuadroMisionTexto, 8=BordeMisionTexto, 9=TextoMision
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
        p.pagina = GlobalGuiSettings.paginaActual;
        GlobalGuiSettings.PANELES.add(p);
    }

    public static void crearRectangulo(int x, int y) {
        GlobalGuiSettings.PanelConfig p = new GlobalGuiSettings.PanelConfig(x, y, 80, 40);
        p.tipo = "RECTANGULO";
        p.pagina = GlobalGuiSettings.paginaActual;
        GlobalGuiSettings.PANELES.add(p);
    }

    public static void crearTriangulo(int x, int y) {
        GlobalGuiSettings.PanelConfig p = new GlobalGuiSettings.PanelConfig(x - 25, y - 20, 50, 40);
        p.tipo = "TRIANGULO";
        p.grosor = 1;
        p.pagina = GlobalGuiSettings.paginaActual;
        GlobalGuiSettings.PANELES.add(p);
    }

    public static void crearCirculo(int x, int y) {
        GlobalGuiSettings.PanelConfig p = new GlobalGuiSettings.PanelConfig(x - 20, y - 20, 40, 40);
        p.tipo = "CIRCULO";
        p.pagina = GlobalGuiSettings.paginaActual;
        GlobalGuiSettings.PANELES.add(p);
    }

public static void crearBotonPagina(int numPagina) {
        GlobalGuiSettings.PanelConfig p = new GlobalGuiSettings.PanelConfig(50, 50 + (numPagina * 30), 40, 40);
        p.tipo = "BOTON_PAGINA";
        p.textoAsociado = String.valueOf(numPagina);
        p.colorARGB = 0xFF888888;
        p.pagina = 0; 
        p.escalaTexto = 1.2f;
        GlobalGuiSettings.PANELES.add(p);
    }

    public static void crearMisionTarjeta(int x, int y, String nombreMision, net.minecraft.resources.ResourceLocation icono) {
        GlobalGuiSettings.PanelConfig p = new GlobalGuiSettings.PanelConfig(x, y, 120, 40);
        p.tipo = "RECTANGULO";
        p.textoAsociado = nombreMision;
        p.iconoRL = icono;
        p.colorARGB = 0xFF444444; // Dark gray background
        p.colorBorde = 0xFFAAAAAA; // Light gray border
        p.pagina = GlobalGuiSettings.paginaActual;
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
        p.pagina = GlobalGuiSettings.paginaActual;
        GlobalGuiSettings.PANELES.add(p);
    }

    public static void crearDetalleMision(int x, int y) {
        GlobalGuiSettings.PanelConfig p = new GlobalGuiSettings.PanelConfig(x, y, 220, 240);
        p.tipo = "DETALLE_MISION";
        p.textoAsociado = "Selecciona una mision";
        p.colorARGB = 0xEE1A1A1A;
        p.colorBorde = 0xFFA6A6A6;
        p.pagina = GlobalGuiSettings.paginaActual;
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
        p.pagina = GlobalGuiSettings.paginaActual;
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
        if (grosor <= 1) {
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
        // ─── RENDERING EN 3D DEL MANIQUÍ RPG ───
        if ("MANIQUI".equals(p.tipo)) {
            Player player = Minecraft.getInstance().player;
            if (player != null) {
                // 1. Fondo y borde editables desde la TopBar
                g.fill(p.x, p.y, p.x + p.ancho, p.y + p.alto, p.colorARGB);
                g.renderOutline(p.x, p.y, p.ancho, p.alto, seleccionado ? 0xFFFFFF00 : p.colorBorde);

                // 2. Calculamos el centro y aplicamos la escala independiente (usando escalaIcono como multiplicador)
                int centroX = p.x + (p.ancho / 2);
                int baseY = p.y + p.alto - 8; 
                int escala = (int) (p.alto * 0.45F * p.escalaIcono); 

                // 3. Seguimiento real del ratón
                double mouseX = Minecraft.getInstance().mouseHandler.xpos() * (double) g.guiWidth() / (double) Minecraft.getInstance().getWindow().getScreenWidth();
                double mouseY = Minecraft.getInstance().mouseHandler.ypos() * (double) g.guiHeight() / (double) Minecraft.getInstance().getWindow().getScreenHeight();

                float rotY = (float) (centroX - mouseX);
                float rotX = (float) (baseY - (p.alto * 0.45f) - mouseY);

                // 4. Invocamos el renderizador nativo en 3D de Minecraft
                g.pose().pushPose();
                InventoryScreen.renderEntityInInventoryFollowsMouse(g, centroX, baseY, escala, rotY, rotX, player);
                g.pose().popPose();
            }
            return; 
        }

        // ─── RENDERING DEL WIDGET DE PROGRESO ───
        if ("PROGRESO".equals(p.tipo)) {
            // 1. DIBUJAR FONDO GENERAL DEL RECUADRO (Si tiene opacidad/color)
            if (p.colorARGB != 0) {
                g.fill(p.x, p.y, p.x + p.ancho, p.y + p.alto, p.colorARGB);
            }
            
            String textoMostrar = "";
            boolean dibujarBarra = false;
            float porcentajeLlenado = 0.0f;
            
            int maxProgreso = 1;
            int actualProgreso = 0;

            if (GlobalGuiSettings.editorActivo) {
                maxProgreso = 100;
                actualProgreso = 65; // 65% simulado en el editor
                if (p.progresoTipo == 2) textoMostrar = "02h 45m";
            } else {
                net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
                if (p.progresoTipo == 0) {
                    maxProgreso = 0;
                    for (java.util.List<Config.MisionData> lista : Config.misionesCargadas.values()) {
                        maxProgreso += lista.size();
                    }
                    if (maxProgreso == 0) maxProgreso = 1; 
                    actualProgreso = questgrupo.questmod.events.ClickAldeano.getMisionesCompletadasCount();
                } else if (p.progresoTipo == 1) {
                    maxProgreso = 30; 
                    actualProgreso = mc.player != null ? mc.player.experienceLevel : 0;
                } else if (p.progresoTipo == 2) {
                    if (mc.level != null) {
                        long totalSecs = mc.level.getGameTime() / 20; 
                        long hours = totalSecs / 3600;
                        long minutes = (totalSecs % 3600) / 60;
                        textoMostrar = hours + "h " + minutes + "m";
                    }
                } else if (p.progresoTipo == 3) {
                    maxProgreso = 53; 
                    actualProgreso = 12; // Muestra de biomas
                }
            }

            if (p.progresoTipo != 2) { 
                porcentajeLlenado = Math.min(1.0f, (float) actualProgreso / maxProgreso);
                if (p.progresoEstilo == 0) {
                    dibujarBarra = true;
                } else if (p.progresoEstilo == 1) {
                    textoMostrar = (int)(porcentajeLlenado * 100) + "%";
                } else if (p.progresoEstilo == 2) {
                    textoMostrar = actualProgreso + " / " + maxProgreso;
                }
            }

            if (dibujarBarra) {
                // 2. DIBUJAR LA BARRA DE PROGRESO (Ocupa el área limpia exacta)
                int anchoLleno = (int) (p.ancho * porcentajeLlenado);
                if (porcentajeLlenado >= 1.0f) {
                    anchoLleno = p.ancho;
                }
                if (anchoLleno > 0) {
                    g.fill(p.x, p.y, p.x + anchoLleno, p.y + p.alto, p.colorBarraLleno); 
                }
            } else if (!textoMostrar.isEmpty()) {
                // Dibujar Texto centrado con escala de botones + y -
                net.minecraft.client.gui.Font font = net.minecraft.client.Minecraft.getInstance().font;
                float scale = p.escalaTexto; 
                float tX = p.x + (p.ancho - (font.width(textoMostrar) * scale)) / 2 + p.offsetXTexto;
                float tY = p.y + (p.alto - (font.lineHeight * scale)) / 2 + p.offsetYTexto;

                g.pose().pushPose();
                g.pose().translate(tX, tY, 0);
                g.pose().scale(scale, scale, 1.0f);
                
                net.minecraft.network.chat.Style estilo = net.minecraft.network.chat.Style.EMPTY
                    .withBold(p.negrita).withItalic(p.cursiva).withUnderlined(p.subrayado).withStrikethrough(p.tachado);
                
                g.drawString(font, net.minecraft.network.chat.Component.literal(textoMostrar).setStyle(estilo), 0, 0, p.colorTexto, p.sombra);
                g.pose().popPose();
            }

            // 3. DIBUJAR BORDE EXTERIOR (1 píxel expandido por fuera para contener todo perfectamente)
            g.renderOutline(p.x - 1, p.y - 1, p.ancho + 2, p.alto + 2, p.colorBorde);

            if (seleccionado) g.renderOutline(p.x - 2, p.y - 2, p.ancho + 4, p.alto + 4, 0xFFFFFF00);
            return;
        } 

        // ─── RENDERING DEL WIDGET DE ESTADÍSTICAS RPG ───
        if (p.tipo != null && p.tipo.startsWith("ESTADISTICA_")) {
            // Fondo general opcional del recuadro
            g.fill(p.x, p.y, p.x + p.ancho, p.y + p.alto, p.colorARGB);
            
            String textoMostrar = "";
            net.minecraft.world.entity.player.Player player = net.minecraft.client.Minecraft.getInstance().player;
            
            if (player != null) {
                if (p.tipo.equals("ESTADISTICA_SALUD")) {
                    int hp = (int) Math.ceil(player.getHealth());
                    int maxHp = (int) Math.ceil(player.getMaxHealth());
                    textoMostrar = hp + " / " + maxHp;
                } else if (p.tipo.equals("ESTADISTICA_DANO")) {
                    double dmg = player.getAttributeBaseValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
                    
                    net.minecraft.world.item.ItemStack arma = player.getMainHandItem();
                    if (!arma.isEmpty()) {
                        com.google.common.collect.Multimap<net.minecraft.world.entity.ai.attributes.Attribute, net.minecraft.world.entity.ai.attributes.AttributeModifier> modificadores = arma.getAttributeModifiers(net.minecraft.world.entity.EquipmentSlot.MAINHAND);
                        for (net.minecraft.world.entity.ai.attributes.AttributeModifier mod : modificadores.get(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE)) {
                            dmg += mod.getAmount();
                        }
                        
                        dmg += net.minecraft.world.item.enchantment.EnchantmentHelper.getDamageBonus(arma, net.minecraft.world.entity.MobType.UNDEFINED);
                    }
                    
                    if (player.hasEffect(net.minecraft.world.effect.MobEffects.DAMAGE_BOOST)) {
                        dmg += 3.0 * (player.getEffect(net.minecraft.world.effect.MobEffects.DAMAGE_BOOST).getAmplifier() + 1);
                    }
                    if (player.hasEffect(net.minecraft.world.effect.MobEffects.WEAKNESS)) {
                        dmg -= 4.0 * (player.getEffect(net.minecraft.world.effect.MobEffects.WEAKNESS).getAmplifier() + 1);
                    }

                    textoMostrar = String.valueOf((int) Math.max(0, dmg));
                } else if (p.tipo.equals("ESTADISTICA_DEFENSA")) {
                    int armor = player.getArmorValue();
                    textoMostrar = String.valueOf(armor);
                } else if (p.tipo.equals("ESTADISTICA_VELOCIDAD")) {
                    double baseSpeed = 0.10000000149011612; // Valor base interno en Minecraft
                    double currentSpeed = player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);
                    int percent = (int) Math.round((currentSpeed / baseSpeed) * 100);
                    textoMostrar = percent + "%";
                }
            } else if (GlobalGuiSettings.editorActivo) {
                if (p.tipo.equals("ESTADISTICA_SALUD")) textoMostrar = "20 / 20";
                else if (p.tipo.equals("ESTADISTICA_DANO")) textoMostrar = "5";
                else if (p.tipo.equals("ESTADISTICA_DEFENSA")) textoMostrar = "8";
                else if (p.tipo.equals("ESTADISTICA_VELOCIDAD")) textoMostrar = "100%";
            }

            net.minecraft.client.gui.Font font = net.minecraft.client.Minecraft.getInstance().font;
            float scale = p.escalaTexto; 
            // Centrado perfecto dentro de tu marco
            float tX = p.x + (p.ancho - (font.width(textoMostrar) * scale)) / 2 + p.offsetXTexto;
            float tY = p.y + (p.alto - (font.lineHeight * scale)) / 2 + p.offsetYTexto;

            g.pose().pushPose();
            g.pose().translate(tX, tY, 0);
            g.pose().scale(scale, scale, 1.0f);
            
            net.minecraft.network.chat.Style estilo = net.minecraft.network.chat.Style.EMPTY
                .withBold(p.negrita).withItalic(p.cursiva).withUnderlined(p.subrayado).withStrikethrough(p.tachado);
            
            g.drawString(font, net.minecraft.network.chat.Component.literal(textoMostrar).setStyle(estilo), 0, 0, p.colorTexto, p.sombra);
            g.pose().popPose();

            if (seleccionado) g.renderOutline(p.x - 1, p.y - 1, p.ancho + 2, p.alto + 2, 0xFFFFFF00);
            return;
        }

        // ─── RENDERING DE LA HOTBAR (ACCESO RÁPIDO CARRUSEL) ───
        if ("HOTBAR".equals(p.tipo)) {
            int slotSize = p.slotSize;
            int gap = p.gap;
            
            // Dibuja el fondo libremente con el tamaño que elijas en el editor
            g.fill(p.x, p.y, p.x + p.ancho, p.y + p.alto, p.colorARGB);

            // Centrado automático de los slots dentro del fondo
            int totalW = p.isVertical ? slotSize : (p.visibleSlots * slotSize) + ((p.visibleSlots - 1) * gap);
            int totalH = p.isVertical ? (p.visibleSlots * slotSize) + ((p.visibleSlots - 1) * gap) : slotSize;
            int startX = p.x + (p.ancho - totalW) / 2;
            int startY = p.y + (p.alto - totalH) / 2;

            Player player = Minecraft.getInstance().player;
            for (int i = 0; i < p.visibleSlots; i++) {
                int realIndex = p.scrollIndex + i;
                if (realIndex >= 9) break;

                int slotX = startX + (p.isVertical ? 0 : i * (slotSize + gap));
                int slotY = startY + (p.isVertical ? i * (slotSize + gap) : 0);

                g.fill(slotX, slotY, slotX + slotSize, slotY + slotSize, p.colorSlotBg);
                g.fill(slotX, slotY, slotX + slotSize - 1, slotY + 1, p.colorSlotDark);
                g.fill(slotX, slotY, slotX + 1, slotY + slotSize - 1, p.colorSlotDark);
                g.fill(slotX + 1, slotY + slotSize - 1, slotX + slotSize, slotY + slotSize, p.colorSlotLight);
                g.fill(slotX + slotSize - 1, slotY + 1, slotX + slotSize, slotY + slotSize, p.colorSlotLight);

                if (player != null) {
                    net.minecraft.world.item.ItemStack item = player.getInventory().items.get(realIndex);
                    if (!item.isEmpty()) {
                        g.pose().pushPose();
                        float scaleFactor = (float)slotSize / 18.0f;
                        float iconOffset = (slotSize - (16 * scaleFactor)) / 2.0f;
                        g.pose().translate(slotX + iconOffset, slotY + iconOffset, 0);
                        g.pose().scale(scaleFactor, scaleFactor, 1.0f);
                        g.renderFakeItem(item, 0, 0);
                        g.renderItemDecorations(Minecraft.getInstance().font, item, 0, 0);
                        g.pose().popPose();
                    } else if (GlobalGuiSettings.editorActivo) {
                        String num = String.valueOf(realIndex + 1);
                        int nw = Minecraft.getInstance().font.width(num);
                        g.drawString(Minecraft.getInstance().font, num, slotX + (slotSize - nw)/2, slotY + (slotSize - 8)/2, 0x55FFFFFF, false);
                    }
                }
            }
            if (seleccionado) g.renderOutline(p.x - 1, p.y - 1, p.ancho + 2, p.alto + 2, 0xFFFFFF00);
            return;
        }

        // ─── RENDERING DEL INVENTARIO CUADRÍCULA ───
        if ("INVENTORY_GRID".equals(p.tipo)) {
            int slotSize = p.slotSize;
            int gap = p.gap;
            Player player = Minecraft.getInstance().player;
            int totalInvSlots = 27; 
            int columnas = p.columnas > 0 ? p.columnas : 9;
            int filas = (int) Math.ceil((double)totalInvSlots / columnas);
            
            g.fill(p.x, p.y, p.x + p.ancho, p.y + p.alto, p.colorARGB); 

            // Centrado de la cuadrícula
            int totalW = (columnas * slotSize) + ((columnas - 1) * gap);
            int totalH = (filas * slotSize) + ((filas - 1) * gap);
            int startX = p.x + (p.ancho - totalW) / 2;
            int startY = p.y + (p.alto - totalH) / 2;
            
            for (int i = 0; i < totalInvSlots; i++) {
                int col = i % columnas;
                int fil = i / columnas;
                int slotX = startX + col * (slotSize + gap);
                int slotY = startY + fil * (slotSize + gap);
                
                g.fill(slotX, slotY, slotX + slotSize, slotY + slotSize, p.colorSlotBg);
                g.fill(slotX, slotY, slotX + slotSize - 1, slotY + 1, p.colorSlotDark);
                g.fill(slotX, slotY, slotX + 1, slotY + slotSize - 1, p.colorSlotDark);
                g.fill(slotX + 1, slotY + slotSize - 1, slotX + slotSize, slotY + slotSize, p.colorSlotLight);
                g.fill(slotX + slotSize - 1, slotY + 1, slotX + slotSize, slotY + slotSize, p.colorSlotLight);
                
                if (player != null) {
                    int realIndex = i + 9; 
                    if (realIndex < player.getInventory().items.size()) {
                        net.minecraft.world.item.ItemStack item = player.getInventory().items.get(realIndex);
                        if (!item.isEmpty()) {
                            g.pose().pushPose();
                            float scaleFactor = (float)slotSize / 18.0f;
                            float iconOffset = (slotSize - (16 * scaleFactor)) / 2.0f;
                            g.pose().translate(slotX + iconOffset, slotY + iconOffset, 0);
                            g.pose().scale(scaleFactor, scaleFactor, 1.0f);
                            g.renderFakeItem(item, 0, 0);
                            g.renderItemDecorations(Minecraft.getInstance().font, item, 0, 0);
                            g.pose().popPose();
                        }
                    }
                }
            }
            if (seleccionado) g.renderOutline(p.x - 1, p.y - 1, p.ancho + 2, p.alto + 2, 0xFFFFFF00);
            return;
        }

        // ─── RENDERING DEL SLOT DE INVENTARIO RPG (DINÁMICO) ───
        if (p.tipo != null && p.tipo.startsWith("SLOT")) {
            // Fondo general del slot (por si le quieres dar un recuadro oscuro de fondo)
            g.fill(p.x, p.y, p.x + p.ancho, p.y + p.alto, p.colorARGB);
            
            int slotSize = p.slotSize;
            int slotX = p.x + (p.ancho - slotSize) / 2;
            int slotY = p.y + (p.alto - slotSize) / 2;
            
            g.fill(slotX, slotY, slotX + slotSize, slotY + slotSize, p.colorSlotBg);
            g.fill(slotX, slotY, slotX + slotSize - 1, slotY + 1, p.colorSlotDark);
            g.fill(slotX, slotY, slotX + 1, slotY + slotSize - 1, p.colorSlotDark);
            g.fill(slotX + 1, slotY + slotSize - 1, slotX + slotSize, slotY + slotSize, p.colorSlotLight);
            g.fill(slotX + slotSize - 1, slotY + 1, slotX + slotSize, slotY + slotSize, p.colorSlotLight);

            float scaleFactor = (float)slotSize / 18.0f;
            float iconOffset = (slotSize - (16 * scaleFactor)) / 2.0f;
            
            net.minecraft.world.item.ItemStack renderItem = net.minecraft.world.item.ItemStack.EMPTY;
            net.minecraft.resources.ResourceLocation watermark = null;
            net.minecraft.world.entity.player.Player player = net.minecraft.client.Minecraft.getInstance().player;

            if (player != null) {
                switch (p.tipo) {
                    case "SLOT_CASCO":   
                        renderItem = player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD);
                        if (renderItem.isEmpty()) watermark = net.minecraft.resources.ResourceLocation.parse("minecraft:textures/item/empty_armor_slot_helmet.png");
                        break;
                    case "SLOT_PECHERA": 
                        renderItem = player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST);
                        if (renderItem.isEmpty()) watermark = net.minecraft.resources.ResourceLocation.parse("minecraft:textures/item/empty_armor_slot_chestplate.png");
                        break;
                    case "SLOT_PANTALON":
                        renderItem = player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.LEGS);
                        if (renderItem.isEmpty()) watermark = net.minecraft.resources.ResourceLocation.parse("minecraft:textures/item/empty_armor_slot_leggings.png");
                        break;
                    case "SLOT_BOTAS":   
                        renderItem = player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.FEET);
                        if (renderItem.isEmpty()) watermark = net.minecraft.resources.ResourceLocation.parse("minecraft:textures/item/empty_armor_slot_boots.png");
                        break;
                    case "SLOT_ESCUDO":  
                        renderItem = player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.OFFHAND);
                        if (renderItem.isEmpty()) watermark = net.minecraft.resources.ResourceLocation.parse("minecraft:textures/item/empty_armor_slot_shield.png");
                        break;
                }
            }

            g.pose().pushPose();
            g.pose().translate(slotX + iconOffset, slotY + iconOffset, 0);
            g.pose().scale(scaleFactor, scaleFactor, 1.0f);

            if (!renderItem.isEmpty()) {
                g.renderFakeItem(renderItem, 0, 0);
                g.renderItemDecorations(net.minecraft.client.Minecraft.getInstance().font, renderItem, 0, 0);
            } else if (watermark != null) {
                com.mojang.blaze3d.systems.RenderSystem.enableBlend();
                com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 0.6f);
                g.blit(watermark, 0, 0, 0, 0, 16, 16, 16, 16);
                com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
                com.mojang.blaze3d.systems.RenderSystem.disableBlend();
            }
            g.pose().popPose();

            if (seleccionado) g.renderOutline(p.x - 1, p.y - 1, p.ancho + 2, p.alto + 2, 0xFFFFFF00);
            return; 
        }

        if (p.tipo.equals("LINEA")) {
            if (p.x2 != 0 || p.y2 != 0) {
                drawLineThick(g, p.x, p.y, p.x2, p.y2, p.colorARGB, p.grosor);
            }
        } else if (p.tipo.equals("CUADRADO") || p.tipo.equals("RECTANGULO") || p.tipo.equals("BOTON_PAGINA")) {
            g.fill(p.x, p.y, p.x + p.ancho, p.y + p.alto, p.colorARGB);
            int t = 1;
            g.fill(p.x, p.y, p.x + p.ancho, p.y + t, p.colorBorde);
            g.fill(p.x, p.y + p.alto - t, p.x + p.ancho, p.y + p.alto, p.colorBorde);
            g.fill(p.x, p.y, p.x + t, p.y + p.alto, p.colorBorde);
            g.fill(p.x + p.ancho - t, p.y, p.x + p.ancho, p.y + p.alto, p.colorBorde);
        } else if (p.tipo.equals("TRIANGULO")) {
            int centerX = p.x + p.ancho / 2;
            int bottomY = p.y + p.alto;
            for (int dy = 0; dy < p.alto; dy++) {
                int progress = dy * p.ancho / 2 / p.alto;
                int lineStart = centerX - progress;
                int lineEnd = centerX + progress;
                if (lineStart < p.x) lineStart = p.x;
                if (lineEnd > p.x + p.ancho) lineEnd = p.x + p.ancho;
                g.fill(lineStart, bottomY - dy, lineEnd, bottomY - dy + 1, p.colorARGB);
            }
        } else if (p.tipo.equals("CIRCULO")) {
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

            // --- 1. NOMBRE ---
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

            // --- 4. OBJETIVOS (Con Checkbox y Colores) ---
            g.pose().pushPose();
            g.pose().translate(p.x + 10 + p.offXObj, p.y + 80 + p.offYObj, 0);
            g.pose().scale(p.scaleObj, p.scaleObj, 1);
            int oY = 0;
            for (Config.Objetivo obj : data.objetivos) {
                int cantJugador = net.minecraft.client.Minecraft.getInstance().player.getInventory().countItem(obj.itemReal);
                boolean completado = cantJugador >= obj.cantidad;

                g.fill(0, oY, p.ancho - 20, oY + 22, p.colorFondoRenglon);

                g.fill(5, oY + 2, 23, oY + 20, p.colorFondoIcono);
                g.renderOutline(5, oY + 2, 18, 18, p.colorBordeIcono);
                if (obj.itemReal != null) g.renderFakeItem(new net.minecraft.world.item.ItemStack(obj.itemReal), 6, oY + 3);

                String txtObj = obj.itemReal.getDescription().getString();
                g.drawString(font, txtObj, 28, oY + 7, p.colorObj, true);

                int boxSize = 11;
                int boxX = p.ancho - 20 - 18;
                int boxY = oY + 5;

                g.renderOutline(boxX - 1, boxY - 1, boxSize + 2, boxSize + 2, p.colorBordeCheckExterno);

                g.fill(boxX, boxY, boxX + boxSize, boxY + boxSize, completado ? 0xFF00AA00 : p.colorFondoCheck);
                g.renderOutline(boxX, boxY, boxSize, boxSize, completado ? 0xFF00FF00 : p.colorBordeCheckInterno);

                if (completado) g.drawString(font, "✔", boxX + 3, boxY + 3, 0xFF00FF00, false);

                g.renderOutline(0, oY, p.ancho - 20, 22, p.colorBordeRenglon);

                String txtCant = cantJugador + " / " + obj.cantidad;
                int cantWidth = font.width(txtCant);
                g.drawString(font, (completado ? "§a" : "§c") + txtCant, boxX - 6 - cantWidth, oY + 7, 0xFFFFFFFF, true);

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
                    g.drawString(font, "x" + rec.cantidad, rX + 20, 10, p.colorRec, true);
                }
                rX += 45;
            }
            g.pose().popPose();
        } else if (p.tipo.equals("MISION_TITULO")) {
            net.minecraft.client.gui.Font font = net.minecraft.client.Minecraft.getInstance().font;
            Config.MisionData data = Config.getMisionPorNombre(GlobalGuiSettings.misionSeleccionadaGlobal);
            String texto = (data != null) ? data.nombre : "Titulo (Toca una mision)";
            if (p.mayusculas) texto = texto.toUpperCase();

            g.fill(p.x, p.y, p.x + p.ancho, p.y + p.alto, p.colorARGB);
            g.renderOutline(p.x, p.y, p.ancho, p.alto, p.colorBorde);

            net.minecraft.network.chat.Style estilo = net.minecraft.network.chat.Style.EMPTY
                    .withBold(p.negrita).withItalic(p.cursiva).withUnderlined(p.subrayado).withStrikethrough(p.tachado);

            g.pose().pushPose();
            float nScale = p.escalaTexto;
            float nX = p.x + (p.ancho / 2f) - (font.width(texto) * nScale / 2f) + p.offsetXTexto;
            float nY = p.y + (p.alto / 2f) - (font.lineHeight * nScale / 2f) + p.offsetYTexto;
            g.pose().translate(nX, nY, 0);
            g.pose().scale(nScale, nScale, 1);
            g.drawString(font, net.minecraft.network.chat.Component.literal(texto).setStyle(estilo), 0, 0, p.colorTexto, p.sombra);
            g.pose().popPose();
        } else if (p.tipo.equals("MISION_DESCRIPCION")) {
            net.minecraft.client.gui.Font font = net.minecraft.client.Minecraft.getInstance().font;
            Config.MisionData data = Config.getMisionPorNombre(GlobalGuiSettings.misionSeleccionadaGlobal);
            String texto = (data != null) ? data.descripcion : "Descripcion (Toca una mision)";
            if (p.mayusculas) texto = texto.toUpperCase();

            g.fill(p.x, p.y, p.x + p.ancho, p.y + p.alto, p.colorARGB);
            g.renderOutline(p.x, p.y, p.ancho, p.alto, p.colorBorde);

            net.minecraft.network.chat.Style estilo = net.minecraft.network.chat.Style.EMPTY
                    .withBold(p.negrita).withItalic(p.cursiva).withUnderlined(p.subrayado).withStrikethrough(p.tachado);

            g.pose().pushPose();
            g.pose().translate(p.x + 5, p.y + 5, 0);
            g.pose().scale(p.scaleDesc, p.scaleDesc, 1);
            g.drawWordWrap(font, net.minecraft.network.chat.Component.literal(texto).setStyle(estilo), 0, 0, (int)((p.ancho - 10) / p.scaleDesc), p.colorTexto);
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

                    g.fill(0, oY, p.ancho - 10, oY + 22, p.colorFondoRenglon);

                    g.fill(5, oY + 2, 23, oY + 20, p.colorFondoIcono);
                    g.renderOutline(5, oY + 2, 18, 18, p.colorBordeIcono);
                    if (obj.itemReal != null) g.renderFakeItem(new net.minecraft.world.item.ItemStack(obj.itemReal), 6, oY + 3);

                    int boxSize = 11;
                    int boxX = p.ancho - 10 - 18;
                    int boxY = oY + 5;

                    g.renderOutline(boxX - 1, boxY - 1, boxSize + 2, boxSize + 2, p.colorBordeCheckExterno);

                    g.fill(boxX, boxY, boxX + boxSize, boxY + boxSize, ok ? 0xFF00AA00 : p.colorFondoCheck);
                    g.renderOutline(boxX, boxY, boxSize, boxSize, ok ? 0xFF00FF00 : p.colorBordeCheckInterno);

                    if (ok) g.drawString(font, "✔", boxX + 3, boxY + 3, 0xFF00FF00, false);

                    g.renderOutline(0, oY, p.ancho - 10, 22, p.colorBordeRenglon);

                    String txt = cant + "/" + obj.cantidad;
                    int cantWidth = font.width(txt);
                    g.drawString(font, (ok ? "§a" : "§c") + txt, boxX - 6 - cantWidth, oY + 7, 0xFFFFFFFF, true);

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
                g.fill(p.x, p.y, p.x + p.ancho, p.y + p.alto, p.colorARGB);
                g.fill(p.x, p.y + p.alto - t, p.x + p.ancho, p.y + p.alto, p.colorBorde);
                g.fill(p.x, p.y, p.x + p.ancho, p.y + t, p.colorBorde);
                g.fill(p.x, p.y, p.x + t, p.y + p.alto, p.colorBorde);
                g.fill(p.x + p.ancho - t, p.y, p.x + p.ancho, p.y + p.alto, p.colorBorde);

                g.enableScissor(p.x + t, p.y + t, p.x + p.ancho - t, p.y + p.alto - t);

                g.pose().pushPose();
                g.pose().translate(0, -p.scrollY, 0);

                int currentY = p.y + 5;
                int marginX = 5;
                int cardHeight = 30;
                int cardWidth = p.ancho - (marginX * 2);

                float titleScale = p.escalaTexto;
                float titleOffsetX = p.offsetXTexto;

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
                int headerHeight = 20;
                int renderHeight = p.desplegado ? p.alto : headerHeight;

                g.fill(p.x, p.y, p.x + p.ancho, p.y + headerHeight, p.colorFondoCabecera);
                g.fill(p.x, p.y, p.x + p.ancho, p.y + t, p.colorBordeCabecera);
                g.fill(p.x, p.y + headerHeight - t, p.x + p.ancho, p.y + headerHeight, p.colorBordeCabecera);
                g.fill(p.x, p.y, p.x + t, p.y + headerHeight, p.colorBordeCabecera);
                g.fill(p.x + p.ancho - t, p.y, p.x + p.ancho, p.y + headerHeight, p.colorBordeCabecera);

                if (p.desplegado && p.alto > headerHeight) {
                    int bodyY = p.y + headerHeight;
                    int bodyHeight = p.alto - headerHeight;
                    g.fill(p.x, bodyY, p.x + p.ancho, bodyY + bodyHeight, p.colorARGB);
                    g.fill(p.x, bodyY + bodyHeight - t, p.x + p.ancho, bodyY + bodyHeight, p.colorBorde);
                    g.fill(p.x, bodyY, p.x + t, bodyY + bodyHeight, p.colorBorde);
                    g.fill(p.x + p.ancho - t, bodyY, p.x + p.ancho, bodyY + bodyHeight, p.colorBorde);

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

                String flecha = p.desplegado ? "▼" : "▶";
                g.drawString(font, flecha, p.x + p.ancho - 15, p.y + 6, p.colorTexto, false);

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
        } else if (!p.tipo.equals("IMAGEN_CUSTOM") && !p.tipo.equals("TEXTURA_JUEGO")) {
            // Dibuja el fondo SOLO si no es una imagen ni una textura
            g.fill(p.x, p.y, p.x + p.ancho, p.y + p.alto, p.colorARGB);
        }

        if (p.textoAsociado != null && (!p.textoAsociado.isEmpty() || escribiendo) && !p.tipo.startsWith("DESPLEGABLE")) {
            net.minecraft.client.gui.Font font = net.minecraft.client.Minecraft.getInstance().font;

            float scaledTextHeight = font.lineHeight * p.escalaTexto;
            float baseIconSize = 16;
            float scaledIconSize = baseIconSize * p.escalaIcono;

            if (p.iconoRL != null && !p.tipo.startsWith("DESPLEGABLE")) {
                float iconBaseX = p.x + 10;
                float iconX = iconBaseX + p.offsetXIcono;
                float iconY = p.y + (p.alto - scaledIconSize) / 2;

                g.pose().pushPose();
                g.pose().translate(iconX, iconY, 0);
                g.pose().scale(p.escalaIcono, p.escalaIcono, 1.0f);
                g.blit(p.iconoRL, 0, 0, 0, 0, (int)baseIconSize, (int)baseIconSize, (int)baseIconSize, (int)baseIconSize);
                g.pose().popPose();
            }

            float textBaseX = p.x + 35;
            float textX = textBaseX + p.offsetXTexto;
            float textY = p.y + (p.alto - scaledTextHeight) / 2;
            if (p.tipo.startsWith("DESPLEGABLE")) {
                textY = p.y + (20 - scaledTextHeight) / 2;
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

        // ─── RENDERING DE IMÁGENES E ÍCONOS ───
        if ("IMAGEN_CUSTOM".equals(p.tipo)) {
            net.minecraft.resources.ResourceLocation rl = net.minecraft.resources.ResourceLocation.tryParse(p.recursoPath);
            if (rl != null) {
                com.mojang.blaze3d.systems.RenderSystem.enableBlend();
                com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();
                
                g.setColor(1.0f, 1.0f, 1.0f, p.opacidad);
                g.blit(rl, p.x, p.y, 0, 0, p.ancho, p.alto, p.ancho, p.alto);
                g.setColor(1.0f, 1.0f, 1.0f, 1.0f); // Restaurar color normal
                
                com.mojang.blaze3d.systems.RenderSystem.disableBlend();
            }
        } else if ("TEXTURA_JUEGO".equals(p.tipo)) {
            net.minecraft.resources.ResourceLocation rl = net.minecraft.resources.ResourceLocation.tryParse(p.recursoPath);
            if (rl != null) {
                net.minecraft.world.item.Item item = net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(rl);
                if (item != null && item != net.minecraft.world.item.Items.AIR) {
                    net.minecraft.world.item.ItemStack stack = new net.minecraft.world.item.ItemStack(item);
                    
                    g.pose().pushPose();
                    g.pose().translate(p.x, p.y, 0);
                    
                    float scaleX = (float)p.ancho / 16.0f;
                    float scaleY = (float)p.alto / 16.0f;
                    g.pose().scale(scaleX, scaleY, 1.0f);
                    
                    // --- USAMOS NUESTRO MOTOR LIMPIO SIEMPRE (100% o menos) ---
                    g.pose().translate(8.0F, 8.0F, 150.0F); 
                    g.pose().scale(16.0F, -16.0F, 16.0F); 
                    
                    net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
                    net.minecraft.client.renderer.MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
                    net.minecraft.client.resources.model.BakedModel model = mc.getItemRenderer().getModel(stack, null, null, 0);
                    
                    if (model.usesBlockLight()) {
                        com.mojang.blaze3d.platform.Lighting.setupFor3DItems();
                    } else {
                        com.mojang.blaze3d.platform.Lighting.setupForFlatItems();
                    }
                    
                    com.mojang.blaze3d.systems.RenderSystem.enableBlend();
                    com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();
                    com.mojang.blaze3d.systems.RenderSystem.enableDepthTest();
                    
                    // PASO 1: Molde invisible
                    com.mojang.blaze3d.systems.RenderSystem.colorMask(false, false, false, false);
                    mc.getItemRenderer().render(stack, net.minecraft.world.item.ItemDisplayContext.GUI, false, g.pose(), bufferSource, 15728880, net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY, model);
                    bufferSource.endBatch();
                    
                    // PASO 2: Dibujo transparente/sólido con nuestro filtro limpio
                    com.mojang.blaze3d.systems.RenderSystem.colorMask(true, true, true, true);
                    net.minecraft.client.renderer.MultiBufferSource wrapper = type -> {
                        return bufferSource.getBuffer(net.minecraft.client.renderer.RenderType.translucent());
                    };
                    
                    com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, p.opacidad);
                    mc.getItemRenderer().render(stack, net.minecraft.world.item.ItemDisplayContext.GUI, false, g.pose(), wrapper, 15728880, net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY, model);
                    bufferSource.endBatch();
                    
                    // PASO 3: Restaurar estado para no romper otros menús
                    com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
                    com.mojang.blaze3d.systems.RenderSystem.disableBlend();
                    com.mojang.blaze3d.platform.Lighting.setupFor3DItems();
                    
                    g.pose().popPose();
                }
            }
        }

        if (seleccionado) {
            g.renderOutline(p.x - 1, p.y - 1, p.ancho + 2, p.alto + 2, 0xFF55FFFF);
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