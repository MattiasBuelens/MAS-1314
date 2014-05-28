package mas.message;

import mas.Packet;

/**
 * A message destined for a {@link Packet}.
 */
public interface PacketMessage {

	public void accept(PacketMessageVisitor visitor);

}