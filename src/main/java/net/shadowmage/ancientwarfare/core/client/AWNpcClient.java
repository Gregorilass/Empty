package net.shadowmage.ancientwarfare.core.client;

import net.minecraft.client.renderer.entity.VillagerRenderer;
import net.minecraft.world.entity.npc.villager.Villager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.shadowmage.ancientwarfare.core.AncientWarfareCore;
import net.shadowmage.ancientwarfare.core.npc.AWNpcRegistry;

/** Use the vanilla Villager renderer until legacy animated NPC models are ported. */
@EventBusSubscriber(modid = AncientWarfareCore.MOD_ID, value = Dist.CLIENT)
public final class AWNpcClient {
    private AWNpcClient() {}

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.<Villager>registerEntityRenderer(AWNpcRegistry.WORKER.get(), VillagerRenderer::new);
        event.<Villager>registerEntityRenderer(AWNpcRegistry.SOLDIER.get(), VillagerRenderer::new);
        event.<Villager>registerEntityRenderer(AWNpcRegistry.COURIER.get(), VillagerRenderer::new);
    }
}
