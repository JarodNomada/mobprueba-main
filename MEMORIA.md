# 🧠 MEMORIA TÉCNICA: Questnomas

*Este documento es el diario técnico del proyecto. Guarda el estado actual, las decisiones tomadas y las tareas pendientes para que cualquier IA pueda continuar donde nos quedamos.*

---

## 📊 1. Estado Actual del Proyecto

- **Última actualización**: 02 de Mayo 2026
- **Análisis completado**: Todos los archivos Java del proyecto han sido analizados
- **Sistema de misiones**: Funcional - carga misiones desde JSONs
- **Editor visual**: Con herramientas de figuras y texto

---

## 🗺️ 2. Mapa de Responsabilidades

Aquí se describe qué hace cada parte del proyecto.

### 🧩 Núcleo del Mod (`questgrupo.questmod`)
- **Questnomas.java**: Inicia el mod, carga configuraciones al iniciar el juego
- **Config.java**: Maneja TODA la información de las misiones y la apariencia visual
- **GlobalGuiSettings.java**: Guarda la configuración actual del editor visual

### 🌐 Comunicación y Red (`questgrupo.questmod.network`)
- **Messages.java**: Canal de comunicación entre cliente y servidor
- **PacketAceptarMision.java**: Se envía cuando el jugador ACEPTA una misión
- **PacketMisionCompletada.java**: Se envía cuando la misión se completa

### 🖥️ Interfaz del Juego (`questgrupo.questmod.client`)
- **QuestScreen.java**: Pantalla de diálogos con efecto máquina de escribir
- **KeyInit.java**: Detecta las teclas para abrir los menús

### 🎨 Editor Visual (`questgrupo.questmod.client.editor`)
- **EditorScreen.java**: Pantalla principal para diseñar misiones visualmente
- **Viewport.java**: Área donde se prévisualiza lo que estás diseñando
- **LeftSidebar.java**: Panel lateral con herramientas del editor
- **TopBar.java**: Barra superior con opciones de texto y figuras
- **KeyboardShortcuts.java**: Atajos de teclado (Ctrl+S, etc.)
- **TextToolHandler.java**: Manejo de herramientas de edición de texto

### ✏️ Componentes Gráficos (`questgrupo.questmod.client.gui`)
- **TextoEdit.java**: Editor avanzado de texto (negrita, cursiva, sombra, interletrado)
- **FigurasEdit.java**: Dibuja rectángulos, líneas y otras formas

### 🔔 Eventos (`questgrupo.questmod.events`)
- **ClickAldeano.java**: Detecta clics en aldeanos para abrir el menú de misiones
- **ClientKeyEvents.java**: Maneja teclas del lado del cliente
- **QuestGeometryEvents.java**: Renderiza elementos geométricos

---

## 📝 3. Decisiones Técnicas import import import import import import import import import import import import import import import import import import import import import import import import import import import import import import import import import import import import import import import import import

- **Transient Items**: Se usa `transient Item itemReal` en CONFIG para guardar solo el ID del item como texto (ej: "minecraft:paper") en lugar del objeto directo. Esto permite que los JSONs se guarden correctamente.
- **Persistencia Externa**: Los archivos de misiones se guardan en `.minecraft/config/questmobs/`, NO dentro del JAR.
- **Texturas Personalizadas**: El mod usa texturas propias en `assets/questnomas/textures/gui/` para la interfaz.

---

## 📌 4. Tareas Pendientes (To-Do)

Estas son las tareas que faltan por hacer en el proyecto:

1. **Persistencia del Editor**: Hacer que lo que diseñes en el editor se guarde en archivos JSON
2. **Mejorar Capas**: Mejorar el sistema de capas para que los elementos no se superpongan por error
3. **Internacionalización**: Preparar el mod para soportar traducciones (archivos .lang)

---

## ⚠️ 5. Notas para la Próxima Sesión

- El sistema de misiones agrupa las misiones por tipo de mob (ej: "minecraft:villager") en un HashMap
- Cada misión tiene un ciclo de diálogo completo: frases iniciales →recordatorio → error → agradecimiento
- El editor permite dibujar figuras, texto avanzado y usar pincel自由 para dibujar

---

*IA: Por favor, lee este archivo al inicio de cada sesión para entender el estado del proyecto antes de hacer cambios.*