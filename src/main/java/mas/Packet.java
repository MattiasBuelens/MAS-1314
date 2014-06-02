package mas;

import java.util.Queue;

import javax.measure.quantity.Length;

import mas.message.AbstractMessage;
import mas.message.NewPacket;
import mas.message.PacketMessage;
import mas.message.PacketMessageVisitor;
import mas.message.Proposal;
import mas.message.Reminder;
import mas.timer.Timer;
import mas.timer.TimerCallback;

import org.jscience.physics.amount.Amount;

import rinde.sim.core.SimulatorUser;
import rinde.sim.core.TickListener;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.communication.CommunicationUser;
import rinde.sim.core.model.communication.Message;
import rinde.sim.core.model.pdp.PDPModel.ParcelState;
import rinde.sim.core.model.pdp.Vehicle;
import rinde.sim.util.TimeWindow;

public class Packet extends BDIParcel implements CommunicationUser,
		TickListener, PacketMessageVisitor, SimulatorUser {

	private final SimulationSettings settings;

	// Beliefs
	private Vehicle deliveringVehicle;
	private long deliveryTime;

	public Packet(Point pDestination, long pPickupDuration,
			TimeWindow pickupTW, long pDeliveryDuration, TimeWindow deliveryTW,
			double pMagnitude, SimulationSettings settings) {
		super(pDestination, pPickupDuration, pickupTW, pDeliveryDuration,
				deliveryTW, pMagnitude);
		this.settings = settings;
	}

	protected Vehicle getDeliveringVehicle() {
		if (getState().isDelivered()) {
			// Already delivered
			return null;
		} else if (isPickingUp()) {
			// Picked up
			return getContainingVehicle();
		} else {
			// Awaiting pickup
			return deliveringVehicle;
		}
	}

	protected long getDeliveryTime() {
		return deliveryTime;
	}

	protected boolean needsAssignment() {
		return !isPickingUp() && getDeliveringVehicle() == null;
	}

	protected boolean isPickingUp() {
		return getState().isPickedUp() || getState() == ParcelState.PICKING_UP;
	}

	@Override
	protected void updateBeliefs(Queue<Message> messages) {
		for (Message message : messages) {
			((PacketMessage) message).accept(this);
		}
	}

	@Override
	public void visitProposal(Proposal proposal) {
		// Ignore if already (being) picked up
		if (isPickingUp())
			return;

		// Set delivering vehicle if better time
		if (proposal.getDeliveryTime() < getDeliveryTime()) {
			deliveringVehicle = proposal.getSender();
			deliveryTime = proposal.getDeliveryTime();
		}
	}

	@Override
	protected void initialize() {
		super.initialize();
		// Schedule broadcast timer
		addTimer(0l, new BroadcastUpdateCallback());
	}

	protected void broadcastUpdate() {
		AbstractMessage<Packet> update;
		if (needsAssignment()) {
			// Request assignment
			update = new NewPacket(this);
		} else {
			// Remind commitment
			PacketInfo info = new PacketInfo(this, getState(),
					getDeliveringVehicle(), getDeliveryTime());
			update = new Reminder(this, info);
		}
		transmit(update);
	}

	private class BroadcastUpdateCallback implements TimerCallback {
		@Override
		public void onTimer(Timer timer, long currentTime) {
			// Send update
			broadcastUpdate();
			// Repeat timer
			timer.schedule(currentTime + getBroadcastPeriod());
		}
	}

	@Override
	public Amount<Length> getRadiusAmount() {
		return settings.getCommunicationRadius();
	}

	@Override
	public double getReliability() {
		return settings.getCommunicationReliability();
	}

	protected final long getBroadcastPeriod() {
		return settings.getPacketBroadcastPeriod().longValue(getTickUnit());
	}

}
