package mas.plan;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;

import mas.BDIVehicle;
import mas.action.Action;
import mas.action.ActionFailedException;
import rinde.sim.core.TimeLapse;

public class Plan {

	private final Queue<Action> queue;

	public Plan(Collection<Action> plan) {
		this.queue = new LinkedList<>(plan);
	}

	public boolean isEmpty() {
		return queue.isEmpty();
	}

	public Collection<Action> getSteps() {
		return Collections.unmodifiableCollection(queue);
	}

	public boolean step(BDIVehicle target, TimeLapse time)
			throws ActionFailedException {
		Action step = queue.peek();
		if (step != null) {
			boolean stepDone = step.execute(target, time);
			if (stepDone) {
				queue.poll();
			}
		}
		return isEmpty();
	}

}
