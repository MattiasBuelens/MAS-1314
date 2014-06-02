package mas.action;

import mas.Packet;

public class PickupFailedException extends ActionFailedException {

	private static final long serialVersionUID = 1L;

	private final Packet packet;

	public PickupFailedException(Packet packet, Throwable cause) {
		super(cause);
		this.packet = packet;
	}

	public PickupFailedException(Packet packet) {
		super();
		this.packet = packet;
	}

	public Packet getPacket() {
		return packet;
	}

}
