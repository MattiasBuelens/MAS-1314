package mas.action;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Set;

import javax.annotation.concurrent.Immutable;

import mas.Packet;
import rinde.sim.core.graph.Point;

import com.google.common.collect.ImmutableSet;

@Immutable
public class SimulationState {

	private final long time;
	private final Point position;
	private final ImmutableSet<Packet> pickedUp;
	private final ImmutableSet<Packet> delivered;

	public SimulationState(long time, Point position,
			Set<? extends Packet> pickedUp) {
		this(time, position, pickedUp, ImmutableSet.<Packet> of());
	}

	protected SimulationState(long time, Point position,
			Set<? extends Packet> pickedUp, Set<? extends Packet> delivered) {
		this.time = time;
		this.position = position;
		this.pickedUp = ImmutableSet.copyOf(pickedUp);
		this.delivered = ImmutableSet.copyOf(delivered);
	}

	public long getTime() {
		return time;
	}

	public Point getPosition() {
		return position;
	}

	public ImmutableSet<Packet> getPickedUp() {
		return pickedUp;
	}

	public ImmutableSet<Packet> getDelivered() {
		return delivered;
	}

	public boolean isPickedUp(Packet packet) {
		return getPickedUp().contains(packet) || isDelivered(packet);
	}

	public boolean isDelivered(Packet packet) {
		return getDelivered().contains(packet);
	}

	public SimulationState nextState(long newTime) {
		return nextState(newTime, getPosition());
	}

	public SimulationState nextState(long newTime, Point newPosition) {
		return nextState(newTime, newPosition, getPickedUp(), getDelivered());
	}

	public SimulationState nextState(long newTime,
			Set<? extends Packet> newPickedUp) {
		return nextState(newTime, getPosition(), newPickedUp, getDelivered());
	}

	public SimulationState nextState(long newTime,
			Set<? extends Packet> newPickedUp,
			Set<? extends Packet> newDelivered) {
		return nextState(newTime, getPosition(), newPickedUp, newDelivered);
	}

	public SimulationState nextState(long newTime, Point newPosition,
			Set<? extends Packet> newPickedUp,
			Set<? extends Packet> newDelivered) {
		checkArgument(newTime >= getTime(),
				"New time must be after current time");
		return new SimulationState(newTime, newPosition, newPickedUp,
				newDelivered);
	}

}
