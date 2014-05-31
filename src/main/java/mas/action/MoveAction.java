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
	public SimulationContext simulate(BDIVehicle target,
			SimulationContext context) {
		long duration = target.getEstimatedTimeBetween(context.getPosition(),
				destination);
		long newTime = context.getTime() + duration;
		return context.next(newTime, destination);
	}

}
