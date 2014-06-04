package mas;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.measure.quantity.Duration;
import javax.measure.quantity.Length;
import javax.measure.quantity.Velocity;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import mas.experiment.Experiment;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.jscience.physics.amount.Amount;

import rinde.sim.core.Simulator;
import rinde.sim.core.graph.Graph;
import rinde.sim.core.graph.MultiAttributeData;
import rinde.sim.core.model.communication.CommunicationModel;
import rinde.sim.core.model.pdp.DefaultPDPModel;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.pdp.twpolicy.TardyAllowedPolicy;
import rinde.sim.core.model.pdp.twpolicy.TimeWindowPolicy;
import rinde.sim.core.model.road.GraphRoadModel;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.examples.core.taxi.TaxiExample;
import rinde.sim.serializers.DotGraphSerializer;
import rinde.sim.serializers.SelfCycleFilter;

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
			3d, SI.KILOMETER);

	/**
	 * Communication reliability, between 0.0 and 1.0.
	 */
	private static final double COMMUNICATION_RELIABILITY = 0.75d;

	/**
	 * Time window policy.
	 */
	private static final TimeWindowPolicy POLICY = new TardyAllowedPolicy();

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
		run();
	}

	public static void run() {
		// initialize a random generator which we use throughout this
		// 'experiment'
		final RandomGenerator rnd = new MersenneTwister(123);

		// One meter on the map corresponds to meterFactor coordinate units
		final double meterFactor = 10d;

		// DIRTY FIX: CommunicationModel uses raw Point distances
		// which do not correspond to actual distances on the map
		final Amount<Length> commRadius = COMMUNICATION_RADIUS
				.times(meterFactor);

		final SimulationSettings settings = SimulationSettings.builder()
				.setTickDuration(TICK_DURATION).setTruckSpeed(VEHICLE_SPEED)
				.setCommunicationRadius(commRadius)
				.setCommunicationReliability(COMMUNICATION_RELIABILITY)
				.setPacketBroadcastPeriod(PACKET_BROADCAST_PERIOD)
				.setTruckReconsiderTimeout(TRUCK_RECONSIDER_TIMEOUT).build();

		// initialize a new Simulator instance
		final Simulator sim = new Simulator(rnd, settings.getTickMeasure());

		RoadModel roadModel = new GraphRoadModel(loadGraph(MAP_FILE),
				SI.METER.times(meterFactor), NonSI.KILOMETERS_PER_HOUR);
		PDPModel pdpModel = new DefaultPDPModel(POLICY);
		CommunicationModel commModel = new CommunicationModel(rnd);
		sim.register(roadModel);
		sim.register(pdpModel);
		sim.register(commModel);

		// configure the simulator, once configured we can no longer change the
		// configuration (i.e. add new models) but we can start adding objects
		sim.configure();

		Experiment experiment = new Experiment(sim, 3, 10, settings,
				Amount.valueOf(6, NonSI.HOUR));
		experiment.enableUI();
		experiment.start();
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
