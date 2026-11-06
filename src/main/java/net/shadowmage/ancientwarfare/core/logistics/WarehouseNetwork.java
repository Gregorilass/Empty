package net.shadowmage.ancientwarfare.core.logistics;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.shadowmage.ancientwarfare.core.AncientWarfareCore;
import net.shadowmage.ancientwarfare.core.npc.AncientNpc;
import java.util.ArrayList;
import java.util.List;

/**
 * Stage 8 local warehouse network. Nearby storage extends the assigned controller.
 * Uses loaded chunks only. It never forces chunk loading or moves items between dimensions.
 */
public final class WarehouseNetwork {
    public static final int STORAGE_RADIUS = 8;
    private WarehouseNetwork() {}

    public static LogisticsBlockEntity findStock(ServerLevel level, BlockPos position) {
        if (!level.hasChunkAt(position)) return null;
        return level.getBlockEntity(position) instanceof LogisticsBlockEntity storage ? storage : null;
    }

    /** Returns only a real controller; destroying it suspends deliveries without deleting cargo. */
    public static boolean isController(ServerLevel level, BlockPos position) {
        return level.hasChunkAt(position)
                && level.getBlockState(position).is(AncientWarfareCore.WAREHOUSE_CONTROLLER.get())
                && findStock(level, position) != null;
    }

    /** Controllers and their nearby loaded storage form one local delivery destination. */
    private static List<LogisticsBlockEntity> findDestinations(ServerLevel level, BlockPos controller) {
        List<LogisticsBlockEntity> destinations = new ArrayList<>();
        if (!isController(level, controller)) return destinations;
        destinations.add(findStock(level, controller));
        for (int dy = -2; dy <= 2; dy++) {
            for (int dx = -STORAGE_RADIUS; dx <= STORAGE_RADIUS; dx++) {
                for (int dz = -STORAGE_RADIUS; dz <= STORAGE_RADIUS; dz++) {
                    BlockPos at = controller.offset(dx, dy, dz);
                    if (!level.hasChunkAt(at)
                            || !level.getBlockState(at).is(AncientWarfareCore.WAREHOUSE_STORAGE.get())) continue;
                    LogisticsBlockEntity stock = findStock(level, at);
                    if (stock != null) destinations.add(stock);
                }
            }
        }
        return destinations;
    }

    /** Transfer courier cargo once, scanning nearby storage only once per delivery. */
    public static int deliverCargo(ServerLevel level, BlockPos controller, AncientNpc courier) {
        List<LogisticsBlockEntity> destinations = findDestinations(level, controller);
        if (destinations.isEmpty()) return 0;
        int total = 0;
        for (int slot = 0; slot < AncientNpc.STORAGE_SLOTS; slot++) {
            ItemStack current = courier.getStorageItem(slot);
            if (current.isEmpty()) continue;
            ItemStack remaining = current.copy();
            for (LogisticsBlockEntity stock : destinations) {
                remaining = stock.insert(remaining);
                if (remaining.isEmpty()) break;
            }
            int moved = current.getCount() - remaining.getCount();
            if (moved > 0) { courier.setStorageItem(slot, remaining); total += moved; }
        }
        return total;
    }

    /** Transfer a source worksite stack to courier, recording only what actually moved. */
    public static int collect(LogisticsBlockEntity source, AncientNpc courier) {
        int moved = 0;
        for (int i = 0; i < source.getContainerSize(); i++) {
            ItemStack stack = source.getItem(i);
            if (stack.isEmpty()) continue;
            ItemStack offered = stack.copy();
            ItemStack leftover = courier.insertStorage(offered);
            int inserted = stack.getCount() - leftover.getCount();
            if (inserted > 0) {
                source.removeItem(i, inserted);
                moved += inserted;
            }
        }
        return moved;
    }
}
