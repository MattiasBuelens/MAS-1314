package mas.action;

import java.util.HashSet;
import java.util.Set;

import mas.BDIVehicle;
import mas.Packet;
import rinde.sim.core.TimeLapse;

public class PickupAction implements Action {

	private final Packet packet;

	public PickupAction(Packet packet) {
		this.packet = packet;
	}

	@Override
	public boolean execute(BDIVehicle target, TimeLapse time) {
		target.pickup(packet, time);
		return target.containsParcel(packet);
	}

	@Override
	public SimulationState simulate(BDIVehicle target, SimulationState state)
			throws IllegalActionException {
		long currentTime = state.getTime();
		if (!target.canPickupAt(packet, currentTime)) {
			throw new IllegalActionException(
					"Cannot pick up at current simulated time.");
		}

		long newTime = currentTime + packet.getPickupDuration();
		Set<Packet> newPickedUp = new HashSet<>(state.getPickedUp());
		newPickedUp.add(packet);

		return state.nextState(newTime, newPickedUp);
	}

}
