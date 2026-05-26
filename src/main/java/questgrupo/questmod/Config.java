package questgrupo.questmod;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraftforge.fml.loading.FMLPaths;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Config {
    private static final File CONFIG_DIR = new File(FMLPaths.CONFIGDIR.get().toFile(), "questnomas/misiones");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static QuestConfig questConfig = new QuestConfig();
    public static Map<String, List<MisionData>> misionesCargadas = new HashMap<>();

    public static void load() {
        if (!CONFIG_DIR.exists()) {
            CONFIG_DIR.mkdirs();
        }
        questConfig.misiones.clear();
        File[] files = CONFIG_DIR.listFiles((dir, name) -> name.endsWith(".json"));
        if (files == null || files.length == 0) {
            crearMisionesPorDefecto();
            files = CONFIG_DIR.listFiles((dir, name) -> name.endsWith(".json"));
        }
        if (files != null) {
            for (File file : files) {
                try (FileReader reader = new FileReader(file)) {
                    QuestConfig partial = GSON.fromJson(reader, QuestConfig.class);
                    if (partial != null && partial.misiones != null) {
                        questConfig.misiones.addAll(partial.misiones);
                    }
                } catch (Exception e) {
                    System.err.println("Error loading " + file.getName() + ": " + e.getMessage());
                }
            }
        }
    }

    public static void save() {
        if (!CONFIG_DIR.exists()) {
            CONFIG_DIR.mkdirs();
        }
        File saveFile = new File(CONFIG_DIR, "_autosave.json");
        try (FileWriter writer = new FileWriter(saveFile)) {
            GSON.toJson(questConfig, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void cargarMisionesAhora() {
        load();
        misionesCargadas.clear();
        for (MisionData m : questConfig.misiones) {
            if (m.textura != null && !m.textura.isEmpty()) {
                m.iconoRL = net.minecraft.resources.ResourceLocation.tryParse(m.textura);
            }
            for (Objetivo obj : m.objetivos) {
                if (obj.item != null && !obj.item.isEmpty()) {
                    obj.itemReal = net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(net.minecraft.resources.ResourceLocation.tryParse(obj.item));
                }
                if (obj.textura != null && !obj.textura.isEmpty()) {
                    obj.iconoRL = net.minecraft.resources.ResourceLocation.tryParse(obj.textura);
                }
            }
            for (Recompensa rec : m.recompensas) {
                if (rec.item != null && !rec.item.isEmpty()) {
                    rec.itemReal = net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(net.minecraft.resources.ResourceLocation.tryParse(rec.item));
                }
            }
            if (m.mob != null && !m.mob.isEmpty()) {
                misionesCargadas.computeIfAbsent(m.mob, k -> new ArrayList<>()).add(m);
            }
        }
    }

    public static void inyectarMisionesEnEditor() {}

    public static java.util.List<String> getNombresMisionesPrincipales() {
        java.util.List<String> result = new java.util.ArrayList<>();
        if (questConfig != null && questConfig.misiones != null) {
            for (MisionData m : questConfig.misiones) {
                if (m.nombre != null && m.esPrimaria) result.add(m.nombre);
            }
        }
        return result;
    }

    public static java.util.List<String> getNombresMisionesSecundarias() {
        java.util.List<String> result = new java.util.ArrayList<>();
        if (questConfig != null && questConfig.misiones != null) {
            for (MisionData m : questConfig.misiones) {
                if (m.nombre != null && !m.esPrimaria) result.add(m.nombre);
            }
        }
        return result;
    }

    public static MisionData getMisionPorNombre(String nombre) {
        if (nombre == null) return null;
        for (MisionData m : questConfig.misiones) {
            if (nombre.equals(m.nombre)) return m;
        }
        return null;
    }

    private static void crearMisionesPorDefecto() {
        QuestConfig defaultConfig = new QuestConfig();

        MisionData g1 = new MisionData();
        g1.id = "granja_1_cosecha";
        g1.nombre = "Problemas de Espalda";
        g1.descripcion = "El granjero Bob necesita ayuda con su cosecha de trigo.";
        g1.mob = "minecraft:villager";
        g1.profession = "farmer";
        g1.type = "plains";
        g1.textura = "minecraft:textures/item/wheat.png";
        g1.esPrimaria = true;

        Objetivo objG1 = new Objetivo(); 
        objG1.item = "minecraft:wheat"; 
        objG1.cantidad = 10; 
        objG1.texto = "Trigo fresco"; 
        g1.objetivos.add(objG1);
        
        Recompensa recG1 = new Recompensa(); 
        recG1.item = "minecraft:emerald"; 
        recG1.cantidad = 2; 
        g1.recompensas.add(recG1);

        g1.puntos_de_entrada.put("sin_aceptar", "nodo_inicio");
        g1.puntos_de_entrada.put("en_progreso", "nodo_espera");

        g1.nodos.put("nodo_inicio", new NodoDialogo(
            List.of(
                "¡Uf, mi espalda! ¿Podrías traerme 10 de trigo? No me puedo ni agachar.",
                "Ay, mi lumbalgia... Oye, forastero, ¿me echas una mano con la cosecha?",
                "El sol quema y mis huesos crujen. ¿Me traes 10 de trigo del huerto?"
            ),
            List.of(
                new OpcionDialogo(List.of("Claro, yo te ayudo.", "Cuenta conmigo, abuelo."), "ACEPTAR_MISION", "nodo_aceptacion"),
                new OpcionDialogo(List.of("¿Y si te ayudo, me das algo a cambio?"), "NADA", "nodo_negociacion"),
                new OpcionDialogo(List.of("¿Por qué no le pides a otro?"), "NADA", "nodo_queja"),
                new OpcionDialogo(List.of("No tengo tiempo, viejo."), "NADA", "nodo_despedida_brusca")
            )
        ));

        g1.nodos.put("nodo_despedida_brusca", new NodoDialogo(
            List.of(
                "Bah, jóvenes sin corazón. Vete, no necesito tu ayuda.",
                "Está bien, ya encontraré a alguien más. Buenos días."
            ),
            null
        ));

        g1.nodos.put("nodo_negociacion", new NodoDialogo(
            List.of(
                "¡Ja! Si traes el trigo rápido, te doy dos hogazas encima. ¿Trato?",
                "Listo para regatear, ¿eh? Trigo rápido, pan gratis. ¿Cerrado?"
            ),
            List.of(
                new OpcionDialogo(List.of("Acepto el trato."), "ACEPTAR_MISION", "nodo_aceptacion"),
                new OpcionDialogo(List.of("Prefiero solo las esmeraldas."), "ACEPTAR_MISION", "nodo_aceptacion")
            )
        ));

        g1.nodos.put("nodo_queja", new NodoDialogo(
            List.of(
                "¡Porque los jóvenes de hoy ya no respetan a sus mayores! ¿Vas a ayudarme o no?",
                "El resto está ocupado. Tú eres el único que no parece tener prisa. ¿Y bien?"
            ),
            List.of(
                new OpcionDialogo(List.of("Está bien, lo haré."), "ACEPTAR_MISION", "nodo_aceptacion"),
                new OpcionDialogo(List.of("Sigue quejándote, me voy."), "NADA", "nodo_historia_inicio")
            )
        ));

        g1.nodos.put("nodo_historia_inicio", new NodoDialogo(
            List.of(
                "Tenía una familia... esposa, un hijo. Cuando la guerra llegó, lo perdí todo.",
                "Mi hijo soñaba con ser granjero como yo. Ahora solo tengo recuerdos..."
            ),
            List.of(
                new OpcionDialogo(List.of("¿Qué pasó con ellos?"), "NADA", "nodo_historia_cont"),
                new OpcionDialogo(List.of("Lo siento, no sabía..."), "NADA", "nodo_historia_final")
            )
        ));

        g1.nodos.put("nodo_historia_cont", new NodoDialogo(
            List.of(
                "Los esqueletos llegaron una noche fría. Prendieron fuego a todo.",
                "El humo, los gritos... todavía los escucho en mis pesadillas."
            ),
            List.of(
                new OpcionDialogo(List.of("Te ayudaré con la cosecha. Por ellos."), "ACEPTAR_MISION", "nodo_aceptacion"),
                new OpcionDialogo(List.of("No sé qué decir..."), "NADA", "nodo_historia_final")
            )
        ));

        g1.nodos.put("nodo_historia_final", new NodoDialogo(
            List.of(
                "No hace falta que digas nada. El silencio a veces es la mejor compañía.",
                "Basta de charla. Si quieres ayudar, el trigo no se cosecha solo."
            ),
            List.of(
                new OpcionDialogo(List.of("Está bien, iré a cosechar."), "ACEPTAR_MISION", "nodo_aceptacion"),
                new OpcionDialogo(List.of("Solo quería escuchar. Adiós."), "CERRAR", "")
            )
        ));

        g1.nodos.put("nodo_aceptacion", new NodoDialogo(
            List.of(
                "¡Gracias a los cielos! La cosecha está al este. Tráemelo rápido.",
                "Dios te lo pague. El huerto está al este, donde la tierra se agrieta.",
                "Gracias, muchacho. Ve al este y cuida con los lobos."
            ),
            List.of(
                new OpcionDialogo(List.of("Entendido, voy ahora mismo."), "NADA", "nodo_agradecimiento")
            )
        ));

        g1.nodos.put("nodo_agradecimiento", new NodoDialogo(
            List.of(
                "¡Muchas gracias, muchacho! Eres todo un héroe.",
                "Dios te bendiga, viajero. Vuelve cuando tengas el trigo.",
                "Te lo agradezco de corazón. Ten cuidado por el camino."
            ),
            null
        ));

        g1.nodos.put("nodo_espera", new NodoDialogo(
            List.of(
                "¿Aún sin el trigo? El pan no se hace solo.",
                "¿Sigues dando vueltas? Los tallos se secan.",
                "No me hagas suplicar. El campo necesita manos, no promesas."
            ),
            List.of(
                new OpcionDialogo(List.of("Toma, ya lo tengo.", "Aquí tienes tu trigo."), "COMPROBAR_ENTREGA", "nodo_entrega", "nodo_mentira"),
                new OpcionDialogo(List.of("Sigo en ello, ya casi."), "CERRAR", "")
            )
        ));

        g1.nodos.put("nodo_mentira", new NodoDialogo(
            List.of(
                "¿Te crees muy gracioso? Tienes las manos vacías. ¡Ve a buscar el trigo!",
                "No veo ningún trigo por aquí. No me hagas perder el tiempo, forastero.",
                "¿Me estás viendo cara de tonto? Vuelve cuando tengas los materiales."
            ),
            null
        ));

        g1.nodos.put("nodo_entrega", new NodoDialogo(
            List.of(
                "¡Buen chico! Se ve sano y dorado. Justo lo que necesitaba para el molino.",
                "Lo has traído a tiempo. El molino no para gracias a ti.",
                "Dorado como el sol. Eres de fiar, muchacho."
            ),
            null
        ));

        g1.nodos.put("nodo_despedida", new NodoDialogo(
            List.of(
                "Vuelve luego. Puede que necesite ayuda con otra cosa. Que la tierra te sea fértil.",
                "Si pasas por aquí, mi puerta estará abierta. Cuida esa espalda.",
                "Gracias de nuevo. Los tiempos son duros, pero la gente buena aún queda."
            ),
            null
        ));

        defaultConfig.misiones.add(g1);

        File defaultFile = new File(CONFIG_DIR, "granjero.json");
        try (FileWriter writer = new FileWriter(defaultFile)) {
            GSON.toJson(defaultConfig, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class QuestConfig { public List<MisionData> misiones = new ArrayList<>(); }

    public static class MisionData {
        public String id; public String nombre; public String descripcion;
        public String mob; public String profession; public String type; public String textura;
        public boolean esPrimaria;
        
        public transient net.minecraft.resources.ResourceLocation iconoRL;

        public RequisitosData requisitos = new RequisitosData();
        public List<Objetivo> objetivos = new ArrayList<>();
        public List<Recompensa> recompensas = new ArrayList<>();

        public Map<String, String> puntos_de_entrada = new HashMap<>();
        public Map<String, NodoDialogo> nodos = new HashMap<>();
    }

    public static class RequisitosData { public List<String> misiones_completadas = new ArrayList<>(); }

    public static class NodoDialogo {
        public List<String> textoNPC = new ArrayList<>();
        public List<OpcionDialogo> opciones = new ArrayList<>();
        
        public NodoDialogo() {}
        public NodoDialogo(List<String> textoNPC, List<OpcionDialogo> opciones) { 
            this.textoNPC = textoNPC; 
            this.opciones = opciones != null ? opciones : new ArrayList<>(); 
        }
    }

    public static class OpcionDialogo {
        public List<String> texto = new ArrayList<>();
        public String accion; 
        public String destino;
        public String destino_fallo;
        
        public OpcionDialogo() {}
        public OpcionDialogo(List<String> texto, String accion, String destino) { 
            this.texto = texto; 
            this.accion = accion; 
            this.destino = destino; 
        }
        public OpcionDialogo(List<String> texto, String accion, String destino, String destino_fallo) { 
            this.texto = texto; 
            this.accion = accion; 
            this.destino = destino; 
            this.destino_fallo = destino_fallo;
        }
    }

    public static class Objetivo {
        public String entidad; public String item; public int cantidad; public String texto; public String textura;
        public transient net.minecraft.world.item.Item itemReal;
        public transient net.minecraft.resources.ResourceLocation iconoRL;
    }

    public static class Recompensa {
        public String item; public int cantidad;
        public transient net.minecraft.world.item.Item itemReal;
    }
}
