package mas.daan;

import rinde.sim.core.model.communication.CommunicationUser;
import rinde.sim.core.model.communication.Message;

public class NewPackage extends Message {

	// CommunicationUser is hier een package, alle nodige info zit (denk ik) in die sender
	public NewPackage(CommunicationUser sender) 
	{
		super(sender);

	}
	
	

}
