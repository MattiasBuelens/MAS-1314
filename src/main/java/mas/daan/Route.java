package mas.daan;

import java.util.LinkedList;

import rinde.sim.core.TimeLapse;
import rinde.sim.core.graph.Point;

public class Route 
{

	private LinkedList<RoutePoint> path;
	private LinkedList<Point> route;
	private final double length;
	
	// TODO
	// Discard this variable because of its timeliness. 
	// Rather save an attribute to calculate the length until a certain point/package or for the route
	//  as a whole
	private long latestETA;


	public Route (LinkedList<RoutePoint> path, long latestETA)
	{
		this.path = path;
		this.latestETA = latestETA;
		this.toRoute(path);
		this.length = this.calcLength();
	}

	private void toRoute(LinkedList<RoutePoint> rp)
	{
		LinkedList<Point> list = new LinkedList<Point>();
		for(RoutePoint r: rp)
		{
			list.addLast(r.getPoint());
		}
		this.route = list;
	}

	public long getETAOf(Package p, TimeLapse t, Truck truck)
	{
		// TODO misschien zijn er hier nog andere methodes van Rinde die kunnen gebruikt worden 

		if (this.route.contains(p))
		{
			return -1;
		}
		else
		{
			boolean found = false;
			long l = 0;
			int i = 0;
			double totDistance = Point.distance(truck.getPosition(), this.route.get(i));
			while (!found && i != route.size())
			{
				if (p == this.path.get(i).getPacket())
				{
					l = (new Double(totDistance/truck.getSpeed())).longValue() + t.getTime();
					found = true;
				}
				else
				{
					totDistance =+ Point.distance(this.route.get(i), this.route.get(i+1));
					i++;
				}
			}
			return l;
		}
	}
	
	private double calcLength()
	{
		double totDistance = 0;
		for (int i = 0; i < route.size(); i++)
		{
			totDistance =+ Point.distance(route.get(i), route.get(i+1));
		}
		return totDistance;
	}

	public LinkedList<RoutePoint> getPath()
	{
		return path;
	}

	public LinkedList<Point> getRoute()
	{
		return route;
	}

	public long getLatestETA()
	{
		return latestETA;
	}

















}
