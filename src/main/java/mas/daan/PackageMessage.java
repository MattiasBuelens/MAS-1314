package mas.daan;

import mas.Packet;

public abstract class PackageMessage extends AbstractMessage {

	protected final Packet sender;

	public PackageMessage(Packet sender) {
		super(sender);
		this.sender = sender;
	}

	@Override
	public Packet getSender() {
		return sender;
	}

}