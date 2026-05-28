package questgrupo.questmod.client;

import com.google.gson.*;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class GuiLayoutManager {
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(ResourceLocation.class, new ResourceLocationAdapter())
            .create();

    private static final java.nio.file.Path GUI_DIR = FMLPaths.CONFIGDIR.get().resolve("questnomas").resolve("guis");

    public static String layoutSeleccionado = "default";
    private static boolean cargaAutomaticaHecha = false;

    static {
        try {
            java.nio.file.Files.createDirectories(GUI_DIR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static File getGuiDir() {
        return GUI_DIR.toFile();
    }

    public static File getFile(String nombre) {
        return GUI_DIR.resolve(nombre + ".json").toFile();
    }

    public static List<String> obtenerListaLayouts() {
        List<String> layouts = new ArrayList<>();
        File dir = GUI_DIR.toFile();
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".json"));
            if (files != null) {
                for (File f : files) {
                    layouts.add(f.getName().replace(".json", ""));
                }
            }
        }
        return layouts;
    }

    public static void guardarLayout(String nombre) {
        try {
            File file = getFile(nombre);
            LayoutData data = new LayoutData();
            data.paneles = GlobalGuiSettings.PANELES;
            data.textos = GlobalGuiSettings.TEXTOS;
            data.dibujos = GlobalGuiSettings.DIBUJOS;

            try (FileWriter writer = new FileWriter(file)) {
                GSON.toJson(data, writer);
            }
            if (Minecraft.getInstance().player != null) {
                Minecraft.getInstance().player.displayClientMessage(Component.literal("§a¡GUI '" + nombre + "' guardada con éxito en questnomas/guis/"), false);
            }
            layoutSeleccionado = nombre;
        } catch (Exception e) {
            e.printStackTrace();
            if (Minecraft.getInstance().player != null) {
                Minecraft.getInstance().player.displayClientMessage(Component.literal("§cError al guardar la GUI. Revisa el registro (log)."), false);
            }
        }
    }

    public static void cargarLayout(String nombre) {
        File file = getFile(nombre);
        if (!file.exists()) {
            if (Minecraft.getInstance().player != null) {
                Minecraft.getInstance().player.displayClientMessage(Component.literal("§eNo se encontró el layout '" + nombre + "' para cargar."), false);
            }
            return;
        }
        try {
            try (FileReader reader = new FileReader(file)) {
                LayoutData data = GSON.fromJson(reader, LayoutData.class);
                if (data != null) {
                    GlobalGuiSettings.PANELES.clear();
                    if (data.paneles != null) GlobalGuiSettings.PANELES.addAll(data.paneles);

                    GlobalGuiSettings.TEXTOS.clear();
                    if (data.textos != null) GlobalGuiSettings.TEXTOS.addAll(data.textos);

                    GlobalGuiSettings.DIBUJOS.clear();
                    if (data.dibujos != null) GlobalGuiSettings.DIBUJOS.addAll(data.dibujos);

                    GlobalGuiSettings.CAPAS_UI.clear();
                    GlobalGuiSettings.sincronizarCapas();

                    if (Minecraft.getInstance().player != null) {
                        Minecraft.getInstance().player.displayClientMessage(Component.literal("§e¡GUI '" + nombre + "' cargada con éxito!"), false);
                    }
                    layoutSeleccionado = nombre;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (Minecraft.getInstance().player != null) {
                Minecraft.getInstance().player.displayClientMessage(Component.literal("§cError al cargar la GUI."), false);
            }
        }
    }

    public static void eliminarLayout(String nombre) {
        File file = getFile(nombre);
        if (file.exists() && file.delete()) {
            if (Minecraft.getInstance().player != null) {
                Minecraft.getInstance().player.displayClientMessage(Component.literal("§e¡GUI '" + nombre + "' eliminada!"), false);
            }
            if (layoutSeleccionado.equals(nombre)) {
                layoutSeleccionado = "default";
            }
        }
    }

    public static void cargarLayoutAutomatico() {
        if (cargaAutomaticaHecha) return;
        cargaAutomaticaHecha = true;

        File defaultFile = getFile(layoutSeleccionado);
        if (defaultFile.exists()) {
            cargarLayout(layoutSeleccionado);
        } else {
            List<String> disponibles = obtenerListaLayouts();
            if (!disponibles.isEmpty()) {
                cargarLayout(disponibles.get(0));
            }
        }
    }

    private static class LayoutData {
        public java.util.List<GlobalGuiSettings.PanelConfig> paneles = new ArrayList<>();
        public java.util.List<GlobalGuiSettings.TextConfig> textos = new ArrayList<>();
        public java.util.List<GlobalGuiSettings.GrupoDibujo> dibujos = new ArrayList<>();
    }

    private static class ResourceLocationAdapter implements JsonSerializer<ResourceLocation>, JsonDeserializer<ResourceLocation> {
        @Override
        public JsonElement serialize(ResourceLocation src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.toString());
        }

        @Override
        public ResourceLocation deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return new ResourceLocation(json.getAsString());
        }
    }
}
