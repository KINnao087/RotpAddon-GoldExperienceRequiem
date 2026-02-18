package com.inza.geraddon.action.returntozero;

import com.github.standobyte.jojo.action.stand.StandEntityAction;
import com.github.standobyte.jojo.capability.world.TimeStopHandler;
import com.github.standobyte.jojo.capability.world.TimeStopInstance;
import com.github.standobyte.jojo.capability.world.WorldUtilCapProvider;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.inza.geraddon.TimeRecoder.BytecodeRewindManager;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.entity.LivingEntity;

import java.util.List;
import java.util.stream.Collectors;

public class ReturnToZero extends StandEntityAction{
    public ReturnToZero(StandEntityAction.Builder builder) {
        super(builder);
    }

    @Override
    public void standPerform(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        super.standPerform(world, standEntity, userPower, task);
        if (world.isClientSide() || !(world instanceof ServerWorld)) {
            return;
        }

        LivingEntity user = userPower.getUser();
        BytecodeRewindManager.rewindToLatestMark((ServerWorld) world, user != null ? user.getUUID() : null);
    }
}
