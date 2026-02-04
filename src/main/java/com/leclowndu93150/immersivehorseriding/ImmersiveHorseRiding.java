package com.leclowndu93150.immersivehorseriding;

import com.leclowndu93150.immersivehorseriding.config.ImmersiveHorseRidingConfig;
import net.fabricmc.api.ModInitializer;

public class ImmersiveHorseRiding implements ModInitializer {

    @Override
    public void onInitialize() {
        ImmersiveHorseRidingConfig.load();
    }
}
