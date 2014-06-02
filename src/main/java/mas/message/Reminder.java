package mas.message;

import mas.Packet;
import mas.PacketInfo;
import mas.Truck;

public class Reminder extends TypedBroadcastMessage<Packet, Truck> implements
		TruckMessage {

	private final PacketInfo info;

	public Reminder(Packet packet, PacketInfo info) {
		super(packet, Truck.class);
		this.info = info;
	}

	public PacketInfo getInfo() {
		return info;
	}

	@Override
	public void accept(TruckMessageVisitor visitor) {
		visitor.visitReminder(this);
	}

}
