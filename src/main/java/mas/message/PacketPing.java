package mas.message;

import mas.Packet;
import mas.Truck;
import rinde.sim.core.model.pdp.Vehicle;

public class PacketPing extends TypedBroadcastMessage<Packet, Truck> implements
		TruckMessage {

	private final Vehicle deliveringTruck;
	private final long deliveryTime;

	public PacketPing(Packet packet, Vehicle deliveringTruck, long deliveryTime) {
		super(packet, Truck.class);
		this.deliveringTruck = deliveringTruck;
		this.deliveryTime = deliveryTime;
	}

	public Packet getPacket() {
		return getSender();
	}

	public Vehicle getDeliveringTruck() {
		return deliveringTruck;
	}

	public long getDeliveryTime() {
		return deliveryTime;
	}

	@Override
	public void accept(TruckMessageVisitor visitor) {
		visitor.visitPacketPing(this);
	}

}
