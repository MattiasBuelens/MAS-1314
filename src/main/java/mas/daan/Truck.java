package mas.daan;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import mas.Packet;
import mas.Plan;
import mas.action.*;

import org.apache.commons.math3.random.RandomGenerator;

import rinde.sim.core.TickListener;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.communication.CommunicationAPI;
import rinde.sim.core.model.communication.CommunicationUser;
import rinde.sim.core.model.communication.Mailbox;
import rinde.sim.core.model.communication.Message;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.pdp.Vehicle;
import rinde.sim.core.model.road.MovingRoadUser;
import rinde.sim.core.model.road.RoadModel;

public class Truck extends Vehicle implements MovingRoadUser, TickListener,  CommunicationUser {

	private static final Exception InvalidRouteException = null;
	protected RoadModel roadModel;
	protected final RandomGenerator rnd;

	private final double VEHICLE_SPEED;
	private final double RADIUS;
	private final double RELIABILITY;
	private final Mailbox mailbox;
	private LinkedList<Packet> commitments;
	private Plan plan;

	private Set<Packet> pickedUp;
	private Set<Packet> newbies;
	private Set<Packet> pool;


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

		//execute actions
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
			Packet sender = reminder.getSender();
			if (commitments.contains(sender))
			{
				Truck t = (Truck) sender.getVehicle();
				if (t != Truck.this)
				{
					commitments.remove(sender.getPosition());
					commitments.remove(sender.getDestination());
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


	private void extendRoute(Set<Packet> set, TimeLapse timeLapse)
	{
		Packet bestPacket = null;
		double bestETA = Double.MAX_VALUE;
		Plan bestPlan = null;

		// TODO while conditions are yet to be set: 1 extra package per tick, 3, as many as you want? 
		while(!set.isEmpty())	// now: until all packages are discarded, invalid or committed to
		{
			HashSet<Packet> toDelete = new HashSet<Packet>();
			for (Packet p : set)
			{
				try 
				{
					Plan currentPlan = nN(p, timeLapse);
					LinkedList<Action> list = (LinkedList<Action>) currentPlan.getSteps();
					if ( ((DeliverAction)list.getLast()).getPacket().getDeliveryTimeWindow().end < bestETA)
					{
						bestPlan = currentPlan;
						bestETA = ((DeliverAction)list.getLast()).getPacket().getDeliveryTimeWindow().end;
						bestPacket = p;
					}
				}
				catch (InvalidRouteException exception)
				{
					toDelete.add(p);
				}	
			}

			if (bestPlan != null)
			{
				this.plan = bestPlan;
				commitments.add(bestPacket);
				// TODO broadcasten
				Proposal prop = new Proposal(this, bestPacket, bestPlan.getETAOf(bestPacket, timeLapse, this));
			}

			for (Packet p : toDelete)
			{
				set.remove(p);
			}
			bestPlan = null;
			bestETA = Double.MAX_VALUE;
			bestPacket = null;
		}		
	}


	//EERSTE NN: originele nN met extra check: als er moet gewacht worden, kies dan een ander punt 
	// geimplementeerd via boolean waitingAllowed die aangezet wordt als er geen viabele opties zijn 
	// nadat alles op de normale drie checks gepasseerd is. Conditie die dit implementeert is wat ingewikkeld
	private Plan nN(Packet p, TimeLapse timeLapse) throws InvalidRouteException
	{
		Point lastPoint = this.getPosition();
		Point closestPoint = null;
		double shortest = Double.MAX_VALUE;
		long simTime = timeLapse.getTime();
		HashMap<Point, Packet> allPoints = new HashMap<Point, Packet>(this.getPlan().getPath());
		HashSet<Packet> container = new HashSet<Packet>(pickedUp);
		LinkedList<Action> newRoute = new LinkedList<Action>();
		allPoints.put(p.getPosition(), p);
		allPoints.put(p.getDestination(), p);
		boolean waitingAllowed = false;

		while (allPoints.size() != 0)
		{
			for (Point point : allPoints.keySet())
			{
				double currentDistance = Point.distance(lastPoint, point);
				long currentSimTime = simTime + new Double(currentDistance/this.getSpeed()).longValue();
				
				if (this.checkTimeConstraints(currentSimTime, allPoints.get(point), container.contains(p)))
					throw new InvalidRouteException("Time constraint is violated");		
				if (currentDistance < shortest)
				{
					if (this.isValidPoint(point, allPoints.get(point), currentSimTime, container.contains(p), waitingAllowed))
					{
						closestPoint = point;
						shortest = currentDistance;
					}
				}				
			}

			if (shortest == Double.MAX_VALUE) //no fitting value
				waitingAllowed = true;
			else
			{
				simTime =+ new Double(Point.distance(lastPoint, closestPoint)/this.getSpeed()).longValue();
				newRoute.addAll(this.selectAction(simTime, closestPoint, allPoints.get(closestPoint)));	
				lastPoint = closestPoint;
				shortest = Double.MAX_VALUE;
				waitingAllowed = false;
				if (!container.contains(allPoints.get(closestPoint)))
					container.add(allPoints.get(closestPoint));
				allPoints.remove(closestPoint);
			}
		}
		return new Plan(newRoute);
	} 

	private boolean checkTimeConstraints(long simTime, Packet packet, boolean inContainer)
	{
		if (inContainer)
		{
			if (simTime < packet.getDeliveryTimeWindow().end)
				return true;
		}
		else
		{
			if (simTime < packet.getPickupTimeWindow().end)
				return true;
		}
		return false;
	}
	
	
	private boolean isValidPoint(Point point, Packet packet, long simTime, boolean inContainer, 
			boolean waitingAllowed)
	{
		if (point == packet.getPosition())
		{
			if (packet.getPickupTimeWindow().isIn(simTime))
				return true;
			else if (waitingAllowed)
				return true;
		}
		else if (point == packet.getDestination())
		{
			if (inContainer)
			{
				if (packet.getDeliveryTimeWindow().isIn(simTime))
					return true;
				else if (waitingAllowed)
					return true;
			}
		}
		return false;	
	}

	
	private LinkedList<Action> selectAction(long time, Point point, Packet packet)
	{
		LinkedList<Action> actions = new LinkedList<Action>();
		actions.add(new MoveAction(point));
		if (point == packet.getPosition())
		{	
			if (!packet.getPickupTimeWindow().isIn(time))
				actions.add(new WaitAction(packet.getPickupTimeWindow().begin));
			actions.add(new PickupAction(packet));
		}	
		else
		{
			if (!packet.getDeliveryTimeWindow().isIn(time))
				actions.add(new WaitAction(packet.getDeliveryTimeWindow().begin));
			actions.add(new DeliverAction(packet));
		}
		return actions;
	}


	/**

	//TWEEDE NN: waiting allowed, maar er is nog een failsafe met de invalid routes dus te lang wachten 
	// wordt nog gestraft, is eigenlijk ons algoritme voor we wisten van windows
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
						// TODO waarom geeft dit geen error terwjil het weer vergelijkt met een long
						boolean checkPickupLimit = (totalDistance + Point.distance(lastPoint, rp.getPoint())/this.getSpeed()) > p.getPickupLimit();
						if ( checkPickupLimit) 
						{
							shortestRP = rp;
							shortest = Point.distance(lastPoint, rp.getPoint());
						}
					}
				}				
			}
			// loop is over, shortest distance selected

			totalDistance =+ Point.distance(lastPoint,  shortestRP.getPoint());
			if ( timeLapse.getTime() + new Double(totalDistance/this.getSpeed()).longValue() > p.getDeliveryLimit() )
				throw new InvalidRouteException("ETA constraint violated, invalid route");
			else 
			{
				newRoute.addLast(shortestRP);
				allPoints.remove(shortestRP);
				lastPoint = shortestRP.getPoint();
				shortest = Double.MAX_VALUE;
				if (shortestRP.getPoint() == shortestRP.getPacket().getPosition())
					// simulation of picked up packets to ensure the right order
					container.add(shortestRP.getPacket());
					// not necessary to reinitialize shortestRP I suppose?
			}

		}

		// At this point the ideal route is created
		// Check Route for changes to be made!
		Route routeObject = new Route(newRoute);
		return routeObject;
	} 

	 */

	/**
	//DERDE NN: gelimiteerd naar het checken van 5 punten. Validiteit van de suboptimale oplossing is niet gegarandeerd
	// en er zijn twee nNs nodig, tenzij je er een extra attribuut bij voegt dat de korte versie aan en uit zet
	// plus er is een nieuwe extendRoute nodig

	private void extendRoute(Set<Package> set, TimeLapse timeLapse)
	{
		Package bestPackage = null;
		double bestETA = Double.MAX_VALUE;
		Route currentRoute;
		Route bestRoute = null;

		// TODO while conditions are yet to be set: 1 extra package per tick, 3, as many as you want? 
		while(!set.isEmpty())	
		{
			HashSet<Package> toDelete = new HashSet<Package>();
			LinkedList<Route> routes = new LinkedList<Route>();
			for (Package p : set)
			{
				try 
				{
					currentRoute = nN(p, timeLapse, true);
					routes.add(currentRoute);
				}
				catch (InvalidRouteException exception)
				{
					// delete invalid package
					toDelete.add(p);
				}	
			}

			for (Package p : toDelete)
			{
				set.remove(p);
			}

			// TODO NOG OPLOSSEN
			Collections.sort(routes, Ordering.natural().onResultOf(
					new Function<Route, double>() 
					{
						public double apply(Route from) 
						{
							return from.getLatestETA(timeLapse, this);
						}
					}
			));


		}

		private Route nN(Package p, TimeLapse timeLapse, boolean shortCalc) throws InvalidRouteException
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

			boolean condition = allPoints.size() != 0;
			int i = 0;
			if (shortCalc)
				condition =  i < 5;

			//TODO: hier opties nog eens uitleggen om loop te vermijden
			while (condition)
			{
				for (RoutePoint rp : allPoints)
				{
					if (Point.distance(lastPoint, rp.getPoint()) < shortest)
					{
						if ( !(!container.contains(rp.getPacket()) && rp.getPoint() == rp.getPacket().getGoal() ) )
						{
							// TODO waarom geeft dit geen error terwjil het weer vergelijkt met een long
							boolean checkPickupLimit = (totalDistance + Point.distance(lastPoint, rp.getPoint())/this.getSpeed()) > p.getPickupLimit();
							if ( checkPickupLimit ) 
							{
								shortestRP = rp;
								shortest = Point.distance(lastPoint, rp.getPoint());
							}
						}
					}				
				}
				// loop is over, shortest distance selected

				totalDistance =+ Point.distance(lastPoint,  shortestRP.getPoint());
				if ( timeLapse.getTime() + new Double(totalDistance/this.getSpeed()).longValue() > p.getDeliveryLimit() )
					throw new InvalidRouteException("ETA constraint violated, invalid route");
				else 
				{
					newRoute.addLast(shortestRP);
					allPoints.remove(shortestRP);
					lastPoint = shortestRP.getPoint();
					shortest = Double.MAX_VALUE;
					if (shortestRP.getPoint() == shortestRP.getPacket().getPosition())
						// simulation of picked up packets to ensure the right order
						container.add(shortestRP.getPacket());
					// not necessary to reinitialize shortestRP I suppose?
				}
				i++;	

			}

			// At this point the ideal route is created
			// Check Route for changes to be made!
			Route routeObject = new Route(newRoute);
			return routeObject;
		} 

	 */

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

	public Plan getPlan()
	{
		return this.plan;
	}


	@Override
	protected void tickImpl(TimeLapse time) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void initRoadPDP(RoadModel pRoadModel, PDPModel pPdpModel) {
		// TODO Auto-generated method stub
		
	}


}








