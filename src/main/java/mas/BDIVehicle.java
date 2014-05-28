package mas;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import javax.annotation.Nullable;

import mas.timer.Timer;
import mas.timer.TimerCallback;
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

public abstract class BDIVehicle extends Vehicle implements CommunicationUser {

	private CommunicationAPI commAPI;
	private final Mailbox mailbox = new Mailbox();

	@Nullable
	private Plan<BDIVehicle> plan;

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

	protected Plan<BDIVehicle> getPlan() {
		return plan;
	}

	protected boolean hasValidPlan() {
		return hasPlan() && !isSucceeded() && !isImpossible();
	}

	protected abstract boolean updateBeliefs(Queue<Message> messages);

	protected abstract boolean isSucceeded();

	protected abstract boolean isImpossible();

	protected abstract boolean shouldReconsider();

	protected abstract Plan<BDIVehicle> reconsider();

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

	protected void send(CommunicationUser recipient, Message message) {
		commAPI.send(recipient, message);
	}

	protected void broadcast(Message message) {
		commAPI.broadcast(message);
	}

	protected void broadcast(Message message,
			Class<? extends CommunicationUser> type) {
		commAPI.broadcast(message, type);
	}

	@Override
	public void initRoadPDP(RoadModel pRoadModel, PDPModel pPdpModel) {
		// TODO
	}

	@Override
	public void receive(Message message) {
		mailbox.receive(message);
	}

	@Override
	public void setCommunicationAPI(CommunicationAPI api) {
		commAPI = api;
	}

	@Override
	public Point getPosition() {
		return getRoadModel().getPosition(this);
	}

	public MoveProgress moveTo(Point destination, TimeLapse time) {
		return getRoadModel().moveTo(this, destination, time);
	}

	public void pickup(Parcel parcel, TimeLapse time) {
		getPDPModel().pickup(this, parcel, time);
	}

	public void deliver(Parcel parcel, TimeLapse time) {
		getPDPModel().deliver(this, parcel, time);
	}

	public boolean containsPacket(Parcel parcel) {
		return getPDPModel().containerContains(this, parcel);
	}

}