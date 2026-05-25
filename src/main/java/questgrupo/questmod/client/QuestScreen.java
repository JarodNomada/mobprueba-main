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

public class QuestScreen extends Screen {

    private final Config.MisionData mision;
    private final String nombreNPC;
    private final UUID entidadUUID;
    private int indiceFrase = 0;
    private String mensajeDinamico = null;
    private boolean esError = false;
    private boolean esAgradecimiento = false;
    private final boolean yaAceptada;
    private int caracteresVisibles = 0;
    private int ticksTranscurridos = 0;
    private static final int VELOCIDAD_TEXTO = 1;

    // ── Dimensiones Reducidas Solicitadas ─────────────────────────────────
    private static final int BOX_W = 345;
    private static final int BOX_H = 120;
    private static final int BOX_MARGIN_BOTTOM = 20;

    // ── Paleta Exacta Actualizada ─────────────────────────────────────────
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
    private static final int C_HOVER_LIGHT  = 0xFF5F9B36;
    private static final int C_HOVER_DARK   = 0xFF36591F;

    public QuestScreen(Config.MisionData mision, String nombreNPC, boolean yaAceptada, UUID entidadUUID) {
        super(Component.literal("Dialogo Quest"));
        this.mision = mision;
        this.nombreNPC = nombreNPC;
        this.yaAceptada = yaAceptada;
        this.entidadUUID = entidadUUID;
        if (yaAceptada) {
            this.mensajeDinamico = (mision.recordatorio != null && !mision.recordatorio.isEmpty())
                    ? mision.recordatorio : "¿Ya lo tienes?";
        }
    }

    @Override public void onClose() { ClickAldeano.liberarMirada(this.entidadUUID); super.onClose(); }
    @Override protected void init() {}

    private int bx() { return (this.width - BOX_W) / 2; }
    private int by() { return this.height - BOX_H - BOX_MARGIN_BOTTOM; }

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
        String colorStatus = esAgradecimiento ? "§a" : (esError ? "§c" : "");
        g.drawWordWrap(this.font, Component.literal(colorStatus + mostrar), rx + 12, ry + 26, BOX_W - 24, C_TEXTO_NP);

        int separatorY = ry + 55;
        g.fill(rx + 8, separatorY, rx + BOX_W - 8, separatorY + 1, C_SEPARATOR);

        renderRespuestasGrid(g, mouseX, mouseY, rx, separatorY + 8);

        if (caracteresVisibles >= completo.length() && !esError && !esFinalCheck() && getCantidadOpciones() == 0) {
            float osc = (float) Math.sin(System.currentTimeMillis() / 250.0) * 2;
            g.drawString(this.font, "\u25BC", rx + (BOX_W / 2) - 3, (int)(ry + BOX_H - 10 + osc), 0xFF555555);
        }

        super.render(g, mouseX, mouseY, partialTick);
    }

    private void renderEstructuraContenedor(GuiGraphics g, int rx, int ry) {
        int outX = rx - 4;
        int outY = ry - 4;
        int outW = BOX_W + 8;
        int outH = BOX_H + 8;

        g.fill(outX + 4, outY + 4, outX + outW + 4, outY + outH + 4, 0x55000000);

        g.fill(outX + 3, outY, outX + outW - 3, outY + outH, C_OUTER_BG);
        g.fill(outX, outY + 3, outX + outW, outY + outH - 3, C_OUTER_BG);
        g.fill(outX + 1, outY + 1, outX + outW - 1, outY + outH - 1, C_OUTER_BG);
        g.fill(outX + 2, outY + 2, outX + outW - 2, outY + outH - 2, C_OUTER_BG);

        int inX = rx;
        int inY = ry;
        int inW = BOX_W;
        int inH = BOX_H;

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

        int pad = 12;
        int gap = 6;
        int btnH = 22;
        int halfW = (BOX_W - pad * 2 - gap) / 2;

        for (int i = 0; i < numOps; i++) {
            int col = i % 2;
            int row = i / 2;
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
            int textW = this.font.width(texto);

            int textX = bX + (bW - textW) / 2;
            g.drawString(this.font, texto, textX, bY + (btnH - 8) / 2, C_BTN_TEXT, false);
        }
    }

    private int getCantidadOpciones() {
        String textoActual = getTextoActual();
        if (caracteresVisibles < textoActual.length()) return 0;
        if (esError || esAgradecimiento) return 1;
        if (!yaAceptada) {
            return (mision.frases != null && indiceFrase < mision.frases.size() - 1) ? 1 : 4;
        } else {
            return 4;
        }
    }

    private String getTextoOpcion(int i) {
        if (esError || esAgradecimiento) return "Entendido.";
        if (!yaAceptada) {
            if (mision.frases != null && indiceFrase < mision.frases.size() - 1) return "Siguiente...";
            switch (i) {
                case 0: return "Cuenta conmigo.";
                case 1: return "¿Puedes repetirlo?";
                case 2: return "No, lo siento.";
                case 3: return "Ahora no puedo.";
            }
        } else {
            switch (i) {
                case 0: return "Aquí tienes lo que pediste.";
                case 1: return "¿De qué trata esto?";
                case 2: return "Aún no lo tengo.";
                case 3: return "Volveré más tarde.";
            }
        }
        return "";
    }

    private void ejecutarOpcion(int i) {
        if (esError || esAgradecimiento) { this.onClose(); return; }

        if (!yaAceptada) {
            if (mision.frases != null && indiceFrase < mision.frases.size() - 1) {
                indiceFrase++; caracteresVisibles = 0; return;
            }
            switch (i) {
                case 0:
                    Messages.sendToServer(new PacketAceptarMision(this.entidadUUID));
                    this.onClose(); break;
                case 1: caracteresVisibles = 0; break;
                case 2:
                case 3: this.onClose(); break;
            }
        } else {
            switch (i) {
                case 0: intentarEntrega(); break;
                case 1:
                    mensajeDinamico = (mision.recordatorio != null && !mision.recordatorio.isEmpty())
                            ? mision.recordatorio : "Completa los objetivos y regresa.";
                    caracteresVisibles = 0; break;
                case 2: prepararMensajeError(); break;
                case 3: this.onClose(); break;
            }
        }
    }

    private void intentarEntrega() {
        boolean tieneTodo = true;
        String questKey = "";
        for (String qk : GlobalGuiSettings.misionesAceptadasCliente) {
            if (qk.endsWith("_" + mision.nombre.replace(" ", "_"))) { questKey = qk; break; }
        }
        for (Config.Objetivo obj : mision.objetivos) {
            if (obj.entidad != null && !obj.entidad.isEmpty()) {
                String pk = this.minecraft.player.getUUID() + "_" + questKey + "_" + obj.entidad;
                if (GlobalGuiSettings.progresoMuertesCliente.getOrDefault(pk, 0) < obj.cantidad) { tieneTodo = false; break; }
            } else if (obj.itemReal != null) {
                if (this.minecraft.player.getInventory().countItem(obj.itemReal) < obj.cantidad) { tieneTodo = false; break; }
            }
        }
        if (!tieneTodo) prepararMensajeError();
        else Messages.sendToServer(new PacketAceptarMision(this.entidadUUID));
    }

    private String getTextoActual() {
        if (esAgradecimiento || esError || yaAceptada) return mensajeDinamico != null ? mensajeDinamico : "...";
        if (mision.frases != null && !mision.frases.isEmpty()) return mision.frases.get(indiceFrase);
        return "...";
    }

    @Override
    public void tick() {
        super.tick();
        String txt = getTextoActual();
        if (caracteresVisibles < txt.length()) {
            if (++ticksTranscurridos >= VELOCIDAD_TEXTO) { caracteresVisibles++; ticksTranscurridos = 0; }
        }
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        for (int i = GlobalGuiSettings.PANELES.size() - 1; i >= 0; i--) {
            GlobalGuiSettings.PanelConfig p = GlobalGuiSettings.PANELES.get(i);
            if (p.tipo.equals("BOTON_PAGINA") && FigurasEdit.mouseSobreFigura(mx, my, p)) {
                try { GlobalGuiSettings.paginaActual = Integer.parseInt(p.textoAsociado); return true; }
                catch (Exception ignored) {}
            }
        }

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
                int separatorY = ry + 55;
                int startY = separatorY + 8;

                for (int i = 0; i < numOps; i++) {
                    int col = i % 2, row = i / 2;
                    int bX = rx + pad + col * (halfW + gap);
                    int bY = startY + row * (btnH + 6);
                    int bW = (numOps == 1) ? (BOX_W - pad * 2) : halfW;

                    if (mx >= bX && mx <= bX + bW && my >= bY && my <= bY + btnH) {
                        ejecutarOpcion(i); return true;
                    }
                }
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    public void mostrarAgradecimiento() {
        this.mensajeDinamico = (mision.agradecimiento != null && !mision.agradecimiento.isEmpty())
                ? mision.agradecimiento : "¡Misión completada!";
        this.esAgradecimiento = true; this.esError = false; this.caracteresVisibles = 0;
    }

    private void prepararMensajeError() {
        String questKey = "";
        for (String qk : GlobalGuiSettings.misionesAceptadasCliente)
            if (qk.endsWith("_" + mision.nombre.replace(" ", "_"))) { questKey = qk; break; }

        Config.Objetivo falta = mision.objetivos.get(0);
        for (Config.Objetivo o : mision.objetivos) {
            if (o.entidad != null && !o.entidad.isEmpty()) {
                String pk = this.minecraft.player.getUUID() + "_" + questKey + "_" + o.entidad;
                if (GlobalGuiSettings.progresoMuertesCliente.getOrDefault(pk, 0) < o.cantidad) { falta = o; break; }
            } else if (o.itemReal != null && this.minecraft.player.getInventory().countItem(o.itemReal) < o.cantidad) {
                falta = o; break;
            }
        }
        if (falta != null) {
            String nombre = (falta.entidad != null && !falta.entidad.isEmpty())
                    ? (falta.texto != null && !falta.texto.isEmpty() ? falta.texto : "enemigos")
                    : (falta.itemReal != null ? falta.itemReal.getDescription().getString() : "objetivo");
            String msg = (mision.error != null && !mision.error.isEmpty()) ? mision.error : "Aún te faltan materiales.";
            this.mensajeDinamico = msg.replace("{cantidad}", String.valueOf(falta.cantidad)).replace("{item}", nombre);
        }
        this.esError = true; this.caracteresVisibles = 0;
    }

    private boolean esFinalCheck() {
        return mision.frases == null || indiceFrase >= mision.frases.size() - 1 || yaAceptada || esAgradecimiento;
    }

    @Override public boolean isPauseScreen() { return false; }
}
