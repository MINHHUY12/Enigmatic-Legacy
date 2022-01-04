package com.integral.enigmaticlegacy.items;

import java.util.List;

import com.integral.enigmaticlegacy.EnigmaticLegacy;
import com.integral.enigmaticlegacy.api.items.ICursed;
import com.integral.enigmaticlegacy.helpers.ItemLoreHelper;
import com.integral.enigmaticlegacy.items.generic.ItemBase;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

public class EvilEssence extends ItemBase implements ICursed {

	public EvilEssence() {
		super(getDefaultProperties().rarity(Rarity.EPIC).stacksTo(8).fireResistant());
		this.setRegistryName(new ResourceLocation(EnigmaticLegacy.MODID, "evil_essence"));
	}

	@Override
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> list, TooltipFlag flagIn) {
		if (Screen.hasShiftDown()) {
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.evilEssence1");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.evilEssence2");
		} else {
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.holdShift");
		}

		ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
		ItemLoreHelper.indicateCursedOnesOnly(list);
	}

}
