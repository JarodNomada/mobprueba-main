package questgrupo.questmod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import questgrupo.questmod.client.GlobalGuiSettings;
import java.util.function.Supplier;

public class PacketSyncProgresoMuertes {
    private final String key;
    private final int cantidad;

    public PacketSyncProgresoMuertes(String key, int cantidad) {
        this.key = key;
        this.cantidad = cantidad;
    }

    public PacketSyncProgresoMuertes(FriendlyByteBuf buf) {
        this.key = buf.readUtf();
        this.cantidad = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(key);
        buf.writeInt(cantidad);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            GlobalGuiSettings.progresoMuertesCliente.put(key, cantidad);
        });
        return true;
    }
}
