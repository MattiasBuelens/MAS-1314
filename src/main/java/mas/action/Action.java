package mas.action;

import rinde.sim.core.TimeLapse;

public interface Action<T> {

	public boolean execute(T target, TimeLapse time);
	
}
