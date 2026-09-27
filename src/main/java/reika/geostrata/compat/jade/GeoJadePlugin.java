package reika.geostrata.compat.jade;

import java.util.Locale;
import java.util.Map;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import org.apache.commons.lang3.tuple.Pair;
import reika.geostrata.GeoStrata;
import reika.geostrata.block.BlockOreVein;
import reika.geostrata.block.entity.BlockEntityOreVein;
import reika.geostrata.block.entity.BlockEntityVent;
import reika.geostrata.block.entity.BlockRFCrystal;
import reika.geostrata.block.entity.BlockRFCrystalSeed;
import reika.geostrata.registry.GeoBlocks;
import reika.geostrata.registry.GeoOptions;
import reika.geostrata.registry.RockShapes;
import reika.geostrata.registry.RockTypes;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;

/** Server-authoritative replacement for GeoStrata's old WAILA block tooltips. */
@WailaPlugin(GeoStrata.MODID)
public final class GeoJadePlugin implements IWailaPlugin {
    private static final Identifier UID = Identifier.fromNamespaceAndPath(GeoStrata.MODID, "survival_data");
    private static final String PREFIX = "geostrata_jade_";

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(ServerData.INSTANCE, Block.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(Tooltip.INSTANCE, Block.class);
    }

    private enum ServerData implements IServerDataProvider<BlockAccessor> {
        INSTANCE;

        @Override
        public Identifier getUid() { return UID; }

        @Override
        public void appendServerData(CompoundTag data, BlockAccessor accessor) {
            if (accessor.getBlockEntity() instanceof BlockEntityOreVein vein
                    && accessor.getBlock() instanceof BlockOreVein block) {
                BlockOreVein.VeinType type = block.getVeinType();
                data.putInt(key("vein_remaining"), vein.getRemainingHarvests(type));
                int i = 0;
                for (Map.Entry<ItemLike, Double> entry : type.getPotentialYields().entrySet()) {
                    data.putString(key("vein_item_" + i), BuiltInRegistries.ITEM.getKey(entry.getKey().asItem()).toString());
                    data.putDouble(key("vein_chance_" + i), entry.getValue());
                    i++;
                }
                data.putInt(key("vein_count"), i);
            }
            if (accessor.getBlockEntity() instanceof BlockRFCrystalSeed.TileRFCrystal crystal) {
                data.putLong(key("energy"), crystal.getAmountAsLong());
                data.putLong(key("capacity"), crystal.getCapacityAsLong());
                data.putBoolean(key("activated"), crystal.isActivated());
            } else if (accessor.getBlockEntity() instanceof BlockRFCrystal.TileRFCrystalAux crystal) {
                data.putLong(key("energy"), crystal.getAmountAsLong());
                data.putLong(key("capacity"), crystal.getCapacityAsLong());
                data.putBoolean(key("orphan"), crystal.getParent() == null);
            }
            if (accessor.getBlockEntity() instanceof BlockEntityVent vent) {
                data.putString(key("vent_type"), vent.getVentType().getName());
                data.putBoolean(key("vent_active"), vent.isActive());
            }
        }
    }

    private enum Tooltip implements IBlockComponentProvider {
        INSTANCE;

        @Override
        public Identifier getUid() { return UID; }

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            if (!GeoOptions.WAILA.getState()) return;
            Block block = accessor.getBlock();
            Pair<RockTypes, RockShapes> rock = GeoBlocks.blockMapping.get(block);
            if (rock == null) rock = GeoBlocks.connectedBlockMapping.get(block);
            if (rock == null) rock = GeoBlocks.stairMapping.get(block);
            if (rock == null) rock = GeoBlocks.slabMapping.get(block);
            if (rock != null) {
                RockTypes type = rock.getLeft();
                tooltip.add(Component.translatable("jade.geostrata.rock_properties",
                        String.format(Locale.ROOT, "%.2f", type.blastResistance),
                        String.format(Locale.ROOT, "%.2f", type.blockHardness)).withStyle(ChatFormatting.GRAY));
            }

            CompoundTag data = accessor.getServerData();
            if (data.contains(key("vein_remaining"))) {
                int remaining = data.getIntOr(key("vein_remaining"), 0);
                if (remaining == 0) {
                    tooltip.add(Component.translatable("jade.geostrata.depleted").withStyle(ChatFormatting.GRAY));
                } else {
                    tooltip.add(Component.translatable("jade.geostrata.potential_yields").withStyle(ChatFormatting.GRAY));
                    for (int i = 0; i < data.getIntOr(key("vein_count"), 0); i++) {
                        Identifier id = Identifier.tryParse(data.getStringOr(key("vein_item_" + i), ""));
                        if (id == null) continue;
                        Item item = BuiltInRegistries.ITEM.getValue(id);
                        int percent = (int) Math.round(100 * data.getDoubleOr(key("vein_chance_" + i), 0));
                        tooltip.add(Component.translatable("jade.geostrata.yield",
                                new ItemStack(item).getHoverName(), percent));
                    }
                    tooltip.add((remaining < 0
                            ? Component.translatable("jade.geostrata.inexhaustible")
                            : Component.translatable("jade.geostrata.remaining", remaining))
                            .withStyle(ChatFormatting.GRAY));
                }
            }
            if (data.contains(key("energy"))) {
                if (data.getBooleanOr(key("orphan"), false))
                    tooltip.add(Component.translatable("jade.geostrata.no_root").withStyle(ChatFormatting.RED));
                tooltip.add(Component.translatable("jade.geostrata.energy",
                        String.format(Locale.ROOT, "%,d", data.getLongOr(key("energy"), 0L)),
                        String.format(Locale.ROOT, "%,d", data.getLongOr(key("capacity"), 0L)))
                        .withStyle(ChatFormatting.GREEN));
                if (data.contains(key("activated")) && !data.getBooleanOr(key("activated"), false))
                    tooltip.add(Component.translatable("jade.geostrata.inactive").withStyle(ChatFormatting.GRAY));
            }
            if (data.contains(key("vent_type"))) {
                String type = data.getStringOr(key("vent_type"), "vent");
                boolean active = data.getBooleanOr(key("vent_active"), false);
                tooltip.add(Component.translatable("jade.geostrata.vent",
                                Component.translatable("jade.geostrata.vent_type." + type),
                                Component.translatable(active ? "jade.geostrata.erupting" : "jade.geostrata.dormant"))
                        .withStyle(active ? ChatFormatting.YELLOW : ChatFormatting.GRAY));
            }
        }
    }

    private static String key(String suffix) { return PREFIX + suffix; }
}
