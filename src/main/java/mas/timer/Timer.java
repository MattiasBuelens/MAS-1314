package mas.timer;

import com.google.common.collect.Ordering;
import com.google.common.primitives.Longs;

public class Timer {

	private final TimerCallback callback;
	private long time;
	private boolean active = true;

	public Timer(long time, TimerCallback callback) {
		this.callback = callback;
		schedule(time);
	}

	public boolean isActive() {
		return active;
	}

	/**
	 * Schedule or re-schedule the timer.
	 * 
	 * @param time
	 *            The scheduled time.
	 */
	public void schedule(long time) {
		this.active = true;
		this.time = time;
	}

	/**
	 * Cancel the timer.
	 */
	public void cancel() {
		this.active = false;
	}

	/**
	 * Check the time and run the callback if past the scheduled time.
	 * 
	 * @param currentTime
	 *            The current time.
	 */
	public void run(long currentTime) {
		if (isActive() && currentTime >= time) {
			cancel();
			callback.onTimer(this, currentTime);
		}
	}

	public static final Ordering<Timer> timeOrdering = new Ordering<Timer>() {
		@Override
		public int compare(Timer left, Timer right) {
			return Longs.compare(left.time, right.time);
		}
	};

}
