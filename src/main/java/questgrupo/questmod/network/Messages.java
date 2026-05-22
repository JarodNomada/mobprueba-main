package questgrupo.questmod.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class Messages {
    private static SimpleChannel INSTANCE;
    private static int packetId = 0;
    private static int id() { return packetId++; }

    public static void register() {
        SimpleChannel net = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation("questnomas", "messages"))
                .networkProtocolVersion(() -> "1.0")
                .clientAcceptedVersions(s -> true)
                .serverAcceptedVersions(s -> true)
                .simpleChannel();

        INSTANCE = net;

        // Paquete Cliente -> Servidor (Aceptar misión)
        net.messageBuilder(PacketAceptarMision.class, id())
                .decoder(PacketAceptarMision::new)
                .encoder(PacketAceptarMision::toBytes)
                .consumerMainThread(PacketAceptarMision::handle)
                .add();

// Paquete Servidor -> Cliente (Misión completada con éxito)
        net.messageBuilder(PacketMisionCompletada.class, id())
                .decoder(PacketMisionCompletada::new)
                .encoder(PacketMisionCompletada::toBytes)
                .consumerMainThread(PacketMisionCompletada::handle)
                .add();
        
        // Paquete Servidor -> Cliente (Sincronizar misiones aceptadas)
        net.messageBuilder(PacketMisionesSync.class, id())
                .decoder(PacketMisionesSync::new)
                .encoder(PacketMisionesSync::toBytes)
                .consumerMainThread(PacketMisionesSync::handle)
                .add();

        // Paquete Servidor -> Cliente (Sincronizar progreso de muertes)
        net.messageBuilder(PacketSyncProgresoMuertes.class, id())
                .decoder(PacketSyncProgresoMuertes::new)
                .encoder(PacketSyncProgresoMuertes::toBytes)
                .consumerMainThread(PacketSyncProgresoMuertes::handle)
                .add();
    }

    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }
}