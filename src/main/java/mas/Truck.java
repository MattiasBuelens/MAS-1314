package mas;

import org.apache.commons.math3.random.RandomGenerator;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.communication.CommunicationUser;

public class Truck extends BDIVehicle implements CommunicationUser {

	private final SimulationSettings settings;

	public Truck(Point startPosition, SimulationSettings settings) {
		this.settings = settings;
		setStartPosition(startPosition);
	}

	protected RandomGenerator getRandomGenerator() {
		return settings.getRandomGenerator();
	}

	// Vehicle

	@Override
	public double getSpeed() {
		return settings.getTruckSpeed(getRoadModel().getSpeedUnit());
	}

	// CommunicationUser

	@Override
	public double getRadius() {
		return settings
				.getCommunicationRadius(getRoadModel().getDistanceUnit());
	}

	@Override
	public double getReliability() {
		return settings.getCommunicationReliability();
	}

}
