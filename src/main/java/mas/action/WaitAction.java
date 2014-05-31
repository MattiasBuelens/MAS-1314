package mas.action;

import mas.BDIVehicle;
import rinde.sim.core.TimeLapse;

/**
 * Waits until the time is past a given time point.
 */
public class WaitAction implements Action {

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

	@Override
	public SimulationContext simulate(BDIVehicle target,
			SimulationContext context) {
		long newTime = Math.max(context.getTime(), until);
		return context.next(newTime);
	}

}
