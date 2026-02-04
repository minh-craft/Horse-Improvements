package com.leclowndu93150.immersivehorseriding.mixin;

import com.leclowndu93150.immersivehorseriding.HorseAccessor;
import com.leclowndu93150.immersivehorseriding.HorseRidingData;
import com.leclowndu93150.immersivehorseriding.config.ImmersiveHorseRidingConfig;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractHorse.class)
public abstract class AbstractHorseMixin extends Animal implements HorseAccessor {

    @Unique
    private float immersive_horse_riding$currentSpeed = 0.0f;

    @Unique
    private float immersive_horse_riding$targetYRot = 0.0f;

    @Unique
    private int immersive_horse_riding$animTick = 0;

    @Unique
    private float immersive_horse_riding$lastForwardInput = 0.0f;

    @Unique
    private float immersive_horse_riding$lastStrafeInput = 0.0f;

    @Unique
    private boolean immersive_horse_riding$wasBeingRidden = false;

    protected AbstractHorseMixin(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public float immersive_horse_riding$getCurrentSpeed() {
        return immersive_horse_riding$currentSpeed;
    }

    @Override
    public int immersive_horse_riding$getAnimTick() {
        return immersive_horse_riding$animTick;
    }

    @Inject(method = "getRiddenRotation", at = @At("HEAD"), cancellable = true)
    private void immersive_horse_riding$overrideRotation(LivingEntity rider, CallbackInfoReturnable<Vec2> cir) {
        if (!(rider instanceof Player player)) return;

        if (!immersive_horse_riding$wasBeingRidden) {
            immersive_horse_riding$targetYRot = this.getYRot();
            immersive_horse_riding$wasBeingRidden = true;
        }

        float strafeInput = immersive_horse_riding$lastStrafeInput;
        float forwardInput = immersive_horse_riding$lastForwardInput;

        boolean isActuallyMoving = this.walkAnimation.isMoving();
        boolean wantsToMove = forwardInput != 0;
        boolean isMoving = isActuallyMoving || wantsToMove;
        boolean isTurning = Math.abs(strafeInput) > 0.01f;

        if (isMoving || isTurning) {
            if (isMoving) {
                immersive_horse_riding$animTick++;
            } else {
                immersive_horse_riding$animTick = 0;
            }

            if (isTurning) {
                float turnAmount = -strafeInput * ImmersiveHorseRidingConfig.turnSpeed;
                immersive_horse_riding$targetYRot = Mth.wrapDegrees(this.getYRot() + turnAmount);
            }

            float currentYRot = this.getYRot();
            float newYRot = immersive_horse_riding$rotateToward(currentYRot, immersive_horse_riding$targetYRot, ImmersiveHorseRidingConfig.turnSpeed);

            cir.setReturnValue(new Vec2(player.getXRot() * 0.5f, newYRot));
        } else {
            immersive_horse_riding$animTick = 0;
            immersive_horse_riding$targetYRot = this.getYRot();
            cir.setReturnValue(new Vec2(player.getXRot() * 0.5f, this.getYRot()));
        }

        if (this.level().isClientSide) {
            HorseRidingData.setCurrentSpeed(immersive_horse_riding$currentSpeed);
            HorseRidingData.setAnimTick(immersive_horse_riding$animTick);
        }
    }

    @ModifyReturnValue(method = "getRiddenInput", at = @At("RETURN"))
    private Vec3 immersive_horse_riding$applyAcceleration(Vec3 original, Player player, Vec3 moveVec) {
        float forwardInput = player.zza;
        float strafeInput = player.xxa;

        immersive_horse_riding$lastForwardInput = forwardInput;
        immersive_horse_riding$lastStrafeInput = strafeInput;

        float targetSpeed = forwardInput > 0 ? 1.0f : 0.0f;

        if (immersive_horse_riding$currentSpeed < targetSpeed) {
            immersive_horse_riding$currentSpeed = Math.min(
                    immersive_horse_riding$currentSpeed + ImmersiveHorseRidingConfig.acceleration,
                    targetSpeed
            );
        } else if (immersive_horse_riding$currentSpeed > targetSpeed) {
            immersive_horse_riding$currentSpeed = Math.max(
                    immersive_horse_riding$currentSpeed - ImmersiveHorseRidingConfig.deceleration,
                    0.0f
            );
        }

        float forwardMovement;
        if (forwardInput < 0) {
            forwardMovement = forwardInput * 0.25f;
        } else {
            forwardMovement = immersive_horse_riding$currentSpeed;
        }

        return new Vec3(0, original.y, forwardMovement);
    }

    @Unique
    private float immersive_horse_riding$rotateToward(float current, float target, float maxDelta) {
        float diff = Mth.wrapDegrees(target - current);
        if (Math.abs(diff) <= maxDelta) {
            return target;
        }
        return current + Math.signum(diff) * maxDelta;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void immersive_horse_riding$checkRiderStatus(CallbackInfo ci) {
        if (immersive_horse_riding$wasBeingRidden && this.getControllingPassenger() == null) {
            immersive_horse_riding$wasBeingRidden = false;
            immersive_horse_riding$currentSpeed = 0.0f;
        }
    }
}
