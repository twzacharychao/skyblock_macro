package com.gromp.mixintestmod.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.gui.inventory.GuiEditSign;

@Mixin(GuiEditSign.class)
public interface GuiEditSignInvoker {
	@Invoker("keyTyped")
	public void invokeKeyTyped(char typedChar, int keyCode);
}
