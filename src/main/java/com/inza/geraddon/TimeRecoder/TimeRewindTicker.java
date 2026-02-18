package com.inza.geraddon.TimeRecoder;

import com.inza.geraddon.AddonMain;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AddonMain.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TimeRewindTicker {
    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.world.isClientSide() || !(event.world instanceof ServerWorld)) {
            return;
        }
        ServerWorld world = (ServerWorld) event.world;
        BytecodeRewindManager.cleanupOrphanGhosts(world);
        BytecodeRewindManager.recordEntityPositions(world);
        BytecodeRewindManager.tickRewind(world);
    }
}
