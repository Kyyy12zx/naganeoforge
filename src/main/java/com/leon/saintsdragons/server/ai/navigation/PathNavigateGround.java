package com.leon.saintsdragons.server.ai.navigation;

import com.leon.saintsdragons.server.ai.pathfinding.DragonWalkNodeEvaluator;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Ground navigation tuned for large dragon hitboxes. The implementation keeps
 * vanilla path following as a fallback while using the dragon pathfinder,
 * support-aware waypoint heights and bounded recovery jumps.
 */
public class PathNavigateGround extends GroundPathNavigation {
    private static final int STALL_TICKS_BEFORE_JUMP = 4;
    private static final int JUMP_COOLDOWN_TICKS = 10;
    private static final int MAX_JUMPS_PER_NODE = 2;
    private static final double MAX_SUPPORT_GAP = GroundPathGeometry.MAX_SUPPORT_GAP;
    private static final double SUPPORT_EPSILON = GroundPathGeometry.SUPPORT_EPSILON;

    private boolean waterEntryAllowed;
    private float finalWaypointTolerance = Float.NaN;
    private int trackedNode = -1;
    private int nodeStallTicks;
    private int jumpCooldown;
    private int jumpAttempts;

    public PathNavigateGround(Mob mob, Level level) {
        super(mob, level);
    }

    public void setWaterEntryAllowed(boolean allowed) {
        this.waterEntryAllowed = allowed;
        this.setCanFloat(allowed);
    }

    public boolean isWaterEntryAllowed() {
        return this.waterEntryAllowed;
    }

    public void setFinalWaypointTolerance(double tolerance) {
        this.finalWaypointTolerance = (float) Math.max(0.05D, tolerance);
    }

    public void clearFinalWaypointTolerance() {
        this.finalWaypointTolerance = Float.NaN;
    }

    @Override
    public boolean moveTo(@Nullable Path path, double speedModifier) {
        this.clearFinalWaypointTolerance();
        this.resetStallTracking();
        return super.moveTo(path, speedModifier);
    }

    @Override
    public void stop() {
        this.clearFinalWaypointTolerance();
        this.resetStallTracking();
        super.stop();
    }

    @Override
    protected @NotNull PathFinder createPathFinder(int maxVisitedNodes) {
        this.nodeEvaluator = new DragonWalkNodeEvaluator();
        this.nodeEvaluator.setCanPassDoors(true);
        return new PathFinderGround(this.nodeEvaluator, maxVisitedNodes);
    }

    @Override
    public void tick() {
        if (this.jumpCooldown > 0) {
            this.jumpCooldown--;
        }
        Path activePath = this.path;
        int node = activePath == null ? -1 : activePath.getNextNodeIndex();
        if (node < 0 || activePath.isDone()) {
            this.resetStallTracking();
        } else if (node != this.trackedNode) {
            this.trackedNode = node;
            this.nodeStallTicks = 0;
            this.jumpAttempts = 0;
        } else {
            this.nodeStallTicks++;
            this.tryRecoveryJump(activePath, node);
        }
        super.tick();
    }

    private void tryRecoveryJump(Path path, int nodeIndex) {
        if (this.nodeStallTicks < STALL_TICKS_BEFORE_JUMP || this.jumpCooldown > 0
                || this.jumpAttempts >= MAX_JUMPS_PER_NODE || !this.mob.onGround()
                || !this.mob.horizontalCollision || this.mob.isInWaterOrBubble()) {
            return;
        }
        Vec3 waypoint = this.getGroundedPathPosition(path, nodeIndex);
        double rise = waypoint.y - this.mob.getY();
        double dx = waypoint.x - this.mob.getX();
        double dz = waypoint.z - this.mob.getZ();
        double maxDistance = Math.max(1.0D, this.mob.getBbWidth());
        if (rise < 0.25D || rise > 1.5D || dx * dx + dz * dz > maxDistance * maxDistance) {
            return;
        }
        this.mob.getJumpControl().jump();
        this.jumpAttempts++;
        this.jumpCooldown = JUMP_COOLDOWN_TICKS;
    }

    private void resetStallTracking() {
        this.trackedNode = -1;
        this.nodeStallTicks = 0;
        this.jumpAttempts = 0;
        this.jumpCooldown = 0;
    }

    @Override
    protected void followThePath() {
        super.followThePath();
        if (this.path == null || this.path.isDone() || !Float.isFinite(this.finalWaypointTolerance)) {
            return;
        }
        if (this.path.getNextNodeIndex() != this.path.getNodeCount() - 1) {
            return;
        }
        Vec3 waypoint = this.getGroundedPathPosition(this.path, this.path.getNextNodeIndex());
        double tolerance = Math.max(0.05D, this.finalWaypointTolerance);
        if (this.mob.position().distanceToSqr(waypoint) <= tolerance * tolerance) {
            this.path.setNextNodeIndex(this.path.getNodeCount());
        }
    }

    private Vec3 getGroundedPathPosition(Path path, int index) {
        Vec3 point = path.getEntityPosAtNode(this.mob, index);
        AABB body = this.mob.getBoundingBox().move(point.subtract(this.mob.position()));
        double support = this.findSupportHeight(body);
        return new Vec3(point.x, Double.isFinite(support) ? support : point.y, point.z);
    }

    @Override
    protected double getGroundY(Vec3 pathPos) {
        AABB body = this.mob.getBoundingBox().move(pathPos.subtract(this.mob.position()));
        double support = this.findSupportHeight(body);
        return Double.isFinite(support) ? support : super.getGroundY(pathPos);
    }

    private double findSupportHeight(AABB body) {
        AABB search = new AABB(body.minX, body.minY - MAX_SUPPORT_GAP, body.minZ,
                body.maxX, body.minY + SUPPORT_EPSILON, body.maxZ);
        double highest = Double.NEGATIVE_INFINITY;
        for (VoxelShape shape : this.level.getBlockCollisions(this.mob, search)) {
            for (AABB obstacle : shape.toAabbs()) {
                highest = Math.max(highest, GroundPathGeometry.supportHeight(body, obstacle));
            }
        }
        return highest;
    }

    @Override
    protected boolean hasValidPathType(@NotNull PathType pathType) {
        if (pathType == PathType.LAVA) {
            return false;
        }
        if (pathType == PathType.WATER) {
            return this.waterEntryAllowed || this.mob.isInWaterOrBubble();
        }
        return pathType != PathType.OPEN;
    }
}
