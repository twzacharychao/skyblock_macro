package com.gromp.mixintestmod.Helpers;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;

public class Logger {
	public static void send(String s) {
		Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new ChatComponentText("[System]: " + s));
	}
}
