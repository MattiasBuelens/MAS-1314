package mas;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import javax.measure.quantity.Duration;
import javax.measure.quantity.Length;
import javax.measure.unit.Unit;

import mas.message.AbstractMessage;
import mas.timer.Timer;
import mas.timer.TimerCallback;

import org.jscience.physics.amount.Amount;

import rinde.sim.core.SimulatorAPI;
import rinde.sim.core.SimulatorUser;
import rinde.sim.core.TickListener;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.communication.CommunicationAPI;
import rinde.sim.core.model.communication.CommunicationUser;
import rinde.sim.core.model.communication.Mailbox;
import rinde.sim.core.model.communication.Message;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.pdp.PDPModel.PDPModelEventType;
import rinde.sim.core.model.pdp.PDPModel.ParcelState;
import rinde.sim.core.model.pdp.PDPModelEvent;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.event.Event;
import rinde.sim.event.Listener;
import rinde.sim.util.TimeWindow;

public abstract class BDIParcel extends Parcel implements CommunicationUser,
		TickListener, SimulatorUser {

	private CommunicationAPI commAPI;
	private SimulatorAPI simAPI;
	private final Mailbox mailbox = new Mailbox();

	private BDIVehicle containingVehicle;

	private final List<Timer> timers = new ArrayList<>();

	public BDIParcel(Point pDestination, long pPickupDuration,
			TimeWindow pickupTW, long pDeliveryDuration, TimeWindow deliveryTW,
			double pMagnitude) {
		super(pDestination, pPickupDuration, pickupTW, pDeliveryDuration,
				deliveryTW, pMagnitude);
	}

	@Override
	public void tick(TimeLapse time) {
		// Read messages
		Queue<Message> messages = mailbox.getMessages();

		// Update beliefs
		updateBeliefs(messages);

		// Run timers
		runTimers(time.getTime());
	}

	@Override
	public void afterTick(TimeLapse time) {
	}

	protected abstract void updateBeliefs(Queue<Message> messages);

	protected ParcelState getState() {
		return getPDPModel().getParcelState(this);
	}

	protected BDIVehicle getContainingVehicle() {
		return containingVehicle;
	}

	protected boolean isPickingUp() {
		return getState().isPickedUp() || getState() == ParcelState.PICKING_UP;
	}

	protected boolean isDelivering() {
		return getState().isDelivered() || getState() == ParcelState.DELIVERING;
	}

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

	protected void cancelTimers() {
		for (Timer timer : timers) {
			timer.cancel();
		}
		timers.clear();
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
		timers.removeAll(toRemove);
	}

	/*
	 * Communication
	 */

	protected void transmit(AbstractMessage<? extends Packet> message) {
		message.transmit(commAPI);
	}

	// CommunicationUser

	@Override
	public void receive(Message message) {
		mailbox.receive(message);
	}

	@Override
	public void setCommunicationAPI(CommunicationAPI api) {
		this.commAPI = api;
	}

	@Override
	public Point getPosition() {
		if (isDelivering()) {
			return getDestination();
		} else if (isPickingUp()) {
			return getContainingVehicle().getPosition();
		} else {
			return getRoadModel().getPosition(this);
		}
	}

	public abstract Amount<Length> getRadiusAmount();

	@Override
	public final double getRadius() {
		return getRadiusAmount().doubleValue(getRoadModel().getDistanceUnit());
	}

	protected void initialize() {
		cancelTimers();

		getPDPModel().getEventAPI().addListener(new Listener() {
			@Override
			public void handleEvent(Event e) {
				PDPModelEvent pdpEvent = (PDPModelEvent) e;
				if (BDIParcel.this.equals(pdpEvent.parcel)) {
					containingVehicle = (BDIVehicle) pdpEvent.vehicle;
				}
			}
		}, PDPModelEventType.START_PICKUP);
	}

	@Override
	public final void initRoadPDP(RoadModel pRoadModel, PDPModel pPdpModel) {
		initialize();
	}

	// SimulatorUser

	protected final Unit<Duration> getTickUnit() {
		return simAPI.getTimeUnit();
	}

	@Override
	public void setSimulator(SimulatorAPI api) {
		simAPI = api;
	}

}