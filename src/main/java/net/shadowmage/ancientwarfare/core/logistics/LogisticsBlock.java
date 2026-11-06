package net.shadowmage.ancientwarfare.core.logistics;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** Stage 8 stock block. The familiar vanilla chest UI avoids a new client protocol. */
public final class LogisticsBlock extends BaseEntityBlock {
    public LogisticsBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LogisticsBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer
                && level.getBlockEntity(pos) instanceof LogisticsBlockEntity inventory) {
            serverPlayer.openMenu(new SimpleMenuProvider((id, playerInventory, menuPlayer) ->
                    inventory.getContainerSize() == 27
                            ? ChestMenu.threeRows(id, playerInventory, inventory)
                            : ChestMenu.sixRows(id, playerInventory, inventory),
                    Component.translatable(state.getBlock().getDescriptionId())));
        }
        return InteractionResult.SUCCESS;
    }
}
