package mas.message;

import mas.Packet;
import mas.Truck;

public class Reminder extends TypedBroadcastMessage<Packet, Truck> implements
		TruckMessage {

	public Reminder(Packet sender) {
		super(sender, Truck.class);
	}

	public void accept(TruckMessageVisitor visitor) {
		visitor.visitReminder(this);
	}

}
