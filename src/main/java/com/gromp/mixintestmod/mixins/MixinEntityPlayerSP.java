package com.gromp.mixintestmod.mixins;

import java.awt.AWTException;
import java.awt.event.KeyEvent;
import java.util.Collection;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.gromp.mixintestmod.KeyboardHandler;
import com.gromp.mixintestmod.MouseHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.util.ChatComponentText;

@Mixin(EntityPlayerSP.class)
public class MixinEntityPlayerSP {
	
	private void send(String s) {
		Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new ChatComponentText("[System]: " + s));
	}
	
	private double smallThreshhold = 0.2;
	private double normalThreshhold = 0.7;
	private double longThreshhold = 2.0;
	
	@Inject(method = "sendChatMessage(Ljava/lang/String;)V", at = @At("HEAD"), cancellable = true)
	private void onSendChatMessage(String message, CallbackInfo ci) throws AWTException {
		if (message.startsWith(";")) {
			String[] each = message.substring(1).split(" ");
			if (each.length < 1) {
				send("Invalid command");
				ci.cancel(); return;
			}
			String cmd = each[0];
			if (cmd.equals("turn")) {
				if (each.length != 3) {
					send("Invalid number of parameters (expected 2)");
					ci.cancel(); return;
				}
				float pitch = 0, yaw = 0;
				try {
					pitch = Float.parseFloat(each[1]);
					yaw = Float.parseFloat(each[2]);
				}
				catch (NumberFormatException e) {
					send("Invalid parameters"); ci.cancel(); return;
				}
				send("Turning to (" + pitch + " " + yaw + ")");
				KeyboardHandler.setTargetPitch(pitch);
				KeyboardHandler.setTargetYaw(yaw);
				KeyboardHandler.turn();
			}
			else if (cmd.equals("move")) {
				if (each.length != 3) {
					send("Invalid number of parameters (expected 2)");
					ci.cancel(); return;
				}
				double x = 0, z = 0;
				try {
					x = Double.parseDouble(each[1]);
					z = Double.parseDouble(each[2]);
				}
				catch (NumberFormatException e) {
					send("Invalid parameters"); ci.cancel(); return;
				}
				send("Moving to (" + x + " " + z + ")");
				KeyboardHandler.setTargetX(x);
				KeyboardHandler.setTargetZ(z);
				KeyboardHandler.move();
			}
			else if (cmd.equals("debug")) {
				NetHandlerPlayClient netHandler = Minecraft.getMinecraft().getNetHandler();
				if (netHandler != null) {
					Collection<NetworkPlayerInfo> playerList = netHandler.getPlayerInfoMap();
					if (playerList != null) {
						for (NetworkPlayerInfo playerInfo : playerList) {
							if (playerInfo == null) continue;

							String playerName = playerInfo.getGameProfile() != null ? playerInfo.getGameProfile().getName() : "Unknown";
				            String displayName = playerInfo.getDisplayName() != null ? playerInfo.getDisplayName().getUnformattedText() : "No Display Name";

				            send("Get Name: " + playerName);
				            send("Display Name: " + displayName);
						}
					}
				}
			}
			else if (cmd.equals("setspeed")) {
				if (each.length != 2) {
					send("Invalid number of parameters (expected 1)");
					ci.cancel(); return;
				}
				try {
					int speed = Integer.parseInt(each[1]);
					send("Speed set to " + speed);
					KeyboardHandler.setSpeed(speed);
				}
				catch (NumberFormatException e){
					send("Invalid speed"); ci.cancel(); return;
				}
			}
			else if (cmd.equals("farm")) {
				if (each.length != 2) {
					send("Invalid number of parameters (expected 1)");
					ci.cancel(); return;
				}
				if (each[1].equals("wheat")) {
					final int[] gaps = {4, 8, 8, 8, 8, 8, 8, 8, 8, 8, 4};
					int side = 0; // side = 0 is starting at where the start is
					double curX = 44; 
					KeyboardHandler.setTimeoutAction(KeyboardHandler.skipAction.SKIP);
					for (int i = 0; i < 11; i++) {
						if (i == 0) {
							KeyboardHandler.addRestriction(longThreshhold);
							KeyboardHandler.addMovement(curX + 0.5, -49.3, 3);
							KeyboardHandler.addTurn(70, 180, 3);
						}
						for (int j = 0; j < gaps[i]; j++) {
							if (side == 0) {
								KeyboardHandler.addRestriction(longThreshhold);
								KeyboardHandler.addMouseStart();
								KeyboardHandler.addMovement(curX + 0.5, -142.7, 20);
								KeyboardHandler.addMouseStop();
								if (j < gaps[i] - 1) {
									KeyboardHandler.addRestriction(normalThreshhold);
									KeyboardHandler.addStartShift();
									KeyboardHandler.addTurn(70, 90, 3);
									curX--;
									KeyboardHandler.addMovement(curX, -142.7, 3);
									KeyboardHandler.addTurn(70, 0, 3);
									KeyboardHandler.addStopShift();
								}
							}
							else {
								if (j < gaps[i] - 1) {
									KeyboardHandler.addRestriction(longThreshhold);
									KeyboardHandler.addMouseStart();
									KeyboardHandler.addMovement(curX + 0.5, -49.3, 20);
									KeyboardHandler.addMouseStop();
									KeyboardHandler.addRestriction(normalThreshhold);
									KeyboardHandler.addStartShift();
									KeyboardHandler.addTurn(70, 90, 3);
									curX--;
									KeyboardHandler.addMovement(curX, -49.3, 3);
									KeyboardHandler.addTurn(70, 180, 3);
									KeyboardHandler.addStopShift();
								}
								else {
									if (i == 10) {
										KeyboardHandler.addRestriction(longThreshhold);
										KeyboardHandler.addMouseStart();
										KeyboardHandler.addMovement(curX + 0.5, -46.3, 20);
										KeyboardHandler.addMouseStop();
										KeyboardHandler.addRestriction(smallThreshhold);
										KeyboardHandler.addTurn(30, -95, 3);
										KeyboardHandler.addMovement(44.7, -46.3, 120);
									}
									else {
										KeyboardHandler.addRestriction(normalThreshhold);
										KeyboardHandler.addMouseStart();
										KeyboardHandler.addMovement(curX + 0.5, -48.5, 20);
										KeyboardHandler.addMouseStop();
//										KeyboardHandler.addStartShift();
										KeyboardHandler.addRestriction(smallThreshhold);
										KeyboardHandler.addTurn(70, 90, 3);
										curX -= 2;
										KeyboardHandler.addMovement(curX + 0.3, -48.5, 3);
										KeyboardHandler.addTurn(70, 180, 3);
//										KeyboardHandler.addStopShift();
									}
								}
							}
							side ^= 1;
						}
					}
//					KeyboardHandler.processActions();
				}
				else if (each[1].equals("pumpkin")) {
					final int A = KeyEvent.VK_A, D = KeyEvent.VK_D;
					KeyboardHandler.setTimeoutAction(KeyboardHandler.skipAction.WARP);
//					KeyboardHandler.addTurn(50, -130, 10);
//					KeyboardHandler.addMovementWithoutTurning(51.7, -46.7, 10, KeyEvent.VK_W);
					int curX = 51;
					for (int i = 0; i < 19; i++) {
						if (i == 18) {
							KeyboardHandler.addTurn(50, 55, 10);
							curX += 6;
							KeyboardHandler.addMovementWithoutTurning(curX + 0.7, 46.7, 30, A);
							continue;
						}
						if (i % 2 == 0) {
							KeyboardHandler.addTurn(50, 55, 10);
							curX += 5;
							KeyboardHandler.addMovementWithoutTurning(curX + 0.7, 46.7, 30, A);
						}
						else {
							KeyboardHandler.addTurn(50, 125, 10);
							curX += 5;
							KeyboardHandler.addMovementWithoutTurning(curX + 0.7, -46.7, 30, D);
						}
					}
					for (int i = 0; i < 19; i++) {
						if (i % 2 == 0) {
							KeyboardHandler.addTurn(50, 125, 10);
							curX += i == 18 ? 0 : 5;
							KeyboardHandler.addMovementWithoutTurning(curX + 0.7, -46.7, 30, D);
						}
						else {
							KeyboardHandler.addTurn(50, 55, 10);
							curX += 5;
							KeyboardHandler.addMovementWithoutTurning(curX + 0.7, 46.7, 30, A);
						}
					}
					KeyboardHandler.addCommand("/warp garden", 3000);
//					KeyboardHandler.processActions();
				}
				else if (each[1].equals("melon")) {
					final int A = KeyEvent.VK_A, D = KeyEvent.VK_D;
					KeyboardHandler.setTimeoutAction(KeyboardHandler.skipAction.WARP);
					int curX = -147;
					for (int i = 0; i < 19; i++) {
						if (i % 2 == 0) {
							KeyboardHandler.addTurn(50, -45, 5);
							if (i < 18) curX -= 5;
							KeyboardHandler.addMovementWithoutTurning(curX - 0.7, 142.7, 45, D);
						}
						else {
							KeyboardHandler.addTurn(50, -135, 5);
							curX -= 5;
							KeyboardHandler.addMovementWithoutTurning(curX - 0.7, -142.7, 45, A);
						}
						/*
						if (i % 4 == 0) {
							KeyboardHandler.addPestRepellent();
						}
						*/
					}
					KeyboardHandler.addCommand("/warp garden", 1000);
				}
				else if (each[1].equals("ryan")) {
					final int A = KeyEvent.VK_A, D = KeyEvent.VK_D, W = KeyEvent.VK_W;
					KeyboardHandler.setTimeoutAction(KeyboardHandler.skipAction.WARP);
					int curX = -49;
					for (int i = 0; i < 13; i++) {
						KeyboardHandler.addPestRepellent();
						KeyboardHandler.addTurn(28.0f, 90.0f, 10, false);
						if (i % 2 == 0) {
							KeyboardHandler.addMovementWithoutTurning(curX - 0.7, 238.7, 100, A);
							if (i < 12) { 
								curX -= 7;
								KeyboardHandler.addMovementWithoutTurning(curX - 0.7, 238.7, 5, W);
							}
							else {
								KeyboardHandler.addCommand("/warp garden", 1000);
							}
						}
						else {
							KeyboardHandler.addMovementWithoutTurning(curX - 0.7, -238.7, 100, D);
							curX -= 7;
							KeyboardHandler.addMovementWithoutTurning(curX - 0.7, -238.7, 5, W);
						}
					}
				}
				else if (each[1].equals("start")) {
					KeyboardHandler.processActions();
				}
				else {
					send("Unknown parameter (expected wheat/pumpkin/melon/ryan/start)");
					ci.cancel(); return;
				}
			}
			else if (cmd.equals("turnwait")) {
				if (each.length != 2) {
					send("Expected 1 parameter");
					ci.cancel(); return;
				}
				KeyboardHandler.turnWait = Integer.parseInt(each[1]);
				send("Successfully set turn wait to " + KeyboardHandler.turnWait);
			}
			else if (cmd.equals("setthreshhold")) {
				if (each.length != 3) {
					send("Expected 2 parameters");
					ci.cancel(); return;
				}
				if (each[1].equals("short")) {
					smallThreshhold = Double.parseDouble(each[2]);
					send("Set short threshhold to " + smallThreshhold);
				}
				else if (each[1].equals("normal")) {
					normalThreshhold = Double.parseDouble(each[2]);
					send("Set normal threshhold to " + normalThreshhold);
				}
				else if (each[1].equals("long")) {
					longThreshhold = Double.parseDouble(each[2]);
					send("Set long threshhold to " + longThreshhold);
				}
				else {
					send("Unknown parameter (expected short/normal/long)");
				}
			}
			else if (cmd.equals("jump")) {
				ci.cancel();
				MouseHandler.jump();
				return;
			}
			else if (cmd.equals("movewithoutturning")) {
				/*
				if (each.length != 4) {
					send("Expected 3 parameters");
					ci.cancel();
					return;
				}
				double x = Double.parseDouble(each[1]);
				double z = Double.parseDouble(each[2]);
				int key = Integer.parseInt(each[3]);
				send("Moving without turning to (" + x + ", " + z + ")");
				KeyboardHandler.setTargetX(x);
				KeyboardHandler.setTargetZ(z);
				KeyboardHandler.moveWithoutTurning(key);
				*/
			}
			else if (cmd.equals("warp")) {
				try {
					KeyboardHandler.walkToPumpkinFarm();
				}
				catch (InterruptedException e) {}
			}
			else if (cmd.equals("modifyconstant")) {
				send("Set turn constant to " + Float.parseFloat(each[1]));
				KeyboardHandler.turnConstant = Float.parseFloat(each[1]);
			}
			else if (cmd.equals("clearqueue")) {
				send("Queue cleared");
				KeyboardHandler.clearQueue();
			}
			else if (cmd.equals("help")) {
				send("Available commands:\nturn (pitch, yaw)\nmove (x, z)\nsetspeed (speed)\nfarm (wheat/pumpkin/start)\nclearqueue\nmodifyconstant");
			}
			else {
				send("Unknown command (try help)");
			}
			ci.cancel();
		}
	}
}
