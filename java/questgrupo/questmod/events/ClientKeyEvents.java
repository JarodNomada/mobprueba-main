package questgrupo.questmod.events;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import questgrupo.questmod.client.GlobalGuiSettings;
import questgrupo.questmod.client.KeyInit;
import questgrupo.questmod.client.editor.EditorScreen;

@Mod.EventBusSubscriber(modid = "questnomas", value = Dist.CLIENT)
public class ClientKeyEvents {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Minecraft mc = Minecraft.getInstance();
            while (KeyInit.KEY_OPEN_EDITOR.consumeClick()) {
                GlobalGuiSettings.editorActivo = true;
                mc.setScreen(new EditorScreen(mc.screen));
            }
            while (KeyInit.KEY_OPEN_JOURNAL.consumeClick()) {
                GlobalGuiSettings.editorActivo = false;
                mc.setScreen(new EditorScreen(mc.screen));
            }
        }
    }
}