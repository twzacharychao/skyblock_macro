package com.gromp.mixintestmod;

import java.awt.Robot;
import java.awt.event.InputEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class AutoClicker {
    final static int sample_count = 20;
    final static int seconds_per_test = 10;
    final static double cutoff_threshhold = 0.05; // remove clicks that are too bad (about 1/3 of all clicks)

    static ArrayList<Double> initial_wait;
    static ArrayList<ArrayList<ArrayList<Double>>> click_profiles;
    
    static {
        initial_wait = new ArrayList<>(sample_count);
        click_profiles = new ArrayList<>(sample_count);
        for (int i = 0; i < sample_count; i++) {
        	click_profiles.add(new ArrayList<>());
        	File cps_file = new File("samples/cps_samples/" + Integer.toString(i) + ".txt");
        	int[] clicks = new int[seconds_per_test];
        	try {
				BufferedReader br = new BufferedReader(new FileReader(cps_file));
	        	for (int j = 0; j < seconds_per_test; j++) {
	        		clicks[j] = Integer.parseInt(br.readLine());
	        		if (j == 0) clicks[j]--;
	        		click_profiles.get(i).add(new ArrayList<>(clicks[j]));
	        	}
				br.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
        	        	
        	File click_file = new File("samples/click_samples/" + Integer.toString(i) + ".txt");
        	try {
        		BufferedReader br = new BufferedReader(new FileReader(click_file));
            	initial_wait.add(Double.parseDouble(br.readLine()));
        		for (int j = 0; j < seconds_per_test; j++) {
        			for (int k = 0; k < clicks[j]; k++) {
        				double time_since_press, time_since_release;
        				time_since_press = Double.parseDouble(br.readLine());
        				time_since_release = Double.parseDouble(br.readLine());
                        if (time_since_press >= cutoff_threshhold || time_since_release >= cutoff_threshhold) {
                            continue;
                        }
                        if (time_since_press >= 0.025) time_since_press -= 0.0075;
                        if (time_since_release >= 0.025) time_since_release -= 0.0075;
                        click_profiles.get(i).get(j).add(time_since_press);
                        click_profiles.get(i).get(j).add(time_since_release);
        			}
        		}
        		br.close();
        	} catch (Exception e) {
        		e.printStackTrace();
        	}
        }
    }
    
    private Random rng;
    
    private ArrayList<Integer> profile_indices;
    private int index_of_index = 0;
    
    private long orig_time;
    public double wait_time;
    private int current_profile = -1;
    private int current_second = 0;
    private int current_wait_index = 0;
    
    private boolean pressing = false;
    private boolean status = false;
    private Button button;
    
    private Robot bot;
    
    void click(boolean press) {
    	int mouseMask = button == Button.LEFT ? InputEvent.BUTTON1_DOWN_MASK : InputEvent.BUTTON3_DOWN_MASK;
    	if (press) bot.mousePress(mouseMask);
    	else bot.mouseRelease(mouseMask);
    }
    
    public void tick(boolean new_pressing) {
    	long new_time = System.currentTimeMillis();
        if (!pressing && new_pressing) {
            pressing = true;
            current_profile = get_profile();
            click(true);
            status = true;
            wait_time = initial_wait.get(current_profile);
            orig_time = new_time;
        }
        else if (pressing && !new_pressing) {
            pressing = false;
            current_second = 0;
            current_wait_index = 0;
            if (status) {
            	status = false;
            	click(false);
            }
        }
        else if (new_pressing) {
        	double time_elapsed = (double)(new_time - orig_time) / 1000.0;
            if (time_elapsed >= wait_time) {
            	click(!status);
                status = !status;
                if (++current_wait_index == click_profiles.get(current_profile).get(current_second).size()) {
                    if (++current_second == 10) {
                        current_profile = get_profile();
                        current_second = current_wait_index = 0;
                        click(true);
                        status = true;
                        wait_time = initial_wait.get(current_profile);
                        orig_time = new_time;
                        return;
                    }
                    else {
                        current_wait_index = 0;
                    }
                }
                try {
                	wait_time = click_profiles.get(current_profile).get(current_second).get(current_wait_index);
                }
                catch (IndexOutOfBoundsException e) {
                	System.err.println("Current profile: " + Integer.toString(current_profile));
                	System.err.println("Current second: " + Integer.toString(current_second));
                	System.err.println("Current wait index: " + Integer.toString(current_wait_index));
                	e.printStackTrace();
                }
                orig_time = new_time;
            }
        }
    }
    
    public int get_profile() {
    	if (index_of_index == 0) {
    		Collections.shuffle(profile_indices, rng);
    	}
    	int profile_index = profile_indices.get(index_of_index);
    	for (ArrayList<Double> clicks : click_profiles.get(profile_index)) {
    		Collections.shuffle(clicks, rng);
    	}
    	index_of_index = (index_of_index + 1) % sample_count;
    	return profile_index;
    }
    
    AutoClicker(Button button) {
    	try {
    		bot = new Robot();
    	}
    	catch (Exception e) {}
    	profile_indices = new ArrayList<>(sample_count);
    	rng = new Random(System.currentTimeMillis());
    	this.button = button;
    	orig_time = System.currentTimeMillis();
    	
    	
    	for (int i = 0; i < sample_count; i++) {
    		profile_indices.add(i);
    	}
    }
    
    public static AutoClicker left = new AutoClicker(Button.LEFT), right = new AutoClicker(Button.RIGHT);
    
    public enum Button {
    	LEFT, RIGHT
    }
    
}
/* Original code in C++ (written by me):
#include <windows.h>
#include <string>
#include <filesystem>
#include <fstream>
#include <iostream>
#include <chrono>
#include <random>
#include <assert.h>


int main() {
    using std::vector;

	std::mt19937 rng(std::chrono::steady_clock::now().time_since_epoch().count());

    const std::filesystem::path cps_samples{ "cps_samples" };
    const std::filesystem::path click_samples{ "click_samples" };

    const int sample_count = 20;
    const int seconds_per_test = 10;
    const double cutoff_threshhold = 0.05; // remove clicks that are too bad (about 1/3 of all clicks)

    vector<double> initial_wait(sample_count);
    vector<vector<vector<double>>> click_profiles(sample_count, vector<vector<double>>(seconds_per_test));

    for (int i = 0; i < sample_count; i++) {
        std::fstream cps_file;
        cps_file.open("cps_samples/" + std::to_string(i) + ".txt");

        vector<int> clicks(seconds_per_test);
        for (int& click : clicks) cps_file >> click;

        clicks[0]--;

        std::fstream click_file;
        click_file.open("click_samples/" + std::to_string(i) + ".txt");
        click_file >> initial_wait[i];
        for (int j = 0; j < seconds_per_test; j++) {
            for (int k = 0; k < clicks[j]; k++) {
				double time_since_press, time_since_release;
                click_file >> time_since_press >> time_since_release;
                if (time_since_press >= cutoff_threshhold || time_since_release >= cutoff_threshhold) {
                    continue;
                }
                if (time_since_press >= 0.15) time_since_press -= 0.1;
                if (time_since_release >= 0.15) time_since_release -= 0.1;
                click_profiles[i][j].push_back(time_since_press);
                click_profiles[i][j].push_back(time_since_release);
            }
        }
    }

    vector<int> profile_indices(sample_count);
    for (int i = 0; i < sample_count; i++) profile_indices[i] = i;

    int index_of_index = 0; // index of profile_indices

    const auto get_profile = [&]() -> int { // get index of current profile
        if (!index_of_index) std::shuffle(profile_indices.begin(), profile_indices.end(), rng);
        int profile_index = profile_indices[index_of_index];
        for (auto& clicks : click_profiles[profile_index]) {
            shuffle(clicks.begin(), clicks.end(), rng);
        }
        index_of_index = (index_of_index + 1) % sample_count;
        return profile_index;
    };

    const auto is_key_down = [&](int keycode) -> bool {
        return GetAsyncKeyState(keycode) & 0x8000;
    };
    const auto click = [&](bool left, bool press) {
        INPUT input;
        input.type = INPUT_MOUSE;
        input.mi.dwFlags = left ? (press ? MOUSEEVENTF_LEFTDOWN : MOUSEEVENTF_LEFTUP) : (press ? MOUSEEVENTF_RIGHTDOWN : MOUSEEVENTF_RIGHTUP);
        SendInput(1, &input, sizeof(input));
    };

    using std::chrono::system_clock;

    auto orig_time = system_clock::now();
    double wait_time = 0;

    int current_profile = -1;
    int current_second = 0; // what second am i currently on
    int current_wait_index = 0; // currently waiting for which 'wait'?

    bool pressing = false; // am i supposed to press
    bool status = false; // am i currently pressing
    bool left = false; // true = left click, false = right click

    for (;;) { // hold middle click to left click; hold middle & right click to right click

        bool new_pressing = is_key_down(VK_MBUTTON);
        auto new_time = system_clock::now();

        if (!pressing && new_pressing) { // begin pressing
            assert(current_second == 0);
            assert(current_wait_index == 0);
            pressing = true;
            left = !is_key_down(VK_RBUTTON);
            current_profile = get_profile();
            click(left, true);
            status = true;
            wait_time = initial_wait[current_profile];
            orig_time = new_time;
        }
        else if (pressing && !new_pressing) { // stop pressing
            pressing = false;
            current_second = current_wait_index = 0;
            if (status) {
                click(left, false);
                status = false;
            }
        }
        else if (new_pressing) { // continue pressing
            if (left && is_key_down(VK_RBUTTON)) { // change from left to right click
                left = false;
                if (status) {
                    click(true, false);
                    status = false;
                }
            }
            double time_elapsed = std::chrono::duration<double>(new_time - orig_time).count();
            if (time_elapsed >= wait_time) {
                click(left, !status);
                status ^= 1;
                if (++current_wait_index == int(click_profiles[current_profile][current_second].size())) {
					assert(!status);
                    if (++current_second == 10) {
                        current_profile = get_profile();
                        current_second = current_wait_index = 0;
                        click(left, true);
                        status = true;
                        wait_time = initial_wait[current_profile];
                        orig_time = new_time;
                        continue;
                    }
                    else { // proceed to the next second
                        current_wait_index = 0;
                    }
                }
                wait_time = click_profiles[current_profile][current_second][current_wait_index];
                orig_time = new_time;
            }
        }
    }

	// Code for profiling my jitterclick:
 //   typedef std::chrono::system_clock::time_point timepoint;
 //   timepoint orig_time = std::chrono::system_clock::now();
 //   int cnt = 0;
 //   bool state = false;
 //   timepoint time;
	//timepoint cps_time;
 //   bool first = true;
 //   int clicks = 0;
 //   std::filesystem::path click_samples{ "click_samples" };
 //   std::filesystem::path cps_samples{ "cps_samples" };
 //   int current_sample = std::distance(std::filesystem::directory_iterator(click_samples), std::filesystem::directory_iterator{});
 //   std::fstream current_cps_file;
 //   std::fstream current_clicks_file;
 //   current_cps_file.open("cps_samples/" + std::to_string(current_sample) + ".txt", std::ios::out);
 //   current_clicks_file.open("click_samples/" + std::to_string(current_sample) + ".txt", std::ios::out);
 //   int seconds = 0;
 //   while (true) {
 //       bool down = IsKeyDown(VK_LBUTTON);
	//	auto now_time = std::chrono::system_clock::now();
 //       if (down ^ state) {
 //           state = down;
 //           if (state) { // just pressed
 //               if (first) {
 //                   first = false;
 //                   time = now_time;
 //                   cps_time = now_time;
 //               }
 //               else {
 //                   double time_since_last = std::chrono::duration<double>(now_time - time).count();
 //                   std::cout << "Pressed after " << std::to_string(time_since_last) << "s since last release\n";
 //                   current_clicks_file << time_since_last << '\n';
 //               }
 //           }
 //           else { // just released
	//			double time_since_last = std::chrono::duration<double>(now_time - time).count();
	//			std::cout << "Released after " << std::to_string(time_since_last) << "s since last press\n";
 //               current_clicks_file << time_since_last << '\n';
 //               clicks++;
 //           }
 //           time = now_time;
 //       }
 //       if (!first && std::chrono::duration<double>(now_time - cps_time).count() >= 1.0) {
 //           cps_time = now_time;
 //           std::cout << "Clicks this second: " << clicks << '\n';
 //           current_cps_file << clicks << '\n';
 //           clicks = 0;
 //           if (++seconds == 10) {
 //               current_clicks_file.close();
 //               current_cps_file.close();
	//			return 0;
 //           }
 //       }
 //   }
}
*/