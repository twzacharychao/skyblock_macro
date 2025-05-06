package com.gromp.mixintestmod.Pest;

import com.gromp.mixintestmod.Helpers.Logger;

import net.minecraft.client.Minecraft;
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
					CommandInfo command = commandQueue.poll();
					Thread.sleep(command.timeBefore);
					Minecraft.getMinecraft().thePlayer.sendChatMessage(command.command);
					Thread.sleep(command.timeAfter);
					continue;
				}
				if (inSkyblock() == -1) {
					Logger.send("Not in skyblock, warping");
					commandQueue.add(new CommandInfo("/lobby", 1000, 3000));
					commandQueue.add(new CommandInfo("/play skyblock", 1000, 3000));
					continue;
				}
				if (inGarden() == -1) {
					Logger.send("Not in garden, warping");
					commandQueue.add(new CommandInfo("/warp garden", 1000, 3000));
					continue;
				}
				{
					double x = Minecraft.getMinecraft().thePlayer.posX;
					double z = Minecraft.getMinecraft().thePlayer.posZ;
					if (x > -143 || x < -240 || z < -143 || z > 143) {
						commandQueue.add(new CommandInfo("/plottp 15", 500, 500));
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
	
	@Override
	public void debug() {
		Logger.send("Lane: " + getLane(Minecraft.getMinecraft().thePlayer.posX));
	}
	
	private int getLane(double x) {
		if (x > -149.5) return 0;
		if (x <= -234.5) return 18;
		return (int)((-x - 149.5) / 5.0) + 1;
	}
}
