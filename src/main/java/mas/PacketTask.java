package mas;

import java.util.Comparator;

import rinde.sim.core.model.pdp.Parcel;

import com.google.common.primitives.Doubles;

public class PacketTask {

	private final boolean isDelivery;
	private final Parcel packet;
	private final double weight;

	public PacketTask(boolean isDelivery, Parcel packet) {
		this(isDelivery, packet, 0d);
	}

	public PacketTask(boolean isDelivery, Parcel packet, double distance) {
		this.packet = packet;
		this.weight = distance;
		this.isDelivery = isDelivery;
	}

	public boolean isDelivery() {
		return isDelivery;
	}

	public boolean isPickup() {
		return !isDelivery();
	}

	public Parcel getPacket() {
		return packet;
	}

	public double getDistance() {
		return weight;
	}

	public static final Comparator<PacketTask> DISTANCE_COMPARATOR = new Comparator<PacketTask>() {
		@Override
		public int compare(PacketTask left, PacketTask right) {
			return Doubles.compare(left.getDistance(), right.getDistance());
		}
	};

}