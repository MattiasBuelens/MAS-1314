package mas.daan;

public class Proposal extends PackageMessage {

	private long ETA;
	private Package packet;

	public Proposal(Truck sender, Package packet, long ETA) {
		super(sender);
		this.packet =  packet;
		this.ETA = ETA;
	}

	public Package getPackage()
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
