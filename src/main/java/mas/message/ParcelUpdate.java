package mas.message;

import rinde.sim.core.model.communication.CommunicationUser;
import rinde.sim.core.model.communication.Message;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.pdp.Vehicle;

public class ParcelUpdate extends Message {

	private final Parcel parcel;
	private final Vehicle deliveringTruck;
	private final long deliveryTime;

	public ParcelUpdate(CommunicationUser sender, Parcel parcel,
			Vehicle deliveringTruck, long deliveryTime) {
		super(sender);
		this.parcel = parcel;
		this.deliveringTruck = deliveringTruck;
		this.deliveryTime = deliveryTime;
	}

	public Parcel getParcel() {
		return parcel;
	}

	public Vehicle getDeliveringTruck() {
		return deliveringTruck;
	}

	public long getDeliveryTime() {
		return deliveryTime;
	}

}
