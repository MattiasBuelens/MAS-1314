package mas.message;

import mas.Truck;

/**
 * A message destined for a {@link Truck}.
 */
public interface TruckMessage {

	public void accept(TruckMessageVisitor visitor);

}