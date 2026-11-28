package com.leon.saintsdragons.util.animation;

import software.bernie.geckolib.animation.EasingType;
import software.bernie.geckolib.animation.keyframe.AnimationPoint;
import software.bernie.geckolib.animation.keyframe.BoneAnimation;
import software.bernie.geckolib.animation.keyframe.Keyframe;
import software.bernie.geckolib.animation.keyframe.KeyframeStack;
import software.bernie.geckolib.loading.math.MathParser;
import software.bernie.geckolib.loading.math.MathValue;
import software.bernie.geckolib.loading.math.MolangQueries;
import software.bernie.geckolib.loading.math.value.Constant;

import java.util.Arrays;
import java.util.List;

/** Samples GeckoLib 4 baked tracks, including easing and dynamic Molang expressions. */
public final class DragonAnimationSampler {
    private DragonAnimationSampler() {}

    public static void sample(BoneAnimation animation, double tick, double expressionTick, float[] pose) {
        if (pose == null || pose.length < 9) {
            throw new IllegalArgumentException("Pose buffer must contain at least 9 values");
        }
        Arrays.fill(pose, 0.0F);
        pose[6] = pose[7] = pose[8] = 1.0F;
        if (animation == null) return;

        double previousTime = MathParser.getVariableFor(MolangQueries.ANIM_TIME).get();
        MathParser.setVariable(MolangQueries.ANIM_TIME, () -> expressionTick / 20.0D);
        try {
            sample(animation.rotationKeyFrames(), tick, pose, 0);
            sample(animation.positionKeyFrames(), tick, pose, 3);
            sample(animation.scaleKeyFrames(), tick, pose, 6);
        } finally {
            MathParser.setVariable(MolangQueries.ANIM_TIME, () -> previousTime);
        }
    }

    private static void sample(KeyframeStack<Keyframe<MathValue>> stack, double tick, float[] pose, int offset) {
        pose[offset] = sample(stack.xKeyframes(), tick, offset == 0, 0, pose[offset]);
        pose[offset + 1] = sample(stack.yKeyframes(), tick, offset == 0, 1, pose[offset + 1]);
        pose[offset + 2] = sample(stack.zKeyframes(), tick, offset == 0, 2, pose[offset + 2]);
    }

    private static float sample(List<Keyframe<MathValue>> frames, double tick, boolean rotation, int axis, float fallback) {
        if (frames.isEmpty()) return fallback;
        double startTick = 0.0D;
        for (Keyframe<MathValue> frame : frames) {
            if (startTick + frame.length() > tick) return sample(frame, tick - startTick, rotation, axis);
            startTick += frame.length();
        }
        Keyframe<MathValue> last = frames.get(frames.size() - 1);
        return sample(last, last.length(), rotation, axis);
    }

    private static float sample(Keyframe<MathValue> frame, double localTick, boolean rotation, int axis) {
        double start = value(frame.startValue(), rotation, axis);
        double end = value(frame.endValue(), rotation, axis);
        return (float) EasingType.lerpWithOverride(new AnimationPoint(frame, localTick, frame.length(), start, end), null);
    }

    private static double value(MathValue value, boolean rotation, int axis) {
        double result = value.get();
        if (rotation && !(value instanceof Constant)) result = Math.toRadians(result) * (axis < 2 ? -1.0D : 1.0D);
        return result;
    }
}
