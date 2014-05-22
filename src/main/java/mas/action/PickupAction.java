package mas.action;

import mas.BDIVehicle;
import mas.Packet;
import rinde.sim.core.TimeLapse;

public class PickupAction implements Action<BDIVehicle> {

	private final Packet packet;

	public PickupAction(Packet pkg) {
		this.packet = pkg;
	}

	@Override
	public boolean execute(BDIVehicle target, TimeLapse time) {
		target.pickup(packet, time);
		return target.containsPacket(packet);
	}

}
