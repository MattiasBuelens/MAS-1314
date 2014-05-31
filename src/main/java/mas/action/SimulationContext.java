package mas.action;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Set;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.pdp.Parcel;

import com.google.common.collect.ImmutableSet;

public class SimulationContext {

	private final long time;
	private final Point position;
	private final ImmutableSet<Parcel> packets;

	public SimulationContext(long time, Point position,
			Set<? extends Parcel> packets) {
		this.time = time;
		this.position = position;
		this.packets = ImmutableSet.copyOf(packets);
	}

	public long getTime() {
		return time;
	}

	public Point getPosition() {
		return position;
	}

	public ImmutableSet<Parcel> getPackets() {
		return packets;
	}

	public SimulationContext next(long newTime) {
		return next(newTime, getPosition());
	}

	public SimulationContext next(long newTime, Point newPosition) {
		return next(newTime, newPosition, getPackets());
	}

	public SimulationContext next(long newTime, Set<? extends Parcel> newPackets) {
		return next(newTime, getPosition(), newPackets);
	}

	public SimulationContext next(long newTime, Point newPosition,
			Set<? extends Parcel> newPackets) {
		checkArgument(newTime >= getTime(),
				"New time must be after current time");
		return new SimulationContext(newTime, newPosition, newPackets);
	}

}
