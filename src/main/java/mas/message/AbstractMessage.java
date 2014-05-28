package mas.message;

import rinde.sim.core.model.communication.CommunicationAPI;
import rinde.sim.core.model.communication.CommunicationUser;
import rinde.sim.core.model.communication.Message;

/**
 * Base class for messages.
 * 
 * @param <S>
 *            The sender type.
 */
public abstract class AbstractMessage<S extends CommunicationUser> extends
		Message {

	public AbstractMessage(S sender) {
		super(sender);
	}

	@Override
	@SuppressWarnings("unchecked")
	public S getSender() {
		return (S) super.getSender();
	}

	public abstract void transmit(CommunicationAPI api);

}