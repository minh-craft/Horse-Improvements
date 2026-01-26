package com.leclowndu93150.horseimprovements.mixin;

import com.leclowndu93150.horseimprovements.HorseAccessor;
import com.leclowndu93150.horseimprovements.HorseRidingData;
import com.leclowndu93150.horseimprovements.config.HorseImprovementsConfig;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractHorse.class)
public abstract class AbstractHorseMixin extends Animal implements HorseAccessor {

    @Unique
    private float horseimprovements$currentSpeed = 0.0f;

    @Unique
    private float horseimprovements$targetYRot = 0.0f;

    @Unique
    private int horseimprovements$animTick = 0;

    @Unique
    private float horseimprovements$lastForwardInput = 0.0f;

    @Unique
    private float horseimprovements$lastStrafeInput = 0.0f;

    protected AbstractHorseMixin(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public float horseimprovements$getCurrentSpeed() {
        return horseimprovements$currentSpeed;
    }

    @Override
    public int horseimprovements$getAnimTick() {
        return horseimprovements$animTick;
    }

    @Inject(method = "getRiddenRotation", at = @At("HEAD"), cancellable = true)
    private void horseimprovements$overrideRotation(LivingEntity rider, CallbackInfoReturnable<Vec2> cir) {
        if (!(rider instanceof Player player)) return;

        float strafeInput = horseimprovements$lastStrafeInput;
        float forwardInput = horseimprovements$lastForwardInput;

        boolean isActuallyMoving = this.walkAnimation.isMoving();
        boolean wantsToMove = forwardInput != 0;
        boolean isMoving = isActuallyMoving || wantsToMove;
        boolean isTurning = Math.abs(strafeInput) > 0.01f;

        if (isMoving || isTurning) {
            if (isMoving) {
                horseimprovements$animTick++;
            } else {
                horseimprovements$animTick = 0;
            }

            if (isTurning) {
                float turnAmount = -strafeInput * HorseImprovementsConfig.turnSpeed;
                horseimprovements$targetYRot = Mth.wrapDegrees(this.getYRot() + turnAmount);
            }

            float currentYRot = this.getYRot();
            float newYRot = horseimprovements$rotateToward(currentYRot, horseimprovements$targetYRot, HorseImprovementsConfig.turnSpeed);

            cir.setReturnValue(new Vec2(player.getXRot() * 0.5f, newYRot));
        } else {
            horseimprovements$animTick = 0;
            horseimprovements$targetYRot = this.getYRot();
            cir.setReturnValue(new Vec2(player.getXRot() * 0.5f, this.getYRot()));
        }

        if (this.level().isClientSide) {
            HorseRidingData.setCurrentSpeed(horseimprovements$currentSpeed);
            HorseRidingData.setAnimTick(horseimprovements$animTick);
        }
    }

    @ModifyReturnValue(method = "getRiddenInput", at = @At("RETURN"))
    private Vec3 horseimprovements$applyAcceleration(Vec3 original, Player player, Vec3 moveVec) {
        float forwardInput = player.zza;
        float strafeInput = player.xxa;

        horseimprovements$lastForwardInput = forwardInput;
        horseimprovements$lastStrafeInput = strafeInput;

        float targetSpeed = forwardInput > 0 ? 1.0f : 0.0f;

        if (horseimprovements$currentSpeed < targetSpeed) {
            horseimprovements$currentSpeed = Math.min(
                    horseimprovements$currentSpeed + HorseImprovementsConfig.acceleration,
                    targetSpeed
            );
        } else if (horseimprovements$currentSpeed > targetSpeed) {
            horseimprovements$currentSpeed = Math.max(
                    horseimprovements$currentSpeed - HorseImprovementsConfig.deceleration,
                    0.0f
            );
        }

        float forwardMovement;
        if (forwardInput < 0) {
            forwardMovement = forwardInput * 0.25f;
        } else {
            forwardMovement = horseimprovements$currentSpeed;
        }

        return new Vec3(0, original.y, forwardMovement);
    }

    @Unique
    private float horseimprovements$rotateToward(float current, float target, float maxDelta) {
        float diff = Mth.wrapDegrees(target - current);
        if (Math.abs(diff) <= maxDelta) {
            return target;
        }
        return current + Math.signum(diff) * maxDelta;
    }
}
