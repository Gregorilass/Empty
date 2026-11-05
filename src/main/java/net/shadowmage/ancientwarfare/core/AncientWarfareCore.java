package net.shadowmage.ancientwarfare.core;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.SpawnEggItem;
import net.shadowmage.ancientwarfare.core.npc.AWNpcRegistry;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Ancient Warfare 2 modern-port milestone 4.
 * Adds manually spawned worker and soldier NPC roles with owner/guard/follow behavior,
 * small-scale crop harvesting and monster defense. Not a complete Ancient Warfare NPC port.
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


    // Usable melee weapons: material durability/damage provided by vanilla ToolMaterial.
    public static final DeferredItem<Item> WOODEN_SPEAR = ITEMS.registerItem("wooden_spear", props -> new Item(props.sword(ToolMaterial.WOOD, 2.0F, -3.0F)));
    public static final DeferredItem<Item> STONE_SPEAR = ITEMS.registerItem("stone_spear", props -> new Item(props.sword(ToolMaterial.STONE, 2.0F, -3.0F)));
    public static final DeferredItem<Item> IRON_SPEAR = ITEMS.registerItem("iron_spear", props -> new Item(props.sword(ToolMaterial.IRON, 2.0F, -3.0F)));
    public static final DeferredItem<Item> GOLDEN_SPEAR = ITEMS.registerItem("golden_spear", props -> new Item(props.sword(ToolMaterial.GOLD, 2.0F, -3.0F)));
    public static final DeferredItem<Item> DIAMOND_SPEAR = ITEMS.registerItem("diamond_spear", props -> new Item(props.sword(ToolMaterial.DIAMOND, 2.0F, -3.0F)));
    public static final DeferredItem<Item> WOODEN_HALBERD = ITEMS.registerItem("wooden_halberd", props -> new Item(props.sword(ToolMaterial.WOOD, 3.0F, -3.2F)));
    public static final DeferredItem<Item> STONE_HALBERD = ITEMS.registerItem("stone_halberd", props -> new Item(props.sword(ToolMaterial.STONE, 3.0F, -3.2F)));
    public static final DeferredItem<Item> IRON_HALBERD = ITEMS.registerItem("iron_halberd", props -> new Item(props.sword(ToolMaterial.IRON, 3.0F, -3.2F)));
    public static final DeferredItem<Item> GOLDEN_HALBERD = ITEMS.registerItem("golden_halberd", props -> new Item(props.sword(ToolMaterial.GOLD, 3.0F, -3.2F)));
    public static final DeferredItem<Item> DIAMOND_HALBERD = ITEMS.registerItem("diamond_halberd", props -> new Item(props.sword(ToolMaterial.DIAMOND, 3.0F, -3.2F)));
    public static final DeferredItem<Item> WOODEN_LANCE = ITEMS.registerItem("wooden_lance", props -> new Item(props.sword(ToolMaterial.WOOD, 2.5F, -3.2F)));
    public static final DeferredItem<Item> STONE_LANCE = ITEMS.registerItem("stone_lance", props -> new Item(props.sword(ToolMaterial.STONE, 2.5F, -3.2F)));
    public static final DeferredItem<Item> IRON_LANCE = ITEMS.registerItem("iron_lance", props -> new Item(props.sword(ToolMaterial.IRON, 2.5F, -3.2F)));
    public static final DeferredItem<Item> GOLDEN_LANCE = ITEMS.registerItem("golden_lance", props -> new Item(props.sword(ToolMaterial.GOLD, 2.5F, -3.2F)));
    public static final DeferredItem<Item> DIAMOND_LANCE = ITEMS.registerItem("diamond_lance", props -> new Item(props.sword(ToolMaterial.DIAMOND, 2.5F, -3.2F)));
    public static final DeferredItem<Item> WOODEN_CLEAVER = ITEMS.registerItem("wooden_cleaver", props -> new Item(props.sword(ToolMaterial.WOOD, 3.5F, -2.8F)));
    public static final DeferredItem<Item> STONE_CLEAVER = ITEMS.registerItem("stone_cleaver", props -> new Item(props.sword(ToolMaterial.STONE, 3.5F, -2.8F)));
    public static final DeferredItem<Item> IRON_CLEAVER = ITEMS.registerItem("iron_cleaver", props -> new Item(props.sword(ToolMaterial.IRON, 3.5F, -2.8F)));
    public static final DeferredItem<Item> GOLDEN_CLEAVER = ITEMS.registerItem("golden_cleaver", props -> new Item(props.sword(ToolMaterial.GOLD, 3.5F, -2.8F)));
    public static final DeferredItem<Item> DIAMOND_CLEAVER = ITEMS.registerItem("diamond_cleaver", props -> new Item(props.sword(ToolMaterial.DIAMOND, 3.5F, -2.8F)));

    public static final DeferredItem<Item> WORKER_SPAWN_EGG = ITEMS.registerItem("worker_spawn_egg",
        props -> new SpawnEggItem(props.spawnEgg(AWNpcRegistry.WORKER.get())));
    public static final DeferredItem<Item> SOLDIER_SPAWN_EGG = ITEMS.registerItem("soldier_spawn_egg",
        props -> new SpawnEggItem(props.spawnEgg(AWNpcRegistry.SOLDIER.get())));

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
                output.accept(WOODEN_SPEAR.get());
                output.accept(STONE_SPEAR.get());
                output.accept(IRON_SPEAR.get());
                output.accept(GOLDEN_SPEAR.get());
                output.accept(DIAMOND_SPEAR.get());
                output.accept(WOODEN_HALBERD.get());
                output.accept(STONE_HALBERD.get());
                output.accept(IRON_HALBERD.get());
                output.accept(GOLDEN_HALBERD.get());
                output.accept(DIAMOND_HALBERD.get());
                output.accept(WOODEN_LANCE.get());
                output.accept(STONE_LANCE.get());
                output.accept(IRON_LANCE.get());
                output.accept(GOLDEN_LANCE.get());
                output.accept(DIAMOND_LANCE.get());
                output.accept(WOODEN_CLEAVER.get());
                output.accept(STONE_CLEAVER.get());
                output.accept(IRON_CLEAVER.get());
                output.accept(GOLDEN_CLEAVER.get());
                output.accept(DIAMOND_CLEAVER.get());
                output.accept(WORKER_SPAWN_EGG.get());
                output.accept(SOLDIER_SPAWN_EGG.get());
                }).build());

    public AncientWarfareCore(IEventBus modBus, ModContainer container) {
        AWNpcRegistry.ENTITIES.register(modBus);
        modBus.addListener(AWNpcRegistry::registerAttributes);
        ITEMS.register(modBus);
        TABS.register(modBus);
    }
}
