package questgrupo.questmod.events;

import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import questgrupo.questmod.Config;
import questgrupo.questmod.client.GlobalGuiSettings;
import questgrupo.questmod.client.QuestScreen;
import questgrupo.questmod.network.Messages;
import questgrupo.questmod.network.PacketMisionCompletada;
import questgrupo.questmod.network.PacketMisionesSync;
import questgrupo.questmod.network.PacketSyncProgresoMuertes;

import java.util.*;

@Mod.EventBusSubscriber(modid = "questnomas")
public class ClickAldeano {

    private static final Set<String> misionesAceptadas = new HashSet<>();
    private static final Set<String> misionesFinalizadas = new HashSet<>();
    private static final Map<UUID, UUID> entidadesMirandoJugador = new HashMap<>();
    public static final Map<String, Integer> progresoMuertes = new HashMap<>();

    public static boolean esMisionAceptada(String key) { return misionesAceptadas.contains(key); }
    public static boolean esMisionFinalizada(String key) { return misionesFinalizadas.contains(key); }
    public static int getMisionesCompletadasCount() { return misionesFinalizadas.size(); }
    public static Set<String> getMisionesAceptadasCopia() { return new HashSet<>(misionesAceptadas); }
    
    public static void recibirMisionesSync(Set<String> missionsFromServer) {
        if (missionsFromServer != null) {
            misionesAceptadas.addAll(missionsFromServer);
            GlobalGuiSettings.PANELES.clear();
            questgrupo.questmod.Config.inyectarMisionesEnEditor();
        }
    }

    public static void registrarMirada(Entity entidad, Entity jugador) {
        entidadesMirandoJugador.put(entidad.getUUID(), jugador.getUUID());
    }

    public static void liberarMirada(UUID entidadUUID) {
        entidadesMirandoJugador.remove(entidadUUID);
    }

    @SubscribeEvent
    public static void onEntityTick(LivingEvent.LivingTickEvent event) {
        Entity entidad = event.getEntity();
        if (entidadesMirandoJugador.containsKey(entidad.getUUID())) {
            UUID playerUUID = entidadesMirandoJugador.get(entidad.getUUID());
            Entity jugador = entidad.level().getPlayerByUUID(playerUUID);

            if (jugador != null && jugador.isAlive()) {
                entidad.lookAt(EntityAnchorArgument.Anchor.EYES, jugador.getEyePosition());
                if (entidad instanceof net.minecraft.world.entity.LivingEntity living) {
                    living.yBodyRot = living.yHeadRot;
                    living.yBodyRotO = living.yHeadRot;
                    if (living instanceof net.minecraft.world.entity.Mob mob) {
                        if (mob.getNavigation() != null) mob.getNavigation().stop();
                        mob.setTarget(null);
                    }
                    living.zza = 0; living.xxa = 0;
                    Vec3 mov = entidad.getDeltaMovement();
                    entidad.setDeltaMovement(0, mov.y, 0);
                }
                if (entidad.distanceToSqr(jugador) > 49.0) entidadesMirandoJugador.remove(entidad.getUUID());
            } else {
                entidadesMirandoJugador.remove(entidad.getUUID());
            }
        }
    }

    @SubscribeEvent
    public static void alHacerClic(PlayerInteractEvent.EntityInteract event) {
        if (event.getHand() != event.getEntity().getUsedItemHand()) return;
        if (Config.misionesCargadas.isEmpty()) Config.cargarMisionesAhora();

        ResourceLocation idEntidad = ForgeRegistries.ENTITY_TYPES.getKey(event.getTarget().getType());
        if (idEntidad == null) return;
        String mobKey = idEntidad.toString();

        if (Config.misionesCargadas.containsKey(mobKey)) {
            Config.MisionData misionValida = obtenerMisionParaEntidad(event.getEntity(), event.getTarget(), Config.misionesCargadas.get(mobKey));

            if (misionValida != null) {
                event.setCanceled(true);
                event.setCancellationResult(net.minecraft.world.InteractionResult.SUCCESS);

                registrarMirada(event.getTarget(), event.getEntity());

                if (event.getLevel().isClientSide()) {
                    String nombreMob = event.getTarget().getDisplayName().getString();
                    String questKey = generarQuestKey(event.getEntity(), event.getTarget(), misionValida);
                    boolean yaAcepto = misionesAceptadas.contains(questKey);

                    net.minecraft.client.Minecraft.getInstance().setScreen(
                            new QuestScreen(misionValida, nombreMob, yaAcepto, event.getTarget().getUUID())
                    );
                }
            }
        }
    }

    public static void registrarAceptacion(ServerPlayer player, UUID targetUUID) {
        Entity target = player.serverLevel().getEntity(targetUUID);
        if (target != null) {
            ResourceLocation idEntidad = ForgeRegistries.ENTITY_TYPES.getKey(target.getType());
            List<Config.MisionData> lista = Config.misionesCargadas.get(idEntidad.toString());
            Config.MisionData misionActual = obtenerMisionParaEntidad(player, target, lista);

            if (misionActual != null) {
                String questKey = generarQuestKey(player, target, misionActual);
                if (!misionesFinalizadas.contains(questKey)) {
                    misionesAceptadas.add(questKey);
                    Messages.sendToPlayer(new PacketMisionesSync(new HashSet<>(misionesAceptadas)), player);
                    procesarEntregaMision(player, targetUUID);
                }
            }
        }
    }

    public static void procesarEntregaMision(ServerPlayer player, UUID targetUUID) {
        Entity entidad = player.serverLevel().getEntity(targetUUID);
        if (entidad == null) return;

        ResourceLocation idEntidad = ForgeRegistries.ENTITY_TYPES.getKey(entidad.getType());
        List<Config.MisionData> misiones = Config.misionesCargadas.get(idEntidad.toString());
        Config.MisionData misionFinal = obtenerMisionParaEntidad(player, entidad, misiones);

        if (misionFinal != null) {
            String questKey = generarQuestKey(player, entidad, misionFinal);

            // VERIFICAR TODOS LOS OBJETIVOS
            boolean tieneTodo = true;
            for (Config.Objetivo obj : misionFinal.objetivos) {
                if (obj.entidad != null && !obj.entidad.isEmpty()) {
                    String progressKey = player.getUUID().toString() + "_" + questKey + "_" + obj.entidad;
                    if (progresoMuertes.getOrDefault(progressKey, 0) < obj.cantidad) {
                        tieneTodo = false;
                        break;
                    }
                } else if (obj.itemReal != null) {
                    if (player.getInventory().countItem(obj.itemReal) < obj.cantidad) {
                        tieneTodo = false;
                        break;
                    }
                }
            }

            if (tieneTodo) {
                // QUITAR TODOS LOS ITEMS
                for (Config.Objetivo obj : misionFinal.objetivos) {
                    if (obj.itemReal != null) {
                        player.getInventory().clearOrCountMatchingItems(p -> p.getItem() == obj.itemReal, obj.cantidad, player.inventoryMenu.getCraftSlots());
                    }
                }

                // DAR TODAS LAS RECOMPENSAS
                for (Config.Recompensa rec : misionFinal.recompensas) {
                    player.addItem(new ItemStack(rec.itemReal, rec.cantidad));
                }

                misionesAceptadas.remove(questKey);
                misionesFinalizadas.add(questKey);

                // Limpiar progreso de muertes liberando memoria
                for (Config.Objetivo obj : misionFinal.objetivos) {
                    if (obj.entidad != null && !obj.entidad.isEmpty()) {
                        progresoMuertes.remove(player.getUUID().toString() + "_" + questKey + "_" + obj.entidad);
                    }
                }

                Messages.sendToPlayer(new PacketMisionCompletada(), player);
            }
        }
    }

    // EVENTO NUEVO PARA CONTAR LAS MUERTES
    @SubscribeEvent
    public static void onEntityDeath(net.minecraftforge.event.entity.living.LivingDeathEvent event) {
        if (event.getSource().getEntity() instanceof ServerPlayer player) {
            ResourceLocation idMuerto = ForgeRegistries.ENTITY_TYPES.getKey(event.getEntity().getType());
            if (idMuerto == null) return;
            String mobMatado = idMuerto.toString();

            for (String questKey : misionesAceptadas) {
                if (questKey.startsWith(player.getUUID().toString())) {
                    Config.MisionData mision = obtenerMisionPorQuestKey(questKey);
                    if (mision != null) {
                        for (Config.Objetivo obj : mision.objetivos) {
                            if (mobMatado.equals(obj.entidad)) {
                                String progressKey = player.getUUID().toString() + "_" + questKey + "_" + obj.entidad;
                                int muertes = progresoMuertes.getOrDefault(progressKey, 0) + 1;
                                progresoMuertes.put(progressKey, muertes);
                                Messages.sendToPlayer(new PacketSyncProgresoMuertes(progressKey, muertes), player);
                            }
                        }
                    }
                }
            }
        }
    }

    private static Config.MisionData obtenerMisionPorQuestKey(String questKey) {
        for(List<Config.MisionData> lista : Config.misionesCargadas.values()) {
            for(Config.MisionData m : lista) {
                if(questKey.endsWith("_" + m.nombre.replace(" ", "_"))) {
                    return m;
                }
            }
        }
        return null;
    }

    private static String generarQuestKey(Entity jugador, Entity entidad, Config.MisionData mision) {
        ResourceLocation res = ForgeRegistries.ENTITY_TYPES.getKey(entidad.getType());
        String mobId = (res != null) ? res.toString() : "unknown";
        String detalles = "";
        if (entidad instanceof Villager villager) {
            String prof = ForgeRegistries.VILLAGER_PROFESSIONS.getKey(villager.getVillagerData().getProfession()).getPath();
            String type = net.minecraft.core.registries.BuiltInRegistries.VILLAGER_TYPE.getKey(villager.getVillagerData().getType()).getPath();
            detalles = "_" + prof + "_" + type;
        }
        // CAMBIO: Usamos el nombre para el ID ya que itemPedido no existe
        String misionId = mision != null ? "_" + mision.nombre.replace(" ", "_") : "";
        return jugador.getUUID().toString() + "_" + mobId + detalles + misionId;
    }

    private static Config.MisionData obtenerMisionParaEntidad(Entity jugador, Entity entidad, List<Config.MisionData> misiones) {
        if (misiones == null) return null;
        for (Config.MisionData m : misiones) {
            String questKey = generarQuestKey(jugador, entidad, m);
            if (misionesFinalizadas.contains(questKey)) continue;
            if (entidad instanceof Villager villager) {
                String prof = ForgeRegistries.VILLAGER_PROFESSIONS.getKey(villager.getVillagerData().getProfession()).getPath();
                String type = net.minecraft.core.registries.BuiltInRegistries.VILLAGER_TYPE.getKey(villager.getVillagerData().getType()).getPath();
                boolean pOk = m.profession == null || m.profession.isEmpty() || m.profession.equals(prof);
                boolean tOk = m.type == null || m.type.isEmpty() || m.type.equals(type);
                if (pOk && tOk) return m;
            } else {
                return m;
            }
        }
        return null;
    }
}