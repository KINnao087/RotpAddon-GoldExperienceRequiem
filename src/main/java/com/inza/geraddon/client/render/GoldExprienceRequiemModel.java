package com.inza.geraddon.client.render;

import com.github.standobyte.jojo.action.stand.StandEntityAction;
import com.github.standobyte.jojo.client.render.entity.model.stand.HumanoidStandModel;
import com.github.standobyte.jojo.client.render.entity.pose.IModelPose;
import com.github.standobyte.jojo.client.render.entity.pose.ModelPose;
import com.github.standobyte.jojo.client.render.entity.pose.RotationAngle;
import com.github.standobyte.jojo.client.render.entity.pose.XRotationModelRenderer;
import com.github.standobyte.jojo.client.render.entity.pose.anim.PosedActionAnimation;
import com.github.standobyte.jojo.entity.stand.StandPose;
import com.inza.geraddon.entity.GoldExprienceRequiemEntity;

import net.minecraft.client.renderer.model.ModelRenderer;

public class GoldExprienceRequiemModel extends HumanoidStandModel<GoldExprienceRequiemEntity> {
    private final ModelRenderer headThing_r1;
    private final ModelRenderer headThing_r2;
    private final ModelRenderer headThing_r3;
    private final ModelRenderer headThing_r4;
    private final ModelRenderer headThing_r5;
    private final ModelRenderer headThing_r6;
    private final ModelRenderer headThing_r7;
    private final ModelRenderer headThing_r8;
    private final ModelRenderer headThing_r9;
    private final ModelRenderer headThing_r10;
    private final ModelRenderer headThing_r11;
    private final ModelRenderer headThing_r12;
    private final ModelRenderer headArrow;
    private final ModelRenderer headArrow_r1;
    private final ModelRenderer headArrow_r2;
    private final ModelRenderer headArrow_r3;
    private final ModelRenderer headArrow_r4;
    private final ModelRenderer headArrow_r5;
    private final ModelRenderer headArrow_r6;
    private final ModelRenderer torso_r1;
    private final ModelRenderer torso_r2;
    private final ModelRenderer torso_r3;
    private final ModelRenderer torso_r4;
    private final ModelRenderer torso_r5;
    private final ModelRenderer torso_r6;
    private final ModelRenderer torso_r7;
    private final ModelRenderer torso_r8;
    private final ModelRenderer torso_r9;
    private final ModelRenderer torso_r10;
    private final ModelRenderer centroid;
    private final ModelRenderer centroid_r1;
    private final ModelRenderer centroid_r2;
    private final ModelRenderer centroid_r3;
    private final ModelRenderer centroid_r4;
    private final ModelRenderer centroid_r5;
    private final ModelRenderer centroid_r6;
    private final ModelRenderer centroid_r7;
    private final ModelRenderer centroid_r8;
    private final ModelRenderer hatLike;
    private final ModelRenderer leftShoulder;
    private final ModelRenderer rightShoulder;
    private final ModelRenderer rightArmJoint2;

	public GoldExprienceRequiemModel() {
//		super();
        root = new ModelRenderer(this);
        root.setPos(0.0F, -29.0F, 0.0F);


        head = new ModelRenderer(this);
        head.setPos(0.0F, 0.0F, 0.0F);
        root.addChild(head);
        head.texOffs(0, 0).addBox(-7.4F, -15.0F, -7.6F, 15.0F, 15.0F, 15.0F, 0.0F, false);

        headThing_r1 = new ModelRenderer(this);
        headThing_r1.setPos(0.95F, -19.225F, -4.75F);
        head.addChild(headThing_r1);
        setRotationAngle(headThing_r1, -0.6545F, 0.0F, 0.0F);
        headThing_r1.texOffs(3, 2).addBox(-1.9F, -4.0F, -1.9F, 2.0F, 4.0F, 0.0F, 0.0F, true);
        headThing_r1.texOffs(2, 1).addBox(-2.0F, -4.0F, -1.9F, 2.0F, 4.0F, 1.0F, 0.0F, false);

        headThing_r2 = new ModelRenderer(this);
        headThing_r2.setPos(-0.95F, -14.475F, -5.7F);
        head.addChild(headThing_r2);
        setRotationAngle(headThing_r2, -0.2618F, 0.0F, 0.0F);
        headThing_r2.texOffs(1, 1).addBox(0.0F, -6.0F, -1.9F, 4.0F, 6.0F, 2.0F, 0.0F, true);
        headThing_r2.texOffs(1, 1).addBox(-2.1F, -6.0F, -1.9F, 4.0F, 6.0F, 2.0F, 0.0F, false);

        headThing_r3 = new ModelRenderer(this);
        headThing_r3.setPos(0.95F, -14.475F, -6.2F);
        head.addChild(headThing_r3);
        setRotationAngle(headThing_r3, -0.2618F, 0.0F, 0.0F);
        headThing_r3.texOffs(1, 1).addBox(-3.8F, -6.0868F, -1.4076F, 4.0F, 6.0F, 2.0F, 0.0F, false);
        headThing_r3.texOffs(1, 1).addBox(-2.1F, -6.0868F, -1.4076F, 4.0F, 6.0F, 2.0F, 0.0F, true);

        headThing_r4 = new ModelRenderer(this);
        headThing_r4.setPos(-6.625F, -17.1F, 2.5F);
        head.addChild(headThing_r4);
        setRotationAngle(headThing_r4, -0.7572F, 0.3775F, 0.3719F);
        headThing_r4.texOffs(2, 1).addBox(-1.9F, -4.0F, 1.9F, 1.0F, 3.0F, 2.0F, 0.0F, false);

        headThing_r5 = new ModelRenderer(this);
        headThing_r5.setPos(6.125F, -18.1F, 2.5F);
        head.addChild(headThing_r5);
        setRotationAngle(headThing_r5, -0.7572F, -0.3775F, -0.3719F);
        headThing_r5.texOffs(1, 1).addBox(1.0F, -6.0F, 1.9F, 1.0F, 6.0F, 2.0F, 0.0F, true);

        headThing_r6 = new ModelRenderer(this);
        headThing_r6.setPos(5.45F, -14.25F, -5.7F);
        head.addChild(headThing_r6);
        setRotationAngle(headThing_r6, -0.4363F, 0.0F, 0.0F);
        headThing_r6.texOffs(-1, -1).addBox(0.0F, -4.0F, -1.9F, 2.0F, 4.0F, 6.0F, 0.0F, true);

        headThing_r7 = new ModelRenderer(this);
        headThing_r7.setPos(-6.125F, -13.775F, 0.125F);
        head.addChild(headThing_r7);
        setRotationAngle(headThing_r7, -0.829F, 0.0F, 0.0F);
        headThing_r7.texOffs(0, 0).addBox(-2.0F, -6.0F, 0.0F, 2.0F, 6.0F, 4.0F, 0.0F, false);
        headThing_r7.texOffs(0, 0).addBox(12.15F, -6.0F, 0.0F, 2.0F, 6.0F, 4.0F, 0.0F, true);

        headThing_r8 = new ModelRenderer(this);
        headThing_r8.setPos(-6.6F, -11.875F, -3.675F);
        head.addChild(headThing_r8);
        setRotationAngle(headThing_r8, -0.829F, 0.0F, 0.0F);
        headThing_r8.texOffs(-1, -1).addBox(-2.0F, -6.0F, 0.0F, 2.0F, 6.0F, 6.0F, 0.0F, false);
        headThing_r8.texOffs(-1, -1).addBox(13.1F, -6.0F, 0.0F, 2.0F, 6.0F, 6.0F, 0.0F, true);

        headThing_r9 = new ModelRenderer(this);
        headThing_r9.setPos(-5.45F, -17.575F, -6.425F);
        head.addChild(headThing_r9);
        setRotationAngle(headThing_r9, -0.829F, 0.0F, 0.0F);
        headThing_r9.texOffs(0, 0).addBox(-2.0F, -6.0F, 0.0F, 1.0F, 6.0F, 4.0F, 0.0F, false);
        headThing_r9.texOffs(1, 0).addBox(12.05F, -6.0F, 0.0F, 1.0F, 6.0F, 4.0F, 0.0F, true);

        headThing_r10 = new ModelRenderer(this);
        headThing_r10.setPos(-5.45F, -14.25F, -5.95F);
        head.addChild(headThing_r10);
        setRotationAngle(headThing_r10, -0.4363F, 0.0F, 0.0F);
        headThing_r10.texOffs(-1, -1).addBox(-2.0F, -4.0F, -1.9F, 2.0F, 4.0F, 6.0F, 0.0F, false);
        headThing_r10.texOffs(-1, -1).addBox(11.05F, -4.0F, -1.9F, 2.0F, 4.0F, 6.0F, 0.0F, true);

        headThing_r11 = new ModelRenderer(this);
        headThing_r11.setPos(6.625F, -17.1F, 2.5F);
        head.addChild(headThing_r11);
        setRotationAngle(headThing_r11, -0.7572F, -0.3775F, -0.3719F);
        headThing_r11.texOffs(2, 1).addBox(0.9F, -4.0F, 1.9F, 1.0F, 3.0F, 2.0F, 0.0F, true);

        headThing_r12 = new ModelRenderer(this);
        headThing_r12.setPos(-6.125F, -18.1F, 2.5F);
        head.addChild(headThing_r12);
        setRotationAngle(headThing_r12, -0.7572F, 0.3775F, 0.3719F);
        headThing_r12.texOffs(1, 1).addBox(-2.0F, -6.0F, 1.9F, 1.0F, 6.0F, 2.0F, 0.0F, false);

        headArrow = new ModelRenderer(this);
        headArrow.setPos(0.0F, -1.0F, 0.0F);
        head.addChild(headArrow);


        headArrow_r1 = new ModelRenderer(this);
        headArrow_r1.setPos(0.2825F, -10.0F, -7.0F);
        headArrow.addChild(headArrow_r1);
        setRotationAngle(headArrow_r1, 0.0F, 0.0F, -1.5708F);
        headArrow_r1.texOffs(0, 0).addBox(-3.5867F, -0.7825F, -1.0F, 5.0F, 1.0F, 2.0F, 0.0F, true);

        headArrow_r2 = new ModelRenderer(this);
        headArrow_r2.setPos(-0.5F, -11.0F, -7.0F);
        headArrow.addChild(headArrow_r2);
        setRotationAngle(headArrow_r2, 0.0F, 0.0F, -0.7854F);
        headArrow_r2.texOffs(0, 0).addBox(-2.5867F, -0.7825F, -1.0F, 4.0F, 2.0F, 2.0F, 0.0F, true);

        headArrow_r3 = new ModelRenderer(this);
        headArrow_r3.setPos(-1.5F, -10.25F, -7.0F);
        headArrow.addChild(headArrow_r3);
        setRotationAngle(headArrow_r3, 0.0F, 0.0F, -0.9599F);
        headArrow_r3.texOffs(2, 0).addBox(-2.5867F, -0.7825F, -1.0F, 2.0F, 2.0F, 2.0F, 0.0F, true);

        headArrow_r4 = new ModelRenderer(this);
        headArrow_r4.setPos(-0.2825F, -10.0F, -7.0F);
        headArrow.addChild(headArrow_r4);
        setRotationAngle(headArrow_r4, 0.0F, 0.0F, 1.5708F);
        headArrow_r4.texOffs(0, 0).addBox(-1.4133F, -0.7825F, -1.0F, 5.0F, 1.0F, 2.0F, 0.0F, false);

        headArrow_r5 = new ModelRenderer(this);
        headArrow_r5.setPos(1.5F, -10.25F, -7.0F);
        headArrow.addChild(headArrow_r5);
        setRotationAngle(headArrow_r5, 0.0F, 0.0F, 0.9599F);
        headArrow_r5.texOffs(2, 0).addBox(0.5867F, -0.7825F, -1.0F, 2.0F, 2.0F, 2.0F, 0.0F, false);

        headArrow_r6 = new ModelRenderer(this);
        headArrow_r6.setPos(0.5F, -11.0F, -7.0F);
        headArrow.addChild(headArrow_r6);
        setRotationAngle(headArrow_r6, 0.0F, 0.0F, 0.7854F);
        headArrow_r6.texOffs(0, 0).addBox(-1.4133F, -0.7825F, -1.0F, 4.0F, 2.0F, 2.0F, 0.0F, false);

        body = new ModelRenderer(this);
        body.setPos(0.0F, 0.0F, 0.0F);
        root.addChild(body);


        upperPart = new ModelRenderer(this);
        upperPart.setPos(0.0F, 24.0F, 0.0F);
        body.addChild(upperPart);


        torso = new ModelRenderer(this);
        torso.setPos(0.0F, -24.0F, 0.0F);
        upperPart.addChild(torso);
        torso.texOffs(-2, 63).addBox(-10.0F, 0.0F, -5.5F, 20.0F, 10.0F, 11.0F, 0.0F, false);
        torso.texOffs(1, 64).addBox(-7.0F, 0.0F, -4.0F, 14.0F, 24.0F, 8.0F, 0.0F, false);

        torso_r1 = new ModelRenderer(this);
        torso_r1.setPos(-2.0F, 22.0F, -4.5F);
        torso.addChild(torso_r1);
        setRotationAngle(torso_r1, 0.0F, 0.0F, 0.0873F);
        torso_r1.texOffs(4, 67).addBox(-1.9128F, -6.0019F, 0.0F, 4.0F, 8.0F, 2.0F, 0.0F, false);

        torso_r2 = new ModelRenderer(this);
        torso_r2.setPos(-2.0F, 25.0F, 1.0F);
        torso.addChild(torso_r2);
        setRotationAngle(torso_r2, 0.0F, 0.0F, 0.1309F);
        torso_r2.texOffs(0, 63).addBox(-6.0F, -3.0F, -6.0F, 8.0F, 3.0F, 10.0F, 0.0F, false);

        torso_r3 = new ModelRenderer(this);
        torso_r3.setPos(1.5F, 17.0F, -4.5F);
        torso.addChild(torso_r3);
        setRotationAngle(torso_r3, 0.0F, 0.0F, 0.0436F);
        torso_r3.texOffs(4, 67).addBox(-2.0872F, -6.0019F, 0.0F, 4.0F, 8.0F, 2.0F, 0.0F, true);

        torso_r4 = new ModelRenderer(this);
        torso_r4.setPos(2.0F, 22.0F, -4.5F);
        torso.addChild(torso_r4);
        setRotationAngle(torso_r4, 0.0F, 0.0F, -0.0873F);
        torso_r4.texOffs(4, 67).addBox(-2.0872F, -6.0019F, 0.0F, 4.0F, 8.0F, 2.0F, 0.0F, true);

        torso_r5 = new ModelRenderer(this);
        torso_r5.setPos(2.0F, 25.0F, 1.0F);
        torso.addChild(torso_r5);
        setRotationAngle(torso_r5, 0.0F, 0.0F, -0.1309F);
        torso_r5.texOffs(0, 63).addBox(-2.0F, -3.0F, -6.0F, 8.0F, 3.0F, 10.0F, 0.0F, true);

        torso_r6 = new ModelRenderer(this);
        torso_r6.setPos(-3.0F, 35.5F, 1.0F);
        torso.addChild(torso_r6);
        setRotationAngle(torso_r6, 0.0F, 0.0F, -0.0873F);
        torso_r6.texOffs(0, 63).addBox(-2.0F, -26.0F, -6.0F, 8.0F, 2.0F, 10.0F, 0.0F, true);

        torso_r7 = new ModelRenderer(this);
        torso_r7.setPos(30.0F, 42.0F, 1.0F);
        torso.addChild(torso_r7);
        setRotationAngle(torso_r7, 0.0F, 0.0F, -0.5236F);
        torso_r7.texOffs(6, 65).addBox(-8.0F, -42.0F, -4.0F, 6.0F, 4.0F, 6.0F, 0.0F, true);

        torso_r8 = new ModelRenderer(this);
        torso_r8.setPos(3.0F, 35.5F, 1.0F);
        torso.addChild(torso_r8);
        setRotationAngle(torso_r8, 0.0F, 0.0F, 0.0873F);
        torso_r8.texOffs(0, 63).addBox(-6.0F, -26.0F, -6.0F, 8.0F, 2.0F, 10.0F, 0.0F, false);

        torso_r9 = new ModelRenderer(this);
        torso_r9.setPos(-1.5F, 17.0F, -4.5F);
        torso.addChild(torso_r9);
        setRotationAngle(torso_r9, 0.0F, 0.0F, -0.0436F);
        torso_r9.texOffs(4, 67).addBox(-1.9128F, -6.0019F, 0.0F, 4.0F, 8.0F, 2.0F, 0.0F, false);

        torso_r10 = new ModelRenderer(this);
        torso_r10.setPos(-30.0F, 42.0F, 1.0F);
        torso.addChild(torso_r10);
        setRotationAngle(torso_r10, 0.0F, 0.0F, 0.5236F);
        torso_r10.texOffs(6, 65).addBox(2.0F, -42.0F, -4.0F, 6.0F, 4.0F, 6.0F, 0.0F, false);

        centroid = new ModelRenderer(this);
        centroid.setPos(0.0F, 0.0F, 1.5F);
        torso.addChild(centroid);


        centroid_r1 = new ModelRenderer(this);
        centroid_r1.setPos(-0.5F, 7.25F, 0.0F);
        centroid.addChild(centroid_r1);
        setRotationAngle(centroid_r1, 0.0F, 0.0F, -0.7854F);
        centroid_r1.texOffs(9, 66).addBox(-4.0F, 1.0F, -8.0F, 2.0F, 1.0F, 3.0F, 0.0F, true);

        centroid_r2 = new ModelRenderer(this);
        centroid_r2.setPos(-2.25F, 7.5F, 0.0F);
        centroid.addChild(centroid_r2);
        setRotationAngle(centroid_r2, 0.0F, 0.0F, -0.7854F);
        centroid_r2.texOffs(10, 66).addBox(-4.0F, -1.0F, -8.0F, 1.0F, 3.0F, 3.0F, 0.0F, true);

        centroid_r3 = new ModelRenderer(this);
        centroid_r3.setPos(3.5F, 4.5F, 0.0F);
        centroid.addChild(centroid_r3);
        setRotationAngle(centroid_r3, 0.0F, 0.0F, 0.7854F);
        centroid_r3.texOffs(6, 66).addBox(-1.0F, 1.0F, -8.0F, 5.0F, 1.0F, 3.0F, 0.0F, false);

        centroid_r4 = new ModelRenderer(this);
        centroid_r4.setPos(0.5F, 7.25F, 0.0F);
        centroid.addChild(centroid_r4);
        setRotationAngle(centroid_r4, 0.0F, 0.0F, 0.7854F);
        centroid_r4.texOffs(9, 66).addBox(2.0F, 1.0F, -8.0F, 2.0F, 1.0F, 3.0F, 0.0F, false);

        centroid_r5 = new ModelRenderer(this);
        centroid_r5.setPos(2.25F, 7.5F, 0.0F);
        centroid.addChild(centroid_r5);
        setRotationAngle(centroid_r5, 0.0F, 0.0F, 0.7854F);
        centroid_r5.texOffs(10, 66).addBox(3.0F, -1.0F, -8.0F, 1.0F, 3.0F, 3.0F, 0.0F, false);

        centroid_r6 = new ModelRenderer(this);
        centroid_r6.setPos(1.5F, 7.5F, 0.0F);
        centroid.addChild(centroid_r6);
        setRotationAngle(centroid_r6, 0.0F, 0.0F, 0.7854F);
        centroid_r6.texOffs(3, 66).addBox(-4.0F, -2.0F, -8.0F, 8.0F, 4.0F, 3.0F, 0.0F, false);

        centroid_r7 = new ModelRenderer(this);
        centroid_r7.setPos(-3.5F, 4.5F, 0.0F);
        centroid.addChild(centroid_r7);
        setRotationAngle(centroid_r7, 0.0F, 0.0F, -0.7854F);
        centroid_r7.texOffs(6, 66).addBox(-4.0F, 1.0F, -8.0F, 5.0F, 1.0F, 3.0F, 0.0F, true);

        centroid_r8 = new ModelRenderer(this);
        centroid_r8.setPos(-1.5F, 7.5F, 0.0F);
        centroid.addChild(centroid_r8);
        setRotationAngle(centroid_r8, 0.0F, 0.0F, -0.7854F);
        centroid_r8.texOffs(3, 66).addBox(-4.0F, -2.0F, -8.0F, 8.0F, 4.0F, 3.0F, 0.0F, true);

        hatLike = new ModelRenderer(this);
        hatLike.setPos(0.0F, 0.0F, 0.0F);
        torso.addChild(hatLike);
        hatLike.texOffs(-7, 0).addBox(-7.0F, -5.0F, 8.0F, 7.0F, 1.0F, 2.0F, 0.0F, false);
        hatLike.texOffs(-17, -8).addBox(0.0F, 0.0F, 0.0F, 11.0F, 2.0F, 10.0F, 0.0F, true);
        hatLike.texOffs(-6, -6).addBox(9.0F, -3.0F, 0.0F, 2.0F, 5.0F, 8.0F, 0.0F, true);
        hatLike.texOffs(-6, -6).addBox(9.0F, -1.0F, -5.75F, 2.0F, 2.0F, 8.0F, 0.0F, true);
        hatLike.texOffs(-7, 0).addBox(0.0F, -4.0F, 8.0F, 11.0F, 4.0F, 2.0F, 0.0F, true);
        hatLike.texOffs(-7, 0).addBox(0.0F, -5.0F, 8.0F, 7.0F, 1.0F, 2.0F, 0.0F, true);
        hatLike.texOffs(-17, -8).addBox(-11.0F, 0.0F, 0.0F, 11.0F, 2.0F, 10.0F, 0.0F, false);
        hatLike.texOffs(-6, -6).addBox(-11.0F, -1.0F, -5.75F, 2.0F, 2.0F, 8.0F, 0.0F, false);
        hatLike.texOffs(-7, 0).addBox(-11.0F, -4.0F, 8.0F, 11.0F, 4.0F, 2.0F, 0.0F, false);
        hatLike.texOffs(-6, -6).addBox(-11.0F, -3.0F, 0.0F, 2.0F, 5.0F, 8.0F, 0.0F, false);

        leftArm = new XRotationModelRenderer(this);
        leftArm.setPos(12.0F, -20.0F, 0.0F);
        upperPart.addChild(leftArm);
        leftArm.texOffs(32, 108).addBox(-4.0F, -4.0F, -4.0F, 7.0F, 12.0F, 8.0F, 0.0F, false);

        leftArmJoint = new ModelRenderer(this);
        leftArmJoint.setPos(0.0F, 7.5F, -0.5F);
        leftArm.addChild(leftArmJoint);
        leftArmJoint.texOffs(32, 102).addBox(-3.125F, -1.125F, -0.875F, 5.0F, 5.0F, 6.0F, -0.125F, true);

        leftForeArm = new ModelRenderer(this);
        leftForeArm.setPos(0.0F, 8.0F, 0.0F);
        leftArm.addChild(leftForeArm);
        leftForeArm.texOffs(32, 118).addBox(-4.001F, -0.001F, -3.999F, 7.0F, 12.0F, 8.0F, -0.001F, false);
        leftForeArm.texOffs(32, 102).addBox(-4.125F, -3.125F, -2.875F, 5.0F, 6.0F, 6.0F, -0.125F, true);
        leftForeArm.texOffs(32, 102).addBox(-2.875F, -3.125F, -2.875F, 4.0F, 6.0F, 6.0F, -0.125F, true);
        leftForeArm.texOffs(-2, -2).addBox(2.5F, 7.0F, -2.0F, 1.0F, 4.0F, 4.0F, 0.0F, false);

        leftShoulder = new ModelRenderer(this);
        leftShoulder.setPos(0.0F, 0.0F, 0.0F);
        leftArm.addChild(leftShoulder);
        leftShoulder.texOffs(-14, -8).addBox(-3.0F, -5.0F, -5.0F, 6.0F, 8.0F, 10.0F, 0.0F, true);
        leftShoulder.texOffs(-11, -6).addBox(-2.0F, -6.0F, -4.0F, 5.0F, 1.0F, 8.0F, 0.0F, true);
        leftShoulder.texOffs(-11, -6).addBox(0.0F, -4.0F, -4.0F, 5.0F, 6.0F, 8.0F, 0.0F, true);
        leftShoulder.texOffs(-4, 1).addBox(-2.0F, -4.0F, 5.0F, 5.0F, 6.0F, 1.0F, 0.0F, true);
        leftShoulder.texOffs(-4, 1).addBox(-2.0F, -4.0F, -6.0F, 5.0F, 6.0F, 1.0F, 0.0F, true);
        leftShoulder.texOffs(-14, -8).addBox(3.0F, -4.0F, -5.0F, 1.0F, 7.0F, 10.0F, 0.0F, true);
        leftShoulder.texOffs(-14, -8).addBox(3.0F, -5.0F, -4.0F, 1.0F, 1.0F, 8.0F, 0.0F, true);

        rightArm = new XRotationModelRenderer(this);
        rightArm.setPos(-12.0F, -20.0F, 0.0F);
        upperPart.addChild(rightArm);
        rightArm.texOffs(32, 108).addBox(-3.0F, -4.0F, -4.0F, 7.0F, 12.0F, 8.0F, 0.0F, true);

        rightArmJoint = new ModelRenderer(this);
        rightArmJoint.setPos(0.0F, 7.5F, -0.5F);
        rightArm.addChild(rightArmJoint);
        rightArmJoint.texOffs(32, 102).addBox(-1.875F, -1.125F, -0.875F, 5.0F, 5.0F, 6.0F, -0.125F, false);

        rightForeArm = new ModelRenderer(this);
        rightForeArm.setPos(0.0F, 8.0F, 0.0F);
        rightArm.addChild(rightForeArm);
        rightForeArm.texOffs(32, 118).addBox(-2.999F, -0.001F, -3.999F, 7.0F, 12.0F, 8.0F, -0.001F, true);
        rightForeArm.texOffs(-2, -2).addBox(-3.5F, 7.0F, -2.0F, 1.0F, 4.0F, 4.0F, 0.0F, true);

        rightShoulder = new ModelRenderer(this);
        rightShoulder.setPos(0.0F, 0.0F, 0.0F);
        rightArm.addChild(rightShoulder);
        rightShoulder.texOffs(-4, 1).addBox(-3.0F, -4.0F, -6.0F, 5.0F, 6.0F, 1.0F, 0.0F, false);
        rightShoulder.texOffs(-14, -8).addBox(-3.0F, -5.0F, -5.0F, 6.0F, 8.0F, 10.0F, 0.0F, false);
        rightShoulder.texOffs(-14, -8).addBox(-4.0F, -4.0F, -5.0F, 1.0F, 7.0F, 10.0F, 0.0F, false);
        rightShoulder.texOffs(-14, -8).addBox(-4.0F, -5.0F, -4.0F, 1.0F, 1.0F, 8.0F, 0.0F, false);
        rightShoulder.texOffs(-11, -6).addBox(-3.0F, -6.0F, -4.0F, 5.0F, 1.0F, 8.0F, 0.0F, false);
        rightShoulder.texOffs(-11, -6).addBox(-5.0F, -4.0F, -4.0F, 5.0F, 6.0F, 8.0F, 0.0F, false);
        rightShoulder.texOffs(-4, 1).addBox(-3.0F, -4.0F, 5.0F, 5.0F, 6.0F, 1.0F, 0.0F, false);

        rightArmJoint2 = new ModelRenderer(this);
        rightArmJoint2.setPos(0.0F, 7.5F, -0.5F);
        rightArm.addChild(rightArmJoint2);


        leftLeg = new XRotationModelRenderer(this);
        leftLeg.setPos(4.0F, 24.0F, 0.0F);
        body.addChild(leftLeg);
        leftLeg.texOffs(96, 108).addBox(-4.0F, 0.0F, -4.0F, 8.0F, 12.0F, 8.0F, 0.0F, false);

        leftLegJoint = new ModelRenderer(this);
        leftLegJoint.setPos(0.0F, 12.0F, 0.0F);
        leftLeg.addChild(leftLegJoint);
        leftLegJoint.texOffs(96, 102).addBox(-3.125F, -3.125F, -2.875F, 6.0F, 6.0F, 6.0F, -0.125F, true);
        leftLegJoint.texOffs(62, 100).addBox(-2.875F, -3.125F, -4.875F, 6.0F, 6.0F, 8.0F, -0.125F, true);

        leftLowerLeg = new ModelRenderer(this);
        leftLowerLeg.setPos(0.0F, 12.0F, 0.0F);
        leftLeg.addChild(leftLowerLeg);
        leftLowerLeg.texOffs(96, 118).addBox(-4.001F, -0.001F, -3.999F, 8.0F, 12.0F, 8.0F, -0.001F, false);

        rightLeg = new XRotationModelRenderer(this);
        rightLeg.setPos(-4.0F, 24.0F, 0.0F);
        body.addChild(rightLeg);
        rightLeg.texOffs(64, 108).addBox(-4.0F, 0.0F, -4.0F, 8.0F, 12.0F, 8.0F, 0.0F, false);

        rightLegJoint = new ModelRenderer(this);
        rightLegJoint.setPos(0.0F, 12.0F, 0.0F);
        rightLeg.addChild(rightLegJoint);
        rightLegJoint.texOffs(62, 100).addBox(-3.125F, -3.125F, -4.875F, 6.0F, 6.0F, 8.0F, -0.125F, false);

        rightLowerLeg = new ModelRenderer(this);
        rightLowerLeg.setPos(0.0F, 12.0F, 0.0F);
        rightLeg.addChild(rightLowerLeg);
        rightLowerLeg.texOffs(64, 118).addBox(-4.001F, -0.001F, -3.999F, 8.0F, 12.0F, 8.0F, -0.001F, false);
	}

	@Override // TODO summon poses
    protected RotationAngle[][] initSummonPoseRotations() {
        return new RotationAngle[][] {
            new RotationAngle[] {
                    
            },
            new RotationAngle[] {
                    
            }
		};
    }
    
    @Override
    protected void initActionPoses() { // TODO pickaxe throwing anim
        actionAnim.put(StandPose.RANGED_ATTACK, new PosedActionAnimation.Builder<GoldExprienceRequiemEntity>()
                .addPose(StandEntityAction.Phase.BUTTON_HOLD, new ModelPose<>(new RotationAngle[] {
                        new RotationAngle(body, 0.0F, -0.48F, 0.0F),
                        new RotationAngle(leftArm, 0.0F, 0.0F, -0.7854F),
                        new RotationAngle(leftForeArm, 0.0F, 0.0F, 0.6109F),
                        new RotationAngle(rightArm, -1.0908F, 0.0F, 1.5708F), 
                        new RotationAngle(rightForeArm, 0.0F, 0.0F, 0.0F)
                }))
                .build(idlePose));
        
        super.initActionPoses();
    }
    
    

    @Override // TODO idle pose
    protected ModelPose<GoldExprienceRequiemEntity> initIdlePose() {
        return super.initIdlePose();
    }

    @Override
    protected IModelPose<GoldExprienceRequiemEntity> initIdlePose2Loop() {
        return super.initIdlePose2Loop();
    }
}