package ru.betterend.blocks;

import java.util.List;

import com.google.common.collect.Lists;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.minecraft.block.AbstractGlassBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.client.color.block.BlockColorProvider;
import net.minecraft.client.color.item.ItemColorProvider;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;
import ru.betterend.client.render.ERenderLayer;
import ru.betterend.interfaces.IColorProvider;
import ru.betterend.interfaces.IRenderTypeable;
import ru.betterend.registry.EndItems;
import ru.betterend.util.MHelper;

public class AuroraCrystalBlock extends AbstractGlassBlock implements IRenderTypeable, IColorProvider {
	public static final Vec3i[] COLORS;
	private static final int MIN_DROP = 1;
	private static final int MAX_DROP = 4;
	
	public AuroraCrystalBlock() {
		super(FabricBlockSettings.of(Material.GLASS)
				.breakByTool(FabricToolTags.PICKAXES)
				.suffocates((state, world, pos) -> { return false; })
				.hardness(1F)
				.resistance(1F)
				.sounds(BlockSoundGroup.GLASS)
				.luminance(15)
				.nonOpaque());
	}

	@Override
	public BlockColorProvider getProvider() {
		return (state, world, pos, tintIndex) -> {
			long i = (long) pos.getX() + (long) pos.getY() + (long) pos.getZ();
			double delta = i * 0.1;
			int index = MHelper.floor(delta);
			int index2 = (index + 1) & 3;
			delta -= index;
			index &= 3;
			
			Vec3i color1 = COLORS[index];
			Vec3i color2 = COLORS[index2];
			
			int r = MHelper.floor(MathHelper.lerp(delta, color1.getX(), color2.getX()));
			int g = MHelper.floor(MathHelper.lerp(delta, color1.getY(), color2.getY()));
			int b = MHelper.floor(MathHelper.lerp(delta, color1.getZ(), color2.getZ()));
			
			return MHelper.color(r, g, b);
		};
	}

	@Override
	public ItemColorProvider getItemProvider() {
		return (stack, tintIndex) -> {
			return MHelper.color(COLORS[3].getX(), COLORS[3].getY(), COLORS[3].getZ());
		};
	}

	@Override
	public ERenderLayer getRenderLayer() {
		return ERenderLayer.TRANSLUCENT;
	}
	
	@Override
	public List<ItemStack> getDroppedStacks(BlockState state, LootContext.Builder builder) {
		ItemStack tool = builder.get(LootContextParameters.TOOL);
		if (tool != null && tool.isEffectiveOn(state)) {
			int count = 0;
			int enchant = EnchantmentHelper.getLevel(Enchantments.SILK_TOUCH, tool);
			if (enchant > 0) {
				return Lists.newArrayList(new ItemStack(this));
			}
			enchant = EnchantmentHelper.getLevel(Enchantments.FORTUNE, tool);
			if (enchant > 0) {
				int min = MathHelper.clamp(MIN_DROP + enchant, MIN_DROP, MAX_DROP);
				int max = MAX_DROP + (enchant / Enchantments.FORTUNE.getMaxLevel());
				if (min == max) {
					return Lists.newArrayList(new ItemStack(EndItems.CRYSTAL_SHARDS, max));
				}
				count = MHelper.randRange(min, max, MHelper.RANDOM);
			} else {
				count = MHelper.randRange(MIN_DROP, MAX_DROP, MHelper.RANDOM);
			}
			return Lists.newArrayList(new ItemStack(EndItems.CRYSTAL_SHARDS, count));
		}
		return Lists.newArrayList();
	}
	
	static {
		COLORS = new Vec3i[] {
			new Vec3i(247,  77, 161),
			new Vec3i(120, 184, 255),
			new Vec3i(120, 255, 168),
			new Vec3i(243,  58, 255)
		};
	}
}
