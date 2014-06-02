package mas.message;

import mas.Packet;
import mas.Truck;

public class Proposal extends UnicastMessage<Truck, Packet> implements
		PacketMessage {

	private long deliveryTime;

	public Proposal(Truck sender, Packet packet, long deliveryTime) {
		super(sender, packet);
		this.deliveryTime = deliveryTime;
	}

	public Packet getPacket() {
		return getRecipient();
	}

	public long getDeliveryTime() {
		return deliveryTime;
	}

	@Override
	public void accept(PacketMessageVisitor visitor) {
		visitor.visitProposal(this);
	}

}
