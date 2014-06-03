package mas;

import static com.google.common.base.Preconditions.checkState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import javax.measure.quantity.Length;
import javax.measure.quantity.Velocity;

import mas.action.ActionFailedException;
import mas.action.DeliverAction;
import mas.action.DeliverFailedException;
import mas.action.IllegalActionException;
import mas.action.MoveAction;
import mas.action.PickupAction;
import mas.action.PickupFailedException;
import mas.action.SimulationState;
import mas.action.WaitAction;
import mas.message.NewPacket;
import mas.message.Proposal;
import mas.message.Reminder;
import mas.message.TruckMessage;
import mas.message.TruckMessageVisitor;

import org.jscience.physics.amount.Amount;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.communication.CommunicationUser;
import rinde.sim.core.model.communication.Message;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

public class Truck extends BDIVehicle implements CommunicationUser,
		TruckMessageVisitor {

	private final SimulationSettings settings;

	/**
	 * Information received from packets.
	 */
	private Map<Packet, PacketInfo> packetInfo = new HashMap<>();

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

	/**
	 * Proposed delivery times for planned intentions.
	 * 
	 * Used to verify received packet messages and re-send proposals.
	 */
	private Map<Packet, Long> plannedDeliveryTimes = new HashMap<>();

	/**
	 * Flag indicating if any packets were dropped from the intentions.
	 * 
	 * If set, reconsider the intentions and plan.
	 */
	private boolean packagesLost = false;

	public Truck(Point startPosition, SimulationSettings settings) {
		this.settings = settings;
		setStartPosition(startPosition);
	}

	protected ImmutableSet<Packet> getContainedPackets() {
		return ImmutableSet.copyOf(Iterables.filter(getContainedParcels(),
				Packet.class));
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
		packagesLost = false;

		for (Message message : messages) {
			((TruckMessage) message).accept(this);
		}

		return shouldReconsider();
	}

	@Override
	public void visitNewPacket(NewPacket newPacket) {
		// Newly introduced packet
		Packet packet = newPacket.getPacket();
		// Might be an obsolete message
		if (!(desires.contains(packet) || intentions.contains(packet))) {
			newDesires.add(packet);
		}
		if (intentions.contains(packet)) {
			// Packet not yet picked up but we intend to do so
			// Packet did not receive our proposal, re-send
			long proposedDeliveryTime = plannedDeliveryTimes.get(packet);
			transmit(new Proposal(this, packet, proposedDeliveryTime));
		}
	}

	@Override
	public void visitReminder(Reminder reminder) {
		// Reminder from packet
		Packet packet = reminder.getSender();
		PacketInfo info = reminder.getInfo();
		// Store updated info
		packetInfo.put(packet, info);
		if (info.isPickingUp()) {
			// Packet already picked up
			if (this.equals(info.getDeliveringTruck())) {
				// We are picking it up, no problem
			} else {
				// Another truck picked it up, forget about package
				desires.remove(packet);
				if (intentions.contains(packet)) {
					// Packet stolen by another truck
					intentions.remove(packet);
					packagesLost = true;
				}
			}
		} else if (intentions.contains(packet)) {
			// Packet not yet picked up but we intend to do so
			long proposedDeliveryTime = plannedDeliveryTimes.get(packet);
			if (this.equals(info.getDeliveringTruck())) {
				// Packet has chosen us to pick it up
				if (proposedDeliveryTime != info.getDeliveryTime()) {
					// Packet did not receive our current proposal, re-send
					transmit(new Proposal(this, packet, proposedDeliveryTime));
				}
			} else {
				// Packet has not chosen us to pick it up
				if (proposedDeliveryTime < info.getDeliveryTime()) {
					// We have a better proposal, re-send
					transmit(new Proposal(this, packet, proposedDeliveryTime));
				} else {
					// Packet has a better proposal from another truck
					intentions.remove(packet);
					packagesLost = true;
				}
			}
		} else {
			// Packet not yet picked up and we don't yet intend to do so
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

	@Override
	protected boolean isSucceeded() {
		return true;
	}

	@Override
	protected boolean isImpossible() {
		return false;
	}

	@Override
	protected void handleActionFailed(ActionFailedException e) {
		if (e instanceof PickupFailedException) {
			// Pick up failed, drop intention
			Packet packet = ((PickupFailedException) e).getPacket();
			intentions.remove(packet);
		} else if (e instanceof DeliverFailedException) {
			// Delivery failed, drop intention
			Packet packet = ((DeliverFailedException) e).getPacket();
			intentions.remove(packet);
		}
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

		// Update and send proposals to all intentions
		updateProposals(plan);
		sendProposals();

		// Build the plan
		return plan.build();
	}

	private PlanBuilder selectIntentions(SimulationState startState,
			Set<Packet> desires) {
		PlanBuilder bestPlan = null;

		// Remove desires that are already intentions
		desires.removeAll(intentions);

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
			ImmutableSet<? extends Packet> packets) {
		return nearestNeighbour(new PlanBuilder(startState), packets);
	}

	private PlanBuilder nearestNeighbour(PlanBuilder plan,
			ImmutableSet<? extends Packet> packets) {
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
			Set<? extends Packet> pickUps) {
		List<PacketTask> tasks = new ArrayList<>();
		for (Packet packet : pickUps) {
			if (state.isPickedUp(packet))
				continue;
			// Pick up at position
			Point position = packet.getPosition();
			double distance = Point.distance(state.getPosition(), position);
			tasks.add(new PacketTask(false, packet, distance));
		}
		for (Packet packet : state.getPickedUp()) {
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

	private PlanBuilder planPickup(PlanBuilder plan, Packet packet,
			boolean allowWait) throws IllegalActionException {
		// Move to packet position
		plan = plan.nextAction(this, new MoveAction(packet.getPosition()));
		// Wait for pickup begin time
		if (allowWait) {
			plan = plan.nextAction(this,
					new WaitAction(packet.getPickupTimeWindow().begin));
		}
		// Pick up
		plan = plan.nextAction(this, new PickupAction(packet));
		return plan;
	}

	private PlanBuilder planDelivery(PlanBuilder plan, Packet packet,
			boolean allowWait) throws IllegalActionException {
		// Move to packet destination
		plan = plan.nextAction(this, new MoveAction(packet.getDestination()));
		// Wait for delivery begin time
		if (allowWait) {
			plan = plan.nextAction(this,
					new WaitAction(packet.getDeliveryTimeWindow().begin));
		}
		// Deliver
		plan = plan.nextAction(this, new DeliverAction(packet));
		return plan;
	}

	private void updateProposals(PlanBuilder plan) {
		plannedDeliveryTimes.clear();
		for (Packet packet : plan.getState().getDelivered()) {
			long deliveryTime = getPlannedDeliveryTime(plan, packet);
			// Store proposed delivery time for re-sending
			plannedDeliveryTimes.put(packet, deliveryTime);
		}
	}

	private void sendProposals() {
		for (Packet packet : intentions) {
			long deliveryTime = plannedDeliveryTimes.get(packet);
			transmit(new Proposal(this, packet, deliveryTime));
		}
	}

	private long getPlannedDeliveryTime(PlanBuilder plan, final Packet packet) {
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
