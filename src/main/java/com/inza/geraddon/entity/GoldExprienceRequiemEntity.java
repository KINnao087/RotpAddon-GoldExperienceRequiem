package com.inza.geraddon.entity;

import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityType;

import com.github.standobyte.jojo.entity.stand.StandRelativeOffset;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.world.World;

public class GoldExprienceRequiemEntity extends StandEntity {
    public GoldExprienceRequiemEntity(StandEntityType<GoldExprienceRequiemEntity> type, World world) {
        super(type, world);
    }

}
