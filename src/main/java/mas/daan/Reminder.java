package mas.daan;

import mas.Packet;

public class Reminder extends PackageMessage {

	public Reminder(Packet sender) {
		super(sender);
	}

	public void accept(MessageVisitor visitor) {
		visitor.visitReminder(this);
	}

}
