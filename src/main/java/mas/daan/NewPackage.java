package mas.daan;

public class NewPackage extends PackageMessage {

	// CommunicationUser is hier een package, alle nodige info zit (denk ik) in die sender
	public NewPackage(Package sender) {
		super(sender);
	}

	@Override
	public void accept(MessageVisitor visitor) {
		visitor.visitNewPackage(this);
	}

}
