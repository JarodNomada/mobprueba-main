package questgrupo.questmod.client.editor;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import questgrupo.questmod.client.GlobalGuiSettings;

import java.util.Stack;

public class KeyboardShortcuts {
    private static final Stack<String> undoStack = new Stack<>();

    public static boolean handleKeyPress(int key, int mods, GlobalGuiSettings.TextConfig tSel) {
        boolean ctrlDown = Screen.hasControlDown();

        if (ctrlDown && key == InputConstants.KEY_C) {
            if (tSel != null) copyText(tSel);
            return true;
        }
        if (ctrlDown && key == InputConstants.KEY_V) {
            pasteText(tSel);
            return true;
        }
        if (ctrlDown && key == InputConstants.KEY_Z) {
            undo();
            return true;
        }
        return false;
    }

    private static void copyText(GlobalGuiSettings.TextConfig tSel) {
        Minecraft.getInstance().keyboardHandler.setClipboard(tSel.contenido);
        undoStack.push(tSel.contenido);
    }

    private static void pasteText(GlobalGuiSettings.TextConfig tSel) {
        if (tSel != null) {
            String clipboard = Minecraft.getInstance().keyboardHandler.getClipboard();
            tSel.contenido += clipboard;
        }
    }

    private static void undo() {
        if (!undoStack.isEmpty()) {
            undoStack.pop();
        }
    }
}
