package mas;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.measure.quantity.Duration;
import javax.measure.quantity.Length;
import javax.measure.quantity.Velocity;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

public class SimulationSettings {

	private final Amount<Velocity> truckSpeed;

	private final Amount<Length> communicationRadius;
	private final double communicationReliability;

	private final Amount<Duration> packetBroadcastPeriod;
	private final Amount<Duration> truckReconsiderTimeout;

	private SimulationSettings(Builder builder) {
		this.truckSpeed = checkNotNull(builder.truckSpeed);
		this.communicationRadius = checkNotNull(builder.communicationRadius);
		this.communicationReliability = builder.communicationReliability;
		this.packetBroadcastPeriod = checkNotNull(builder.packetBroadcastPeriod);
		this.truckReconsiderTimeout = checkNotNull(builder.truckReconsiderTimeout);
	}

	/**
	 * Get the speed of a truck.
	 */
	public Amount<Velocity> getTruckSpeed() {
		return truckSpeed;
	}

	/**
	 * Get the communication range of an agent.
	 */
	public Amount<Length> getCommunicationRadius() {
		return communicationRadius;
	}

	/**
	 * Get the communication reliability of an agent, as a value between 0.0 and
	 * 1.0.
	 */
	public double getCommunicationReliability() {
		return communicationReliability;
	}

	/**
	 * Get the broadcast period for a packet.
	 */
	public Amount<Duration> getPacketBroadcastPeriod() {
		return packetBroadcastPeriod;
	}

	/**
	 * Get the delay for a truck to re-consider a packet after it failed to add
	 * it to its plan.
	 */
	public Amount<Duration> getTruckReconsiderTimeout() {
		return truckReconsiderTimeout;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private Amount<Velocity> truckSpeed = Amount.valueOf(50d,
				NonSI.KILOMETRES_PER_HOUR);
		private Amount<Length> communicationRadius = Amount.valueOf(1d,
				SI.KILOMETRE);
		private double communicationReliability = 1.0d;
		private Amount<Duration> packetBroadcastPeriod = Amount.valueOf(5d,
				SI.SECOND);
		private Amount<Duration> truckReconsiderTimeout = Amount.valueOf(180l,
				SI.SECOND);

		protected Builder() {
		}

		public Builder setTruckSpeed(Amount<Velocity> speed) {
			this.truckSpeed = speed;
			return this;
		}

		public Builder setCommunicationRadius(Amount<Length> radius) {
			this.communicationRadius = radius;
			return this;
		}

		public Builder setCommunicationReliability(double reliability) {
			this.communicationReliability = reliability;
			return this;
		}

		public Builder setPacketBroadcastPeriod(Amount<Duration> period) {
			this.packetBroadcastPeriod = period;
			return this;
		}

		public Builder setTruckReconsiderTimeout(Amount<Duration> delay) {
			this.truckReconsiderTimeout = delay;
			return this;
		}

		public SimulationSettings build() {
			return new SimulationSettings(this);
		}

	}

}
