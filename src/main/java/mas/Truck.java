package mas;

import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

import javax.measure.quantity.Length;
import javax.measure.quantity.Velocity;

import mas.message.NewPacket;
import mas.message.Reminder;
import mas.message.TruckMessage;
import mas.message.TruckMessageVisitor;

import org.jscience.physics.amount.Amount;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.communication.CommunicationUser;
import rinde.sim.core.model.communication.Message;

public class Truck extends BDIVehicle implements CommunicationUser {

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

	public Truck(Point startPosition, SimulationSettings settings) {
		this.settings = settings;
		setStartPosition(startPosition);
	}

	// Vehicle

	@Override
	public Amount<Velocity> getSpeedAmount() {
		return settings.getTruckSpeed();
	}

	// CommunicationUser

	@Override
	public Amount<Length> getRadiusAmount() {
		return settings.getCommunicationRadius();
	}

	@Override
	public double getReliability() {
		return settings.getCommunicationReliability();
	}

	@Override
	protected boolean updateBeliefs(Queue<Message> messages) {
		MessageHandler handler = new MessageHandler();
		for (Message message : messages) {
			((TruckMessage) message).accept(handler);
		}
		return handler.shouldReconsider();
	}

	private class MessageHandler implements TruckMessageVisitor {

		private boolean packagesLost = false;

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
				// TODO beslissen of we dit in de pool steken of niet: enkel
				// nieuwe
				// packages overwegen/niet?
				desires.add(packet);
				newDesires.remove(packet);
			}
		}

		public boolean shouldReconsider() {
			// TODO Complete?
			return packagesLost;
		}

	}

	@Override
	protected boolean isSucceeded() {
//		// Succeeded if all packets delivered
//		for (Packet packet : intentions) {
//			// TODO Niet vragen aan pakket! Haal uit beliefs.
//			if (!packet.getState().isDelivered()) {
//				return false;
//			}
//		}
		return true;
	}

	@Override
	protected boolean isImpossible() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected Plan reconsider() {
		// TODO Auto-generated method stub
		return null;
	}

	protected Plan nearestNeighbour() {
		// TODO Auto-generated method stub
		return null;
	}

}
