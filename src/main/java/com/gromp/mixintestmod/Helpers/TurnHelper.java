package com.gromp.mixintestmod.Helpers;

public class TurnHelper { // communication between my turns and MixinEntityRenderer
	public static volatile int dx = 0; // how many pixels to move
	public static volatile int dy = 0;
	public static volatile boolean running = false;
}
