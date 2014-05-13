package mas;

import java.util.Queue;

import mas.message.ParcelUpdate;
import rinde.sim.core.TickListener;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.communication.CommunicationAPI;
import rinde.sim.core.model.communication.CommunicationUser;
import rinde.sim.core.model.communication.Mailbox;
import rinde.sim.core.model.communication.Message;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.pdp.Vehicle;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.util.TimeWindow;

public class Package extends Parcel implements CommunicationUser, TickListener {

	private final SimulationSettings settings;

	private CommunicationAPI commAPI;
	private final Mailbox mailbox = new Mailbox();

	// Beliefs
	private Vehicle deliveringTruck;
	private long deliveryTime;

	public Package(Point pDestination, long pPickupDuration,
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

		// TODO Update beliefs

		// Broadcast update
		ParcelUpdate update = new ParcelUpdate(this, this, deliveringTruck,
				deliveryTime);
		commAPI.broadcast(update);
	}

	@Override
	public void afterTick(TimeLapse timeLapse) {
		// TODO Auto-generated method stub

	}

	@Override
	public void initRoadPDP(RoadModel pRoadModel, PDPModel pPdpModel) {
		// TODO
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

}
