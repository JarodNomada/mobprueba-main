package questgrupo.questmod.events;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.joml.Matrix4f;
import questgrupo.questmod.Config;

import java.util.List;

@Mod.EventBusSubscriber(modid = "questnomas", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class QuestGeometryEvents {

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        PoseStack poseStack = event.getPoseStack();
        Vec3 camera = mc.gameRenderer.getMainCamera().getPosition();

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity == mc.player || !entity.isAlive()) continue;

            ResourceLocation idEntidad = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
            if (idEntidad == null) continue;
            String mobKey = idEntidad.toString();

            if (Config.misionesCargadas.containsKey(mobKey)) {
                double distSq = mc.player.distanceToSqr(entity);

                // Distancia de 5 bloques (5x5 = 25)
                if (distSq <= 25.0) {
                    Config.MisionData mision = obtenerMisionParaRender(mc.player, entity, Config.misionesCargadas.get(mobKey));

                    if (mision != null) {
                        String questKey = generarKeyParaRender(mc.player, entity, mision);

                        // --- USO DE GETTERS AQUÍ ---
                        if (ClickAldeano.esMisionAceptada(questKey)) {
                            renderizarSignoInterrogacion(entity, poseStack, camera, mc);
                        } else {
                            renderizarBurbujaChat(entity, poseStack, camera, mc);
                        }
                    }
                }
            }
        }
    }

    private static void renderizarSignoInterrogacion(Entity entity, PoseStack poseStack, Vec3 camera, Minecraft mc) {
        if (!hayLineaDeVision(entity, mc)) return;

        poseStack.pushPose();
        configurarPosicionBase(entity, poseStack, camera, mc);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(519);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        Matrix4f matrix = poseStack.last().pose();

        int rY = 255, gY = 220, bY = 0, aY = 255; // Amarillo
        int rB = 0, gB = 0, bB = 0, aB = 255;     // Negro
        float p = 0.025f;
        float brd = p * 1.5f;

        buffer.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
        // Contorno
        drawRectTri(buffer, matrix, -p*3-brd, p*11+brd, p*6+brd*2, -p*3-brd*2, rB, gB, bB, aB);
        drawRectTri(buffer, matrix, p*1-brd, p*11+brd, p*2+brd*2, -p*7-brd*2, rB, gB, bB, aB);
        drawRectTri(buffer, matrix, -p*1-brd, p*6+brd, p*4+brd*2, -p*2-brd*2, rB, gB, bB, aB);
        drawRectTri(buffer, matrix, -p-brd, p*2+brd, p*2+brd*2, -p*2-brd*2, rB, gB, bB, aB);
        tesselator.end();

        poseStack.translate(0, 0, -0.01f);
        matrix = poseStack.last().pose();
        buffer.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
        // Relleno
        drawRectTri(buffer, matrix, -p*3, p*11, p*6, -p*3, rY, gY, bY, aY);
        drawRectTri(buffer, matrix, p*1, p*11, p*2, -p*7, rY, gY, bY, aY);
        drawRectTri(buffer, matrix, -p*1, p*6, p*4, -p*2, rY, gY, bY, aY);
        drawRectTri(buffer, matrix, -p, p*2, p*2, -p*2, rY, gY, bY, aY);
        tesselator.end();

        RenderSystem.depthFunc(515);
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        poseStack.popPose();
    }

    private static void renderizarBurbujaChat(Entity entity, PoseStack poseStack, Vec3 camera, Minecraft mc) {
        if (!hayLineaDeVision(entity, mc)) return;
        poseStack.pushPose();
        configurarPosicionBase(entity, poseStack, camera, mc);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(519);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        Matrix4f matrix = poseStack.last().pose();

        int rW = 255, gW = 255, bW = 255, aW = 255;
        int rB = 0,   gB = 0,   bB = 0,   aB = 255;
        int rG = 120, gG = 120, bG = 120, aG = 255;
        float p = 0.020f;
        float w = p * 20; float h = p * 14;
        float tail = p * 5; float brd = p * 1.5f;
        float r1 = p * 1; float r2 = p * 2.5f;

        buffer.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
        drawRectTri(buffer, matrix, -w/2 - brd + r2, h + brd, w + brd*2 - r2*2, -h - brd*2, rB, gB, bB, aB);
        drawRectTri(buffer, matrix, -w/2 - brd + r1, h + brd - r1, w + brd*2 - r1*2, -h - brd*2 + r1*2, rB, gB, bB, aB);
        drawRectTri(buffer, matrix, -w/2 - brd, h + brd - r2, w + brd*2, -h - brd*2 + r2*2, rB, gB, bB, aB);
        drawTri(buffer, matrix, -p*3 - brd, -brd, p*3 + brd, -brd, 0, -tail - brd, rB, gB, bB, aB);
        tesselator.end();

        poseStack.translate(0, 0, -0.01f);
        matrix = poseStack.last().pose();
        buffer.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
        drawRectTri(buffer, matrix, -w/2 + r2, h, w - r2*2, -h, rW, gW, bW, aW);
        drawRectTri(buffer, matrix, -w/2 + r1, h - r1, w - r1*2, -h + r1*2, rW, gW, bW, aW);
        drawRectTri(buffer, matrix, -w/2, h - r2, w, -h + r2*2, rW, gW, bW, aW);
        drawTri(buffer, matrix, -p*3, 0, p*3, 0, 0, -tail, rW, gW, bW, aW);
        tesselator.end();

        poseStack.translate(0, 0, -0.01f);
        matrix = poseStack.last().pose();
        buffer.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
        float dS = p * 2.2f; float gap = p * 5;
        drawRectTri(buffer, matrix, -gap, h/2 + dS/2, dS, -dS, rG, gG, bG, aG);
        drawRectTri(buffer, matrix, -dS/2, h/2 + dS/2, dS, -dS, rG, gG, bG, aG);
        drawRectTri(buffer, matrix, gap - dS, h/2 + dS/2, dS, -dS, rG, gG, bG, aG);
        tesselator.end();

        RenderSystem.depthFunc(515);
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        poseStack.popPose();
    }

    private static String generarKeyParaRender(Entity jugador, Entity entidad, Config.MisionData mision) {
        ResourceLocation res = ForgeRegistries.ENTITY_TYPES.getKey(entidad.getType());
        String mobId = (res != null) ? res.toString() : "unknown";
        String detalles = "";
        if (entidad instanceof Villager villager) {
            String prof = ForgeRegistries.VILLAGER_PROFESSIONS.getKey(villager.getVillagerData().getProfession()).getPath();
            String type = net.minecraft.core.registries.BuiltInRegistries.VILLAGER_TYPE.getKey(villager.getVillagerData().getType()).getPath();
            detalles = "_" + prof + "_" + type;
        }
        String misionId = mision != null ? "_" + mision.nombre.replace(" ", "_") : "";
        return jugador.getUUID().toString() + "_" + mobId + detalles + misionId;
    }

    private static Config.MisionData obtenerMisionParaRender(Entity jugador, Entity entidad, List<Config.MisionData> misiones) {
        if (misiones == null) return null;
        for (Config.MisionData m : misiones) {
            String key = generarKeyParaRender(jugador, entidad, m);
            // --- USO DE GETTER AQUÍ ---
            if (ClickAldeano.esMisionFinalizada(key)) continue;

            if (entidad instanceof Villager villager) {
                String prof = ForgeRegistries.VILLAGER_PROFESSIONS.getKey(villager.getVillagerData().getProfession()).getPath();
                String type = net.minecraft.core.registries.BuiltInRegistries.VILLAGER_TYPE.getKey(villager.getVillagerData().getType()).getPath();
                boolean pOk = m.profession == null || m.profession.isEmpty() || m.profession.contains(prof);
                boolean tOk = m.type == null || m.type.isEmpty() || m.type.contains(type);
                if (pOk && tOk) return m;
            } else {
                return m;
            }
        }
        return null;
    }

    private static boolean hayLineaDeVision(Entity entity, Minecraft mc) {
        Vec3 start = mc.player.getEyePosition(1.0F);
        Vec3 end = new Vec3(entity.getX(), entity.getY() + entity.getBbHeight() + 0.6, entity.getZ());
        BlockHitResult hit = mc.level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mc.player));
        return hit.getType() != HitResult.Type.BLOCK;
    }

    private static void configurarPosicionBase(Entity entity, PoseStack poseStack, Vec3 camera, Minecraft mc) {
        double x = entity.getX() - camera.x;
        double y = entity.getY() - camera.y + entity.getBbHeight() + 0.6;
        double z = entity.getZ() - camera.z;
        poseStack.translate(x, y, z);
        double offsetAnim = Math.sin(mc.level.getGameTime() * 0.1) * 0.04;
        poseStack.translate(0, offsetAnim, 0);
        poseStack.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());
        poseStack.mulPose(Axis.YP.rotationDegrees(180f));
    }

    private static void drawRectTri(BufferBuilder buffer, Matrix4f matrix, float x, float y, float w, float h, int r, int g, int b, int a) {
        buffer.vertex(matrix, x, y, 0).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x + w, y, 0).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x, y + h, 0).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x + w, y, 0).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x + w, y + h, 0).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x, y + h, 0).color(r, g, b, a).endVertex();
    }

    private static void drawTri(BufferBuilder buffer, Matrix4f matrix, float x1, float y1, float x2, float y2, float x3, float y3, int r, int g, int b, int a) {
        buffer.vertex(matrix, x1, y1, 0).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x2, y2, 0).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x3, y3, 0).color(r, g, b, a).endVertex();
    }
}