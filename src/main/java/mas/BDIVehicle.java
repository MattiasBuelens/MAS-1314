package mas;

import java.util.Queue;

import javax.annotation.Nullable;

import rinde.sim.core.TimeLapse;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.communication.CommunicationAPI;
import rinde.sim.core.model.communication.CommunicationUser;
import rinde.sim.core.model.communication.Mailbox;
import rinde.sim.core.model.communication.Message;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.pdp.Vehicle;
import rinde.sim.core.model.road.MoveProgress;
import rinde.sim.core.model.road.RoadModel;

public abstract class BDIVehicle extends Vehicle implements CommunicationUser {

	private CommunicationAPI commAPI;
	private final Mailbox mailbox = new Mailbox();

	@Nullable
	private Plan<BDIVehicle> plan;

	@Override
	protected void tickImpl(TimeLapse time) {
		// Read messages
		Queue<Message> messages = mailbox.getMessages();

		// TODO Update beliefs

		do {
			if (!hasPlan() || shouldReconsider()) {
				// Update desires
				
				// Update intentions
				
				// Update plan
				plan = createPlan();
			} else if (hasPlan() && !isSound(getPlan())) {
				// Update plan
				plan = createPlan();
			}
			if (hasPlan()) {
				getPlan().step(this, time);
			}
		} while (hasPlan() && time.hasTimeLeft());
	}

	protected boolean hasPlan() {
		return getPlan() != null && !getPlan().isEmpty();
	}

	protected Plan<BDIVehicle> getPlan() {
		return plan;
	}

	protected abstract boolean shouldReconsider();
	
	protected abstract Plan<BDIVehicle> createPlan();

	protected abstract boolean isSound(Plan<BDIVehicle> plan);

	@Override
	public void initRoadPDP(RoadModel pRoadModel, PDPModel pPdpModel) {
		// TODO
	}

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

	public MoveProgress moveTo(Point destination, TimeLapse time) {
		return getRoadModel().moveTo(this, destination, time);
	}

	public void pickup(Parcel parcel, TimeLapse time) {
		getPDPModel().pickup(this, parcel, time);
	}

	public void deliver(Parcel parcel, TimeLapse time) {
		getPDPModel().deliver(this, parcel, time);
	}

	public boolean containsPacket(Parcel parcel) {
		return getPDPModel().containerContains(this, parcel);
	}

}