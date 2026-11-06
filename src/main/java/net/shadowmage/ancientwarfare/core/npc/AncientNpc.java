package net.shadowmage.ancientwarfare.core.npc;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.shadowmage.ancientwarfare.core.AncientWarfareCore;
import net.shadowmage.ancientwarfare.core.logistics.LogisticsBlockEntity;
import net.shadowmage.ancientwarfare.core.logistics.WarehouseNetwork;
import net.shadowmage.ancientwarfare.core.logistics.TreeFarmTask;

/**
 * Stage 7: server-owned NPC with a vanilla-screen control GUI, persistent storage,
 * configurable guard position/range, worker farm assignment, and soldier combat mode.
 * AI remains a deliberately small milestone, not the full original Ancient Warfare 2 system.
 */
public class AncientNpc extends Villager {
    public static final int STORAGE_SLOTS = 27;
    private UUID ownerId;
    private boolean following = true;
    private boolean farmingEnabled = true;
    private boolean combatEnabled = true;
    private BlockPos guardPosition = BlockPos.ZERO;
    private BlockPos assignedFarm;
    private BlockPos assignedWarehouse;
    private boolean courierEnabled = true;
    private int taskRadius = 4;
    private final NonNullList<ItemStack> storage = NonNullList.withSize(STORAGE_SLOTS, ItemStack.EMPTY);
    private int tickOffset;
    private BlockPos cachedFarm;
    private long nextFarmSearch;
    private boolean controlsOpen;
    private long nextTreeHarvestAt;

    public AncientNpc(EntityType<? extends Villager> type, Level level) {
        super(type, level);
        this.tickOffset = this.getRandom().nextInt(10);
        this.setPersistenceRequired();
    }

    public boolean isSoldier() {
        return this.getType() == AWNpcRegistry.SOLDIER.get();
    }

    public boolean isLumberjack() {
        return this.getType() == AWNpcRegistry.LUMBERJACK.get();
    }

    public boolean isCourier() {
        return this.getType() == AWNpcRegistry.COURIER.get();
    }

    public boolean isOwnedBy(Player player) {
        return ownerId != null && ownerId.equals(player.getUUID());
    }

    public boolean isFollowing() { return following; }
    public boolean isFarmingEnabled() { return farmingEnabled; }
    public boolean isCombatEnabled() { return combatEnabled; }
    public boolean isCourierEnabled() { return courierEnabled; }
    public boolean hasAssignedWarehouse() { return assignedWarehouse != null; }
    public int getTaskRadius() { return taskRadius; }
    public boolean hasAssignedFarm() { return assignedFarm != null; }

    public String statusText() {
        return (isSoldier() ? "Soldier" : isCourier() ? "Courier" : isLumberjack() ? "Lumberjack" : "Worker") + " | "
                + (following ? "Following" : isSoldier() ? "Guarding" : isCourier() ? (courierEnabled ? "Delivering" : "Paused") : farmingEnabled ? (isLumberjack() ? "Tree cutting" : "Farming") : "Idle")
                + " | range " + taskRadius
                + (assignedFarm == null ? "" : " | farm " + assignedFarm.toShortString())
                + (assignedWarehouse == null ? "" : " | warehouse " + assignedWarehouse.toShortString());
    }

    public void setControlsOpen(boolean open) {
        this.controlsOpen = open;
        if (open) this.getNavigation().stop();
    }

    public ItemStack getStorageItem(int slot) {
        return storage.get(slot);
    }

    public void setStorageItem(int slot, ItemStack stack) {
        if (slot < 0 || slot >= STORAGE_SLOTS) return;
        storage.set(slot, stack);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        if (this.level().isClientSide()) return InteractionResult.SUCCESS;

        if (ownerId == null) {
            ownerId = player.getUUID();
            following = true;
            guardPosition = blockPosition();
            player.sendSystemMessage(Component.literal("Ancient Warfare: NPC recruited. Right-click again for controls."));
            return InteractionResult.SUCCESS;
        }
        if (!isOwnedBy(player)) {
            player.sendSystemMessage(Component.literal("Ancient Warfare: NPC belongs to another player."));
            return InteractionResult.SUCCESS;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(new SimpleMenuProvider(
                    (containerId, inventory, owner) -> new NpcControlsMenu(containerId, inventory, this),
                    Component.literal(isSoldier() ? "Soldier - Orders & Storage" : isCourier() ? "Courier - Routes & Storage" : isLumberjack() ? "Lumberjack - Orders & Storage" : "Worker - Orders & Storage")));
        }
        return InteractionResult.SUCCESS;
    }

    /** Executed only after a validated owner clicks a virtual GUI button. */
    public void handleControlClick(int slot, ServerPlayer player) {
        if (!isOwnedBy(player) || distanceToSqr(player) > 64.0) return;
        String message = null;
        switch (slot) {
            case 0 -> {
                following = true;
                this.getNavigation().stop();
                message = "Following you.";
            }
            case 1, 6 -> {
                if (slot == 6 && isCourier()) {
                    courierEnabled = false;
                    following = false;
                    this.getNavigation().stop();
                    message = "Courier deliveries paused. Holding near home.";
                } else if (slot == 6 && !isSoldier()) {
                    farmingEnabled = !farmingEnabled;
                    message = farmingEnabled ? (isLumberjack() ? "Tree cutting enabled." : "Farming enabled.")
                            : (isLumberjack() ? "Tree cutting disabled." : "Farming disabled.");
                } else {
                    following = false;
                    if (isCourier()) courierEnabled = true;
                    this.getNavigation().stop();
                    message = isSoldier() ? "Guarding home position."
                            : isCourier() ? "Working assigned delivery route."
                            : isLumberjack() ? "Working at Tree Farm Worksite." : "Working near assigned farm.";
                }
            }
            case 2 -> {
                guardPosition = player.blockPosition();
                cachedFarm = null;
                nextFarmSearch = 0L;
                message = "Home position set: " + guardPosition.toShortString();
            }
            case 3 -> {
                if (isSoldier()) {
                    combatEnabled = !combatEnabled;
                    message = combatEnabled ? "Monster combat enabled." : "Monster combat disabled.";
                } else if (player.level() instanceof ServerLevel level) {
                    BlockPos worksite = findWorksiteNear(level, player.blockPosition(), 10, isCourier(), isLumberjack());
                    if (worksite == null) {
                        message = "No suitable Worksite found within 10 blocks of you.";
                    } else {
                        assignedFarm = worksite;
                        guardPosition = worksite;
                        cachedFarm = worksite;
                        nextFarmSearch = 0L;
                        following = false;
                        farmingEnabled = true;
                        message = "Assigned worksite: " + worksite.toShortString();
                    }
                }
            }
            case 4 -> {
                if (isCourier()) {
                    if (player.level() instanceof ServerLevel level) {
                        BlockPos target = findWarehouseControllerNear(level, player.blockPosition(), 12);
                        if (target != null) { assignedWarehouse = target; following = false; courierEnabled = true; }
                        message = target == null ? "No Warehouse Controller within 12 blocks of you."
                                : "Warehouse assigned: " + target.toShortString();
                    }
                } else {
                    taskRadius = Math.max(2, taskRadius - 1);
                    message = "Task range: " + taskRadius + " blocks.";
                }
            }
            case 5 -> {
                if (isCourier()) {
                    courierEnabled = !courierEnabled;
                    message = courierEnabled ? "Courier delivery enabled." : "Courier delivery paused.";
                } else {
                    taskRadius = Math.min(8, taskRadius + 1);
                    message = "Task range: " + taskRadius + " blocks.";
                }
            }
            case 7 -> message = statusText();
            case 8 -> message = isCourier()
                    ? "Courier carries 27 slots. Assign a Farm Worksite and Warehouse Controller nearby, then use Deliver."
                    : "NPC storage: 27 slots in the bottom three rows. Farmer harvests go here.";
            default -> { }
        }
        if (message != null) player.sendSystemMessage(Component.literal("Ancient Warfare: " + message));
    }

    private static BlockPos findWorksiteNear(ServerLevel level, BlockPos center, int radius, boolean courier, boolean lumberjack) {
        BlockPos nearest = null;
        double best = (double) radius * radius;
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-radius, -2, -radius), center.offset(radius, 2, radius))) {
            if (!level.hasChunkAt(pos)) continue;
            boolean matching = courier ? isDeliveryWorksite(level, pos)
                    : level.getBlockState(pos).is(lumberjack ? AncientWarfareCore.TREE_FARM_WORKSITE.get()
                            : AncientWarfareCore.FARM_WORKSITE.get());
            if (!matching) continue;
            double distance = pos.distSqr(center);
            if (distance <= best) { nearest = pos.immutable(); best = distance; }
        }
        return nearest;
    }

    private static boolean isDeliveryWorksite(ServerLevel level, BlockPos at) {
        return level.getBlockState(at).is(AncientWarfareCore.FARM_WORKSITE.get())
                || level.getBlockState(at).is(AncientWarfareCore.TREE_FARM_WORKSITE.get());
    }

    private static BlockPos findWarehouseControllerNear(ServerLevel level, BlockPos center, int radius) {
        BlockPos nearest = null;
        double best = (double) radius * radius;
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-radius, -2, -radius), center.offset(radius, 2, radius))) {
            if (!WarehouseNetwork.isController(level, pos)) continue;
            double distance = pos.distSqr(center);
            if (distance <= best) { nearest = pos.immutable(); best = distance; }
        }
        return nearest;
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        // The menu owns a live copy of inventory while open. Pause farming to
        // avoid racing inventory mutations while a player is manipulating slots.
        if (controlsOpen) return;
        if ((level.getGameTime() + tickOffset) % 10 != 0) return;
        if (isSoldier() && combatEnabled && fightNearbyMonster(level)) return;
        if (following) followOwner(level);
        else if (isSoldier()) returnToGuard();
        else if (isCourier()) {
            if (courierEnabled) runCourier(level);
            else returnToGuard();
        }
        else if (farmingEnabled && isLumberjack()) chopNearbyTree(level);
        else if (farmingEnabled) harvestNearbyCrop(level);
        else returnToGuard();
    }

    private void followOwner(ServerLevel level) {
        if (ownerId == null) return;
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(ownerId);
        if (player == null || player.level() != level) return;
        double d2 = distanceToSqr(player);
        if (d2 > 9.0 && d2 < 48.0 * 48.0) getNavigation().moveTo(player, 1.10);
        else if (d2 <= 9.0) getNavigation().stop();
    }

    private boolean fightNearbyMonster(ServerLevel level) {
        Monster nearest = null;
        double radius = 4.0 + 2.0 * taskRadius;
        double closest = radius * radius;
        for (Monster mob : level.getEntitiesOfClass(Monster.class, getBoundingBox().inflate(radius))) {
            if (!mob.isAlive()) continue;
            if (!following) {
                double dx = mob.getX() - (guardPosition.getX() + 0.5);
                double dz = mob.getZ() - (guardPosition.getZ() + 0.5);
                if (dx * dx + dz * dz > radius * radius) continue;
            }
            double d2 = distanceToSqr(mob);
            if (d2 < closest) { closest = d2; nearest = mob; }
        }
        if (nearest == null) return false;
        if (closest > 5.0) getNavigation().moveTo(nearest, 1.20);
        else if ((level.getGameTime() + tickOffset) % 20 == 0) {
            this.swingForAttack(InteractionHand.MAIN_HAND);
            nearest.hurtServer(level, level.damageSources().mobAttack(this), 5.0f);
        }
        return true;
    }

    private void returnToGuard() {
        double dx = guardPosition.getX() + 0.5 - getX();
        double dz = guardPosition.getZ() + 0.5 - getZ();
        if (dx * dx + dz * dz > 9.0) {
            getNavigation().moveTo(guardPosition.getX() + 0.5, guardPosition.getY(), guardPosition.getZ() + 0.5, 1.0);
        }
    }

    /**
     * Physical pickup and delivery. Only handles loaded, explicitly assigned blocks.
     * If the destination is absent or full, cargo stays in the courier inventory.
     */
    private void runCourier(ServerLevel level) {
        if (assignedWarehouse == null || assignedFarm == null) { returnToGuard(); return; }
        boolean carrying = !isStorageEmpty();
        BlockPos destination = carrying ? assignedWarehouse : assignedFarm;
        if (distanceToSqr(destination.getX() + 0.5, destination.getY() + 0.5,
                          destination.getZ() + 0.5) > 6.25) {
            getNavigation().moveTo(destination.getX() + 0.5, destination.getY(),
                                   destination.getZ() + 0.5, 1.10);
            return;
        }
        if (carrying) {
            WarehouseNetwork.deliverCargo(level, assignedWarehouse, this);
        } else if (level.hasChunkAt(assignedFarm)
                && isDeliveryWorksite(level, assignedFarm)) {
            LogisticsBlockEntity farm = WarehouseNetwork.findStock(level, assignedFarm);
            if (farm != null) WarehouseNetwork.collect(farm, this);
        }
    }

    private boolean isStorageEmpty() {
        for (ItemStack stack : storage) if (!stack.isEmpty()) return false;
        return true;
    }

    private void chopNearbyTree(ServerLevel level) {
        BlockPos treeFarm = nearestFarmWorksite(level);
        if (treeFarm == null) { returnToGuard(); return; }
        if (level.getGameTime() < nextTreeHarvestAt) return;
        int task = TreeFarmTask.tick(level, this, treeFarm, taskRadius);
        if (task == 2) nextTreeHarvestAt = level.getGameTime() + 100L;
        if (task == 0) returnToGuard();
    }

    private void harvestNearbyCrop(ServerLevel level) {
        BlockPos farm = nearestFarmWorksite(level);
        BlockPos center = farm == null ? guardPosition : farm;
        BlockPos target = null;
        double closest = 14.0 * 14.0;
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-taskRadius, -1, -taskRadius),
                center.offset(taskRadius, 1, taskRadius))) {
            if (!level.hasChunkAt(pos)) continue;
            BlockState state = level.getBlockState(pos);
            if (!(state.getBlock() instanceof CropBlock crop) || !crop.isMaxAge(state)) continue;
            double dx = pos.getX() + 0.5 - getX();
            double dy = pos.getY() - getY();
            double dz = pos.getZ() + 0.5 - getZ();
            double d2 = dx * dx + dy * dy + dz * dz;
            if (d2 < closest) { closest = d2; target = pos.immutable(); }
        }
        if (target == null) { returnToGuard(); return; }
        if (closest > 6.25) {
            getNavigation().moveTo(target.getX() + 0.5, target.getY(), target.getZ() + 0.5, 1.0);
        } else {
            BlockState mature = level.getBlockState(target);
            if (mature.getBlock() instanceof CropBlock crop && crop.isMaxAge(mature)) {
                // Respect vanilla loot rules: store drops in the NPC inventory,
                // and drop only the overflow on the ground (never silently delete).
                LogisticsBlockEntity stock = farm == null ? null : WarehouseNetwork.findStock(level, farm);
                for (ItemStack drop : Block.getDrops(mature, level, target, null)) {
                    ItemStack remaining = drop.copy();
                    if (stock != null) remaining = stock.insert(remaining);
                    remaining = insertStorage(remaining);
                    if (!remaining.isEmpty()) Block.popResource(level, target, remaining);
                }
                level.setBlock(target, crop.defaultBlockState(), Block.UPDATE_ALL);
                // Seed consumption will be part of the later full inventory AI.
            }
        }
    }

    public ItemStack insertStorage(ItemStack remaining) {
        if (remaining.isEmpty()) return remaining;
        for (int i = 0; i < STORAGE_SLOTS && !remaining.isEmpty(); i++) {
            ItemStack existing = storage.get(i);
            if (existing.isEmpty() || !ItemStack.isSameItemSameComponents(existing, remaining)) continue;
            int amount = Math.min(remaining.getCount(), existing.getMaxStackSize() - existing.getCount());
            if (amount <= 0) continue;
            existing.grow(amount);
            remaining.shrink(amount);
        }
        for (int i = 0; i < STORAGE_SLOTS && !remaining.isEmpty(); i++) {
            if (!storage.get(i).isEmpty()) continue;
            int amount = Math.min(remaining.getCount(), remaining.getMaxStackSize());
            storage.set(i, remaining.copyWithCount(amount));
            remaining.shrink(amount);
        }
        return remaining;
    }

    private BlockPos nearestFarmWorksite(ServerLevel level) {
        if (assignedFarm != null) {
            if (level.hasChunkAt(assignedFarm) && level.getBlockState(assignedFarm).is(isLumberjack()
                    ? AncientWarfareCore.TREE_FARM_WORKSITE.get() : AncientWarfareCore.FARM_WORKSITE.get())) {
                return assignedFarm;
            }
            assignedFarm = null;
        }
        if (cachedFarm != null && level.hasChunkAt(cachedFarm)
                && level.getBlockState(cachedFarm).is(isLumberjack()
                        ? AncientWarfareCore.TREE_FARM_WORKSITE.get() : AncientWarfareCore.FARM_WORKSITE.get())) return cachedFarm;
        cachedFarm = null;
        if (level.getGameTime() < nextFarmSearch) return null;
        nextFarmSearch = level.getGameTime() + 100;
        BlockPos best = null;
        double bestDist2 = 12.0 * 12.0;
        for (BlockPos pos : BlockPos.betweenClosed(guardPosition.offset(-8, -2, -8), guardPosition.offset(8, 2, 8))) {
            if (!level.hasChunkAt(pos) || !level.getBlockState(pos).is(isLumberjack()
                    ? AncientWarfareCore.TREE_FARM_WORKSITE.get() : AncientWarfareCore.FARM_WORKSITE.get())) continue;
            double dx = pos.getX() - guardPosition.getX();
            double dz = pos.getZ() - guardPosition.getZ();
            double d2 = dx * dx + dz * dz;
            if (d2 < bestDist2) { best = pos.immutable(); bestDist2 = d2; }
        }
        cachedFarm = best;
        return best;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
        super.dropCustomDeathLoot(level, source, killedByPlayer);
        for (int i = 0; i < STORAGE_SLOTS; i++) {
            ItemStack stack = storage.get(i);
            if (!stack.isEmpty()) Block.popResource(level, this.blockPosition(), stack.copy());
            storage.set(i, ItemStack.EMPTY);
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        if (ownerId != null) output.putString("AWOwner", ownerId.toString());
        output.putBoolean("AWFollowing", following);
        output.putBoolean("AWFarming", farmingEnabled);
        output.putBoolean("AWCombat", combatEnabled);
        output.putInt("AWTaskRadius", taskRadius);
        output.putInt("AWGuardX", guardPosition.getX());
        output.putInt("AWGuardY", guardPosition.getY());
        output.putInt("AWGuardZ", guardPosition.getZ());
        if (assignedFarm != null) {
            output.putBoolean("AWHasFarm", true);
            output.putInt("AWFarmX", assignedFarm.getX());
            output.putInt("AWFarmY", assignedFarm.getY());
            output.putInt("AWFarmZ", assignedFarm.getZ());
        } else output.putBoolean("AWHasFarm", false);
        if (assignedWarehouse != null) {
            output.putBoolean("AWHasWarehouse", true);
            output.putInt("AWWarehouseX", assignedWarehouse.getX());
            output.putInt("AWWarehouseY", assignedWarehouse.getY());
            output.putInt("AWWarehouseZ", assignedWarehouse.getZ());
        } else output.putBoolean("AWHasWarehouse", false);
        output.putBoolean("AWCourierEnabled", courierEnabled);
        ContainerHelper.saveAllItems(output.child("AWStorage"), storage);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        ownerId = null;
        String rawOwner = input.getStringOr("AWOwner", "");
        try { if (!rawOwner.isEmpty()) ownerId = UUID.fromString(rawOwner); }
        catch (IllegalArgumentException ignored) { ownerId = null; }
        following = input.getBooleanOr("AWFollowing", true);
        farmingEnabled = input.getBooleanOr("AWFarming", true);
        combatEnabled = input.getBooleanOr("AWCombat", true);
        taskRadius = Math.clamp(input.getIntOr("AWTaskRadius", 4), 2, 8);
        guardPosition = new BlockPos(
                input.getIntOr("AWGuardX", blockPosition().getX()),
                input.getIntOr("AWGuardY", blockPosition().getY()),
                input.getIntOr("AWGuardZ", blockPosition().getZ()));
        assignedFarm = input.getBooleanOr("AWHasFarm", false)
                ? new BlockPos(input.getIntOr("AWFarmX", 0), input.getIntOr("AWFarmY", 0), input.getIntOr("AWFarmZ", 0))
                : null;
        assignedWarehouse = input.getBooleanOr("AWHasWarehouse", false)
                ? new BlockPos(input.getIntOr("AWWarehouseX", 0), input.getIntOr("AWWarehouseY", 0), input.getIntOr("AWWarehouseZ", 0))
                : null;
        courierEnabled = input.getBooleanOr("AWCourierEnabled", true);
        storage.replaceAll(stack -> ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input.childOrEmpty("AWStorage"), storage);
        controlsOpen = false;
        cachedFarm = null;
        nextFarmSearch = 0;
        nextTreeHarvestAt = 0;
    }
}
