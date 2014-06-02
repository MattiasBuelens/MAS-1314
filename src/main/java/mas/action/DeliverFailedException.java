package mas.action;

import mas.Packet;

public class DeliverFailedException extends ActionFailedException {

	private static final long serialVersionUID = 1L;

	private final Packet packet;

	public DeliverFailedException(Packet packet, Throwable cause) {
		super(cause);
		this.packet = packet;
	}

	public DeliverFailedException(Packet packet) {
		super();
		this.packet = packet;
	}

	public Packet getPacket() {
		return packet;
	}

}
