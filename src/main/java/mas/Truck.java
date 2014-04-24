package mas;

import java.util.Queue;

import javax.annotation.Nullable;

import org.apache.commons.math3.random.RandomGenerator;

import rinde.sim.core.TimeLapse;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.communication.CommunicationAPI;
import rinde.sim.core.model.communication.CommunicationUser;
import rinde.sim.core.model.communication.Mailbox;
import rinde.sim.core.model.communication.Message;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.pdp.Vehicle;
import rinde.sim.core.model.road.RoadModel;

public class Truck extends Vehicle implements CommunicationUser {

	private final RandomGenerator rng;
	private final SimulationSettings settings;

	private CommunicationAPI commAPI;
	private Mailbox mailbox = new Mailbox();

	@Nullable
	private Point destination;

	public Truck(RandomGenerator rng, Point startPosition,
			SimulationSettings settings) {
		this.rng = rng;
		this.settings = settings;
		setStartPosition(startPosition);
	}

	@Override
	protected void tickImpl(TimeLapse time) {
		// Read messages
		Queue<Message> messages = mailbox.getMessages();

		if (destination == null
				|| destination.equals(getRoadModel().getPosition(this))) {
			destination = getRoadModel().getRandomPosition(rng);
		}
		getRoadModel().moveTo(this, destination, time);
	}

	@Override
	public void initRoadPDP(RoadModel pRoadModel, PDPModel pPdpModel) {
		// TODO
	}
	
	// Vehicle

	@Override
	public double getSpeed() {
		return settings.getTruckSpeed(getRoadModel().getSpeedUnit());
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
		return getRoadModel().getPosition(this);
	}

	@Override
	public double getRadius() {
		return settings.getCommunicationRadius(getRoadModel().getDistanceUnit());
	}

	@Override
	public double getReliability() {
		return settings.getCommunicationReliability();
	}

}
