package com.leclowndu93150.immersivehorseriding.mixin.client;

import com.leclowndu93150.immersivehorseriding.config.ImmersiveHorseRidingConfig;
import net.minecraft.client.model.HorseModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HorseModel.class)
public class HorseModelMixin<T extends AbstractHorse> {

    @Final
    @Shadow
    protected ModelPart headParts;

    @Unique
    private float immersive_horse_riding$savedLimbSwing;

    @Unique
    private float immersive_horse_riding$savedLimbSwingAmount;

    @Inject(method = "prepareMobModel(Lnet/minecraft/world/entity/animal/horse/AbstractHorse;FFF)V",
            at = @At("HEAD"))
    private void immersive_horse_riding$captureParams(T horse, float limbSwing, float limbSwingAmount, float partialTick, CallbackInfo ci) {
        this.immersive_horse_riding$savedLimbSwing = limbSwing;
        this.immersive_horse_riding$savedLimbSwingAmount = limbSwingAmount;
    }

    @Inject(method = "prepareMobModel(Lnet/minecraft/world/entity/animal/horse/AbstractHorse;FFF)V",
            at = @At("RETURN"))
    private void immersive_horse_riding$adjustHeadBob(T horse, float limbSwing, float limbSwingAmount, float partialTick, CallbackInfo ci) {
        if (limbSwingAmount <= 0.2F) return;

        float standAnim = horse.getStandAnim(partialTick);
        float eatAnim = horse.getEatAnim(partialTick);
        if (standAnim > 0 || eatAnim > 0) return;

        float vanillaBob = Mth.cos(immersive_horse_riding$savedLimbSwing * 0.8F) * 0.15F * immersive_horse_riding$savedLimbSwingAmount;

        float customBob = Mth.cos(immersive_horse_riding$savedLimbSwing * ImmersiveHorseRidingConfig.horseHeadBobFrequency)
                * ImmersiveHorseRidingConfig.horseHeadBobIntensity
                * immersive_horse_riding$savedLimbSwingAmount;

        this.headParts.xRot = this.headParts.xRot - vanillaBob + customBob;
    }
}
