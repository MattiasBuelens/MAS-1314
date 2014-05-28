package mas.timer;

public interface TimerCallback {

	/**
	 * Called when the current time passes the timer's scheduled time.
	 * 
	 * The timer can be kept alive by rescheduling it in this callback using
	 * {@link Timer#schedule(long)}. Otherwise, it will be removed.
	 * 
	 * @param timer
	 *            The timer triggering this callback.
	 * @param currentTime
	 *            The current time.
	 */
	public void onTimer(Timer timer, long currentTime);

}
