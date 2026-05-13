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
    public static final KeyMapping KEY_OPEN_EDITOR = new KeyMapping(
            "key.questnomas.editor", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_O, "category.questnomas.general"
    );
    public static final KeyMapping KEY_OPEN_JOURNAL = new KeyMapping(
            "key.questnomas.journal", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_P, "category.questnomas.general"
    );

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(KEY_OPEN_EDITOR);
        event.register(KEY_OPEN_JOURNAL);
    }
}