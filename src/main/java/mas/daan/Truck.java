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

	private static final Exception InvalidRouteException = null;
	protected RoadModel roadModel;
	protected final RandomGenerator rnd;

	private final double VEHICLE_SPEED;
	private final double RADIUS;
	private final double RELIABILITY;
	private final Mailbox mailbox;
	private LinkedList<Package> commitments;
	private Route route;

	private Set<Package> pickedUp;
	private Set<Package> newbies;
	private Set<Package> pool;


	public Truck(RandomGenerator r, double speed, double radius, double reliability ) 
	{
		rnd = r;
		VEHICLE_SPEED = speed;
		RADIUS = radius;
		RELIABILITY = reliability;
		mailbox =  new Mailbox ();
	}


	public void tick(TimeLapse timeLapse)
	{
		// checkMessages(): build pool
		boolean packagesLost = checkMessages();
		extendRoute(newbies, timeLapse);
		if(packagesLost)
		{
			extendRoute(pool, timeLapse);
		}
		
		// TODO:  hoe wordt er vooruitgegaan? while loop 'while time left'?
		// ofwel:
		roadModel.moveTo(this, route.getRoute().get(0), timeLapse);
		// ofwel:
		roadModel.followPath(this, this.route.getRoute(), timeLapse);
	} 	

	private boolean checkMessages()
	{
		// Hier voorzichtig mee omgaan, dit heb je maar eenmaal
		Queue<Message> messages = mailbox.getMessages();
		MessageHandler handler = new MessageHandler();
		for (Message m : messages)
		{
			// TODO nog foutje?
			((AbstractMessage) m).accept(handler);
		} 
		return handler.hasLostPackages();
	}

	private class MessageHandler implements MessageVisitor {

		private boolean packagesLost = false;

		public boolean hasLostPackages() {
			return packagesLost;
		}

		@Override
		public void visitNewPackage(NewPackage newPackage) 
		{
			newbies.add(newPackage.getSender());
		}

		@Override
		public void visitProposal(Proposal proposal) 
		{
			// TODO Betekenis voor Truck != betekenis voor Package. Waar wordt dat verschil in verwerkt?
		}

		@Override
		public void visitReminder(Reminder reminder) 
		{
			// TODO waarom werkt dit niet?
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

	
	// set is not yet cloned, so the set is empty when the method has finished!
	private void extendRoute(Set<Package> set, TimeLapse timeLapse)
	{
		Package bestPackage = null;
		double bestETA = Double.MAX_VALUE;
		Route currentRoute;
		Route bestRoute = null;

		// TODO while conditions are yet to be set: 1 extra package per tick, 3, as many as you want? 
		while(!set.isEmpty())	// now: until all packages are discarded, invalid or committed to
		{
			HashSet<Package> toDelete = new HashSet<Package>();
			for (Package p : set)
			{
				try 
				{
					currentRoute = nN(p, timeLapse);
					
					// Or Route works with ETA, or with length
					// TODO change ETA to length
					if (currentRoute.getLatestETA()<bestETA)
					{
						bestRoute = currentRoute;
						bestETA = currentRoute.getLatestETA();
						// why save this info?
						bestPackage = p;
					}
				}
				catch (InvalidRouteException exception)
				{
					// delete invalid package
					toDelete.add(p);
				}	
			}

			if (bestRoute != null)
			{
				this.route = bestRoute;
				commitments.add(bestPackage);
				// TODO aanpassen van ETA raadplegen in Route naar length en timeLapse
				Proposal prop = new Proposal(this, bestPackage, bestRoute.getETAOf(bestPackage, timeLapse, this));
			}
			
			// Delete invalid, discarded packages
			for (Package p : toDelete)
			{
				set.remove(p);
			}
			
			//Reinitialize variables
			bestRoute = null;
			bestETA = Double.MAX_VALUE;
			bestPackage = null;
		}		
	}

	
	private Route nN(Package p, TimeLapse timeLapse) throws InvalidRouteException
	{
		Point lastPoint = this.getPosition();
		double totalDistance = 0;
		double shortest = Double.MAX_VALUE;
		RoutePoint shortestRP = null;

		LinkedList<RoutePoint> allPoints = new LinkedList<RoutePoint>(this.getRoute().getPath());
		LinkedList<RoutePoint> newRoute = new LinkedList<RoutePoint>();
		HashSet<Package> container = new HashSet<Package>(pickedUp);

		allPoints.add(new RoutePoint(p.getPosition(), p));
		allPoints.add(new RoutePoint(p.getGoal(), p));

		//TODO: hier opties nog eens uitleggen om loop te vermijden
		while (allPoints.size() != 0)
		{
			for (RoutePoint rp : allPoints)
			{
				if (Point.distance(lastPoint, rp.getPoint()) < shortest)
				{
					if ( !(!container.contains(rp.getPacket()) && rp.getPoint() == rp.getPacket().getGoal() ) )
					{
						shortestRP = rp;
						shortest = Point.distance(lastPoint, rp.getPoint());
					}
				}				
			}
			// loop is over, shortest distance selected

			totalDistance =+ Point.distance(lastPoint,  shortestRP.getPoint());
			if ( timeLapse.getTime() + new Double(totalDistance/this.getSpeed()).longValue() > p.getDeliveryLimit() )
			{
				new InvalidRouteException("ETA constraint violated, invalid route");
			}
			else 
			{
				newRoute.addLast(shortestRP);
				allPoints.remove(shortestRP);
				lastPoint = shortestRP.getPoint();
				shortest = Double.MAX_VALUE;
				if (shortestRP.getPoint() == shortestRP.getPacket().getPosition())
				{
					// simulation of picked up packets to ensure the right order
					container.add(shortestRP.getPacket());
				}
				// not necessary to reinitialize shortestRP I suppose?
			}
		}

		// At this point the ideal route is created
		// Check Route for changes to be made!
		Route routeObject = new Route(newRoute, timeLapse.getTime() + new Double(totalDistance/this.getSpeed()).longValue());
		return routeObject;
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
		return this.VEHICLE_SPEED;
	}

	public double getRadius() 
	{
		return this.RADIUS;
	}

	public double getReliability()
	{
		return this.RELIABILITY;
	}

	public Point getPosition()
	{
		return this.roadModel.getPosition(this);
	}

	public Route getRoute()
	{
		return this.route;
	}

	public void setCommunicationAPI(CommunicationAPI api) {
		// TODO Auto-generated method stub

	}

	public void receive(Message message) {
		// TODO Auto-generated method stub

	}


}








