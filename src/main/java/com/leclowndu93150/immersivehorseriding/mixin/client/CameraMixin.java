package com.leclowndu93150.immersivehorseriding.mixin.client;

import com.leclowndu93150.immersivehorseriding.HorseRidingData;
import com.leclowndu93150.immersivehorseriding.config.ImmersiveHorseRidingConfig;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.level.BlockGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {

    @Shadow
    private Entity entity;

    @Shadow
    protected abstract void move(double d, double e, double f);

    @Inject(method = "setup", at = @At("RETURN"))
    private void immersive_horse_riding$addHeadBob(BlockGetter blockGetter, Entity entity, boolean detached, boolean thirdPerson, float partialTick, CallbackInfo ci) {
        if (detached || thirdPerson) return;
        if (this.entity == null) return;

        Entity vehicle = this.entity.getVehicle();
        if (!(vehicle instanceof AbstractHorse horse)) return;

        if (horse.isJumping()) return;

        float currentSpeed = HorseRidingData.getCurrentSpeed();
        if (currentSpeed < 0.01f) return;

        int animTick = HorseRidingData.getAnimTick();
        float intensity = ImmersiveHorseRidingConfig.headBobIntensity;
        float frequency = ImmersiveHorseRidingConfig.headBobFrequency;

        float smoothTick = animTick + partialTick;
        float bobOffset = (float) -Math.cos(smoothTick * frequency) * intensity * currentSpeed;

        this.move(0, bobOffset, 0);
    }
}
