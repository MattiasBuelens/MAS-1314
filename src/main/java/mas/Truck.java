package mas;

import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

import mas.message.NewPacket;
import mas.message.Reminder;
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

	/**
	 * Packets this truck may consider to pick up and deliver.
	 */
	private Set<Packet> desires = new HashSet<>();

	/**
	 * New packets this truck may consider to pick up and deliver.
	 */
	private Set<Packet> newDesires = new HashSet<>();

	/**
	 * Packets this truck intents to pick up and deliver.
	 */
	private Set<Packet> intentions = new HashSet<>();

	private boolean packagesLost = false;

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
		packagesLost = false;
		for (Message message : messages) {
			((TruckMessage) message).accept(this);
		}
		// TODO Return if should reconsider
		return packagesLost;
	}

	@Override
	public void visitNewPacket(NewPacket newPacket) {
		// Newly introduced packet
		Packet packet = newPacket.getPacket();
		// Might be an obsolete message
		if (!(desires.contains(packet) || intentions.contains(packet))) {
			newDesires.add(packet);
		}
	}

	@Override
	public void visitReminder(Reminder reminder) {
		// Commitment reminder from packet
		// TODO waarom werkt dit niet?
		Packet packet = reminder.getSender();
		if (intentions.contains(packet)) {
			if (!this.equals(reminder.getDeliveringTruck())) {
				intentions.remove(packet);
				packagesLost = true;
			}
		} else {
			// Add to desires
			// TODO beslissen of we dit in de pool steken of niet: enkel nieuwe
			// packages overwegen/niet?
			desires.add(packet);
			newDesires.remove(packet);
		}
	}

	@Override
	protected boolean isSucceeded() {
		// Succeeded if all packets
		for (Packet packet : intentions) {
			// TODO Niet vragen aan pakket! Haal uit beliefs.
			if (!packet.getState().isDelivered()) {
				return false;
			}
		}
		return true;
	}

	@Override
	protected boolean isImpossible() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected Plan<BDIVehicle> reconsider() {
		// TODO Auto-generated method stub
		return null;
	}

}
