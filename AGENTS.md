# 🤖 GUÍA PARA EL AGENTE: Proyecto Questnomas

*Este documento es el manual operativo para cualquier IA que trabaje en el mod de Minecraft "Questnomas". Contiene la estructura clave del proyecto y las reglas esenciales a seguir.*

---

## 🚀 1. Visión General del Proyecto

- **Nombre**: Questnomas
- **Plataforma**: Minecraft Forge 1.20.1
- **Tipo**: Mod de sistema de misiones RPG con editor visual.
- **Paquete Principal**: `questgrupo.questmod`

---

## 🗺️ 2. Mapa Rápido de Componentes Clave

Aquí se describe la función principal de cada parte del código.

### 🧩 2.1. Núcleo del Mod (`questgrupo.questmod`)
- **`Questnomas.java`**: El "cerebro principal". Inicia el mod, carga configuraciones y conecta el sistema de red.
- **`Config.java`**: La "base de datos". Almacena y gestiona TODA la información de las misiones y la apariencia visual del mod.
- **`GlobalGuiSettings.java`**: La "memoria del editor". Guarda la configuración actual y los elementos dibujados en el editor visual.

### 🌐 2.2. Comunicación entre Jugadores y Servidor (`questgrupo.questmod.network`)
- **`Messages.java`**: El "centro de mensajería". Aquí se definen y registran todos los mensajes que viajan entre el cliente (jugador) y el servidor.
- **`PacketAceptarMision.java`**: Mensaje para cuando un jugador ACEPTA una misión. Va del Cliente al Servidor.
- **`PacketMisionCompletada.java`**: Mensaje para cuando una misión ha sido COMPLETADA. Va del Servidor al Cliente.

### 🖥️ 2.3. Interfaz del Juego (`questgrupo.questmod.client`)
- **`QuestScreen.java`**: La "pantalla de diálogos". Es la interfaz principal donde el jugador interactúa con las misiones.
- **`KeyInit.java`**: El "lector de teclas". Configura y detecta las pulsaciones de teclado específicas del mod.

### 🎨 2.4. Editor Visual de Misiones (`questgrupo.questmod.client.editor`)
- **`EditorScreen.java`**: El "lienzo y herramientas". Es la pantalla principal donde puedes DISEÑAR y editar las misiones visualmente.
- **`Viewport.java`**: La "ventana de previsualización". Muestra en tiempo real lo que estás diseñando.
- **`LeftSidebar.java`**: El "panel de herramientas izquierdo". Contiene las opciones principales del editor.
- **`TopBar.java`**: La "barra superior de herramientas". Ofrece controles específicos para texto y figuras.
- **`KeyboardShortcuts.java`**: Los "atajos de teclado". Define combinaciones de teclas para acciones rápidas en el editor.
- **`TextToolHandler.java`**: El "gestor de texto". Ayuda a manejar las herramientas de edición de texto.

### ✏️ 2.5. Componentes Gráficos (`questgrupo.questmod.client.gui`)
- **`TextoEdit.java`**: El "editor de texto inteligente". Permite aplicar formatos (negrita, cursiva, sombra, etc.) y ajustar espaciados.
- **`FigurasEdit.java`**: El "dibujante de formas". Se encarga de renderizar rectángulos, líneas y otras figuras.

### 🔔 2.6. Eventos y Disparadores (`questgrupo.questmod.events`)
- **`ClickAldeano.java`**: El "detector de clics". Activa la interfaz de misiones al hacer clic en un aldeano.
- **`ClientKeyEvents.java`**: El "manejador de eventos de teclado". Procesa otras teclas del lado del cliente.
- **`QuestGeometryEvents.java`**: El "renderizador de geometría". Dibuja elementos visuales específicos del mod en el mundo o la UI.

---

## 📜 3. Reglas Esenciales para el Desarrollo

1.  **Versión Única**: Este proyecto está configurado para funcionar EXCLUSIVAMENTE con **Minecraft Forge 1.20.1**. No se debe intentar adaptar o generar código para otras versiones.
2.  **Estructura Flexible**: Se permite crear nuevas carpetas o paquetes dentro del proyecto si es necesario para mantener una organización lógica y escalable del código.
3.  **Separación Cliente/Servidor**: El código de interfaz de usuario (`RenderSystem`, `Screen`, `GuiGraphics` o texturas) DEBE residir solo en los paquetes con `.client`. Evita mezclarlos con la lógica del servidor para prevenir errores de ejecución.

---

## ⚠️ 4. Lecciones Aprendidas y Fragmentos Clave (Registro de Errores)

Para evitar repetir errores pasados durante el desarrollo, ten en cuenta lo siguiente y guíate por estos fragmentos de código estructurales:

1.  **Orden de Eventos en UI (Condiciones de Carrera)**: Si un componente como `EditBox` tiene un *responder* que modifica variables de estado (ej. cierra el menú al validar), establece tus variables de estado *después* de llamar a `setValue()`. Además, ten en cuenta que cuando creas un componente visual con `new EditBox(..., 0, 0, ...)`, este se inicializa en la posición `(0,0)`. Es el método `render()` el encargado de moverlo a su posición final. Si la lógica condicional en `render()` falla (por ejemplo, porque una variable booleana se apagó incorrectamente), el componente se quedará atascado en la esquina superior izquierda.
    *   *Ejemplo en `EditorScreen.java` (`init` y `mouseClicked`):*
        ```java
        // INCORRECTO: El responder de setValue apaga la variable 'editandoColorHerramientas'
        // Esto causa que el render() ignore la lógica de posicionamiento y el cuadro se quede en 0,0
        editandoColorHerramientas = true;
        inputColor.setValue("FF000000"); 
        
        // CORRECTO: Asignar el valor primero, el responder se ejecuta, y luego encendemos la variable
        // Así el render() detecta la variable en true y posiciona el cuadro correctamente
        inputColor.setValue("FF000000");
        editandoColorHerramientas = true; 
        inputColor.visible = true;
        ```

2.  **Manejo de Llaves `{}` y Duplicación (`LeftSidebar.java`)**: Al modificar el método `handleClick`, respeta estrictamente la jerarquía de los bloques `else if`. Un error común es cerrar prematuramente el bloque principal de herramientas.
    *   *Estructura Clave en `LeftSidebar.java` (`handleClick`):*
        ```java
        if (selectedModule == 1) {
            // ...
            if (showShapesMenu) {
                // Lógica de formas...
            } else { // <-- ESTE ES EL BLOQUE PRINCIPAL DE HERRAMIENTAS
                // 1. Detección de clics en el menú principal (Mouse, Pincel, Línea)
                // 2. Detección de clics en el cuadro de Color (compartido)
                // 3. Detección de clics en controles de Grosor (+ / -)
            }
        }
        ```

3.  **Implementación Completa de Propiedades (Ej: Grosor)**: Toda nueva propiedad visual debe implementarse en tres fases exactas:
    *   **Creación/Guardado (`EditorScreen.java` - `mouseReleased`)**:
        ```java
        linea.colorARGB = GlobalGuiSettings.colorHerramientas;
        linea.grosor = GlobalGuiSettings.grosorPincel; // ¡CRUCIAL GUARDARLO!
        GlobalGuiSettings.PANELES.add(linea);
        ```
    *   **Previsualización en vivo (`EditorScreen.java` - `render`)**:
        ```java
        drawLineThick(g, startX, startY, currentX, currentY, color, GlobalGuiSettings.grosorPincel);
        ```
    *   **Renderizado Definitivo (`FigurasEdit.java` - `renderizar`)**:
        ```java
        drawLineThick(g, p.x, p.y, p.x2, p.y2, p.colorARGB, p.grosor);
        ```

4.  **Cuidado con Métodos "Hardcodeados"**: No uses métodos fijos si necesitas variables dinámicas. `drawLine1px` fue reemplazado por `drawLineThick` usando una interpolación simple con cuadrados para simular grosor tipo Minecraft:
    *   *Lógica base para líneas gruesas:*
        ```java
        // En lugar de g.fill(x, y, x+1, y+1) de 1px:
        int half = grosor / 2;
        g.fill(x - half, y - half, x - half + grosor, y - half + grosor, color);
        ```

5.  **Comportamiento del Responder de `EditBox` (Cierre Prematuro)**: Cuando utilices un `EditBox` para validar entradas (como un código de color hexadecimal de 8 caracteres), evita colocar lógica que oculte o desenfoque el recuadro dentro del bloque de validación si la longitud se cumple inmediatamente.
    *   Si cierras el recuadro tan pronto como la condición es verdadera (ej. `if (s.length() == 8)`), el usuario no podrá borrar, corregir o mover el cursor después de pegar o escribir el valor completo, ya que el recuadro "desaparecerá mágicamente".
    *   **Solución**: El *responder* solo debe actualizar las variables de datos en tiempo real. La lógica para cerrar u ocultar el recuadro (`inputColor.visible = false;`) debe manejarse en el método `keyPressed` (al presionar `Enter` o `Esc`) o en `mouseClicked` (al hacer clic fuera del recuadro). Además, al abrir un campo de texto interactivo sobre un elemento existente (como una vista previa de color), asegúrate de asignar sus coordenadas (`setX`, `setY`) inmediatamente en el evento de clic, para que no dependa solo del renderizador para encontrar su posición correcta.

6.  **Desplegables de Misiones en Modo OFF**: En el editor visual, cuando el editor está en modo OFF (`GlobalGuiSettings.editorActivo = false`), los paneles de tipo `DESPLEGABLE_MAESTRO` permiten alternar la visibilidad de las secciones "principales" y "secundarias" mediante clics en sus cabeceras.
    *   El estado desplegado/colapsado se gestiona mediante las banderas `principalesAbierto` y `secundariasAbierto` en `GlobalGuiSettings.PanelConfig`.
    *   El renderizado de estas secciones se controla en `EditorScreen.java` (líneas ~800-812) mediante comprobaciones de coordenadas y actualización de las banderas correspondientes.
    *   Esta funcionalidad permite a los usuarios ver u ocultar detalles de misiones en modo visualización sin entrar al modo edición.