package com.gromp.mixintestmod.Pest;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;

import com.gromp.mixintestmod.Helpers.Logger;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiDownloadTerrain;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemStack;

public class PestThread extends Thread {
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
				if (Minecraft.getMinecraft().theWorld == null || 
					Minecraft.getMinecraft().thePlayer == null || 
					Minecraft.getMinecraft().currentScreen instanceof GuiDownloadTerrain) {
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
	
	public void toggleRunning() {
		running = !running;
		Logger.send("Pest farming " + (running ? "resumed" : "paused"));
	}
	
	private int getLane(double x) {
		if (x > -149.5) return 0;
		if (x <= -234.5) return 18;
		return (int)((-x - 149.5) / 5.0) + 1;
	}
	
	private boolean inSkyblock() {
		ItemStack slotOneItem = Minecraft.getMinecraft().thePlayer.inventory.getStackInSlot(0);
		return slotOneItem != null && slotOneItem.getDisplayName().contains("Melon");
	}
	private boolean inGarden() {
		return findMatchingStringInTab("Garden") != null;
	}

	private String findMatchingStringInTab(String s) {
		NetHandlerPlayClient netHandler = Minecraft.getMinecraft().getNetHandler();
		if (netHandler == null) return null;
		Collection<NetworkPlayerInfo> playerList = netHandler.getPlayerInfoMap();
		if (playerList == null) return null;
		for (NetworkPlayerInfo playerInfo : playerList) {
			if (playerInfo == null) continue;
			String displayName = playerInfo.getDisplayName() != null ? playerInfo.getDisplayName().getUnformattedText() : "";
			if (displayName.contains(s)) return displayName;
		}
		return null;
	}
	
	private volatile boolean running = false;
	private Queue<String> commandQueue = new LinkedList<>();
	private TurnThread turnThread = new TurnThread();
}
