package mas.action;

import static com.google.common.base.Preconditions.checkState;

import java.util.HashSet;
import java.util.Set;

import mas.BDIVehicle;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.model.pdp.Parcel;

public class PickupAction implements Action {

	private final Parcel packet;

	public PickupAction(Parcel packet) {
		this.packet = packet;
	}

	@Override
	public boolean execute(BDIVehicle target, TimeLapse time) {
		target.pickup(packet, time);
		return target.containsPacket(packet);
	}

	@Override
	public SimulationState simulate(BDIVehicle target, SimulationState state) {
		long currentTime = state.getTime();
		checkState(target.canPickupAt(packet, currentTime),
				"Cannot pick up at current simulated time.");

		long newTime = currentTime + packet.getPickupDuration();
		Set<Parcel> newPackets = new HashSet<>(state.getPackets());
		newPackets.add(packet);

		return new SimulationState(newTime, state.getPosition(), newPackets);
	}

}
