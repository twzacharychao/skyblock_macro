package com.gromp.mixintestmod.Pest;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;

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
	
	protected String macroResponseMessage() {
		if (macroCheckCount == 0) {
			return "???";
		}
		else if (macroCheckCount == 1) {
			return "no way again bruh";
		}
		else if (macroCheckCount == 2) {
			return "hi again";
		}
		else {
			return "lool";
		}
	}
	
	
	protected volatile boolean running = false;
	protected Queue<CommandInfo> commandQueue = new LinkedList<>();
	protected TurnThread turnThread = new TurnThread();

	protected final long INF = (long)1e14;
	protected long lastCropBreakTime = INF;
	protected int macroCheckCount = 0;
	protected long upTime = 0;
}
