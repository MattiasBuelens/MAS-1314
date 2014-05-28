package mas;

import java.util.Queue;

import org.apache.commons.math3.random.RandomGenerator;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.communication.CommunicationUser;
import rinde.sim.core.model.communication.Message;

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

	@Override
	protected boolean updateBeliefs(Queue<Message> messages) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected boolean isSucceeded() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected boolean isImpossible() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected boolean shouldReconsider() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected Plan<BDIVehicle> reconsider() {
		// TODO Auto-generated method stub
		return null;
	}

}
