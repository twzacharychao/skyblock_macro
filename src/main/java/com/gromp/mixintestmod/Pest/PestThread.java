package com.gromp.mixintestmod.Pest;

import com.gromp.mixintestmod.Helpers.Logger;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiDownloadTerrain;
import net.minecraft.client.settings.KeyBinding;

public class PestThread extends FarmingThread {
	@Override
	public void run() {
		try {
			turnThread.start();
			while (true) {
				final int tickDuration = 50;
				Thread.sleep(tickDuration);
				if (!running) {
					turnThread.stopRunning();
					continue;
				}
				if (inLoadingScreen()) {
					continue;
				}
				if (!commandQueue.isEmpty()) {
					String command = commandQueue.poll();
					Minecraft.getMinecraft().thePlayer.sendChatMessage(command);
					Thread.sleep(3000);
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
					double x = Minecraft.getMinecraft().thePlayer.posX;
					double z = Minecraft.getMinecraft().thePlayer.posZ;
					if (x > -143 || x < -240 || z < -143 || z > 143) {
						commandQueue.add("/plottp 15");
						continue;
					}
					if (Minecraft.getMinecraft().thePlayer.inventory.currentItem != 0) {
						Minecraft.getMinecraft().thePlayer.inventory.currentItem = 0;
					}
					if (!Minecraft.getMinecraft().thePlayer.onGround) {
						KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindSneak.getKeyCode(), true);
						continue;
					}
					else {
						KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindSneak.getKeyCode(), false);
					}
					int lane = getLane(x);
					turnThread.setTargetPitch(50);
					turnThread.setTargetYaw(lane % 2 == 0 ? -45 : -135);
					turnThread.startRunning();
				}
			}
		}
		catch (InterruptedException e) {
			turnThread.interrupt();
		}
	}
	
	public void debug() {
		Logger.send("Lane: " + getLane(Minecraft.getMinecraft().thePlayer.posX));
	}
	
	private int getLane(double x) {
		if (x > -149.5) return 0;
		if (x <= -234.5) return 18;
		return (int)((-x - 149.5) / 5.0) + 1;
	}
}
