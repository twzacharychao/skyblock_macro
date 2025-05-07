package com.gromp.mixintestmod.bazaar;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import com.gromp.mixintestmod.Helpers.Logger;
import com.gromp.mixintestmod.mixins.GuiEditSignInvoker;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiEditSign;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.Slot;

public class BazaarFlipper {
	public BazaarFlipper() {
		thread = new Thread(() -> {
			try {
				monitorOrderFillThread = new Thread(() -> {
					try {
						while (true) {
							Thread.sleep(100);
							if (!running) continue;
							Minecraft mc = Minecraft.getMinecraft();
							if (mc.thePlayer.openContainer instanceof ContainerChest && getChestName().equals("Your Bazaar Orders")) {
								for (Slot slot : mc.thePlayer.openContainer.inventorySlots) {
									if (slot != null && slot.getHasStack()) {
										List<String> tooltip = slot.getStack().getTooltip(mc.thePlayer, false);
										for (String s : tooltip) {
											s = formatString(s);
											if (s.length() >= 6 && s.substring(0, 6).equals("Filled") && s.contains("100%")) {
												String name = formatString(slot.getStack().getDisplayName());
												if (name.length() >= 3 && name.substring(0, 3).equals("BUY")) {
													filledItemType = "BUY";
													filledItemName = name.substring(4);
													Logger.send("Filled");
													break;
												}
												else if (name.length() >= 4 && name.substring(0, 4).equals("SELL")) {
													filledItemType = "SELL";
													filledItemName = name.substring(5);
													Logger.send("Filled");
													break;
												}
											}
										}
										if (filledItemType.length() > 0) break;
									}
								}
							}
						}
					}
					catch (Exception e) {
						Logger.send("Crashed");
						Logger.send(e.toString());
						Writer writer = new StringWriter();
						e.printStackTrace(new PrintWriter(writer));
						String err = writer.toString();
						Logger.send(err);
					}
				});
				monitorOrderFillThread.start();
				int lastBuyStep = 0, lastSellStep = 0, lastCancelBuyStep = 0, lastCancelSellStep = 0, equalToLastCount = 0, lastInitStep = 0;
				while (true) {
					Thread.sleep((int)(rand.nextDouble() * 200) + 250);
					if (!running) continue;
					Minecraft mc = Minecraft.getMinecraft();
					if (findMatchingStringInTab("Area") != 1) { // not in skyblock
						Logger.send("Not in skyblock");
						mc.thePlayer.sendChatMessage("/lobby");
						Thread.sleep(2000);
						mc.thePlayer.sendChatMessage("/play skyblock");
						Thread.sleep(2000);
						continue;
					}
					String dataPath = "C:\\Users\\zacha\\Desktop\\VS Code\\Python\\BazaarScraper\\data.txt";

					List<String> lines = null;
					for (int attempts = 0; attempts < 10 && lines == null; attempts++) {
						try {
							lines = Files.readAllLines(Paths.get(dataPath));
						}
						catch (IOException e) {}
					}
					if (lines == null) {
						Logger.send("Failed to read scraper data");
						continue;
					}
					ArrayList<BazaarItem> flipCandidates = new ArrayList<>();
					HashMap<String, BazaarItem> bazaarItems = new HashMap<>();
					for (int i = 0; i < lines.size()-1; i += 10) {
						String name = lines.get(i);
						double[] data = new double[8];
						for (int j = 1; j < 9; j++) {
							data[j-1] = Double.parseDouble(lines.get(i+j));
						}
						int volume = (int)Math.round(Math.min(data[1], data[3]) / 30);
						double margin = (data[0] - data[2]) - data[0] * (tax * (derpyMayor ? 4 : 1));
						BazaarItem itemData = new BazaarItem(name, data[0], data[2], margin, volume, margin * volume);
						bazaarItems.put(name, itemData);
						if (volume >= 10 && volume <= 2240 && data[4] <= 0.21 && data[5] <= 0.21 && margin * volume >= 300000) {
							flipCandidates.add(itemData);
						}
					}
					int scraperIteration = Integer.parseInt(lines.get(lines.size() - 1));
					Collections.sort(flipCandidates, (a, b) -> {
						if (a.profit > b.profit) return -1;
						else if (a.profit < b.profit) return 1;
						else return 0;
					});
					if (showInfo) {
						/*
						Logger.send("To sell queue " + toSellQueue);
						Logger.send("BuyOrders " + buyOrders);
						Logger.send("SellOrders " + sellOrders);
						*/
						Logger.send("Steps " + initStep + " " + sellStep + " " + buyStep + " " + cancelBuyStep + " " + cancelSellStep + " " + equalToLastCount);
					}
					if (buyStep == lastBuyStep && sellStep == lastSellStep && cancelBuyStep == lastCancelBuyStep && cancelSellStep == lastCancelSellStep && initStep == lastInitStep
							&& (buyStep != 0 || sellStep != 0 || cancelBuyStep != 0 || cancelSellStep != 0 || initStep != 0)) {
						if (++equalToLastCount >= 7) {
							Logger.send("restarting because stuck");
							buyStep = 0;
							sellStep = 0;
							lastBuyStep = 0;
							lastSellStep = 0;
							cancelBuyItem = "";
							cancelSellItem = "";
							toBuy = "";
							toSell = "";
							toBuyAmount = 0;
							initStep = 0;
							equalToLastCount = 0;
							if (mc.thePlayer.openContainer instanceof ContainerChest) {
								mc.addScheduledTask(() -> {
									mc.thePlayer.closeScreen();
								});
							}
							continue;
						}
					}
					else {
						equalToLastCount = 0;
					}
					lastBuyStep = buyStep;
					lastSellStep = sellStep;
					lastCancelBuyStep = cancelBuyStep;
					lastCancelSellStep = cancelSellStep;
					lastInitStep = initStep;
					if ((System.currentTimeMillis() - lastInitTime) >= 10000 && 
							(toSell.length() == 0 && cancelSellItem.length() == 0 && cancelBuyItem.length() == 0 && toBuyAmount == 0)) {
							// initialization / sync with server
						if (initStep == 0) {
							if (mc.thePlayer.openContainer instanceof ContainerChest) {
								initStep++;
							}
							else {
								mc.thePlayer.sendChatMessage("/bz");
							}
						}
						if (initStep == 1) {
							if (getChestName().equals("Your Bazaar Orders")) {
								initStep++;
								continue;
							}
							else {
								clickItem("Manage Orders");
							}
						}
						if (initStep == 2) {
							if (!(mc.thePlayer.openContainer instanceof ContainerChest)) {
								initStep++;
							}
							else {
								ArrayList<BazaarOrder> buyOrders2 = new ArrayList<>();
								ArrayList<BazaarOrder> sellOrders2 = new ArrayList<>();
								for (Slot slot : mc.thePlayer.openContainer.inventorySlots) {
									if (slot == null || !slot.getHasStack()) continue;
									String name = formatString(slot.getStack().getDisplayName());
									if (name.length() >= 3 && name.substring(0, 3).equals("BUY")) {
										String item = name.substring(4);
										List<String> tooltip = slot.getStack().getTooltip(mc.thePlayer, false);
										double unitPrice = 0;
										for (String s : tooltip) {
											s = formatString(s);
											if (s.length() >= 14 && s.substring(0, 14).equals("Price per unit")) {
												String price = s.substring(16, s.length() - 6).replace(",", "");
												unitPrice = Double.parseDouble(price);
											}
										}
										buyOrders2.add(new BazaarOrder(item, unitPrice, scraperIteration));
									}
									else if (name.length() >= 4 && name.substring(0, 4).equals("SELL")) {
										String item = name.substring(5);
										List<String> tooltip = slot.getStack().getTooltip(mc.thePlayer, false);
										double unitPrice = 0;
										for (String s : tooltip) {
											s = formatString(s);
											if (s.length() >= 14 && s.substring(0, 14).equals("Price per unit")) {
												String price = s.substring(16, s.length() - 6).replace(",", "");
												unitPrice = Double.parseDouble(price);
											}
										}
										sellOrders2.add(new BazaarOrder(item, unitPrice, scraperIteration));
									}
								}
								if (buyOrders != null) {
									for (BazaarOrder item : buyOrders) {
										for (BazaarOrder item2 : buyOrders2) {
											if (item.name.equals(item2.name) && Math.abs(item.price - item2.price) < 0.01 && item2.iterationBought == scraperIteration) {
												item2.iterationBought = item.iterationBought;
												break;
											}
										}
									}
									for (BazaarOrder item : sellOrders) {
										for (BazaarOrder item2 : sellOrders2) {
											if (item.name.equals(item2.name) && Math.abs(item.price - item2.price) < 0.01 && item2.iterationBought == scraperIteration) {
												item2.iterationBought = item.iterationBought;
												break;
											}
										}
									}
								}
								buyOrders = buyOrders2;
								sellOrders = sellOrders2;
								mc.addScheduledTask(() -> {
									mc.thePlayer.closeScreen();
								});
							}
						}
						if (initStep == 3) {
							initStep = 0;
							lastInitTime = System.currentTimeMillis();
							Logger.send("Buy orders " + buyOrders.toString());
							Logger.send("Sell orders " + sellOrders.toString());
							Logger.send("Synced with server");
						}
						continue;
					}
					if (toSell.length() > 0) {
						if (sellStep == 0) {
							if (mc.thePlayer.openContainer instanceof ContainerChest) {
								sellStep++;
							}
							else {
								mc.thePlayer.sendChatMessage("/bz " + toSell.toLowerCase());
							}
						}
						if (sellStep == 1) {
							if (inventoryContains("Create Sell Offer")) {
								sellStep++;
							}
							else {
								clickItem(toSell);
							}
						}
						if (sellStep == 2) {
							if (inventoryContains("Best Offer -0.1")) {
								sellStep++;
							}
							else {
								clickItem("Create Sell Offer");
							}
						}
						if (sellStep == 3) {
							if (inventoryContains("Sell Offer")) {
								sellStep++;
							}
							else {
								clickItem("Best Offer -0.1");
							}
						}
						if (sellStep == 4) {
							if (!(mc.thePlayer.openContainer instanceof ContainerChest)) {
								sellStep++;
							}
							else {
								for (Slot slot : mc.thePlayer.openContainer.inventorySlots) {
									if (slot != null && slot.getHasStack() && formatString(slot.getStack().getDisplayName()).equals("Sell Offer")) {
										for (String s : slot.getStack().getTooltip(mc.thePlayer, false)) {
											s = formatString(s);
											if (s.length() >= 14 && s.substring(0, 14).equals("Price per unit")) {
												String price = s.substring(16, s.length() - 6).replace(",", "");
												sellPrice = Double.parseDouble(price);
												break;
											}
										}
									}
								}
								clickItem("Sell Offer");
							}
						}
						if (sellStep == 5) {
							Logger.send("Sold " + toSell + " " + sellPrice);
							sellOrders.add(new BazaarOrder(toSell, sellPrice, scraperIteration));
							sellStep = 0;
							toSell = "";
						}
						continue;
					}
					if (cancelSellItem.length() > 0) {
						if (cancelSellStep == 0) {
							if (mc.thePlayer.openContainer instanceof ContainerChest) {
								cancelSellStep++;
							}
							else {
								mc.thePlayer.sendChatMessage("/bz");
							}
						}
						if (cancelSellStep == 1) {
							if (getChestName().equals("Your Bazaar Orders")) {
								cancelSellStep++;
							}
							else if (getChestName().equals("")) {
								cancelSellStep = 0;
								cancelBuyItem = "";
								mc.addScheduledTask(() -> {
									mc.thePlayer.closeScreen();
								});
							}
							else {
								clickItem("Manage Orders");
							}
						}
						if (cancelSellStep == 2) {
							if (inventoryContains("Cancel Order")) {
								cancelSellStep++;
							}
							else {
								boolean found = false;
								for (Slot slot : mc.thePlayer.openContainer.inventorySlots) {
									if (slot != null && slot.getHasStack()) {
										String name = formatString(slot.getStack().getDisplayName());
										if (name.length() >= 4 && name.substring(0, 4).equals("SELL") && name.endsWith(cancelSellItem)) {
											clickSlot(slot.slotNumber);
											found = true;
										}
									}
								}
								if (!found) {
									cancelSellStep++;
								}
								sellOrderFilled = !found;
							}
						}
						if (cancelSellStep == 3) {
							if (getChestName().equals("Order options")) {
								if (getChestName().equals("Your Bazaar Orders")) {
									cancelSellStep++;
								}
								else {
									clickItem("Cancel Order");
								}
							}
							else if (getChestName().equals("")) {
								cancelSellStep = 0;
								cancelSellItem = "";
								mc.addScheduledTask(() -> {
									mc.thePlayer.closeScreen();
								});
							}
							else {
								cancelSellStep++;
							}
						}
						if (cancelSellStep == 4) {
							for (int i = 0; i < sellOrders.size(); i++) {
								if (sellOrders.get(i).name.equals(cancelSellItem)) {
									sellOrders.remove(i);
									break;
								}
							}
							Logger.send("Canceled sell order for " + cancelSellItem);
							if (!sellOrderFilled) { 
								toSellQueue.add(cancelSellItem);
							}
							cancelSellItem = "";
							cancelSellStep = 0;
							mc.addScheduledTask(() -> {
								mc.thePlayer.closeScreen();
							});
						}
						continue;
					}
					if (cancelBuyItem.length() > 0) {
						if (cancelBuyStep == 0) {
							if (mc.thePlayer.openContainer instanceof ContainerChest) {
								cancelBuyStep++;
							}
							else {
								mc.thePlayer.sendChatMessage("/bz");
							}
						}
						if (cancelBuyStep == 1) {
							if (getChestName().equals("Your Bazaar Orders")) {
								cancelBuyStep++;
							}
							else if (getChestName().equals("")) {
								cancelBuyStep = 0;
								cancelBuyItem = "";
								mc.addScheduledTask(() -> {
									mc.thePlayer.closeScreen();
								});
							}
							else {
								clickItem("Manage Orders");
							}
						}
						if (cancelBuyStep == 2) {
							if (inventoryContains("Cancel Order")) {
								cancelBuyStep++;
							}
							else {
								boolean found = false;
								for (Slot slot : mc.thePlayer.openContainer.inventorySlots) {
									if (slot != null && slot.getHasStack()) {
										String name = formatString(slot.getStack().getDisplayName());
										if (name.length() >= 3 && name.substring(0, 3).equals("BUY") && name.endsWith(cancelBuyItem)) {
											List<String> tooltip = slot.getStack().getTooltip(mc.thePlayer, false);
											for (String s : tooltip) {
												s = formatString(s);
												if (s.endsWith("items to claim!")) {
													int amount = Integer.parseInt(s.substring(9, s.length() - 16).replace(",", ""));
													Logger.send("Claiming " + amount + " " + cancelBuyItem);
													amountBought = amount;
													break;
												}
											}
											clickSlot(slot.slotNumber);
											found = true;
											break;
										}
									}
								}
								if (!found) {
									cancelBuyStep++;
								}
							}
						}
						if (cancelBuyStep == 3) {
							if (getChestName().equals("Order options")) {
								if (getChestName().equals("Your Bazaar Orders")) {
									cancelBuyStep++;
								}
								else {
									clickItem("Cancel Order");
								}
							}
							else if (getChestName().equals("")) {
								cancelBuyStep = 0;
								cancelBuyItem = "";
								mc.addScheduledTask(() -> {
									mc.thePlayer.closeScreen();
								});
							}
							else {
								cancelBuyStep++;
							}
						}
						if (cancelBuyStep == 4) {
							for (int i = 0; i < buyOrders.size(); i++) {
								if (buyOrders.get(i).name.equals(cancelBuyItem)) {
									buyOrders.remove(i);
									break;
								}
							}
							Logger.send("Canceled buy order for " + cancelBuyItem);
							if (amountBought > 0) { 
								toSellQueue.add(cancelBuyItem);
							}
							cancelBuyItem = "";
							cancelBuyStep = 0;
							amountBought = 0;
							mc.addScheduledTask(() -> {
								mc.thePlayer.closeScreen();
							});
						}
						continue;
					}
					if (toBuyAmount > 0) {
						if (buyStep == 0) {
							if (mc.thePlayer.openContainer instanceof ContainerChest) {
								buyStep++;
							}
							else {
								mc.thePlayer.sendChatMessage("/bz " + toBuy.toLowerCase());
							}
						}
						if (buyStep == 1) {
							if (inventoryContains("Create Buy Order")) {
								buyStep++;
							}
							else {
								clickItem(toBuy);
							}
						}
						if (buyStep == 2) {
							if (inventoryContains("Custom Amount")) {
								buyStep++;
							}
							else {
								clickItem("Create Buy Order");
							}
						}
						if (buyStep == 3) {
							if (mc.currentScreen instanceof GuiEditSign) {
								buyStep++;
							}
							else {
								clickItem("Custom Amount");
							}
						}
						if (buyStep == 4) {
							if (inventoryContains("Top Order +0.1")) {
								buyStep++;
							}
							else {
								enterInSign(toBuyAmount);
							}
						}
						if (buyStep == 5) {
							if (inventoryContains("Buy Order")) {
								buyStep++;
							}
							else {
								clickItem("Top Order +0.1");
							}
						}
						if (buyStep == 6) {
							if (!(mc.thePlayer.openContainer instanceof ContainerChest)) {
								buyStep++;
							}
							else {
								for (Slot slot : mc.thePlayer.openContainer.inventorySlots) {
									if (slot != null && slot.getHasStack() && formatString(slot.getStack().getDisplayName()).equals("Buy Order")) {
										for (String s : slot.getStack().getTooltip(mc.thePlayer, false)) {
											s = formatString(s);
											if (s.length() >= 14 && s.substring(0, 14).equals("Price per unit")) {
												String price = s.substring(16, s.length() - 6).replace(",", "");
												buyPrice = Double.parseDouble(price);
												break;
											}
										}
									}
								}
								clickItem("Buy Order");
							}
						}
						if (buyStep == 7) {
							buyOrders.add(new BazaarOrder(toBuy, buyPrice, scraperIteration));
							buyStep = 0;
							toBuyAmount = 0;
							toBuy = "";
							buyPrice = 0;
							Logger.send("Finished buying");
						}
						continue;
					}
					if (toSellQueue.size() > 0) {
						toSell = toSellQueue.get(toSellQueue.size() - 1);
						toSellQueue.remove(toSellQueue.size() - 1);
						continue;
					}
					//if one of our orders got overwritten
					for (BazaarOrder item : sellOrders) {
						if (!bazaarItems.containsKey(item.name) || (bazaarItems.containsKey(item.name) && bazaarItems.get(item.name).buyPrice < item.price - 0.01
								&& scraperIteration != item.iterationBought)) {
							cancelSellItem = item.name;
							break;
						}
					}
					if (cancelSellItem.length() > 0) { // we found something bad
						continue;
					}
					for (BazaarOrder item : buyOrders) {
						if (!bazaarItems.containsKey(item.name) || (bazaarItems.containsKey(item.name) && bazaarItems.get(item.name).sellPrice > item.price + 0.01
								&& scraperIteration != item.iterationBought)) {
							cancelBuyItem = item.name;
							break;
						}
					}
					if (cancelBuyItem.length() > 0) {
						continue;
					}
					if (filledItemType.equals("SELL")) {
						Logger.send("SELL " + filledItemName + " is filled");
						cancelSellItem = filledItemName;
						filledItemName = "";
						filledItemType = "";
						continue;
					}
					if (filledItemType.equals("BUY")) {
						Logger.send("BUY " + filledItemName + " is filled");
						cancelBuyItem = filledItemName;
						filledItemName = "";
						filledItemType = "";
						continue;
					}
					if (canBuy && buyOrders.size() + sellOrders.size() < 14) {
						for (BazaarItem b : flipCandidates) {
							boolean found = false;
							for (BazaarOrder item : buyOrders) {
								if (item.name.equals(b.name)) {
									found = true;
									break;
								}
							}
							for (BazaarOrder item : sellOrders) {
								if (item.name.equals(b.name)) {
									found = true;
									break;
								}
							}
							if (!found) {
								Logger.send("Buy order " + b.name + " " + b.profit);
								toBuy = b.name;
								toBuyAmount = b.volume;
								break;
							}
						}
						if (toBuyAmount > 0) {
							continue;
						}
					}
				}
			}
			catch (Exception e) {
				Logger.send("Crashed");
				Logger.send(e.toString());
				Writer writer = new StringWriter();
				e.printStackTrace(new PrintWriter(writer));
				String err = writer.toString();
				Logger.send(err);
			}
		});
		thread.start();
	}
	public void start() {
		running = true;
		Logger.send("Bazaar flipper started");
	}
	public void stop() {
		thread.interrupt();
	}
	public void pause() {
		running = false;
		Logger.send("Bazaar flipper paused");
	}
	
	public void setDerpyMayor(boolean b) {
		derpyMayor = b;
	}
	
	private String formatString(String s) {
		String res = "";
		for (int i = 0; i < s.length(); i++) {
			if ((int)s.charAt(i) > 127) {
				i++; continue;
			}
			res += s.charAt(i);
		}
		return res;
	}
	
	private void enterInSign(int amount) throws InterruptedException {
		Minecraft mc = Minecraft.getMinecraft();
		if (mc.currentScreen instanceof GuiEditSign) {
			GuiEditSign gui = (GuiEditSign)mc.currentScreen;
			String s = String.valueOf(amount);
			for (int i = 0; i < s.length(); i++) {
				int keyCode = s.charAt(i) == '0' ? 11 : (s.charAt(i)-'0') + 1;
				((GuiEditSignInvoker)gui).invokeKeyTyped(s.charAt(i), keyCode);
				Thread.sleep((int)(rand.nextDouble() * 100 + 200));
			}
			((GuiEditSignInvoker)gui).invokeKeyTyped((char)0, 1);
		}
	}
	private void clickSlot(int slotId) {
		Minecraft mc = Minecraft.getMinecraft();
		mc.playerController.windowClick(mc.thePlayer.openContainer.windowId, slotId, 0, 0, mc.thePlayer);
	}
	
	private String getChestName() {
		if (Minecraft.getMinecraft().thePlayer.openContainer instanceof ContainerChest) {
			return ((ContainerChest)Minecraft.getMinecraft().thePlayer.openContainer).getLowerChestInventory().getName();
		}
		return "";
	}
	
	private void clickItem(String name) {
		Minecraft mc = Minecraft.getMinecraft();
		if (mc.thePlayer.openContainer != null && mc.thePlayer.openContainer.inventorySlots != null) {
			for (Slot slot : mc.thePlayer.openContainer.inventorySlots) {
				if (slot != null && slot.getStack() != null) {
					String cur = formatString(slot.getStack().getDisplayName());
					if (cur.equals(name)) {
						mc.playerController.windowClick(mc.thePlayer.openContainer.windowId, slot.slotNumber, 0, 0, mc.thePlayer);
						return;
					}
				}
			}
		}
		Logger.send("Click " + name + " failed");
	}
						
	private boolean inventoryContains(String word) {
		Minecraft mc = Minecraft.getMinecraft();
		if (mc.thePlayer.openContainer != null && mc.thePlayer.openContainer.inventorySlots != null) {
			for (Slot slot : mc.thePlayer.openContainer.inventorySlots) {
				if (slot != null && slot.getStack() != null && formatString(slot.getStack().getDisplayName()).equals(word)) {
					return true;
				}
			}
		}
		return false;
	}

	private int findMatchingStringInTab(String s) {
		// 0 --> other error
		// 1 --> ok, -1 --> didnt find
		NetHandlerPlayClient netHandler = Minecraft.getMinecraft().getNetHandler();
		if (netHandler == null || netHandler.getPlayerInfoMap() == null) return 0;
		Collection<NetworkPlayerInfo> playerList = null;
		for (int tries = 0; tries < 10; tries++) {
			try {
				playerList = new ArrayList<>(netHandler.getPlayerInfoMap());
			}
			catch (ConcurrentModificationException e) {
				
			}
			if (playerList != null) break;
		}
		if (playerList == null) { 
			Logger.send("Failed after ten iterations");
			return 0;
		}
		for (NetworkPlayerInfo playerInfo : playerList) {
			if (playerInfo == null) continue;
			String displayName = playerInfo.getDisplayName() != null ? playerInfo.getDisplayName().getUnformattedText() : "";
			if (displayName.contains(s)) return 1;
		}
		return -1;
	}
	private class BazaarOrder {
		public String name;
		public double price;
		public int iterationBought;
		public BazaarOrder(String name, double price, int iteration) {
			this.name = name;
			this.price = price;
			iterationBought = iteration;
		}
		@Override
		public String toString() {
			return name + " " + price + " " + iterationBought;
		}
	}

	private Thread thread = null, monitorOrderFillThread = null;
	private volatile boolean running = false;
	private volatile double tax = 0.01125, buyPrice = 0, sellPrice = 0, amountBought;
	private volatile boolean derpyMayor = false;
	private volatile int buyStep = 0, sellStep = 0, initStep = 0, cancelSellStep = 0, cancelBuyStep = 0;
	private volatile String cancelSellItem = "", cancelBuyItem = "";
	private ArrayList<BazaarOrder> buyOrders = null, sellOrders = null;
	private volatile boolean sellOrderFilled = false, buyOrderFilled = false;
	private ArrayList<String> toSellQueue = new ArrayList<>();
	private Random rand = new Random();
	private volatile String filledItemType = "", filledItemName = "";
	private volatile long lastInitTime = 0;

	public volatile boolean canBuy = true;
	public volatile String toBuy = "", toSell = "";
	public volatile int toBuyAmount = 0;
	public volatile boolean showInfo = false;
}