package mas.message;

import rinde.sim.core.model.communication.CommunicationAPI;
import rinde.sim.core.model.communication.CommunicationUser;

/**
 * A unicast message sent to one specific recipient.
 * 
 * @param <S>
 *            The sender type.
 * @param <R>
 *            The recipient type.
 */
public abstract class UnicastMessage<S extends CommunicationUser, R extends CommunicationUser>
		extends AbstractMessage<S> {

	protected final R recipient;

	public UnicastMessage(S sender, R recipient) {
		super(sender);
		this.recipient = recipient;
	}

	protected R getRecipient() {
		return recipient;
	}

	@Override
	public void transmit(CommunicationAPI api) {
		api.send(recipient, this);
	}

}