package com.leon.saintsdragons.util.animation;

import net.minecraft.world.entity.LivingEntity;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.state.BoneSnapshot;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;

import java.util.Map;

/** Keeps GeckoLib playback alive on client ticks without evaluating a hidden skeleton. */
public class EntityAnimationController<T extends LivingEntity & TickingGeoEntity> extends AnimationController<T> {
    private int lastPlaybackTick = Integer.MIN_VALUE;
    private Object animationResource;
    private boolean needsRenderSnapshots;

    public EntityAnimationController(T entity, String name, int transitionTicks, AnimationStateHandler<T> predicate) {
        super(entity, name, transitionTicks, predicate);
    }

    public final void tickPlayback(GeoModel<T> model, AnimationState<T> state, Object resource) {
        if (!animatable.level().isClientSide || lastPlaybackTick == animatable.tickCount) return;
        lastPlaybackTick = animatable.tickCount;
        if (animationResource != null && animationResource != resource) forceAnimationReset();
        animationResource = resource;
        var previousAnimation = currentAnimation;
        isJustStarting = false;
        super.process(model, state.withController(this), Map.of(), Map.of(), state.animationTick, false);
        if (currentAnimation != previousAnimation) needsRenderSnapshots = true;
        onPlaybackTick(model, state);
    }

    protected void onPlaybackTick(GeoModel<T> model, AnimationState<T> state) {}

    @Override
    public void process(GeoModel<T> model, AnimationState<T> state,
                        Map<String, GeoBone> bones, Map<String, BoneSnapshot> snapshots,
                        double seekTime, boolean crashWhenCantFindBone) {
        if (lastPlaybackTick != Integer.MIN_VALUE) isJustStarting = false;
        if (needsRenderSnapshots) {
            needsRenderSnapshots = false;
            if (animationState == State.TRANSITIONING && currentAnimation != null) {
                boneSnapshots.clear();
                for (var track : currentAnimation.animation().boneAnimations()) {
                    BoneSnapshot snapshot = snapshots.get(track.boneName());
                    if (snapshot != null) boneSnapshots.put(track.boneName(), BoneSnapshot.copy(snapshot));
                }
            }
        }
        super.process(model, state, bones, snapshots, seekTime, crashWhenCantFindBone);
    }
}
