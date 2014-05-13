package mas.daan;

public class Proposal extends PackageMessage {

	private double ETA;

	public Proposal(Package sender, double ETA) {
		super(sender);
		this.ETA = ETA;
	}

	public double getETA() {
		return ETA;
	}

	@Override
	public void accept(MessageVisitor visitor) {
		visitor.visitProposal(this);
	}

}
