package questgrupo.questmod.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import questgrupo.questmod.client.QuestScreen;

import java.util.function.Supplier;

public class PacketMisionCompletada {
    public PacketMisionCompletada() {}
    public PacketMisionCompletada(FriendlyByteBuf buf) {}
    public void toBytes(FriendlyByteBuf buf) {}

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            if (Minecraft.getInstance().screen instanceof QuestScreen screen) {
            }
        });
        return true;
    }
}