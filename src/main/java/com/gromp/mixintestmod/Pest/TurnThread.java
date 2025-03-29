package com.gromp.mixintestmod.Pest;

import java.util.Random;

import com.gromp.mixintestmod.Helpers.Logger;
import com.gromp.mixintestmod.Helpers.TurnHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.C03PacketPlayer;

public class TurnThread extends Thread {
	@Override
	public void run() {
		try {
			while (true) {
				Thread.sleep(50);
				if (!running) continue;
				Minecraft mc = Minecraft.getMinecraft();
				float yaw = mc.thePlayer.rotationYaw;
				float pitch = mc.thePlayer.rotationPitch;
				float yawAngleDiff = angleDiff(yaw, targetYaw);
				float pitchAngleDiff = angleDiff(pitch, targetPitch);
				float dYaw = turnAngle(yawAngleDiff);
				float dPitch = turnAngle(pitchAngleDiff);
				if (Math.abs(dYaw) > 15) dYaw += jitter();
				if (Math.abs(dPitch) > 15) dPitch += jitter();

				final float sens = Minecraft.getMinecraft().gameSettings.mouseSensitivity;
				final float constant = 1.0f / (1.2f * (float)Math.pow(0.6F * sens + 0.2f, 3));
				
				int dx = (int)(dYaw * constant);
				int dy = (int)(dPitch * constant);
				
				TurnHelper.dx = dx;
				TurnHelper.dy = dy;
			}
		}
		catch (InterruptedException e) {}
	}
	
	public void startRunning() {
		if (running) return;
		running = true;
	}

	public void stopRunning() {
		if (!running) return;
		running = false;
	}
	
	public void setTargetPitch(float pitch) { 
		this.targetPitch = pitch; 
	}
	public void setTargetYaw(float yaw) { 
		this.targetYaw = yaw; 
	}
	
	private float jitter() {
		final float jitterConstant = 1.5f;
		return (rand.nextFloat() * 2 - 1) * jitterConstant;
	}
	
	private float turnAngle(float angle) {
		final float turnConstant = 1.0f / 5;
		final float turnMin = 3.0f;
		final float turnMax = 20.0f;
		if (Math.abs(angle) < turnMin) return angle;
		return Math.signum(angle) * Math.min(turnMax, Math.abs(angle) * turnConstant);
	}

	private float angleDiff(float cur, float target) {
	    float diff = (target - cur) - (int)(target - cur) / 360 * 360;
	    while (diff < -180.0f) diff += 360.0f;
	    while (diff >= 180.0f) diff -= 360.0f;
	    return diff;
	}
	
	private final Random rand = new Random();
	
	private volatile boolean running = false;
	private volatile float targetYaw = 0, targetPitch = 0;
}
