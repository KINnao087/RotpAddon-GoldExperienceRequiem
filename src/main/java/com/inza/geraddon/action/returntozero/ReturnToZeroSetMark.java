package com.inza.geraddon.action.returntozero;

import com.github.standobyte.jojo.action.stand.StandEntityAction;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.inza.geraddon.TimeRecoder.BytecodeRewindManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class ReturnToZeroSetMark extends StandEntityAction{
    private static final int MARK_RADIUS = 8;

    public ReturnToZeroSetMark(StandEntityAction.Builder builder) {
        super(builder);
    }

    private int latestTick = -1;

//    @Override
//    protected ActionConditionResult checkSpecificConditions(LivingEntity user, IStandPower userPower, ActionTarget target) {
//        if (latestTick == -1) {
//            return ActionConditionResult.NEGATIVE;
//        }
//        return super.checkSpecificConditions(user, userPower, target);
//    }

    @Override
    public void standPerform(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        super.standPerform(world, standEntity, userPower, task);
        if (world.isClientSide() || !(world instanceof ServerWorld)) {
            return;
        }

        LivingEntity user = userPower.getUser();
        if (user != null) {
            BytecodeRewindManager.Mark mark = BytecodeRewindManager.mark((ServerWorld) world, user.blockPosition(), MARK_RADIUS);
        }

        standEntity.stopTask();
    }
}
