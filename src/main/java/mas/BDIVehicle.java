package mas;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import javax.annotation.Nullable;
import javax.measure.quantity.Duration;
import javax.measure.quantity.Length;
import javax.measure.quantity.Velocity;
import javax.measure.unit.Unit;

import mas.message.AbstractMessage;
import mas.timer.Timer;
import mas.timer.TimerCallback;

import org.apache.commons.math3.random.RandomGenerator;
import org.jscience.physics.amount.Amount;

import rinde.sim.core.SimulatorAPI;
import rinde.sim.core.SimulatorUser;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.communication.CommunicationAPI;
import rinde.sim.core.model.communication.CommunicationUser;
import rinde.sim.core.model.communication.Mailbox;
import rinde.sim.core.model.communication.Message;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.pdp.Vehicle;
import rinde.sim.core.model.road.MoveProgress;
import rinde.sim.core.model.road.RoadModel;

public abstract class BDIVehicle extends Vehicle implements CommunicationUser,
		SimulatorUser {

	private CommunicationAPI commAPI;
	private SimulatorAPI simAPI;
	private final Mailbox mailbox = new Mailbox();

	@Nullable
	private Plan plan;

	private final List<Timer> timers = new ArrayList<>();

	@Override
	protected void tickImpl(TimeLapse time) {
		// Read messages
		Queue<Message> messages = mailbox.getMessages();

		// Update beliefs
		boolean shouldReconsider = updateBeliefs(messages);

		do {
			if (!hasPlan() || shouldReconsider) {
				// Update desires + intentions + plan
				plan = reconsider();
			}
			if (hasValidPlan()) {
				getPlan().step(this, time);
				if (!hasValidPlan()) {
					plan = null;
				}
			} else {
				plan = null;
			}
		} while (hasPlan() && time.hasTimeLeft());

		// TODO Send messages

		// Run timers
		runTimers(time.getTime());
	}

	protected boolean hasPlan() {
		return getPlan() != null && !getPlan().isEmpty();
	}

	protected Plan getPlan() {
		return plan;
	}

	protected boolean hasValidPlan() {
		return hasPlan() && !isSucceeded() && !isImpossible();
	}

	protected abstract boolean updateBeliefs(Queue<Message> messages);

	protected abstract boolean isSucceeded();

	protected abstract boolean isImpossible();

	protected abstract Plan reconsider();

	/*
	 * Timers
	 */

	protected Timer addTimer(long time, TimerCallback callback) {
		Timer timer = new Timer(time, callback);
		timers.add(timer);
		return timer;
	}

	protected void cancelTimer(Timer timer) {
		timer.cancel();
		timers.remove(timer);
	}

	private void runTimers(long currentTime) {
		// Sort timers by time
		List<Timer> toRun = Timer.timeOrdering.immutableSortedCopy(timers);
		// Timers to remove
		List<Timer> toRemove = new ArrayList<>();
		// Run timers
		for (Timer timer : toRun) {
			timer.run(currentTime);
			if (!timer.isActive()) {
				toRemove.add(timer);
			}
		}
		// Remove timers
		for (Timer timer : toRemove) {
			timers.remove(timer);
		}
	}

	/*
	 * Communication
	 */

	protected void transmit(AbstractMessage<? extends BDIVehicle> message) {
		message.transmit(commAPI);
	}

	/*
	 * Actions
	 */

	public MoveProgress moveTo(Point destination, TimeLapse time) {
		return getRoadModel().moveTo(this, destination, time);
	}

	public long getEstimatedTimeBetween(Point from, Point to) {
		Amount<Length> distance = getDistanceBetween(from, to);
		Amount<Duration> duration = distance.divide(getSpeedAmount()).to(
				getTickUnit());
		return duration.longValue(getTickUnit());
	}

	public Amount<Length> getDistanceBetween(Point from, Point to) {
		List<Point> path = getRoadModel().getShortestPathTo(from, to);
		Unit<Length> unit = getRoadModel().getDistanceUnit();
		Amount<Length> distance = Amount.valueOf(0d, unit);
		Point previous = null;

		for (Point current : path) {
			if (previous != null) {
				distance = distance.plus(Amount.valueOf(
						Point.distance(previous, current), unit));
			}
			previous = current;
		}

		return distance;
	}

	public void pickup(Parcel parcel, TimeLapse time) {
		getPDPModel().pickup(this, parcel, time);
	}

	public boolean canPickupAt(Parcel parcel, long pickupTime) {
		return getPDPModel().getTimeWindowPolicy().canPickup(
				parcel.getPickupTimeWindow(), pickupTime,
				parcel.getPickupDuration());
	}

	public void deliver(Parcel parcel, TimeLapse time) {
		getPDPModel().deliver(this, parcel, time);
	}

	public boolean canDeliverAt(Parcel parcel, long deliveryTime) {
		return getPDPModel().getTimeWindowPolicy().canDeliver(
				parcel.getDeliveryTimeWindow(), deliveryTime,
				parcel.getDeliveryDuration());
	}

	public boolean containsPacket(Parcel parcel) {
		return getPDPModel().containerContains(this, parcel);
	}

	@Override
	public void receive(Message message) {
		mailbox.receive(message);
	}

	@Override
	public void setCommunicationAPI(CommunicationAPI api) {
		commAPI = api;
	}

	public abstract Amount<Length> getRadiusAmount();

	@Override
	public final double getRadius() {
		return getRadiusAmount().doubleValue(getRoadModel().getDistanceUnit());
	}

	public abstract Amount<Velocity> getSpeedAmount();

	@Override
	public final double getSpeed() {
		return getSpeedAmount().doubleValue(getRoadModel().getSpeedUnit());
	}

	@Override
	public Point getPosition() {
		return getRoadModel().getPosition(this);
	}

	@Override
	public void initRoadPDP(RoadModel pRoadModel, PDPModel pPdpModel) {
	}

	protected final RandomGenerator getRandomGenerator() {
		return simAPI.getRandomGenerator();
	}

	protected final Unit<Duration> getTickUnit() {
		return simAPI.getTimeUnit();
	}

	@Override
	public void setSimulator(SimulatorAPI api) {
		simAPI = api;
	}

}