package questgrupo.questmod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.network.NetworkEvent;
import questgrupo.questmod.Config;
import questgrupo.questmod.client.GlobalGuiSettings;
import java.util.Set;
import java.util.HashSet;
import java.util.function.Supplier;

public class PacketMisionesSync {
    private final Set<String> misionesAceptadas;

    public PacketMisionesSync(Set<String> acceptedMissions) {
        this.misionesAceptadas = acceptedMissions;
    }

    public PacketMisionesSync(FriendlyByteBuf buf) {
        int size = buf.readInt();
        this.misionesAceptadas = new HashSet<>();
        for (int i = 0; i < size; i++) {
            this.misionesAceptadas.add(buf.readUtf());
        }
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(misionesAceptadas.size());
        for (String key : misionesAceptadas) {
            buf.writeUtf(key);
        }
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            GlobalGuiSettings.misionesAceptadasCliente.clear();
            GlobalGuiSettings.misionesAceptadasCliente.addAll(this.misionesAceptadas);
            Config.inyectarMisionesEnEditor();
        });
        return true;
    }
}