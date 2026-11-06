package net.shadowmage.ancientwarfare.core.logistics;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import net.shadowmage.ancientwarfare.core.AncientWarfareCore;

import java.util.List;

/**
 * 9x6 vanilla chest menu: 45 read-only stock entries and nine navigation/info
 * buttons. Does not invent, withdraw, store or duplicate any inventory items.
 * Clients need no custom GUI or custom packet registered through Velocity.
 */
public final class WarehouseOverviewMenu extends ChestMenu {
    private static final int DISPLAY = 54;
    private static final int PER_PAGE = 45;
    private final ServerLevel level;
    private final BlockPos viewerPos;
    private final SimpleContainer display;
    private int page = 0;

    public WarehouseOverviewMenu(int id, Inventory playerInventory, ServerLevel level, BlockPos viewerPos) {
        this(id, playerInventory, level, viewerPos, new SimpleContainer(DISPLAY));
    }

    private WarehouseOverviewMenu(int id, Inventory playerInventory, ServerLevel level,
                                  BlockPos viewerPos, SimpleContainer display) {
        super(MenuType.GENERIC_9x6, id, playerInventory, display, 6);
        this.level = level;
        this.viewerPos = viewerPos.immutable();
        this.display = display;
        refresh();
    }

    @Override
    public boolean stillValid(Player player) {
        return player.level() == level
                && level.getBlockState(viewerPos).is(AncientWarfareCore.WAREHOUSE_STOCK_VIEWER.get())
                && player.distanceToSqr(viewerPos.getX() + 0.5, viewerPos.getY() + 0.5,
                                        viewerPos.getZ() + 0.5) <= 64.0;
    }

    @Override
    public void clicked(int slot, int button, ContainerInput input, Player player) {
        if (!stillValid(player)) return;
        if (slot >= 0 && slot < DISPLAY) {
            // Every displayed item is virtual. Block every item transfer route,
            // including collect-all, creative clone, dragging and shift-click.
            if (input == ContainerInput.PICKUP && player instanceof ServerPlayer serverPlayer) {
                if (slot == 45) page = Math.max(0, page - 1);
                else if (slot == 53) page++;
                else if (slot < PER_PAGE && !display.getItem(slot).isEmpty()) {
                    serverPlayer.sendSystemMessage(Component.literal(
                            "Ancient Warfare: Stock Viewer is read-only. Open Controller/Storage to move items."));
                }
                refresh();
                broadcastChanges();
            }
            return;
        }
        super.clicked(slot, button, input, player);
    }

    @Override
    public boolean canDragTo(Slot slot) {
        return this.slots.indexOf(slot) >= DISPLAY && super.canDragTo(slot);
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        return this.slots.indexOf(slot) >= DISPLAY && super.canTakeItemForPickAll(stack, slot);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        // Don't shift-click a real player item into virtual summary slots.
        return ItemStack.EMPTY;
    }

    private void refresh() {
        display.clearContent();
        BlockPos controller = WarehouseNetwork.findControllerNear(level, viewerPos, 12);
        if (controller == null) {
            display.setItem(22, labelled(Items.BARRIER, "No Warehouse Controller",
                    "Place a controller within 12 blocks / +/-2 height"));
            return;
        }
        List<WarehouseNetwork.StockEntry> stock = WarehouseNetwork.summarize(level, controller);
        int pages = Math.max(1, (stock.size() + PER_PAGE - 1) / PER_PAGE);
        page = Math.clamp(page, 0, pages - 1);
        int start = page * PER_PAGE;
        for (int i = 0; i < PER_PAGE && start + i < stock.size(); i++) {
            WarehouseNetwork.StockEntry entry = stock.get(start + i);
            ItemStack example = entry.example();
            ItemStack icon = example.copyWithCount(1);
            String name = example.getHoverName().getString();
            icon.set(DataComponents.CUSTOM_NAME, Component.literal(name + " - " + entry.count())
                    .withStyle(ChatFormatting.GOLD));
            icon.set(DataComponents.LORE, new ItemLore(List.of(
                    Component.literal("Total in loaded controller & nearby storages").withStyle(ChatFormatting.GRAY),
                    Component.literal("Read-only inventory summary").withStyle(ChatFormatting.DARK_GRAY))));
            display.setItem(i, icon);
        }
        long total = stock.stream().mapToLong(WarehouseNetwork.StockEntry::count).sum();
        display.setItem(45, labelled(Items.ARROW, "Previous page", "Click to browse stored items"));
        display.setItem(49, labelled(Items.COMPASS, "Warehouse network",
                "Page " + (page + 1) + "/" + pages + " | " + stock.size() + " types | " + total + " items"));
        display.setItem(53, labelled(Items.SPECTRAL_ARROW, "Next page", "Click to browse stored items"));
    }

    private static ItemStack labelled(Item item, String name, String desc) {
        ItemStack stack = new ItemStack(item);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(name).withStyle(ChatFormatting.GOLD));
        stack.set(DataComponents.LORE, new ItemLore(List.of(Component.literal(desc).withStyle(ChatFormatting.GRAY))));
        return stack;
    }
}
