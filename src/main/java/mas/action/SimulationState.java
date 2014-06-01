package mas.action;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Set;

import javax.annotation.concurrent.Immutable;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.pdp.Parcel;

import com.google.common.collect.ImmutableSet;

@Immutable
public class SimulationState {

	private final long time;
	private final Point position;
	private final ImmutableSet<Parcel> pickedUp;
	private final ImmutableSet<Parcel> delivered;

	public SimulationState(long time, Point position,
			Set<? extends Parcel> pickedUp) {
		this(time, position, pickedUp, ImmutableSet.<Parcel> of());
	}

	protected SimulationState(long time, Point position,
			Set<? extends Parcel> pickedUp, Set<? extends Parcel> delivered) {
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

	public ImmutableSet<Parcel> getPickedUp() {
		return pickedUp;
	}

	public ImmutableSet<Parcel> getDelivered() {
		return delivered;
	}
	
	public boolean isPickedUp(Parcel parcel) {
		return getPickedUp().contains(parcel) || isDelivered(parcel);
	}
	
	public boolean isDelivered(Parcel parcel) {
		return getDelivered().contains(parcel);
	}

	public SimulationState nextState(long newTime) {
		return nextState(newTime, getPosition());
	}

	public SimulationState nextState(long newTime, Point newPosition) {
		return nextState(newTime, newPosition, getPickedUp(), getDelivered());
	}

	public SimulationState nextState(long newTime,
			Set<? extends Parcel> newPickedUp) {
		return nextState(newTime, getPosition(), newPickedUp, getDelivered());
	}

	public SimulationState nextState(long newTime,
			Set<? extends Parcel> newPickedUp,
			Set<? extends Parcel> newDelivered) {
		return nextState(newTime, getPosition(), newPickedUp, newDelivered);
	}

	public SimulationState nextState(long newTime, Point newPosition,
			Set<? extends Parcel> newPickedUp,
			Set<? extends Parcel> newDelivered) {
		checkArgument(newTime >= getTime(),
				"New time must be after current time");
		return new SimulationState(newTime, newPosition, newPickedUp,
				newDelivered);
	}

}
