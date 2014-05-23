package mas.action;

import mas.BDIVehicle;
import mas.Packet;
import rinde.sim.core.TimeLapse;

public class DeliverAction implements Action<BDIVehicle> {

	private final Packet packet;

	public DeliverAction(Packet packet) {
		this.packet = packet;
	}

	@Override
	public boolean execute(BDIVehicle target, TimeLapse time) {
		target.deliver(packet, time);
		return !target.containsPacket(packet);
	}

}
