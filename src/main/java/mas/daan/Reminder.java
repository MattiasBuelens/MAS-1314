package mas.daan;

public class Reminder extends PackageMessage {

	public Reminder(Package sender) {
		super(sender);
	}

	public void accept(MessageVisitor visitor) {
		visitor.visitReminder(this);
	}

}
