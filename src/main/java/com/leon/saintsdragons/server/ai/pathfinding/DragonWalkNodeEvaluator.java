package com.leon.saintsdragons.server.ai.pathfinding;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Target;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

/**
 * Dragon-aware ground evaluator. Keeps vanilla walk evaluation while adding
 * ladder handling, optional water avoidance, tree-passability injection and
 * the existing path-search diagnostics hook.
 */
public class DragonWalkNodeEvaluator extends WalkNodeEvaluator implements DragonPathSearchDebuggable {
    private final Predicate<BlockState> canPassThroughTrees;
    private final boolean avoidWater;
    private @Nullable DragonPathSearchDebug.NodeCollector pathSearchDebugCollector;

    public DragonWalkNodeEvaluator() {
        this(state -> false, false);
    }

    public DragonWalkNodeEvaluator(boolean canPassThroughTrees) {
        this(state -> canPassThroughTrees && (state.is(BlockTags.LOGS) || state.is(BlockTags.LEAVES)), false);
    }

    public DragonWalkNodeEvaluator(boolean canPassThroughTrees, boolean avoidWater) {
        this(state -> canPassThroughTrees && (state.is(BlockTags.LOGS) || state.is(BlockTags.LEAVES)), avoidWater);
    }

    public DragonWalkNodeEvaluator(Predicate<BlockState> canPassThroughTrees) {
        this(canPassThroughTrees, false);
    }

    public DragonWalkNodeEvaluator(Predicate<BlockState> canPassThroughTrees, boolean avoidWater) {
        this.canPassThroughTrees = canPassThroughTrees;
        this.avoidWater = avoidWater;
    }

    @Override
    public void setPathSearchDebugCollector(@Nullable DragonPathSearchDebug.NodeCollector collector) {
        this.pathSearchDebugCollector = collector;
    }

    @Override
    public int getNeighbors(Node[] neighbors, Node current) {
        int count = super.getNeighbors(neighbors, current);
        if (this.pathSearchDebugCollector != null) {
            this.pathSearchDebugCollector.recordExpansion(current, neighbors, count);
        }
        return count;
    }

    @Override
    public @NotNull Node getStart() {
        Node vanillaStart = super.getStart();
        int footprintOffset = Math.max(0, this.entityWidth / 2);
        if (footprintOffset == 0) {
            return vanillaStart;
        }
        return getStartNode(new BlockPos(
                this.mob.getBlockX() - footprintOffset,
                vanillaStart.y,
                this.mob.getBlockZ() - footprintOffset
        ));
    }

    @Override
    public @NotNull Target getGoal(double x, double y, double z) {
        int footprintOffset = Math.max(0, this.entityWidth / 2);
        return getTargetFromNode(getNode(
                net.minecraft.util.Mth.floor(x) - footprintOffset,
                net.minecraft.util.Mth.floor(y),
                net.minecraft.util.Mth.floor(z) - footprintOffset
        ));
    }

    @Override
    public @NotNull PathType getBlockPathType(BlockGetter level, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        BlockState state = level.getBlockState(pos);
        if (state.is(Blocks.LADDER)) {
            return PathType.WALKABLE;
        }
        if (this.canPassThroughTrees.test(state)) {
            return PathType.OPEN;
        }
        PathType pathType = super.getBlockPathType(level, x, y, z);
        if (this.avoidWater && (pathType == PathType.WATER || pathType == PathType.WATER_BORDER)) {
            return PathType.BLOCKED;
        }
        return pathType;
    }
}
