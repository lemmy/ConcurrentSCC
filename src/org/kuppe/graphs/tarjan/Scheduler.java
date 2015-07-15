package org.kuppe.graphs.tarjan;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

public class Scheduler {

	private static final int TASKS = Integer.MAX_VALUE / 1000;

	public static void main(String[] args) {
		final ForkJoinPool executor = new ForkJoinPool();

		final Map<Integer, Long> startTimes = new ConcurrentHashMap<>(TASKS);
		
		for (int i = 0; i < TASKS; i++) {
			executor.submit(new MyRunnable(i, startTimes, executor));
		}

		executor.awaitQuiescence(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		executor.shutdown();
		
		System.out.println(Arrays.toString(startTimes.entrySet().toArray()));
	}
	
	public static class MyRunnable implements Runnable {

		private final int i;
		private final Map<Integer, Long> startTimes;
		private ForkJoinPool executor;
		private boolean waited = false;

		public MyRunnable(int i, Map<Integer, Long> startTimes, ForkJoinPool executor) {
			this.i = i;
			this.startTimes = startTimes;
			this.executor = executor;
		}

		@Override
		public void run() {
			if (i < 1000 && waited == false) {
				waited  = true;
				executor.submit(this);
			}
			startTimes.put(i, System.nanoTime());
		}
	}
}
