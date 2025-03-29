package com.gromp.mixintestmod.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.gromp.mixintestmod.Helpers.TurnHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {
	@ModifyVariable(method = "updateCameraAndRender(FJ)V", at = @At("STORE"), ordinal = 0)
	private boolean injectedFlag(boolean orig) {
		return true;
	}
	@Inject(method = "updateCameraAndRender(FJ)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/MouseHelper;mouseXYChange()V", shift = At.Shift.AFTER), cancellable = true)
	private void onUpdateCameraAndRender(float partialTicks, long nanoTime, CallbackInfo ci) {
		if (TurnHelper.dx != 0 || TurnHelper.dy != 0) {
			Minecraft.getMinecraft().mouseHelper.deltaX = TurnHelper.dx;
			Minecraft.getMinecraft().mouseHelper.deltaY = -TurnHelper.dy;
			TurnHelper.dx = 0;
			TurnHelper.dy = 0;
		}
	}
}
