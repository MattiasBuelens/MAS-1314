package mas.action;

import static com.google.common.base.Preconditions.checkState;

import java.util.HashSet;
import java.util.Set;

import mas.BDIVehicle;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.model.pdp.Parcel;

public class DeliverAction implements Action {

	private final Parcel packet;

	public DeliverAction(Parcel parcel) {
		this.packet = parcel;
	}

	@Override
	public boolean execute(BDIVehicle target, TimeLapse time) {
		target.deliver(packet, time);
		return !target.containsPacket(packet);
	}

	@Override
	public SimulationState simulate(BDIVehicle target, SimulationState state) {
		long currentTime = state.getTime();
		checkState(target.canDeliverAt(packet, currentTime),
				"Cannot deliver at current simulated time.");

		long newTime = currentTime + packet.getDeliveryDuration();
		Set<Parcel> newPackets = new HashSet<>(state.getPackets());
		newPackets.remove(packet);

		return state.nextState(newTime, newPackets);
	}

}
