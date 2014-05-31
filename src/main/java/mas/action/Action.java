package mas.action;

import mas.BDIVehicle;
import rinde.sim.core.TimeLapse;

public interface Action {

	public boolean execute(BDIVehicle target, TimeLapse time);

	public SimulationContext simulate(BDIVehicle target,
			SimulationContext context);

}
