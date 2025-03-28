package com.gromp.mixintestmod;

public class Action {
	double x, z;
	long cutoff;
	int type; // 0 = turn, 1 = move, 2 = press mouse, 3 = release mouse
	String command;
	boolean addNoise = true;
	Action(double x, double z, int type, long cutoff) {
		this.x = x;
		this.z = z;
		this.type = type;
		this.cutoff = cutoff;
	}
	Action(double x, double z, int type, long cutoff, boolean noise) {
		this.x = x;
		this.z = z;
		this.type = type;
		this.cutoff = cutoff;
		this.addNoise = noise;
	}
	Action(double x, double z, int type) {
		this.x = x;
		this.z = z;
		this.type = type;
		this.cutoff = 60;
	}
	Action(String s, int timeout) {
		type = 9;
		cutoff = timeout;
		command = s;
	}
	Action() {
		type = 10;
	}
}