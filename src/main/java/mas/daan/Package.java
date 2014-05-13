package mas.daan;

import java.util.Queue;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.communication.CommunicationAPI;
import rinde.sim.core.model.communication.CommunicationModel;
import rinde.sim.core.model.communication.CommunicationUser;
import rinde.sim.core.model.communication.Mailbox;
import rinde.sim.core.model.communication.Message;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.core.model.road.RoadUser;


public class Package implements RoadUser, CommunicationUser
{
	public final String name;
	private Point Position;
	private final double deliveryLimit;
	// TODO initialiseren op een heel hoge waarde
	private double bestETA;
	private Point goal;
	private Mailbox mailbox;
	private Truck assignedTruck;


	private CommunicationModel cm;

	// Tickcounter houdt bij om de hoeveel keren een broadcast met de verbonden truck en
	// tijd gegeven wordt
	private int tickCounter;
	private int broadcast;

	public Package(String name, Point location) 
	{

		//TODO waar wordt de deliveryLimit geset???
		this.name = name;
		this.Position = location;
	}



	public void run()
	{
		checkMessages();
		tickCounter++;
		if (tickCounter == broadcast)
		{
			broadcast();
		}

	}

	private void checkMessages()
	{
		Queue<Message> messages = mailbox.getMessages();
		boolean newETA = false;
		if (messages.peek() != null)
		{
			for(Message m : messages)
			{
				if (m instanceof Proposal)
				{
					if (((Proposal) m).getETA() < this.bestETA)
					{
						// TODO moet er bij deze typecasting nog getried/catched worden?
						this.bestETA = ((Proposal) m).getETA();
						this.assignedTruck = ((Truck) ((Proposal) m).getSender());
						newETA = true;
					}
				}
			}
		}
		if (newETA)
		{
			broadcast();
		}		  
	}

	private void broadcast()
	{
		//TODO waar wordt CommunicationModel toegewezen?
		// TODO: klopt dit met null referentie?
		Message m;
		if( this.getTruck() == null)
		{
			m = new NewPackage(((CommunicationUser) this));
		}
		else
		{
			m = new Reminder(((CommunicationUser) this));	
		}
		cm.broadcast(m);
		tickCounter = 0;
	}

	public Truck getTruck()
	{
		return assignedTruck;
	}
	
	public Point getGoal()
	{
		return goal;
	}

	@Override
	public String toString() 
	{
		return name;
	}

	@Override
	public void initRoadUser(RoadModel model) 
	{
		model.addObjectAt(((RoadUser) this), Position);
	}

	public Point getPosition() 
	{
		return Position;
	}



	public void setCommunicationAPI(CommunicationAPI api) {
		// TODO Auto-generated method stub
		
	}



	public double getRadius() {
		// TODO Auto-generated method stub
		return 0;
	}



	public double getReliability() {
		// TODO Auto-generated method stub
		return 0;
	}



	public void receive(Message message) {
		// TODO Auto-generated method stub
		
	}
}




