package mas.message;

import rinde.sim.core.model.communication.CommunicationAPI;
import rinde.sim.core.model.communication.CommunicationUser;

/**
 * A broadcast message sent to all possible recipients.
 * 
 * @param <S>
 *            The sender type.
 */
public abstract class BroadcastMessage<S extends CommunicationUser> extends
		AbstractMessage<S> {

	public BroadcastMessage(S sender) {
		super(sender);
	}

	@Override
	public void transmit(CommunicationAPI api) {
		api.broadcast(this);
	}

}