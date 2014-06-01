package mas.action;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Set;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.pdp.Parcel;

import com.google.common.collect.ImmutableSet;

public class SimulationState {

	private final long time;
	private final Point position;
	private final ImmutableSet<Parcel> packets;

	public SimulationState(long time, Point position,
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

	public SimulationState nextState(long newTime) {
		return nextState(newTime, getPosition());
	}

	public SimulationState nextState(long newTime, Point newPosition) {
		return nextState(newTime, newPosition, getPackets());
	}

	public SimulationState nextState(long newTime,
			Set<? extends Parcel> newPackets) {
		return nextState(newTime, getPosition(), newPackets);
	}

	public SimulationState nextState(long newTime, Point newPosition,
			Set<? extends Parcel> newPackets) {
		checkArgument(newTime >= getTime(),
				"New time must be after current time");
		return new SimulationState(newTime, newPosition, newPackets);
	}

}
