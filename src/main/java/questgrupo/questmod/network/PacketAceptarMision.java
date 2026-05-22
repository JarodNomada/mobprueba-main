package questgrupo.questmod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import questgrupo.questmod.events.ClickAldeano;
import java.util.UUID;
import java.util.function.Supplier;

public class PacketAceptarMision {
    private final UUID entityUUID;

    public PacketAceptarMision(UUID entityUUID) {
        this.entityUUID = entityUUID;
    }

    public PacketAceptarMision(FriendlyByteBuf buf) {
        this.entityUUID = buf.readUUID();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(entityUUID);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                // Primero registra que se aceptó (si no estaba aceptada)
                ClickAldeano.registrarAceptacion(player, entityUUID);
                // Luego procesa la entrega (si tiene los items)
                ClickAldeano.procesarEntregaMision(player, entityUUID);
            }
        });
        return true;
    }
}