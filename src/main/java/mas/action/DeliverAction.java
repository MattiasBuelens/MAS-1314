package mas.action;

import java.util.HashSet;
import java.util.Set;

import mas.BDIVehicle;
import mas.Packet;
import rinde.sim.core.TimeLapse;

public class DeliverAction implements Action {

	private final Packet packet;

	public DeliverAction(Packet packet) {
		this.packet = packet;
	}

	@Override
	public boolean execute(BDIVehicle target, TimeLapse time) {
		target.deliver(packet, time);
		return !target.containsParcel(packet);
	}

	@Override
	public SimulationState simulate(BDIVehicle target, SimulationState state)
			throws IllegalActionException {
		long currentTime = state.getTime();
		if (!target.canDeliverAt(packet, currentTime)) {
			throw new IllegalActionException(
					"Cannot deliver at current simulated time.");
		}

		long newTime = currentTime + packet.getDeliveryDuration();
		Set<Packet> newPickedUp = new HashSet<>(state.getPickedUp());
		Set<Packet> newDelivered = new HashSet<>(state.getDelivered());
		newPickedUp.remove(packet);
		newDelivered.add(packet);

		return state.nextState(newTime, newPickedUp, newDelivered);
	}

}
