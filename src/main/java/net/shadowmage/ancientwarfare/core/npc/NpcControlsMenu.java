package net.shadowmage.ancientwarfare.core.npc;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
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

import java.util.List;

/**
 * Stage 7 NPC controller. Uses the vanilla six-row chest screen deliberately:
 * no custom menu type, client renderer or new protocol packets are required.
 * Rows 1-3 are server-authoritative command buttons; rows 4-6 are a real,
 * persistent 27-slot NPC inventory.
 */
public final class NpcControlsMenu extends ChestMenu {
    private static final int CONTROL_SLOTS = 27;
    private static final int ALL_NPC_SLOTS = 54;
    private final AncientNpc npc;
    private final ControlContainer controlContainer;

    public NpcControlsMenu(int containerId, Inventory playerInventory, AncientNpc npc) {
        this(containerId, playerInventory, npc, new ControlContainer(npc));
    }

    private NpcControlsMenu(int containerId, Inventory playerInventory, AncientNpc npc, ControlContainer container) {
        super(MenuType.GENERIC_9x6, containerId, playerInventory, container, 6);
        this.npc = npc;
        this.controlContainer = container;
        npc.setControlsOpen(true);
    }

    @Override
    public boolean stillValid(Player player) {
        return npc.isAlive()
                && npc.level() == player.level()
                && npc.distanceToSqr(player) <= 64.0
                && npc.isOwnedBy(player);
    }

    @Override
    public void clicked(int slotIndex, int buttonNum, ContainerInput input, Player player) {
        if (!stillValid(player)) return;
        if (slotIndex >= 0 && slotIndex < CONTROL_SLOTS) {
            // All control slots are *virtual*. Never allow stealing, swapping,
            // throwing, dragging or cloning the displayed button items.
            if (input == ContainerInput.PICKUP && (buttonNum == 0 || buttonNum == 1)
                    && player instanceof ServerPlayer serverPlayer) {
                npc.handleControlClick(slotIndex, serverPlayer);
                controlContainer.refreshControls();
                this.broadcastChanges();
            }
            return;
        }
        super.clicked(slotIndex, buttonNum, input, player);
    }

    @Override
    public boolean canDragTo(Slot slot) {
        return this.slots.indexOf(slot) >= CONTROL_SLOTS && super.canDragTo(slot);
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        return this.slots.indexOf(slot) >= CONTROL_SLOTS
                && super.canTakeItemForPickAll(stack, slot);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        // Never use ChestMenu's default shift-click routing: it would try to
        // insert items into the virtual control rows.
        if (!stillValid(player) || slotIndex < CONTROL_SLOTS || slotIndex >= this.slots.size()) {
            return ItemStack.EMPTY;
        }
        Slot slot = this.slots.get(slotIndex);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack existing = slot.getItem();
        ItemStack original = existing.copy();
        if (slotIndex < ALL_NPC_SLOTS) {
            if (!this.moveItemStackTo(existing, ALL_NPC_SLOTS, this.slots.size(), true)) return ItemStack.EMPTY;
        } else {
            if (!this.moveItemStackTo(existing, CONTROL_SLOTS, ALL_NPC_SLOTS, false)) return ItemStack.EMPTY;
        }
        if (existing.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
        else slot.setChanged();
        controlContainer.persistStorage();
        return original;
    }

    @Override
    public void removed(Player player) {
        controlContainer.persistStorage();
        super.removed(player);
        npc.setControlsOpen(false);
    }

    private static final class ControlContainer extends SimpleContainer {
        private final AncientNpc npc;
        private boolean updating;

        private ControlContainer(AncientNpc npc) {
            super(ALL_NPC_SLOTS);
            this.npc = npc;
            this.updating = true;
            for (int slot = 0; slot < AncientNpc.STORAGE_SLOTS; slot++) {
                super.setItem(CONTROL_SLOTS + slot, npc.getStorageItem(slot).copy());
            }
            this.updating = false;
            refreshControls();
        }

        private void refreshControls() {
            this.updating = true;
            for (int slot = 0; slot < CONTROL_SLOTS; slot++) {
                super.setItem(slot, labelled(Items.GRAY_STAINED_GLASS_PANE, " ", "NPC controls above; storage below"));
            }
            setButton(0, Items.COMPASS, "Follow owner", "NPC follows you", npc.isFollowing());
            setButton(1, npc.isSoldier() ? Items.SHIELD : Items.WHEAT,
                    npc.isSoldier() ? "Guard position" : "Work at farm",
                    npc.isSoldier() ? "Defend the selected guard position" : "Start farming near the worksite",
                    !npc.isFollowing());
            setButton(2, Items.RED_BED, "Set guard/home here", "Set NPC home to your current location", false);
            if (npc.isSoldier()) {
                setButton(3, Items.IRON_SWORD, "Attack nearby monsters", "Enable or disable auto combat", npc.isCombatEnabled());
            } else {
                setButton(3, Items.COMPOSTER, "Assign nearest farm", "Find a Farm Worksite near your feet", npc.hasAssignedFarm());
            }
            setButton(4, Items.ARROW, "Decrease range", "Current range: " + npc.getTaskRadius() + " blocks", false);
            setButton(5, Items.SPECTRAL_ARROW, "Increase range", "Current range: " + npc.getTaskRadius() + " blocks", false);
            if (npc.isSoldier()) {
                setButton(6, Items.IRON_CHESTPLATE, "Hold position", "Stay at home until ordered to follow", !npc.isFollowing());
            } else {
                setButton(6, Items.BARREL, "Farming enabled", "Toggle harvesting when in work mode", npc.isFarmingEnabled());
            }
            setButton(7, Items.WRITABLE_BOOK, "NPC status", npc.statusText(), false);
            setButton(8, Items.CHEST, "NPC inventory", "27 persistent slots in the three bottom rows", false);
            setButton(13, Items.OAK_SIGN, npc.isSoldier() ? "SOLDIER CONTROLS" : "WORKER CONTROLS",
                    "Click upper buttons to give orders", false);
            setButton(22, Items.HOPPER, "STORAGE BELOW", "Shift-click to transfer items", false);
            this.updating = false;
            super.setChanged();
        }

        private void setButton(int slot, Item item, String name, String description, boolean enabled) {
            String prefix = enabled ? "[ACTIVE] " : "";
            super.setItem(slot, labelled(item, prefix + name, description));
        }

        private static ItemStack labelled(Item item, String title, String detail) {
            ItemStack stack = new ItemStack(item);
            stack.set(DataComponents.CUSTOM_NAME, Component.literal(title).withStyle(ChatFormatting.GOLD));
            stack.set(DataComponents.LORE, new ItemLore(List.of(Component.literal(detail).withStyle(ChatFormatting.GRAY))));
            return stack;
        }

        @Override
        public boolean canPlaceItem(int slot, ItemStack stack) {
            return slot >= CONTROL_SLOTS;
        }

        @Override
        public void setChanged() {
            super.setChanged();
            if (!updating && npc != null) persistStorage();
        }

        private void persistStorage() {
            for (int slot = 0; slot < AncientNpc.STORAGE_SLOTS; slot++) {
                npc.setStorageItem(slot, this.getItem(CONTROL_SLOTS + slot).copy());
            }
        }
    }
}
