package questgrupo.questmod.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = "questnomas", bus = Mod.EventBusSubscriber.Bus.MOD)
public class KeyInit {
    // Usaremos GLFW_KEY_TAB. Si prefieres otra, cambia TAB por J, por ejemplo.
    public static final KeyMapping QUEST_JOURNAL_KEY = new KeyMapping(
            "key.questnomas.journal",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_TAB,
            "category.questnomas.general"
    );

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(QUEST_JOURNAL_KEY);
    }
}