package questgrupo.questmod.client.editor;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import questgrupo.questmod.client.GlobalGuiSettings;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class TextToolHandler {
    private static Button btnNegrita, btnCursiva, btnSubrayado, btnTachado, btnMayusculas;
    private static Button btnEspaciado, btnMas, btnMenos, btnSombra;
    private static Button btnMasEspaciado, btnMenosEspaciado;

    public static void inicializarOActualizarBotones(int guiWidth, int y, java.util.function.Consumer<Button> adder, java.util.function.Supplier<GlobalGuiSettings.TextConfig> tSelSupplier, java.util.function.Supplier<GlobalGuiSettings.PanelConfig> pSelSupplier, Runnable spacingCallback) {
        GlobalGuiSettings.TextConfig tSel = tSelSupplier.get();
        GlobalGuiSettings.PanelConfig pSel = pSelSupplier.get();

        boolean esMisionTexto = pSel != null && (pSel.tipo.equals("MISION_TITULO") || pSel.tipo.equals("MISION_DESCRIPCION"));
        boolean esEstadistica = pSel != null && pSel.tipo.startsWith("ESTADISTICA_");

        int barX = LeftSidebar.getSidebarWidth();
        int barW = TopBar.calculateBarWidth(true, false, tSel, pSel);
        int barStartX = barX + (guiWidth - barX - barW) / 2;

        int colorSectionWidth = esMisionTexto ? 38 : 18;
        int btnStartX = barStartX + 4 + colorSectionWidth + 2;
        int curX1 = btnStartX;
        int curX2 = btnStartX;
        
        // Centrar estéticamente la fila inferior si es Estadística (ya que tiene un botón menos)
        if (esEstadistica) {
            curX2 += 10;
        }

        int btnSize = 18;
        int btnGap = 2;
        // Alineación milimétrica con los recuadros de color
        int rowY1 = y + 3; 
        int rowY2 = y + 23;

        btnNegrita = Button.builder(Component.literal("B"), b -> {
            GlobalGuiSettings.TextConfig t = tSelSupplier.get();
            GlobalGuiSettings.PanelConfig p = pSelSupplier.get();
            if (t != null) t.negrita = !t.negrita;
            else if (p != null) p.negrita = !p.negrita;
        }).bounds(curX1, rowY1, btnSize, btnSize).build();
        curX1 += btnSize + btnGap;

        btnCursiva = Button.builder(Component.literal("I"), b -> {
            GlobalGuiSettings.TextConfig t = tSelSupplier.get();
            GlobalGuiSettings.PanelConfig p = pSelSupplier.get();
            if (t != null) t.cursiva = !t.cursiva;
            else if (p != null) p.cursiva = !p.cursiva;
        }).bounds(curX1, rowY1, btnSize, btnSize).build();
        curX1 += btnSize + btnGap;

        btnSubrayado = Button.builder(Component.literal("U"), b -> {
            GlobalGuiSettings.TextConfig t = tSelSupplier.get();
            GlobalGuiSettings.PanelConfig p = pSelSupplier.get();
            if (t != null) t.subrayado = !t.subrayado;
            else if (p != null) p.subrayado = !p.subrayado;
        }).bounds(curX1, rowY1, btnSize, btnSize).build();
        curX1 += btnSize + btnGap;

        btnMas = Button.builder(Component.literal("+"), b -> {
            GlobalGuiSettings.TextConfig t = tSelSupplier.get();
            GlobalGuiSettings.PanelConfig p = pSelSupplier.get();
            if (t != null) t.escala = Math.min(10.0f, t.escala + 0.1f);
            else if (p != null) {
                if (p.tipo.equals("MISION_TITULO") || p.tipo.startsWith("ESTADISTICA_")) p.escalaTexto = Math.min(10.0f, p.escalaTexto + 0.1f);
                else if (p.tipo.equals("MISION_DESCRIPCION")) p.scaleDesc = Math.min(10.0f, p.scaleDesc + 0.1f);
            }
        }).bounds(curX1, rowY1, btnSize, btnSize).build();
        curX1 += btnSize + btnGap;

        if (!esEstadistica) {
            btnMasEspaciado = Button.builder(Component.literal("→"), b -> {
                GlobalGuiSettings.TextConfig t = tSelSupplier.get();
                GlobalGuiSettings.PanelConfig p = pSelSupplier.get();
                if (t != null) t.interletrado += 0.5f;
                else if (p != null) p.interletrado += 0.5f;
            }).bounds(curX1, rowY1, btnSize, btnSize).build();
            
            btnMayusculas = Button.builder(Component.literal("aA"), b -> {
                GlobalGuiSettings.TextConfig t = tSelSupplier.get();
                GlobalGuiSettings.PanelConfig p = pSelSupplier.get();
                if (t != null) t.mayusculas = !t.mayusculas;
                else if (p != null) p.mayusculas = !p.mayusculas;
            }).bounds(curX2, rowY2, btnSize, btnSize).build();
            curX2 += btnSize + btnGap;
        } else {
            btnMasEspaciado = null;
            btnMayusculas = null;
        }

        btnSombra = Button.builder(Component.literal("Sh"), b -> {
            GlobalGuiSettings.TextConfig t = tSelSupplier.get();
            GlobalGuiSettings.PanelConfig p = pSelSupplier.get();
            if (t != null) t.sombra = !t.sombra;
            else if (p != null) p.sombra = !p.sombra;
        }).bounds(curX2, rowY2, btnSize, btnSize).build();
        curX2 += btnSize + btnGap;

        btnTachado = Button.builder(Component.literal("S"), b -> {
            GlobalGuiSettings.TextConfig t = tSelSupplier.get();
            GlobalGuiSettings.PanelConfig p = pSelSupplier.get();
            if (t != null) t.tachado = !t.tachado;
            else if (p != null) p.tachado = !p.tachado;
        }).bounds(curX2, rowY2, btnSize, btnSize).build();
        curX2 += btnSize + btnGap;

        btnMenos = Button.builder(Component.literal("-"), b -> {
            GlobalGuiSettings.TextConfig t = tSelSupplier.get();
            GlobalGuiSettings.PanelConfig p = pSelSupplier.get();
            if (t != null) t.escala = Math.max(0.1f, t.escala - 0.1f);
            else if (p != null) {
                if (p.tipo.equals("MISION_TITULO") || p.tipo.startsWith("ESTADISTICA_")) p.escalaTexto = Math.max(0.1f, p.escalaTexto - 0.1f);
                else if (p.tipo.equals("MISION_DESCRIPCION")) p.scaleDesc = Math.max(0.1f, p.scaleDesc - 0.1f);
            }
        }).bounds(curX2, rowY2, btnSize, btnSize).build();
        curX2 += btnSize + btnGap;

        if (!esEstadistica) {
            btnMenosEspaciado = Button.builder(Component.literal("←"), b -> {
                GlobalGuiSettings.TextConfig t = tSelSupplier.get();
                GlobalGuiSettings.PanelConfig p = pSelSupplier.get();
                if (t != null) t.interletrado = Math.max(0.0f, t.interletrado - 0.5f);
                else if (p != null) p.interletrado = Math.max(0.0f, p.interletrado - 0.5f);
            }).bounds(curX2, rowY2, btnSize, btnSize).build();
        } else {
            btnMenosEspaciado = null;
        }

        actualizarEstadoBotones(tSel, pSel);

        adder.accept(btnNegrita);
        adder.accept(btnCursiva);
        adder.accept(btnSubrayado);
        adder.accept(btnMas);
        adder.accept(btnSombra);
        adder.accept(btnTachado);
        adder.accept(btnMenos);
        
        if (btnMasEspaciado != null) adder.accept(btnMasEspaciado);
        if (btnMayusculas != null) adder.accept(btnMayusculas);
        if (btnMenosEspaciado != null) adder.accept(btnMenosEspaciado);
    }

    public static void updateButtonVisibility(GlobalGuiSettings.TextConfig tSel) {
        boolean visible = (tSel != null);
        if (btnNegrita != null) {
            btnNegrita.visible = btnCursiva.visible = btnSubrayado.visible = btnTachado.visible = btnMayusculas.visible = btnEspaciado.visible = visible;
        }
    }

    private static void actualizarEstadoBotones(GlobalGuiSettings.TextConfig tSel, GlobalGuiSettings.PanelConfig pSel) {
        // Placeholder para futura lógica de resaltado de botones activos
    }
}
