package questgrupo.questmod.events;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import questgrupo.questmod.client.KeyInit;
import questgrupo.questmod.client.editor.EditorScreen;

@Mod.EventBusSubscriber(modid = "questnomas", value = Dist.CLIENT)
public class ClientKeyEvents {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        // Solo revisamos al final del tick del cliente para evitar doble ejecución
        if (event.phase == TickEvent.Phase.END) {
            // Mientras la tecla se haya presionado...
            while (KeyInit.QUEST_JOURNAL_KEY.consumeClick()) {
                Minecraft mc = Minecraft.getInstance();
                // Pasamos mc.screen al constructor para que el botón "Volver" funcione
                mc.setScreen(new EditorScreen(mc.screen));
            }
        }
    }
}