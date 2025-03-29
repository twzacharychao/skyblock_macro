package com.gromp.mixintestmod;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;

import com.gromp.mixintestmod.Helpers.Logger;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;

public class KeyboardHandler {
	
	private static final double defaultWalkingSpeed = 4.317;
	
	private static double x = 0, y = 0, z = 0;
	private static volatile float yaw = 0, pitch = 0;
	private static volatile float targetYaw = 0, targetPitch = 0;
	private static double targetX = 0, targetY = 0, targetZ = 0;
	private static double moveThreshhold = 0.5;
	private static int moveSpeed = 400;
	private static ArrayList<Action> actionQueue = new ArrayList();
	public static int turnWait = 100;
	
	public static float turnConstant = 1;
	
	public static void setTargetYaw(float yaw) {
		targetYaw = yaw;
	}
	public static void setTargetPitch(float pitch) {
		targetPitch = pitch;
	}
	
	public static double getX() {
		return x;
	}
	public static double getY() {
		return y;
	}
	public static double getZ() {
		return z;
	}
	
	public static void setX(double _x) {
		x = _x;
	}
	public static void setY(double _y) {
		y = _y;
	}
	public static void setZ(double _z) {
		z = _z;
	}
	public static void setYaw(float x) {
		yaw = x;
	}
	public static void setPitch(float x) {
		pitch = x;
	}
	public static void setTargetX(double x) {
		targetX = x;
	}
	public static void setTargetY(double y) {
		targetY = y;
	}
	public static void setTargetZ(double z) {
		targetZ = z;
	}
	
	public static void setSpeed(int speed) {
		moveSpeed = speed;
	}
	
	private static float normalize(float ang) {
		int num = (int)ang;
		float res = (num % 360) + (ang - num);
		if (res < 0) res += 360;
		return res;
	}
	
	private static float cube(float x) {
		return x * x * x;
	}
	
	private static int abs(int x) {
		return x < 0 ? -x : x;
	}
	
	private static volatile boolean robotTurning = false;
	private static volatile Thread turningThread;
	
	public static volatile int reqDx = 0, reqDy = 0;
	
	public static void turn() {
		
		final float sens = Minecraft.getMinecraft().gameSettings.mouseSensitivity;
		final float constant = 1.2F * cube(0.6F * sens + 0.2F) * turnConstant;
		
//		Logger.send("Turning " + pitch + " " + yaw);
		
		if (robotTurning) {
			reqDx = 0; reqDy = 0;
			turningThread.interrupt();
			robotTurning = false;
		}
		
		turningThread = new Thread(() -> {
//			try {
				yaw = normalize(yaw);
				targetYaw = normalize(targetYaw);
				float yawRight = targetYaw - yaw;
				if (yawRight < 0) yawRight += 360;
				final int dy = (int)Math.round((targetPitch - pitch) / constant);
				final int dx = (int)Math.round(yawRight <= 180.0 ? yawRight / constant : (yaw - targetYaw < 0 ? (yaw - targetYaw + 360) : yaw - targetYaw) / -constant);
				reqDx = dx; reqDy = dy;
				while (reqDx != 0 || reqDy != 0) {
					try {
						Thread.sleep(2);
					}
					catch (InterruptedException e) {}
				}
//				Thread.sleep(40);
				robotTurning = false;
//			}
//			catch (InterruptedException e) {
//				reqDx = 0; reqDy = 0;
//				robotTurning = false;
//			}
		});
		
		robotTurning = true;
		turningThread.start();
	}
	
	public static void stopTurn() {
		if (!robotTurning) return;
		reqDx = 0; reqDy = 0;
		robotTurning = false;
		turningThread.interrupt();
	}
	
	private static volatile boolean robotMoving = false;
	private static Thread moveThread;
	
	private static double square(double x) {
		return x * x;
	}
	
	
	public static void move() {
		
		if (robotMoving) {
			robotMoving = false;
			moveThread.interrupt();
		}
		
		if (movingWithoutTurning) {
			moveWithoutTurningRobot.interrupt();
		}
		
		moveThread = new Thread(() -> {
			Robot r = null;
			try {
				while (square(targetX - x) + square(targetZ - z) > moveThreshhold) {
					double distance = Math.sqrt(square(targetX - x) + square(targetZ - z));
					double waitTime = distance / (defaultWalkingSpeed * moveSpeed / 100.0);
					
					double dx = targetX - x, dz = targetZ - z;
					double ang = Math.atan2(dx, dz) * 180.0 / Math.PI;
					double targetYaw = 360 - ang;
					setTargetYaw((float)targetYaw);
					setTargetPitch(pitch);
					turn();
					
					while (robotTurning) {
						Thread.sleep(2);
					}
					
					if (distance <= 6 && !isShifting) { 
						startShifting();
						Thread.sleep(300);
					}
					
					r = new Robot();
					r.keyPress(KeyEvent.VK_W);
					Thread.currentThread().sleep(Math.min(1000L, (long)(waitTime * 500)));				
				}
			} catch (AWTException e) {}
			catch (InterruptedException e) {}
			finally {
//				stopShifting();
				if (r != null && robotMoving) { 
					r.keyRelease(KeyEvent.VK_W);
				}
				robotMoving = false;
			}
		});
		
		robotMoving = true;
		moveThread.start();
	}
	
	private static volatile Thread moveWithoutTurningRobot;
	private static volatile boolean movingWithoutTurning = false;
	
	public static void moveWithoutTurning(int key, float wantPitch, float wantYaw) {
		
		if (movingWithoutTurning) {
			moveWithoutTurningRobot.interrupt();
		}
		
		if (robotMoving) {
			moveThread.interrupt();
		}
		
		moveWithoutTurningRobot = new Thread(() -> {
			Robot r = null;
			try {
//				Minecraft.getMinecraft().thePlayer.sendChatMessage("" + running);
//				Minecraft.getMinecraft().thePlayer.sendChatMessage("Current: " + x + " " + z);
//				Minecraft.getMinecraft().thePlayer.sendChatMessage("Target: " + targetX + " " + targetZ);
				MouseHandler.start();
				while (running && Math.sqrt(square(targetX - x) + square(targetZ - z)) > 0.3) {
					double distance = Math.sqrt(square(targetX - x) + square(targetZ - z));
//					double waitTime = distance / (defaultWalkingSpeed * moveSpeed / 100.0);
					
					if (Math.abs(pitch - wantPitch) > 5 || Math.abs(yaw - wantYaw) > 5) {
						setTargetPitch(wantPitch);
						setTargetYaw(wantYaw);
						turn();
						while (robotTurning) {
							Thread.sleep(50);
						}
					}
					
					r = new Robot();
					r.keyPress(key);
//					Minecraft.getMinecraft().thePlayer.sendChatMessage("Pressing");
					Thread.sleep(distance > 13 ? 1000 : 100);
//					Minecraft.getMinecraft().thePlayer.sendChatMessage("Released");
				}
			} catch (AWTException e) {}
			catch (InterruptedException e) {}
			finally {
				if (r != null && movingWithoutTurning) {
					r.keyRelease(key);
				}
				movingWithoutTurning = false;
				MouseHandler.stop();
			}
		});
		
		movingWithoutTurning = true;
		moveWithoutTurningRobot.start();
	}
	
	
	public static void stopMoveWithoutTurning() {
		if (!movingWithoutTurning) return;
		moveWithoutTurningRobot.interrupt();
	}
	
	private static Robot shiftingRobot;
	private static boolean isShifting = false;
	static {
		try {
			shiftingRobot = new Robot();
		}
		catch (AWTException e) {}
	}
	
	public static void startShifting() {
		if (isShifting) return;
		isShifting = true;
		shiftingRobot.keyPress(KeyEvent.VK_SHIFT);
	}
	public static void stopShifting() {
		if (!isShifting) return;
		isShifting = false;
		shiftingRobot.keyRelease(KeyEvent.VK_SHIFT);
	}
	
	public static void stopMove() {
		if (!robotMoving) return;
		moveThread.interrupt();
	}
	
	public static void addTurn(float pitch, float yaw, long time) {
		actionQueue.add(new Action(pitch, yaw, 0, time));
	}
	
	public static void addTurn(float pitch, float yaw, long time, boolean noise) {
		actionQueue.add(new Action(pitch, yaw, 0, time, noise));
	}
	
	public static void addMovement(double x, double z, long time) {
		actionQueue.add(new Action(x, z, 1, time));
	}
	public static void addMouseStart() {
		actionQueue.add(new Action(0, 0, 2));
	}
	public static void addMouseStop() {
		actionQueue.add(new Action(0, 0, 3));
	}
	
	public static void addMovementWithoutTurning(double x, double z, int time, int key) {
		actionQueue.add(new Action(x, z, -key, time));
	}
	
	public static void addStartShift() {
		actionQueue.add(new Action(0, 0, 5));
	}
	
	public static void addStopShift() {
		actionQueue.add(new Action(0, 0, 6));
	}
	
	public static void addRestriction(double x) {
		actionQueue.add(new Action(x, 0, 7));
	}
	public static void addJump() {
		actionQueue.add(new Action(0, 0, 8));
	}
	
	public static void addCommand(String s, int time) {
		actionQueue.add(new Action(s, time));
	}
	
	public static void addPestRepellent() {
		actionQueue.add(new Action(0, 0, 10));
	}
	
	public static enum skipAction {
		SKIP, WARP
	}
	
	static skipAction currentMode = skipAction.SKIP;
	
	public static void setTimeoutAction(skipAction a) {
		currentMode = a;
	}
		
	private static Thread actionThread;
	private static Thread pestThread;
	private static volatile boolean running;
	public static volatile boolean paused = false;
	
	private static volatile int lastQueueIndex;
	public static volatile boolean outsidePause = false;
	public static volatile boolean afterPause = false;
	public static volatile boolean manualPause = false;
	
	private static int getNoise(int threshhold) { // returns random integer between [-threshhold, threshhold]
		return (int)(Math.random() * (2 * threshhold + 1)) - threshhold;
	}
	
	public static void pauseActions() {
		outsidePause = !outsidePause;
		if (outsidePause) {
			afterPause = true;
			stop();
			paused = true;
		}
		else {
			paused = false;
		}
	}
	
	public static void manualPauseActions() {
		pauseActions();
		manualPause = !manualPause;
		Logger.send(manualPause ? "Paused" : "Continued");
	}
	
	private static volatile float actionTargetPitch = -1, actionTargetYaw = -1;
	
	public static void processActions() {
		if (running) {
			actionThread.interrupt();
			pestThread.interrupt();
			running = false;
		}
		
		lastQueueIndex = 0;
//		Minecraft.getMinecraft().thePlayer.sendChatMessage("" + actionQueue.size());
		actionThread = new Thread(() -> {
			try {
				long lastTime = (long)1e18;
				long cutoff = 1 << 30, mul = 1000000000;
				Logger.send("Begin");
				while (running) {
					actionLoops:
					for (int i = lastQueueIndex; i < actionQueue.size(); i++) {
						Action a = actionQueue.get(i);
						if (!running) break;
						while (running) {
							if (outsidePause) {
								afterPause = true;
								Thread.sleep(100);
								continue;
							}
							else {
								if (afterPause) {
									lastTime = (long)1e18;
									afterPause = false;
									break actionLoops;
								}
							}
							{
								if (Minecraft.getMinecraft().thePlayer.inventory == null) { 
									Thread.sleep(10);
									continue;
								}
								ItemStack itemHolding = Minecraft.getMinecraft().thePlayer.inventory.getStackInSlot(0);
//								Logger.send("Before: itemHolding == null: " + (itemHolding == null));
								if (itemHolding == null) {
									Thread.sleep(3000);
									itemHolding = Minecraft.getMinecraft().thePlayer.inventory.getStackInSlot(0);
								}
//								Logger.send("itemHolding == null: " + (itemHolding == null));
								if (itemHolding == null || !itemHolding.getDisplayName().contains("Melon")) {
									Logger.send("Warped because we are not in skyblock");
									pauseActions();
									Minecraft.getMinecraft().thePlayer.sendChatMessage("/lobby");
									Thread.sleep(10000);
									Minecraft.getMinecraft().thePlayer.sendChatMessage("/play skyblock");
									Thread.sleep(10000);
									Minecraft.getMinecraft().thePlayer.sendChatMessage("/warp garden");
									Thread.sleep(10000);
									startShifting();
									Thread.sleep(1000);
									stopShifting();
									pauseActions();
									continue;
								}
							}
							if (System.nanoTime() - lastTime >= cutoff) {
								lastQueueIndex = 0;
								if (currentMode == skipAction.SKIP) { 
									Logger.send("Skipped");
									setTargetYaw(yaw);
									setTargetPitch(pitch);
									setTargetX(x);
									setTargetY(y);
									setTargetX(z);
									stopMove();
									stopTurn();
									MouseHandler.stop();
									stopShifting();
								}
								else {
									walkToPumpkinFarm();
									Thread.sleep(28000);
									lastTime = 1000000000000000000L;
									cutoff = 1 << 30;
									break actionLoops;
								}
							}

							// checks done; actual farming starts here

							lastQueueIndex = i;
							lastTime = System.nanoTime();
							cutoff = a.cutoff * mul;
							if (a.type == 0) {
//								Logger.send("At " + i);
								float newTargetPitch = (float)a.x + getNoise(10) / 10.0f;
								float newTargetYaw = (float)a.z + (a.addNoise ? getNoise(10) / 10.0f : 0);
								setTargetPitch(newTargetPitch);
								setTargetYaw(newTargetYaw);
								actionTargetPitch = newTargetPitch;
								actionTargetYaw = newTargetYaw;
								turn();
								while (robotTurning) {
									Thread.sleep(2);
								}
							}
							else if (a.type == 1) {
								setTargetX(a.x);
								setTargetZ(a.z);
								move();
								stopShifting();
							}
							else if (a.type == 2) {
//								MouseHandler.start();
							}
							else if (a.type == 3) {
//								MouseHandler.stop();
//									Thread.sleep(150);
//									stopShifting();
							}
							else if (a.type < 0) {
//								Thread.sleep(100);
//								if (Math.abs(actionTargetPitch - pitch) > 6 range || Math.abs(actionTargetYaw - yaw) > 6) {
//									setTargetPitch(actionTargetPitch);
//									setTargetYaw(actionTargetYaw);
//									turn();
//									while (robotTurning);
//								}
								setTargetX(a.x);
								setTargetZ(a.z);
								Minecraft.getMinecraft().thePlayer.inventory.currentItem = 0;
								moveWithoutTurning(-a.type, actionTargetPitch, actionTargetYaw);
							}
							else if (a.type == 5) {
								startShifting();
								Thread.sleep(300);
							}
							else if (a.type == 6) {
								stopShifting();
							}
							else if (a.type == 7) {
								moveThreshhold = a.x;
							}
							else if (a.type == 8) {
								MouseHandler.jump();
							}
							else if (a.type == 9) {
								Logger.send("sent warp garden command");
								paused = true;
								Minecraft.getMinecraft().thePlayer.sendChatMessage(a.command);
								lastTime = 1000000000000000000L;
								cutoff = 1 << 30;
								Thread.sleep(a.cutoff);
								paused = false;
								lastQueueIndex = 0;
							}
							else if (a.type == 10) {
								Logger.send("Repellent attempt");
								int orig = Minecraft.getMinecraft().thePlayer.inventory.currentItem;
								for (int j = 1; j <= 6; j++) {
									ItemStack is = Minecraft.getMinecraft().thePlayer.inventory.getStackInSlot(j);
									if (is != null && is.getDisplayName().contains("Pest")) {
										Logger.send("idx " + j + " has pest repellent");
										Minecraft.getMinecraft().thePlayer.inventory.currentItem = j;
										MouseHandler.startRightClick();
										Thread.sleep(400);
										MouseHandler.stopRightClick();
										if (Minecraft.getMinecraft().thePlayer.inventory.getStackInSlot(j) == null) {
											Logger.send("Used repellent successfully");
										}
										else Logger.send("Did not use repellent");
										break;
									}
								}
								Minecraft.getMinecraft().thePlayer.inventory.currentItem = orig;
							}
							while (running && (robotMoving || robotTurning || movingWithoutTurning) && (System.nanoTime() - lastTime < cutoff) && !outsidePause) {
								Thread.sleep(10);
							}
							break;
						}
						Thread.sleep(10);
					}
					Thread.sleep(10);
				}
			}
			catch (InterruptedException e) {}
			finally {
//				actionQueue.clear();
				outsidePause = false;
				afterPause = false;
				stop();
			}
		});
		
		pestThread = new Thread(() -> {
			long lastTime = -1, lastNotifyTime = -1;
			int targetEntity = -1;
			while (running) {
				try {
					Thread.sleep(10);
				}
				catch (InterruptedException e) {}
				if (manualPause) continue;
				long curTime = System.nanoTime();
				if (!robotTurning && targetEntity == -1 && curTime - lastTime >= (long)1e9) {
					lastTime = curTime;
					for (Entity e : Minecraft.getMinecraft().theWorld.loadedEntityList) {
						String name = e.getName();
//						if ((name.equals("Bat") || name.equals("Silverfish"))) {
//							Logger.send(e.toString());
//						}
						if ((name.equals("Bat") || name.equals("Silverfish")) && e.getDistanceToEntity(Minecraft.getMinecraft().thePlayer) <= 13.5) {
							targetEntity = e.getEntityId();
							if (!paused) pauseActions();
							DecimalFormat df = new DecimalFormat("#####0.000");
							Logger.send("Pest detected (id = " + targetEntity + ")");
							lastNotifyTime = -1;
							Minecraft.getMinecraft().thePlayer.inventory.currentItem = 7;
							MouseHandler.startRightClick();
							break;
						}
					}
				}
				if (targetEntity != -1) {
					Entity e = Minecraft.getMinecraft().theWorld.getEntityByID(targetEntity);
					if (e == null || e.getDistanceToEntity(Minecraft.getMinecraft().thePlayer) > 13.5) {
						if (e == null) Logger.send("Pest cleared (id = " + targetEntity + ")");
						MouseHandler.stopRightClick();
						Minecraft.getMinecraft().thePlayer.inventory.currentItem = 0;
						targetEntity = -1;
						setTargetPitch(actionTargetPitch);
						setTargetYaw(actionTargetYaw);
						turn();
						while (robotTurning) {
							try {
								Thread.sleep(2);
							}
							catch (InterruptedException e2) {}
						}
						if (paused) pauseActions();
					}
					else {
						if (!robotTurning) {
							double pestX = e.getPositionVector().xCoord;
							double pestY = e.getPositionVector().yCoord;
							double pestZ = e.getPositionVector().zCoord;
							long curNotifyTime = System.nanoTime();
							if (curNotifyTime - lastNotifyTime >= (long)1.5e9) {
								DecimalFormat df = new DecimalFormat("#####0.00");
								Logger.send("Pest at (" + df.format(pestX) + ", " + df.format(pestY) + ", " + df.format(pestZ) + ", D: " + df.format(e.getDistanceToEntity(Minecraft.getMinecraft().thePlayer)) + ")");
								lastNotifyTime = curNotifyTime;
							}
							
							double dx = pestX - x, dz = pestZ - z;
							double ang = Math.atan2(dx, dz) * 180.0 / Math.PI;
							double targetYaw = 360 - ang;
							
							double dist = Math.sqrt(square(pestX - x) + square(pestZ - z));
							
							setTargetYaw((float)targetYaw);
							setTargetPitch((float)(-Math.atan2(pestY - (y + 1.62), dist) * 180.0 / Math.PI));
							turn();
						}
					}
				}
			}
		});
		
		running = true;
		actionThread.start();
		pestThread.start();
	}
	public static boolean stopActions() {
		if (!running) return false;
		running = false;
		actionThread.interrupt();
		pestThread.interrupt();
		stop();
		return true;
	}
	
	public static void clearQueue() {
		actionQueue.clear();
	}
	
	public static void stop() {
		stopMove();
		stopTurn();
		stopMoveWithoutTurning();
		stopShifting();
		MouseHandler.stop();
	}
	
	public static void walkToPumpkinFarm() throws InterruptedException {
		if (manualPause) return;
		Thread walkThread = new Thread(() -> {
			try {
				paused = true;
				Logger.send("Warped");
				stop();
				Thread.sleep(3000);
				Minecraft.getMinecraft().thePlayer.sendChatMessage("/warp hub");
				Thread.sleep(10000);
				Minecraft.getMinecraft().thePlayer.sendChatMessage("/warp garden");
				Thread.sleep(10000);
				KeyboardHandler.startShifting();
				Thread.sleep(1000);
				KeyboardHandler.stopShifting();
				paused = false;
				if (!running) {
					Logger.send("Start again");
					running = true;
					processActions();
				}
			}
			catch (InterruptedException e) {}
		});
		walkThread.start();
	}
	
}
