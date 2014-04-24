package mas;

import javax.measure.Measure;
import javax.measure.quantity.Length;
import javax.measure.quantity.Velocity;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

public class SimulationSettings {

	private final Measure<? extends Number, Velocity> truckSpeed;
	private final Measure<? extends Number, Length> communicationRadius;
	private final double communicationReliability;

	private SimulationSettings(Builder builder) {
		this.truckSpeed = builder.truckSpeed;
		this.communicationRadius = builder.communicationRadius;
		this.communicationReliability = builder.communicationReliability;
	}

	/**
	 * Get the speed of a truck.
	 */
	public double getTruckSpeed(Unit<Velocity> unit) {
		return truckSpeed.doubleValue(unit);
	}

	/**
	 * Get the communication range of an agent.
	 */
	public double getCommunicationRadius(Unit<Length> unit) {
		return communicationRadius.doubleValue(unit);
	}

	/**
	 * Get the communication reliability of an agent, as a value between 0.0 and
	 * 1.0.
	 */
	public double getCommunicationReliability() {
		return communicationReliability;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private Measure<? extends Number, Velocity> truckSpeed = Measure
				.valueOf(50d, NonSI.KILOMETRES_PER_HOUR);
		private Measure<? extends Number, Length> communicationRadius = Measure
				.valueOf(10d, SI.KILOMETRE);
		private double communicationReliability = 1.0d;

		protected Builder() {
		}

		public Builder setTruckSpeed(Measure<? extends Number, Velocity> speed) {
			this.truckSpeed = speed;
			return this;
		}

		public Builder setCommunicationRadius(
				Measure<? extends Number, Length> radius) {
			this.communicationRadius = radius;
			return this;
		}

		public Builder setCommunicationReliability(double reliability) {
			this.communicationReliability = reliability;
			return this;
		}

		public SimulationSettings build() {
			return new SimulationSettings(this);
		}

	}

}
