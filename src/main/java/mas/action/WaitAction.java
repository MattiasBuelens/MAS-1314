package mas.action;

import rinde.sim.core.TimeLapse;
import mas.BDIVehicle;

/**
 * Waits until the time is past a given time point.
 */
public class WaitAction implements Action<BDIVehicle> {

	private final long until;

	public WaitAction(long until) {
		this.until = until;
	}

	@Override
	public boolean execute(BDIVehicle target, TimeLapse time) {
		if (time.getTime() < until) {
			long consumeTime = Math.min(until - time.getTime(),
					time.getTimeLeft());
			time.consume(consumeTime);
		}
		return time.getTime() >= until;
	}

}
