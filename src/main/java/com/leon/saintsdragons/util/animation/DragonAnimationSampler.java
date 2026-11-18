package com.leon.saintsdragons.util.animation;

import com.eliotlash.mclib.math.Constant;
import com.eliotlash.mclib.math.IValue;
import software.bernie.geckolib.core.animation.EasingType;
import software.bernie.geckolib.core.keyframe.AnimationPoint;
import software.bernie.geckolib.core.keyframe.BoneAnimation;
import software.bernie.geckolib.core.keyframe.Keyframe;
import software.bernie.geckolib.core.keyframe.KeyframeStack;
import software.bernie.geckolib.core.molang.MolangParser;
import software.bernie.geckolib.core.molang.MolangQueries;

import java.util.Arrays;
import java.util.List;

/** Samples GeckoLib baked tracks, including easing and dynamic Molang expressions. */
public final class DragonAnimationSampler {
    private DragonAnimationSampler() {}

    public static void sample(BoneAnimation animation, double tick, double expressionTick, float[] pose) {
        if (pose == null || pose.length < 9) {
            throw new IllegalArgumentException("Pose buffer must contain at least 9 values");
        }
        Arrays.fill(pose, 0.0F);
        pose[6] = pose[7] = pose[8] = 1.0F;
        if (animation == null) return;
        MolangParser parser = MolangParser.INSTANCE;
        double previousTime = parser.getVariable(MolangQueries.ANIM_TIME).get();
        parser.setValue(MolangQueries.ANIM_TIME, () -> expressionTick / 20.0D);
        try {
            sample(animation.rotationKeyFrames(), tick, pose, 0);
            sample(animation.positionKeyFrames(), tick, pose, 3);
            sample(animation.scaleKeyFrames(), tick, pose, 6);
        } finally {
            parser.setValue(MolangQueries.ANIM_TIME, () -> previousTime);
        }
    }

    private static void sample(KeyframeStack<Keyframe<IValue>> stack, double tick, float[] pose, int offset) {
        pose[offset] = sample(stack.xKeyframes(), tick, offset == 0, 0, pose[offset]);
        pose[offset + 1] = sample(stack.yKeyframes(), tick, offset == 0, 1, pose[offset + 1]);
        pose[offset + 2] = sample(stack.zKeyframes(), tick, offset == 0, 2, pose[offset + 2]);
    }

    private static float sample(List<Keyframe<IValue>> frames, double tick, boolean rotation, int axis, float fallback) {
        if (frames.isEmpty()) return fallback;
        double startTick = 0.0D;
        for (Keyframe<IValue> frame : frames) {
            if (startTick + frame.length() > tick) return sample(frame, tick - startTick, rotation, axis);
            startTick += frame.length();
        }
        Keyframe<IValue> last = frames.get(frames.size() - 1);
        return sample(last, last.length(), rotation, axis);
    }

    private static float sample(Keyframe<IValue> frame, double localTick, boolean rotation, int axis) {
        double start = value(frame.startValue(), rotation, axis);
        double end = value(frame.endValue(), rotation, axis);
        return (float) EasingType.lerpWithOverride(new AnimationPoint(frame, localTick, frame.length(), start, end), null);
    }

    private static double value(IValue value, boolean rotation, int axis) {
        double result = value.get();
        if (rotation && !(value instanceof Constant)) result = Math.toRadians(result) * (axis < 2 ? -1.0D : 1.0D);
        return result;
    }
}
