package questgrupo.questmod.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import questgrupo.questmod.Config;
import questgrupo.questmod.events.ClickAldeano;
import questgrupo.questmod.network.Messages;
import questgrupo.questmod.network.PacketAceptarMision;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.UUID;

public class QuestScreen extends Screen {
    private static final ResourceLocation TEXTURA_ZELDA = ResourceLocation.fromNamespaceAndPath("questnomas", "textures/gui/dialogo_zelda.png");
    private static final ResourceLocation TXT_ACEPTAR_NORMAL = ResourceLocation.fromNamespaceAndPath("questnomas", "textures/gui/btn_aceptar.png");
    private static final ResourceLocation TXT_ACEPTAR_HOVER = ResourceLocation.fromNamespaceAndPath("questnomas", "textures/gui/btn_aceptar_zoom.png");
    private static final ResourceLocation TXT_ENTREGAR = ResourceLocation.fromNamespaceAndPath("questnomas", "textures/gui/btn_entregar.png");
    private static final ResourceLocation TXT_ENTREGAR_HOVER = ResourceLocation.fromNamespaceAndPath("questnomas", "textures/gui/btn_entregar_zoom.png");

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

    private Button btnAceptar;
    private Button btnEntregar;

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

    @Override
    public void onClose() {
        ClickAldeano.liberarMirada(this.entidadUUID);
        super.onClose();
    }

    @Override
    protected void init() {
        int x = this.width / 2;
        int y = this.height - 35;

        this.btnAceptar = this.addRenderableWidget(new BotonCustom(x - 40, y, 80, 20, TXT_ACEPTAR_NORMAL, TXT_ACEPTAR_HOVER, b -> {
            Messages.sendToServer(new PacketAceptarMision(this.entidadUUID));
            this.onClose();
        }));

        this.btnEntregar = this.addRenderableWidget(new BotonCustom(x - 40, y, 80, 20, TXT_ENTREGAR, TXT_ENTREGAR_HOVER, b -> {
            // VERIFICAR SI TIENE TODO
            boolean tieneTodo = true;
            for (Config.Objetivo obj : mision.objetivos) {
                if (this.minecraft.player.getInventory().countItem(obj.itemReal) < obj.cantidad) {
                    tieneTodo = false;
                    break;
                }
            }

            if (!tieneTodo) {
                prepararMensajeError();
                actualizarVisibilidadBotones();
            } else {
                Messages.sendToServer(new PacketAceptarMision(this.entidadUUID));
            }
        }));
        actualizarVisibilidadBotones();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        int relX = (this.width - 256) / 2;
        int relY = this.height - 100;

        RenderSystem.enableBlend();
        guiGraphics.blit(TEXTURA_ZELDA, relX, relY, 0, 0, 256, 80, 256, 80);
        RenderSystem.disableBlend();

        guiGraphics.drawString(this.font, "§6" + nombreNPC, relX + 20, relY + 12, 0xFFFFFF);

        String textoCompleto = getTextoActual();
        String textoAMostrar = textoCompleto.substring(0, Math.min(caracteresVisibles, textoCompleto.length()));
        String color = esAgradecimiento ? "§a" : (esError ? "§c" : "§f");

        guiGraphics.drawWordWrap(this.font, Component.literal(color + textoAMostrar), relX + 20, relY + 30, 215, 0xFFFFFF);

        if (caracteresVisibles >= textoCompleto.length() && !esError && !esFinalCheck()) {
            float osc = (float) Math.sin(System.currentTimeMillis() / 250.0) * 2;
            guiGraphics.drawString(this.font, "▼", relX + 124, (int)(relY + 68 + osc), 0xAAAAAA);
        }
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    private String getTextoActual() {
        if (esAgradecimiento || esError || yaAceptada) return mensajeDinamico != null ? mensajeDinamico : "...";
        if (mision.frases != null && !mision.frases.isEmpty()) return mision.frases.get(indiceFrase);
        return "...";
    }

    @Override
    public void tick() {
        super.tick();
        String textoActual = getTextoActual();
        if (caracteresVisibles < textoActual.length()) {
            ticksTranscurridos++;
            if (ticksTranscurridos >= VELOCIDAD_TEXTO) {
                caracteresVisibles++;
                ticksTranscurridos = 0;
                if (caracteresVisibles == textoActual.length()) actualizarVisibilidadBotones();
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            String textoActual = getTextoActual();
            if (caracteresVisibles < textoActual.length()) {
                caracteresVisibles = textoActual.length();
                actualizarVisibilidadBotones();
                return true;
            }
            if (esError || esAgradecimiento) { this.onClose(); return true; }
            if (!yaAceptada && !esAgradecimiento && !esError) {
                if (mision.frases != null && indiceFrase < mision.frases.size() - 1) {
                    indiceFrase++;
                    caracteresVisibles = 0;
                    actualizarVisibilidadBotones();
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void actualizarVisibilidadBotones() {
        String textoActual = getTextoActual();
        boolean textoTerminado = caracteresVisibles >= textoActual.length();
        boolean esFinal = (mision.frases == null) || (indiceFrase >= mision.frases.size() - 1);
        this.btnAceptar.visible = esFinal && !yaAceptada && !esAgradecimiento && !esError && textoTerminado;
        this.btnEntregar.visible = yaAceptada && !esAgradecimiento && !esError && textoTerminado;
    }

    public void mostrarAgradecimiento() {
        this.mensajeDinamico = (mision.agradecimiento != null && !mision.agradecimiento.isEmpty()) 
            ? mision.agradecimiento 
            : "¡Misión completada!";
        this.esAgradecimiento = true; this.esError = false; this.caracteresVisibles = 0;
        actualizarVisibilidadBotones();
    }

    private void prepararMensajeError() {
        // Buscamos el primer item que le falta al jugador para el mensaje
        Config.Objetivo falta = mision.objetivos.get(0);
        for(Config.Objetivo o : mision.objetivos) {
            if(this.minecraft.player.getInventory().countItem(o.itemReal) < o.cantidad) {
                falta = o;
                break;
            }
        }
        if (falta != null) {
            String nombreItem = falta.itemReal.getDescription().getString();
            String msgError = (mision.error != null && !mision.error.isEmpty()) 
                ? mision.error 
                : "Aún te faltan materiales.";
            this.mensajeDinamico = msgError.replace("{cantidad}", String.valueOf(falta.cantidad)).replace("{item}", nombreItem);
        }
        this.esError = true; this.caracteresVisibles = 0;
    }

    private boolean esFinalCheck() {
        return (mision.frases == null) || (indiceFrase >= mision.frases.size() - 1) || yaAceptada || esAgradecimiento;
    }

    @Override public boolean isPauseScreen() { return false; }

    private static class BotonCustom extends Button {
        private final ResourceLocation normal, hover;
        public BotonCustom(int x, int y, int w, int h, ResourceLocation n, ResourceLocation hv, OnPress op) {
            super(x, y, w, h, Component.empty(), op, DEFAULT_NARRATION);
            this.normal = n; this.hover = hv;
        }
        @Override
        public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float pt) {
            if (this.visible) {
                guiGraphics.blit(this.isHoveredOrFocused() ? hover : normal, this.getX(), this.getY(), 0, 0, this.width, this.height, this.width, this.height);
            }
        }
    }
}