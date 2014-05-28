package mas.message;

public interface TruckMessageVisitor {

	public void visitNewPacket(NewPacket newPackage);

	public void visitReminder(Reminder reminder);

	public void visitPacketPing(PacketPing ping);

}
