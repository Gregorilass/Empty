package net.shadowmage.ancientwarfare.core.logistics;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.shadowmage.ancientwarfare.core.AncientWarfareCore;
import net.shadowmage.ancientwarfare.core.npc.AncientNpc;
import java.util.List;

/**
 * Stage 9 lumberjack job. Only recognized single-column overworld trees on dirt
 * with a nearby leaf canopy are eligible. It never scans unloaded chunks, mines
 * unrelated wooden buildings, or invents replacement saplings.
 */
public final class TreeFarmTask {
    private static final int MAX_TREE_HEIGHT = 16;
    private TreeFarmTask() {}

    /** @return 0 when idle, 1 when approaching a tree, 2 when a tree was felled. */
    public static int tick(ServerLevel level, AncientNpc npc, BlockPos worksite, int radius) {
        if (!level.hasChunkAt(worksite)
                || !level.getBlockState(worksite).is(AncientWarfareCore.TREE_FARM_WORKSITE.get())) return 0;

        Tree candidate = null;
        double best = Double.MAX_VALUE;
        int range = Math.clamp(radius, 2, 8);
        for (BlockPos pos : BlockPos.betweenClosed(worksite.offset(-range, -1, -range),
                                                    worksite.offset(range, 1, range))) {
            Tree tree = inspect(level, pos);
            if (tree == null) continue;
            double distance = npc.distanceToSqr(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
            if (distance < best) { candidate = tree; best = distance; }
        }
        if (candidate == null) return 0;
        if (best > 6.25) {
            BlockPos base = candidate.base();
            npc.getNavigation().moveTo(base.getX() + 0.5, base.getY(), base.getZ() + 0.5, 1.05);
            return 1;
        }

        // Recheck immediately before editing; other lumberjacks may have cut it.
        Tree fresh = inspect(level, candidate.base());
        if (fresh == null) return 0;
        LogisticsBlockEntity stock = WarehouseNetwork.findStock(level, worksite);
        npc.swingForAttack(InteractionHand.MAIN_HAND);
        for (int height = fresh.height() - 1; height >= 0; height--) {
            BlockPos at = fresh.base().above(height);
            BlockState trunk = level.getBlockState(at);
            if (!trunk.is(fresh.log())) break;
            // Determine the normal loot first, but only credit the inventory
            // after the block is actually removed. Failed edits grant nothing.
            List<ItemStack> drops = Block.getDrops(trunk, level, at, null);
            if (!level.setBlock(at, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL)) continue;
            for (ItemStack drop : drops) {
                ItemStack remaining = drop.copy();
                if (stock != null) remaining = stock.insert(remaining);
                remaining = npc.insertStorage(remaining);
                if (!remaining.isEmpty()) Block.popResource(level, at, remaining);
            }
        }
        // Replacement seedlings are paid for from the Tree Farm inventory.
        // If none are present, the bare ground remains until replenished.
        if (stock != null && level.getBlockState(fresh.base()).isAir()
                && fresh.sapling().defaultBlockState().canSurvive(level, fresh.base())) {
            for (int slot = 0; slot < stock.getContainerSize(); slot++) {
                ItemStack supplies = stock.getItem(slot);
                if (supplies.isEmpty() || !supplies.is(fresh.sapling().asItem())) continue;
                if (level.setBlock(fresh.base(), fresh.sapling().defaultBlockState(), Block.UPDATE_ALL)) {
                    stock.removeItem(slot, 1);
                }
                break;
            }
        }
        return 2;
    }

    private static Tree inspect(ServerLevel level, BlockPos base) {
        if (!level.hasChunkAt(base) || !level.getBlockState(base.below()).is(BlockTags.DIRT)) return null;
        BlockState bottom = level.getBlockState(base);
        Block sapling = saplingFor(bottom);
        if (sapling == null) return null;
        Block log = bottom.getBlock();
        // Reject 2x2 trunks and trees packed directly against another trunk;
        // these need the original mod's more sophisticated tree scanner.
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) continue;
                BlockPos adjacent = base.offset(dx, 0, dz);
                if (!level.hasChunkAt(adjacent)) return null;
                if (level.getBlockState(adjacent).is(log)) return null;
            }
        }
        int height = 0;
        for (; height < MAX_TREE_HEIGHT; height++) {
            BlockPos at = base.above(height);
            if (!level.hasChunkAt(at)) return null;
            BlockState state = level.getBlockState(at);
            if (!state.is(log)) break;
            if (level.getBlockEntity(at) != null) return null;
        }
        if (height < 3 || height == MAX_TREE_HEIGHT) return null;
        // A trunk above soil by itself is not enough: require a leaf crown.
        boolean canopy = false;
        BlockPos crown = base.above(height - 1);
        for (BlockPos at : BlockPos.betweenClosed(crown.offset(-3, -1, -3), crown.offset(3, 3, 3))) {
            if (!level.hasChunkAt(at)) return null;
            if (level.getBlockState(at).is(BlockTags.LEAVES)) { canopy = true; break; }
        }
        return canopy ? new Tree(base.immutable(), log, sapling, height) : null;
    }

    private static Block saplingFor(BlockState trunk) {
        // Conservative 1x1 trunk support only. Excludes stripped logs, planks,
        // giant 2x2 trunks, mangrove roots and Nether stems.
        return switch (BuiltInRegistries.BLOCK.getKey(trunk.getBlock()).getPath()) {
            case "oak_log" -> Blocks.OAK_SAPLING;
            case "birch_log" -> Blocks.BIRCH_SAPLING;
            case "spruce_log" -> Blocks.SPRUCE_SAPLING;
            case "jungle_log" -> Blocks.JUNGLE_SAPLING;
            case "acacia_log" -> Blocks.ACACIA_SAPLING;
            case "cherry_log" -> Blocks.CHERRY_SAPLING;
            default -> null;
        };
    }

    private record Tree(BlockPos base, Block log, Block sapling, int height) {}
}
