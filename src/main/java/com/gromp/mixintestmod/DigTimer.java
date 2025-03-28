package com.gromp.mixintestmod;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class DigTimer extends Thread {
	public static boolean mining = false;
	private IBlockState blockState;
	private ItemStack[] hotbar;
	public DigTimer(IBlockState blockState, ItemStack[] hotbar) {
		this.blockState = blockState;
		this.hotbar = hotbar;
	}
	
	private float getTime(ItemStack itemStack) {
		if (itemStack == null) return 10000;
		final int efficiencyEnchID = Enchantment.efficiency.effectId;
		float speed = itemStack.getItem().getDigSpeed(itemStack, blockState);
		boolean canHarvest = speed > 1.0F;
		int effLevel = EnchantmentHelper.getEnchantmentLevel(efficiencyEnchID, itemStack);
		if (canHarvest && effLevel > 0) speed += effLevel * effLevel + 1;
		return (canHarvest ? 3.0F : 10.0F) / speed;
	}
	
	public void run() {
		try { // a little more than one tick
			Thread.sleep(60);
		} catch (InterruptedException e) {}
		
		if (mining) {
			int index = Minecraft.getMinecraft().thePlayer.inventory.currentItem;
			float least_time = getTime(hotbar[index]);
			for (int i = 0; i < 9; i++) {
				float cur_time = getTime(hotbar[i]);
				if (cur_time < least_time) {
					least_time = cur_time;
					index = i;
				}
			}
			Minecraft.getMinecraft().thePlayer.inventory.currentItem = index;
		}
	}
}
