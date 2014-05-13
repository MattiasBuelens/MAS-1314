package mas.daan;
import rinde.sim.core.graph.Point;


public class RoutePoint {
	
	private final Point point;
	private final Package packet;
	

	public RoutePoint(Point pt, Package pe) 
	{
		point= pt;
		packet = pe;
	}


	public Point getPoint() {
		return point;
	}

	public Package getPacket() {
		return packet;
	}


}
