package com.leon.saintsdragons.server.ai.navigation;

import com.leon.saintsdragons.server.ai.pathfinding.DragonPathSearchDebug;
import com.leon.saintsdragons.server.ai.pathfinding.DragonPathSearchDebuggable;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** Adds path-search diagnostics and entity-width-aware waypoint positions. */
public class PathFinderGround extends PathFinder {
    private final NodeEvaluator dragonNodeEvaluator;

    public PathFinderGround(NodeEvaluator processor, int maxVisitedNodes) {
        super(processor, maxVisitedNodes);
        this.dragonNodeEvaluator = processor;
    }

    @Nullable
    @Override
    public Path findPath(@NotNull PathNavigationRegion regionIn, @NotNull Mob mob,
                         @NotNull Set<BlockPos> targetPositions, float maxRange,
                         int accuracy, float searchDepthMultiplier) {
        BlockPos target = targetPositions.stream().findFirst().orElse(mob.blockPosition());
        DragonPathSearchDebug.NodeCollector debugCollector = DragonPathSearchDebug.beginNodeSearch(
                mob,
                DragonPathSearchDebug.SearchType.GROUND,
                Vec3.atCenterOf(target)
        );
        if (this.dragonNodeEvaluator instanceof DragonPathSearchDebuggable debuggable) {
            debuggable.setPathSearchDebugCollector(debugCollector);
        }

        Path path = null;
        try {
            path = super.findPath(regionIn, mob, targetPositions, maxRange, accuracy, searchDepthMultiplier);
            return path == null ? null : new DragonPatchedPath(path);
        } finally {
            if (debugCollector != null) {
                debugCollector.complete(path);
            }
            if (this.dragonNodeEvaluator instanceof DragonPathSearchDebuggable debuggable) {
                debuggable.setPathSearchDebugCollector(null);
            }
        }
    }

    static class DragonPatchedPath extends Path {
        DragonPatchedPath(Path original) {
            super(copyPathPoints(original), original.getTarget(), original.canReach());
        }

        @Override
        public @NotNull Vec3 getEntityPosAtNode(@NotNull Entity entity, int index) {
            Node point = this.getNode(index);
            double centerOffset = Mth.floor(entity.getBbWidth() + 1.0F) * 0.5D;
            return new Vec3(point.x + centerOffset, point.y, point.z + centerOffset);
        }

        private static List<Node> copyPathPoints(Path original) {
            List<Node> points = new ArrayList<>(original.getNodeCount());
            for (int i = 0; i < original.getNodeCount(); i++) {
                points.add(original.getNode(i));
            }
            return points;
        }
    }
}
