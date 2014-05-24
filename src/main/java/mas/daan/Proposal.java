package mas.daan;

import mas.Packet;

public class Proposal extends PackageMessage {

	private long ETA;
	private Packet packet;

	// TODO is dit wel onderdeel van PacketMessage? niet van TruckMessage ofzo?
	public Proposal(Truck sender, Packet packet, long ETA) {
		super(sender);
		this.packet =  packet;
		this.ETA = ETA;
	}

	public Packet getPacket()
	{
		return this.packet;
	}
	
	public long getETA() 
	{
		return ETA;
	}

	@Override
	public void accept(MessageVisitor visitor) {
		visitor.visitProposal(this);
	}

}
