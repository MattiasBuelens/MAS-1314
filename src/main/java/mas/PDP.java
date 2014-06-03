package mas;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.measure.Measure;
import javax.measure.quantity.Duration;
import javax.measure.quantity.Length;
import javax.measure.quantity.Velocity;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.jscience.physics.amount.Amount;

import rinde.sim.core.Simulator;
import rinde.sim.core.graph.Graph;
import rinde.sim.core.graph.MultiAttributeData;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.communication.CommunicationModel;
import rinde.sim.core.model.pdp.DefaultPDPModel;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.road.GraphRoadModel;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.examples.core.taxi.TaxiExample;
import rinde.sim.serializers.DotGraphSerializer;
import rinde.sim.serializers.SelfCycleFilter;
import rinde.sim.ui.View;
import rinde.sim.ui.renderers.GraphRoadModelRenderer;
import rinde.sim.ui.renderers.RoadUserRenderer;
import rinde.sim.ui.renderers.UiSchema;
import rinde.sim.util.TimeWindow;

public class PDP {

	/**
	 * Speed, in kilometers per hour.
	 */
	private static final Amount<Velocity> VEHICLE_SPEED = Amount.valueOf(50d,
			NonSI.KILOMETERS_PER_HOUR);

	/**
	 * Communication radius, in kilometers.
	 */
	private static final Amount<Length> COMMUNICATION_RADIUS = Amount.valueOf(
			1d, SI.KILOMETER);

	/**
	 * Communication reliability, between 0.0 and 1.0.
	 */
	private static final double COMMUNICATION_RELIABILITY = 0.75d;

	/**
	 * Tick duration, in milliseconds.
	 */
	private static final Amount<Duration> TICK_DURATION = Amount.valueOf(1000l,
			SI.MILLI(SI.SECOND));

	/**
	 * Packet broadcast period, in seconds.
	 */
	private static final Amount<Duration> PACKET_BROADCAST_PERIOD = Amount
			.valueOf(30l, SI.SECOND);

	/**
	 * Truck reconsider delay, in seconds.
	 */
	private static final Amount<Duration> TRUCK_RECONSIDER_TIMEOUT = Amount
			.valueOf(5l, NonSI.MINUTE);

	private static final String MAP_FILE = "/data/maps/leuven-simple.dot";

	private PDP() {
	}

	/**
	 * Starts the example.
	 * 
	 * @param args
	 *            This is ignored.
	 */
	public static void main(String[] args) {
		run(false);
	}

	public static void run(boolean testing) {
		// initialize a random generator which we use throughout this
		// 'experiment'
		final RandomGenerator rnd = new MersenneTwister(123);

		// initialize a new Simulator instance
		final Simulator sim = new Simulator(rnd, Measure.valueOf(
				TICK_DURATION.getExactValue(), TICK_DURATION.getUnit()));

		final SimulationSettings settings = SimulationSettings.builder()
				.setTruckSpeed(VEHICLE_SPEED)
				.setCommunicationRadius(COMMUNICATION_RADIUS)
				.setCommunicationReliability(COMMUNICATION_RELIABILITY)
				.setPacketBroadcastPeriod(PACKET_BROADCAST_PERIOD)
				.setTruckReconsiderTimeout(TRUCK_RECONSIDER_TIMEOUT).build();

		RoadModel roadModel = new GraphRoadModel(loadGraph(MAP_FILE),
				SI.METER, SI.METERS_PER_SECOND);
		PDPModel pdpModel = new DefaultPDPModel();
		// FIXME CommunicationModel radius doesn't seem to work?!
		// TODO Change true back to false when radiuses are fixed
		CommunicationModel commModel = new CommunicationModel(rnd, true);
		sim.register(roadModel);
		sim.register(pdpModel);
		sim.register(commModel);

		// configure the simulator, once configured we can no longer change the
		// configuration (i.e. add new models) but we can start adding objects
		sim.configure();

		// add a number of drivers on the road
		final int numDrivers = 10;
		for (int i = 0; i < numDrivers; i++) {
			// when an object is registered in the simulator it gets
			// automatically 'hooked up' with models that it's interested in. An
			// object declares to be interested in an model by implementing an
			// interface.
			sim.register(new Truck(roadModel.getRandomPosition(rnd), settings));
		}

		// add a number of packets on the road
		sim.register(createPacket(
				// Start
				roadModel.getRandomPosition(rnd),
				// Destination
				roadModel.getRandomPosition(rnd),
				// Pickup
				Amount.valueOf(5d, NonSI.MINUTE),
				Amount.valueOf(0d, NonSI.MINUTE),
				Amount.valueOf(15d, NonSI.MINUTE),
				// Delivery
				Amount.valueOf(10d, NonSI.MINUTE),
				Amount.valueOf(60d, NonSI.MINUTE),
				Amount.valueOf(75d, NonSI.MINUTE), settings));

		sim.register(createPacket(
				// Start
				roadModel.getRandomPosition(rnd),
				// Destination
				roadModel.getRandomPosition(rnd),
				// Pickup
				Amount.valueOf(10d, NonSI.MINUTE),
				Amount.valueOf(20d, NonSI.MINUTE),
				Amount.valueOf(40d, NonSI.MINUTE),
				// Delivery
				Amount.valueOf(5d, NonSI.MINUTE),
				Amount.valueOf(90d, NonSI.MINUTE),
				Amount.valueOf(120d, NonSI.MINUTE), settings));

		final UiSchema uis = new UiSchema();
		uis.add(Truck.class, "/graphics/perspective/empty-truck-32.png");
		uis.add(Packet.class, "/graphics/perspective/deliverypackage.png");

		// initialize the GUI. We use separate renderers for the road model and
		// for the drivers. By default the road model is rendererd as a square
		// (indicating its boundaries), and the drivers are rendererd as red
		// dots.
		final View.Builder viewBuilder = View.create(sim).with(
				new GraphRoadModelRenderer(), new RoadUserRenderer(uis, false));

		if (testing) {
			viewBuilder.setSpeedUp(16).enableAutoClose().enableAutoPlay()
					.stopSimulatorAtTime(10 * 60 * 1000);
		}

		viewBuilder.show();
		// in case a GUI is not desired, the simulation can simply be run by
		// calling the start method of the simulator.
	}

	private static Packet createPacket(Point pStartPosition,
			Point pDestination, Amount<Duration> pickupDuration,
			Amount<Duration> pickupStart, Amount<Duration> pickupEnd,
			Amount<Duration> deliveryDuration, Amount<Duration> deliveryStart,
			Amount<Duration> deliveryEnd, SimulationSettings settings) {
		final Unit<Duration> tickUnit = TICK_DURATION.getUnit();
		long pPickupDuration = pickupDuration.longValue(tickUnit);
		final TimeWindow pickupTW = new TimeWindow(
				pickupStart.longValue(tickUnit), pickupEnd.longValue(tickUnit));
		long pDeliveryDuration = deliveryDuration.longValue(tickUnit);
		final TimeWindow deliveryTW = new TimeWindow(
				deliveryStart.longValue(tickUnit),
				deliveryEnd.longValue(tickUnit));
		return new Packet(pStartPosition, pDestination, pPickupDuration,
				pickupTW, pDeliveryDuration, deliveryTW, 0l, settings);
	}

	// load the graph file
	private static Graph<MultiAttributeData> loadGraph(String name) {
		try {
			final Graph<MultiAttributeData> g = DotGraphSerializer
					.getMultiAttributeGraphSerializer(new SelfCycleFilter())
					.read(TaxiExample.class.getResourceAsStream(name));
			return g;
		} catch (final FileNotFoundException e) {
			throw new IllegalStateException(e);
		} catch (final IOException e) {
			throw new IllegalStateException(e);
		}
	}

}
