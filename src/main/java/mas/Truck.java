package mas;

import static com.google.common.base.Preconditions.checkState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import javax.measure.quantity.Length;
import javax.measure.quantity.Velocity;

import mas.action.DeliverAction;
import mas.action.IllegalActionException;
import mas.action.MoveAction;
import mas.action.PickupAction;
import mas.action.SimulationState;
import mas.action.WaitAction;
import mas.message.NewPacket;
import mas.message.Reminder;
import mas.message.TruckMessage;
import mas.message.TruckMessageVisitor;

import org.jscience.physics.amount.Amount;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.communication.CommunicationUser;
import rinde.sim.core.model.communication.Message;
import rinde.sim.core.model.pdp.Parcel;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;

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
	protected boolean updateBeliefs(Queue<Message> messages, long time) {
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
				// nieuwe packages overwegen/niet?
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
	protected Plan reconsider(long time) {
		// Initialize from actual truck state
		SimulationState startState = new SimulationState(time, getPosition(),
				getContainedPackets());

		// Make plan with current intentions
		PlanBuilder plan = nearestNeighbour(startState,
				ImmutableSet.copyOf(intentions));
		checkState(plan != null, "Truck has no plan for its current intentions");

		// Add intentions from new packages
		PlanBuilder newPlan = selectIntentions(startState, newDesires);
		if (newPlan != null) {
			plan = newPlan;
		}

		// Add intentions from old packages
		// TODO When should we do this?
		newPlan = selectIntentions(startState, desires);
		if (newPlan != null) {
			plan = newPlan;
		}

		return plan.build();
	}

	private PlanBuilder selectIntentions(SimulationState startState,
			Set<Packet> desires) {
		PlanBuilder bestPlan = null;

		// TODO Adjust the stop condition?
		while (!desires.isEmpty()) {
			List<Packet> impossibleDesires = new ArrayList<>();
			Packet bestDesire = null;
			long bestTotalTime = Long.MAX_VALUE;

			// Make plans for all desires
			for (Packet desire : desires) {
				// Make a plan with the current intentions and this desire
				ImmutableSet<Packet> newIntentions = ImmutableSet
						.<Packet> builder().addAll(intentions).add(desire)
						.build();
				PlanBuilder newPlan = nearestNeighbour(startState,
						newIntentions);
				if (newPlan == null) {
					// No possible plan with this desire
					// Mark for removal
					impossibleDesires.add(desire);
				} else {
					// Check if better total time
					long totalTime = newPlan.getState().getTime();
					if (totalTime < bestTotalTime) {
						bestDesire = desire;
						bestTotalTime = totalTime;
						bestPlan = newPlan;
					}
				}
			}

			// Remove impossible desires
			desires.removeAll(impossibleDesires);

			if (bestDesire == null) {
				// No additional desire can be added
				break;
			} else {
				// Add to intentions
				desires.remove(bestDesire);
				intentions.add(bestDesire);
			}
		}

		return bestPlan;
	}

	private PlanBuilder nearestNeighbour(SimulationState startState,
			ImmutableSet<? extends Parcel> packets) {
		return nearestNeighbour(new PlanBuilder(startState), packets);
	}

	private PlanBuilder nearestNeighbour(PlanBuilder plan,
			ImmutableSet<? extends Parcel> packets) {
		List<PacketTask> tasks = getNextTasks(plan.getState(), packets);

		while (!tasks.isEmpty()) {
			// First, try without waiting
			PlanBuilder newPlan = planFirstTask(plan, tasks, false);
			// If no result, try with waiting
			if (newPlan == null) {
				newPlan = planFirstTask(plan, tasks, true);
			}
			if (newPlan == null) {
				// No valid new plan
				return null;
			}
			// Get new tasks from new plan
			plan = newPlan;
			tasks = getNextTasks(plan.getState(), packets);
		}

		return plan;
	}

	private List<PacketTask> getNextTasks(SimulationState state,
			Set<? extends Parcel> pickUps) {
		List<PacketTask> tasks = new ArrayList<>();
		for (Parcel packet : pickUps) {
			if (state.isPickedUp(packet))
				continue;
			// Pick up at position
			Point position = getRoadModel().getPosition(packet);
			double distance = Point.distance(state.getPosition(), position);
			tasks.add(new PacketTask(false, packet, distance));
		}
		for (Parcel packet : state.getPickedUp()) {
			// Deliver at destination
			Point destination = packet.getDestination();
			double distance = Point.distance(state.getPosition(), destination);
			tasks.add(new PacketTask(true, packet, distance));
		}
		// Sort by distance
		Collections.sort(tasks, PacketTask.DISTANCE_COMPARATOR);
		return tasks;
	}

	private PlanBuilder planFirstTask(PlanBuilder plan, List<PacketTask> tasks,
			boolean allowWait) {
		for (PacketTask task : tasks) {
			try {
				return planTask(plan, task, allowWait);
			} catch (IllegalActionException e) {
				// No plan, continue with next task
			}
		}
		return null;
	}

	private PlanBuilder planTask(PlanBuilder plan, PacketTask task,
			boolean allowWait) throws IllegalActionException {
		if (task.isDelivery()) {
			return planDelivery(plan, task.getPacket(), allowWait);
		} else {
			return planPickup(plan, task.getPacket(), allowWait);
		}
	}

	private PlanBuilder planPickup(PlanBuilder plan, Parcel packet,
			boolean allowWait) throws IllegalActionException {
		Point pickupPosition = getRoadModel().getPosition(packet);
		if (!plan.getState().getPosition().equals(pickupPosition)) {
			// Move to packet position
			plan = plan.nextAction(this, new MoveAction(pickupPosition));
		}
		if (!canPickupAt(packet, plan.getState().getTime()) && allowWait) {
			// Wait for pickup begin time
			plan = plan.nextAction(this,
					new WaitAction(packet.getPickupTimeWindow().begin));
		}
		// Pick up
		plan = plan.nextAction(this, new PickupAction(packet));
		return plan;
	}

	private PlanBuilder planDelivery(PlanBuilder plan, Parcel packet,
			boolean allowWait) throws IllegalActionException {
		if (!plan.getState().getPosition().equals(packet.getDestination())) {
			// Move to packet destination
			plan = plan.nextAction(this,
					new MoveAction(packet.getDestination()));
		}
		if (!canDeliverAt(packet, plan.getState().getTime()) && allowWait) {
			// Wait for delivery begin time
			plan = plan.nextAction(this,
					new WaitAction(packet.getDeliveryTimeWindow().begin));
		}
		// Deliver
		plan = plan.nextAction(this, new DeliverAction(packet));
		return plan;
	}

	private long getPlannedDeliveryTime(PlanBuilder plan, final Parcel packet) {
		PlanBuilder deliveryPlan = plan
				.findOldest(new Predicate<PlanBuilder>() {
					@Override
					public boolean apply(PlanBuilder input) {
						return input.getState().isDelivered(packet);
					}
				});
		return deliveryPlan.getState().getTime();
	}

}
