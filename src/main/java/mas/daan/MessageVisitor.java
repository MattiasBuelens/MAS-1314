package mas.daan;

public interface MessageVisitor {

	public void visitNewPackage(NewPackage newPackage);

	public void visitProposal(Proposal proposal);

	public void visitReminder(Reminder reminder);

}
