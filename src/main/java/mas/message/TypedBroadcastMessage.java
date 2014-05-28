package mas.message;

import rinde.sim.core.model.communication.CommunicationAPI;
import rinde.sim.core.model.communication.CommunicationUser;

/**
 * A broadcast message sent to all possible recipients of the given recipient
 * type.
 * 
 * @param <S>
 *            The sender type.
 * @param <R>
 *            The recipient type.
 */
public abstract class TypedBroadcastMessage<S extends CommunicationUser, R extends CommunicationUser>
		extends AbstractMessage<S> {

	private final Class<R> recipientType;

	public TypedBroadcastMessage(S sender, Class<R> recipientType) {
		super(sender);
		this.recipientType = recipientType;
	}

	protected Class<R> getRecipientType() {
		return recipientType;
	}

	@Override
	public void transmit(CommunicationAPI api) {
		api.broadcast(this, recipientType);
	}

}