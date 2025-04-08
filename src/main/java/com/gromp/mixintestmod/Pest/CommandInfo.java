package com.gromp.mixintestmod.Pest;

public class CommandInfo {
	CommandInfo(String command, int timeBefore, int timeAfter) {
		this.command = command;
		this.timeBefore = timeBefore;
		this.timeAfter = timeAfter;
	}
	public String command;
	public int timeBefore;
	public int timeAfter;
}