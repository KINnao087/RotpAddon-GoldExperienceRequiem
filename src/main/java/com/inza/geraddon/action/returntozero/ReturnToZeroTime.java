package com.inza.geraddon.action.returntozero;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.stand.StandEntityAction;
import com.github.standobyte.jojo.capability.world.TimeStopHandler;
import com.github.standobyte.jojo.capability.world.WorldUtilCapProvider;
import com.github.standobyte.jojo.capability.world.TimeStopInstance;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.inza.geraddon.TimeRecoder.BytecodeRewindManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.List;
import java.util.stream.Collectors;

public class ReturnToZeroTime extends StandEntityAction {
    public ReturnToZeroTime(StandEntityAction.Builder builder) {
        super(builder);
    }

    public static long oldestTimeStoptick = -1;

    @Override
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, IStandPower userPower, ActionTarget target) {
        if (!BytecodeRewindManager.hasAvailableMark(user.level)) {
            return ActionConditionResult.NEGATIVE;
        }
        return super.checkSpecificConditions(user, userPower, target);
    }

    @Override
    public void standPerform(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        super.standPerform(world, standEntity, userPower, task);
        userPower.stopHeldAction(false);
        if (world.isClientSide() || !(world instanceof ServerWorld)) {
            return;
        }
        ServerWorld serverWorld = (ServerWorld) world;
        oldestTimeStoptick = BytecodeRewindManager.getActiveSessionOldestTick(serverWorld);
        LivingEntity user = userPower.getUser();
        BytecodeRewindManager.RewindResult result = BytecodeRewindManager.rewindActiveSession(serverWorld, user != null ? user.getUUID() : null);
        if (result.success) {
            oldestTimeStoptick = result.targetTick;
            serverWorld.getCapability(WorldUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                List<TimeStopInstance> instances = cap.getTimeStopHandler()
                        .getAllTimeStopInstances()
                        .collect(Collectors.toList());
                for (TimeStopInstance instance : instances) {
                    TimeStopHandler.resumeTime(serverWorld, instance);
                }
            });
        }
    }

}
