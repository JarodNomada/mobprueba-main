package questgrupo.questmod.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import questgrupo.questmod.Config;
import questgrupo.questmod.client.gui.FigurasEdit;
import questgrupo.questmod.events.ClickAldeano;
import questgrupo.questmod.network.Messages;
import questgrupo.questmod.network.PacketAceptarMision;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

public class QuestScreen extends Screen {

    private final Config.MisionData mision;
    private final String nombreNPC;
    private final UUID entidadUUID;
    private final boolean yaAceptada;
    private static final Random RANDOM = new Random();
    
    private String nodoActualId;
    private Config.NodoDialogo nodoActual;
    
    private String textoNPCSeleccionado = "...";
    private final List<String> textosOpcionesSeleccionados = new ArrayList<>();
    
    private final List<Integer> textScrollOffsets = new ArrayList<>();
    private final List<Integer> scrollPauseTicks = new ArrayList<>();
    private static final int SCROLL_SPEED = 2;
    private static final int SCROLL_PAUSE = 60;
    private static final int INITIAL_SCROLL_PAUSE = 30;
    
    private int caracteresVisibles = 0;
    private int ticksTranscurridos = 0;
    private static final int VELOCIDAD_TEXTO = 1;

    private static final int BOX_W = 345;
    private static final int BOX_H = 125;
    private static final int BOX_MARGIN_BOTTOM = 20;

    private static final int C_MAIN_BG      = 0xFFA79F94;
    private static final int C_OUTER_BG     = 0xFF938A82;
    private static final int C_TRAZO_NEGRO  = 0xFF1B1610;
    private static final int C_TEXTO_NP     = 0xFF1E1E1E;
    private static final int C_SEPARATOR    = 0xFF625D54;

    private static final int C_BTN_LIGHT    = 0xFF76736E;
    private static final int C_BTN_DARK     = 0xFF353631;
    private static final int C_BTN_BG_NORM  = 0xFF505050;
    private static final int C_BTN_TEXT     = 0xFFF2F2F2;
    private static final int C_HOVER_GREEN  = 0xFF4A792A;
    private static final int C_HOVER_LIGHT  = 0xFF73BD42;
    private static final int C_HOVER_DARK   = 0xFF36591F;

    public QuestScreen(Config.MisionData mision, String nombreNPC, boolean yaAceptada, UUID entidadUUID) {
        super(Component.literal("Dialogo Quest"));
        this.mision = mision;
        this.nombreNPC = nombreNPC;
        this.entidadUUID = entidadUUID;
        this.yaAceptada = yaAceptada;
    }

    @Override
    protected void init() {
        super.init();
        if (!yaAceptada) {
            this.nodoActualId = mision.puntos_de_entrada.getOrDefault("sin_aceptar", "nodo_inicio");
        } else {
            this.nodoActualId = mision.puntos_de_entrada.getOrDefault("en_progreso", "nodo_espera");
        }
        
        this.nodoActual = mision.nodos.get(this.nodoActualId);
        seleccionarVariantes();
    }
    
    private void seleccionarVariantes() {
        textosOpcionesSeleccionados.clear();
        if (this.nodoActual != null) {
            if (nodoActual.textoNPC != null && !nodoActual.textoNPC.isEmpty()) {
                textoNPCSeleccionado = nodoActual.textoNPC.get(RANDOM.nextInt(nodoActual.textoNPC.size()));
            }
            if (nodoActual.opciones != null) {
                for (Config.OpcionDialogo op : nodoActual.opciones) {
                    if (op.texto != null && !op.texto.isEmpty()) {
                        textosOpcionesSeleccionados.add(op.texto.get(RANDOM.nextInt(op.texto.size())));
                    } else {
                        textosOpcionesSeleccionados.add("...");
                    }
                }
            }
        } else {
            textoNPCSeleccionado = "...";
        }
        textScrollOffsets.clear();
        scrollPauseTicks.clear();
        for (int i = 0; i < textosOpcionesSeleccionados.size(); i++) {
            textScrollOffsets.add(0);
            scrollPauseTicks.add(INITIAL_SCROLL_PAUSE);
        }
    }

    @Override public void onClose() { ClickAldeano.liberarMirada(this.entidadUUID); super.onClose(); }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(g);
        int rx = bx(), ry = by();

        renderEstructuraContenedor(g, rx, ry);
        g.drawString(this.font, "§l" + nombreNPC, rx + 12, ry + 10, C_TEXTO_NP, false);

        int ex = rx + BOX_W - 20, ey = ry + 10;
        boolean hovExit = mouseX >= ex && mouseX <= ex + 10 && mouseY >= ey && mouseY <= ey + 10;
        g.drawString(this.font, "x", ex, ey, hovExit ? 0xFFFF5555 : 0xFF555555, false);

        String completo = getTextoActual();
        String mostrar = completo.substring(0, Math.min(caracteresVisibles, completo.length()));
        g.drawWordWrap(this.font, Component.literal(mostrar), rx + 12, ry + 26, BOX_W - 24, C_TEXTO_NP);

        int separatorY = ry + 55;
        g.fill(rx + 8, separatorY, rx + BOX_W - 8, separatorY + 1, C_SEPARATOR);

        renderRespuestasGrid(g, mouseX, mouseY, rx, separatorY + 8);

        if (caracteresVisibles >= completo.length() && getCantidadOpciones() == 0) {
            float osc = (float) Math.sin(System.currentTimeMillis() / 250.0) * 2;
            g.drawString(this.font, "\u25BC", rx + (BOX_W / 2) - 3, (int)(ry + BOX_H - 10 + osc), 0xFF555555);
        }

        super.render(g, mouseX, mouseY, partialTick);
    }

    private void renderEstructuraContenedor(GuiGraphics g, int rx, int ry) {
        int outX = rx - 4; int outY = ry - 4; int outW = BOX_W + 8; int outH = BOX_H + 8;
        g.fill(outX + 4, outY + 4, outX + outW + 4, outY + outH + 4, 0x55000000);
        g.fill(outX + 3, outY, outX + outW - 3, outY + outH, C_OUTER_BG);
        g.fill(outX, outY + 3, outX + outW, outY + outH - 3, C_OUTER_BG);
        g.fill(outX + 1, outY + 1, outX + outW - 1, outY + outH - 1, C_OUTER_BG);
        g.fill(outX + 2, outY + 2, outX + outW - 2, outY + outH - 2, C_OUTER_BG);

        int inX = rx; int inY = ry; int inW = BOX_W; int inH = BOX_H;
        g.fill(inX + 3, inY - 1, inX + inW - 3, inY + inH + 1, C_TRAZO_NEGRO);
        g.fill(inX - 1, inY + 3, inX + inW + 1, inY + inH - 3, C_TRAZO_NEGRO);
        g.fill(inX + 1, inY, inX + inW - 1, inY + inH, C_TRAZO_NEGRO);
        g.fill(inX, inY + 1, inX + inW, inY + inH - 1, C_TRAZO_NEGRO);

        g.fill(inX + 3, inY, inX + inW - 3, inY + inH, C_MAIN_BG);
        g.fill(inX, inY + 3, inX + inW, inY + inH - 3, C_MAIN_BG);
        g.fill(inX + 1, inY + 1, inX + inW - 1, inY + inH - 1, C_MAIN_BG);
        g.fill(inX + 2, inY + 2, inX + inW - 2, inY + inH - 2, C_MAIN_BG);
    }

    private void renderRespuestasGrid(GuiGraphics g, int mx, int my, int rx, int ry) {
        int numOps = getCantidadOpciones();
        if (numOps == 0) return;

        int pad = 12; int gap = 6; int btnH = 22;
        int halfW = (BOX_W - pad * 2 - gap) / 2;

        for (int i = 0; i < numOps; i++) {
            int col = i % 2; int row = i / 2;
            int bX = rx + pad + col * (halfW + gap);
            int bY = ry + row * (btnH + 6);
            int bW = (numOps == 1) ? (BOX_W - pad * 2) : halfW;

            boolean hov = mx >= bX && mx <= bX + bW && my >= bY && my <= bY + btnH;

            g.renderOutline(bX - 1, bY - 1, bW + 2, btnH + 2, C_TRAZO_NEGRO);

            int cFondo = hov ? C_HOVER_GREEN : C_BTN_BG_NORM;
            g.fill(bX, bY, bX + bW, bY + btnH, cFondo);

            int cLight = hov ? C_HOVER_LIGHT : C_BTN_LIGHT;
            g.fill(bX, bY, bX + bW, bY + 1, cLight);
            g.fill(bX, bY, bX + 1, bY + btnH, cLight);

            int cDark = hov ? C_HOVER_DARK : C_BTN_DARK;
            g.fill(bX, bY + btnH - 1, bX + bW, bY + btnH, cDark);
            g.fill(bX + bW - 1, bY, bX + bW, bY + btnH, cDark);

            String texto = getTextoOpcion(i);
            int textFullWidth = this.font.width(texto);
            int maxTextW = bW - 8;
            if (textFullWidth > maxTextW) {
                int offset = i < textScrollOffsets.size() ? textScrollOffsets.get(i) : 0;
                g.enableScissor(bX, bY, bX + bW, bY + btnH);
                g.drawString(this.font, texto, bX + 4 - offset, bY + (btnH - 8) / 2, C_BTN_TEXT, false);
                g.disableScissor();
            } else {
                int textX = bX + (bW - textFullWidth) / 2;
                g.drawString(this.font, texto, textX, bY + (btnH - 8) / 2, C_BTN_TEXT, false);
            }
        }
    }

    private int getCantidadOpciones() {
        if (caracteresVisibles < getTextoActual().length()) return 0;
        return textosOpcionesSeleccionados.size();
    }

    private String getTextoOpcion(int i) {
        if (i < textosOpcionesSeleccionados.size()) {
            return textosOpcionesSeleccionados.get(i);
        }
        return "";
    }

    private void ejecutarOpcion(int i) {
        if (nodoActual != null && nodoActual.opciones != null && i < nodoActual.opciones.size()) {
            Config.OpcionDialogo opcion = nodoActual.opciones.get(i);
            
            String sigDestino = opcion.destino;
            
            if ("COMPROBAR_ENTREGA".equals(opcion.accion)) {
                if (!verificarObjetivos()) {
                    sigDestino = (opcion.destino_fallo != null && !opcion.destino_fallo.isEmpty()) ? opcion.destino_fallo : "CERRAR";
                } else {
                    Messages.sendToServer(new PacketAceptarMision(this.entidadUUID, this.mision.nombre));
                }
            }
            
            if ("ACEPTAR_MISION".equals(opcion.accion)) {
                Messages.sendToServer(new PacketAceptarMision(this.entidadUUID, this.mision.nombre));
            }
            
            if ("CERRAR".equals(sigDestino) || "ACEPTAR_Y_CERRAR".equals(opcion.accion)) {
                this.onClose();
            } else if (sigDestino != null && !sigDestino.isEmpty()) {
                this.nodoActualId = sigDestino;
                this.nodoActual = mision.nodos.get(this.nodoActualId);
                this.caracteresVisibles = 0;
                if (this.nodoActual == null) {
                    this.onClose();
                } else {
                    seleccionarVariantes();
                }
            } else {
                this.onClose();
            }
        }
    }

    private boolean verificarObjetivos() {
        if (this.minecraft == null || this.minecraft.player == null) return false;
        boolean tieneTodo = true;
        String questKey = "";
        for (String qk : GlobalGuiSettings.misionesAceptadasCliente) {
            if (qk.endsWith("_" + mision.nombre.replace(" ", "_"))) { questKey = qk; break; }
        }
        for (Config.Objetivo obj : mision.objetivos) {
            if (obj.entidad != null && !obj.entidad.isEmpty()) {
                String pk = this.minecraft.player.getUUID().toString() + "_" + questKey + "_" + obj.entidad;
                if (GlobalGuiSettings.progresoMuertesCliente.getOrDefault(pk, 0) < obj.cantidad) { tieneTodo = false; break; }
            } else if (obj.itemReal != null) {
                if (this.minecraft.player.getInventory().countItem(obj.itemReal) < obj.cantidad) { tieneTodo = false; break; }
            }
        }
        return tieneTodo;
    }

    private String getTextoActual() {
        return textoNPCSeleccionado;
    }

    @Override
    public void tick() {
        super.tick();
        if (caracteresVisibles < getTextoActual().length()) {
            if (++ticksTranscurridos >= VELOCIDAD_TEXTO) { caracteresVisibles++; ticksTranscurridos = 0; }
        }
        if (caracteresVisibles >= getTextoActual().length()) {
            int numOps = getCantidadOpciones();
            if (numOps == 0) return;
            int pad = 12; int gap = 6;
            int halfW = (BOX_W - pad * 2 - gap) / 2;
            for (int i = 0; i < numOps && i < textScrollOffsets.size(); i++) {
                String texto = getTextoOpcion(i);
                int bW = (numOps == 1) ? (BOX_W - pad * 2) : halfW;
                int maxTextW = bW - 8;
                if (font.width(texto) > maxTextW) {
                    int pause = scrollPauseTicks.get(i);
                    if (pause > 0) {
                        scrollPauseTicks.set(i, pause - 1);
                    } else {
                        int offset = textScrollOffsets.get(i) + SCROLL_SPEED;
                        int fullWidth = font.width(texto) + 8;
                        int maxOffset = fullWidth - maxTextW;
                        if (offset >= maxOffset) {
                            offset = 0;
                            scrollPauseTicks.set(i, SCROLL_PAUSE);
                        }
                        textScrollOffsets.set(i, offset);
                    }
                }
            }
        }
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button == 0) {
            int rx = bx(), ry = by();
            int ex = rx + BOX_W - 20, ey = ry + 10;
            if (mx >= ex && mx <= ex + 10 && my >= ey && my <= ey + 10) { this.onClose(); return true; }

            String txt = getTextoActual();
            if (caracteresVisibles < txt.length()) { caracteresVisibles = txt.length(); return true; }

            int numOps = getCantidadOpciones();
            if (numOps > 0) {
                int pad = 12, gap = 6, btnH = 22;
                int halfW = (BOX_W - pad * 2 - gap) / 2;
                int startY = ry + 55 + 8;

                for (int i = 0; i < numOps; i++) {
                    int col = i % 2, row = i / 2;
                    int bX = rx + pad + col * (halfW + gap);
                    int bY = startY + row * (btnH + 6);
                    int bW = (numOps == 1) ? (BOX_W - pad * 2) : halfW;

                    if (mx >= bX && mx <= bX + bW && my >= bY && my <= bY + btnH) {
                        ejecutarOpcion(i); return true;
                    }
                }
            } else {
                if (caracteresVisibles >= txt.length()) {
                    this.onClose();
                    return true;
                }
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override public boolean isPauseScreen() { return false; }
    private int bx() { return (this.width - BOX_W) / 2; }
    private int by() { return this.height - BOX_H - BOX_MARGIN_BOTTOM; }
}
