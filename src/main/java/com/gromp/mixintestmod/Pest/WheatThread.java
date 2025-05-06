package com.gromp.mixintestmod.Pest;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
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
					sendChatMessage(command.command);
					Thread.sleep(command.timeAfter);
					upTime = 0;
					continue;
				}
				if (inSkyblock() == -1) {
					Logger.send("Not in skyblock, warping");
					commandQueue.add(new CommandInfo("/lobby", 1000, 3000));
					commandQueue.add(new CommandInfo("/play skyblock", 3000, 3000));
					continue;
				}
				if (inGarden() == -1) {
					Logger.send("Not in garden, warping");
					commandQueue.add(new CommandInfo("/warp garden", 1000, 3000));
					continue;
				}
				if (macroCheckType != null) {
					Logger.send("Macro check: " + macroCheckType);
					switch (macroCheckType) {
					case "Hotbar Swap":
						Thread.sleep(1500);
						resetKeyBinds();
						mc.thePlayer.inventory.currentItem = 0;
						Thread.sleep(5000);
						break;
					case "Idle":
						resetKeyBinds();
						Thread.sleep(2000);
						sendChatMessage(macroResponseMessage());
						macroCheckCount++;
						Thread.sleep(10000);
						break;
					case "Bedrock":
						Thread.sleep(2000);
						resetKeyBinds();
						Thread.sleep(2000);
						sendChatMessage(macroResponseMessage());
						macroCheckCount++;
						commandQueue.add(new CommandInfo("/warp garden", 3000, 500));
						break;
					case "Turn":
						Thread.sleep(1000);
						resetKeyBinds();
						Thread.sleep(5000);
					}
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
				if (mc.thePlayer.inventory.currentItem != 0) {
					macroCheckType = "Hotbar Swap";
					continue;
				}
				{
					double x = mc.thePlayer.posX;
					double y = mc.thePlayer.posY;
					double z = mc.thePlayer.posZ;
					if (x < -47.8 || x > 47.8 || z < -143.8 || z > -48.2 || y < 68.5 || y > 69.9) {
						macroCheckType = "Bedrock";
						continue;
					}
				}
				if (upTime >= 3000 && !turnThread.onTarget()) {
					macroCheckType = "Turn";
					continue;
				}
				boolean foundPest = false;
				List<Entity> entities = new ArrayList<>(mc.theWorld.loadedEntityList);
				for (Entity e : entities) {
					if (e == null) continue;
					String name = e.getName();
					if ((name.equals("Bat") || name.equals("Silverfish")) && e.getDistanceToEntity(mc.thePlayer) <= 13) {
						foundPest = true;
						final int id = e.getEntityId();
						resetKeyBinds();
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
						setTurnThreadToFarmingAngles();
						mc.thePlayer.inventory.currentItem = 0;
						upTime = 0;
						break;
					}
				}
				if (foundPest) { 
					continue;
				}
				mc.addScheduledTask(() -> {
					if (mc.thePlayer == null) return;
					double x = mc.thePlayer.posX;
					double z = mc.thePlayer.posZ;
					if (!mc.thePlayer.onGround) {
						KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), true);
						return;
					}
					else {
						KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);
					}

					KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), true);
					turnThread.startRunning();

					if (!turnThread.onTarget()) {
						return;
					}

					KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), true);

					int lane = getLane(x);
					if (lane == 15 && z >= -48.4) {
						resetKeyBinds();
						commandQueue.add(new CommandInfo("/warp garden", 500, 500));
						resetFarmingAngles();
						setTurnThreadToFarmingAngles();
						upTime = 0;
						return;
					}
					if (lane % 2 == 0) { // go left
						KeyBinding.setKeyBindState(mc.gameSettings.keyBindLeft.getKeyCode(), true);
						KeyBinding.setKeyBindState(mc.gameSettings.keyBindRight.getKeyCode(), false);
					}
					else { // go right
						KeyBinding.setKeyBindState(mc.gameSettings.keyBindLeft.getKeyCode(), false);
						KeyBinding.setKeyBindState(mc.gameSettings.keyBindRight.getKeyCode(), true);
					}
				});
			}
		}
		catch (InterruptedException e) {
			Logger.send("Thread interrupted");
			turnThread.interrupt();
			resetKeyBinds();
		}
		catch (Exception e) {
			Logger.send(e.toString());
			Writer writer = new StringWriter();
			e.printStackTrace(new PrintWriter(writer));
			String err = writer.toString();
			Logger.send(err);
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
		upTime = 0;
		mc.addScheduledTask(() -> {
			KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), false);
			KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), false);

			KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);

			KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), false);
			KeyBinding.setKeyBindState(mc.gameSettings.keyBindLeft.getKeyCode(), false);
			KeyBinding.setKeyBindState(mc.gameSettings.keyBindRight.getKeyCode(), false);
			turnThread.stopRunning();
		});
	}
	
	private void sendChatMessage(String msg) {
		Minecraft.getMinecraft().addScheduledTask(() -> {
			Minecraft.getMinecraft().thePlayer.sendChatMessage(msg);
		});
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
