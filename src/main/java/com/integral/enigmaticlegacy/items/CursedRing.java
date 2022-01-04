package com.integral.enigmaticlegacy.items;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.integral.enigmaticlegacy.EnigmaticLegacy;
import com.integral.enigmaticlegacy.api.generic.SubscribeConfig;
import com.integral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.integral.enigmaticlegacy.helpers.ItemLoreHelper;
import com.integral.enigmaticlegacy.items.generic.ItemBaseCurio;
import com.integral.enigmaticlegacy.triggers.CursedRingEquippedTrigger;
import com.integral.omniconfig.Configuration;
import com.integral.omniconfig.wrappers.Omniconfig;
import com.integral.omniconfig.wrappers.OmniconfigWrapper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.IAngerable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.EndermanEntity;
import net.minecraft.world.entity.monster.VindicatorEntity;
import net.minecraft.world.entity.monster.piglin.AbstractPiglinEntity;
import net.minecraft.world.entity.monster.piglin.PiglinEntity;
import net.minecraft.world.entity.monster.piglin.PiglinTasks;
import net.minecraft.world.entity.passive.BeeEntity;
import net.minecraft.world.entity.passive.IronGolemEntity;
import net.minecraft.world.entity.passive.TameableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import top.theillusivec4.curios.api.type.capability.ICurio.DropRule;

public class CursedRing extends ItemBaseCurio {
	public static Omniconfig.PerhapsParameter painMultiplier;
	public static Omniconfig.PerhapsParameter monsterDamageDebuff;
	public static Omniconfig.PerhapsParameter armorDebuff;
	public static Omniconfig.PerhapsParameter experienceBonus;
	public static Omniconfig.IntParameter fortuneBonus;
	public static Omniconfig.IntParameter lootingBonus;
	public static Omniconfig.IntParameter enchantingBonus;

	public static Omniconfig.PerhapsParameter knockbackDebuff;
	public static Omniconfig.DoubleParameter neutralAngerRange;
	public static Omniconfig.DoubleParameter neutralXRayRange;
	public static Omniconfig.DoubleParameter endermenRandomportRange;
	public static Omniconfig.DoubleParameter endermenRandomportFrequency;
	public static Omniconfig.BooleanParameter saveTheBees;
	public static Omniconfig.BooleanParameter enableSpecialDrops;

	public static Omniconfig.BooleanParameter ultraHardcore;

	@SubscribeConfig
	public static void onConfig(OmniconfigWrapper builder) {
		String prevCategory = builder.getCurrentCategory();
		builder.popCategory();
		builder.pushCategory("The Seven Curses", "Config options directly affecting Ring of the Seven Curses");
		builder.pushPrefix("CursedRing");

		painMultiplier = builder
				.comment("Defines how much damage bearers of the ring receive from any source. Measured as percentage.")
				.getPerhaps("PainModifier", 200);

		monsterDamageDebuff = builder
				.comment("How much damage monsters receive from bearers of the ring will be decreased, in percents.")
				.getPerhaps("MonsterDamageDebuff", 50);

		armorDebuff = builder
				.comment("How much less effective armor will be for those who bear the ring. Measured as percetage.")
				.max(100)
				.getPerhaps("ArmorDebuff", 30);

		experienceBonus = builder
				.comment("How much experience will drop from mobs to bearers of the ring, measured in percents.")
				.getPerhaps("ExperienceBonus", 400);

		fortuneBonus = builder
				.comment("How many bonus Fortune levels ring provides")
				.getInt("FortuneBonus", 1);

		lootingBonus = builder
				.comment("How many bonus Looting levels ring provides")
				.getInt("LootingBonus", 1);

		enchantingBonus = builder
				.comment("How much additional Enchanting Power ring provides in Enchanting Table.")
				.getInt("EnchantingBonus", 10);

		enableSpecialDrops = builder
				.comment("Set to false to disable ALL special drops that can be obtained from vanilla mobs when "
						+ "bearing Ring of the Seven Curses.")
				.getBoolean("EnableSpecialDrops", true);

		ultraHardcore = builder
				.comment("If true, Ring of the Seven Curses will be equipped into player's ring slot right away when "
						+ "entering a new world, instead of just being added to their inventory.")
				.getBoolean("UltraHardcode", false);



		knockbackDebuff = builder
				.comment("How much knockback bearers of the ring take, measured in percents.")
				.getPerhaps("KnockbackDebuff", 200);

		neutralAngerRange = builder
				.comment("Range in which neutral creatures are angered against bearers of the ring.")
				.min(4)
				.getDouble("NeutralAngerRange", 24);

		neutralXRayRange = builder
				.comment("Range in which neutral creatures can see and target bearers of the ring even if they can't directly see them.")
				.getDouble("NeutralXRayRange", 4);

		endermenRandomportFrequency = builder
				.comment("Allows to adjust how frequently Endermen will try to randomly teleport to player bearing the ring, even "
						+ "if they can't see the player and are not angered yet. Lower value = less probability of this happening.")
				.min(0.01)
				.getDouble("EndermenRandomportFrequency", 1);

		endermenRandomportRange = builder
				.comment("Range in which Endermen can try to randomly teleport to bearers of the ring.")
				.min(8)
				.getDouble("EndermenRandomportRange", 32);

		builder.popCategory();
		builder.pushCategory("Save the Bees", "This category exists solely because of Jusey1z who really wanted to protect his bees."
				+ Configuration.NEW_LINE + "Btw Jusey, when I said 'very cute though', I meant you. Bees are cute either of course.");

		saveTheBees = builder
				.comment("If true, bees will never affected by the Second Curse of Ring of the Seven Curses.")
				.getBoolean("DontTouchMyBees", false);

		builder.popCategory();
		builder.popPrefix();
		builder.pushCategory(prevCategory);

	}

	public CursedRing() {
		super(ItemBaseCurio.getDefaultProperties().rarity(Rarity.EPIC));
		this.setRegistryName(new ResourceLocation(EnigmaticLegacy.MODID, "cursed_ring"));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> list, TooltipFlag flagIn) {
		ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");

		if (Screen.hasShiftDown()) {
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.cursedRing3");
			if (painMultiplier.getValue().asMultiplier() == 2.0) {
				ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.cursedRing4");
			} else {
				ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.cursedRing4_alt", ChatFormatting.GOLD, painMultiplier+"%");
			}
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.cursedRing5");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.cursedRing6", ChatFormatting.GOLD, armorDebuff+"%");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.cursedRing7", ChatFormatting.GOLD, monsterDamageDebuff+"%");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.cursedRing8");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.cursedRing9");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.cursedRing10");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.cursedRing11");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.cursedRing12", ChatFormatting.GOLD, lootingBonus);
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.cursedRing13", ChatFormatting.GOLD, fortuneBonus);
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.cursedRing14", ChatFormatting.GOLD, experienceBonus+"%");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.cursedRing15", ChatFormatting.GOLD, enchantingBonus);
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.cursedRing16");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.cursedRing17");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.cursedRing18");
		} else {
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.cursedRing1");

			if (Minecraft.getInstance().player != null && Minecraft.getInstance().player.isCreative()) {
				ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.cursedRing2_creative");
			} else {
				ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.cursedRing2");
			}

			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.holdShift");
		}

	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean canRender(String identifier, int index, LivingEntity living, ItemStack stack) {
		return false;
	}


	@Override
	public Multimap<Attribute, AttributeModifier> getAttributeModifiers(String identifier, ItemStack stack) {
		Multimap<Attribute, AttributeModifier> atrributeMap = ArrayListMultimap.create();

		atrributeMap.put(Attributes.ARMOR, new AttributeModifier(UUID.fromString("457d0ac3-69e4-482f-b636-22e0802da6bd"), "enigmaticlegacy:armor_modifier", -armorDebuff.getValue().asModifier(), AttributeModifier.Operation.MULTIPLY_TOTAL));
		atrributeMap.put(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(UUID.fromString("95e70d83-3d50-4241-a835-996e1ef039bb"), "enigmaticlegacy:armor_toughness_modifier", -armorDebuff.getValue().asModifier(), AttributeModifier.Operation.MULTIPLY_TOTAL));

		return atrributeMap;
	}

	@Override
	public boolean showAttributesTooltip(String identifier, ItemStack stack) {
		return false;
	}

	@Override
	public boolean canUnequip(String identifier, LivingEntity living, ItemStack stack) {
		if (living instanceof Player && ((Player) living).isCreative())
			return super.canUnequip(identifier, living, stack);
		else
			return false;
	}

	@Override
	public boolean canRightClickEquip(ItemStack stack) {
		return false;
	}

	@Override
	public void onUnequip(String identifier, int index, LivingEntity entityLivingBase, ItemStack stack) {
		// NO-OP
	}

	@Override
	public void onEquip(String identifier, int index, LivingEntity entityLivingBase, ItemStack stack) {
		// TODO Use Curios trigger for this
		if (entityLivingBase instanceof ServerPlayer) {
			CursedRingEquippedTrigger.INSTANCE.trigger((ServerPlayer) entityLivingBase);
		}
	}

	@Override
	public DropRule getDropRule(LivingEntity livingEntity, ItemStack stack) {
		return DropRule.ALWAYS_KEEP;
	}

	public boolean isItemDeathPersistent(ItemStack stack) {
		return stack.getItem().equals(this) || stack.getItem().equals(EnigmaticLegacy.enigmaticAmulet);
	}

	@Override
	public void inventoryTick(ItemStack stack, Level worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
		// TODO Dirty self-equipping tricks
	}

	@Override
	public void curioTick(String identifier, int index, LivingEntity livingPlayer, ItemStack stack) {
		if (livingPlayer.level.isClientSide || !(livingPlayer instanceof Player))
			return;

		Player player = (Player) livingPlayer;

		if (player.isCreative() || player.isSpectator())
			return;

		List<LivingEntity> genericMobs = livingPlayer.level.getEntitiesOfClass(LivingEntity.class, SuperpositionHandler.getBoundingBoxAroundEntity(player, neutralAngerRange.getValue()));
		List<EndermanEntity> endermen = livingPlayer.level.getEntitiesOfClass(EndermanEntity.class, SuperpositionHandler.getBoundingBoxAroundEntity(player, endermenRandomportRange.getValue()));

		for (EndermanEntity enderman : endermen) {
			if (random.nextDouble() <= (0.002 * endermenRandomportFrequency.getValue())) {
				if (enderman.teleportTowards(player) && player.canSee(enderman)) {
					enderman.setTarget(player);
				}
			}

		}

		for (LivingEntity checkedEntity : genericMobs) {
			double visibility = player.getVisibilityPercent(checkedEntity);
			double angerDistance = Math.max(neutralAngerRange.getValue() * visibility, neutralXRayRange.getValue());

			if (checkedEntity.distanceToSqr(player.getX(), player.getY(), player.getZ()) > angerDistance * angerDistance) {
				continue;
			}

			if (checkedEntity instanceof PiglinEntity && !SuperpositionHandler.hasCurio(player, EnigmaticLegacy.avariceScroll)) {
				PiglinEntity piglin = (PiglinEntity) checkedEntity;

				if (piglin.getTarget() == null || !piglin.getTarget().isAlive()) {
					if (player.canSee(checkedEntity) || player.distanceTo(checkedEntity) <= neutralXRayRange.getValue()) {
						PiglinTasks.wasHurtBy(piglin, player);
					} else {
						continue;
					}
				}

			} else if (checkedEntity instanceof IAngerable) {
				IAngerable neutral = (IAngerable) checkedEntity;

				if (neutral instanceof TameableEntity) {
					if (SuperpositionHandler.hasItem(player, EnigmaticLegacy.animalGuide) || ((TameableEntity)neutral).isTame()) {
						continue;
					}
				} else if (neutral instanceof IronGolemEntity) {
					if (((IronGolemEntity)neutral).isPlayerCreated()) {
						continue;
					}
				} else if (neutral instanceof BeeEntity) {
					if (saveTheBees.getValue() || SuperpositionHandler.hasItem(player, EnigmaticLegacy.animalGuide)) {
						continue;
					}
				}

				if (neutral.getTarget() == null || !neutral.getTarget().isAlive()) {
					if (player.canSee(checkedEntity) || player.distanceTo(checkedEntity) <= neutralXRayRange.getValue()) {
						neutral.setTarget(player);
					} else {
						continue;
					}
				}
			}
		}

	}

	@Override
	public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
		Map<Enchantment, Integer> list = EnchantmentHelper.getEnchantments(book);

		if (list.containsKey(Enchantments.VANISHING_CURSE))
			return false;
		else
			return super.isBookEnchantable(stack, book);
	}

	public double getAngerRange() {
		return neutralAngerRange.getValue();
	}

	@Override
	public int getFortuneBonus(String identifier, LivingEntity livingEntity, ItemStack curio, int index) {
		return super.getFortuneBonus(identifier, livingEntity, curio, index) + fortuneBonus.getValue();
	}

	@Override
	public int getLootingBonus(String identifier, LivingEntity livingEntity, ItemStack curio, int index) {
		return super.getLootingBonus(identifier, livingEntity, curio, index) + lootingBonus.getValue();
	}

}