package mas.action;

import mas.BDIVehicle;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.graph.Point;

public class MoveAction implements Action {

	private final Point destination;

	public MoveAction(Point destination) {
		this.destination = destination;
	}

	@Override
	public boolean execute(BDIVehicle target, TimeLapse time) {
		target.moveTo(destination, time);
		return target.getPosition().equals(destination);
	}

	@Override
	public SimulationState simulate(BDIVehicle target, SimulationState state) {
		long duration = target.getEstimatedTimeBetween(state.getPosition(),
				destination);
		long newTime = state.getTime() + duration;
		return state.nextState(newTime, destination);
	}

}
