package net.shadowmage.ancientwarfare.core;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Ancient Warfare 2 modern-port milestone 2.
 * Only 11 core material items and 10 crafting recipes are migrated from Remastered; no NPC, structure, machine,
 * vehicle, research, combat or world-gen functionality is present yet.
 */
@Mod(AncientWarfareCore.MOD_ID)
public final class AncientWarfareCore {
    public static final String MOD_ID = "ancientwarfare";
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);
    private static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);

    public static final DeferredItem<Item> STEEL_INGOT = ITEMS.registerSimpleItem("steel_ingot");
    public static final DeferredItem<Item> RESEARCH_BOOK = ITEMS.registerSimpleItem("research_book");
    public static final DeferredItem<Item> WOODEN_GEAR = ITEMS.registerSimpleItem("wooden_gear");
    public static final DeferredItem<Item> IRON_GEAR = ITEMS.registerSimpleItem("iron_gear");
    public static final DeferredItem<Item> STEEL_GEAR = ITEMS.registerSimpleItem("steel_gear");
    public static final DeferredItem<Item> WOODEN_SHAFT = ITEMS.registerSimpleItem("wooden_shaft");
    public static final DeferredItem<Item> IRON_SHAFT = ITEMS.registerSimpleItem("iron_shaft");
    public static final DeferredItem<Item> STEEL_SHAFT = ITEMS.registerSimpleItem("steel_shaft");
    public static final DeferredItem<Item> WOODEN_BEARINGS = ITEMS.registerSimpleItem("wooden_bearings");
    public static final DeferredItem<Item> IRON_BEARINGS = ITEMS.registerSimpleItem("iron_bearings");
    public static final DeferredItem<Item> STEEL_BEARINGS = ITEMS.registerSimpleItem("steel_bearings");

    private static final DeferredHolder<CreativeModeTab, CreativeModeTab> MATERIALS_TAB =
            TABS.register("materials", () -> CreativeModeTab.builder()
                .title(Component.translatable("itemGroup.ancientwarfare"))
                .icon(() -> STEEL_INGOT.get().getDefaultInstance())
                .displayItems((parameters, output) -> {
                output.accept(STEEL_INGOT.get());
                output.accept(RESEARCH_BOOK.get());
                output.accept(WOODEN_GEAR.get());
                output.accept(IRON_GEAR.get());
                output.accept(STEEL_GEAR.get());
                output.accept(WOODEN_SHAFT.get());
                output.accept(IRON_SHAFT.get());
                output.accept(STEEL_SHAFT.get());
                output.accept(WOODEN_BEARINGS.get());
                output.accept(IRON_BEARINGS.get());
                output.accept(STEEL_BEARINGS.get());
                }).build());

    public AncientWarfareCore(IEventBus modBus, ModContainer container) {
        ITEMS.register(modBus);
        TABS.register(modBus);
    }
}
