package mas.daan;

import rinde.sim.core.model.communication.CommunicationUser;
import rinde.sim.core.model.communication.Message;

public class Proposal extends Message {

	private double ETA;
	
	
	public Proposal(CommunicationUser sender, double ETA) 
	{
		super(sender);
		ETA = ETA;
	}
	
	
	public double getETA()
	{
		return ETA;
	}

}
