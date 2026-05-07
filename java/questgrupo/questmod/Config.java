package questgrupo.questmod;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;
import questgrupo.questmod.client.GlobalGuiSettings;


import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;


public class Config {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // Rutas y Constantes
    private static final Path CARPETA_QUESTS = FMLPaths.CONFIGDIR.get().resolve("questmobs");
    private static final Path ARCHIVO_VISUAL = CARPETA_QUESTS.resolve("apariencia.json");
    private static final int LIMITE_MISIONES = 30;

    // Datos en Memoria
    public static final Map<String, List<MisionData>> misionesCargadas = new HashMap<>();
    public static VisualConfig apariencia = new VisualConfig();

    // --- CLASES INTERNAS DE CONFIGURACIÓN ---

    public static class VisualConfig {
        public String colorMarco = "FFFFFF";
        public int opacidadMarco = 68;
        public String colorPaneles = "050505";
        public int opacidadPaneles = 221;
        public int opacidadBordeFino = 68;
        public int anchoDiario = 600;
        public boolean estirarDerecha = true;

        public int getColorMarcoARGB() {
            return (opacidadMarco << 24) | (Integer.parseInt(colorMarco, 16) & 0xFFFFFF);
        }

        public int getColorPanelesARGB() {
            return (opacidadPaneles << 24) | (Integer.parseInt(colorPaneles, 16) & 0xFFFFFF);
        }

        public int getColorBordeFino() {
            int colorBase = Integer.parseInt(colorMarco, 16) & 0xFFFFFF;
            return (opacidadBordeFino << 24) | colorBase;
        }
    }

    public static class Objetivo {
        public String item;
        public int cantidad;
        public transient Item itemReal;
    }

    public static class Recompensa {
        public String item;
        public int cantidad;
        public transient Item itemReal;
    }

    public static class MisionData {
        public String nombre;
        public String descripcion;
        public String mob;
        public String profession;
        public String type;
        public String textura;
        public boolean esPrimaria = true;

        public List<Objetivo> objetivos = new ArrayList<>();
        public List<Recompensa> recompensas = new ArrayList<>();
        public List<String> frases;

        public String recordatorio;
        public String error;
        public String agradecimiento;

        public transient ResourceLocation iconoRL;
    }

    public static MisionData getMisionPorNombre(String nombre) {
        if (nombre == null || nombre.isEmpty()) return null;
        for (List<MisionData> lista : misionesCargadas.values()) {
            for (MisionData m : lista) {
                if (nombre.equals(m.nombre)) return m;
            }
        }
        return null;
    }

    // --- MÉTODOS DE CARGA Y GUARDADO ---

    public static void guardarVisual() {
        try (Writer writer = new FileWriter(ARCHIVO_VISUAL.toFile())) {
            GSON.toJson(apariencia, writer);
        } catch (IOException e) {
            LOGGER.error("Error al guardar apariencia", e);
        }
    }

    public static void cargarMisionesAhora() {
        misionesCargadas.clear();
        try {
            if (!Files.exists(CARPETA_QUESTS)) {
                Files.createDirectories(CARPETA_QUESTS);
            }

            // 1. Cargar Configuración Visual
            if (Files.exists(ARCHIVO_VISUAL)) {
                try (Reader reader = new FileReader(ARCHIVO_VISUAL.toFile())) {
                    VisualConfig cargada = GSON.fromJson(reader, VisualConfig.class);
                    if (cargada != null) apariencia = cargada;
                }
            } else {
                guardarVisual();
            }

            // 2. Cargar Misiones JSON
            File[] archivos = CARPETA_QUESTS.toFile().listFiles((dir, name) ->
                    name.endsWith(".json") && !name.equals("apariencia.json")
            );

            if (archivos == null || archivos.length == 0) {
                crearEjemplo();
                archivos = CARPETA_QUESTS.toFile().listFiles((dir, name) ->
                        name.endsWith(".json") && !name.equals("apariencia.json")
                );
            }

            if (archivos != null) {
                for (File archivo : archivos) {
                    try (Reader reader = new FileReader(archivo)) {
                        MisionData[] arrayMisiones = GSON.fromJson(reader, MisionData[].class);
                        if (arrayMisiones == null) continue;

                        for (MisionData m : arrayMisiones) {
                            if (m.mob == null) continue;

                            List<MisionData> listaActual = misionesCargadas.computeIfAbsent(m.mob, k -> new ArrayList<>());

                            if (listaActual.size() < LIMITE_MISIONES) {
                                // Registrar Ítems de Objetivos
                                if (m.objetivos != null) {
                                    for (Objetivo obj : m.objetivos) {
                                        obj.itemReal = ForgeRegistries.ITEMS.getValue(ResourceLocation.tryParse(obj.item));
                                    }
                                }

                                // Registrar Ítems de Recompensas
                                if (m.recompensas != null) {
                                    for (Recompensa rec : m.recompensas) {
                                        rec.itemReal = ForgeRegistries.ITEMS.getValue(ResourceLocation.tryParse(rec.item));
                                    }
                                }

                                // Configurar Textura/Icono
                                String rutaTex = (m.textura != null && !m.textura.isEmpty()) ? m.textura : "minecraft:textures/item/paper.png";
                                m.iconoRL = ResourceLocation.parse(rutaTex);

                                listaActual.add(m);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error crítico al cargar la configuración de misiones", e);
        }
        
        inyectarMisionesEnEditor();
    }

    public static void inyectarMisionesEnEditor() {
        GlobalGuiSettings.PanelConfig maestro = null;
        GlobalGuiSettings.PanelConfig soloPrincipal = null;
        GlobalGuiSettings.PanelConfig soloSecundaria = null;

        // Buscamos si ya existen en la lista para no duplicarlos
        for (GlobalGuiSettings.PanelConfig p : GlobalGuiSettings.PANELES) {
            if (p.tipo.equals("DESPLEGABLE_MAESTRO")) {
                maestro = p;
                maestro.listaPrincipales.clear();
                maestro.listaSecundarias.clear();
            } else if (p.tipo.equals("DESPLEGABLE_PRINCIPAL")) {
                soloPrincipal = p;
                soloPrincipal.listaPrincipales.clear();
            } else if (p.tipo.equals("DESPLEGABLE_SECUNDARIA")) {
                soloSecundaria = p;
                soloSecundaria.listaSecundarias.clear();
            }
        }

        // Si no los has creado tú manualmente, no hacemos nada (Evita que aparezcan solos)
        if (maestro == null && soloPrincipal == null && soloSecundaria == null) {
            return;
        }

        java.util.Set<String> aceptadasDelCliente = new java.util.HashSet<>(GlobalGuiSettings.misionesAceptadasCliente);

        for (java.util.Map.Entry<String, java.util.List<MisionData>> entry : misionesCargadas.entrySet()) {
            for (MisionData m : entry.getValue()) {
                String nombreMision = m.nombre;
                boolean estaAceptada = false;
                for (String keyAceptada : aceptadasDelCliente) {
                    if (keyAceptada.contains(nombreMision.replace(" ", "_"))) {
                        estaAceptada = true;
                        break;
                    }
                }
                if (!estaAceptada) continue;

                if (m.esPrimaria) {
                    if (maestro != null) maestro.listaPrincipales.add(nombreMision);
                    if (soloPrincipal != null) soloPrincipal.listaPrincipales.add(nombreMision);
                } else {
                    if (maestro != null) maestro.listaSecundarias.add(nombreMision);
                    if (soloSecundaria != null) soloSecundaria.listaSecundarias.add(nombreMision);
                }
            }
        }
    }

    private static void crearEjemplo() throws IOException {
        File fileAldeanos = CARPETA_QUESTS.resolve("misiones_aldeanos.json").toFile();
        List<MisionData> listaAldeanos = new ArrayList<>();

        MisionData a1 = new MisionData();
        a1.nombre = "Kit del Desierto";
        a1.descripcion = "El bibliotecario necesita papel y algo de cuero para sus nuevos libros.";
        a1.mob = "minecraft:villager";
        a1.profession = "librarian";
        a1.type = "desert";
        a1.textura = "minecraft:textures/item/map.png";
        a1.esPrimaria = true;

        Objetivo obj1 = new Objetivo(); obj1.item = "minecraft:paper"; obj1.cantidad = 10;
        Objetivo obj2 = new Objetivo(); obj2.item = "minecraft:leather"; obj2.cantidad = 2;
        a1.objetivos = List.of(obj1, obj2);

        Recompensa rec1 = new Recompensa(); rec1.item = "minecraft:emerald"; rec1.cantidad = 2;
        a1.recompensas = List.of(rec1);

        a1.frases = List.of("¡Viajero!", "¿Podrías traerme materiales?");
        a1.recordatorio = "¿Tienes las cosas?";
        a1.error = "Aún te faltan materiales.";
        a1.agradecimiento = "¡Excelente!";

        listaAldeanos.add(a1);

        try (Writer writerA = new FileWriter(fileAldeanos)) {
            GSON.toJson(listaAldeanos, writerA);
        }
    }
}