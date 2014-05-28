package mas.message;

import mas.Packet;
import mas.Truck;

public class Proposal extends UnicastMessage<Truck, Packet> implements
		PacketMessage {

	private long ETA;

	public Proposal(Truck sender, Packet packet, long ETA) {
		super(sender, packet);
		this.ETA = ETA;
	}

	public Packet getPacket() {
		return getRecipient();
	}

	public long getETA() {
		return ETA;
	}

	@Override
	public void accept(PacketMessageVisitor visitor) {
		visitor.visitProposal(this);
	}

}
