package questgrupo.questmod.client.gui;

import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import questgrupo.questmod.client.GlobalGuiSettings;
import questgrupo.questmod.client.editor.LeftSidebar;
import questgrupo.questmod.client.editor.TopBar;
import questgrupo.questmod.client.editor.TopBar.ButtonInfo;
import java.util.Collections;

public class TextoEdit {
    public static boolean editandoColor = false;
    public static Button btnNegrita, btnCursiva, btnSubrayado, btnTachado, btnMayusculas, btnEspaciado, btnSombra;
    public static Button btnMas, btnMenos;
    private static long ultimoClic = 0;
    private static GlobalGuiSettings.TextConfig ultimoTextoClickeado = null;

    public static int getAnclajeX(int guiWidth) { return LeftSidebar.getSidebarWidth() + 5; }
    public static int getAnclajeY() { return 25; }

    public static void crearNuevoTexto(int x, int y) {
        GlobalGuiSettings.TextConfig nuevo = new GlobalGuiSettings.TextConfig("Nuevo Texto", x, y);
        GlobalGuiSettings.TEXTOS.add(nuevo);
    }

    public static void eliminarTexto(GlobalGuiSettings.TextConfig t) {
        if (t != null) {
            GlobalGuiSettings.TEXTOS.remove(t);
        }
    }

    public static boolean esDobleClic(GlobalGuiSettings.TextConfig t) {
        long ahora = System.currentTimeMillis();
        boolean esDoble = (t == ultimoTextoClickeado && (ahora - ultimoClic) < 250);
        ultimoClic = ahora;
        ultimoTextoClickeado = t;
        return esDoble;
    }

    public static void inicializarOActualizarBotones(int guiWidth, int y, java.util.function.Consumer<Button> adder, java.util.function.Supplier<GlobalGuiSettings.TextConfig> tSelSupplier, Runnable spacingCallback) {
        GlobalGuiSettings.TextConfig tSel = tSelSupplier.get();
        Font font = Minecraft.getInstance().font;

        int barX = LeftSidebar.getSidebarWidth();
        int barW = TopBar.getWidth();
        int barStartX = barX + (guiWidth - barX - barW) / 2;
        int curX = barStartX + 10 + 15 + 5;

        int w = font.width("-") + 6;
        btnMenos = Button.builder(Component.literal("-"), b -> {
            GlobalGuiSettings.TextConfig t = tSelSupplier.get();
            if (t != null) t.escala = Math.max(0.1f, t.escala - 0.1f);
        }).bounds(curX, y, w, 18).build();
        curX += w + 2;

        w = font.width("+") + 6;
        btnMas = Button.builder(Component.literal("+"), b -> {
            GlobalGuiSettings.TextConfig t = tSelSupplier.get();
            if (t != null) t.escala = Math.min(10.0f, t.escala + 0.1f);
        }).bounds(curX, y, w, 18).build();

        actualizarEstadoBotones(tSelSupplier.get());
    }

    public static void actualizarEstadoBotones(GlobalGuiSettings.TextConfig tSel) {
        boolean visible = (tSel != null) && TopBar.isVisible();
        if (btnMas != null) {
            btnMas.visible = btnMenos.visible = visible;
        }
    }

    public static void renderizar(GuiGraphics g, GlobalGuiSettings.TextConfig t, Font font, boolean seleccionado, boolean editando) {
        String contenido = t.contenido + (editando && (System.currentTimeMillis() / 500) % 2 == 0 ? "_" : "");
        if (t.mayusculas) contenido = contenido.toUpperCase();

        g.pose().pushPose();
        g.pose().translate(t.x, t.y, 0);
        g.pose().mulPose(Axis.ZP.rotationDegrees(t.rotacion));
        g.pose().scale(t.escala, t.escala, 1.0f);

        // Renderizar con interletrado (espaciado entre caracteres)
        float xOffset = 0;
        for (int i = 0; i < contenido.length(); i++) {
            char c = contenido.charAt(i);
            Style estilo = Style.EMPTY.withBold(t.negrita).withItalic(t.cursiva).withUnderlined(t.subrayado).withStrikethrough(t.tachado);
            FormattedCharSequence charSeq = FormattedCharSequence.forward(String.valueOf(c), estilo);
            g.drawString(font, charSeq, (int)xOffset, 0, t.colorARGB, t.sombra);
            xOffset += font.width(String.valueOf(c)) + t.interletrado;
        }

        if (seleccionado) {
            int ancho = (int)(xOffset - t.interletrado);
            g.renderOutline(-2, -2, ancho + 4, (int)(font.lineHeight), 0xFFFFFFFF);
        }
        g.pose().popPose();
    }

    public static boolean mouseSobreTexto(double mx, double my, GlobalGuiSettings.TextConfig t, Font font) {
        // Calculate text dimensions with scaling and interletrado
        String contenido = t.mayusculas ? t.contenido.toUpperCase() : t.contenido;
        float totalWidth = 0;
        for (int i = 0; i < contenido.length(); i++) {
            totalWidth += font.width(String.valueOf(contenido.charAt(i))) + t.interletrado;
        }
        if (contenido.length() > 0) totalWidth -= t.interletrado; // Remove last spacing
        
        float ancho = totalWidth * t.escala;
        float alto = font.lineHeight * t.escala;

        if (t.rotacion == 0) {
            return mx >= t.x && mx <= t.x + ancho && my >= t.y && my <= t.y + alto;
        }

        // For rotated text, transform mouse coordinates to local space
        // Render order: translate(t.x, t.y) -> rotate(t.rotacion) -> scale(t.escala)
        // Inverse: unscale -> unrotate -> untranslate
        double rad = Math.toRadians(-t.rotacion); // Inverse rotation
        double cos = Math.cos(rad);
        double sin = Math.sin(rad);
        double lx = ((mx - t.x) * cos - (my - t.y) * sin) / t.escala;
        double ly = ((mx - t.x) * sin + (my - t.y) * cos) / t.escala;

        return lx >= 0 && lx <= totalWidth && ly >= 0 && ly <= font.lineHeight;
    }

    public static void dibujarUIExtra(GuiGraphics g, Font font, int guiWidth, int y, GlobalGuiSettings.TextConfig t) {
        if (t == null) return;
        int barX = LeftSidebar.getSidebarWidth();
        int centroX = barX + (guiWidth - barX) / 2;
        int inicioX = centroX - 72;
        int colorX = getAnclajeX(guiWidth);

        String val = String.format("%.1f", t.escala);
        g.drawString(font, val, inicioX + 160 - (font.width(val)/2), y + 5, 0xFF00FFFF);

        g.fill(colorX, y, colorX + 15, y + 15, t.colorARGB);
        g.renderOutline(colorX - 1, y - 1, 17, 17, 0xFFFFFFFF);
    }

    public static boolean clickEnColor(double mx, double my, int guiWidth, int y) {
        int colorX = getAnclajeX(guiWidth);
        return mx >= colorX && mx <= colorX + 15 && my >= y && my <= y + 15;
    }

    public static void moverTextoAlFrente(GlobalGuiSettings.TextConfig t) {
        if (t == null) return;
        GlobalGuiSettings.TEXTOS.remove(t);
        GlobalGuiSettings.TEXTOS.add(t);
    }

    public static void moverTextoAlFondo(GlobalGuiSettings.TextConfig t) {
        if (t == null) return;
        GlobalGuiSettings.TEXTOS.remove(t);
        GlobalGuiSettings.TEXTOS.add(0, t);
    }

    public static void moverTextoAdelante(GlobalGuiSettings.TextConfig t) {
        if (t == null) return;
        int idx = GlobalGuiSettings.TEXTOS.indexOf(t);
        if (idx > -1 && idx < GlobalGuiSettings.TEXTOS.size() - 1) {
            Collections.swap(GlobalGuiSettings.TEXTOS, idx, idx + 1);
        }
    }

    public static void moverTextoAtras(GlobalGuiSettings.TextConfig t) {
        if (t == null) return;
        int idx = GlobalGuiSettings.TEXTOS.indexOf(t);
        if (idx > 0) {
            Collections.swap(GlobalGuiSettings.TEXTOS, idx, idx - 1);
        }
    }

    public static void rotarTexto(GlobalGuiSettings.TextConfig t, float grados) {
        if (t != null) {
            t.rotacion = grados;
        }
    }
}
