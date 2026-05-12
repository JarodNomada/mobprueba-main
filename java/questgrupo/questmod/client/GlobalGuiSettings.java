package questgrupo.questmod.client;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.ResourceLocation;

public class GlobalGuiSettings {
    public static boolean editorActivo = false;
    
    public static java.util.Set<String> misionesAceptadasCliente = new java.util.HashSet<>();
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
        public String tipo = "CUADRADO";
        public int grosor = 2; // Para pincel y formas
        public String textoAsociado = null;
        public ResourceLocation iconoRL = null;
        public float escalaTexto = 1.0f;
        public float escalaIcono = 1.0f;
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
        public int colorFondoRenglon = 0x22FFFFFF;
        public int colorBordeRenglon = 0xFFA6A6A6;
        public int colorFondoIcono = 0x00000000;
        public int colorBordeIcono = 0xFFAAAAAA;
        public int colorFondoCheck = 0x44000000;
        public int colorBordeCheckInterno = 0xFFFFFFFF;
        public int colorBordeCheckExterno = 0xFF000000;
        public int pagina = 1;

        public boolean desplegado = true;
        public boolean principalesAbierto = true;
        public boolean secundariasAbierto = true;
        public String tituloPrincipales = "Principales";
        public String tituloSecundarias = "Secundarias";
        public int scrollY = 0;
        public List<String> listaPrincipales = new ArrayList<>();
        public List<String> listaSecundarias = new ArrayList<>();

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

    public static final List<PanelConfig> PANELES = new ArrayList<>();
    public static final List<TextConfig> TEXTOS = new ArrayList<>();
    public static final List<BrushStroke> TRAZOS = new ArrayList<>();
    public static int grosorPincel = 2;
    public static int colorHerramientas = 0xFF000000;
}
