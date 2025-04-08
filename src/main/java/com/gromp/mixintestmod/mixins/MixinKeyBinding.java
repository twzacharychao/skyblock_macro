package com.gromp.mixintestmod.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.gromp.mixintestmod.Pest.PestMain;

import net.minecraft.client.settings.KeyBinding;

@Mixin(KeyBinding.class)
public class MixinKeyBinding {
	@Inject(method = "setKeyBindState(IZ)V", at = @At(value = "HEAD"), cancellable = true)
	private static void onSetKeyBindState(int keyCode, boolean pressed, CallbackInfo ci) {
		if (pressed) {
			if (keyCode == 25) { // p
				PestMain.toggleRunning();
			}
			else if (keyCode == 24) { // o
			}
		}
		//Logger.send("Used key " + keyCode + " " + pressed);
	}

}
