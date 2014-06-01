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
	public SimulationState simulate(BDIVehicle target, SimulationState state) {
		long newTime = Math.max(state.getTime(), until);
		return state.nextState(newTime);
	}

}
