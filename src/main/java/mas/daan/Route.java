package mas.daan;

import java.util.LinkedList;

import rinde.sim.core.TimeLapse;
import rinde.sim.core.graph.Point;

public class Route 
{

	private LinkedList<RoutePoint> path;
	private LinkedList<Point> route;
	private final double length;
	
	public Route (LinkedList<RoutePoint> path)
	{
		this.path = path;
		this.toRoute(path);
		// at runtime berekenen
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

	public double getLatestETA(TimeLapse timeLapse, Truck truck)
	{
		double latestETA = timeLapse.getTime();
		Point lastPoint = truck.getPosition();
		for(RoutePoint rp : path)
		{
			//time to get there
			latestETA =+ Point.distance(lastPoint, rp.getPoint())/truck.getSpeed();
			//wait or not?
			if ( rp.getPoint() == rp.getPacket().getPosition())
			{
				double diff = rp.getPacket().getPickupLimit() - latestETA;
				if(diff > 0)
				{
					latestETA =+ diff;
				}
			}
			lastPoint = rp.getPoint();
		}
		return latestETA;
	}

















}
