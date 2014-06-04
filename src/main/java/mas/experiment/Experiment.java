package mas.experiment;

import javax.measure.quantity.Duration;
import javax.measure.unit.NonSI;

import mas.Packet;
import mas.SimulationSettings;
import mas.Truck;
import mas.ui.SimulatorUI;

import org.jscience.physics.amount.Amount;

import rinde.sim.core.Simulator;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.scenario.Scenario;
import rinde.sim.scenario.ScenarioBuilder;
import rinde.sim.scenario.ScenarioController;
import rinde.sim.scenario.TimedEvent;
import rinde.sim.scenario.TimedEventHandler;
import rinde.sim.util.TimeWindow;

public class Experiment implements TimedEventHandler {

	private final Simulator sim;
	private final int nbTrucks;
	private final int nbPackets;
	private final Amount<Duration> duration;
	private final SimulationSettings settings;

	private Scenario scenario;
	private ScenarioController controller;

	private static final Amount<Duration> MAX_PICKUP_DELAY = Amount.valueOf(30,
			NonSI.MINUTE);
	private static final Amount<Duration> MAX_PICKUP_SPAN = Amount.valueOf(30,
			NonSI.MINUTE);
	private static final Amount<Duration> MAX_PICKUP_DURATION = Amount.valueOf(
			10, NonSI.MINUTE);
	private static final Amount<Duration> MAX_DELIVERY_DELAY = Amount.valueOf(
			90, NonSI.MINUTE);
	private static final Amount<Duration> MAX_DELIVERY_SPAN = Amount.valueOf(
			30, NonSI.MINUTE);
	private static final Amount<Duration> MAX_DELIVERY_DURATION = Amount
			.valueOf(10, NonSI.MINUTE);

	private static final Amount<Duration> MAX_PACKET_TIME = MAX_PICKUP_DELAY
			.plus(MAX_PICKUP_SPAN).plus(MAX_DELIVERY_DELAY)
			.plus(MAX_DELIVERY_SPAN);

	public Experiment(Simulator sim, int nbTrucks, int nbPackets,
			SimulationSettings settings, Amount<Duration> duration) {
		this.sim = sim;
		this.nbTrucks = nbTrucks;
		this.nbPackets = nbPackets;
		this.settings = settings;
		this.duration = duration;
		int nbTicks = (int) duration.longValue(sim.getTimeUnit());

		createTrucks();
		scenario = buildScenario();
		controller = new ScenarioController(scenario, sim, this, nbTicks);
	}
	
	public void enableUI() {
		controller.enableUI(new SimulatorUI());
	}

	public void start() {
		controller.start();
	}

	public void stop() {
		controller.stop();
	}

	private void createTrucks() {
		for (int i = 0; i < nbTrucks; i++) {
			Point startPosition = getRandomPosition();
			Truck truck = new Truck(startPosition, settings);
			sim.register(truck);
		}
	}

	private Scenario buildScenario() {
		ScenarioBuilder builder = new ScenarioBuilder(EventType.NEW_PACKET);

		for (int i = 0; i < nbPackets; i++) {
			Point startPosition = getRandomPosition();
			Point destination = getRandomPosition();
			long createTime = getRandomTime(duration.minus(MAX_PACKET_TIME));
			long pickupStart = getRandomTime(createTime, MAX_PICKUP_DELAY);
			long pickupEnd = getRandomTime(pickupStart, MAX_PICKUP_SPAN);
			long pickupDuration = getRandomTime(MAX_PICKUP_DURATION);
			long deliveryStart = getRandomTime(pickupEnd, MAX_DELIVERY_DELAY);
			long deliveryEnd = getRandomTime(deliveryStart, MAX_DELIVERY_SPAN);
			long deliveryDuration = getRandomTime(MAX_DELIVERY_DURATION);

			TimeWindow pickupTW = new TimeWindow(pickupStart, pickupEnd);
			TimeWindow deliveryTW = new TimeWindow(deliveryStart, deliveryEnd);
			Packet packet = new Packet(startPosition, destination,
					pickupDuration, pickupTW, deliveryDuration, deliveryTW, 1l,
					settings);

			builder.addEvent(new TimedPacketEvent(createTime, packet));
		}

		return builder.build();
	}

	private long getRandom(long max) {
		return (long) (sim.getRandomGenerator().nextDouble() * max);
	}

	private long getRandomTime(Amount<Duration> maxTime) {
		return getRandom(maxTime.longValue(sim.getTimeUnit()));
	}

	private long getRandomTime(long after, long max) {
		return after + getRandom(max);
	}

	private long getRandomTime(long after, Amount<Duration> maxTime) {
		return getRandomTime(after, getTicks(maxTime));
	}

	private long getTicks(Amount<Duration> duration) {
		return duration.longValue(sim.getTimeUnit());
	}

	private Point getRandomPosition() {
		return getRoadModel().getRandomPosition(sim.getRandomGenerator());
	}

	protected RoadModel getRoadModel() {
		return sim.getModelProvider().getModel(RoadModel.class);
	}

	private void addPacket(Packet packet) {
		sim.register(packet);
	}

	@Override
	public boolean handleTimedEvent(TimedEvent event) {
		if (event.getEventType() == EventType.NEW_PACKET) {
			addPacket(((TimedPacketEvent) event).packet);
			return true;
		}
		return false;
	}

	public enum EventType {
		NEW_PACKET
	}

	private class TimedPacketEvent extends TimedEvent {

		private static final long serialVersionUID = 1L;
		
		public final Packet packet;

		public TimedPacketEvent(long timestamp, Packet packet) {
			super(EventType.NEW_PACKET, timestamp);
			this.packet = packet;
		}

	}

}