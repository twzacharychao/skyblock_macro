package com.gromp.mixintestmod.Pest;

import com.gromp.mixintestmod.Helpers.Logger;

public class PestMain {
	public static void start(String farmingMode) {
		if (mainThread == null) {
			switch (farmingMode) {
				case "pest":
					mainThread = new PestThread();
					mainThread.start();
					Logger.send("Pest farming started");
					break;
				case "wheat":
					mainThread = new WheatThread();
					mainThread.start();
					Logger.send("Wheat farming started");
					break;
				default:
					Logger.send("Invalid parameter");
			}
		}
	}
	
	public static void debug() {
		mainThread.debug();
	}
	
	public static void stop() {
		if (mainThread != null) { 
			mainThread.interrupt();
			mainThread = null;
			Logger.send("Pest thread shut down");
		}
	}
	public static void toggleRunning() {
		if (mainThread != null) {
			mainThread.toggleRunning();
		}
	}
	
	private static volatile FarmingThread mainThread = null;
}
