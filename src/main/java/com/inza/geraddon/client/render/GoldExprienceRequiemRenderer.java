package com.inza.geraddon.client.render;

import com.github.standobyte.jojo.client.render.entity.model.stand.StandEntityModel;
import com.github.standobyte.jojo.client.render.entity.model.stand.StandModelRegistry;
import com.github.standobyte.jojo.client.render.entity.renderer.stand.StandEntityRenderer;
import com.inza.geraddon.AddonMain;
import com.inza.geraddon.entity.GoldExprienceRequiemEntity;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

public class GoldExprienceRequiemRenderer extends StandEntityRenderer<GoldExprienceRequiemEntity, StandEntityModel<GoldExprienceRequiemEntity>> {
    
    public GoldExprienceRequiemRenderer(EntityRendererManager renderManager) {
        super(renderManager, 
                StandModelRegistry.registerModel(new ResourceLocation(AddonMain.MOD_ID, "example_stand"), GoldExprienceRequiemModel::new),
                new ResourceLocation(AddonMain.MOD_ID, "textures/entity/stand/example_stand.png"), 0);
    }
}
