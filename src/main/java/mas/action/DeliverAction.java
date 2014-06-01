package mas.action;

import java.util.HashSet;
import java.util.Set;

import mas.BDIVehicle;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.model.pdp.Parcel;

public class DeliverAction implements Action {

	private final Parcel packet;

	public DeliverAction(Parcel packet) {
		this.packet = packet;
	}

	@Override
	public boolean execute(BDIVehicle target, TimeLapse time) {
		target.deliver(packet, time);
		return !target.containsPacket(packet);
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
		Set<Parcel> newPickedUp = new HashSet<>(state.getPickedUp());
		Set<Parcel> newDelivered = new HashSet<>(state.getDelivered());
		newPickedUp.remove(packet);
		newDelivered.add(packet);

		return state.nextState(newTime, newPickedUp, newDelivered);
	}

}
