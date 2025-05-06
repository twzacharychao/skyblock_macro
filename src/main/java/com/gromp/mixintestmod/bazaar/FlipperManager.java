package com.gromp.mixintestmod.bazaar;

public class FlipperManager {
	public static BazaarFlipper flipper = null;
	public static void setDerpyMayor(boolean b) {
		if (flipper != null) {
			flipper.setDerpyMayor(b);
		}
	}
	public static void setShowInfo(boolean b) {
		if (flipper != null) {
			flipper.showInfo = b;
		}
	}
	public static void buy(String item, int amount) {
		if (flipper != null) {
			flipper.toBuy = item;
			flipper.toBuyAmount = amount;
		}
	}
	public static void setCanBuy(boolean b) {
		if (flipper != null) {
			flipper.canBuy = b;
		}
	}
}