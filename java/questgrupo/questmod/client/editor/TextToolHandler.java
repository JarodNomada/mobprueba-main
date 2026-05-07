package questgrupo.questmod.client.editor;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import questgrupo.questmod.client.GlobalGuiSettings;
import questgrupo.questmod.client.gui.TextoEdit;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class TextToolHandler {
    private static Button btnNegrita, btnCursiva, btnSubrayado, btnTachado, btnMayusculas;
    private static Button btnEspaciado;

    public static void initializeButtons(int guiWidth, int y, Consumer<Button> adder, Supplier<GlobalGuiSettings.TextConfig> tSelSupplier) {
        int centroX = guiWidth / 2;
        int inicioX = centroX - 72;

        btnNegrita = Button.builder(Component.literal("B"), b -> {
            GlobalGuiSettings.TextConfig tSel = tSelSupplier.get();
            if (tSel != null) tSel.negrita = !tSel.negrita;
        }).bounds(inicioX, y, 20, 18).build();

        btnCursiva = Button.builder(Component.literal("I"), b -> {
            GlobalGuiSettings.TextConfig tSel = tSelSupplier.get();
            if (tSel != null) tSel.cursiva = !tSel.cursiva;
        }).bounds(inicioX + 22, y, 20, 18).build();

        btnSubrayado = Button.builder(Component.literal("U"), b -> {
            GlobalGuiSettings.TextConfig tSel = tSelSupplier.get();
            if (tSel != null) tSel.subrayado = !tSel.subrayado;
        }).bounds(inicioX + 44, y, 20, 18).build();

        btnTachado = Button.builder(Component.literal("~~Ø~~"), b -> {
            GlobalGuiSettings.TextConfig tSel = tSelSupplier.get();
            if (tSel != null) tSel.tachado = !tSel.tachado;
        }).bounds(inicioX + 66, y, 30, 18).build();

        btnMayusculas = Button.builder(Component.literal("aA"), b -> {
            GlobalGuiSettings.TextConfig tSel = tSelSupplier.get();
            if (tSel != null) tSel.mayusculas = !tSel.mayusculas;
        }).bounds(inicioX + 98, y, 24, 18).build();

        btnEspaciado = Button.builder(Component.literal("↔"), b -> {
        }).bounds(inicioX + 124, y, 20, 18).build();

        adder.accept(btnNegrita);
        adder.accept(btnCursiva);
        adder.accept(btnSubrayado);
        adder.accept(btnTachado);
        adder.accept(btnMayusculas);
        adder.accept(btnEspaciado);
    }

    public static void updateButtonVisibility(GlobalGuiSettings.TextConfig tSel) {
        boolean visible = (tSel != null);
        if (btnNegrita != null) {
            btnNegrita.visible = btnCursiva.visible = btnSubrayado.visible = btnTachado.visible = btnMayusculas.visible = btnEspaciado.visible = visible;
        }
    }
}
