package mas.daan;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;

import rinde.sim.core.TickListener;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.communication.CommunicationAPI;
import rinde.sim.core.model.communication.CommunicationUser;
import rinde.sim.core.model.communication.Mailbox;
import rinde.sim.core.model.communication.Message;
import rinde.sim.core.model.road.MovingRoadUser;
import rinde.sim.core.model.road.RoadModel;

public class Truck implements MovingRoadUser, TickListener,  CommunicationUser {

	protected RoadModel roadModel;
	protected final RandomGenerator rnd;
	
	private final double VEHICLE_SPEED;
	private final double RADIUS;
	private final double RELIABILITY;
	private final Mailbox mailbox;
	//Moet zo geformuleerd worden door de aard van mailbox.getMessages()
	private Queue<Message> messages;
	private Set<Package> commitments;
	private LinkedList<RoutePoint> route;
	private LinkedList<Point> path;


	private Set<Package> pickedUp;
	private Set<Package> newbies;
	private Set<Package> pool;

	// TODO nodig voor GUI in TruckSimulator, hopelijk nog verwijderen
	public static final String C_BLACK = "color.black";
	public static final String C_YELLOW = "color.yellow";
	public static final String C_GREEN = "color.green";



	public Truck(RandomGenerator r, double speed, double radius, double reliability ) {

		rnd = r;
		VEHICLE_SPEED = speed;
		RADIUS = radius;
		RELIABILITY = reliability;
		mailbox =  new Mailbox ();

	}


	public void tick(TimeLapse timeLapse)
	{
		boolean packagesLost = checkMessages();
		extendRoute(newbies);
		if(packagesLost)
		{
			extendRoute(pool);
		}

		// wat met de tijd die mogelijk op overschot is, wordt hier meteen verdergereden naar de volgende bestemming?
		
		
		// ofwel:
		roadModel.moveTo(this, path, timeLapse);
		// ofwel:
		roadModel.followPath(this, path, timeLapse);
	} 	



	// TODO oplossen, aanvullen & checken op efficientie O(n^?)

	private boolean checkMessages()
	{
		// Hier voorzichtig mee omgaan, dit heb je maar eenmaal
		messages = mailbox.getMessages();

		MessageHandler handler = new MessageHandler();

		// Sowieso een for want in elk geval is het nodig om een volledige pool te bouwen
		for( Message m : messages)
		{
			((AbstractMessage)m).accept(handler);
		} // einde van de loop, alle messages overlopen

		return handler.hasLostPackages();
		// TODO als de messages hier niet opgeslagen worden voor verder gebruik, gaan ze hier verloren		
	}
	
	private class MessageHandler implements MessageVisitor {

		private boolean packagesLost = false;
		
		public boolean hasLostPackages() {
			return packagesLost;
		}

		@Override
		public void visitNewPackage(NewPackage newPackage) {
			// TODO try/catch?
			// enkel een add, niet nodig om het bericht nadien te verwijderen uit de mailbox, niet?
			newbies.add(newPackage.getSender());
		}

		@Override
		public void visitProposal(Proposal proposal) {
			// TODO
		}

		@Override
		public void visitReminder(Reminder reminder) {
			//TODO bij memory inbouwen rond packages, hier ook implementeren
			// voor memory: van pool naar oldPool en dan zo checken of je al eens alle berekeningen gedaan hebt?
			// 3 opties, 	1. volledig onbekend = Pool
			//				2. bekend (commitments) & jezelf: chill
			//				3. bekend & niet jezelf: verwijderen
			// TODO beter typecasten in het begin aangezien een Reminder enkel kan komen van een Package
			Package sender = reminder.getSender();
			if (commitments.contains(sender))
			{
				Truck t = sender.getTruck();
				if (t != Truck.this)
				{
					// Route opnieuw berekenen of niet? grote impact? kan zijn, lijkt me
					// Geen rekening houden met packages die pickedUp zijn
					commitments.remove(sender.getPosition());
					commitments.remove(sender.getGoal());
					packagesLost = true;
				}
				// else: do nothing
			}
			else
			{
				//TODO beslissen of we dit in de pool steken of niet: enkel nieuwe packages overwegen/niet?	
				pool.add(sender);
			}
		}
		
	}



	private void extendRoute(Set<Package> set)
	{
		for(Package p : set)
		{
			LinkedList<RoutePoint> route = this.nN(p);
			// use route to calculate latest ETA
			// compare ETA with others
			
		}
		// determine what package to take on in the route
		// this.path = route;
		// + broadcast Proposal to package!
		
		// REPEAT? HOW MANY TIMES?
	}

	
	private LinkedList<RoutePoint> nN(Package p)
	{
		// NOG ALTIJD VEEL TE ZWAAR: n^5 ofzo. Dus beter een lijst bijhouden van welke pakketen
		// al eens overwogen zijn om die dan voor eeuwig te bannen;
		
		// Maak je hierin een failsafe voor tijdslimieten of niet?
		Point lastPoint = this.getPosition();
		double totalDistance = 0;
		double shortest = Double.MAX_VALUE;
		RoutePoint shortestRP = null;
		LinkedList <RoutePoint> allPoints = route;
		LinkedList <RoutePoint> newRoute = null;
		Set<Package> container = new HashSet<Package>(pickedUp);
		
		Boolean invalid = false;
		
		allPoints.add(new RoutePoint(p.getPosition(), p));
		allPoints.add(new RoutePoint(p.getGoal(), p));
		
		while (allPoints.size() != 0 && invalid == false)
		{
			for (RoutePoint rp : allPoints)
			{
				if (Point.distance(lastPoint, rp.getPoint()) < shortest)
				{
					if ( !(!container.contains(rp.getPacket()) && rp.getPoint() == rp.getPacket().getGoal() ) )
					{
						shortestRP = rp;
						shortest = Point.distance(lastPoint, rp.getPoint());
						// TODO mechanisme om deliveryLimit te implementeren adhv totalDistance => invalid
					}
				}				
			}
			
			newRoute.addLast(shortestRP);
			if (shortestRP.getPoint() == shortestRP.getPacket().getPosition())
			{
				container.add(shortestRP.getPacket());
			}
			allPoints.remove(shortestRP);
			totalDistance = totalDistance + Point.distance(lastPoint, shortestRP.getPoint());
			lastPoint = shortestRP.getPoint();
			
			shortest = Double.MAX_VALUE;
		}
		
		return newRoute;
	} 
	
	/**
	private double nearestNeighbor(Package p)
	{
		double latestETA = 0;
		if (commitments.isEmpty())
		{
			commitments.add(p);
			path.put(p.getPosition(), p);
			path.put(p.getGoal(), p);
			// graph model is hier hard gecodeerd
			// computeConnectionLenght() is protected. Is er hier een weg errond of 
			// gaan we verder via roadModel.getConnection().getLenght()?
			List<Point> list = roadModel.getShortestPathTo(this, ((RoadUser) p));
			double totalDistance =  0;
			for( int i = 0; i == (list.size()-1); i++)
			{
				totalDistance = totalDistance + Point.distance(list.get(i), list.get(i+1));
			}
			latestETA = totalDistance/VEHICLE_SPEED;	
		}
		else //commitments not empty
		{

			HashMap<Point, Package> possiblePath =  new HashMap<Point, Package>();
			HashMap<Point, Package> allPoints = path;
			allPoints.put(p.getPosition(), p);
			allPoints.put(p.getGoal(), p);

			// eerste punt instellen met eigen locatie
			double shortestDistance = Double.MAX_VALUE;
			Point lastPoint = null;

			for(Point point : allPoints.keySet())
			{
				double workDistance = Point.distance(this.getPosition(), point);
				if (workDistance < shortestDistance)
				{
					if (point == allPoints.get(point).getPosition())
					{
						shortestDistance = workDistance;
						lastPoint = point;
					}
					// point == allPoints.get(point).getGoal()
					else
					{
						if (pickedUp.contains(allPoints.get(point)))
						{
							shortestDistance = workDistance;
							lastPoint = point;
						}
					}
				}
			}

			possiblePath.put(lastPoint, allPoints.get(lastPoint));
			allPoints.remove(lastPoint);

			while (allPoints.size() != 0)
			{
				for(Point point : allPoints.keySet())
				{
					double workDistance = Point.distance(lastPoint, point);
					if (workDistance < shortestDistance)
					{
						if (point == allPoints.get(point).getPosition())
						{
							shortestDistance = workDistance;
							lastPoint = point;
						}
						// point == allPoints.get(point).getGoal()
						else
						{
							if (pickedUp.contains(allPoints.get(point)))
							{
								shortestDistance = workDistance;
								lastPoint = point;
							}
						}
					}	
				}
				possiblePath.put(lastPoint, allPoints.get(lastPoint));
				allPoints.remove(lastPoint);
			}

			path = possiblePath;

		}
		return latestETA;
	}
	
	**/

	// the MovingRoadUser interface indicates that this class can move on a
	// RoadModel. The TickListener interface indicates that this class wants
	// to keep track of time.
	public void initRoadUser(RoadModel model) {
		// this is where we receive an instance to the model. we store the
		// reference and add ourselves to the model on a random position.
		roadModel = model;
		roadModel.addObjectAt(this, roadModel.getRandomPosition(rnd));
	}

	public void afterTick(TimeLapse timeLapse) {
		// we don't need this in this example. This method is called after
		// all TickListener#tick() calls, hence the name.
	}

	public double getSpeed() {
		// the drivers speed
		return VEHICLE_SPEED;
	}
	
	public double getRadius() 
	{
		return RADIUS;
	}
	
	
	public double getReliability()
	{
		return RELIABILITY;
	}
	
	public Point getPosition()
	{
		return roadModel.getPosition(this);
	}


	public void setCommunicationAPI(CommunicationAPI api) {
		// TODO Auto-generated method stub
		
	}


	public void receive(Message message) {
		// TODO Auto-generated method stub
		
	}
	
	
	
	
	
	
	
}








