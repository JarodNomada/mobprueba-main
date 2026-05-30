package questgrupo.questmod.client;

import com.google.gson.*;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class InterfaceManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final java.nio.file.Path INTERFACE_DIR = FMLPaths.CONFIGDIR.get().resolve("questnomas").resolve("interface");

    static {
        try {
            java.nio.file.Files.createDirectories(INTERFACE_DIR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static File getInterfaceDir() {
        return INTERFACE_DIR.toFile();
    }

    public static File getFile(String nombre) {
        return INTERFACE_DIR.resolve(nombre + ".json").toFile();
    }

    public static List<String> obtenerListaInterfaces() {
        List<String> list = new ArrayList<>();
        File dir = INTERFACE_DIR.toFile();
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".json"));
            if (files != null) {
                for (File f : files) {
                    list.add(f.getName().replace(".json", ""));
                }
            }
        }
        return list;
    }

    public static void guardarColoresInterface(String nombre) {
        try {
            File file = getFile(nombre);
            ColorsData data = new ColorsData();
            data.outerBg = DialogueColors.outerBg;
            data.mainBg = DialogueColors.mainBg;
            data.trazoExterior = DialogueColors.trazoExterior;
            data.trazoRecuadro = DialogueColors.trazoRecuadro;
            data.trazoInterior = DialogueColors.trazoInterior;
            data.separator = DialogueColors.separator;
            data.separatorSombra = DialogueColors.separatorSombra;
            data.textNPC = DialogueColors.textNPC;
            data.btnBgNorm = DialogueColors.btnBgNorm;
            data.btnLight = DialogueColors.btnLight;
            data.btnDark = DialogueColors.btnDark;
            data.btnText = DialogueColors.btnText;
            data.btnOutline = DialogueColors.btnOutline;
            data.hoverGreen = DialogueColors.hoverGreen;
            data.hoverLight = DialogueColors.hoverLight;
            data.hoverDark = DialogueColors.hoverDark;

            try (FileWriter writer = new FileWriter(file)) {
                GSON.toJson(data, writer);
            }
            if (Minecraft.getInstance().player != null) {
                Minecraft.getInstance().player.displayClientMessage(Component.literal("§a¡Colores de interfaz '" + nombre + "' guardados!"), false);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (Minecraft.getInstance().player != null) {
                Minecraft.getInstance().player.displayClientMessage(Component.literal("§cError al guardar interfaz."), false);
            }
        }
    }

    public static void cargarColoresInterface(String nombre) {
        File file = getFile(nombre);
        if (!file.exists()) return;
        try (FileReader reader = new FileReader(file)) {
            ColorsData data = GSON.fromJson(reader, ColorsData.class);
            if (data == null) return;
            DialogueColors.outerBg = data.outerBg;
            DialogueColors.mainBg = data.mainBg;
            DialogueColors.trazoExterior = data.trazoExterior;
            DialogueColors.trazoRecuadro = data.trazoRecuadro;
            DialogueColors.trazoInterior = data.trazoInterior;
            DialogueColors.separator = data.separator;
            DialogueColors.separatorSombra = data.separatorSombra;
            DialogueColors.textNPC = data.textNPC;
            DialogueColors.btnBgNorm = data.btnBgNorm;
            DialogueColors.btnLight = data.btnLight;
            DialogueColors.btnDark = data.btnDark;
            DialogueColors.btnText = data.btnText;
            DialogueColors.btnOutline = data.btnOutline;
            DialogueColors.hoverGreen = data.hoverGreen;
            DialogueColors.hoverLight = data.hoverLight;
            DialogueColors.hoverDark = data.hoverDark;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class ColorsData {
        public int outerBg = 0xFF655746;
        public int mainBg = 0xFF4C4238;
        public int trazoExterior = 0xFF7D7060;
        public int trazoRecuadro = 0xFF403A30;
        public int trazoInterior = 0xFF736351;
        public int separator = 0xFF312D26;
        public int separatorSombra = 0xFF5A4D42;
        public int textNPC = 0xFFCECECF;
        public int btnBgNorm = 0xFF665A48;
        public int btnLight = 0xFF837563;
        public int btnDark = 0xFF383028;
        public int btnText = 0xFFF2F2F2;
        public int btnOutline = 0xFF201C17;
        public int hoverGreen = 0xFF4A792A;
        public int hoverLight = 0xFF73BD42;
        public int hoverDark = 0xFF36591F;
    }
}
