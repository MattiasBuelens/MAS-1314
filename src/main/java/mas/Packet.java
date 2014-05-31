package mas;

import java.util.Queue;

import mas.message.AbstractMessage;
import mas.message.PacketMessage;
import mas.message.PacketMessageVisitor;
import mas.message.Reminder;
import mas.message.Proposal;
import rinde.sim.core.TickListener;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.communication.CommunicationAPI;
import rinde.sim.core.model.communication.CommunicationUser;
import rinde.sim.core.model.communication.Mailbox;
import rinde.sim.core.model.communication.Message;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.pdp.PDPModel.ParcelState;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.pdp.Vehicle;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.util.TimeWindow;

public class Packet extends Parcel implements CommunicationUser, TickListener,
		PacketMessageVisitor {

	private final SimulationSettings settings;

	private CommunicationAPI commAPI;
	private final Mailbox mailbox = new Mailbox();

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

	@Override
	public void tick(TimeLapse timeLapse) {
		// Read messages
		Queue<Message> messages = mailbox.getMessages();

		// Update beliefs
		updateBeliefs(messages);

		// Broadcast update
		// TODO Move to timer?
		Reminder update = new Reminder(this, getState(), getDeliveringVehicle(), getDeliveryTime());
		transmit(update);
	}

	@Override
	public void afterTick(TimeLapse timeLapse) {
	}

	private void updateBeliefs(Queue<Message> messages) {
		for (Message message : messages) {
			((PacketMessage) message).accept(this);
		}
	}

	@Override
	public void visitProposal(Proposal proposal) {
		// TODO Auto-generated method stub

	}

	protected ParcelState getState() {
		return getPDPModel().getParcelState(this);
	}
	
	protected Vehicle getDeliveringVehicle() {
		return deliveringVehicle;
	}

	protected long getDeliveryTime() {
		return deliveryTime;
	}


	/*
	 * Communication
	 */

	protected void transmit(AbstractMessage<? extends Packet> message) {
		message.transmit(commAPI);
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
		return settings
				.getCommunicationRadius(getRoadModel().getDistanceUnit());
	}

	@Override
	public double getReliability() {
		return settings.getCommunicationReliability();
	}

	@Override
	public void initRoadPDP(RoadModel pRoadModel, PDPModel pPdpModel) {
		// TODO
	}

}
