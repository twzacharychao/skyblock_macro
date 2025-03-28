package com.gromp.mixintestmod;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.InputEvent;

import java.awt.event.KeyEvent;

import net.minecraft.client.Minecraft;

public class MouseHandler {
	
	private static Robot mouse1, mouse2;
	private static volatile boolean pressing = false, pressingRight = false;
	
	static {
		try {
			mouse1 = new Robot();
			mouse2 = new Robot();
		}
		catch (AWTException e) {}
	}
	
	public static void start() {
		if (pressing) return;
		pressing = true;
		mouse1.mousePress(InputEvent.BUTTON1_DOWN_MASK);
	}
	public static void stop() {
		if (!pressing) return;
		pressing = false;
		mouse1.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
	}
	
	public static void startRightClick() {
		if (pressingRight) return;
		pressingRight = true;
		mouse2.mousePress(InputEvent.BUTTON3_DOWN_MASK);
	}
	
	public static void stopRightClick() {
		if (!pressingRight) return;
		pressingRight = false;
		mouse2.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
	}
	
	public static void flip() {
		if (pressing) stop();
		else start();
	}
	
	public static void jump() {
		Thread thread = new Thread(() -> {
			Robot r = null;
			try {
				r = new Robot();
			}
			catch (AWTException e) {}
			try {
				Thread.sleep(100);
				r.keyPress(KeyEvent.VK_SPACE);
				Thread.sleep(100);
				r.keyRelease(KeyEvent.VK_SPACE);
			}
			catch (InterruptedException e) {}
		});
		thread.start();
	}
	
}