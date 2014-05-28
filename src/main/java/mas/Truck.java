package mas;

import java.util.Queue;

import mas.message.NewPacket;
import mas.message.PacketPing;
import mas.message.Reminder;
import mas.message.TruckMessage;
import mas.message.TruckMessageVisitor;

import org.apache.commons.math3.random.RandomGenerator;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.communication.CommunicationUser;
import rinde.sim.core.model.communication.Message;

public class Truck extends BDIVehicle implements CommunicationUser,
		TruckMessageVisitor {

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
		for (Message message : messages) {
			((TruckMessage) message).accept(this);
		}
		// TODO Return if should reconsider
		return false;
	}

	@Override
	public void visitNewPacket(NewPacket newPackage) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitReminder(Reminder reminder) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitPacketPing(PacketPing ping) {
		// TODO Auto-generated method stub

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
