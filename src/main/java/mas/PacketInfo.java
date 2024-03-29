package mas;

import rinde.sim.core.model.pdp.PDPModel.ParcelState;
import rinde.sim.core.model.pdp.Vehicle;

public class PacketInfo {

	private final Packet packet;
	private final ParcelState state;
	private final Vehicle deliveringTruck;
	private final long deliveryTime;

	public PacketInfo(Packet packet, ParcelState state,
			Vehicle deliveringTruck, long deliveryTime) {
		this.packet = packet;
		this.state = state;
		this.deliveringTruck = deliveringTruck;
		this.deliveryTime = deliveryTime;
	}

	public Packet getPacket() {
		return packet;
	}

	public ParcelState getState() {
		return state;
	}

	public boolean isPickingUp() {
		return getState().isPickedUp() || getState() == ParcelState.PICKING_UP;
	}

	public Vehicle getDeliveringTruck() {
		return deliveringTruck;
	}

	public long getDeliveryTime() {
		return deliveryTime;
	}

}
