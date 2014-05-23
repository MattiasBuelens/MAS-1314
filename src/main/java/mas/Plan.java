package mas;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;

import mas.action.Action;
import rinde.sim.core.TimeLapse;

public class Plan<T> {

	private final Queue<Action<T>> queue;

	public Plan(Collection<Action<T>> plan) {
		this.queue = new LinkedList<>(plan);
	}

	public boolean isEmpty() {
		return queue.isEmpty();
	}

	public Collection<Action<T>> getSteps() {
		return Collections.unmodifiableCollection(queue);
	}

	public boolean step(T target, TimeLapse time) {
		Action<T> step = queue.peek();
		if (step != null) {
			boolean stepDone = step.execute(target, time);
			if (stepDone) {
				queue.poll();
			}
		}
		return isEmpty();
	}

}
