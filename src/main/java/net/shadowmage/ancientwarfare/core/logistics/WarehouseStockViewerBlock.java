package net.shadowmage.ancientwarfare.core.logistics;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** Stage 10: vanilla-compatible viewer of real warehouse network stock. */
public final class WarehouseStockViewerBlock extends Block {
    public WarehouseStockViewerBlock(BlockBehaviour.Properties properties) { super(properties); }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit) {
        if (!level.isClientSide() && level instanceof ServerLevel serverLevel
                && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(new SimpleMenuProvider(
                    (id, inventory, owner) -> new WarehouseOverviewMenu(id, inventory, serverLevel, pos),
                    Component.literal("Warehouse Stock Viewer")));
        }
        return InteractionResult.SUCCESS;
    }
}
