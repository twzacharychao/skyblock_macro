package com.gromp.mixintestmod.mixins;

import java.awt.AWTException;
import java.awt.MouseInfo;
import java.awt.PointerInfo;
import java.awt.Robot;
import java.io.IOException;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.gromp.mixintestmod.KeyboardHandler;

import net.minecraft.client.Minecraft;

@Mixin(Minecraft.class)
public class MixinMinecraft {

	/*
	 * @Inject(method = "runGameLoop()V", at = @At("HEAD"), cancellable = true)
	 * private void onRunGameLoop(CallbackInfo ci) { if (Minecraft.getMinecraft() !=
	 * null && Minecraft.getMinecraft().thePlayer != null) { if
	 * (!Minecraft.getMinecraft().gameSettings.keyBindPickBlock.isKeyDown()) {
	 * AutoClicker.left.tick(false); AutoClicker.right.tick(false); } else {
	 * ItemStack holding =
	 * Minecraft.getMinecraft().thePlayer.inventory.getCurrentItem(); boolean isLeft
	 * = (holding == null) || (Block.getBlockFromItem(holding.getItem()) == null &&
	 * holding.getItem() != Items.experience_bottle); AutoClicker.left.tick(isLeft
	 * && Minecraft.getMinecraft().objectMouseOver.typeOfHit !=
	 * MovingObjectType.BLOCK); AutoClicker.right.tick(!isLeft); } } }
	 */

	/*
	 * Minecraft.runTick() ==> World.updateEntities() ==> EntityPlayerSP.onUpdate()
	 * ==> EntityPlayerSP.onUpdateWalkingPlayer() ==> sends packet1s ==>
	 * NetHandlerPlayServer.processPlayer() ==> entity.setPositionAndRotation()
	 */

	private Robot r = null;
	
//	long pestLastTime = -1;
	
	@Shadow
	private boolean isGamePaused;
	
	@Inject(method = "runTick()V", at = @At(value = "HEAD"))
    private void testRunTick(CallbackInfo info) throws IOException, AWTException {
		if (Minecraft.getMinecraft() != null && Minecraft.getMinecraft().thePlayer != null && Minecraft.getMinecraft().inGameHasFocus) {
			if (KeyboardHandler.reqDx != 0 || KeyboardHandler.reqDy != 0) {
				if (r == null) {
					try {
						r = new Robot();
					}
					catch (AWTException e) {}
				}
				PointerInfo mouse = MouseInfo.getPointerInfo();
				int mouseX = (int)mouse.getLocation().getX();
				int mouseY = (int)mouse.getLocation().getY();
				final int steps = 6;
				int nx = KeyboardHandler.reqDx / steps, ny = KeyboardHandler.reqDy / steps;
				if (nx == 0) nx = KeyboardHandler.reqDx < 0 ? -1 : 1;
				if (ny == 0) ny = KeyboardHandler.reqDy < 0 ? -1 : 1;
				int mx = min(nx, KeyboardHandler.reqDx);
				int my = min(ny, KeyboardHandler.reqDy);
				r.mouseMove(mouseX + mx, mouseY + my);
				KeyboardHandler.reqDx -= mx;
				KeyboardHandler.reqDy -= my;
			}
		}
    }
	
	@Inject(method = "runGameLoop()V", at = @At(value = "HEAD"), cancellable = true)
	private void onRunGameLoop(CallbackInfo ci) throws IOException {
		/*
		if (Minecraft.getMinecraft().thePlayer != null) {
			Logger.send("Run game loop" + " " + Minecraft.getMinecraft().skipRenderWorld);
		}
		*/
	}
	
	private int min(int x, int y) {
		return Math.abs(x) < Math.abs(y) ? x : y;
	}

//	@Inject(method = "runTick()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/settings/KeyBinding;setKeyBindState(IZ)V", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
//	private void afterKeyBindState(CallbackInfo info, int i) throws IOException {
//		if (MouseHandler.pressing) {
//			KeyBinding.setKeyBindState(-100, true);
//		}
//	}
//
//	@Shadow
//	private void clickMouse() {
//	}

	@Inject(method = "middleClickMouse()V", at = @At("HEAD"), cancellable = true)
	private void onMiddleMouseClick(CallbackInfo ci) {
		KeyboardHandler.manualPauseActions();
//		KeyboardHandler.stopActions();
		ci.cancel();
	}

}
