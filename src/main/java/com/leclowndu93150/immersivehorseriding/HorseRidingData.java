package com.leclowndu93150.immersivehorseriding;

public class HorseRidingData {
    private static float currentSpeed = 0.0f;
    private static int animTick = 0;

    public static float getCurrentSpeed() {
        return currentSpeed;
    }

    public static void setCurrentSpeed(float speed) {
        currentSpeed = speed;
    }

    public static int getAnimTick() {
        return animTick;
    }

    public static void setAnimTick(int tick) {
        animTick = tick;
    }
}
