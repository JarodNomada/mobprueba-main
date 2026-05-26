package questgrupo.questmod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import questgrupo.questmod.events.ClickAldeano;
import java.util.UUID;
import java.util.function.Supplier;

public class PacketAceptarMision {
    private final UUID entityUUID;
    private final String missionName;

    public PacketAceptarMision(UUID entityUUID, String missionName) {
        this.entityUUID = entityUUID;
        this.missionName = missionName;
    }

    public PacketAceptarMision(FriendlyByteBuf buf) {
        this.entityUUID = buf.readUUID();
        this.missionName = buf.readUtf();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(entityUUID);
        buf.writeUtf(missionName);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                ClickAldeano.registrarAceptacion(player, entityUUID, missionName);
            }
        });
        return true;
    }
}
