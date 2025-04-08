package com.gromp.mixintestmod.Pest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.gromp.mixintestmod.Helpers.Logger;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiDownloadTerrain;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;

public abstract class FarmingThread extends Thread {

	public void toggleRunning() {
		running = !running;
		Logger.send("Pest farming " + (running ? "resumed" : "paused"));
	}
	
	public void debug() {
		Logger.send("Debug");
	}
	
	public void notifyBlockBreak() {
		lastCropBreakTime = System.currentTimeMillis();
	}

	protected boolean inLoadingScreen() {
		return Minecraft.getMinecraft().theWorld == null || 
				Minecraft.getMinecraft().thePlayer == null || 
				Minecraft.getMinecraft().currentScreen instanceof GuiDownloadTerrain;
		
	}
	protected boolean inSkyblock() {
		return findMatchingStringInTab("SB Level") != null;
	}
	protected boolean inGarden() {
		return findMatchingStringInTab("Garden") != null;
	}

	protected String findMatchingStringInTab(String s) {
		NetHandlerPlayClient netHandler = Minecraft.getMinecraft().getNetHandler();
		if (netHandler == null || netHandler.getPlayerInfoMap() == null) return null;
		Collection<NetworkPlayerInfo> playerList = new ArrayList<>(netHandler.getPlayerInfoMap());
		for (NetworkPlayerInfo playerInfo : playerList) {
			if (playerInfo == null) continue;
			String displayName = playerInfo.getDisplayName() != null ? playerInfo.getDisplayName().getUnformattedText() : "";
			if (displayName.contains(s)) return displayName;
		}
		return null;
	}
	
	protected String macroResponseMessage() {
		if (macroCheckCount == 0) {
			return "???";
		}
		else if (macroCheckCount == 1) {
			return "bruh";
		}
		else if (macroCheckCount == 2) {
			return "wtf";
		}
		else {
			return "lmao";
		}
	}
	
	
	protected volatile boolean running = false;
	protected volatile ConcurrentLinkedQueue<CommandInfo> commandQueue = new ConcurrentLinkedQueue<>();
	protected volatile TurnThread turnThread = new TurnThread();

	protected final long INF = (long)1e14;
	protected volatile long lastCropBreakTime = INF;
	protected volatile int macroCheckCount = 0;
	protected volatile long upTime = 0;
}
