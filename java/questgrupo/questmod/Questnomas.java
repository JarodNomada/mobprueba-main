package questgrupo.questmod;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import questgrupo.questmod.network.Messages;

@Mod("questnomas")
public class Questnomas {

    public Questnomas() {
        // 1. Cargamos las misiones
        Config.cargarMisionesAhora();

        // 2. REGISTRAMOS LOS MENSAJES

        // 2. REGISTRAMOS LOS MENSAJES (Esto evita el crash al pulsar Aceptar)
        Messages.register();

        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        Config.cargarMisionesAhora();
    }
}