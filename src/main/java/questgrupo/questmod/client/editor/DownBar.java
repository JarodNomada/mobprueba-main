package questgrupo.questmod.client.editor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import questgrupo.questmod.client.GlobalGuiSettings;
import questgrupo.questmod.client.gui.FigurasEdit;
import java.util.Iterator;

public class DownBar {
    public static final int TAB_WIDTH = 26;
    public static final int TAB_HEIGHT = 26;
    public static final int TAB_GAP = 6;
    public static final int MAX_PAGES = 5;

    public static void drawVanillaButton(GuiGraphics g, Font font, int x, int y, int width, int height, String text, boolean active, boolean isAddBtn) {
        int bgColor = active ? 0xFFAAAAAA : 0xFFC6C6C6;
        int borderColor = active ? 0xFF55FFFF : 0xFF000000;
        if (isAddBtn) bgColor = 0xFFC6C6C6;

        g.fill(x + 1, y + 1, x + width - 1, y + height - 1, bgColor);
        g.fill(x + 1, y, x + width - 1, y + 1, borderColor);
        g.fill(x + 1, y + height - 1, x + width - 1, y + height, borderColor);
        g.fill(x, y + 1, x + 1, y + height - 1, borderColor);
        g.fill(x + width - 1, y + 1, x + width, y + height - 1, borderColor);

        if (!active && !isAddBtn) {
            g.fill(x + 1, y + 1, x + width - 2, y + 2, 0xFFFFFFFF);
            g.fill(x + 1, y + 1, x + 2, y + height - 2, 0xFFFFFFFF);
            g.fill(x + 1, y + height - 2, x + width - 1, y + height - 1, 0xFF555555);
            g.fill(x + width - 2, y + 1, x + width - 1, y + height - 1, 0xFF555555);
        } else if (isAddBtn) {
            g.fill(x + 1, y + 1, x + width - 2, y + 2, 0xFFFFFFFF);
            g.fill(x + 1, y + 1, x + 2, y + height - 2, 0xFFFFFFFF);
            g.fill(x + 1, y + height - 2, x + width - 1, y + height - 1, 0xFF555555);
            g.fill(x + width - 2, y + 1, x + width - 1, y + height - 1, 0xFF555555);
        } else {
            g.fill(x + 1, y + 1, x + width - 1, y + 2, 0xFF555555);
            g.fill(x + 1, y + 1, x + 2, y + height - 1, 0xFF555555);
        }

        int textColor = active ? 0xFF000000 : 0xFF202020;
        if (isAddBtn) textColor = 0xFF006600;

        int textWidth = font.width(text);
        g.drawString(font, text, x + (width - textWidth) / 2, y + (height - font.lineHeight) / 2, textColor, false);
    }

    public static void render(GuiGraphics g, int guiWidth, int guiHeight) {
        Font font = Minecraft.getInstance().font;
        int tabsToDraw = GlobalGuiSettings.totalPaginas;
        int totalWidth = (tabsToDraw * TAB_WIDTH) + ((tabsToDraw - 1) * TAB_GAP);
        if (tabsToDraw < MAX_PAGES) totalWidth += TAB_WIDTH + TAB_GAP;

        int startX = (guiWidth - totalWidth) / 2;
        int y = guiHeight - 35;
        int currentX = startX;

        for (int i = 1; i <= tabsToDraw; i++) {
            boolean active = (i == GlobalGuiSettings.paginaActual);
            drawVanillaButton(g, font, currentX, y, TAB_WIDTH, TAB_HEIGHT, String.valueOf(i), active, false);

            if (active && GlobalGuiSettings.totalPaginas > 1 && GlobalGuiSettings.editorActivo) {
                g.fill(currentX + TAB_WIDTH - 8, y - 4, currentX + TAB_WIDTH + 4, y + 8, 0xFFFF3333);
                g.renderOutline(currentX + TAB_WIDTH - 8, y - 4, 12, 12, 0xFF000000);
                g.drawString(font, "x", currentX + TAB_WIDTH - 3, y - 2, 0xFFFFFFFF, false);
            }
            currentX += TAB_WIDTH + TAB_GAP;
        }
        if (GlobalGuiSettings.totalPaginas < MAX_PAGES && GlobalGuiSettings.editorActivo) {
            drawVanillaButton(g, font, currentX, y, TAB_WIDTH, TAB_HEIGHT, "+", false, true);
        }
    }

    public static boolean handleClick(double mx, double my, int guiWidth, int guiHeight) {
        int tabsToDraw = GlobalGuiSettings.totalPaginas;
        int totalWidth = (tabsToDraw * TAB_WIDTH) + ((tabsToDraw - 1) * TAB_GAP);
        if (tabsToDraw < MAX_PAGES) totalWidth += TAB_WIDTH + TAB_GAP;

        int startX = (guiWidth - totalWidth) / 2;
        int y = guiHeight - 35;

        if (my < y - 5 || my > y + TAB_HEIGHT) return false;

        int currentX = startX;
        for (int i = 1; i <= tabsToDraw; i++) {
            boolean active = (i == GlobalGuiSettings.paginaActual);

            if (active && GlobalGuiSettings.totalPaginas > 1 && GlobalGuiSettings.editorActivo) {
                if (mx >= currentX + TAB_WIDTH - 8 && mx <= currentX + TAB_WIDTH + 4 && my >= y - 4 && my <= y + 8) {
                    deletePage(i);
                    return true;
                }
            }

            if (mx >= currentX && mx <= currentX + TAB_WIDTH) {
                if (GlobalGuiSettings.paginaActual != i) {
                    GlobalGuiSettings.paginaActual = i;
                    GlobalGuiSettings.panelSeleccionado = null;
                    GlobalGuiSettings.textoSeleccionado = null;
                }
                return true;
            }
            currentX += TAB_WIDTH + TAB_GAP;
        }

        if (GlobalGuiSettings.totalPaginas < MAX_PAGES && GlobalGuiSettings.editorActivo) {
            if (mx >= currentX && mx <= currentX + TAB_WIDTH) {
                GlobalGuiSettings.totalPaginas++;
                GlobalGuiSettings.paginaActual = GlobalGuiSettings.totalPaginas;
                GlobalGuiSettings.panelSeleccionado = null;
                GlobalGuiSettings.textoSeleccionado = null;
                FigurasEdit.crearBotonPagina(GlobalGuiSettings.totalPaginas);
                return true;
            }
        }
        return false;
    }

    private static void deletePage(int pageToDelete) {
        GlobalGuiSettings.PANELES.removeIf(p -> p.pagina == pageToDelete);
        GlobalGuiSettings.TEXTOS.removeIf(t -> t.pagina == pageToDelete);
        GlobalGuiSettings.PANELES.removeIf(p -> p.tipo.equals("BOTON_PAGINA") && String.valueOf(pageToDelete).equals(p.textoAsociado));

        for (GlobalGuiSettings.PanelConfig p : GlobalGuiSettings.PANELES) {
            if (p.pagina > pageToDelete) p.pagina--;
            if (p.tipo.equals("BOTON_PAGINA")) {
                try {
                    int btnPage = Integer.parseInt(p.textoAsociado);
                    if (btnPage > pageToDelete) p.textoAsociado = String.valueOf(btnPage - 1);
                } catch(Exception e){}
            }
        }
        for (GlobalGuiSettings.TextConfig t : GlobalGuiSettings.TEXTOS) {
            if (t.pagina > pageToDelete) t.pagina--;
        }

        GlobalGuiSettings.totalPaginas--;
        GlobalGuiSettings.paginaActual = 1;
        GlobalGuiSettings.panelSeleccionado = null;
        GlobalGuiSettings.textoSeleccionado = null;
    }
}