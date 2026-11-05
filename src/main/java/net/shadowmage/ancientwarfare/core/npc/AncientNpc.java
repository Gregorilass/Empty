package net.shadowmage.ancientwarfare.core.npc;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Basic server-authoritative Ancient Warfare worker/soldier, using vanilla Villager visuals.
 * This is a deliberately narrow NPC milestone, not the original faction or inventory system.
 */
public class AncientNpc extends Villager {
    private UUID ownerId;
    private boolean following = true;
    private BlockPos guardPosition = BlockPos.ZERO;
    private int tickOffset;

    public AncientNpc(EntityType<? extends Villager> type, Level level) {
        super(type, level);
        this.tickOffset = this.getRandom().nextInt(10);
        this.setPersistenceRequired();
    }

    public boolean isSoldier() {
        return this.getType() == AWNpcRegistry.SOLDIER.get();
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        if (this.level().isClientSide()) return InteractionResult.SUCCESS;

        if (ownerId == null) {
            ownerId = player.getUUID();
            following = true;
            player.sendSystemMessage(Component.literal("Ancient Warfare: NPC recruited. Following you."));
            return InteractionResult.SUCCESS;
        }
        if (!ownerId.equals(player.getUUID())) {
            player.sendSystemMessage(Component.literal("Ancient Warfare: NPC belongs to another player."));
            return InteractionResult.SUCCESS;
        }
        following = !following;
        if (!following) {
            guardPosition = blockPosition();
            this.getNavigation().stop();
        }
        player.sendSystemMessage(Component.literal(following
            ? "Ancient Warfare: Following you."
            : (isSoldier() ? "Ancient Warfare: Guarding here." : "Ancient Warfare: Farming nearby crops.")));
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        // Do not tick Villager's brain here: it would overwrite our NPC navigation orders.
        if ((level.getGameTime() + tickOffset) % 10 != 0) return;

        // Soldiers prioritize hostile mobs while in guard/follow mode.
        if (isSoldier() && fightNearbyMonster(level)) return;

        if (following) {
            followOwner(level);
        } else if (isSoldier()) {
            returnToGuard();
        } else {
            harvestNearbyCrop(level);
        }
    }

    private void followOwner(ServerLevel level) {
        if (ownerId == null) return;
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(ownerId);
        if (player == null || player.level() != level) return;
        double d2 = distanceToSqr(player);
        if (d2 > 9.0 && d2 < 48.0 * 48.0) {
            getNavigation().moveTo(player, 1.10);
        } else if (d2 <= 9.0) {
            getNavigation().stop();
        }
    }

    private boolean fightNearbyMonster(ServerLevel level) {
        Monster nearest = null;
        double closest = 12.0 * 12.0;
        for (Monster mob : level.getEntitiesOfClass(Monster.class, getBoundingBox().inflate(12.0))) {
            if (!mob.isAlive()) continue;
            double d2 = distanceToSqr(mob);
            if (d2 < closest) {
                closest = d2;
                nearest = mob;
            }
        }
        if (nearest == null) return false;
        if (closest > 5.0) {
            getNavigation().moveTo(nearest, 1.20);
        } else if ((level.getGameTime() + tickOffset) % 20 == 0) {
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

    private void harvestNearbyCrop(ServerLevel level) {
        BlockPos target = null;
        double closest = 64.0;
        // Stay within a small assigned work area. Do not cause remote chunk loading.
        for (BlockPos pos : BlockPos.betweenClosed(guardPosition.offset(-4, -1, -4), guardPosition.offset(4, 1, 4))) {
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
                Block.dropResources(mature, level, target);
                level.setBlock(target, crop.defaultBlockState(), 3);
                // Replanting consumes no seed in this initial milestone; replace with NPC inventory logic later.
            }
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        if (ownerId != null) output.putString("AWOwner", ownerId.toString());
        output.putBoolean("AWFollowing", following);
        output.putInt("AWGuardX", guardPosition.getX());
        output.putInt("AWGuardY", guardPosition.getY());
        output.putInt("AWGuardZ", guardPosition.getZ());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        ownerId = null;
        String rawOwner = input.getStringOr("AWOwner", "");
        try { if (!rawOwner.isEmpty()) ownerId = UUID.fromString(rawOwner); }
        catch (IllegalArgumentException ignored) { ownerId = null; }
        following = input.getBooleanOr("AWFollowing", true);
        guardPosition = new BlockPos(
            input.getIntOr("AWGuardX", blockPosition().getX()),
            input.getIntOr("AWGuardY", blockPosition().getY()),
            input.getIntOr("AWGuardZ", blockPosition().getZ())
        );
    }
}
