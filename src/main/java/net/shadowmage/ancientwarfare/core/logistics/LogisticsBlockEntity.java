package net.shadowmage.ancientwarfare.core.logistics;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.shadowmage.ancientwarfare.core.AncientWarfareCore;

/** Persistent, server-owned stock buffer. Vanilla container removal drops inventory contents. */
public final class LogisticsBlockEntity extends BlockEntity implements Container {
    private final NonNullList<ItemStack> items;

    public LogisticsBlockEntity(BlockPos pos, BlockState state) {
        super(AncientWarfareCore.LOGISTICS_BLOCK_ENTITY.get(), pos, state);
        int slots = state.is(AncientWarfareCore.FARM_WORKSITE.get()) ? 27 : 54;
        items = NonNullList.withSize(slots, ItemStack.EMPTY);
    }

    @Override public int getContainerSize() { return items.size(); }
    @Override public boolean isEmpty() {
        for (ItemStack stack : items) if (!stack.isEmpty()) return false;
        return true;
    }
    @Override public ItemStack getItem(int slot) {
        return slot >= 0 && slot < items.size() ? items.get(slot) : ItemStack.EMPTY;
    }
    @Override public ItemStack removeItem(int slot, int count) {
        if (slot < 0 || slot >= items.size() || count <= 0) return ItemStack.EMPTY;
        ItemStack result = ContainerHelper.removeItem(items, slot, count);
        if (!result.isEmpty()) setChanged();
        return result;
    }
    @Override public ItemStack removeItemNoUpdate(int slot) {
        if (slot < 0 || slot >= items.size()) return ItemStack.EMPTY;
        ItemStack result = ContainerHelper.takeItem(items, slot);
        if (!result.isEmpty()) setChanged();
        return result;
    }
    @Override public void setItem(int slot, ItemStack stack) {
        if (slot < 0 || slot >= items.size()) return;
        items.set(slot, stack);
        if (!stack.isEmpty() && stack.getCount() > Math.min(getMaxStackSize(), stack.getMaxStackSize())) {
            stack.setCount(Math.min(getMaxStackSize(), stack.getMaxStackSize()));
        }
        setChanged();
    }
    @Override public boolean stillValid(Player player) {
        return this.level != null && this.level.getBlockEntity(worldPosition) == this
                && player.distanceToSqr(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5,
                                        worldPosition.getZ() + 0.5) <= 64.0;
    }
    @Override public void clearContent() {
        for (int i = 0; i < items.size(); ++i) items.set(i, ItemStack.EMPTY);
        setChanged();
    }

    /** Inserts only the amount that fits; caller retains overflow. Never destroys items. */
    public ItemStack insert(ItemStack remaining) {
        if (remaining.isEmpty()) return remaining;
        for (int i = 0; i < items.size() && !remaining.isEmpty(); i++) {
            ItemStack existing = items.get(i);
            if (existing.isEmpty() || !ItemStack.isSameItemSameComponents(existing, remaining)) continue;
            int move = Math.min(remaining.getCount(), Math.min(getMaxStackSize(), existing.getMaxStackSize()) - existing.getCount());
            if (move > 0) { existing.grow(move); remaining.shrink(move); setChanged(); }
        }
        for (int i = 0; i < items.size() && !remaining.isEmpty(); i++) {
            if (!items.get(i).isEmpty()) continue;
            int move = Math.min(remaining.getCount(), Math.min(getMaxStackSize(), remaining.getMaxStackSize()));
            items.set(i, remaining.copyWithCount(move));
            remaining.shrink(move);
            setChanged();
        }
        return remaining;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output.child("AWLogisticsItems"), items);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        items.replaceAll(stack -> ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input.childOrEmpty("AWLogisticsItems"), items);
    }
}
