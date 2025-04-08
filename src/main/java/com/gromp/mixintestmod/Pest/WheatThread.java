package com.gromp.mixintestmod.Pest;

import java.util.Random;

import com.gromp.mixintestmod.Helpers.Logger;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;

public class WheatThread extends FarmingThread {
	@Override
	public void toggleRunning() {
		running = !running;
		Logger.send("Pest farming " + (running ? "resumed" : "paused"));
		if (!running) { 
			turnThread.stopRunning();
			resetKeyBinds();
			upTime = 0;
		}
	}
	@Override
	public void debug() {
		
	}
	
	@Override
	public void run() {
		try {
			turnThread.start();
			resetFarmingAngles();
			setTurnThreadToFarmingAngles();
			while (true) {
				Minecraft mc = Minecraft.getMinecraft();
				final int tickDuration = 50;
				Thread.sleep(tickDuration);
				if (!running) {
					continue;
				}
				if (inLoadingScreen()) {
					resetKeyBinds();
					upTime = 0;
					continue;
				}
				if (mc.currentScreen != null) {
					resetKeyBinds();
					mc.addScheduledTask(() -> {
						mc.displayGuiScreen(null);
					});
					upTime = 0;
					continue;
				}
				if (!commandQueue.isEmpty()) {
					resetKeyBinds();
					CommandInfo command = commandQueue.poll();
					Thread.sleep(command.timeBefore);
					mc.thePlayer.sendChatMessage(command.command);
					Thread.sleep(command.timeAfter);
					upTime = 0;
					continue;
				}
				if (!inSkyblock()) {
					Logger.send("Not in skyblock, warping");
					commandQueue.add(new CommandInfo("/lobby", 1000, 3000));
					commandQueue.add(new CommandInfo("/play skyblock", 1000, 3000));
					continue;
				}
				if (!inGarden()) {
					Logger.send("Not in garden, warping");
					commandQueue.add(new CommandInfo("/warp garden", 1000, 3000));
					continue;
				}
				if (macroCheckType != null) {
					Logger.send("Macro check: " + macroCheckType);
					switch (macroCheckType) {
					case "Hotbar Swap":
						Thread.sleep(2000);
						resetKeyBinds();
						mc.thePlayer.inventory.currentItem = 0;
						Thread.sleep(2000);
						break;
					case "Idle":
						resetKeyBinds();
						Thread.sleep(2000);
						mc.thePlayer.sendChatMessage(macroResponseMessage());
						Thread.sleep(10000);
						break;
					case "Bedrock":
						Thread.sleep(2000);
						resetKeyBinds();
						Thread.sleep(2000);
						mc.thePlayer.sendChatMessage(macroResponseMessage());
						commandQueue.add(new CommandInfo("/warp garden", 3000, 500));
					}
					macroCheckCount++;
					macroCheckType = null;
					upTime = 0;
					continue;
				}
				upTime += 50;
				final long cropBreakInterval = 3000;
				if (upTime >= cropBreakInterval && System.currentTimeMillis() - lastCropBreakTime >= cropBreakInterval) {
					macroCheckType = "Idle";
					continue;
				}
				boolean foundPest = false;
				for (Entity e : mc.theWorld.loadedEntityList) {
					String name = e.getName();
					if ((name.equals("Bat") || name.equals("Silverfish")) && e.getDistanceToEntity(mc.thePlayer) <= 13) {
						foundPest = true;
						final int id = e.getEntityId();
						resetKeyBinds();
						turnThread.stopRunning();
						while (mc.theWorld.getEntityByID(id) != null && mc.theWorld.getEntityByID(id).getDistanceToEntity(mc.thePlayer) <= 15) {
							double x = mc.thePlayer.posX;
							double y = mc.thePlayer.posY;
							double z = mc.thePlayer.posZ;

							double pestX = e.getPositionVector().xCoord;
							double pestY = e.getPositionVector().yCoord;
							double pestZ = e.getPositionVector().zCoord;

							double dx = pestX - x, dz = pestZ - z;
							double ang = Math.atan2(dx, dz) * 180.0 / Math.PI;
							double dist = Math.sqrt(square(pestX - x) + square(pestZ - z));
							
							double targetYaw = (float)(360 - ang);
							double targetPitch = -Math.atan2(pestY - (y + 1.62), dist) * 180.0 / Math.PI;
							
							turnThread.setTargetYaw((float)targetYaw);
							turnThread.setTargetPitch((float)targetPitch);
							turnThread.startRunning();
							int vacuumHotbarSlot = -1;
							for (int i = 0; i < 9; i++) {
								ItemStack item = mc.thePlayer.inventory.getStackInSlot(i);
								if (item != null && item.getDisplayName().contains("Vacuum")) {
									vacuumHotbarSlot = i;
									break;
								}
							}
							if (vacuumHotbarSlot == -1) {
								break;
							}
							mc.thePlayer.inventory.currentItem = vacuumHotbarSlot;
							KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), true);
							Thread.sleep(10);
						}
						resetKeyBinds();
						turnThread.stopRunning();
						setTurnThreadToFarmingAngles();
						mc.thePlayer.inventory.currentItem = 0;
						break;
					}
				}
				if (foundPest) continue;
				{
					if (!mc.thePlayer.onGround) {
						KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), true);
						continue;
					}
					else {
						KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);
					}
					if (mc.thePlayer.inventory.currentItem != 0) {
						macroCheckType = "Hotbar Swap";
						continue;
					}
					double x = mc.thePlayer.posX;
					double y = mc.thePlayer.posY;
					double z = mc.thePlayer.posZ;
					if (x < -47.7 || x > 47.7 || z < -143.7 || z > -48.3 || y < 68.5 || y > 69.9) {
						macroCheckType = "Bedrock";
						continue;
					}
					

					KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), true);
					KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), true);
					turnThread.startRunning();

					if (!turnThread.onTarget()) {
						continue;
					}

					int lane = getLane(x);
					if (lane == 15 && z >= -48.4) {
						resetKeyBinds();
						commandQueue.add(new CommandInfo("/warp garden", 500, 500));
						turnThread.stopRunning();
						resetFarmingAngles();
						setTurnThreadToFarmingAngles();
						upTime = 0;
						continue;
					}
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
		KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), false);
		KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), false);

		KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);

		KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), false);
		KeyBinding.setKeyBindState(mc.gameSettings.keyBindLeft.getKeyCode(), false);
		KeyBinding.setKeyBindState(mc.gameSettings.keyBindRight.getKeyCode(), false);
	}
	
	private void resetFarmingAngles() {
		farmingYaw = rand.nextFloat() * 2 - 1 - 90;
		farmingPitch = rand.nextFloat() * 2 - 1 - 4;
	}
	
	private void setTurnThreadToFarmingAngles() {
		turnThread.setTargetYaw(farmingYaw);
		turnThread.setTargetPitch(farmingPitch);
	}
	
	private double square(double x) {
		return x * x;
	}

	private Random rand = new Random();
	private String macroCheckType = null;
	private float farmingYaw = 0, farmingPitch = 0;
}
