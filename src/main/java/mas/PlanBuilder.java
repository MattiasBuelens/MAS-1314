package mas;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.LinkedList;

import javax.annotation.concurrent.Immutable;

import mas.action.Action;
import mas.action.IllegalActionException;
import mas.action.SimulationState;

import com.google.common.base.Predicate;

@Immutable
public class PlanBuilder {

	private final PlanBuilder previous;
	private final Action action;
	private final SimulationState state;

	public PlanBuilder(SimulationState state) {
		this(null, null, state);
	}

	protected PlanBuilder(PlanBuilder previous, Action action,
			SimulationState state) {
		this.previous = previous;
		this.action = action;
		this.state = checkNotNull(state);
	}

	public final Action getAction() {
		return action;
	}

	public final SimulationState getState() {
		return state;
	}

	public PlanBuilder nextAction(BDIVehicle target, Action action)
			throws IllegalActionException {
		checkNotNull(target);
		checkNotNull(action);
		return new PlanBuilder(this, action,
				action.simulate(target, getState()));
	}

	public Plan build() {
		LinkedList<Action> actions = new LinkedList<>();
		PlanBuilder cursor = this;
		while (cursor.previous != null) {
			actions.addFirst(cursor.action);
			cursor = cursor.previous;
		}
		return new Plan(actions);
	}

	public PlanBuilder findNewest(Predicate<? super PlanBuilder> predicate) {
		checkNotNull(predicate);
		// Rewind until predicate succeeds
		PlanBuilder cursor = this;
		do {
			if (predicate.apply(cursor)) {
				return cursor;
			}
			cursor = cursor.previous;
		} while (cursor.previous != null);
		return null;
	}

	public PlanBuilder findOldest(Predicate<? super PlanBuilder> predicate) {
		checkNotNull(predicate);
		// Rewind until predicate succeeds
		PlanBuilder oldest = findNewest(predicate);
		if (oldest == null)
			return null;
		// Rewind until predicate fails or fully rewinded
		while (oldest.previous != null && predicate.apply(oldest.previous)) {
			oldest = oldest.previous;
		}
		return oldest;
	}

}
