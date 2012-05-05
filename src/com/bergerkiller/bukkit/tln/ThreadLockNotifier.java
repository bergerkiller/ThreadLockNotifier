package com.bergerkiller.bukkit.tln;

import java.util.ArrayList;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class ThreadLockNotifier extends JavaPlugin {

	public static boolean running = true;
	public static boolean pulse = true;
	private int id;
	public static Thread mainthread = Thread.currentThread();
	
	
	public void onEnable() {
		new Thread("Thread Lock Notifier") {
			public void run() {
				StackTraceElement[] previous = null;
				int maxidx = Integer.MAX_VALUE;
				ArrayList<StackTraceElement> elems = new ArrayList<StackTraceElement>();
				ArrayList<StackTraceElement> tmpelems = new ArrayList<StackTraceElement>();
				while (running) {
					pulse = false;
					try {
						Thread.sleep(5000);
					} catch (InterruptedException ex) {}
					if (pulse) {
						previous = null;
						maxidx = Integer.MAX_VALUE;
					} else if (previous != null) {
						while (true) {
							tmpelems.clear();
							StackTraceElement[] newstack = mainthread.getStackTrace();
							maxidx = Math.min(maxidx, Math.min(newstack.length, previous.length));
							
							int i = 1;
							for (; i < maxidx; i++) {
								if (newstack[newstack.length - i - 1].equals(previous[previous.length - i - 1])) {
									tmpelems.add(0, newstack[newstack.length - i - 1]);
								} else {
									maxidx = i;
									break;
								}
							}
							if (tmpelems.size() != elems.size()) {
								break;
							} else {
								try {
									Thread.sleep(200);
								} catch (InterruptedException ex) {}
							}
						}
						elems.clear();
						elems.addAll(tmpelems);
						Bukkit.getLogger().log(Level.WARNING, "[TLN] The main thread is still stuck, updated stack trace is:");
						for (StackTraceElement elem : elems) {
							Bukkit.getLogger().log(Level.INFO, "[TLN] at " + elem);
						}
					} else {
						previous = mainthread.getStackTrace();
						maxidx = Integer.MAX_VALUE;
						Bukkit.getLogger().log(Level.WARNING, "[TLN] The main thread failed to respond after 5 seconds");
						Bukkit.getLogger().log(Level.WARNING, "[TLN] What follows is the stack trace of the main thread");
						Bukkit.getLogger().log(Level.WARNING, "[TLN] This stack trace will be further refined as long as the thread is stuck");
						for (StackTraceElement elem : previous) {
							Bukkit.getLogger().log(Level.INFO, "[TLN] at " + elem);
						}
					}
				}
			}
		}.start();
		id = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			public void run() {
				pulse = true;
			}
		}, 1, 1);
	}
	
	public void onDisable() {
		running = false;
		Bukkit.getScheduler().cancelTask(id);
	}
	
}
