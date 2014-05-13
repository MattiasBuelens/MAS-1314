package mas.daan;

import rinde.sim.core.model.communication.CommunicationUser;
import rinde.sim.core.model.communication.Message;

public abstract class AbstractMessage extends Message {

	public AbstractMessage(CommunicationUser sender) {
		super(sender);
	}

	public abstract void accept(MessageVisitor visitor);

}