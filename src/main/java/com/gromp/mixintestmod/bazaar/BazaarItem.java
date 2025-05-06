package com.gromp.mixintestmod.bazaar;

public class BazaarItem {
	public String name;
	public int volume;
	public double buyPrice, sellPrice, margin, profit;
	BazaarItem(String name, double buyPrice, double sellPrice, double margin, int volume, double profit) {
		this.name = name;
		this.buyPrice = buyPrice;
		this.sellPrice = sellPrice;
		this.margin = margin;
		this.volume = volume;
		this.profit = profit;
	}
}
