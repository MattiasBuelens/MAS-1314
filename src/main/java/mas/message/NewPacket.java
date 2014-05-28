package mas.message;

import mas.Packet;
import mas.Truck;

public class NewPacket extends TypedBroadcastMessage<Packet, Truck> implements
		TruckMessage {

	public NewPacket(Packet sender) {
		super(sender, Truck.class);
	}

	public Packet getPacket() {
		return getSender();
	}

	@Override
	public void accept(TruckMessageVisitor visitor) {
		visitor.visitNewPacket(this);
	}

}
