package com.leclowndu93150.immersivehorseriding;

import com.bawnorton.mixinsquared.api.MixinCanceller;

import java.util.List;

public class ImmersiveHorseRidingMixinCanceller implements MixinCanceller {

    // Fix compatibility issue with horseman
    // free_camera_when_mounted feature breaks camera bobbing
    // rotate_horse_when_mounting feature doesn't work with this mod, it just causes the player's camera to not be rotated when mounting a horse
    @Override
    public boolean shouldCancel(List<String> targetClassNames, String mixinClassName) {
        return mixinClassName.equals("io.github.mortuusars.horseman.mixin.free_camera_when_mounted.AbstractHorseMixin")
                || mixinClassName.equals("io.github.mortuusars.horseman.mixin.rotate_horse_when_mounting.AbstractHorseMixin");
    }
}
