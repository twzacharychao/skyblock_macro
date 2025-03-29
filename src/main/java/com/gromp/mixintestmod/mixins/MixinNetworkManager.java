package com.gromp.mixintestmod.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.gromp.mixintestmod.KeyboardHandler;

import io.netty.channel.ChannelHandlerContext;
import net.minecraft.client.Minecraft;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition;
import net.minecraft.network.play.client.C03PacketPlayer.C05PacketPlayerLook;
import net.minecraft.network.play.client.C03PacketPlayer.C06PacketPlayerPosLook;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.util.ChatComponentText;


@Mixin(NetworkManager.class)
public class MixinNetworkManager {
	
	@Inject(method = "channelRead0", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Packet;processPacket(Lnet/minecraft/network/INetHandler;)V", shift = At.Shift.BEFORE), cancellable = true)
	private void packetReceived(ChannelHandlerContext ctx, Packet<?> packet, CallbackInfo ci) {
		/*
		if (packet instanceof S02PacketChat) {
			Logger.send("Message in chat " + ((S02PacketChat)packet).getChatComponent().getUnformattedText());
		}
		*/
	}
	
	
	private double square(double x) {
		return x * x;
	}
	
	private void check(double x, double y, double z) {
		if (KeyboardHandler.paused) return;
		if (square(x - KeyboardHandler.getX()) 
				+ square(y - KeyboardHandler.getY()) 
				+ square(z - KeyboardHandler.getZ()) >= 9) {
			if (KeyboardHandler.stopActions()) {
				KeyboardHandler.stop();
				try {
					KeyboardHandler.walkToPumpkinFarm();
				}
				catch (InterruptedException e) {}
			}
		}
	}

	@Inject(method = "sendPacket(Lnet/minecraft/network/Packet;)V", at = @At("HEAD"), cancellable = true)
	private void sendPacket(Packet packetIn, CallbackInfo ci) throws InterruptedException {

		double x = 0, y = 0, z = 0;
		
		if (packetIn instanceof C06PacketPlayerPosLook) {
			C06PacketPlayerPosLook p = (C06PacketPlayerPosLook)packetIn;
			x = p.getPositionX();
			y = p.getPositionY();
			z = p.getPositionZ();
			check(x, y, z);
		    KeyboardHandler.setX(x);
		    KeyboardHandler.setY(y);
		    KeyboardHandler.setZ(z);
		    KeyboardHandler.setPitch(p.getPitch());
		    KeyboardHandler.setYaw(p.getYaw());
		}
		else if (packetIn instanceof C05PacketPlayerLook) {
		    KeyboardHandler.setPitch(((C05PacketPlayerLook)packetIn).getPitch());
		    KeyboardHandler.setYaw(((C05PacketPlayerLook)packetIn).getYaw());
		}
		else if (packetIn instanceof C04PacketPlayerPosition) {
			C04PacketPlayerPosition p = (C04PacketPlayerPosition)packetIn;
			x = p.getPositionX();
			y = p.getPositionY();
			z = p.getPositionZ();
			check(x, y, z);
		    KeyboardHandler.setX(x);
		    KeyboardHandler.setY(y);
		    KeyboardHandler.setZ(z);
		}
		
		/*
		boolean AutoTool = false; // temporarily disabled
		if (AutoTool && c == C07PacketPlayerDigging.class) { // AutoTool
			
			C07PacketPlayerDigging packet = (C07PacketPlayerDigging) packetIn;
			
			IBlockState blockState = Minecraft.getMinecraft().theWorld.getBlockState(packet.getPosition());
			ItemStack[] hotbar = new ItemStack[9];
			
			for (int i = 0; i < 9; i++) {
				hotbar[i] = Minecraft.getMinecraft().thePlayer.inventory.getStackInSlot(i);
			}
			
			if (packet.getStatus() == C07PacketPlayerDigging.Action.START_DESTROY_BLOCK) {
				DigTimer.mining = true;
				new DigTimer(blockState, hotbar).start();
			}
			else {
				DigTimer.mining = false;
			}
			
		}
		*/
	}
	
}
