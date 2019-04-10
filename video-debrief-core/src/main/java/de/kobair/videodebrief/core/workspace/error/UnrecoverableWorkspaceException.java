package de.kobair.videodebrief.core.workspace.error;

public class UnrecoverableWorkspaceException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;
	
	private final WorkspaceException rollbackReason;
	
	public UnrecoverableWorkspaceException(Exception cause, WorkspaceException rollbackReason) {
		super(cause);
		this.rollbackReason = rollbackReason;
	}
	
	public WorkspaceException getRollbackReason() {
		return rollbackReason;
	}
}
