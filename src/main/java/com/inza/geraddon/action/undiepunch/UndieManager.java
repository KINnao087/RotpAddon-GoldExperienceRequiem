package com.inza.geraddon.action.undiepunch;

import com.inza.geraddon.AddonMain;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = AddonMain.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class UndieManager {
    public static final String ON_UNDIE = "onUndie";
    private static final String UNDIE_COUNT_KEY = "undieLoopCount";
    private static final String UNDIE_REKILL_DELAY_KEY = "undieRekillDelay";
    private static final int MAX_UNDIE_LOOPS = 10;
    private static final int REKILL_DELAY_TICKS = 12;
    private static final Map<UUID, Integer> PLAYER_UNDIE_COUNTS = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        LivingEntity entity = event.getEntityLiving();
        World world = event.getEntity().level;
        boolean isClient = world.isClientSide();
        int undieCount = getUndieCount(entity);
        boolean playerLooping = isPlayerLooping(entity);
        boolean canUndie = isClient || undieCount < MAX_UNDIE_LOOPS;

        if (!isClient && entity.isAlive()) {
            int rekillDelay = entity.getPersistentData().getInt(UNDIE_REKILL_DELAY_KEY);
            if (rekillDelay > 0) {
                rekillDelay--;
                entity.getPersistentData().putInt(UNDIE_REKILL_DELAY_KEY, rekillDelay);
                if (rekillDelay == 0) {
                    entity.invulnerableTime = 0;
                    entity.hurt(DamageSource.OUT_OF_WORLD, Float.MAX_VALUE);
                }
            }
        }

        if ((entity.deathTime == 15 && (entity.getTags().contains(UndiePunch.TAG) || playerLooping) && canUndie)
                || entity.getTags().contains(ON_UNDIE)) {
            if (!entity.getTags().contains(ON_UNDIE)) {
                entity.addTag(ON_UNDIE);
            }

            entity.deathTime = Math.max(entity.deathTime - 2, 0);
            if (!isClient && entity.deathTime <= 0 && entity.getHealth() <= 0.0F) {
                entity.setHealth(1F);
                undieCount++;
                setUndieCount(entity, undieCount);
                entity.removeTag(ON_UNDIE);
                if (undieCount >= MAX_UNDIE_LOOPS) {
                    entity.removeTag(UndiePunch.TAG);
                    clearUndieCount(entity);
                    entity.getPersistentData().remove(UNDIE_REKILL_DELAY_KEY);
                }
                else {
                    entity.getPersistentData().putInt(UNDIE_REKILL_DELAY_KEY, REKILL_DELAY_TICKS);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        PlayerEntity player = event.getPlayer();
        UUID id = player.getUUID();
        Integer count = PLAYER_UNDIE_COUNTS.get(id);
        if (count != null && count < MAX_UNDIE_LOOPS) {
            player.getPersistentData().putInt(UNDIE_REKILL_DELAY_KEY, REKILL_DELAY_TICKS);
        }
    }

    private static int getUndieCount(LivingEntity entity) {
        if (entity instanceof PlayerEntity) {
            return PLAYER_UNDIE_COUNTS.getOrDefault(entity.getUUID(), 0);
        }
        return entity.getPersistentData().getInt(UNDIE_COUNT_KEY);
    }

    private static void setUndieCount(LivingEntity entity, int count) {
        if (entity instanceof PlayerEntity) {
            PLAYER_UNDIE_COUNTS.put(entity.getUUID(), count);
        }
        else {
            entity.getPersistentData().putInt(UNDIE_COUNT_KEY, count);
        }
    }

    private static void clearUndieCount(LivingEntity entity) {
        if (entity instanceof PlayerEntity) {
            PLAYER_UNDIE_COUNTS.remove(entity.getUUID());
        }
        else {
            entity.getPersistentData().remove(UNDIE_COUNT_KEY);
        }
    }

    private static boolean isPlayerLooping(LivingEntity entity) {
        if (!(entity instanceof PlayerEntity)) {return false;}

        Integer count = PLAYER_UNDIE_COUNTS.get(entity.getUUID());
        return count != null && count < MAX_UNDIE_LOOPS;
    }
}
