package questgrupo.questmod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import questgrupo.questmod.events.ClickAldeano;
import java.util.Set;
import java.util.HashSet;
import java.util.function.Supplier;

public class PacketMisionesSync {
    private final Set<String> misionesAceptadas;
    private final Set<String> misionesFinalizadas;

    public PacketMisionesSync(Set<String> acceptedMissions, Set<String> finalizedMissions) {
        this.misionesAceptadas = acceptedMissions;
        this.misionesFinalizadas = finalizedMissions;
    }

    public PacketMisionesSync(FriendlyByteBuf buf) {
        int size = buf.readInt();
        this.misionesAceptadas = new HashSet<>();
        for (int i = 0; i < size; i++) {
            this.misionesAceptadas.add(buf.readUtf());
        }
        size = buf.readInt();
        this.misionesFinalizadas = new HashSet<>();
        for (int i = 0; i < size; i++) {
            this.misionesFinalizadas.add(buf.readUtf());
        }
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(misionesAceptadas.size());
        for (String key : misionesAceptadas) {
            buf.writeUtf(key);
        }
        buf.writeInt(misionesFinalizadas.size());
        for (String key : misionesFinalizadas) {
            buf.writeUtf(key);
        }
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ClickAldeano.recibirMisionesSync(this.misionesAceptadas, this.misionesFinalizadas);
        });
        return true;
    }
}