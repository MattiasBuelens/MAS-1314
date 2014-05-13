package mas.daan;

public abstract class PackageMessage extends AbstractMessage {

	protected final Package sender;

	public PackageMessage(Package sender) {
		super(sender);
		this.sender = sender;
	}

	@Override
	public Package getSender() {
		return sender;
	}

}