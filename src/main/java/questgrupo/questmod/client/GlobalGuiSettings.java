package questgrupo.questmod.client;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.ResourceLocation;

public class GlobalGuiSettings {
    public static boolean editorActivo = false;
    
    public static java.util.Set<String> misionesAceptadasCliente = new java.util.HashSet<>();
    public static java.util.Set<String> misionesFinalizadasCliente = new java.util.HashSet<>();
    public static java.util.Map<String, Integer> progresoMuertesCliente = new java.util.HashMap<>();
    public static String misionSeleccionadaGlobal = "";

    public static PanelConfig panelSeleccionado = null;
    public static TextConfig textoSeleccionado = null;
    public static int paginaActual = 1;
    public static int totalPaginas = 1;

    public static class PanelConfig {
        public int x, y, ancho, alto;
        public int x2, y2; // For LINEA: endpoint
        public int colorARGB = 0xAA000000;
        public int colorBorde = 0xFFFFFFFF;
        public int colorTexto = 0xFFFFFFFF;
        public int colorFondoMision = 0xFF444444;
        public int colorBordeMision = 0xFFAAAAAA;
        public int colorFondoCabecera = 0xFF222222;
        public int colorBordeCabecera = 0xFFFFFFFF;
        public int colorFondoMisionDetalle = 0xFF222222;
        public int colorBordeMisionDetalle = 0xFFAAAAAA;
        
        public int colorSecundario = 0xFF181818; 
        public int colorFondoIcono = 0xFF111111;
        public int colorBordeIcono = 0xFF333333;
        public int colorFondoRenglon = 0xFF181818;
        public int colorBordeRenglon = 0xFF2A2A2A;
        public int colorFondoCheck = 0xFF111111;
        public int colorBordeCheckInterno = 0xFF2A2A2A;
        public int colorFondoBarra = 0xFF222222;
        
        public String tipo = "CUADRADO";
        public int grosor = 2; // Para pincel y formas
        public String textoAsociado = null;
        public ResourceLocation iconoRL = null;
        public float escalaTexto = 1.0f;
        public float escalaIcono = 1.0f;
        public float escalaDesc = 0.85f;
        public float escalaItem = 1.0f;
        public float escalaCheck = 1.0f;
        public float escalaMarco = 1.0f;
        public int grosorBorde = 1;
        public int redondezBorde = 1;
        public int redondezBordeInferior = 1;
        public float offsetXTexto = 0.0f;
        public float offsetYTexto = 0.0f;
        public float escalaTextoMision = 1.0f;
        public float offsetXTextoMision = 0.0f;
        public float offsetYTextoMision = 0.0f;
        public float offsetXIcono = 0.0f;
        public float offsetYIcono = 0.0f;
        public int colorTextoMision = 0xFFFFFFFF;

        // Propiedades de formato de texto para MISION_TITULO y MISION_DESCRIPCION
        public boolean negrita = false;
        public boolean cursiva = false;
        public boolean subrayado = false;
        public boolean tachado = false;
        public boolean mayusculas = false;
        public boolean sombra = false;
        public float interletrado = 0.0f;

        // Sub-secciones editables para DETALLE_MISION
        public int subElementoSel = 0;
        public float offXDesc = 0.0f, offYDesc = 0.0f, scaleDesc = 1.0f;
        public float offXIcon = 0.0f, offYIcon = 0.0f, scaleIcon = 1.0f;
        public float offXObj = 0.0f, offYObj = 0.0f, scaleObj = 1.0f;
        public float offXRec = 0.0f, offYRec = 0.0f, scaleRec = 1.0f;
        public int colorDesc = 0xFFFFFFFF;
        public int colorObj = 0xFFFFFFFF;
        public int colorRec = 0xFFFFFFFF;

        // Nuevos colores para Objetivos
        public int colorBordeCheckExterno = 0xFF000000;
        public int pagina = 1;
        public boolean isVertical = false;
        public int visibleSlots = 5;
        public int scrollIndex = 0;
        public int columnas = 9;
        public int slotSize = 20;
        public int gap = 0;
        public int colorSlotBg = 0xFF8B8B8B;
        public int colorSlotDark = 0xFF373737;
        public int colorSlotLight = 0xFFFFFFFF;
        public boolean visible = true;
        public boolean bloqueado = false;

        public String ancla = "ANCLA_CENTRO";

        public boolean desplegado = true;
        public boolean principalesAbierto = true;
        public boolean secundariasAbierto = true;
        public String tituloPrincipales = "Principales";
        public String tituloSecundarias = "Secundarias";
        public int scrollY = 0;
        public List<String> listaPrincipales = new ArrayList<>();
        public List<String> listaSecundarias = new ArrayList<>();

        public String recursoPath = ""; // Guarda el nombre de la imagen o el ID del ítem
        public float opacidad = 1.0f; // 1.0 = 100% visible, 0.0 = invisible

        // --- NUEVAS VARIABLES PARA EL WIDGET DE PROGRESO ---
        public int progresoTipo = 0; // 0: Misiones Generales, 1: Logros, 2: Tiempo Jugado, 3: Misiones Completadas
        public int progresoEstilo = 0; // 0: Barra, 1: Porcentaje, 2: Fracción (X/Y)
        public int colorBarraLleno = 0xFF00AA00; // Verde por defecto
        public int colorBarraFondo = 0xFF555555; // Gris oscuro por defecto
        public int disenoBarra = 0; // 0: Plana, 1: Segmentada, 2: Hitos, 3: Extremos


        public PanelConfig(int x, int y, int ancho, int alto) {
            this.x = x;
            this.y = y;
            this.ancho = ancho;
            this.alto = alto;
        }
    }

    public static class BrushStroke {
        public List<int[]> puntos = new ArrayList<>(); // x, y
        public int colorARGB = 0xFF000000;
        public int grosor = 2;

        public void agregarPunto(int x, int y) {
            puntos.add(new int[]{x, y});
        }
    }

    public static class TextConfig {
        public String contenido;
        public int x, y;
        public float escala = 1.0f;
        public int colorARGB = 0xFFFFFFFF;
        public int pagina = 1;
        public boolean visible = true;
        public boolean bloqueado = false;
        public boolean negrita = false, cursiva = false, subrayado = false, tachado = false;
        public boolean sombra = true;
        public boolean mayusculas = false;
        public float rotacion = 0.0f;
        public float interletrado = 0.0f;
        public float interlineado = 1.0f;
        public int espaciadoLetras = 0;

        public TextConfig(String txt, int x, int y) {
            this.contenido = txt;
            this.x = x;
            this.y = y;
        }
    }

    // --- NUEVO SISTEMA DE GRUPO DE DIBUJO ---
    public static class GrupoDibujo {
        public java.util.List<BrushStroke> trazos = new java.util.ArrayList<>();
        public java.util.List<PanelConfig> lineas = new java.util.ArrayList<>();
        public int pagina = 1;
    }

    public static class Capa {
        public String nombre;
        public boolean visible = true;
        public boolean bloqueado = false;
        public int pagina = 1;

        public PanelConfig panel = null;
        public TextConfig texto = null;
        public GrupoDibujo dibujo = null;

        public Capa(String nombre, PanelConfig panel) {
            this.nombre = nombre; this.panel = panel;
            if (panel != null) this.pagina = panel.pagina;
        }
        public Capa(String nombre, TextConfig texto) {
            this.nombre = nombre; this.texto = texto;
            if (texto != null) this.pagina = texto.pagina;
        }
        public Capa(String nombre, GrupoDibujo dibujo) {
            this.nombre = nombre; this.dibujo = dibujo;
            if (dibujo != null) this.pagina = dibujo.pagina;
        }
    }

    public static final java.util.List<Capa> CAPAS_UI = new java.util.ArrayList<>();
    public static GrupoDibujo dibujoSeleccionado = null;

    public static void sincronizarCapas() {
        CAPAS_UI.removeIf(c -> (c.panel != null && !PANELES.contains(c.panel)) || 
                               (c.texto != null && !TEXTOS.contains(c.texto)) ||
                               (c.dibujo != null && !DIBUJOS.contains(c.dibujo)));

        for (PanelConfig p : PANELES) {
            boolean existe = false;
            for (Capa c : CAPAS_UI) { if (c.panel == p) { existe = true; break; } }
            if (!existe) CAPAS_UI.add(new Capa(p.tipo, p));
        }
        for (TextConfig t : TEXTOS) {
            boolean existe = false;
            for (Capa c : CAPAS_UI) { if (c.texto == t) { existe = true; break; } }
            if (!existe) CAPAS_UI.add(new Capa(t.contenido, t));
        }
        for (GrupoDibujo d : DIBUJOS) {
            boolean existe = false;
            for (Capa c : CAPAS_UI) { if (c.dibujo == d) { existe = true; break; } }
            if (!existe) CAPAS_UI.add(new Capa("Capa Dibujo", d));
        }
    }

    public static final java.util.List<PanelConfig> PANELES = new java.util.ArrayList<>();
    public static final java.util.List<TextConfig> TEXTOS = new java.util.ArrayList<>();
    public static final java.util.List<GrupoDibujo> DIBUJOS = new java.util.ArrayList<>();
    public static int grosorPincel = 2;
    public static int colorHerramientas = 0xFF000000;

    // --- SISTEMA DE IMÁGENES CUSTOM ---
    public static final java.util.List<net.minecraft.resources.ResourceLocation> CUSTOM_IMAGES = new java.util.ArrayList<>();
    private static boolean imagenesCustomCargadas = false;

    public static void cargarImagenesCustom() {
        java.nio.file.Path dir = net.minecraftforge.fml.loading.FMLPaths.GAMEDIR.get().resolve("questmod_images");
        if (!java.nio.file.Files.exists(dir)) {
            try { java.nio.file.Files.createDirectories(dir); } catch (Exception e) { e.printStackTrace(); }
            CUSTOM_IMAGES.clear(); // Limpiamos si la carpeta desaparece
            return;
        }

        java.io.File[] files = dir.toFile().listFiles((d, name) -> name.toLowerCase().endsWith(".png"));
        if (files == null) {
            CUSTOM_IMAGES.clear(); // Limpiamos si no hay archivos
            return;
        }

        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        net.minecraft.client.renderer.texture.TextureManager tm = mc.getTextureManager();

        // Creamos una lista temporal para ver qué imágenes existen AHORA MISMO
        java.util.List<net.minecraft.resources.ResourceLocation> imagenesActuales = new java.util.ArrayList<>();

        for (java.io.File f : files) {
            String nombreLimpio = f.getName().toLowerCase().replace(" ", "_").replace(".png", "");
            nombreLimpio = nombreLimpio.replaceAll("[^a-z0-9_.-]", ""); 
            net.minecraft.resources.ResourceLocation rl = net.minecraft.resources.ResourceLocation.tryParse("questnomas:custom_" + nombreLimpio);
            
            if (rl != null) {
                imagenesActuales.add(rl); // La anotamos en la lista actual
                
                // Si es una imagen nueva que no teníamos registrada, la procesamos
                if (!CUSTOM_IMAGES.contains(rl)) {
                    try (java.io.InputStream in = new java.io.FileInputStream(f)) {
                        com.mojang.blaze3d.platform.NativeImage nativeImage = com.mojang.blaze3d.platform.NativeImage.read(in);
                        net.minecraft.client.renderer.texture.DynamicTexture dynamicTexture = new net.minecraft.client.renderer.texture.DynamicTexture(nativeImage);
                        tm.register(rl, dynamicTexture);
                        CUSTOM_IMAGES.add(rl);
                    } catch (Exception e) {
                        System.out.println("Error al cargar imagen custom: " + f.getName());
                    }
                }
            }
        }
        
        // El toque maestro: Eliminamos de nuestra memoria las imágenes que ya no están en la carpeta
        CUSTOM_IMAGES.removeIf(rl -> !imagenesActuales.contains(rl));
    }

    public static final java.util.List<net.minecraft.world.item.ItemStack> TODAS_LAS_TEXTURAS = new java.util.ArrayList<>();
    
    public static void cargarTexturasJuego() {
        if (!TODAS_LAS_TEXTURAS.isEmpty()) return;
        for (net.minecraft.world.item.Item item : net.minecraftforge.registries.ForgeRegistries.ITEMS) {
            TODAS_LAS_TEXTURAS.add(new net.minecraft.world.item.ItemStack(item));
        }
    }
}
