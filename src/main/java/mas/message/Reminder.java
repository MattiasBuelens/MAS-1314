package mas.message;

import mas.Packet;
import mas.Truck;
import rinde.sim.core.model.pdp.Vehicle;
import rinde.sim.core.model.pdp.PDPModel.ParcelState;

public class Reminder extends TypedBroadcastMessage<Packet, Truck> implements
		TruckMessage {

	private final ParcelState state;
	private final Vehicle deliveringTruck;
	private final long deliveryTime;

	public Reminder(Packet packet, ParcelState state, Vehicle deliveringTruck, long deliveryTime) {
		super(packet, Truck.class);
		this.state = state;
		this.deliveringTruck = deliveringTruck;
		this.deliveryTime = deliveryTime;
	}

	public ParcelState getState() {
		return state;
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
		visitor.visitReminder(this);
	}

}
