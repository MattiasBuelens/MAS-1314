package mas.action;

import mas.BDIVehicle;
import rinde.sim.core.TimeLapse;

public interface Action {

	public boolean execute(BDIVehicle target, TimeLapse time) throws ActionFailedException;

	public SimulationState simulate(BDIVehicle target, SimulationState state)
			throws IllegalActionException;

}
