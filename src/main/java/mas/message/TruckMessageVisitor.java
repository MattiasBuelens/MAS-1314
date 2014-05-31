package mas.message;

public interface TruckMessageVisitor {

	public void visitNewPacket(NewPacket newPacket);

	public void visitReminder(Reminder reminder);

}
