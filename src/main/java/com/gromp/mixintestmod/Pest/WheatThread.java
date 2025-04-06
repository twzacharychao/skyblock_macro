package com.gromp.mixintestmod.Pest;

import java.util.Random;

import com.gromp.mixintestmod.Helpers.Logger;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;

public class WheatThread extends FarmingThread {
	@Override
	public void toggleRunning() {
		running = !running;
		Logger.send("Pest farming " + (running ? "resumed" : "paused"));
		if (!running) { 
			turnThread.stopRunning();
			resetKeyBinds();
		}
	}
	@Override
	public void run() {
		try {
			turnThread.start();
			turnThread.setTargetPitch(rand.nextFloat() * 2 - 1 - 4);
			turnThread.setTargetYaw(rand.nextFloat() * 2 - 1 - 90);
			while (true) {
				final int tickDuration = 50;
				Thread.sleep(tickDuration);
				if (!running) {
					continue;
				}
				if (inLoadingScreen()) {
					continue;
				}
				if (!commandQueue.isEmpty()) {
					String command = commandQueue.poll();
					Thread.sleep(1000);
					Minecraft.getMinecraft().thePlayer.sendChatMessage(command);
					Thread.sleep(1000);
					continue;
				}
				if (!inSkyblock()) {
					Logger.send("Not in skyblock, warping");
					commandQueue.add("/lobby");
					commandQueue.add("/play skyblock");
					continue;
				}
				if (!inGarden()) {
					Logger.send("Not in garden, warping");
					commandQueue.add("/warp garden");
					continue;
				}
				{
					Minecraft mc = Minecraft.getMinecraft();
					if (!mc.thePlayer.onGround) {
						KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), true);
						continue;
					}
					else {
						KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);
					}
					if (!Minecraft.getMinecraft().thePlayer.inventory.getStackInSlot(0).getDisplayName().contains("Wheat Hoe")) {
						macroCheck();
						continue;
					}
					double x = mc.thePlayer.posX;
					double z = mc.thePlayer.posZ;
					if (x < -47.7 || x > 47.7 || z < -143.7 || z > -48.3) {
						Logger.send("Invalid position");
						commandQueue.add("/warp garden");
						continue;
					}
					
					turnThread.startRunning();
					if (!turnThread.onTarget()) {
						continue;
					}

					int lane = getLane(x);
					if (lane == 15 && z >= -48.4) {
						resetKeyBinds();
						commandQueue.add("/warp garden");
						turnThread.stopRunning();
						turnThread.setTargetPitch(rand.nextFloat() * 2 - 1 - 4);
						turnThread.setTargetYaw(rand.nextFloat() * 2 - 1 - 90);
						continue;
					}
					KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), true);
					KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), true);
					if (lane % 2 == 0) { // go left
						KeyBinding.setKeyBindState(mc.gameSettings.keyBindLeft.getKeyCode(), true);
						KeyBinding.setKeyBindState(mc.gameSettings.keyBindRight.getKeyCode(), false);
					}
					else { // go right
						KeyBinding.setKeyBindState(mc.gameSettings.keyBindLeft.getKeyCode(), false);
						KeyBinding.setKeyBindState(mc.gameSettings.keyBindRight.getKeyCode(), true);
					}
				}
			}
		}
		catch (InterruptedException e) {
			turnThread.interrupt();
			resetKeyBinds();
		}
	}
	private int getLane(double x) {
		if (x < -41.5) return 0;
		if (x >= 42.5) return 15;
		return (int)((41.5 + x) / 6) + 1;
	}
	private void resetKeyBinds() {
		Minecraft mc = Minecraft.getMinecraft();
		KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), false);
		KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), false);
		KeyBinding.setKeyBindState(mc.gameSettings.keyBindLeft.getKeyCode(), false);
		KeyBinding.setKeyBindState(mc.gameSettings.keyBindRight.getKeyCode(), false);
	}
	
	private Random rand = new Random();
}
