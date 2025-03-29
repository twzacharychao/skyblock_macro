package com.gromp.mixintestmod.Pest;

import com.gromp.mixintestmod.Helpers.Logger;

public class PestMain {
	public static void start() {
		if (mainThread == null) {
			mainThread = new PestThread();
			mainThread.start();
		}
		Logger.send("Pest thread activated");
	}
	public static void stop() {
		if (mainThread != null) { 
			mainThread.interrupt();
			mainThread = null;
		}
		Logger.send("Pest thread shut down");
	}
	public static void toggleRunning() {
		if (mainThread != null) {
			mainThread.toggleRunning();
		}
	}
	
	public static void debug() {
		if (mainThread != null) {
			mainThread.debug();
		}
	}

	private static volatile PestThread mainThread = null;
}
